"""Compile the changed entry points and run isolated permission regressions."""
import argparse
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--classpath-file', type=Path, required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parents[2]
dependencies = args.classpath_file.read_text().strip()
mockito = next((path for path in dependencies.split(':') if Path(path).name.startswith('mockito-core-')), None)
if mockito is None:
    raise SystemExit('Missing installed Mockito dependency')
lombok = Path.home() / '.m2/repository/org/projectlombok/lombok/1.18.46/lombok-1.18.46.jar'
if not lombok.is_file():
    raise SystemExit('Missing installed Lombok dependency')
sources = [root / path for path in (
    'sdk/api/api-base/src/main/java/io/dataease/api/ds/vo/BusiDsRequest.java',
    'sdk/api/api-sync/src/main/java/io/dataease/api/sync/datasource/api/SyncDatasourceImportProvider.java',
    'sdk/api/api-sync/src/main/java/io/dataease/api/sync/datasource/dto/SyncDatasourceImport.java',
    'core/core-backend/src/main/java/io/dataease/resource/RootFolderCreatePermissionManage.java',
    'core/core-backend/src/main/java/io/dataease/datasource/server/DatasourceServer.java',
    'core/core-backend/src/main/java/io/dataease/config/ConfigurationTransport.java',
    'core/core-backend/src/main/java/io/dataease/config/ConfigurationRequestAdvice.java',
    'core/core-backend/src/main/java/io/dataease/config/ConfigurationResponseAdvice.java',
    'sdk/api/api-base/src/main/java/io/dataease/api/ds/DatasourceApi.java',
    'tests/sync-datasource-import/Regression.java',
    'tests/sync-datasource-import/HttpRegression.java',
    'tests/sync-datasource-import/CommunityImportRegression.java',
)]


def run(command):
    result = subprocess.run(command, cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if result.returncode:
        print('\n'.join(result.stdout.splitlines()[-60:]))
        raise SystemExit(result.returncode)
    for line in result.stdout.splitlines():
        if line.startswith('PASS '):
            print(line)


with tempfile.TemporaryDirectory(prefix='dataease-sync-import-') as directory:
    entries = [directory, str(root / 'core/core-backend/target/classes')]
    entries += [str(path) for path in (root / 'sdk').glob('**/target/classes')]
    entries.append(dependencies)
    cp = ':'.join(entries)
    run(['javac', '-parameters', '-J-Xmx1536m', '-cp', cp, '-processorpath', str(lombok),
         '-d', directory, *map(str, sources)])
    print('PASS changed production sources compiled')
    run(['java', '-Xmx768m', '-javaagent:' + mockito, '-cp', cp, 'io.dataease.regression.Regression'])

    run(['java', '-Xmx768m', '-javaagent:' + mockito, '-cp', cp, 'io.dataease.regression.CommunityImportRegression'])
