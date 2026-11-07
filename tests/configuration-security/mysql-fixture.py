"""Explicitly create/clean a dedicated schema and read-only account on the isolated local test server."""
import argparse
import os
from pathlib import Path
import shlex
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument('operation', choices=['create', 'cleanup', 'engine-check', 'engine-cleanup'])
parser.add_argument('--classpath-file', type=Path, required=True)
parser.add_argument('--env-file', type=Path, default=Path('.vscode/local/backend.env'))
parser.add_argument('--fixture-file', type=Path, default=Path('/private/tmp/de-configuration-mysql-fixture.json'))
args = parser.parse_args()
environment = os.environ.copy()
for line in args.env_file.read_text().splitlines():
    line = line.strip()
    if not line or line.startswith('#') or '=' not in line:
        continue
    key, value = line.split('=', 1)
    if key.strip() not in {'LOCAL_DB_URL', 'LOCAL_DB_USERNAME', 'LOCAL_DB_PASSWORD'}:
        continue
    value = value.strip()
    if value[:1] in ('"', "'") and value[-1:] == value[:1]:
        value = shlex.split(value)[0]
    environment[key.strip()] = value
classpath = args.classpath_file.read_text().strip()
fixture_class = 'EngineFixture' if args.operation.startswith('engine-') else 'LiveMysqlFixture'
with tempfile.TemporaryDirectory(prefix='configuration-mysql-fixture-') as temporary:
    result = subprocess.run(['javac', '-proc:none', '-cp', classpath, '-d', temporary,
                             str(Path(__file__).with_name(fixture_class + '.java'))],
                            capture_output=True, text=True)
    if result.returncode:
        raise SystemExit('MySQL fixture compilation failed')
    result = subprocess.run(['java', '-cp', temporary + os.pathsep + classpath, fixture_class,
                             args.operation, str(args.fixture_file)], env=environment,
                            capture_output=True, text=True)
    if result.returncode:
        # JDBC errors can contain connection details. Keep them out of test output.
        raise SystemExit('MySQL fixture operation failed; check isolated database privileges and fixture ownership')
    print(result.stdout.strip())
