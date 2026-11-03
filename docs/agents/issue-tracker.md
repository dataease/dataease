# Issue tracker: GitHub

适用范围遵循根目录 `AGENTS.md` 的 Agent skills 节。

默认问题跟踪仓库为 `dataease/dataease`，使用 `gh` CLI。
开发、提交和跨仓库边界遵循根目录 `AGENTS.md`。

## 工作项定位

- 调用时显式指定 `--repo dataease/dataease`，避免在子模块中误操作其他仓库。
- 用户提供其他仓库或 TAPD 工作项时，按明确来源读取；不自动复制到 GitHub。
- 首次写入前使用 `gh repo view dataease/dataease --json nameWithOwner,url` 核对目标。
- GitHub Issue 和 PR 共用编号空间，先确认对象类型。
- 发布前核对开源边界；XPack 专有实现和内部资料不得写入公开 Issue。

## 常用操作

- 读取：`gh issue view <number> --repo dataease/dataease --json number,title,body,labels,comments`
- 列表：`gh issue list --repo dataease/dataease --state open --json number,title,labels`，按任务增加过滤和分页。
- 创建：`gh issue create --repo dataease/dataease --title "<title>" --body-file <file>`
- 评论：`gh issue comment <number> --repo dataease/dataease --body-file <file>`
- 标签：`gh issue edit <number> --repo dataease/dataease --add-label "<label>"`，移除使用 `--remove-label`。
- 关闭：先发表处理结论，再执行 `gh issue close <number> --repo dataease/dataease`。
- 多行正文写入临时文件，再通过 `--body-file` 传递。

读取或分析工作项不自动授权创建、评论、标签修改或关闭；
写操作按用户任务及其明确调用的技能授权执行。

## 父子关系与阻塞关系

优先使用 GitHub 原生子 Issue 和依赖关系。
使用前检查当前 `gh` 帮助及仓库支持情况。

原生能力不可用时：

- 子 Issue 正文注明 `Part of #<parent>`，父 Issue 用任务列表引用子项。
- 被阻塞 Issue 正文注明 `Blocked by: #<number>`。
- 所有阻塞项已关闭才视为解除阻塞。

## 技能指令解释

- “publish to the issue tracker”：创建 GitHub Issue。
- “fetch the relevant ticket”：读取对应 Issue 及相关评论。
- PRs as a request surface: no.
