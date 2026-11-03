"""Run integration tests in a new temporary PostgreSQL cluster, never the application database."""
import argparse
import os
from pathlib import Path
import shutil
import socket
import subprocess
import tempfile


def main(test_class="BeneficiaryModuleTest", module="beneficiary"):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--gui", action="store_true", help="Also exercise actual Swing windows (requires a desktop)")
    parser.add_argument("--postgres-bin", type=Path, help="Folder containing initdb, pg_ctl, createdb and psql")
    parser.add_argument("--jdbc-jar", type=Path, default=Path.home() / ".m2/repository/org/postgresql/postgresql/42.7.8/postgresql-42.7.8.jar")
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    suffix = ".exe" if os.name == "nt" else ""
    pg = args.postgres_bin
    if pg is None:
        executable = shutil.which("initdb")
        if executable:
            pg = Path(executable).parent
        elif os.name == "nt":
            candidates = sorted((Path(os.environ.get("ProgramFiles", "C:/Program Files")) / "PostgreSQL").glob("*/bin/initdb.exe"))
            if candidates:
                pg = candidates[-1].parent
    if pg is None or not (pg / ("initdb" + suffix)).exists():
        parser.error("PostgreSQL tools not found. Supply --postgres-bin.")
    if not args.jdbc_jar.is_file():
        parser.error("PostgreSQL JDBC jar not found. Run Maven dependency resolution or supply --jdbc-jar.")
    javac, java = shutil.which("javac"), shutil.which("java")
    if not javac or not java:
        parser.error("A JDK supporting Java 17 is required on PATH.")
    work = root / f"target/{module}-verification"
    classes = root / f"target/{module}-classes"
    work.mkdir(parents=True, exist_ok=True)
    classes.mkdir(parents=True, exist_ok=True)

    def run(command, **kwargs):
        # A file avoids Windows child-server pipe handles keeping communicate() open.
        with tempfile.TemporaryFile(mode="w+", encoding="utf-8") as output:
            result = subprocess.run([str(a) for a in command], cwd=root, text=True,
                                    stdout=output, stderr=subprocess.STDOUT,
                                    creationflags=subprocess.CREATE_NO_WINDOW if os.name == "nt" else 0,
                                    **kwargs)
            output.seek(0)
            print(output.read(), end="", flush=True)
        if result.returncode:
            raise RuntimeError("Command failed: " + str(command[0]))

    sources = list((root / "src").rglob("*.java")) + list((root / "tests").glob("*.java"))
    run([javac, "--release", "17", "-d", classes, *sources], timeout=60)
    database = f"foodbridge_{module}_test"
    # All database files are under target. Verify the absolute cleanup boundary.
    with tempfile.TemporaryDirectory(prefix="postgres-", dir=work) as cluster_name:
        cluster = Path(cluster_name).resolve()
        if not cluster.is_relative_to(work.resolve()):
            raise RuntimeError("Temporary cluster escaped the test directory.")
        with socket.socket() as sock:
            sock.bind(("127.0.0.1", 0))
            port = sock.getsockname()[1]
        run([pg / ("initdb" + suffix), "-D", cluster, "-U", "postgres", "-A", "trust",
             "--encoding=UTF8", "--locale=C", "--no-instructions"], timeout=60)
        started = False
        try:
            run([pg / ("pg_ctl" + suffix), "-D", cluster, "-l", work / "postgres.log",
                 "-o", f"-h 127.0.0.1 -p {port} -F", "-w", "start"], timeout=60)
            started = True
            connection = ["-h", "127.0.0.1", "-p", str(port), "-U", "postgres"]
            run([pg / ("createdb" + suffix), *connection, database], timeout=30)
            for filename in ["schema.sql", "seed.sql"]:
                run([pg / ("psql" + suffix), *connection, "-d", database,
                     "-v", "ON_ERROR_STOP=1", "-f", root / "database" / filename], timeout=30)
            env = os.environ.copy()
            env.update(DB_URL=f"jdbc:postgresql://127.0.0.1:{port}/{database}",
                       DB_USER="postgres", DB_PASSWORD="isolated-test-only")
            run([java, "-cp", str(classes) + os.pathsep + str(args.jdbc_jar.resolve()),
                 test_class, *(["--gui"] if args.gui else [])], env=env, timeout=150)
        finally:
            if started:
                run([pg / ("pg_ctl" + suffix), "-D", cluster, "-m", "fast", "-w", "stop"], timeout=60)


if __name__ == "__main__":
    main()
