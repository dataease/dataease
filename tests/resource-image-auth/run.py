"""Compile real V2 whitelist sources using cached dependencies; no product services."""
from pathlib import Path
import os
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
repository = Path(os.environ.get('DE_MAVEN_REPO', str(Path.home() / '.m2/repository')))
jars = [repository / 'io/dataease/common/2.10.29/common-2.10.29.jar',
        repository / 'org/apache/commons/commons-lang3/3.20.0/commons-lang3-3.20.0.jar']
for artifact in ['spring-core', 'spring-jcl', 'spring-beans', 'spring-context', 'spring-expression', 'spring-aop']:
    jars.append(repository / f'org/springframework/{artifact}/6.2.19/{artifact}-6.2.19.jar')
for jar in jars:
    if not jar.is_file():
        raise SystemExit(f'Missing cached dependency: {jar}')
classpath = os.pathsep.join(map(str, jars))
with tempfile.TemporaryDirectory(prefix='v2-image-auth-') as output:
    subprocess.run(['javac', '-proc:none', '-cp', classpath, '-d', output,
                    str(root / 'sdk/common/src/main/java/io/dataease/utils/WhitelistUtils.java'),
                    str(Path(__file__).with_name('Regression.java'))], check=True)
    subprocess.run(['java', '-cp', output + os.pathsep + classpath, 'Regression'], check=True)
