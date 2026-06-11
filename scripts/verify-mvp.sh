#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "[1/6] Git status"
git status --short

echo
echo "[2/6] Version check"
grep -R "0.2.0-alpha.4-SNAPSHOT" -n \
  pom.xml javacup-*/pom.xml \
  javacup-core/src/main/java/io/cutalab/javacup/core/AppInfo.java

echo
echo "[3/6] Essential UI/workflow checks"
grep -R "Session health\|Open archived reports\|Preview report\|Archive JSON report" -n \
  javacup-dashboard/src/main/java/io/cutalab/javacup/dashboard/views/ExternalProcessMetricsView.java

echo
echo "[4/6] Essential scripts"
test -x scripts/run-dashboard.sh
test -x scripts/run-demo.sh || echo "WARN: scripts/run-demo.sh is missing or not executable"
test -x scripts/verify-mvp.sh

echo
echo "[5/6] Tests"
mvn -q test

echo
echo "[6/6] Package"
mvn -q -DskipTests package

echo
echo "MVP verification completed successfully."
