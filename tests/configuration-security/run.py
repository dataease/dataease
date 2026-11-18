"""Compile and test changed production code without starting the application or a database."""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('--classpath-file', type=Path, required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parents[2]
repository = Path(os.environ.get('DE_MAVEN_REPO', str(Path.home() / '.m2/repository')))
spring_test = repository / 'org/springframework/spring-test/6.2.19/spring-test-6.2.19.jar'
if not spring_test.exists():
    raise SystemExit('Missing spring-test 6.2.19 regression prerequisite')
entries = [str(root / 'core/core-backend/target/classes')]
entries += [str(path) for path in (root / 'sdk').glob('**/target/classes')]
entries += [args.classpath_file.read_text().strip(), str(spring_test)]
sources = [
    'sdk/api/api-permissions/src/main/java/io/dataease/api/permissions/user/api/CurrentPasswordVerifier.java',
    'sdk/common/src/main/java/io/dataease/utils/SensitiveConfigurationProvider.java',
    'core/core-backend/src/main/java/io/dataease/sensitive/SensitiveServer.java',
    'tests/configuration-security/SensitiveRegression.java',
    'tests/configuration-security/EngineRegression.java',
    'sdk/api/api-base/src/main/java/io/dataease/api/sensitive/SecretType.java',
    'sdk/api/api-base/src/main/java/io/dataease/api/sensitive/SensitiveApi.java',
    'sdk/common/src/main/java/io/dataease/auth/ConfigurationSecurity.java',
    'sdk/api/api-base/src/main/java/io/dataease/api/ds/DatasourceApi.java',
    'sdk/api/api-base/src/main/java/io/dataease/api/ds/EngineApi.java',
    'sdk/api/api-sync/src/main/java/io/dataease/api/sync/datasource/api/SyncDatasourceApi.java',
    'sdk/api/api-sync/src/main/java/io/dataease/api/sync/task/api/TaskApi.java',
    'sdk/common/src/main/java/io/dataease/utils/ConfigurationCredentials.java',
    'sdk/common/src/main/java/io/dataease/utils/RsaUtils.java',
    'sdk/api/api-base/src/main/java/io/dataease/api/ds/vo/BusiDsRequest.java',
    'sdk/api/api-base/src/main/java/io/dataease/api/ds/vo/RemoteExcelRequest.java',
    'core/core-backend/src/main/java/io/dataease/config/ConfigurationTransport.java',
    'core/core-backend/src/main/java/io/dataease/config/ConfigurationRequestAdvice.java',
    'core/core-backend/src/main/java/io/dataease/config/ConfigurationResponseAdvice.java',
    'core/core-backend/src/main/java/io/dataease/home/ConfigurationKeyController.java',
    'core/core-backend/src/main/java/io/dataease/home/RestIndexController.java',
    'core/core-backend/src/main/java/io/dataease/datasource/server/DatasourceServer.java',
    'core/core-backend/src/main/java/io/dataease/datasource/server/EngineServer.java',
    'tests/configuration-security/Regression.java',
]
with tempfile.TemporaryDirectory(prefix='configuration-security-') as temporary:
    classpath = os.pathsep.join([temporary, *entries])
    compile_result = subprocess.run(['javac', '-cp', classpath, '-d', temporary,
                                     *[str(root / source) for source in sources]], capture_output=True, text=True)
    if compile_result.returncode:
        print(compile_result.stderr)
        raise SystemExit(compile_result.returncode)
    result = subprocess.run(['java', '-cp', classpath, 'Regression', str(root), temporary],
                            capture_output=True, text=True)
    if result.returncode:
        print(result.stdout)
        print(result.stderr)
        raise SystemExit(result.returncode)
    print(next(line for line in result.stdout.splitlines() if line.startswith('PASS:')))
