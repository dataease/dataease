# SMB remote Excel regression

The standalone entry is `io.dataease.datasource.provider.SmbFileDownloaderRegression`.
It calls the real remote-file dispatch, downloads a file, and uses the existing Excel/CSV
parser. It never writes to the remote share or a database. Invalid URL tests run offline;
live tests require an existing, non-empty CSV/XLS/XLSX file on a test share.

Prepare the updated SDK/backend classes and their Maven runtime dependencies, including
SMBJ. Set `SMB_TEST_CLASSPATH` to that classpath (put updated classes before packaged jars).
Do not rely on `mvn test`: this repository's Surefire configuration skips tests.

```sh
SMB_TEST_CLASSES=$(mktemp -d)
javac -proc:none -cp "$SMB_TEST_CLASSPATH" -d "$SMB_TEST_CLASSES" \
  core/core-backend/src/test/java/io/dataease/datasource/provider/SmbFileDownloaderRegression.java
java -cp "$SMB_TEST_CLASSES:$SMB_TEST_CLASSPATH" \
  io.dataease.datasource.provider.SmbFileDownloaderRegression
```

For live tests, supply `SMB_TEST_URL`, `SMB_TEST_USER`, `SMB_TEST_PASSWORD`, and optionally
`SMB_TEST_DOMAIN` through the environment. Never commit credentials or put them in reports.
The URL must point to a file, e.g. `smb://server:445/share/folder/data.xlsx`.
Credentials in the URL are rejected. Percent-encode spaces or reserved characters in paths.
Domain authentication uses a separate domain and username via NTLM; Kerberos SSO and SMB1
are outside this change. SMB signing is required and DFS referrals are disabled.

Assertions cover malformed URLs, traversal segments, unsupported file types, missing
username, real download/parse, wrong password, missing file and failed-download cleanup.
Repeat the live run for CSV, XLS and XLSX and with a real AD domain when available.

Implementation notes: SMBJ 0.14.0 is Apache-2.0 licensed. Its Bouncy Castle dependency is
excluded in favor of the existing parent-managed provider; no provider downgrade or second
provider variant is introduced. SMBJ, asn-one and mbassador add about 0.74 MiB of jars.
HTTP/HTTPS/FTP and existing schedules continue using their existing paths.
