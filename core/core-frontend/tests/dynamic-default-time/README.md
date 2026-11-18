# 查询组件动态时分秒偏移回归

在 `core/core-frontend` 执行，使用已有依赖，不安装新包。

逻辑回归：

```sh
node --test tests/dynamic-default-time/regression.cjs
```

Chrome 交互回归（配置 `PLAYWRIGHT_TEST_MODULE` 为已安装的 Playwright Test 路径，通过其独立 CLI 执行）：

```sh
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test node /path/to/playwright/cli.js test --config tests/dynamic-default-time/playwright.config.cjs
```

浏览器测试创建隔离的 Vite 服务（127.0.0.1:7096）并挂载实际的 `ConditionDefaultConfiguration.vue`、动态预览及时间偏移编辑器，使用真实 UI 库和三种语言。非时间组件和允许筛选范围配置被替换为空组件，不访问业务后端、不保存仪表板。测试结束清理临时目录和服务，失败产物在 `/tmp/dataease-dynamic-default-results`。

覆盖：

- 固定基准时间下的前后时分秒、组合、零偏移、25 小时、日期与时钟方向独立、跨日/月/年和闰年。
- 空值、小数、负数、分秒越界和日期溢出；范围共用基准、混合模式及开始晚于结束。
- 旧配置指定时刻兼容、模式切换保留值、预览基准显示、非活动模式忽略及日期粒度隐藏新控件。
- JSON 往返后的规则保留；实际 `searchQuery` 首次计算和手动查询、实际重置函数及保存校验；年/月/日完整周期过滤。
- 简中、繁中和英文；配置布局不横向溢出，数字组件可增减且完整显示两位数，日期/指定时刻控件撑满可用宽度。
- 报告独立保存入口拒绝非法偏移及颠倒范围（包括自动字段），摘要展示混合时钟规则。
- 旧配置空值保持运行兼容；非法规则在原生/插件首次加载时提示并停止取数；重置失败恢复原筛选并清理联动标记。

限制：保存按钮的浏览器场景调用生产校验函数；完整配置对话框及报告配置的校验另以实际函数执行验证。未连接真实图表 SQL、移动端/分享/嵌入宿主和定时报告服务，也未执行模板文件导入导出或数据库边界记录查询。报告执行复用首次加载过滤器的链路仅核对了代码，未运行真实报告任务。
