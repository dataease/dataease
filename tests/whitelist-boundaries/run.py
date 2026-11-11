"""Compile the real whitelist and run isolated matching and SDK Filter regressions.

Uses cached dependencies only; no product application, network, database or Maven.
"""
import os
from pathlib import Path
import re
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
repository = Path(os.environ.get('DE_MAVEN_REPO', str(Path.home() / '.m2/repository')))
jars = []


def add(group, version, artifacts):
    for artifact in artifacts.split():
        jar = repository / group / artifact / version / f'{artifact}-{version}.jar'
        if not jar.is_file():
            raise SystemExit(f'Missing local dependency: {jar}')
        jars.append(str(jar))


add('org/springframework', '6.2.19',
    'spring-core spring-jcl spring-beans spring-context spring-expression spring-aop spring-web spring-test')
add('org/apache/commons', '3.20.0', 'commons-lang3')
add('org/apache/tomcat/embed', '10.1.59', 'tomcat-embed-core')
add('io/micrometer', '1.15.12', 'micrometer-commons')
add('io/dataease', '3.2.1', 'common dataease-license-sdk')
add('com/auth0', '3.12.1', 'java-jwt')
classpath = os.pathsep.join(jars)
ui = repository / 'com/github/xiaoymin/knife4j-openapi3-ui/4.5.0/knife4j-openapi3-ui-4.5.0.jar'
if not ui.is_file():
    raise SystemExit(f'Missing local dependency: {ui}')

# Audit literal mappings in the SDK interfaces of the affected controllers.
# This deliberately avoids importing or packaging any XPack implementation.
api = root / 'sdk/api/api-base/src/main/java/io/dataease/api'
interfaces = {
    '/map': 'map/MapApi.java',
    '/typeface': 'font/api/FontApi.java',
    '/share': 'xpack/share/XpackShareApi.java',
    '/exportCenter': 'exportCenter/ExportCenterApi.java',
    '/xpackComponent': 'xpack/component/XpackComponentApi.java',
}
routes = []
for prefix, filename in interfaces.items():
    source = (api / filename).read_text()
    for path in re.findall(r'@(?:Get|Post|Delete|Put|Patch)Mapping\(\s*"([^"]+)"', source):
        routes.append(prefix + '/' + path.lstrip('/'))

with tempfile.TemporaryDirectory(prefix='whitelist-boundaries-') as output:
    mappings = Path(output) / 'routes.txt'
    mappings.write_text('\n'.join(routes))
    subprocess.run(['javac', '-proc:none', '-cp', classpath, '-d', output,
                    str(root / 'sdk/common/src/main/java/io/dataease/utils/WhitelistUtils.java'),
                    str(root / 'sdk/common/src/main/java/io/dataease/utils/CommonBeanFactory.java'),
                    str(root / 'sdk/common/src/main/java/io/dataease/constant/AuthConstant.java'),
                    str(Path(__file__).with_name('Regression.java'))], check=True)
    result = subprocess.run(['java', '-cp', output + os.pathsep + classpath,
                             'Regression', str(root / 'mapFiles'), str(mappings)],
                            capture_output=True, text=True, timeout=60)
    if result.returncode:
        print(result.stdout)
        print(result.stderr)
        result.check_returncode()
    print(result.stdout.strip())
    # Reuse the existing static-resource regression without requiring Maven target/classes.
    compatibility = Path(output) / 'compatibility'
    compatibility.mkdir()
    subprocess.run(['javac', '-proc:none', '-cp', output + os.pathsep + classpath,
                    '-d', str(compatibility),
                    str(root / 'tests/apidoc-whitelist/Regression.java')], check=True)
    subprocess.run(['java', '-cp', os.pathsep.join([str(compatibility), output, classpath]),
                    'Regression', str(ui)], check=True)
