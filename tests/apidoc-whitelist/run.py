"""Compile the current whitelist source in isolation; reuse existing SDK dependencies.

Run from any directory with Python 3 and JDK 21. Requires sdk/common/target/classes
and locally cached Maven dependencies. Does not launch Spring or access a database.
"""
from pathlib import Path
import os
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
repo = Path.home() / '.m2/repository'
jars = [
    repo / 'org/apache/commons/commons-lang3/3.20.0/commons-lang3-3.20.0.jar',
    *[repo / f'org/springframework/{name}/6.2.19/{name}-6.2.19.jar'
      for name in ('spring-core', 'spring-context', 'spring-beans')],
]
ui = repo / 'com/github/xiaoymin/knife4j-openapi3-ui/4.5.0/knife4j-openapi3-ui-4.5.0.jar'
classes = root / 'sdk/common/target/classes'
for path in [classes, ui, *jars]:
    if not path.exists():
        raise SystemExit(f'Missing prerequisite: {path}')
cp = os.pathsep.join(map(str, [classes, *jars]))
with tempfile.TemporaryDirectory(prefix='apidoc-whitelist-') as output:
    subprocess.run(['javac', '-proc:none', '-cp', cp, '-d', output,
                    str(root / 'sdk/common/src/main/java/io/dataease/utils/WhitelistUtils.java'),
                    str(Path(__file__).with_name('Regression.java'))], check=True)
    subprocess.run(['java', '-cp', output + os.pathsep + cp, 'Regression', str(ui)], check=True)
