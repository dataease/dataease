# 查询条件弹窗下拉层回归

在前端目录执行：

```sh
./node_modules/.bin/vite --config tests/query-dialog-popup/vite.config.ts
```

打开 `http://127.0.0.1:7089/`，点击“执行回归”。成功时显示 `PASS: 36 assertions`。

页面从生产 `QueryConditionConfiguration.vue` 读取主弹窗的 `destroy-on-close` 配置，使用真实 `element-plus-secondary` Dialog/Select 和本地选项，验证单选、时间范围多选、数值范围多选在取消、确认和关闭按钮关闭后，其挂载到 body 的下拉层及选择器均被销毁，并可反复打开。

打开 `http://127.0.0.1:7089/?baseline` 可禁用该配置，对照原先保留弹窗内容的行为；预期在首个取消场景因下拉层残留而失败。

这是隔离的 UI 生命周期回归，不连接后端，不挂载完整查询配置组件，不验证字段映射、级联、键盘交互和后端保存。完整仪表板/大屏的业务流程需另行联调。
