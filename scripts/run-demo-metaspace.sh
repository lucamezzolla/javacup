#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_DIR"

mvn -q -pl javacup-demo-apps -am package

java -XX:MaxMetaspaceSize=96m \
     -jar javacup-demo-apps/target/javacup-demo-apps-0.1.0-SNAPSHOT.jar \
     --mode=metaspace
