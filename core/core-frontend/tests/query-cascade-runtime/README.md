# 级联查询运行态同步回归

来源：V2 TAPD `1139007144001078146`，参考 V2 提交 `a9094a6200`。V3 中级联上级变化后，失效子级只清理显示值，旧的 `config.selectValue` 仍参与查询，可能使富文本等关联图表无数据。

```sh
node --test tests/query-cascade-runtime/regression.cjs tests/query-cascade-runtime/cascade-sync-clear.cjs tests/query-options-race/regression.cjs tests/query-cascade-default/regression.cjs tests/query-tree-default/regression.cjs
```

新用例从真实 Select.vue 提取枚举重载、ID 映射、值同步和级联通知方法，覆盖单选、多选、空枚举、保留有效项、显示名不变但 ID 更新、查询按钮、重复查询及配置态。查询调度与接口使用桩，不访问业务数据。通过 `SELECT_SOURCE=/path/to/before.vue` 可对照修改前源码。

`cascade-sync-clear.cjs` 单独覆盖 F1：上级改选（非空/清空）时 `getOptionFromCascade` 必须同步清空子级的 `selectValue`/`mapValue`/`currentSelectValue` 并通知下级，避免上级触发的即时查询携带旧子级值；子级原有关联值时，运行态还要按查询按钮规则刷新子级自身关联的图表（上下级绑定不同图表的场景），为空或配置态则不发查询；批量应用默认值（重置）期间保留原值。仅做单元级校验，与下方浏览器交互用例互补。

V3 同步时还需避免默认值分支在运行态值已同步后重复查询或绕过查询按钮。竞态用例明确以配置态隔离选项请求生命周期，运行态同步由此目录覆盖。

限制：未覆盖真实浏览器连续交互、实际富文本渲染、后端 SQL 和多数据源执行；不将这些逻辑回归描述为完整产品回归。

## 两份改动组合后的浏览器交互

```sh
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test \
node /path/to/playwright/cli.js test --config tests/query-cascade-runtime/playwright.config.cjs
```

需要 Chrome、已安装前端依赖及空闲的 7098 端口。独立 Vite 服务器和缓存使用临时目录，结束后关闭并清理，不连接现有业务接口。

每轮先冻结查询源码，直接挂载真实 `Component.vue`、`StyleInject.vue`、`Select.vue`、`Flat.vue`，使用真实 Element Plus Secondary 下拉框、真实 Vue 生命周期和 `useEmitt`，通过真实 `useFilter` 构造图表查询参数。枚举接口、图表接口、编辑器状态及无关编辑组件使用桩；页面 output 模拟富文本的数据展示，不挂载 `DeRichTextView` 或真实画布。

8 项交互覆盖：三级连续改选、反复恢复三级默认值、查询按钮模式、延迟旧响应、多选、清空上级和全部清空、实际单选下拉框、下级空枚举。检查最终运行态值与映射、查询结果、查询次数及 pageerror；用等待窗口确认延迟枚举到达后默认值或新条件未被再次覆盖。

这些用例验证两份修改组合后的查询交互链。它们不代替真实仪表板/大屏的富文本视觉、后端 SQL、真实数据权限或多数据源回归。

验证期间工作区的 Claude 改动被撤回，因此另以 `CASCADE_VARIANT=combined` 在内存快照中补回此前读取到的改动：`cascadeResetState`、重置期间保护和上级改选时清空子级。快照由独立 Vite 插件提供，不写回源码；组合快照与当前代码各自通过 8 项浏览器交互。

```sh
CASCADE_VARIANT=combined PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test \
node /path/to/playwright/cli.js test --config tests/query-cascade-runtime/playwright.config.cjs
```

## 上下级绑定不同图表

补充 2 项浏览器回归，当前共 10 项：部门/项目绑定独立图表，人员单独绑定富文本结果。修改部门使人员被同步清空后，无查询按钮时应通知人员关联的图表重新取数；有查询按钮时应保持原结果，点击查询后再更新。修复前自动查询用例可复现“条件已空但结果仍为旧人员”，修复后两种模式及原有 8 项交互全部通过。
