# 关闭默认值后的级联状态回归

运行：`node --test tests/query-cascade-default/regression.cjs`。

测试从生产 `QueryConditionConfiguration.vue` 提取并执行 `validate`、`multipleChange`、`confirmClick`，使用 Vue 的 nextTick，隔离页面依赖及快照/通知副作用。覆盖禁用默认值后显示值及映射值清理、重复级联节点、单选/多选/自动条件、启用的默认值及数字 0、验证失败和配置序列化后重新保存。

可通过 `QUERY_CONFIGURATION_SOURCE=/path/to/before.vue` 指定旧组件源码进行对照。修复前的源码会在映射值清理等用例失败。

本测试验证配置保存逻辑，不挂载完整编辑器，不调用后端。实际仪表板/大屏的选项加载、图表查询、撤销重做、持久化保存和移动端/嵌入入口仍需联调。
