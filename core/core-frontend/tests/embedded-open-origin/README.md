# 新窗口嵌入握手来源验证

运行：`node --test tests/embedded-open-origin/regression.cjs`。

测试执行生产 `OpenHandler.vue` 的脚本，替换窗口、Store 和 Vue 生命周期；覆盖真实 iframe 窗口与 DIV 嵌入包装窗口两条登记路径、精确 origin 与窗口身份、伪造 UUID/消息来源、窗口跨域跳转、空/opaque origin、URL 子串伪装、无效 baseUrl、弹窗拦截、Token 清除及监听清理，并编译生产 SFC。

有效握手仅向登记的 WindowProxy 和明确的目标 origin 发送配置。DIV 嵌入以配置的 DataEase baseUrl 的 origin 为准，不将宿主页 origin 误作为 DataEase 新窗口 origin。

上述 Node 用例仅覆盖发送端隔离逻辑，不运行真实浏览器或 XPack 联调。

## 新窗口接收端回归

从仓库根目录运行（复用 `tests/browser` 已安装的 Playwright 和本机 Chrome，不安装依赖）：

```sh
node tests/browser/node_modules/@playwright/test/cli.js test --config core/core-frontend/tests/embedded-open-origin/playwright.config.cjs
```

`new-window.spec.cjs` 执行生产 `NewWindowHandler.vue` 脚本，使用真实浏览器窗口和跨域 `postMessage`，模拟 Vue 生命周期、Store 和域名验证接口。覆盖合法 DIV 跨域和 iframe 同源握手、错误窗口与消息、域名或 token 校验失败、网络失败、重复消息、卸载后的迟到响应、无 opener 和损坏的 URL 参数，并编译生产 SFC。

测试不连接实际服务，不验证后端 token 签名、部署代理、完整编辑/预览页面或第三方宿主集成。失败截图及 trace 写入 `tests/browser/output/new-window-origin`。产品联调仍按 `tests/browser/README.md` 使用独立 CLI 会话复现，再用 Playwright Test 回归。

## 与 embedded-demo 真实联调

先在隔离测试环境启动原始 `embedded-demo`（默认 `http://localhost:8000`），配置有效的嵌入应用和测试账号；可信来源应与 demo 的浏览器 origin 一致。DataEase 必须已部署待验证的前端修改。

```sh
# 从主仓库根目录运行；只传账号名，不传密钥或 token。
EMBEDDED_TEST_ACCOUNT='<测试账号>' node tests/browser/node_modules/@playwright/test/cli.js test --config tests/browser/playwright.embedded-live.config.mjs
```

`EMBEDDED_DEMO_URL` 和 `EMBEDDED_BI_URL` 可覆盖默认的 `http://localhost:8000` 与 `http://homeserver.local:9080`。此套测试不创建应用、不改配置，也不模拟后端。当前检查 div/iframe 的资源树接口成功返回、预览按钮打开的新窗口成功取得仪表板元数据，以及真实 token 配合合法/非法来源的验证结果；初始化失败会直接判失败。默认使用现有的“【官方示例】/连锁茶饮销售看板”，可通过 `EMBEDDED_RESOURCE_GROUP`、`EMBEDDED_RESOURCE_NAME` 覆盖；仅做预览，不保存资源。接口校验用例加载当前 V3 部署的 `encryption`、`embedded` 构建模块，构建导出变化后需同步核对。

报告保存到 `tests/browser/output/embedded-live/results.json`；为避免捕获真实 token，不启用 trace 和截图。用例不代表所有图表取数及图片、字体等静态素材已完整验证；需要另行检查控制台及实际画面。
