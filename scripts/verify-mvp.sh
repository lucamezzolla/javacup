#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "[1/8] Git status"
git status --short

echo
echo "[2/8] Version check"
grep -R "0.3.0" -n \
  pom.xml javacup-*/pom.xml \
  javacup-core/src/main/java/io/cutalab/javacup/core/AppInfo.java

if grep -R "SNAPSHOT" -n pom.xml javacup-*/pom.xml javacup-core/src/main/java/io/cutalab/javacup/core/AppInfo.java; then
  echo "ERROR: SNAPSHOT version found in release files."
  exit 1
fi

echo
echo "[3/8] Essential External Metrics workflow checks"
grep -R "Session health\|Open archived reports\|Preview report\|Archive JSON report\|Download JSON report" -n \
  javacup-dashboard/src/main/java/io/cutalab/javacup/dashboard/views/ExternalProcessMetricsView.java

echo
echo "[4/8] Essential report preview checks"
grep -R "Report verdict\|Probe summary\|Trend interpretation\|Diagnostic summary\|Recommended next actions" -n \
  javacup-dashboard/src/main/java/io/cutalab/javacup/dashboard/ExternalMonitoringReportService.java

echo
echo "[5/8] Essential archive/compare checks"
grep -R "Compare\|Archived reports\|Sample quality notes\|jcmd status notes" -n \
  javacup-dashboard/src/main/java/io/cutalab/javacup/dashboard/views/ArchivedReportsView.java

echo
echo "[6/8] Essential scripts"
test -x scripts/run-dashboard.sh
test -x scripts/verify-mvp.sh
if [ -f scripts/run-demo.sh ]; then
  test -x scripts/run-demo.sh
fi

echo
echo "[7/8] Tests"
mvn -q test

echo
echo "[8/8] Package"
mvn -q -DskipTests package

echo
echo "Javacup MVP verification completed successfully."
