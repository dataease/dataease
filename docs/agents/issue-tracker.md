# Issue tracker: GitHub / TAPD

适用范围遵循根目录 `AGENTS.md` 的 Agent skills 节。

## 平台与工作项定位

- 根据用户提供的链接、明确说明或当前任务已确认的来源，选择 GitHub 或 TAPD。
- 同一任务沿用已确认的平台和工作项，无需重复询问。
- 新建规格（spec）或实施任务（tickets）时，以当前发布批次的明确指令或已确认上下文确定平台；
  不仅凭上一批的目的地创建。同一批次不逐票重复确认。
- 来源不明确且需要读取或发布工作项时，先询问平台及必要的仓库或项目。
  普通讨论和本地开发不要求先创建工作项。
- 同时关联 GitHub 和 TAPD 时，分别保留引用；写入目标不明确时先询问。
- 不自动跨平台复制、同步、评论或关闭工作项。
- 读取或分析不自动授权写操作；写操作按用户任务及明确调用的技能授权执行。
- 发布规格、发布任务、同步附件、调整状态及开始实施分别按已获授权范围执行；
  历史讨论阶段的限制不覆盖用户后来明确给出的实施指令。
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

- 只读定位与分析：仅读取 TAPD 约定的 [项目与来源核对](tapd-publishing.md#项目与来源核对)
  和 [连接](tapd-publishing.md#连接) 两节，再读取目标单据及相关资料。
- 创建、更新规格、任务、依赖、附件及交付记录：操作前读取完整的 [TAPD 发布约定](tapd-publishing.md)，
  按实际写入范围执行发布维护与回读流程。

### Wayfinding operations

使用 wayfinder 在 TAPD 建图或续接时，以地图链接为入口。采用 Task 承载时：

| 对象     | 载体与关联                                                                                                                                       |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| 地图     | 独立 Task，原生 `label` 为 `wayfinder:map`；保存 Destination、Notes、任务导航与直接依赖、已决摘要、待明确范围和范围外事项                        |
| 决策票   | 独立 Task，原生 `label` 为对应的 `wayfinder:research`、`wayfinder:prototype`、`wayfinder:grilling` 或 `wayfinder:task`；正文 `Parent` 链接到地图 |
| 来源需求 | 通过地图和决策票中的来源链接引用，保留原有需求内容                                                                                               |

地图是唯一续接入口和规划主版本，在“任务与依赖”中集中维护全部决策票的标题、真实链接及直接前置票。
索引只保存导航和依赖，决策详情及解决评论保存在各票，状态和领取情况从任务实时读取。
来源需求仅作为参考，不承载地图的任务索引。地图与决策票默认不挂靠来源需求（`story_id` 为 `0`），
Task 地图的父子关系通过 `Parent` 表达；`story_id` 只能填需求 ID，不能填地图 Task ID。
优先使用当前实体可写且可回读的原生阻塞关系；MCP 不支持 Task 阻塞时，
在决策票正文用 `Blocked by` 列出直接前置票的标题与真实链接，并注明这是正文约定。
Wayfinder 票使用上述类型标签，按决策流程推进，不套用实施任务的 `ready-for-agent`。

新会话续接步骤：

1. 读取地图最新的 Destination、Notes 和“任务与依赖”，确认本次范围、阶段、分支和连接方式。
   地图负责人的设置不代表其决策票已被领取。
2. 按地图索引中的任务 ID，用 `get_stories_or_tasks` 查询同一项目的 `tasks`，显式设置 `limit` 并读完分页，
   返回字段包含 `id,name,description,label,status,owner,story_id,created`。
   核对类型标签、`Parent` 指向当前地图、各票 `Blocked by` 与地图索引一致，按需读取前置票的实时记录。
   发现缺票或关系不一致时核对最新记录并补齐，不通过来源需求代替地图定位任务。
3. 前沿为未完成、处理人为空且全部直接前置票为 `done` 的决策票。
   用户指定票时先核对该票，否则按地图索引顺序选择第一张；开展工作前设置实际推进者为处理人并回读。
   已被领取的票按领取记录核对是否属于本次续接，保留其他会话的领取。
4. 按类型解决后，先发表解决评论，再将决策票设为 `done`，向地图 Decisions so far 增量加入摘要和链接。
   新增或调整决策票时同步地图索引、Parent 及各票直接依赖；完成后回读。本轮解决数量遵循 wayfinder。

交接给新会话时提供地图 Task 链接；范围、阶段和分支以地图 Notes 为准。
同一目标的新问题继续加入原地图，跨出目标的需求先明确范围调整或另建地图。

## 资料归属

- 任务专属规格、方案比较、实验报告和实验脚本随选定平台的规格正文及附件维护；
  本地临时文件作为工作稿，交接提供稳定的规格入口及附件索引。
- Git 保存通用项目规范、长期架构决策、产品维护文档、实施代码和正式测试。
  仅在用户明确要求或确有长期维护用途时保留任务资料副本，并说明主版本和同步方式。
- 业务术语和架构决策遵循 [领域文档约定](domain.md)；拟引入概念标明尚未实现。
  XPack 内部资料遵循根目录的专有边界，不复制到开源主仓库或公开工作项。

## 技能指令解释

- “publish to the issue tracker”：在已确定的平台创建相应工作项；
  平台、项目或对象类型不明确时先确定。
- “fetch the relevant ticket”：从已确定的平台读取对应工作项及相关讨论。
- PRs as a request surface: no.
