#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

mvn -q -pl javacup-demo-apps -am package

java -Xmx256m -jar javacup-demo-apps/target/javacup-demo-apps-0.1.0-SNAPSHOT.jar --mode=burst
