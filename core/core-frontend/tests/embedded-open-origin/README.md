# 新窗口嵌入握手来源验证

运行：`node --test tests/embedded-open-origin/regression.cjs`。

测试执行生产 `OpenHandler.vue` 的脚本，替换窗口、Store 和 Vue 生命周期；覆盖真实 iframe 窗口与 DIV 嵌入包装窗口两条登记路径、精确 origin 与窗口身份、伪造 UUID/消息来源、窗口跨域跳转、空/opaque origin、URL 子串伪装、无效 baseUrl、弹窗拦截、Token 清除及监听清理，并编译生产 SFC。

有效握手仅向登记的 WindowProxy 和明确的目标 origin 发送配置。DIV 嵌入以配置的 DataEase baseUrl 的 origin 为准，不将宿主页 origin 误作为 DataEase 新窗口 origin。

这是隔离的消息处理回归，未运行真实跨域浏览器窗口、完整编辑/预览流程或 XPack 联调；接收端 NewWindowHandler 和宿主消息入口不在本次修复范围。
