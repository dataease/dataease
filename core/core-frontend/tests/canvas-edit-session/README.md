# 编辑器会话与保存安全（第 1 批）

本批只处理仪表板、大屏的加载、切换、保存与草稿恢复边界，不迁移持久化格式，不改变布局，不增加网络管理器。

## 状态与写入口清单

| 归属                 | 当前字段/载体                                                                                                                               | 当前主要写入口                                                     | 本批处理                                                                       |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------ | ------------------------------------------------------------------------------ |
| 文档内容             | `componentData`、`pcComponentData`、`mobileComponentData`、`canvasStyleData`、`canvasViewInfo`                                              | `createInit`、`initCanvasData`、`snapshotPublish`、图表/组件编辑器 | 保留 Store 与保存协议；保存发起时复制文档，禁止校验等待期间改用其他资源        |
| 文档身份与元数据     | `dvInfo` 的 ID、名称、父目录、类型、状态、contentId、水印等                                                                                 | `updateCurDvInfo`、`updateDvInfoCall`、首次保存、恢复已发布版本    | 会话绑定已装载文档；新建/复制生成 ID 后更新绑定；旧成功回调不更新新会话        |
| 待保存应用与模板资源 | `appData`、`dvInfo.materialBundle`、模板来源                                                                                                | 模板解包、应用保存及保存后重载                                     | 切换时清除旧应用暂态；模板与保存后重载检查所属会话                             |
| 图表运行数据         | `canvasViewDataInfo`、`canvasViewInstanceInfo`、`canvasViewOriginDataInfo`、`lastViewRequestInfo`、`pureCanvasViewDataInfo`、`viewPageInfo` | 图表取数/绘制、`useChartRuntimeCache`                              | 复用已有请求与实例缓存；本批不迁移这些字段                                     |
| 编辑交互             | `curComponent`、选中索引、批量操作、拖拽、Tabs 活动/碰撞状态、联动设置                                                                      | 画布操作、组件操作、属性编辑器、`snapshotPublish`                  | 切换清空当前选择；动作归拢留给后续批次                                         |
| 历史与草稿           | snapshot Store、`DE-DV-CATCH-<id>`                                                                                                          | `recordSnapshot`、`snapshotCatchToStore`、草稿恢复弹窗             | 恢复前检查资源/类型及必要结构；拒绝旧弹窗操作新资源；容量及写入调度留给第 2 批 |
| 页面与宿主上下文     | `canvasAttachInfo`、嵌入/公共链接状态、折叠、预览、缩放、滚动等                                                                             | 编辑入口、宿主桥接及布局组件                                       | 保持现有语义；不把会话写进文档或撤销历史                                       |

这些是迁移边界，不代表已经将 `dvMain` 全部拆开。`dvInfo` 中仍含 `dataState`、`optType` 等混合字段，后续迁移不能直接把整个对象视为纯持久化文档。

会话由编辑入口持有，随入口销毁；WeakMap 只为既有全局保存工具提供当前 Store 的会话校验，不支持同一个单文档 Store 同时编辑多个独立资源。其他入口接管后，旧入口不得提交回调或删除新入口的会话。

## 验证

```sh
node --test tests/canvas-edit-session/regression.cjs
```

测试执行生产会话模块、保存函数及从 SFC 提取的真实初始化/保存调用方；路由测试使用真实 Vue 响应式和同步 watcher。接口、存储及页面组件使用替身，不连接业务后端。

覆盖加载失败/重试、A→B→A、未就绪禁止保存、草稿归属与旧弹窗、模板失败、新建/复制首次保存、名称校验途中切换、保存完成晚到、请求快照不被后续编辑修改、保存后路由替换、正常离开编辑器、发布前导航等待及 loading 释放。

未覆盖完整产品浏览器、真实接口保存重开、完整模板素材上传、XPack 插件与电子表格，以及全部撤销/组合/Tabs 交互。后续交付前仍需对应联调回归，不能把这些逻辑测试计作完整产品验收。

## 保存后刷新失败

应用或素材包的写入已经成功、刷新失败时，提示“已保存，未继续发布，请重新打开后重试”。清除已提交的应用暂态，令当前会话不可再次保存，并关闭 `dataPrepareState`，阻止尚未刷新配置进入草稿。重新打开使用已保存的新 ID 重新装载，不再次创建应用数据集。旧会话失败不得提示或冻结新会话。该分支不绕过重载继续发布。

## 独立浏览器回归

```sh
PLAYWRIGHT_TEST_MODULE=/path/to/playwright/test \
node /path/to/playwright/cli.js test --config tests/canvas-edit-session/playwright.config.cjs
```

使用现有前端依赖、Chrome 和空闲的 7131 端口。测试挂载实际 `useCanvasEditSession` / `useLatestQuery`，执行从仪表板和大屏 SFC 抽取的初始化、路由监听、保存发布函数以及公共保存函数。取数、保存、模板与发布接口全部拦截为合成响应，独立临时 Vite 服务在测试后关闭。

覆盖快速切换、卸载、新建/复制/模板首次保存、保存后路由替换、应用刷新失败的可见提示及写入保护、重新打开恢复、刷新成功后发布。它是实际 Chrome/Vue 生命周期回归，但未挂载完整编辑器与插件，不能算真实业务后端联调或完整产品验收。
