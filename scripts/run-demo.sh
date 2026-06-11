#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-normal}"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

case "$MODE" in
  normal|heap|metaspace)
    ;;
  *)
    echo "Usage: $0 [normal|heap|metaspace]"
    echo
    echo "Modes:"
    echo "  normal     low-memory baseline demo"
    echo "  heap       heap-growth demo"
    echo "  metaspace  metaspace-growth demo"
    exit 1
    ;;
esac

cd "$PROJECT_DIR"

if [ ! -d "javacup-demo-apps/target/classes" ]; then
  echo "Demo classes not found. Building project first..."
  mvn -q -DskipTests package
fi

echo "Starting Javacup memory demo in mode: $MODE"
echo "Press Ctrl+C to stop."
echo

java -cp javacup-demo-apps/target/classes io.cutalab.javacup.demo.MemoryDemoApplication "$MODE"
