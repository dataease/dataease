#!/usr/bin/env bash
# Repeatable Fesod Excel regression runner. See README-fesod-excel.md.
set -euo pipefail
cd "$(dirname "$0")/../../../../.."

mvn -N install -q
mvn -f sdk/pom.xml install -DskipTests -q
mvn -f sdk/common/pom.xml dependency:build-classpath \
  -Dmdep.outputFile=/tmp/de-sdk-common-cp.txt -DincludeScope=test -q

{
  cat /tmp/de-sdk-common-cp.txt
  printf ':%s' core/core-backend/target/classes
  for d in $(find sdk -path '*/target/classes' -type d | sort); do
    printf ':%s' "$d"
  done
} > /tmp/de-fesod-cp.txt

FESOD_TEST_CLASSES=$(mktemp -d)
javac -proc:none -encoding UTF-8 -cp @/tmp/de-fesod-cp.txt -d "$FESOD_TEST_CLASSES" \
  core/core-backend/src/main/java/io/dataease/datasource/provider/ExcelUtils.java \
  core/core-backend/src/test/java/io/dataease/datasource/provider/FesodExcelRegression.java

SAMPLE_ARGS=()
if [ -n "${FESOD_SAMPLE_DIR:-}" ]; then
  SAMPLE_ARGS+=(--samples "$FESOD_SAMPLE_DIR")
fi
if [ -n "${FESOD_SAMPLE_MANIFEST:-}" ]; then
  SAMPLE_ARGS+=(--manifest "$FESOD_SAMPLE_MANIFEST")
fi

if [ ${#SAMPLE_ARGS[@]} -gt 0 ]; then
  java -Xmx1g -cp "$FESOD_TEST_CLASSES:$(cat /tmp/de-fesod-cp.txt)" \
    io.dataease.datasource.provider.FesodExcelRegression "${SAMPLE_ARGS[@]}"
else
  java -Xmx1g -cp "$FESOD_TEST_CLASSES:$(cat /tmp/de-fesod-cp.txt)" \
    io.dataease.datasource.provider.FesodExcelRegression
fi
