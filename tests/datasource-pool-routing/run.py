"""Run initialization routing regressions against compiled backend classes."""
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
# Mockito is used only by this standalone regression, not added to production dependencies.
test_jars = [
    repository / 'org/mockito/mockito-core/5.17.0/mockito-core-5.17.0.jar',
    repository / 'net/bytebuddy/byte-buddy/1.15.11/byte-buddy-1.15.11.jar',
    repository / 'net/bytebuddy/byte-buddy-agent/1.15.11/byte-buddy-agent-1.15.11.jar',
    repository / 'org/objenesis/objenesis/3.3/objenesis-3.3.jar',
]
for jar in test_jars:
    if not jar.exists():
        raise SystemExit(f'Missing regression prerequisite: {jar}')
classpath = os.pathsep.join([
    str(root / 'core/core-backend/target/classes'),
    args.classpath_file.read_text().strip(),
    *map(str, test_jars),
])
with tempfile.TemporaryDirectory(prefix='datasource-pool-routing-') as output:
    subprocess.run(['javac', '-proc:none', '-cp', classpath, '-d', output,
                    str(Path(__file__).with_name('Regression.java'))], check=True)
    result = subprocess.run(['java', '-javaagent:' + str(test_jars[2]), '-cp',
                             output + os.pathsep + classpath, 'Regression'],
                            capture_output=True, text=True)
    if result.returncode:
        print(result.stdout)
        print(result.stderr)
        result.check_returncode()
    print(next(line for line in result.stdout.splitlines() if line.startswith('PASS:')))
