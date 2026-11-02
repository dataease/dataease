# 动态季度范围回归

逻辑验证：`node --test tests/quarter-range/regression.cjs`。

浏览器验证：使用已安装的 Playwright CLI 执行 `test --config tests/quarter-range/playwright.config.cjs`。若 Playwright 不在当前模块，设置 `PLAYWRIGHT_TEST_MODULE` 为其 `test` 模块路径。

逻辑用例覆盖自然季度、跨年、闰年、三种粒度、配置选项、报告摘要、实际查询参数、首次打开与重置、手动查询、动态允许区间和固定限制。最近三个月保留原有口径。

浏览器使用隔离的 Vite 页面（7097 端口），加载真实默认配置、允许区间和预览组件以及现有 UI 库，验证三种粒度的预览、规则 JSON 往返、实际下拉选择。固定测试时钟后，显式推进时间以完成弹层动画。

测试替代国际化入口和不相关字段控件，不连接业务后端，不写业务数据。未覆盖真实数据库取数、报告任务调度以及完整移动端、分享或嵌入宿主交互。
