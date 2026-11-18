# 缓存兼容性定向回归

运行前准备现有 Maven 依赖的 classpath 文件，并确保 SDK 已有编译产物。
脚本不会执行 Maven 生命周期、安装依赖、启动应用或连接 Redis/数据库。
仅将本次真实缓存源码和回归入口编译到临时目录，退出后删除临时产物。
classpath 中的目录会被排除，避免加载其他任务的临时编译结果。

```sh
CACHE_DEPENDENCY_CP_FILE=/absolute/path/dependency-classpath.txt python3 tests/cache-compatibility/run.py
```

需要 Java 21、本地 Ehcache 3.10.8、JCache、Spring、Mockito 和 Byte Buddy 等既有依赖。
脚本固定使用已有的 Ehcache 3.10.8 通用 JAR 测试缓存核心，不使用 classpath 中其他版本。
该测试不加载产品 XML：用类型和过期策略相同的 native 配置验证 JCache 桥接，
不证明 Jakarta XML 解析、磁盘持久化恢复或完整应用装配正确。

本地实现覆盖混合 TTL 与顺序互换、覆盖延长/缩短、读取不续期、失效后的存在性、
默认八小时、非正时长的历史回退、空单位、失败写入上下文清理、
预建 native 缓存策略保留、Spring 共享读取与删除、并发首次创建及不同键 TTL、
原子消费、删除，以及未引入全局对象复制。

Redis 用真实实现、模拟传输校验逐键写入参数、非正时长、空单位、默认八小时、
读取不续期命令、双路径失效及 Lua 消费派发；使用真实 JDK 序列化器验证嵌套值往返。
这不等同于真实 Redis 的过期、Lua 原子性、网络或集群集成测试。

追加 `--baseline` 可将 基线提交 `0f92cc7cc0` 的旧本地实现编译到临时目录运行同一回归，
用于确认用例能够检出修复前的混合 TTL 问题，不修改工作区。

本轮兼容范围：仅动态创建缓存的正 TTL 改为逐键生效；
XML 预建缓存、Redis、公共默认时长及非正时长的历史规则保持不变。
因此这不是两种缓存实现的全面语义统一。

## 真实 Redis、产品 XML 与磁盘恢复

```sh
CACHE_DEPENDENCY_CP_FILE=/absolute/path/dependency-classpath.txt \
CACHE_REDIS_SERVER=/absolute/path/redis-server \
CACHE_EHCACHE_JAKARTA_JAR=/absolute/path/ehcache-3.10.8-jakarta.jar \
python3 tests/cache-compatibility/run-integration.py
```

该入口不会自动下载、安装或修改依赖。需要事先提供 Redis 服务端程序、项目指定的
Ehcache 3.10.8 Jakarta 构件，以及已有 JAXB/SDK/API 编译依赖。本次使用官方 Redis
7.2.7 源码在临时目录编译的服务端；脚本为 Redis 7.2 系列编写版本断言。

脚本只启动自己的 Redis 子进程，绑定 `127.0.0.1` 随机端口，禁用 Redis 磁盘保存。
连接后先用 INFO 核对进程 PID，再写入合成数据；结束时只停止自己的子进程，
不连接预先存在的 Redis、不执行 FLUSH，也不加载应用或数据库配置。
若运行环境禁止本地端口绑定，需允许此隔离测试进程使用回环网络。

真实 Redis 覆盖逐键过期、覆盖延长/缩短、读取不续期、默认八小时、
非正时长和空单位兼容、嵌套对象读写隔离、Spring/模板双键删除、
24 个并发消费者的 Lua take 单次消费、失效/缺失消费、身份键隔离，
以及登录有效成员过期、注销黑名单在给定时长内保留。

XML 测试直接加载 `core/core-backend/src/main/resources/ehcache/ehcache.xml`，
不复制或修改 XML。仅把持久化路径指向临时目录，将登录超时属性设置为一分钟。
使用真实 XML 类型、监听器、堆外及磁盘资源配置；覆盖语言、表名、登录、注销缓存
不受传入短 TTL 覆盖，及 Spring 与服务访问的失效一致性。

磁盘恢复分别验证“新实现写入 → 新 JVM 新实现读取”和
“基线旧实现写入 → 新 JVM 新实现读取”。每组再启动第三个 JVM，
在首次写入满 61 秒后核对原登录记录已到期、重启未续期，注销黑名单仍存在，
已删除/消费的磁盘项不恢复。非持久化缓存重启后冷启动是现有 XML 行为。
测试使用正常关闭，不覆盖进程崩溃、磁盘损坏、Redis 集群或完整登录 HTTP 流程。

所有编译、磁盘缓存和 Redis 数据位于独立临时目录；成功或失败退出时清理。
未改变本轮保留的业务有效期和缓存公共规则。

2026-11-08 验证结果：真实 Redis 7.2.7 的 25 项检查通过；产品 XML 加载、
正常关闭后的磁盘恢复及一分钟期限验证，在“当前写入”和“旧版写入后升级”两组中
各通过 31 项，共新增 87 项检查。旧实现取自主仓库提交 `0f92cc7cc0`。
同时重跑本地 41 项、Redis 客户端 13 项及独立权限管理方法 21 项回归，全部通过。
这些结果针对上述隔离场景，不表示已覆盖完整应用登录、业务数据库或 Redis 集群。

可设置 `CACHE_BASELINE_REF` 指定其他兼容性基线；默认固定到修复前提交，避免合并后误用新实现作为旧版。
