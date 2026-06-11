#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cat <<EOF
Javacup Local Lab

Project:
  $PROJECT_DIR

Recommended workflow:

1) Build:
  cd "$PROJECT_DIR"
  mvn -q -DskipTests package

2) Start dashboard:
  ./scripts/run-dashboard.sh

3) In another terminal, start a demo JVM:
  ./scripts/run-demo.sh normal
  ./scripts/run-demo.sh heap
  ./scripts/run-demo.sh metaspace

4) In the app:
  Dashboard -> Local Lab
  Processes -> select demo JVM
  External Metrics -> collect samples -> generate report
  Archived reports -> compare reports
EOF
