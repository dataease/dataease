# 仪表板可视区域取数

来源：TAPD `1140899682001022805`。桌面端仪表板的原生图表默认在进入视口附近 200px 时首次取数，首次加载后保留。保留条件监听与最近一次确认的请求快照，避免屏外图表漏掉查询事件或读取查询按钮尚未确认的输入。编辑画布、移动端、数据大屏和插件图表沿用原取数行为；插件首次加载完成的公共通知尚未统一，本次不改变其加载契约。

```sh
node --test tests/chart-viewport-load/regression.cjs
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test \
node /path/to/playwright/cli.js test --config tests/chart-viewport-load/playwright.config.cjs
```

需要已安装的前端依赖、Chrome 和空闲的 7099 端口。浏览器用例挂载真实 Vue 组件生命周期和 `useChartViewportLoad`，通过独立 Vite 服务与模拟数据接口验证首屏、窗口/容器滚动、连续筛选、查询按钮、导出等待与容器范围、卸载、无 Observer 降级及模式切换。不会连接现有后端或修改业务数据。

逻辑用例从生产入口提取实际函数，检查参数快照、插件原有查询契约、启用范围、定时刷新，以及模板导出补齐屏外数据后再校验/打包、失败时提示并结束加载。图片/PDF/模板/应用导出统一补齐当前容器内的屏外原生图表。

浏览器图表采用简化组件，没有挂载完整图表库或 XPack 插件；未覆盖真实 SQL、图表视觉、移动端和完整产品导出文件。
