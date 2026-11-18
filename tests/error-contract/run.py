"""Compile current error-contract sources and run isolated regressions; never start DataEase."""
import argparse
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--classpath-file', type=Path, required=True)
parser.add_argument('--xpack', action='store_true', help='Also compile/test the locally available private module')
args = parser.parse_args()
root = Path(__file__).resolve().parents[2]
repository = Path.home() / '.m2/repository'
lombok = repository / 'org/projectlombok/lombok/1.18.46/lombok-1.18.46.jar'
spring_test = repository / 'org/springframework/spring-test/6.2.19/spring-test-6.2.19.jar'
h2 = repository / 'com/h2database/h2/2.2.220/h2-2.2.220.jar'
for dependency in (lombok, spring_test, h2):
    if not dependency.exists():
        raise SystemExit('Missing regression prerequisite: ' + dependency.name)


def run(command):
    result = subprocess.run(command, cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if result.returncode:
        print('\n'.join(result.stdout.splitlines()[-60:]))
        raise SystemExit(result.returncode)
    for line in result.stdout.splitlines():
        if line.startswith('PASS '):
            print(line)


sources = list((root / 'sdk/common/src/main/java/io/dataease/exception').glob('*.java'))
sources += [root / path for path in (
    'sdk/common/src/main/java/io/dataease/result/ResultMessage.java',
    'sdk/common/src/main/java/io/dataease/result/ResultCode.java',
    'sdk/common/src/main/java/io/dataease/filter/RequestIdFilter.java',
    'sdk/common/src/main/java/io/dataease/filter/HtmlResourceFilter.java',
    'sdk/common/src/main/java/io/dataease/auth/interceptor/CorsConfig.java',
    'sdk/common/src/main/java/io/dataease/utils/RemoteTransfer.java',
    'sdk/common/src/main/java/io/dataease/utils/HttpClientUtil.java',
    'sdk/api/api-base/src/main/java/io/dataease/api/visualization/request/DataVisualizationBaseRequest.java',
    'core/core-backend/src/main/java/io/dataease/visualization/dao/auto/mapper/DataVisualizationInfoRepository.java',
    'core/core-backend/src/main/java/io/dataease/visualization/server/DataVisualizationServer.java',
    'core/core-backend/src/main/java/io/dataease/datasource/server/DatasourceServer.java',
    'core/core-backend/src/main/java/io/dataease/datasource/provider/ExcelUtils.java',
    'core/core-backend/src/main/java/io/dataease/datasource/provider/SmbFileDownloader.java',
)]
tests = ['Regression', 'SaveConflictRegression', 'RemoteFileRegression']
if args.xpack:
    manifest = root / 'de-xpack/tests/error-contract/sources.txt'
    if not manifest.is_file():
        raise SystemExit('Missing private regression source manifest; use the matching XPack revision')
    sources += [root / 'de-xpack' / path.strip() for path in manifest.read_text().splitlines() if path.strip()]
    tests.append('GatewayRegression')
with tempfile.TemporaryDirectory(prefix='dataease-errors-') as directory:
    entries = [directory, str(root / 'core/core-backend/src/main/resources'),
               str(root / 'core/core-backend/target/classes')]
    entries += [str(path) for path in (root / 'sdk').glob('**/target/classes')]
    if args.xpack:
        entries += [str(path) for path in (root / 'de-xpack').glob('*/target/classes')]
    entries += [args.classpath_file.read_text().strip(), str(spring_test), str(h2)]
    cp = ':'.join(entries)
    test_sources = [root / ('de-xpack/tests/error-contract' if name == 'GatewayRegression' else 'tests/error-contract')
                    / (name + '.java') for name in tests]
    run(['javac', '-parameters', '-J-Xmx1536m', '-cp', cp, '-processorpath', str(lombok),
         '-d', directory, *map(str, sources + test_sources)])
    print('PASS changed production sources and regression fixtures compiled')
    for main in tests:
        run(['java', '-Xmx768m', '-cp', cp, 'io.dataease.regression.' + main])
