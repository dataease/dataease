"""Compile real delay queue sources and run isolated regressions without Maven or services."""
import os
from pathlib import Path
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
    'spring-core spring-jcl spring-beans spring-context spring-expression spring-aop')
add('jakarta/annotation', '2.1.1', 'jakarta.annotation-api')
add('org/slf4j', '2.0.18', 'slf4j-api')
classpath = os.pathsep.join(jars)
with tempfile.TemporaryDirectory(prefix='delay-queue-regression-') as output:
    subprocess.run(['javac', '-proc:none', '-cp', classpath, '-d', output,
                    str(root / 'sdk/common/src/main/java/io/dataease/utils/DelayQueueUtils.java'),
                    str(root / 'sdk/common/src/main/java/io/dataease/utils/LogUtil.java'),
                    str(Path(__file__).with_name('Regression.java'))], check=True)
    for arguments in [[], ['interrupted-close']]:
        result = subprocess.run(['java', '-cp', output + os.pathsep + classpath,
                                 'Regression', *arguments],
                                capture_output=True, text=True, timeout=35)
        if result.returncode:
            print(result.stdout)
            print(result.stderr)
            result.check_returncode()
        print(next(line for line in result.stdout.splitlines() if line.startswith('PASS:')))
