"""Compile production chart permission code and exercise isolated read entry points."""
import os
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
backend = root / 'core/core-backend'
with tempfile.TemporaryDirectory(prefix='chart-copy-permission-') as output:
    dependencies = Path(output) / 'classpath'
    subprocess.run(['mvn', '-o', '-q', '-f', str(backend / 'pom.xml'),
                    'dependency:build-classpath', f'-Dmdep.outputFile={dependencies}'], check=True)
    classpath = os.pathsep.join([str(backend / 'target/classes'), dependencies.read_text().strip()])
    subprocess.run(['javac', '-proc:none', '-cp', classpath, '-d', output,
                    str(backend / 'src/main/java/io/dataease/chart/manage/ChartViewManege.java'),
                    str(Path(__file__).with_name('Regression.java'))], check=True)
    subprocess.run(['java', '-cp', output + os.pathsep + classpath, 'Regression'], check=True)
