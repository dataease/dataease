# 查询配置绑定操作：首批回归

范围仅为配置态：绑定的读取、日期/数字范围参数写入、候选来源切换清理与静态绑定校验。继续读写旧字段，不新增保存字段，不迁移运行态候选加载、不改变默认值/级联/必填规则，也不改变产品 UI。

在前端目录执行：

```sh
node --test tests/query-config-bindings/regression.cjs tests/query-copied-field/regression.cjs tests/query-tree-default/regression.cjs tests/query-cascade-default/regression.cjs tests/query-options-race/regression.cjs tests/query-cascade-runtime/regression.cjs tests/dynamic-default-time/regression.cjs tests/quarter-range/regression.cjs tests/query-custom-separator-i18n/regression.cjs
```

用真实纯函数和从生产 SFC 提取的配置处理、保存函数验证：

- 只读投影和校验不补写旧字段，不改输入；修改只返回涉及绑定的 patch，保留其他图表及未知历史字段。
- 日期/数字范围的起止方向、单端暂存、移除端点、范围清空、切回单值和 Long 字符串 ID。
- 切换范围类型时清除另一类型残留的起止字段（包括数组已经为空的历史状态）。
- 数据集、字段和候选来源切换保留原默认值清理、公共/私有字段归属及自动推断数据集行为。
- 配置预览“至少有一个绑定”和保存“全部选中图表有绑定”保持各自既有规则。
- 保存、序列化、重开保留未知字段、0/false/空字符串/null、动态日期和参数配置；快照和刷新事件不重复。

已有保存/树/复制字段测试夹具加载真实新模块，动态日期测试补齐先前请求隔离引入的可选作用域上下文；绑定校验使用真实新模块，日期规则由已有专项回归覆盖。

可选地对比重构前的真实配置处理函数：

```sh
# 将重构前的 QueryConditionConfiguration.vue 放入临时目录，无需切换工作区。
QUERY_CONFIGURATION_BASELINE=/tmp/QueryConditionConfiguration-before.vue \
  node --test tests/query-config-bindings/regression.cjs
```

独立浏览器交互：

```sh
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test \
  node /path/to/playwright/cli.js test --config tests/query-config-bindings/playwright.config.cjs
```

浏览器回归分为两组：

- `browser.cjs`：临时 Vite（127.0.0.1:7131），使用真实 Vue、Element Plus Secondary、生产纯函数及提取的配置处理函数，通过简化面板验证取消、绑定切换、清空校验与保存。
- `full-dialog.cjs`：临时 Vite（127.0.0.1:7132），直接挂载完整生产 `QueryConditionConfiguration.vue`，以及真实默认值、树字段和级联子面板。覆盖取消不保存、数据集与查询字段切换、数字范围切单值及切日期参数、日期端点互换、必填默认值校验、级联确认、树层级切换；保存后通过 JSON 序列化并重新挂载检查配置保留。

两组均检查浏览器 `pageerror`，结束后清理临时目录。完整弹窗使用项目中文资源和实际样式，API、Pinia 状态、缓存及图标为隔离边界，数据全部合成；保存指向内存中的查询组件配置，不写业务后端。

这些用例不覆盖已登录完整仪表板的后端保存，也不替代分享、移动端、嵌入入口或所有配置组合的验收。构建验证采用项目 `npm run build:distributed`，需单独执行，不由上述测试自动触发。
