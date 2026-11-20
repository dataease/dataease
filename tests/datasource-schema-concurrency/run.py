"""Compile the current provider and run real Calcite/H2 regressions without Maven or external services."""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--classpath-file', type=Path, required=True,
                        help='Existing backend/SDK classes and locally installed dependency jars')
    parser.add_argument('--attempts', type=int, default=1)
    args = parser.parse_args()
    if not 1 <= args.attempts <= 20:
        parser.error('--attempts must be between 1 and 20')
    root = Path(__file__).resolve().parents[2]
    classpath = args.classpath_file.read_text().strip()
    java_home = os.environ.get('JAVA_HOME')
    java = str(Path(java_home) / 'bin/java') if java_home else 'java'
    javac = str(Path(java_home) / 'bin/javac') if java_home else 'javac'
    with tempfile.TemporaryDirectory(prefix='de-datasource-schema-') as temporary:
        # Never trust an old target/classes CalciteProvider: compile the checked-out source first.
        subprocess.run([javac, '-g', '-proc:none', '-sourcepath', '', '-cp', classpath,
                        '-d', temporary,
                        str(root / 'core/core-backend/src/main/java/io/dataease/datasource/provider/CalciteProvider.java'),
                        str(Path(__file__).with_name('Regression.java'))], check=True)
        for attempt in range(args.attempts):
            print(f'Provider regression attempt {attempt + 1}/{args.attempts}', flush=True)
            result = subprocess.run([java, '-cp', temporary + os.pathsep + classpath, 'Regression'],
                                    capture_output=True, text=True, timeout=60)
            if result.returncode:
                print(result.stdout)
                print(result.stderr)
                raise SystemExit(result.returncode)
            for line in result.stdout.splitlines():
                if line.startswith(('PASS:', 'Provider loaded from:')):
                    print(line)


if __name__ == '__main__':
    main()
