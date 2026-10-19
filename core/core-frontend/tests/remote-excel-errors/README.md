# 远程 Excel/CSV 加载错误提示回归

在 `core/core-frontend` 安装现有依赖后执行：

```sh
node tests/remote-excel-errors/check.mjs
```

用例直接解析并执行 `ExcelRemoteDetail.vue` 的 `loadData` 和共享 Axios 响应拦截器，使用真实 Axios 错误类型及三种语言资源，不复制生产错误处理逻辑。

覆盖三语共 27 个场景：断网、请求中断、两种标准 Axios 超时、非标准超时消息、主动取消、HTTP 失败、SMB 业务失败及正常加载。验证只提示一次、取消不提示、失败清空预览且结束 loading、成功加载不受影响。

可用 `REMOTE_EXCEL_COMPONENT_FILE` 指定旧组件文件，并用 `REMOTE_EXCEL_CASE` 选择场景（例如 `network disconnected` 或 `normal Axios timeout`）进行修复前对照。

这是组件与拦截器的行为回归，不是 QA 环境浏览器验收或真实 SMB 故障注入。测试不访问网络、SMB、数据库，也不需要任何凭据。
