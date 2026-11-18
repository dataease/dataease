"""Compile actual cache sources and run against the pinned local Ehcache, offline."""
import os
import sys
from pathlib import Path
import subprocess
import tempfile

repo = Path(__file__).resolve().parents[2]
cp_file = os.environ.get('CACHE_DEPENDENCY_CP_FILE')
if not cp_file:
    raise SystemExit('Set CACHE_DEPENDENCY_CP_FILE to an existing Maven dependency classpath file')
deps = [p for p in Path(cp_file).read_text().strip().split(os.pathsep)
        if Path(p).suffix == '.jar' and '/org/ehcache/ehcache/' not in p]
m2 = Path.home() / '.m2/repository'
ehcache = m2 / 'org/ehcache/ehcache/3.10.8/ehcache-3.10.8.jar'
deps.insert(0, str(ehcache))
if not all(Path(p).exists() for p in deps):
    raise SystemExit('Required local dependency is missing; no dependencies will be downloaded')
with tempfile.TemporaryDirectory(prefix='cache-compatibility-') as output:
    cp = os.pathsep.join([output, str(repo / 'sdk/common/target/classes'), *deps])
    sources = ['sdk/common/src/main/java/io/dataease/cache/DECacheService.java',
               'sdk/common/src/main/java/io/dataease/cache/impl/DefaultCacheImpl.java',
               'sdk/common/src/main/java/io/dataease/cache/impl/DynamicCacheExpiryPolicy.java',
               'sdk/common/src/main/java/io/dataease/utils/CacheUtils.java',
               'sdk/common/src/main/java/io/dataease/cache/impl/RedisCacheImpl.java',
               'tests/cache-compatibility/Regression.java',
               'tests/cache-compatibility/RedisRegression.java']
    if '--baseline' in sys.argv:
        original = subprocess.run(['git', 'show', os.environ.get('CACHE_BASELINE_REF', '0f92cc7cc0') + ':sdk/common/src/main/java/io/dataease/cache/impl/DefaultCacheImpl.java'], cwd=repo, capture_output=True, text=True, check=True)
        baseline = Path(output) / 'DefaultCacheImpl.java'
        baseline.write_text(original.stdout)
        sources[1] = str(baseline)
    compiled = subprocess.run(['javac', '-proc:none', '-cp', cp, '-d', output, *sources], cwd=repo, capture_output=True, text=True)
    if compiled.returncode:
        raise SystemExit(compiled.stdout + compiled.stderr)
    mockito = next(p for p in deps if '/mockito-core/' in p)
    for entry in ['Regression', 'RedisRegression']:
        result = subprocess.run(['java', '-javaagent:' + mockito, '-cp', cp, 'io.dataease.cache.impl.' + entry], cwd=repo, capture_output=True, text=True)
        if result.returncode:
            raise SystemExit(result.stdout + result.stderr)
        print(result.stdout.strip().splitlines()[-1])
