"""Run owned Redis and product-XML persistence integration in temporary directories."""
import os
from pathlib import Path
import socket
import subprocess
import tempfile
import time

repo = Path(__file__).resolve().parents[2]
cp_file = os.environ.get('CACHE_DEPENDENCY_CP_FILE')
redis_binary = os.environ.get('CACHE_REDIS_SERVER')
ehcache_jar = os.environ.get('CACHE_EHCACHE_JAKARTA_JAR')
if not all([cp_file, redis_binary, ehcache_jar]):
    raise SystemExit('Set CACHE_DEPENDENCY_CP_FILE, CACHE_REDIS_SERVER and CACHE_EHCACHE_JAKARTA_JAR')
if Path(ehcache_jar).name != 'ehcache-3.10.8-jakarta.jar':
    raise SystemExit('Use the project-pinned ehcache-3.10.8-jakarta.jar')
deps = [p for p in Path(cp_file).read_text().strip().split(os.pathsep)
        if Path(p).suffix == '.jar' and '/org/ehcache/ehcache/' not in p]
deps.insert(0, ehcache_jar)
if not all(Path(p).is_file() for p in [redis_binary, *deps]):
    raise SystemExit('Required local binaries/dependencies missing; this script never downloads or installs them')
version = subprocess.run([redis_binary, '--version'], capture_output=True, text=True, check=True).stdout.strip()
print(version, flush=True)
xml = repo / 'core/core-backend/src/main/resources/ehcache/ehcache.xml'
xml_before = xml.read_bytes()


def run(command, label, cwd=repo):
    result = subprocess.run(command, cwd=cwd, capture_output=True, text=True, timeout=120)
    if result.returncode:
        raise SystemExit(label + '\n' + result.stdout + result.stderr)
    for line in result.stdout.splitlines():
        if line.startswith('PASS '):
            print(label + ': ' + line, flush=True)


with tempfile.TemporaryDirectory(prefix='dataease-cache-integration-') as temporary:
    work = Path(temporary)
    classes = work / 'classes'
    baseline = work / 'baseline'
    classes.mkdir()
    baseline.mkdir()
    modules = ['sdk/common', 'sdk/api/api-base', 'sdk/api/api-permissions']
    cp = os.pathsep.join([str(classes), *[str(repo / module / 'target/classes') for module in modules], *deps])
    sources = ['sdk/common/src/main/java/io/dataease/cache/DECacheService.java',
               'sdk/common/src/main/java/io/dataease/cache/impl/DefaultCacheImpl.java',
               'sdk/common/src/main/java/io/dataease/cache/impl/DynamicCacheExpiryPolicy.java',
               'sdk/common/src/main/java/io/dataease/cache/impl/RedisCacheImpl.java',
               'sdk/common/src/main/java/io/dataease/utils/CacheUtils.java',
               'sdk/common/src/main/java/io/dataease/listener/MyCacheListener.java',
               'tests/cache-compatibility/XmlIntegration.java',
               'tests/cache-compatibility/RedisIntegration.java']
    run(['javac', '-proc:none', '-cp', cp, '-d', str(classes), *sources], 'compile current cache sources')
    original = subprocess.run(['git', 'show', os.environ.get('CACHE_BASELINE_REF', '0f92cc7cc0') + ':sdk/common/src/main/java/io/dataease/cache/impl/DefaultCacheImpl.java'],
                              cwd=repo, capture_output=True, text=True, check=True)
    original_file = baseline / 'DefaultCacheImpl.java'
    original_file.write_text(original.stdout)
    run(['javac', '-proc:none', '-cp', cp, '-d', str(baseline), str(original_file)], 'compile baseline cache source')
    java = ['java', '-Xmx256m', '-XX:MaxDirectMemorySize=512m']
    cases = []
    for name, seed_cp in [('current', cp), ('baseline-upgrade', str(baseline) + os.pathsep + cp)]:
        directory = work / name
        directory.mkdir()
        cases.append((name, directory))
        run([*java, '-cp', seed_cp, 'io.dataease.cache.impl.XmlIntegration', str(xml), str(directory), 'seed'], name)
        run([*java, '-cp', cp, 'io.dataease.cache.impl.XmlIntegration', str(xml), str(directory), 'recover'], name)

    redis_dir = work / 'redis'
    redis_dir.mkdir()
    with socket.socket() as reservation:
        reservation.bind(('127.0.0.1', 0))
        port = reservation.getsockname()[1]
    command = [redis_binary, '--bind', '127.0.0.1', '--port', str(port), '--protected-mode', 'yes',
               '--save', '', '--appendonly', 'no', '--dir', str(redis_dir),
               '--logfile', str(redis_dir / 'redis.log')]
    # Never daemonize, and only terminate this Popen child. No shared Redis cleanup commands.
    with (redis_dir / 'launcher.log').open('w') as output:
        redis = subprocess.Popen(command, stdout=output, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + 10
            while True:
                if redis.poll() is not None:
                    raise SystemExit('Owned Redis failed to start\n' + (redis_dir / 'redis.log').read_text())
                try:
                    with socket.create_connection(('127.0.0.1', port), timeout=0.5) as connection:
                        connection.sendall(b'*1\r\n$4\r\nPING\r\n')
                        if connection.recv(64).startswith(b'+PONG'):
                            break
                except OSError:
                    pass
                if time.monotonic() >= deadline:
                    raise SystemExit('Owned Redis readiness timeout')
                time.sleep(0.1)
            run([*java, '-cp', cp, 'io.dataease.cache.impl.RedisIntegration', str(port), str(redis.pid)], 'isolated Redis')
        finally:
            redis.terminate()
            try:
                redis.wait(timeout=10)
            except subprocess.TimeoutExpired:
                redis.kill()
                redis.wait(timeout=5)
    deadline = max(int((directory / 'written-at').read_text()) for _, directory in cases) / 1000 + 61
    print('Waiting for the original XML one-minute login deadline; no TTL/configuration shortcuts', flush=True)
    while time.time() < deadline:
        time.sleep(max(0, min(0.5, deadline - time.time())))
    for name, directory in cases:
        run([*java, '-cp', cp, 'io.dataease.cache.impl.XmlIntegration', str(xml), str(directory), 'expired'], name)
    if xml.read_bytes() != xml_before:
        raise SystemExit('Product XML changed during regression')
    print('PASS product XML unchanged; owned Redis stopped; temporary data will be removed', flush=True)
