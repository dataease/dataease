# 可视化辅助读取权限回归（TAPD 1076998）

`POST /dataVisualization/findNameById` 保留 HTTP URL，但只返回主表的 `id`、`name`，忽略请求中的 `resourceTable`。该接口要求真实资源的读取权限。用于操作日志的 Java `findNameById` 方法保留原签名，但不再映射为 HTTP 入口。

`GET /dataVisualization/viewDetailList/{dvId}` 服务于编辑器外部参数配置，返回草稿图表和字段元数据，因此要求目标资源的 **MANAGE** 权限。两者均拒绝匿名和公共分享身份，使用数据库中的资源类型进行授权，不信任请求中的 `busiFlag`。组织、继承授权及管理员代理语义沿用现有 `InteractiveAuthApi`。只有实际单用户社区版可缺省该权限服务，企业版服务缺失时拒绝访问。

## 定向回归

仓库根目录使用 Java 21；先安装本次 SDK，再编译后端。测试使用后端解析的依赖类路径（包含 Mockito），不依赖已跳过的 Surefire：

```sh
mvn -q -pl sdk/api/api-base -am install -Dmaven.test.skip=true
mvn -q -f core/core-backend/pom.xml compile dependency:build-classpath -Pstandalone -Dmaven.test.skip=true -Dmdep.outputFile=/tmp/visualization-read-classpath
mkdir -p /tmp/visualization-read-test
visual_test_cp="core/core-backend/target/classes:sdk/common/target/classes:sdk/api/api-base/target/classes:sdk/api/api-permissions/target/classes:$(cat /tmp/visualization-read-classpath)"
javac -proc:none -cp "$visual_test_cp" -d /tmp/visualization-read-test tests/visualization-read-security/Regression.java
java -cp "/tmp/visualization-read-test:$visual_test_cp" Regression
```

25 项断言调用实际服务方法，模拟资源仓库和权限提供方，验证最小响应、草稿参数忽略、实际资源类型解析、READ/MANAGE 区分、删除/不存在/零/空 ID、匿名与分享拒绝、权限服务缺失时拒绝、社区版兼容及 HTTP 映射。权限提供方被模拟，不将这些断言视为真实跨组织授权验证。

## 本地 HTTP 验收

在隔离开发环境创建无角色临时账号，对仪表板和大屏分别执行：无权限时两个接口均拒绝；仅授予 READ 后名称接口只返回 id/name，字段接口拒绝；伪造 busiFlag 与 snapshot 不扩大响应；授予 MANAGE 后字段接口成功。用产品授权 API 修改临时账号权限，最后删除临时账号及授权。

同时检查普通预览/分享继续使用原 `findById` 链路、编辑器外部参数配置可用、操作日志仍能解析资源名称。分布式部署必须更新承载权限注解的 api-base 依赖和核心服务。未覆盖的部署模式、跨组织管理员代理、完整分享/嵌入页面回归需单独验收；本修复不修改 XPack 实现及数据库结构。

本次已执行上述仪表板/大屏 HTTP 权限矩阵，另外验证匿名请求和直连本地后端的无权名称请求均被拒绝。临时账号已删除；未改动测试目标画布、草稿及字段。SDK 安装、后端 package 与 25 项独立断言通过；Maven package 跳过 Surefire，不将构建结果视为测试执行。
