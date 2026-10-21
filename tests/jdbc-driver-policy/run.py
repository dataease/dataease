"""Run against current SDK sources and compiled sync classes without a database."""
import os
from pathlib import Path
import subprocess
import tempfile
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[2]
properties = ET.parse(root / 'pom.xml').getroot().find(
    '{http://maven.apache.org/POM/4.0.0}properties'
)


def version(name):
    return properties.find('{http://maven.apache.org/POM/4.0.0}' + name).text


repository = Path(os.environ.get('DE_MAVEN_REPO', str(Path.home() / '.m2/repository')))
paths = [
    root / 'sdk/extensions/extensions-datasource/target/classes',
    root / 'sdk/extensions/extensions-sync/target/classes',
    repository / ('io/dataease/dataease-license-sdk/{0}/dataease-license-sdk-{0}.jar'
                  .format(version('dataease.version'))),
    repository / ('org/apache/commons/commons-lang3/{0}/commons-lang3-{0}.jar'
                  .format(version('commons-lang3.version'))),
    repository / ('com/github/mwiede/jsch/{0}/jsch-{0}.jar'.format(version('jsch.version'))),
]
for path in paths:
    if not path.exists():
        raise SystemExit(f'Missing prerequisite: {path}; compile extensions-sync and its dependencies first')
classpath = os.pathsep.join(map(str, paths))
with tempfile.TemporaryDirectory(prefix='jdbc-driver-policy-') as output:
    sources = [
        root / 'sdk/extensions/extensions-datasource/src/main/java/io/dataease/extensions/datasource/provider/JdbcDriverPolicy.java',
        root / 'core/core-backend/src/main/java/io/dataease/datasource/security/JdbcUrlSecurityPolicy.java',
        Path(__file__).with_name('Regression.java'),
    ]
    subprocess.run(['javac', '-proc:none', '-cp', classpath, '-d', output, *map(str, sources)], check=True)
    subprocess.run(['java', '-cp', output + os.pathsep + classpath, 'Regression'], check=True)
