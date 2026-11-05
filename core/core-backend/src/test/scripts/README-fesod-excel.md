# Fesod Excel adapter regression

The standalone entry is `io.dataease.datasource.provider.FesodExcelRegression`.
It generates synthetic XLSX, XLS and CSV fixtures at runtime, reads them through the
new `io.dataease.excel` adapter and the real `ExcelUtils` caller, and re-opens written
files with POI to verify counts, types, comments and styles. Generated fixtures are
removed afterwards. It never creates a datasource, writes to a database, or uses real
credentials or business data.

Covered checks:

- Formats/sources: XLSX, XLS, CSV; local files and loopback HTTP remote dispatch;
  single and multi-sheet workbooks. Real WPS binaries are not generated (WPS output is
  accepted through the same XLS/XLSX/CSV parser paths, but is not covered by a real
  WPS fixture here).
- Data correctness: intermediate empty columns, empty cells, special characters, long
  numeric text, high-precision numeric values, 1900 and 1904 date systems (the 1904
  fixture sets the workbook `workbookPr/date1904` flag and asserts serial 45292 becomes
  2028-01-02 instead of 2024-01-02), percent, boolean and error cells.
- Business flows: 100-row preview early stop, field type inference (LONG/DOUBLE/TEXT),
  full parse, CSV header/data/row limit, user import through the real
  `ExcelUtils.excelSaveAndParse` caller, append/replace sheet merge through
  `ExcelUtils.mergeSheets`, loopback remote preview and temporary-file cleanup.
- Write round trip: model write through `ExcelIO.writeModel`, POI re-open and checks for
  row count, cell types, header comments and red error postil style, then model read-back;
  template write through `ExcelIO.writeTemplate`.
- Resources and exceptions: stream close on normal and early-stop reads, consecutive
  reads of different workbooks on the same thread, and parse failure surfacing instead
  of returning empty success.
- Not covered here: real SMB/FTP live runs, real WPS files, append/replace database
  persistence, scheduled sync and XPack flows. Keep those samples and implementations
  private where required.

## External sample acceptance (the 31 review attachments)

The same entry can also replay a directory of real sample files against the real user
import caller (`ExcelUtils.excelSaveAndParse`) and assert each expected accept/reject
outcome. Put the sample files in one directory and describe them in a manifest. The
runner refuses sample paths that escape the sample directory and reports
`SAMPLE SUMMARY: <n> manifest entries, <a> accepted, <r> rejected`.

Manifest format (`samples-manifest.json` next to the samples, or set
`FESOD_SAMPLE_MANIFEST`; see `fesod-excel-samples-manifest.example.json`):

```json
[
  {
    "file": "sample-01-local.xlsx",
    "expect": "accept",
    "sheets": 1,
    "fields": 9,
    "previewRows": 1,
    "dataRows": 1,
    "sha256": "optional fixture digest"
  },
  {
    "file": "sample-08-invalid.xlsx",
    "expect": "reject",
    "errorContains": "not a valid workbook"
  }
]
```

For accepted files the runner additionally re-reads the file persisted by
`excelSaveAndParse` and checks the field count and that the persisted row count is at
least the preview row count (set `"verifyPersisted": false` for fixtures whose sparse
headers are legitimately normalized). For rejected files it checks the surfaced error
message when `errorContains` is set. When `sha256` is present the runner verifies the
fixture digest before parsing. When no manifest is present the runner still reports
accept/reject counts for every `.xlsx/.xls/.csv` file, but deterministic expectations
require the manifest.

The 31-file sample package from the review is wired through the committed manifest
`fesod-excel-samples-manifest.json`; run it from the package root:

```sh
java -Xmx1g -cp "$FESOD_TEST_CLASSES:$(cat /tmp/de-fesod-cp.txt)" \
  io.dataease.datasource.provider.FesodExcelRegression \
  --samples /path/to/excel-adapter-regression-20261028 \
  --manifest core/core-backend/src/test/scripts/fesod-excel-samples-manifest.json
```

Expected sample summary: `31 manifest entries, 24 accepted, 7 rejected`. The 31 sample
files themselves are synthetic review attachments and are not committed; only the
manifest and this repeatable runner are part of the source tree.

Prepare the updated SDK/backend classes and runtime dependencies, including Fesod,
POI, Spring, Jackson and Lombok. Do not rely on `mvn test`, because the current
Surefire configuration skips tests.

```sh
# 1. install parent and SDK modules once, if the local repository lacks them
mvn -N install -q
mvn -f sdk/pom.xml install -DskipTests -q

# 2. build the sdk/common dependency classpath
mvn -f sdk/common/pom.xml dependency:build-classpath \
  -Dmdep.outputFile=/tmp/de-sdk-common-cp.txt -DincludeScope=test -q

# 3. put updated backend/sdk classes before packaged jars
{ cat /tmp/de-sdk-common-cp.txt; printf ':%s' core/core-backend/target/classes; \
  for d in $(find sdk -path '*/target/classes' -type d | sort); do printf ':%s' "$d"; done; \
} > /tmp/de-fesod-cp.txt

# 4. compile the updated caller and the regression entry
FESOD_TEST_CLASSES=$(mktemp -d)
javac -proc:none -encoding UTF-8 -cp @/tmp/de-fesod-cp.txt -d "$FESOD_TEST_CLASSES" \
  core/core-backend/src/main/java/io/dataease/datasource/provider/ExcelUtils.java \
  core/core-backend/src/test/java/io/dataease/datasource/provider/FesodExcelRegression.java

# 5. run from the repository root using Java 21
java -Xmx1g -cp "$FESOD_TEST_CLASSES:$(cat /tmp/de-fesod-cp.txt)" \
  io.dataease.datasource.provider.FesodExcelRegression
```

Expected result: every check prints `PASS: ...` and the final line is
`RESULT: all Fesod Excel regression checks passed`.

If `core/core-backend/target/classes` or the `sdk/*/target/classes` directories are
stale, rebuild them in your IDE or with the project build first, then rerun step 3.
Do not commit credentials, downloaded customer data or generated fixtures.
