# 配置传输与敏感字段回归

本次保持数据库现有 AES 密钥和密文格式不变，不执行数据迁移。

- `/dekey` 只返回 RSA 公钥；移除 `/symmetricKey`。
- 配置写请求采用 AES-256-GCM + RSA-OAEP-SHA256（MGF1-SHA256）。每次请求获取短期挑战，绑定用户、组织、路径，通过现有缓存原子消费，拒绝重放。
- HTTP 响应边界将敏感字段替换为 `***`，不修改内部配置或实体。
- 编辑保留占位凭据，空字符串表示清空；复制通过独立源 ID 校验权限。改变连接目标或 API 条目对应关系时要求重新输入凭据。
- 前端、核心、SDK 和受影响扩展必须配套发布。旧客户端明文配置请求不再兼容；请刷新页面。保留未轮换存储密钥的历史泄露风险。
- 普通 HTTP 支持使用已有 node-forge 实现，无需 `crypto.subtle`，但不能防止主动中间人替换网页代码或公钥。

## 执行

已有 Maven 依赖及编译产物就绪后，在仓库根目录执行：

```sh
mvn -o -f core/core-backend/pom.xml dependency:build-classpath -Dmdep.outputFile=/private/tmp/configuration-classpath.txt -DincludeScope=test -q
python3 tests/configuration-security/run.py --classpath-file /private/tmp/configuration-classpath.txt
```

回归将当前生产源码编译到临时目录，使用临时 RSA 密钥、内存缓存和模拟 HTTP 请求；不启动应用，不访问数据库，不改写 `target`。

覆盖前端真实加密函数与 Java 解密互通、长中文配置、旧存储密文、脱敏副本、占位恢复与清空、连接变更拒绝、条目错配拒绝、篡改/过期/跨用户/跨路径/重放拒绝，以及鉴权预解析不提前消费挑战。

发布前仍须在隔离环境验证实际数据源编辑、复制、测试连接、API 数据源、远程 Excel、引擎与扩展流程；分布式部署验证共享缓存和鉴权代理，HTTP 部署验证目标浏览器。
