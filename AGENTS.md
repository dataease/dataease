# DataEase 开发规范

本文件适用于仓库根目录及其子目录。修改前先确认目标模块的职责、构建入口和既有实现；优先沿用同一业务域的模式。用户明确提出的要求优先于本文件。

## 仓库身份与分支

- 当前目录是 DataEase 产品 v2 源码。主仓库 `https://github.com/dataease/dataease` 是开源项目；Git 子模块 `de-xpack` 对应 `https://github.com/dataease/de-xpack`，其 xpack 包不是开源代码。不要把 xpack 源码、内部实现或构建产物复制进开源主仓库或其公开 PR。
- DataEase 的开发目标分支是 `dev-v2`，以 PR 形式提交。PR 分支按 `pr@dev-v2@<类型>_<描述>` 命名，例如 `pr@dev-v2@fix_something`、`pr@dev-v2@perf_something`、`pr@dev-v2@chore_something`。不要直接推送主仓库的 `dev-v2`。
- `de-xpack` 的开发目标分支是 `main`。需要发布已完成的 xpack 改动时，在子模块内先执行 `git pull origin main --rebase`，解决冲突并确认状态后再执行 `git push origin main`。这属于 xpack 的提交流程，不要在仅编辑文档、检查代码或尚未准备发布时运行。

## 项目结构与模块边界

- `pom.xml` 是 Maven 父 POM，统一版本与依赖管理，当前根聚合 `<modules>` 仅包含 `sdk`。`core/pom.xml` 和 `de-xpack/pom.xml` 各自聚合子模块。不要假设在根目录运行 Maven 就会构建全部模块。
- `sdk/common` 放跨模块通用能力；`sdk/api/api-base`、`api-permissions`、`api-sync` 放供实现方共享的 API 契约、请求/响应类型；`sdk/distributed` 放分布式支持；`sdk/extensions` 放数据源、视图、数据填报的扩展接口和基础能力。SDK 不应依赖 `core` 或具体 xpack 实现。
- `core/core-backend` 是 Spring Boot 主服务，入口为 `io.dataease.CoreApplication`。按 `datasource`、`dataset`、`chart`、`visualization`、`system` 等业务域组织代码。跨模块接口放 SDK，业务实现留在所属业务域。
- `core/core-frontend` 是 Vue 3 + TypeScript + Vite 应用。页面放 `src/views` 或对应 `src/pages` 入口，接口请求放 `src/api`，共享状态放 `src/store`，路由放 `src/router`，可复用组件放 `src/components`；画布元素放 `src/custom-component`。不要在页面中复制已有 API 请求和共享状态逻辑。
- `de-xpack` 是独立 Git 子模块，包含扩展后端及前端。仅在任务确实涉及扩展功能时修改它；检查其自身状态、依赖和提交边界，不要将其视为普通目录。`drivers`、`installer`、`staticResource` 等目录分别承载驱动、部署与静态资源，按用途维护。
- 主仓库与 xpack 分开提交。涉及两边的功能先确认共享契约和兼容性，再分别处理各仓库的改动；主仓库中的子模块引用变更应指向已提交的 xpack 版本。

## 后端实现约定

- 遵循现有业务域的 `Api` → `server` → `manage` → `dao` 调用与职责划分：SDK 中的 `Api` 定义接口和传输类型；`server` 承接 HTTP 入口；`manage` 处理业务规则与编排；`dao/auto` 是生成的实体和 Mapper，`dao/ext` 与 `resources/mybatis` 承载自定义查询。具体域若已有不同惯例，以相邻代码为准。
- 新接口先检查 SDK 是否已有契约及前端调用。变更请求、响应或权限语义时，同步检查接口实现、调用方、序列化字段和兼容性。避免让 DAO 实体直接承担对外 API 契约。
- 数据库结构或种子数据变更使用所属模块的 Flyway 迁移。核心后端的 `db/migration` 用于 standalone，`db/desktop` 用于 desktop；当前 `application-distributed.yml` 关闭了 Flyway，分布式部署的变更方式需单独确认。xpack 权限、同步模块也有自己的迁移与配置。已发布迁移不要原地改写，应新增迁移，并同步检查 SQL、Mapper、Java 类型及适用模式。
- 涉及数据源、SQL 拼接、导入导出或权限时，复用现有校验、参数化、授权与资源访问机制；不要把密钥、账号或环境专属地址写进代码或提交物。
- 配置按 `application.yml` 与 `application-standalone.yml`、`application-desktop.yml`、`application-distributed.yml` 的分层维护。新增配置要确认默认值、环境覆盖和使用处，避免只验证一种运行模式。

