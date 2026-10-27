# 开发环境首次懒路由导航

从前端目录运行：

```sh
node --test tests/dev-first-navigation/regression.mjs
```

`first-navigation-deps.json` 记录冷缓存复现时 Vite 在首次点击仪表板后新增的依赖 ID，不包含登录态、业务数据或网络请求内容。回归加载实际 Vite 配置，检查这些入口和 UI 深层样式入口已加入 dev 预构建，并检查 base/distributed/lib 构建配置未启用新增项；配置加载通过不等于完整构建通过。

实页复现步骤：使用隔离的 Vite `cacheDir` 启动 dev，登录工作台并等页面稳定，记录优化器元数据及 `full-reload` 事件，首次点击顶部仪表板。修复前新增依赖导致优化器发送整页刷新，导航尚未提交时刷新旧工作台地址；修复后第一次点击应进入 `/panel/index`，不新增优化依赖或触发整页刷新。生产构建没有这条开发依赖重优化路径。

预构建会提前处理相应依赖，将部分开销移到启动阶段。新增懒路由依赖时需重新核对覆盖；不要用关闭 Vite 刷新或绕过路由权限掩盖问题。

数据源场景使用独立冷缓存直接加载 `/data/dataset`，待页面稳定后首次点击数据准备 → 数据源。修复前新增 `datasource-navigation-deps.json` 所列 6 个依赖并触发整页刷新，回到数据集；修复后应一次进入 `/data/datasource`，不新增优化依赖或触发整页刷新。

`reoptimization-deps.json` 从 2026-10-27 仪表板、数据源两次冷启动的 `new dependencies optimized` 日志中提取，记录此前配置未覆盖的 47 个入口（仅包名，不包含日志中的业务内容）。包含基础运行库、UI 入口、图表、地图和导出依赖；`vue-router_2` 是当前源码实际导入的独立包名。配置回归也检查这些新增项不会进入 base/distributed/lib 模式。

验证预构建时应指定临时 `cacheDir`，不要删除正在使用的 `node_modules/.vite`。这些项增加首次启动的预构建工作量，以减少访问页面后的追加优化；依赖未在日志出现时，不应凭猜测批量纳入。
