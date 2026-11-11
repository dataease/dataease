# 白名单路径边界回归

在仓库根目录执行：

```sh
python3 -B tests/whitelist-boundaries/run.py
```

要求 JDK 21、Python 3，以及脚本中列出的本地 Maven 缓存依赖。可通过 `DE_MAVEN_REPO` 指定缓存位置。脚本将当前 `WhitelistUtils`、`CommonBeanFactory` 和 `AuthConstant` 源码编译到临时目录，使用现有 common 和 license-sdk JAR；不安装依赖、不启动产品应用，不访问网络、数据库或缓存服务。

覆盖内容：

- 字体、分享预检、插件资源、地图文件及区域树继续匹配公共白名单；同名前缀和额外子路径不再匹配。
- 默认部署、自定义 context path、普通 API、CAS/OIDC 前缀和网关 URI 查询参数。
- OAuth2、SAML、第三方平台登录、嵌入初始化、消息附件、WebSocket/SockJS 和静态资源白名单兼容性。
- 遍历 `mapFiles` 实际文件，验证所有现有内置地图资源仍可匿名匹配。
- 复用 `tests/apidoc-whitelist/Regression.java`，验证静态资源白名单与本地 Knife4j 4.5.0 资源兼容性，不依赖 Maven 生成的 `target/classes`。
- 读取五组受影响 SDK 接口的字面量路由，使用普通标识符及 JSON 资源形态参数采样，核对公共控制器清单，检测非预期的白名单重叠。
- 使用本地 license-sdk 中实际的认证 Filter 和隔离测试身份验证器，验证公共路径匿名通过、受保护路径缺失或无效 Token 返回 401、有效测试身份传递及请求后身份清理；覆盖 standalone、distributed 和 desktop 模式分支。

范围与限制：

- 保留 `/map/worldTree`、字体信息等现有公共接口；本改动收紧路径边界，不改变它们的数据公开策略。
- `match(String)` 仍为路径级契约，HTTP 方法由现有 Filter 和路由控制；SSO 及其他未调整的目录规则保持不变。
- 路由审计针对受影响 SDK 接口的字面量映射并采用参数采样，不是所有部署的完整运行时路由枚举，也不穷举任意路径模式交集。
- distributed 测试只覆盖 Filter 的模式分支及网关 URI 形态，未连接 APISIX；没有验证真实账号、签名票据、SSO 平台、浏览器或生产部署。公共路径进入 FilterChain 不代表已经通过其后续业务授权。

## 已启动开发环境的功能回归

```sh
npm --prefix tests/browser test -- --config=playwright.whitelist-live.config.mjs
```

默认访问 `http://localhost:7070`，使用开发代理 `/api`；可设置 `WHITELIST_LIVE_URL`。测试账号从项目本地 `.codex/playwright_mcp/.env.local` 的 `TEST_USERNAME`、`TEST_PASSWORD` 读取，不输出凭据。字体测试默认使用本机 Arial TTF，可通过 `WHITELIST_TEST_FONT` 指定测试字体。

该入口连接真实前后端，执行字体上传、列表、重命名、匿名下载及浏览器字体加载，仪表板创建发布与匿名分享密码验证，实际导出任务、一次性签名下载及浏览器下载，WebSocket STOMP 连接、订阅与导出通知，自定义地图上传和坐标展示，并验证相似前缀拒绝访问。测试创建的资源使用 `codex-whitelist-` 前缀并在结束时清理；正常审计日志仍会保留。

内置地图文件和扩展插件需在运行环境中实际存在，缺失时相应用例明确跳过。SSO 用例验证 SAML 元数据、登录发起和非法回调拒绝，不跟随外部身份平台跳转，不代表真实 IdP 登录闭环。该入口不启动后端、不执行数据库迁移或部署，浏览器输出保存在忽略目录 `tests/browser/output/whitelist-live/`。
