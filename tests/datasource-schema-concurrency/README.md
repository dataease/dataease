# Calcite 共享 schema 并发回归

使用 Java 21 和已有后端运行 classpath（含已编译 SDK、后端及本地 Calcite、H2、Spring 依赖）：

```sh
JAVA_HOME=/path/to/jdk-21 uv run python tests/datasource-schema-concurrency/run.py \
  --classpath-file /tmp/de-backend-runtime-classpath.txt --attempts 3
```

runner 总是先将当前检出的 `CalciteProvider.java` 和回归源码编译到独立临时目录，并将它放在 classpath 首位，避免旧 `target/classes` 掩盖源码行为。无需 Maven、下载依赖、启动 DataEase 或提供真实数据源配置。测试仅创建 owned 内存 H2 数据库，结束后关闭连接池和连接，删除临时编译输出；每个测试 JVM 受进程超时约束。

回归通过正式 `take()`、`update()`、`getTables()`、`updateDsPoolAfterCheckStatus()`、`delete()`、`fetchTableField()` 和 `fetchResultField()` 调用真实 Calcite/DBCP/JDBC，不替换 Provider 实现或手动对测试操作加锁。覆盖：

- 16 个刷新线程与一个表读取线程共享 64 个 schema alias，断言期限内完成 2,000 次正确表读取且无异常。
- 刷新、状态检查、删除重建与单源字段、跨源字段和结果读取并发；跨源查询连接两个不同内存数据库，核对字段及来自第二库的实际数据。
- 单源物理表字段兼容，以及刷新后跨源读取仍正确。
- 暂停一条已借出连接的单源 SQL，验证同一连接池有余量时第二张图表能返回。刷新该数据源后再放行慢查询，核对新旧连接都返回正确结果。
- 占满 A 池的 4 个连接，确认第 5 个请求已经进入 DBCP 的借用等待，然后在释放 A 的连接前核对 B 图表的真实结果。
- 在 H2 中暂停正在执行的跨源 JOIN，同时核对使用相同数据源的另一条跨源查询、字段查询和单源图表均能返回。刷新参与 JOIN 的数据源时，检查旧池在查询结束前可用，刷新后关闭，新查询读到切换后的数据，第三个数据源仍能取数。
- 未携带数据源清单的跨源查询仍能取数，跨源 SQL 规划失败后仍能刷新并重新查询。

未修复时该竞争可损坏 Calcite schema 的 `TreeMap` 并产生永久循环，测试会在 deadline 输出线程栈后失败并结束 owned JVM。并发缺陷具有概率性，建议运行三次；单次通过不能证明线程安全。

该回归覆盖 Provider 边界，不启动 HTTP、认证、AI 模型或真实厂商数据库。公开 HTTP 工具链和其他数据库的验证应由对应集成环境补充。
