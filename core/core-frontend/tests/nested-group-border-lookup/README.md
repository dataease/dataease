# Tab 内分组边界重算回归

在 `core/core-frontend` 执行：

```sh
node --test tests/nested-group-border-lookup/regression.cjs
```

测试执行生产 `compose.ts` 的 `updateGroupBorder`，并复用生产 `common.ts` 的组件查找与 `utils/style.ts`、`utils/decomposeComponent.ts` 的坐标换算逻辑。覆盖：

- 分组位于 Tab 页内时，按内部组件重新计算分组边界，子组件仍留在 `Group-<分组ID>` 画布内并相对分组定位。
- 分组位于主画布时行为不变，作为回归保护。
- canvasId 对应的分组已不存在时安全返回，不再抛出异常。

修复前：分组位于 Tab 页内时 `updateGroupBorder` 只在主画布组件数组中查找分组，取到 `undefined` 后访问 `propValue` 抛 `TypeError: Cannot read properties of undefined (reading 'propValue')`，拖动或缩放 Tab 内分组的子组件会在鼠标抬起时中断分组边界重算。

修复后：改为在整棵画布树（主画布、分组、Tab 页）中查找所属分组，查不到时直接返回。

这是隔离的坐标换算回归，未在浏览器中实际拖动 Tab 内分组中的组件，也未覆盖分组嵌套分组、移动端设计与嵌入入口；完整仪表板/大屏的交互与保存流程仍需联调。
