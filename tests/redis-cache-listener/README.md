# Redis 启动清理回归（TAPD 1077673）

在仓库根目录执行以下命令，需要 Java 21、Python 3 和本地已有的对应 Maven 依赖。
只编译真实监听器、日志工具和回归入口到临时目录，不启动 Spring、不访问 Redis、
不下载依赖，也不使用可能跳过测试的 Maven 生命周期。

```sh
python3 - <<'PY'
import os
from pathlib import Path
import subprocess
import tempfile

repo = Path.cwd()
m2 = Path.home() / '.m2/repository'
jars = []
def add(group, version, artifacts):
    for artifact in artifacts.split():
        path = m2 / group / artifact / version / f'{artifact}-{version}.jar'
        if not path.is_file():
            raise SystemExit(f'Missing local dependency: {path}')
        jars.append(str(path))

add('org/springframework', '6.2.19', 'spring-core spring-jcl spring-beans spring-context spring-expression spring-aop spring-tx spring-oxm')
add('org/springframework/boot', '3.5.15', 'spring-boot spring-boot-autoconfigure')
add('org/springframework/data', '3.5.12', 'spring-data-redis spring-data-commons spring-data-keyvalue')
add('jakarta/annotation', '2.1.1', 'jakarta.annotation-api')
add('org/slf4j', '2.0.18', 'slf4j-api')
add('io/projectreactor', '3.7.19', 'reactor-core')
add('org/reactivestreams', '1.0.4', 'reactive-streams')
classpath = os.pathsep.join(jars)
with tempfile.TemporaryDirectory(prefix='redis-listener-regression-') as output:
    subprocess.run(['javac', '-proc:none', '-cp', classpath, '-d', output,
        str(repo / 'sdk/common/src/main/java/io/dataease/listener/RedisCacheListener.java'),
        str(repo / 'sdk/common/src/main/java/io/dataease/utils/LogUtil.java'),
        str(repo / 'tests/redis-cache-listener/Regression.java')], check=True)
    subprocess.run(['java', '-cp', output + os.pathsep + classpath,
        'io.dataease.listener.Regression'], check=True)
PY
```

覆盖 9 个场景：JDK/字符串/二进制键混存、字符串序列化模板、空扫描、整批、
多批及尾批（含重复键）、扫描初始化异常、迭代异常、删除异常、关闭异常。
校验原始字节不变、单批不超过 1000、复用连接、关闭顺序和未扫描键保留。
同时模拟 Lettuce 的 Cursor 关闭连接行为，确认尾批删除发生在关闭之前。

连接和 Cursor 使用受控替身，因此此测试不证明真实 Redis 的 MATCH/SCAN 行为、
集群跨节点遍历/跨槽删除、拓扑变化或并发启动正确。集成验证须使用隔离 Redis，
覆盖匹配键删除、不匹配键保留和不同槽位/主节点；JCache 条件装配也需另行验证。
当前清理范围仍为 `*de_v3_*`，共享 Redis 的实例启动会清理该范围内缓存。
