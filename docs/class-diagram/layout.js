// Reads an ELK graph as JSON on stdin and writes the laid-out graph as JSON to stdout.
const ELK = require("elkjs");

let input = "";
process.stdin.on("data", (chunk) => (input += chunk));
process.stdin.on("end", async () => {
  try {
    const result = await new ELK().layout(JSON.parse(input));
    process.stdout.write(JSON.stringify(result));
  } catch (error) {
    console.error(error);
    process.exit(1);
  }
});
