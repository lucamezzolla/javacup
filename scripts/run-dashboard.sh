#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

mvn -q -DskipTests install
mvn -pl javacup-dashboard spring-boot:run
