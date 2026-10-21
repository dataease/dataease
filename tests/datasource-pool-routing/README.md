# 数据源启动连接池路由回归

先使用 Java 21 编译当前 SDK 和后端，再生成运行依赖清单：

```sh
mvn -B -DskipTests install
mvn -B -N -f core/pom.xml install
mvn -B -f core/core-backend/pom.xml compile -Pstandalone -Dmaven.antrun.skip=true
mvn -B -f core/core-backend/pom.xml dependency:build-classpath -Dmdep.outputFile=/tmp/de-backend-classpath.txt
python3 tests/datasource-pool-routing/run.py --classpath-file /tmp/de-backend-classpath.txt
```

独立回归调用实际编译的 `CalciteProvider.initConnectionPool()` 及真实 Provider 类型判断，使用 Mockito 替代数据库连接、配置解密和线程调度。覆盖仅插件数据源、空数据源、混合数据源、内置默认引擎、未知插件不妨碍其他数据源五种场景，同时验证插件配置不会被 Calcite 解密处理，类型判断无需请求许可上下文。

需要本地 Maven 缓存已有 Mockito 5.17.0（MIT）、Byte Buddy/Agent 1.15.11（Apache-2.0）及 Objenesis 3.3（Apache-2.0）；仅用于独立测试，不修改生产依赖。自定义缓存可设置 `DE_MAVEN_REPO`。

此回归不启动应用或访问数据库，不能替代真实达梦首次安装、默认引擎和 Demo 查询验证。
