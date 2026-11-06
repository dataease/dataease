# Issue tracker: GitHub / TAPD

适用范围遵循根目录 `AGENTS.md` 的 Agent skills 节。

## 平台与工作项定位

- 根据用户提供的链接、明确说明或当前任务已确认的来源，选择 GitHub 或 TAPD。
- 同一任务沿用已确认的平台和工作项，无需重复询问。
- 来源不明确且需要读取或发布工作项时，先询问平台及必要的仓库或项目。
  普通讨论和本地开发不要求先创建工作项。
- 同时关联 GitHub 和 TAPD 时，分别保留引用；写入目标不明确时先询问。
- 不自动跨平台复制、同步、评论或关闭工作项。
- 读取或分析不自动授权写操作；写操作按用户任务及明确调用的技能授权执行。
- 开发、提交及公开内容边界遵循根目录 `AGENTS.md`。

## GitHub

GitHub 平台下的默认仓库为 `dataease/dataease`，使用 `gh` CLI。
用户明确指定其他仓库时，以下命令中的仓库参数相应替换。

- 调用时显式指定 `--repo dataease/dataease`，避免在子模块中误操作其他仓库。
- 首次写入前使用 `gh repo view dataease/dataease --json nameWithOwner,url` 核对目标。
- GitHub Issue 和 PR 共用编号空间，先确认对象类型。
- 发布前核对开源边界；XPack 专有实现和内部资料不得写入公开 Issue。

### 常用操作

- 读取：`gh issue view <number> --repo dataease/dataease --json number,title,body,labels,comments`
- 列表：`gh issue list --repo dataease/dataease --state open --json number,title,labels`，按任务增加过滤和分页。
- 创建：`gh issue create --repo dataease/dataease --title "<title>" --body-file <file>`
- 评论：`gh issue comment <number> --repo dataease/dataease --body-file <file>`
- 标签：`gh issue edit <number> --repo dataease/dataease --add-label "<label>"`，移除使用 `--remove-label`。
- 关闭：先发表处理结论，再执行 `gh issue close <number> --repo dataease/dataease`。
- 多行正文写入临时文件，再通过 `--body-file` 传递。

### 父子关系与阻塞关系

优先使用 GitHub 原生子 Issue 和依赖关系。
使用前检查当前 `gh` 帮助及仓库支持情况。

原生能力不可用时：

- 子 Issue 正文注明 `Part of #<parent>`，父 Issue 用任务列表引用子项。
- 被阻塞 Issue 正文注明 `Blocked by: #<number>`。
- 所有阻塞项已关闭才视为解除阻塞。

## TAPD

- 根据工作项链接或已确认上下文定位项目、对象类型和编号；
  不将 TAPD 编号作为 GitHub Issue 编号。
- 优先使用可用的 TAPD 连接工具；能力不足时使用已登录浏览器。
  无法访问时说明限制，请用户提供必要正文或访问方式。
- 需求、缺陷及其状态、字段、父子关系，以所在项目的实际配置为准。
- `docs/agents/triage-labels.md` 中的 GitHub 标签不直接映射为 TAPD 状态；
  需要分诊时先核对项目流程，映射不明确时询问。

## 技能指令解释

- “publish to the issue tracker”：在已确定的平台创建相应工作项；
  平台、项目或对象类型不明确时先确定。
- “fetch the relevant ticket”：从已确定的平台读取对应工作项及相关讨论。
- PRs as a request surface: no.
