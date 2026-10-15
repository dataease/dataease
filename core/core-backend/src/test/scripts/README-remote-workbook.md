# Remote workbook size regression

The standalone entry is `io.dataease.datasource.provider.RemoteWorkbookRegression`.
It generates a 100,000-row XLSX with 20 columns and a second sheet. The expanded
worksheet exceeds the previous 32 MiB entry limit and the archive exceeds the
previous 128 MiB total limit. It uses the actual remote HTTP dispatch, workbook
validation, preview and full Excel parser, without creating a datasource or
writing to a database. Generated fixtures and downloaded files are removed.

The remote XLSX guard permits at most 256 MiB per entry and 512 MiB total expanded
data. It still limits compression ratio to 100:1 after a 1 MiB grace size, archive
entries to 10,000 and validation time to 120 seconds. These independent safety
limits are not the compressed file-size setting. HTTP, HTTPS, FTP and SMB all use
this guard. No global POI security settings are modified.

Prepare the existing SDK/backend classes and runtime dependencies, including
SMBJ, EasyExcel, POI and Lombok. Put their classpath in `SMB_TEST_CLASSPATH`.
Run from the repository root using Java 21. Do not rely on `mvn test`, because the
current Surefire configuration skips tests.

```sh
SMB_TEST_CLASSES=$(mktemp -d)
javac -processor 'lombok.launch.AnnotationProcessorHider$AnnotationProcessor' \
  -cp "$SMB_TEST_CLASSPATH" -d "$SMB_TEST_CLASSES" \
  core/core-backend/src/main/java/io/dataease/datasource/provider/ExcelUtils.java \
  core/core-backend/src/main/java/io/dataease/datasource/provider/SmbFileDownloader.java \
  core/core-backend/src/test/java/io/dataease/datasource/provider/RemoteWorkbookRegression.java
java -Xmx1g -cp "$SMB_TEST_CLASSES:$SMB_TEST_CLASSPATH" \
  io.dataease.datasource.provider.RemoteWorkbookRegression
```

Offline checks include a 100-row preview, subsequent-sheet preservation, all
100,000 rows in the full parser, exact size/ratio boundaries, compressed ZIP
bombs, excessive archive entries, interruption and failed-download cleanup.
CSV and XLS are confirmed to bypass the XLSX-specific archive check.

For a real read-only SMB run, supply `SMB_TEST_URL`, `SMB_TEST_USER`,
`SMB_TEST_PASSWORD` and optionally `SMB_TEST_DOMAIN` in the process environment.
Only download, validation, bounded preview and temporary-file cleanup are
asserted; this does not test QA deployment, datasource saving or scheduled sync.
Never put credentials or downloaded customer data in source control or reports.
