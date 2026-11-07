# 配置传输与敏感字段回归

本次保持数据库现有 AES 密钥和密文格式不变，不执行数据迁移。

- `/dekey` 只返回 RSA 公钥；移除 `/symmetricKey`。
- 敏感凭据占位符统一为 `******`，同步模块引用公共常量；字面值 `***` 按真实新值处理。图表行列脱敏规则不在本次替换范围。
- 配置写请求采用 AES-256-GCM + RSA-OAEP-SHA256（MGF1-SHA256）。每次请求获取短期挑战，绑定用户、组织、路径，通过现有缓存原子消费，拒绝重放。
- 仅通过 SDK 方法上的 `@ConfigurationSecurity` 标记启用请求解密、响应脱敏；无标记接口原样放行。
- 响应仅复制并脱敏声明的配置/认证属性，保留 DTO、分页及统一响应类型，不扫描 API 数据样本或整个业务响应，不修改内部配置或实体。
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

## 真实浏览器与本机隔离数据库回归

协议夹具（6 个用例，包含普通 HTTP 域名）不连接数据库：

```sh
npm --prefix tests/browser test -- --config=playwright.configuration-security.config.mjs
```

完整 MySQL 链路默认使用前端 `127.0.0.1:7070`、后端 `127.0.0.1:8100`，以及本机测试登录配置；独立前端可通过 `CONFIGURATION_LIVE_URL` 指定地址，使用正确代理及独立 Vite 缓存，不修改现有开发服务。**仅在已确认的隔离数据库上执行**：准备脚本使用 `.vscode/local/backend.env`，创建唯一前缀测试库、只读账号和 3 行合成数据，需要创建库和账号权限。凭据只写入权限为 600 的临时文件，不加入 Git。

```sh
python3 tests/configuration-security/mysql-fixture.py create --classpath-file /private/tmp/configuration-classpath.txt
npm --prefix tests/browser test -- --config=playwright.configuration-live.config.mjs
python3 tests/configuration-security/mysql-fixture.py cleanup --classpath-file /private/tmp/configuration-classpath.txt
```

测试创建并最终删除专用数据源，覆盖保存、实际页面仅改名称、修改连接池查询超时、复制、错误密码拒绝、恢复正确密码，以及每阶段通过数据集 SQL 预览验证 COUNT=3、SUM=60。也检查引擎、同步源/目标分页、数据集树的只读响应。即使测试失败，也必须执行最后的清理命令。

测试账号为平台管理员时，按产品现有“进入组织”操作选择已有组织，不修改权限数据。同步任务执行、API 数据源、远程 Excel、填报和多节点网关不属于这组连接配置回归的覆盖范围。

## 按需查看已保存敏感信息

`POST /sensitive/reveal` 复用一次性 AES-GCM / RSA-OAEP 请求封装。解密后的参数为
`resourceId`、`secretType`、当前用户 `password`、独立临时 `responseKey`（Base64，32 字节）。
仅在原有 `show-password` 控件处接入验证；普通 SSH/SSL 私钥文本域不新增查看入口。
枚举仅允许数据源密码、SSH 密码/私钥口令、远程文件密码、引擎密码与同步连接密码；不接受任意 JSON 路径，也不开放未接入显示控件的敏感字段。
后端验证当前身份与登录密码，并复用对应资源的管理权限、同步组织边界和引擎系统参数管理权限。
返回 `iv` 与 `ciphertext`（含 GCM tag），AAD 固定为 `dataease:sensitive-reveal:v1`，设置 `Cache-Control: no-store`。
请求与响应均不暴露数据库 AES 密钥，历史存储密文及解密路径保持兼容。

弹框按数据源、引擎、同步连接、远程文件区分文案。查看结果单独展示，30 秒后清除，关闭或切换资源也清除，不写回表单。
本地登录用户复用系统账号锁定检查；接口另设每用户、每节点 5 分钟内最多 5 次失败的限制，避免登录锁定关闭后无限猜测。
外部认证用户没有本地密码验证能力时拒绝查看。审计仅记录用户 ID、资源 ID 和枚举，禁止记录密码或返回明文。
普通 HTTP 下此封装不能抵御主动替换页面或公钥的攻击，不能替代 TLS。

