# 构建与验证

选择构建或验证命令、完成修改后的检查前读取本文；全局边界见 [AGENTS.md](../../AGENTS.md)。命令默认从仓库根目录执行，前端命令另有说明。

先选择最小但有意义的验证，再根据依赖影响扩大范围。纯文档或低风险样式改动不要求新增测试；查询、权限、迁移、字段更新等行为改动应尽量增加能复现问题的回归验证。

## 修改后的格式化与检查

- 完成修改后，交付前按文件类型执行项目已有的格式化与检查，仅处理本次任务修改的文件；不能依赖编辑器保存操作触发格式化。
- JS、TS、JSX、TSX、Vue、CSS、SCSS、Less、HTML、JSON、JSONC、Markdown、YAML 等 Prettier 支持的文件，使用所属模块已安装的 Prettier，遵循该模块配置及忽略规则。缺少适用配置时沿用现有风格，不跨模块套用前端配置。
- JS、TS、JSX、TSX、Vue 文件格式化后，执行定向 ESLint 检查；样式文件按所属模块现有 Stylelint 配置执行定向检查。
- Java、XML、SQL、Shell、Python 等文件先确认所属模块是否已有格式化工具和配置；有则沿用，没有则遵循现有风格，不自行安装工具、增加插件或引入新的格式规范。
- 不格式化生成文件、构建产物或第三方代码，不执行全仓库格式化或带全量自动修复的 lint 命令。文件包含用户已有修改时，检查差异，避免无关格式变动。
- 只读分析、审查任务不执行格式化。完成后检查差异和 `git diff --check`，报告实际执行的检查；工具缺失、检查失败或未覆盖的文件类型应明确说明。
- 主前端在 `core/core-frontend` 目录使用 `./node_modules/.bin/prettier --write <本次修改的文件路径>`，随后对适用文件执行 `./node_modules/.bin/eslint <本次修改的 JS、TS、JSX、TSX、Vue 文件路径>`；占位符替换为明确的文件列表，不使用全目录通配符。

## 前端

以下命令在 `core/core-frontend` 执行，依赖需已安装：

| 命令 | 用途与边界 |
| --- | --- |
| `npm run ts:check` | TypeScript 检查；若存在基线问题，区分与本次改动的关系 |
| `./node_modules/.bin/eslint <改动文件路径>` | 定向 lint，不加 `--fix`；占位符需替换为实际文件 |
| `npm run dev` | 开发服务；当前配置端口 7070，代理后端 8100 |
| `npm run build:base` | base 模式构建 |
| `npm run build:distributed` | distributed 模式构建 |

- `npm run lint` 和 `npm run lint:stylelint` 带自动修复，不能作为只读检查命令；避免造成无关文件改动。
- `build:base` / `build:distributed` 会继续执行 `build:flush`，其中包含子目录依赖安装和文件生成。执行前确认副作用与任务范围一致。
- 前端没有统一 `test` 脚本。从 `core/core-frontend/tests/` 中查找对应场景的 README；饼图标签相关改动见 [饼图标签回归](../../core/core-frontend/tests/pie-label-overflow/README.md)。逻辑或组件夹具通过不等于完整产品回归。
- 浏览器问题按 [浏览器复现与回归](../../tests/browser/README.md) 使用独立 Playwright CLI 复现、Playwright Test 定向回归，不使用 Playwright MCP。该文档统一维护会话隔离、快照、报告和失败排查方式；CLI/Test 遇到限制时先排查工具配置和测试入口，只有用户明确要求时才调整工具约定。
- 不自动升级依赖或重建全部锁文件；新增依赖需说明必要性，并检查许可证、体积和实际跟踪的锁文件。
- 脚本与构建模式以 `package.json`、`vite.config.ts` 和对应配置为准；当前没有独立的 `build:lib` 脚本。

## 后端与整包

- 根目录 `mvn install` 用于当前聚合的 SDK 及父 POM；不会自动构建 `core` 和 XPack。
- 完整核心应用可在依赖就绪后使用 `mvn -f core/pom.xml package -Pstandalone` 或任务所需的其他 Profile；这会触发前端构建及资源处理，不能用作轻量或只读检查。
- 后端局部编译可使用 `mvn -f core/core-backend/pom.xml compile -Pstandalone`，前提是父 POM、SDK 和所需前端产物已就绪。Maven 生命周期可能复制资源、生成 Q 类和 flattened POM；它不是无副作用的语法检查。
- `standalone` 默认启用；`desktop` 使用桌面配置；`distributed` 需要相应依赖并排除替补权限代码。以实际 POM 为准，不使用未定义的 Profile。
- 前端 Maven 当前固定执行 `build:distributed`，不要认为后端 `-Pdesktop` 或 `-Pstandalone` 会自动切换前端模式。
- 后端 Surefire 当前有显式 `<skip>true</skip>`。普通 `mvn test` 成功不代表测试执行；必须核对有效配置、实际日志和本次生成的测试报告。不要假定添加 `-DskipTests=false` 就能覆盖该配置。
- 行为测试需要启用时，先确认可行的项目测试入口；若需调整构建配置，保持改动明确且符合任务范围。无法执行时说明原因，不把编译结果写成测试通过。
- 不默认执行 `mvn clean`：前端 Maven clean 会清理依赖和锁文件，后端部分 Profile 会清理生成的静态资源。只在确实需要清理时使用。
- 不直接修改已有 SDK 二进制或生成类来绕过编译错误；修复源码及依赖关系后重新生成。

## 验证覆盖选择

| 改动类型 | 优先验证 |
| --- | --- |
| API / DTO | 请求与响应兼容性、前端调用、核心和扩展实现编译 |
| 权限 | 允许/拒绝、不同用户或组织、相关分享/嵌入路径、导出路径 |
| 查询 / 图表计算 | 空值、筛选、排序、聚合、行列权限及受影响的数据源方言 |
| JPA 更新 / 迁移 | 保留未提交字段、显式清空、事务失败、存量升级与失败重试 |
| 编辑器 / 图表 UI | 实际渲染、重复交互、保存重开、相关移动端或嵌入入口 |
| 部署 / Profile | 构建产物、资源位置、外置配置与目标运行模式 |

只声明实际验证过的环境和行为；未覆盖的数据库、入口或 Profile 明确列为限制。
