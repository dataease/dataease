# 移动端设计面板消息来源校验

对应 TAPD `1140899682001077720`。消息必须同时匹配面板 iframe 的 WindowProxy 和其 URL 的精确 HTTP(S) origin。普通页面使用相对 mobile.html 的 origin，跨域嵌入使用配置的 DataEase baseUrl；不信任宿主页、其他 iframe 或 opaque origin。出站配置与嵌入握手也只发送到面板 ref 和明确的 origin。

```sh
node --test tests/mobile-config-message/regression.cjs
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test \
node /path/to/playwright/cli.js test --config tests/mobile-config-message/playwright.config.cjs
```

回归执行生产 SFC 的脚本，并编译生产脚本与模板。浏览器测试使用独立 Chrome 会话及两个临时本地 HTTP 端口，验证真实 postMessage 的 origin/source：同源操作、跨域嵌入握手、同源其他窗口、外域窗口、iframe 导航、指定 ref 出站和卸载清理。不连接现有后端，不修改业务数据。

Vue 状态、Store 和保存操作使用桩；未挂载完整仪表板编辑器，也未验证真实保存接口或 XPack 联调。其他消息接收入口不在本次修复范围。
