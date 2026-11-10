"""Compile changed AI sources and run isolated regressions; never start DataEase or use external credentials.
Uses the existing CoreApplication.jar and locally installed Maven dependencies. No downloads/build lifecycle.
"""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile
import socket
import time
from contextlib import ExitStack
from zipfile import ZipFile
import xml.etree.ElementTree as ET


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--xpack', action='store_true', help='also compile the two changed private extension sources')
    parser.add_argument('--ai-classpath', default=os.environ.get('AI_SERVICE_DEPENDENCY_CP_FILE'), help='Maven-resolved Spring AI dependency classpath file')
    parser.add_argument('--redis-server', help='existing local Redis binary; start only an owned temporary loopback instance')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[2]
    repository = Path.home() / '.m2/repository'
    application = root / 'core/core-backend/target/CoreApplication.jar'
    by_name = {file.name: file for file in repository.rglob('*.jar') if not file.name.endswith(('-sources.jar', '-javadoc.jar'))}
    with tempfile.TemporaryDirectory(prefix='de-ai-regression-') as temporary:
        work = Path(temporary)
        boot = work / 'boot'
        with ZipFile(application) as archive:
            names = [Path(name).name for name in archive.namelist() if name.startswith('BOOT-INF/lib/') and name.endswith('.jar')]
            for name in archive.namelist():
                if name.startswith('BOOT-INF/classes/') and not name.endswith('/'):
                    target = boot / name.removeprefix('BOOT-INF/classes/')
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_bytes(archive.read(name))
        missing = [name for name in names if name not in by_name and not name.startswith('spring-boot-jarmode-')]
        if missing:
            parser.error('Missing local application dependencies: ' + ', '.join(missing))
        libraries = [by_name[name] for name in names if name in by_name and not name.startswith('spring-ai-')]
        for pattern in ['org/springframework/ai/*/1.1.8/*.jar', 'org/projectlombok/lombok/*/*.jar',
                        'org/apache/httpcomponents/client5/httpclient5/5.5.2/*.jar',
                        'org/apache/httpcomponents/core5/httpcore5/5.3.6/*.jar',
                        'org/apache/httpcomponents/core5/httpcore5-h2/5.3.6/*.jar',
                        'org/springframework/retry/spring-retry/*/*.jar',
                        'org/springframework/spring-webflux/6.2.19/*.jar', 'io/projectreactor/reactor-core/3.7.19/*.jar',
                        'org/antlr/ST4/*/*.jar', 'org/antlr/antlr-runtime/*/*.jar',
                        'com/knuddels/jtokkit/*/*.jar', 'org/reactivestreams/reactive-streams/*/*.jar',
                        'com/h2database/h2/2.2.220/*.jar']:
            libraries.extend(file for file in repository.glob(pattern) if not file.name.endswith(('-sources.jar', '-javadoc.jar')))
        if not list(repository.glob('org/springframework/ai/spring-ai-openai/1.1.8/*.jar')):
            parser.error('Spring AI 1.1.8 dependencies must already be installed')
        if args.ai_classpath:
            resolved = [Path(path) for path in Path(args.ai_classpath).read_text().strip().split(os.pathsep)]
            if not all(path.exists() for path in resolved):
                parser.error('Resolved AI dependency files missing')
            libraries = [*resolved, *libraries]
        # Match the current parent version rather than the old packaged license SDK.
        parent = ET.parse(root / 'pom.xml').getroot()
        version = parent.find('{*}properties/{*}dataease.version').text
        license_sdk = repository / f'io/dataease/dataease-license-sdk/{version}/dataease-license-sdk-{version}.jar'
        if not license_sdk.is_file():
            parser.error('Current license SDK must already be installed')
        libraries = [license_sdk, *[file for file in libraries if not file.name.startswith('dataease-license-sdk-')]]
        classes = work / 'classes'
        classes.mkdir()
        classpath = os.pathsep.join(map(str, [classes, *[path for path in libraries if path.is_dir()], boot, *list((root/'sdk').glob('**/target/classes')), *list((root/'de-xpack').glob('**/target/classes')), *libraries]))
        sources = [file for file in (root/'core/core-backend/src/main/java/io/dataease/ai').rglob('*.java') if file.name != 'AiBaseService.java']
        sources.extend((root/'sdk/api/api-base/src/main/java/io/dataease/api/ai').rglob('*.java'))
        sources.extend(root/path for path in [
            'sdk/common/src/main/java/io/dataease/constant/AuthResourceEnum.java',
            'sdk/common/src/main/java/io/dataease/auth/ConfigurationSecurity.java',
            'sdk/api/api-base/src/main/java/io/dataease/api/ds/DatasourceApi.java',
            'core/core-backend/src/main/java/io/dataease/menu/dao/auto/entity/CoreMenu.java',
            'core/core-backend/src/main/java/io/dataease/system/manage/CorePermissionManage.java',
            'sdk/api/api-permissions/src/main/java/io/dataease/api/permissions/auth/api/InteractiveAuthApi.java',
            'sdk/api/api-permissions/src/main/java/io/dataease/api/permissions/org/api/AiOrganizationLifecycle.java',
            'core/core-backend/src/main/java/io/dataease/listener/sql/AiConfigurationInit.java',
            'core/core-backend/src/main/java/io/dataease/listener/sql/AiKeyringInit.java',
            'core/core-backend/src/main/java/io/dataease/listener/sql/AiConfigurationMenuInit.java',
            'core/core-backend/src/main/java/io/dataease/listener/sql/AiRecordsMenuInit.java',
            'core/core-backend/src/main/java/io/dataease/menu/manage/MenuManage.java',
            'core/core-backend/src/main/java/io/dataease/config/ConfigurationTransport.java'])
        if args.xpack:
            sources.extend(root/path for path in [
                'de-xpack/xpack-permissions/src/main/java/io/dataease/xpack/permissions/auth/server/InteractiveAuthServer.java',
                'de-xpack/xpack-permissions/src/main/java/io/dataease/xpack/permissions/auth/manage/MenuPermissionManage.java',
                'de-xpack/xpack-permissions/src/main/java/io/dataease/xpack/permissions/auth/manage/AuthManage.java',
                'de-xpack/xpack-permissions/src/test/java/io/dataease/xpack/permissions/ai/AiMenuClassificationRegression.java',
                'de-xpack/xpack-permissions/src/main/java/io/dataease/xpack/permissions/org/manage/OrgPageManage.java'])
        sources.extend((root/'tests/ai-service').glob('*.java'))
        java = ['java']
        redis = None
        if args.redis_server:
            with socket.socket() as reserved:
                reserved.bind(('127.0.0.1',0))
                port = reserved.getsockname()[1]
            redis = subprocess.Popen([args.redis_server, '--bind','127.0.0.1','--port',str(port),'--save','','--appendonly','no','--dir',str(work)], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            for attempt in range(100):
                if redis.poll() is not None:
                    parser.error('Owned Redis stopped during startup')
                try:
                    with socket.create_connection(('127.0.0.1',port),timeout=0.1):
                        break
                except OSError:
                    time.sleep(0.05)
            java.append('-Dai.test.redisPort=' + str(port))
        commands = [['javac', '-parameters', '-encoding', 'UTF-8', '-processor', 'lombok.launch.AnnotationProcessorHider$AnnotationProcessor',
                     '-cp', classpath, '-d', str(classes), *map(str, sources)],
                    [*java, '-cp', classpath, 'io.dataease.ai.Regression']]
        if args.xpack:
            commands.append([*java, '-cp', classpath, 'io.dataease.xpack.permissions.ai.AiMenuClassificationRegression'])
        try:
            for command in commands:
                result = subprocess.run(command, cwd=root, capture_output=True, text=True, timeout=120)
                if result.returncode:
                    print('\n'.join((result.stdout + result.stderr).splitlines()[-100:]))
                    return result.returncode
                for line in (result.stdout + result.stderr).splitlines():
                    if line.startswith('PASS '):
                        print(line)
        finally:
            if redis is not None:
                redis.terminate()
                try:
                    redis.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    redis.kill()
                    redis.wait()
        if args.xpack:
            print('PASS changed private extension sources compiled against the new public contract')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