## 前端实现约定

- 沿用现有 Vue 单文件组件、TypeScript、Pinia、Vue Router、i18n、Vite 与项目已有 UI/图表组件的写法。按功能靠近现有页面、组件、状态和 API 文件，避免建立重复的全局抽象。
- HTTP 调用统一经 `src/api` 和现有请求封装；路由访问与按钮操作遵循已有权限机制。新增用户可见文案检查本地化资源；图表、仪表板与大屏改动要同时考虑编辑态、预览态和分享/嵌入态。
- 不手工修改 `dist`、`node_modules` 或构建产物。修改依赖时先检查该模块实际使用的包管理方式；有受版本控制的锁文件时保持其与 `package.json` 一致，不凭空添加锁文件。不要仅为顺手整理运行自动修复命令而造成大量无关改动。
- 本地调试 xpack 前端时，DataEase 前端通过静态相对路径引入子模块中的 Vue 组件；线上发布时先构建 xpack，将其前端产出为 JS，再由 DataEase 前端在运行时动态加载。实现入口见 `src/components/plugin/src/index.vue`、`PluginComponent.vue`、`ImportXpackTool.ts` 和 `src/api/plugin.ts`。修改插件加载逻辑时同时检查这两条路径、授权失败回退和模块缓存，避免把仅适用于本地路径的导入写进发布流程。

## 构建与验证

- 后端以根 POM 的 Java 21 和 Spring Boot 版本为准；前端以各自模块的 `package.json` 和 Maven POM 中的 Node/npm 配置为准。`core/core-frontend` 与 xpack 前端有独立依赖及构建脚本，不要凭 README 的概述或另一模块的版本推断当前模块版本。
- 后端修改优先对受影响模块运行定向编译或测试，并检查其上游 SDK 依赖。因为根聚合只包含 SDK，构建 `core` 或 `de-xpack` 时应显式选其 POM，并确认依赖已可解析。
- 前端修改按所在模块选择验证命令：核心前端提供 `npm run ts:check`，xpack 前端以各自 `package.json` 的脚本为准。核心前端的 Vite 配置区分 `dev`、`base`、`distributed`、`lib`；`build:base`、`build:distributed` 还会执行 `build:flush`。Maven 打包也可能复制或移动前端 `dist` 到后端静态资源目录，运行前检查脚本副作用。需要检查代码风格时先确认脚本是否带 `--fix`；核心前端的 `npm run lint` 和 `npm run lint:stylelint` 会写入文件。
- 对数据库、权限、数据查询和跨模块 API 变更增加或运行能验证行为的测试；对纯文案、低风险样式等改动采用与影响相称的检查。报告实际运行的验证和未覆盖的部分，不宣称未执行的检查通过。

## 提交与改动范围

- 遵循根目录和前端各自的 `.editorconfig`，保持相邻文件现有格式。改动聚焦当前任务，不顺手重排无关文件、修改生成代码或清理他人工作区。
- 开始和结束时分别查看主仓库与 `de-xpack` 的 `git status`。保留已有未提交、已暂存和子模块改动；提交或推送时只包含本次任务的文件，并按上述各仓库流程操作。
- 修改公共契约、数据库迁移、构建配置或子模块引用时，说明受影响模块、兼容性、验证方式和未解决风险。重大改动按 `CONTRIBUTING.md` 的建议拆成便于审查的小变更。
