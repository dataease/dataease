# V2 嵌入背景图片认证回归

对应 V3 [PR #19863](https://github.com/dataease/dataease/pull/19863) / Issue #19826。
V2 仍使用原生图片 URL 和认证 Cookie 回退，跨站环境不能依赖 Cookie，因此本次增加通过既有请求封装下载图片的显示路径，同时要求网关认证上传图片。图片配置仍保存原始路径，外部 URL 不附加凭据。

在仓库根目录执行（要求已安装的 JDK 21 和本地 Maven 缓存依赖）：

```sh
python3 -B tests/resource-image-auth/run.py
npm --prefix tests/browser test -- --config=playwright.resource-images.config.mjs
```

Java 回归编译真实白名单源码，覆盖 54 项路径断言。移除目录白名单后，旧的全局图片后缀规则仍会放行图片；本用例在增加目录认证优先判断前实测失败。

浏览器回归仅启动本机 4186 Vite 夹具，加载真实 `resourceImages.ts` 和 `api/staticResource.ts`，替换认证状态、Axios 传输边界和上传校验依赖，使用合成图片。覆盖 CSS/图片渲染、请求去重、登录/嵌入/分享及组织状态切换、401/403/404 不重试、导出等待与拒绝、旧身份请求隔离和非法路径拒绝。

测试不连接业务数据库，不验证真实凭据签名、过期判定、APISIX 换发、CORS、iframe/Div 宿主、真实仪表板/大屏或完整导出文件。真实环境仍需部署后回归，不能将夹具通过等同于产品闭环通过。

CLI 可单独启动 `node tests/browser/fixtures/resource-images/server.mjs`，使用本缺陷独立会话打开 `http://127.0.0.1:4186/` 并拦截合成图片响应。结束后仅关闭该会话和该夹具。
