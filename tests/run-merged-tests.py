"""Run the merged workflow tests using the existing isolated PostgreSQL test harness."""
import runpy
from pathlib import Path

if __name__ == "__main__":
    harness = runpy.run_path(str(Path(__file__).with_name("run-beneficiary-tests.py")))
    harness["main"]("MergedWorkflowTest", "merged")
