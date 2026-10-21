# JDBC 驱动共享策略回归

在仓库根目录使用 Java 21 和 Python 3 执行：

```sh
mvn -B -pl sdk/extensions/extensions-sync -am -DskipTests compile
python3 tests/jdbc-driver-policy/run.py
```

脚本使用当前 POM 指定版本的本地 Maven 缓存；自定义缓存路径可设置 `DE_MAVEN_REPO`。直接编译当前 SDK 策略及核心委托类源码，调用实际编译的 `ProviderUtil.getConnection`。不启动应用、不访问数据库，模拟驱动仅记录类加载和连接调用。

覆盖既有内置驱动映射、大小写与土耳其语默认 Locale、缺省驱动、显式合法驱动、未知类型、任意类名、跨数据库驱动及加载前拒绝。验证核心委托与同步入口使用同一策略；不代表真实数据库连接或整包构建验证。