独立 Java 回归增加明文返回的加密互通、随机 IV、无权限/错误密码/未知枚举/无扩展/重复验证器拒绝及次数限制。
真实 MySQL 用例还覆盖：错误密码拒绝、正确密码查看、请求和响应不含明文、关闭与 30 秒超时清除、表单仍为 `******`、新输入密码保持本地显示行为，以及查看后的保存和 SQL 查询。
扩展独立回归验证本地/外部/禁用/锁定账号、错误次数记录、引擎菜单权限和同步资源管理权限调用。
SSH 密码/私钥口令、远程文件、其它数据库方言及多节点共享缓存尚未逐项进行真实环境联调。

## 引擎与同步连接正常页面回归

`playwright.configuration-business.config.mjs` 使用页面实际挂载的路由实例，验证正常菜单页面，连接真实后端；不模拟业务接口或绕过鉴权。
平台管理员先按原产品流程进入已有组织，再访问业务页面。复制测试只拦截浏览器复制命令校验内容，不覆盖宿主系统剪贴板。

引擎是全局配置。本夹具只允许在原先**没有引擎**的隔离环境中运行，创建唯一前缀的专用 MySQL 引擎；拒绝覆盖已有引擎。
在上面 MySQL 夹具仍存在、同步 JDBC 驱动目录已正确配置的前提下执行：

```sh
python3 tests/configuration-security/mysql-fixture.py engine-check --classpath-file /private/tmp/configuration-classpath.txt --fixture-file /private/tmp/de-security-engine-test.json
npm --prefix tests/browser test -- --config=playwright.configuration-business.config.mjs
python3 tests/configuration-security/mysql-fixture.py engine-cleanup --classpath-file /private/tmp/configuration-classpath.txt --fixture-file /private/tmp/de-security-engine-test.json
```

结束后恢复原启动参数并重启测试后端，清除临时引擎在 Calcite 中的运行缓存，再清理 MySQL 夹具。
引擎夹具只删除自身唯一前缀的记录并核验恢复为空；同步用例通过实际 API 删除专用连接。

引擎回归覆盖新建保存、原页面编辑保留密码、明文查看/复制、配置编辑与再次连接。
同步回归覆盖 MySQL 源连接新建、原页面改名保存、明文查看/复制、查询超时修改、连接校验及保存后读取 `sales` 表。
目标端需要独立 Doris/插件数据库实例，本机未准备相应实例，不能据源端测试声明目标端实际保存或任务执行已通过。

新增 `EngineRegression` 验证 8 种引擎配置表示的回显/保存兼容、目标变更拒绝、跨类型必须重填凭据，以及使用新凭据切换类型的校验语义。
该测试不等同于对这些数据库分别执行连接测试。

六星占位回归还覆盖：JDBC/SSH/SSL/远程文件/API 认证字段的遮罩、保留、显式清空与目标变化拒绝；数据源接口实际保存字面值三颗星后按需查看验证，并恢复原测试密码以继续 SQL 查询。
真实页面用例分别拒绝数据源、引擎和同步连接在保留占位凭据时变更连接目标，随后验证原配置仍可用。

## 本轮验证结果

六星占位版本已通过：SDK 安装、核心与受影响扩展编译；核心 218 项与扩展 48 项断言；浏览器协议 6 项；真实 MySQL 数据源、引擎和同步源连接共 3 条页面用例。数据源在恢复原后端启动参数后再次通过。测试使用独立 7071 前端，未修改原 7070 开发服务。

真实页面覆盖保存、保留密码编辑、查看与复制、目标变化拒绝、连接复用；数据源还覆盖复制资源、30 秒递减清除、字面值三颗星密码和 SQL 预览，同步源验证保存后读取测试表。测试结束已删除专用数据源、同步连接、引擎、测试库和账号，并恢复原后端参数。

SSH/SSL、远程文件和 API 凭据仅完成契约与拒绝路径验证；其它数据库、同步目标端及任务执行、多节点缓存和系统剪贴板尚未完成真实环境回归。
