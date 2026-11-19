"""Compile the changed entry points and run isolated permission regressions."""
import argparse
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--classpath-file', type=Path, required=True)
parser.add_argument('--compiled-root', type=Path, help='Workspace with matching existing build artifacts')
args = parser.parse_args()
root = Path(__file__).resolve().parents[2]
compiled_root = args.compiled_root or root
dependencies = args.classpath_file.read_text().strip()
mockito = next((path for path in dependencies.split(':') if Path(path).name.startswith('mockito-core-')), None)
if mockito is None:
    raise SystemExit('Missing installed Mockito dependency')
lombok = Path.home() / '.m2/repository/org/projectlombok/lombok/1.18.46/lombok-1.18.46.jar'
if not lombok.is_file():
    raise SystemExit('Missing installed Lombok dependency')
sources = [root / path for path in (
    'sdk/api/api-permissions/src/main/java/io/dataease/api/permissions/auth/api/InteractiveAuthApi.java',
    'sdk/api/api-base/src/main/java/io/dataease/api/ds/DatasourceApi.java',
    'core/core-backend/src/main/java/io/dataease/config/MaterialImportAspect.java',
    'core/core-backend/src/main/java/io/dataease/visualization/server/DataVisualizationServer.java',
    'core/core-backend/src/main/java/io/dataease/resource/RootFolderCreatePermissionManage.java',
    'core/core-backend/src/main/java/io/dataease/datasource/server/DatasourceServer.java',
    'core/core-backend/src/main/java/io/dataease/dataset/server/DatasetTreeServer.java',
    'core/core-backend/src/main/java/io/dataease/substitute/permissions/login/SubstituleLoginServer.java',
    'tests/root-folder-permission/Regression.java',
    'tests/root-folder-permission/CommunityRegression.java',
)]


def run(command):
    result = subprocess.run(command, cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if result.returncode:
        print('\n'.join(result.stdout.splitlines()[-60:]))
        raise SystemExit(result.returncode)
    for line in result.stdout.splitlines():
        if line.startswith('PASS '):
            print(line)


with tempfile.TemporaryDirectory(prefix='dataease-root-folder-') as directory:
    entries = [directory, str(compiled_root / 'core/core-backend/target/core-backend-3.3.0.jar'),
               str(compiled_root / 'core/core-backend/target/classes')]
    entries += [str(path) for path in (compiled_root / 'sdk').glob('**/target/classes')]
    entries.append(dependencies)
    cp = ':'.join(entries)
    run(['javac', '-parameters', '-J-Xmx1536m', '-cp', cp, '-processorpath', str(lombok),
         '-d', directory, *map(str, sources)])
    print('PASS changed production sources compiled')
    run(['java', '-Xmx768m', '-javaagent:' + mockito, '-cp', cp, 'io.dataease.regression.Regression'])
    if any('/xpack-' in path for path in entries):
        raise SystemExit('Community regression requires a classpath without XPack implementation modules')
    run(['java', '-Xmx768m', '-javaagent:' + mockito, '-cp', cp, 'io.dataease.regression.CommunityRegression'])
