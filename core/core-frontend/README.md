# DataEase V3 前端开发

开始前读取根目录 [AGENTS.md](../../AGENTS.md) 和 [前端开发规范](../../docs/development/frontend.md)。本目录包含主应用、预览与移动端页面，以及扩展组件桥接。

## 开发与检查

运行脚本以 [package.json](package.json) 为准，Maven 使用的 Node/npm 版本与构建命令见 [pom.xml](pom.xml)。依赖已就绪时，在本目录执行 `npm run dev`。端口、代理和各模式配置从 [vite.config.ts](vite.config.ts) 进入 `config/` 核对。

选择编译、类型检查、格式化或 lint 命令前，读取 [构建与验证](../../docs/development/validation.md)。该文档说明定向检查方式，以及构建后处理、自动修复和 Maven clean 的副作用。

## 按任务阅读

| 任务                              | 入口                                                                         |
| --------------------------------- | ---------------------------------------------------------------------------- |
| 图表取数、查询参数、权限与缓存    | [数据查询、图表与权限](../../docs/development/backend.md#数据查询图表与权限) |
| 错误处理、认证失败与兼容旧服务端  | [错误契约](../../sdk/common/ERROR_CONTRACT.md)                               |
| 定向逻辑或组件回归                | `tests/` 下对应场景的 README，按其中说明选择执行目录                         |
| 浏览器复现或产品回归              | [独立 Playwright 工具](../../tests/browser/README.md)                        |
| 地图依赖补丁应用、修改或升级      | [补丁维护说明](patches/README.md)                                            |
| AI 对话助手的会话、工具与增量输出 | [对话助手开发说明](../../docs/ai/conversation.md)                            |

专项测试中的模拟接口、组件重挂载和隔离构建各有覆盖边界；以相应 README 的说明记录结果，再补充受影响的实际产品入口验证。
