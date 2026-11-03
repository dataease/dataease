# DataEase 项目 MCP

[Codex 官方 MCP 文档](https://developers.openai.com/codex/mcp)支持受信任项目从 `.codex/config.toml` 加载 MCP 配置。同名项目配置覆盖个人配置，其他个人 MCP 保留。修改后重启 Codex 任务。启动脚本路径相对于仓库根，应从 DataEase 仓库根启动会话。

每位开发者按需复制各 MCP 目录的 `.env.example` 为同目录 `.env.local`，填写个人令牌及本机 MCP 入口；原有 TAPD 配置还需填写项目 ID 和用户名，官方实现配置见下文。值使用 JSON 字符串语法（双引号，特殊字符需要转义）。`NODE_BIN` 可填写 Node 可执行文件绝对路径。Playwright 的测试 URL 和登录账号密码仅保存在本地；启动器不会自动登录，浏览器操作需在具体任务中明确指定。

浏览器测试按 `tests/browser/README.md` 使用 Playwright CLI 复现、Playwright Test 回归，不使用 Playwright MCP。项目配置已将 `[mcp_servers.playwright]` 设为 `enabled = false`；保留启动器作为历史配置，不自动启用或回退。修改配置后重启 Codex 任务，使工具列表重新加载。

CLI 快照、截图和 Test 报告位于 `tests/browser/output/`，登录状态按需保存到 `tests/browser/.auth/`，均被 Git 忽略。旧 Playwright MCP 的浏览器资料和输出仍留在 `.codex/playwright_mcp/` 下的本地忽略目录中。建议用 `chmod 600` 限制凭据文件权限。

TAPD 开放配置中的读取工具及既有的 `update_bug`、`update_story_or_task`、`create_comments`。写入仅在用户授权的任务范围内执行；更新前读取实体最新内容和适用工作流，更新后回读核对。不得将代码已提交等同于已合并、已部署或已验证。查询“我的待办”时，默认查询整个项目的需求 `owner` 和缺陷 `current_owner`，不限定迭代。成员接口若返回 403，需相应接口权限。

## TAPD 实现选择

`config.toml` 仅注册一个 `tapd` 服务，通过 `launch_tapd.py` 按本地配置选择实现：

| 本地配置                            | 使用的实现                     |
| ----------------------------------- | ------------------------------ |
| 存在 `tapd_official_mcp/.env.local` | 官方 Python `mcp-server-tapd`  |
| 仅存在 `tapd_mcp/.env.local`        | 原有 Node / Python TAPD 启动器 |
| 两者均不存在                        | 提示创建本地配置，不启动服务   |

两份配置同时存在时，官方实现优先。选中后如果配置、入口或认证有误，直接报错，不自动切换实现或凭据。需要切回原实现时，移走官方 `.env.local` 并重新加载 MCP。不要仅复制空模板而不填写；暂不使用 TAPD 时可将其 `enabled` 设为 `false`。

官方实现参考 SQLBot 的独立启动器，来源为 [TAPD 官方 MCP](https://cnb.cool/tapd.cn/skills/mcp-server-tapd)。参考实现使用的 `mcp-server-tapd==8.0.83` 要求 Python 3.13+；使用独立工具环境，不改变产品运行环境。未安装时可执行：

```bash
uv tool install --python 3.13 'mcp-server-tapd==8.0.83'
uv tool dir
```

创建项目本地配置：

```bash
cp .codex/tapd_official_mcp/.env.example .codex/tapd_official_mcp/.env.local
chmod 600 .codex/tapd_official_mcp/.env.local
```

填写个人 `TAPD_ACCESS_TOKEN`，将 `uv tool dir` 输出目录下的 `mcp-server-tapd/bin/mcp-server-tapd` 填入 `TAPD_MCP_ENTRY`，同目录的 `python` 填入 `PYTHON_BIN`。已有独立安装可直接使用，无需重新安装。值使用 JSON 字符串语法；不支持 shell 展开、单引号或行尾注释。不要复制其他项目的凭据、项目 ID 或浏览器会话。

官方实现不要求预填项目 ID 或用户名。通过 `get_user_participant_projects` 查找项目并确认 DataEase 对应的 `workspace_id`，后续查询和更新须显式传入该 ID。两个实现共用工具白名单和上述写入授权规则。

官方启动器仅加载自己的 `.env.local`，清除继承的 TAPD 身份和地址变量，设置 `PYTHON_DOTENV_DISABLED=1` 以禁用服务自动加载其他 `.env`。本地配置已被 Git 忽略；模板不含令牌。离线验证不代表认证有效，配置后需重新加载 MCP 并执行握手和只读查询。

## DBHub

`.codex/dbhub_mcp/` 提供数据库 MCP 配置模板和只读启动器。开发者可按 [DBHub 官方说明](https://github.com/bytebase/dbhub)安装 `@bytebase/dbhub@1.3.1` 到个人的 `~/.codex/mcp/dbhub/`，并在 `.env.local` 中填写实际安装入口和自己的只读数据库 DSN。由于示例中没有可安全使用的数据库连接信息，`config.toml` 默认设为 `enabled = false`；填写 DSN 后再改为 `true` 并重启任务。示例 DSN 仅作格式示意，不是项目真实连接。

启动器通过环境变量传 DSN，不在命令参数中暴露密码。DBHub 1.3.1 的只读限制在 `dbhub.toml` 中对 `execute_sql` 设置；数据库账号本身仍应授予只读权限，因为应用层 SQL 检查不能替代数据库权限。不要用生产或共享环境做配置验证。

## Node / Python 入口

各服务启动器共享 `mcp_runtime.py`，支持 `MCP_RUNTIME="auto"`（默认）、`"node"` 和 `"python"`；官方 TAPD 启动器仅接受 Python 入口。
`auto` 识别 `.js/.cjs/.mjs`、`.py` 和带 Node/Python shebang 的入口。
入口必须填写绝对路径；`NODE_BIN` / `PYTHON_BIN` 支持绝对路径或 PATH 中的命令名。
桌面进程的 PATH 可能与终端不同，推荐填写解释器绝对路径。Python MCP 应使用安装它的虚拟环境解释器。
启动配置文件的 `command="python3"` 是外层启动器（要求 Python 3.9+）；`PYTHON_BIN` 只决定内部 MCP 的解释器。

原有 TAPD 启动器支持 Node 实现，以及 Python 包 `mcp-server-tapd` 的 `bin/mcp-server-tapd` 入口。
Python 模式将 `TAPD_API_TOKEN` 映射到 `TAPD_ACCESS_TOKEN`，将 `TAPD_CURRENT_USER` 映射到 `CURRENT_USER_NICK`，强制使用 stdio。
可用 `TAPD_API_BASE_URL`、`TAPD_BASE_URL` 指定服务地址，默认使用 TAPD 官方站点。
不要指向另行加载个人 credentials.env 的包装脚本，以免覆盖项目凭据。
配置保留两种实现的工具白名单；Python 查询须显式传入本项目 `workspace_id`，不会自动将项目 ID 注入每次调用。

运行时兼容不代表任意同名 MCP 的参数和 API 兼容：Playwright 入口须支持现有 Chrome/profile/output 参数，DBHub 入口须支持现有 `--config` 和只读配置。
Playwright 和 DBHub 的已验证安装包是 Node 实现，不需要改成 Python。
TEST\_\* 字段仍由执行测试的代理按需使用，不自动登录；DBHub 的默认禁用状态保持不变。

离线回归：`python3 -B .codex/tests/test_mcp_runtime.py`，覆盖实现选择、失败不回退、凭据隔离和启动参数，使用临时配置和模拟进程启动，不访问 TAPD 或数据库。
