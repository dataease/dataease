# DataEase 项目 MCP

受信任项目会从 `.codex/config.toml` 加载 MCP 配置。同名项目配置覆盖个人配置，其他个人 MCP 保留。修改后重启 Codex 任务。

每位开发者按需复制各 MCP 目录的 `.env.example` 为同目录 `.env.local`，填写自己的 TAPD 项目 ID、用户名、个人令牌，以及本机 MCP 入口。值使用 JSON 字符串语法（双引号，特殊字符需要转义）。`NODE_BIN` 可填写 Node 可执行文件绝对路径。Playwright 的测试 URL 和登录账号密码仅保存在本地；启动器不会自动登录，浏览器操作需在具体任务中明确指定。

浏览器资料和截图分别保存在 `.codex/playwright_mcp/browser-profile/` 与 `output/`。本地凭据、浏览器资料和输出目录均被 Git 忽略。建议用 `chmod 600` 限制凭据文件权限。

TAPD 仅开放配置中的读取工具。查询“我的待办”时，默认查询整个项目的需求 `owner` 和缺陷 `current_owner`，不限定迭代。成员接口若返回 403，需相应接口权限。

## DBHub

`.codex/dbhub_mcp/` 提供数据库 MCP 配置模板和只读启动器。开发者可按 [DBHub 官方说明](https://github.com/bytebase/dbhub)安装 `@bytebase/dbhub@1.3.1` 到个人的 `~/.codex/mcp/dbhub/`，并在 `.env.local` 中填写实际安装入口和自己的只读数据库 DSN。由于示例中没有可安全使用的数据库连接信息，`config.toml` 默认设为 `enabled = false`；填写 DSN 后再改为 `true` 并重启任务。示例 DSN 仅作格式示意，不是项目真实连接。

启动器通过环境变量传 DSN，不在命令参数中暴露密码。DBHub 1.3.1 的只读限制在 `dbhub.toml` 中对 `execute_sql` 设置；数据库账号本身仍应授予只读权限，因为应用层 SQL 检查不能替代数据库权限。不要用生产或共享环境做配置验证。
