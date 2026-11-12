# AI 服务配置与原生 Spring AI 接入

本文维护模型管理、使用设置、同步 SDK 和对话助手的验证入口。基础配置与同步 SDK 的原始交付不包含工具执行和流式调用；后续对话能力见 [对话助手回归](#对话助手回归)，其契约与生命周期见 [开发说明](../../docs/ai/conversation.md)。社区版可以使用；调用记录仍仅提供菜单和说明占位页。

需求：[AI 服务配置与统一调用基础](https://www.tapd.cn/tapd_fe/40899682/story/detail/1140899682001022747)。原型：[模型管理](https://dataease-ai-studio-20261010.reaperww.chatgpt.site/ai-service/?tab=models)、[使用设置](https://dataease-ai-studio-20261010.reaperww.chatgpt.site/ai-service/?tab=usage)。实施范围采用本轮确认的设计收敛结果，覆盖需求中的配置部分。

## 代码与边界

| 位置                                                          | 职责                                                          |
| ------------------------------------------------------------- | ------------------------------------------------------------- |
| `sdk/api/api-base/.../api/ai/config`                          | 管理 HTTP 契约、安全 DTO                                      |
| `sdk/api/api-base/.../api/ai/runtime`                         | 原生 Spring AI 模型提供器、可信上下文、业务数据范围与授权 SPI |
| `core/core-backend/.../ai/dao`                                | 三张配置表及 JPA Repository                                   |
| `core/core-backend/.../ai/manage`                             | 配置协调、发现/测试、身份及运行策略解析                       |
| `core/core-backend/.../ai/support`                            | 密钥、出站边界、参数白名单、HTTP 执行与 JVM/Redis 准入        |
| `core/core-backend/.../ai/server`                             | 管理 API 实现                                                 |
| `core/core-backend/.../listener/sql/AiConfigurationInit.java` | `2.44` 建表及 `2.45` 菜单兼容迁移                             |
| `core/core-frontend/src/views/system/ai-service`              | 实际配置页面、模型抽屉、组织及限制抽屉                        |
| `tests/browser/fixtures/ai-service`                           | 加载上述真实组件的隔离浏览器夹具                              |

Spring AI 锁定 `1.1.8`，沿 Java 21 / Spring Boot 3.5.15，不升级 Boot。公开 AI Java 契约直接使用 Spring AI 类型；管理 HTTP DTO 不含 Spring AI 对象。只引入所需的 ChatClient 和 OpenAI 模型库，没有增加默认提供商自动配置或工具执行入口。Apache HttpClient 5 沿 Boot 管理版本，用于受控传输、DNS 解析及硬超时；模型协议、消息和响应仍由 Spring AI 处理。

既有 `AiBaseService` 属于旧集成，保持其契约，未自动迁移或删除它的业务调用方。

## 部署参数

在服务端外置配置或对应环境变量中设置；不要将真实值提交到仓库。

| 参数                     | 规则                                                                                       |
| ------------------------ | ------------------------------------------------------------------------------------------ |
| `dataease.ai.keys`       | 可选的外置 key ring：`keyId:Base64编码的32字节密钥`，多项逗号分隔                          |
| `dataease.ai.active-key` | 使用外置 key ring 时必填，当前写入/签名使用的 keyId                                        |
| `dataease.ai.namespace`  | Redis 准入必填；同部署所有节点一致，不同部署不同；字母、数字、下划线或短横线，最长 64 字符 |

未提供上述两个密钥参数时，全新部署在迁移完成后自动生成独立的 32 字节随机 AI 密钥，保存在现有 `core_ai_setting` 行，不增加表、不复用 RSA/AES 密钥。`key_source`、`key_ring`、`active_key_id`、`key_revision` 和 `key_fingerprint` 是内部字段，不通过设置 DTO、实体 JSON 或配置接口读写。数据库模式的密钥与模型密文同库，完整数据库备份也包含解密材料；需要存储分离的部署继续使用外置 Secret。

`2.47` 仅幂等补列，不生成密钥。`AiKeyringStarter` 在 `InitSqlListener` 之后执行首次初始化：各节点在独立短事务中锁定同一设置行，只有第一个节点生成，其他节点等待提交并读取。事务提交前不会发布候选密钥；重启、扩容不重新生成。当前迁移框架没有集群迁移锁，首次安装/升级应先由单节点完成全部迁移，再启动其余节点；不能把密钥初始化并发安全当作所有 DDL 并发安全。

已有手工 key ring 的部署升级时，必须保留原 keyId、密钥和 active-key，并让所有节点一致。首次登记逐一校验存量模型密文；外置模式只在设置行登记来源、active-key、版本和摘要，不复制密钥原文到数据库。显式配置不完整、格式错误或与已登记来源/摘要不一致时拒绝 AI 密钥操作，不自动回退数据库模式。有历史密文但没有原密钥，或初始化标记存在而密钥损坏/丢失时，必须恢复原配置或配套备份，禁止重新生成覆盖。启动初始化失败仅记录安全提示，原有非 AI 功能不依赖该密钥；修复后重启节点重试初始化。

密钥按用途派生加密、连接指纹和签名子密钥。密文格式和模型 ID 绑定的 AES-GCM 保持兼容。每次加解密、指纹或签名操作直接查询共享密钥字段，绕过实体一级缓存；不进入 Redis/JCache，也不使用长期 JVM 密钥快照。来源、active-key、内容摘要或记录异常、数据库不可读时拒绝操作，不使用明文或固定密钥回退。

轮换不自动执行，也不新增 HTTP 管理入口。内部维护方法 `AiKeyringManage.replace(expectedRevision, ring, active)` 使用设置行锁和密钥版本校验，要求 AI 已停用/排空、完成备份，且新 ring 保留全部旧 keyId 及原始密钥；它不允许删除旧密钥。数据库模式在事务中整体切换，新节点下一次操作读取新版本；外置模式先将完整新旧 Secret 分发到待启用节点，再由持有旧配置的维护进程登记新摘要和 active-key，随后启用新配置节点，旧配置节点此后拒绝操作。该方法是内部维护原语，本次未提供运维脚本或界面，不能通过普通设置保存触发。轮换不与旧版节点混跑；首次滚动升级全部完成前保留原外置 key ring 和 active-key。

新密钥用于后续写入，旧模型保存时仍可按原逻辑重包装；旧密文及短期证明仍依赖旧密钥，不能删掉。数据库密钥字段最多容纳 4000 个 ASCII 字符，超长轮换会安全拒绝；外置模式不新增密钥数量限制。本次不提供退役密钥删除工具。备份和恢复必须同时覆盖模型密文、设置行密钥字段及全部旧密钥；外置模式还需独立备份 Secret。数据库模式与外置模式之间不自动切换。

模型地址由有配置权限的用户在页面维护，支持有效的 HTTP/HTTPS 基础地址、公网或内网域名、IPv4/IPv6、本机服务和 1–65535 自定义端口，保留 `/gateway/v1` 等路径。地址不允许夹带用户名、密码、查询参数或片段；API Key 通过专用字段配置。格式校验不执行 DNS，请求连接时才按服务器 DNS 解析；不因内网或代理 Fake-IP 结果拒绝，不再使用 `dataease.ai.allowed-hosts` / `dataease.ai.allowed-ports`，已有同名配置不再参与判断。新增服务无需修改后端配置或重启。

保留系统默认 HTTPS 证书及主机名校验，禁用重定向与自动重试。错误区分地址格式、DNS 解析、连接、TLS、认证和超时，不透传上游异常正文。SDK 仅使用已配置的模型连接，不能通过业务参数覆盖 URL 或认证头；已有部署网络策略仍由部署环境执行。

缓存选用 Redis 或运行 distributed profile 时使用 Redis 原子准入；缺失 Redis、namespace 或协调结构时拒绝新调用，不能退回各节点 JVM 计数。全新 namespace 或许可结构丢失进入 RECOVERING，等待旧请求最大期限 300 秒及 10 秒宽限后，由持有元数据库配置协调行的恢复器重建；此期间发现、测试也可能暂时拒绝。

Redis 主从异步复制发生故障切换时，不能承诺任何故障下的绝对上游物理并发上限。关闭本地传输也不能证明供应商停止计算或计费。

## 数据与保存

管理写接口沿用 DE 当前认证与网关支持的方法：设置保存 `POST /ai/settings`、模型编辑 `POST /ai/models/{id}`、启停 `POST /ai/models/{id}/enabled`。不使用会被现有认证过滤器提前拒绝的 PUT，也不为 AI 功能放宽全局方法规则；既有参数校验、权限、配置加密、版本及影响确认保持生效。

仅有三张新表：

- `core_ai_setting`：总开关、默认模型、样本开关、五项限制、策略 revision 和 JPA 行版本。
- `core_ai_model`：稳定 ID、规范化唯一名称、连接信息、信封密文（有界字符串 8192 字符，不使用 PostgreSQL OID LOB）、连接/凭据/测试资格版本、最近测试摘要及删除占位。
- `core_ai_org_policy`：真实组织 ID、INHERIT / SPECIFIED / DISABLED、指定模型及行版本；物理策略列名使用 `policy_mode` 避免数据库保留字。没有记录等同继承系统默认。

新增抽屉先取得绑定身份和期限的签名草稿句柄及稳定 ID。模型发现只返回候选标识；基础生成和结构化测试均成功后才签发 10 分钟 testProof。证明绑定当前身份及组织/代理、目标 ID、连接基线、候选连接 HMAC、测试契约和输出参数能力，可在有效期内复用，不建设 receipt 表或一次性消费机制。

基础测试仅在上游 400/422 有界错误结构明确给出 `code=unsupported_parameter`、`param=max_tokens` 时，最多使用 `max_completion_tokens` 协商一次。普通 400、认证、限流、网络或超时不触发；基础及结构化两项合计最多 3 次实际 HTTP，每次重新鉴权/准入并共用截止时间。结构化项使用已确定参数和 `response_format.type=json_object`，提示词包含 JSON 示例。DE 严格检查响应仅为 `{"ok":true}`：不接受错误类型、false、额外字段、非法 JSON 或尾随内容；不因 HTTP 200 放行，不通过取消结构化检查回避失败。不要求所有 OpenAI 兼容服务都实现原生 `json_schema`，不对任意 400 进行格式降级重试。测试证明契约更新为 3，旧的短期证明需重新测试，已保存的模型配置保持不变。SDK 显式传入的结构化格式仍按业务参数执行，不自动转换或宣称测试通过即支持所有 JSON Schema。

正式新增使用 `EntityManager.persist`，重复 ID 不覆盖原模型；编辑检查预期行版本；连接变化须重新验证 testProof，仅改名称不要求重测。名称 trim 后采用 NFKC + Locale.ROOT 小写规范化唯一键。数据库约束、版本冲突和清空均在短事务中处理，不依赖 Repository 非空合并切面完成 PATCH 或并发控制。

删除先检查默认及全部组织引用，保留不可复用的 ID 占位并清除密钥、名称及连接内容；释放名称唯一键。旧新增请求不能在删除后复活同一 ID。这里提供防重复创建及防陈旧覆盖，不承诺历史响应重放或 exactly-once。保存结果未知时前端查询原 ID 并保留候选；当前查到模型不等于已证明本次所有字段保存成功。

配置写入通过同一 setting 行 CAS 协调引用、删除和组织批量变更。外部模型调用不占元数据库事务。设置发布先建立运行屏障，再进入自有短事务，提交后发布实际数据库 revision；外层已有事务时拒绝该配置入口，避免在外层提交前发布。发布失败返回已持久化/已生效版本及运行状态，恢复前保持阻断。

组织删除通过权限 SDK 内部契约 `AiOrganizationLifecycle.beforeDelete` 加入原删除事务，使用相同协调顺序并移除组织策略。批量保存只处理明确 ID，不展开子组织；单批最多 2000 项，任一目标、资格或版本失败整批回滚。组织目录与策略批量读取，影响确认同时绑定配置版本及完整组织目录指纹，组织新增/重命名也使陈旧确认失效。

## 权限

`2.46` 迁移在系统设置下建立 `AI 服务` 目录（ID141），下设模型管理（保留 ID140）和调用记录（ID142）。配置页面保留“模型管理”“使用设置”两个页签，调用记录只展示说明，不调用配置 API 或提供记录 API。两个叶子菜单独立参与现有权限矩阵，未将旧配置授权复制为记录授权；目录没有独立功能授权。已有配置权限仍绑定 ID140。旧 `/sys-setting/ai-service` 入口跳转当前身份可访问的首个子菜单，新配置和占位页路径分别为 `/sys-setting/ai-service/models`、`/sys-setting/ai-service/records`。无可见子菜单时目录隐藏。

既有菜单只有“使用”权重 1 与“授权”权重 9；配置页全部功能复用配置菜单使用权，没有新增只读配置档位或独立 AI 授权表。权限扩展同步识别 AI 目录下的叶子菜单属于系统设置，保留系统菜单的用户授权边界及组织菜单过滤，不因多一层目录自动授予普通业务身份。

登录认证完全复用 DE 原有机制，不增加登录流程。AI 使用后端统一能力判定：

- 有效许可：保留已有菜单使用权、组织和代理资格校验；排队后的维护请求仍检查捕获身份。
- 社区模式：涵盖未安装 XPack 的真实替补部署，以及已安装 XPack 但许可管理源明确返回无记录、无效或过期的环境。加载 XPack 时，通过内部 `checkAiCommunityAllowed` 复用原认证账号的管理员和有效状态检查，不读菜单矩阵或组织。纯替补部署必须确实存在替补认证，不能仅凭 uid=1 或缺失权限 Bean 放行。
- 状态未知、许可源异常或扩展缺失：拒绝操作，不能推断为社区。模式读取来自现有 `F2CLicManage`，不把工作线程空的 `LicenseUtil` / `CommunityUtils` ThreadLocal 当作许可状态。

私有权限扩展仅实现公共身份校验和最小组织目录契约，不向主仓库复制实现。新社区校验和原 AI 校验/目录方法均排除许可证拦截；旧扩展未实现新契约时拒绝，不做异常降级。

有效许可模式的 AI 配置授权能管理全局 AI 组织策略，因此内部目录返回全部真实组织的 ID、名称和父 ID；它不授予组织管理或业务数据权限。普通业务用户调用 AI 不需要配置菜单权，但其已注册功能必须校验当前资源、数据与行列权限。

社区模式只应用系统默认模型、全局开关、数据范围和调用限制。`Settings.organizationPolicyAvailable=false`，前端隐藏整个组织配置卡片并停止请求组织目录；组织读取返回明确的不可用能力，组织写入拒绝。能力字段缺失或状态不可确认时显示加载失败。社区身份和任务不接受组织/代理参数，不把企业代理身份转换成社区管理员。

三表保持不变；历史企业组织策略冻结保留，不参与社区调用路由或当前影响组织统计。模型列表通过 `historicalOrganizationReferences` 标识冻结引用，当前组织引用计为 0；删除仍受历史引用保护并说明原因，避免恢复许可后出现悬空引用。恢复有效许可后，按现有组织、权限和模型可用性重新解析策略。

已创建的 SDK 执行上下文、维护调用、草稿、测试证明和影响确认绑定当前部署模式及许可记录状态指纹。每次实际发送前及配置操作边界复查同一快照，发生变化拒绝旧上下文。读取许可记录前后摘要不同或验证结果不明确时拒绝，不在同一次多步骤测试中接受新的模式。该指纹描述现有许可管理源的当前记录版本和验证状态，不新增许可变更历史或承诺检测每一个中间切换；更新与缓存传播沿用现有许可管理机制。

## 原生 SDK 接入

业务注册 `AiFeature` Bean，提供固定功能码及以下检查：

- `authorize(context)`：当前资源、组织及权限版本。
- `authorizeInput(context, finalPrompt)`：模板及 Advisor 增补后的最终输入是否符合该业务的授权数据范围；收到的是深拷贝校验副本。
- `checkSampleConfirmation(context)`：含样本时验证当次确认、身份、资源/权限版本、输入摘要与期限。
- 后台功能实现 `taskIdentity(taskId)`，恢复可信执行人、有效组织、代理组织及身份版本；不接受前端后台标记。

非样本调用示意：

```java
AiDataScope scope = new AiDataScope(
    "dataset:123@resource-v5", "permissions-v8", null, false, null
);
AiExecutionContext context = contextFactory.current("registered-sql-feature", scope);
ChatModel model = modelProvider.current(context);
ChatResponse response = ChatClient.builder(model).build()
    .prompt(new Prompt("由业务准备并授权的必要上下文"))
    .call()
    .chatResponse();
// 业务自行转换和校验 SQL/图表等结果，再沿原确认、应用和保存流程处理。
```

无样本可选绑定 `AiPromptFingerprint.of(finalPrompt)`；样本必须先完成业务确认并绑定该摘要和资源/权限依据。不能把前端传入的“已授权”或“含样本=false”直接当作证据。SDK 不声称可从任意文本自动识别其业务权限；授权 SPI 必须由可信后端功能实现。

上下文绑定当前请求或受控任务，含可信 executionId 与不可续期的共同截止时间；同一操作重试复用上下文，新用户操作创建新上下文。不要把模型代理或绑定它的 ChatClient 放入单例跨请求复用。每次调用及发送前重新检查身份、策略和模型，固定本次连接快照；发生变化拒绝，不在执行中自动换模。后台不发送样本。

只支持同步 `ChatModel.call`；`stream` 明确返回不支持。参数从白名单重新构造，固定已解析模型、n=1 及文本输出；拒绝媒体、工具、认证头、extraBody 和未知扩展。maxTokens / maxCompletionTokens 统一受输出上限约束，不同时发送两者；业务可请求更小值。模型内 `output_token_parameter` 由成功测试和已签名证明确定，运行时把通用预算映射到该字段；显式提供商参数须与能力一致。旧记录迁移默认 MAX_TOKENS，能力变化增加连接版本；业务调用不进行参数协商或自动重试。最终消息、结构化 schema 和 stop 文本按 Unicode code point 计数，另有序列化/响应字节上限。上游未遵守输出参数时不能保证其物理生成量；空、截断或非法响应不作为完整结果返回。

AI 传输组件共享一个 Apache HttpClient 5 连接池，总连接数及单目标连接数均为 40，与执行器容量一致；连接最长使用 5 分钟，空闲 60 秒清理，复用前检查闲置连接。每个请求独立设置租借/响应超时，截止时间和调用方中断只取消该请求，不关闭共享客户端；组件销毁时统一关闭连接池。禁用共享 Cookie、重定向和自动重试，认证头仍按本次模型配置传入。池容量只约束本进程传输资源，业务并发限制继续由 JVM/Redis 准入执行。隔离回归验证跨凭据 TCP 复用、Cookie 隔离、两个并行请求中的超时/中断隔离及后续请求恢复。

网络使用独立有界执行器和共同截止时间，每次实际 HTTP 独立申请许可；无框架或传输隐藏重试。仅传不可变身份/范围，finally 清理线程上下文，不传播事务、EntityManager、连接或受管实体。SDK 拒绝在已有元数据库事务内调用模型；业务保存仍由业务自己的短事务负责。

Spring AI 1.1.8 的 OpenAiChatModel 特定空响应分支会 WARN 完整 Prompt。本实现精确关闭该类 logger，以底座安全错误代替，且有合成敏感标记回归；不要重新启用其正文日志。供应商认证、限流、余额、超时、并发和格式错误采用安全错误，不回传未经处理的上游正文。

## 原型复刻与范围差异

前端逐项核对模型列表、新增/编辑、发现与手动标识、测试反馈、默认切换/清空、停用/删除确认、组织选择及批量、数据范围和五项限制抽屉。600px 抽屉、底栏位置、表格主次文字、主题变量及本期文案沿原型和现有 Element Plus Secondary 组件实现。

本期提供 AI 服务导航分组，包含模型管理及调用记录两个独立菜单；调用记录目前仅为说明占位页。产品不保留原型专用的“请勿填写真实密钥”演示提示；处理位置提示改为连接声明用途，避免暗示已交付调用审计。保存结果未知、真实安全失败原因及运行策略未生效有相应状态反馈。

## 验证

先解析新依赖，不执行产品 Maven 生命周期：

```sh
mvn -f tests/ai-service/dependencies.xml dependency:build-classpath \
  -Dmdep.outputFile=/absolute/path/ai-dependencies.txt
python3 tests/ai-service/run.py --xpack \
  --ai-classpath /absolute/path/ai-dependencies.txt \
  --redis-server /absolute/path/to/existing/redis-server
npm --prefix tests/browser test -- --config=playwright.ai-service.config.mjs
```

Java 夹具使用现有 CoreApplication.jar 读取基线依赖，重新编译本次源码，使用内存 H2 及仅绑定本机的合成模型 HTTP；可选 Redis 参数启动本次任务独占实例并在结束后关闭。没有该参数时不宣称 Redis 动态回归通过。`--xpack` 仅定向编译修改的私有扩展源码，不启动应用或连接产品数据库。所有模型、密钥和业务数据均为合成数据。

浏览器夹具加载真实 Vue 页面、API 封装和 Element Plus Secondary，仅替换 axios、身份 Store 和国际化边界。覆盖新增/编辑、旧响应、失败反馈、未知保存核对、组织隐藏选择及批量、两种尺寸和主题，以及社区组织卡片隐藏、历史引用删除保护和未知能力拒绝。它不替代完整产品登录、真实权限矩阵或网关加解密联调。

全量 `npm run ts:check` 当前被仓库已有 `.vue.js` 入口错误阻断；本次页面另通过相同隔离边界的定向 vue-tsc。Java 回归不依赖项目 Surefire，不能把 Maven 默认跳过测试称为测试通过。

仍需在部署验收环境覆盖实际元数据库矩阵、完整权限矩阵/代理、网关与加密通道的完整成功和拒绝矩阵、集群进程崩溃及 Redis 故障切换、供应商参数兼容性。本地隔离验证不替代这些环境证据。上述隔离回归不启动完整产品；下述真实页面回归访问由 VS Code 启动的现有服务。未访问真实付费模型、未提交或推送代码。

### 真实开发环境页面回归

使用 Playwright CLI 的独立有头会话，手动登录后将状态保存到私有临时文件，再运行：

```sh
AI_SERVICE_LIVE_URL=http://localhost:7070 \
PLAYWRIGHT_STORAGE_STATE=/absolute/private/path/review-auth.json \
npm --prefix tests/browser test -- --config=playwright.ai-service-live.config.mjs
```

该配置不启动产品、不替换网关或后端响应，检查真实菜单和路由、新增签名草稿、手动标识、密钥显隐、放弃修改、组织及批量配置，以及数据范围和五项限制抽屉。取消操作检查没有提交配置；历史回归曾用合成凭据配合被禁止的回环地址验证真实加密通道和失败提示；当前地址策略已允许回环地址，该用例需更新后再用于当前版本验收，不访问真实供应商。不保存模型或全局策略，不能替代新增/编辑成功保存及真实供应商验收。临时认证文件完成后删除。

测试账号可能配置了很短的登录时长；运行前检查认证快照尚未过期，过期时明确拒绝启动，避免把登录跳转误判为页面回归。当前开发账号 Token 有效期约一分钟，应在登录后立即保存状态并运行。社区模式优化前的前后端重启后，六项真实环境用例整批通过，耗时 18.3 秒。本轮新增模式校验使用合成许可管理源、认证 SPI 和隔离组件验证；没有更改实际许可或执行混淆产物专项验收。

实际联调发现早期菜单 ID130 与既有菜单冲突，导致插件加载路径错误。AI 菜单采用独立 ID140；保留 2.44，新增 2.45 兼容迁移，仅移除旧 AI 菜单，不转移属于其他菜单的 130 授权。AI 中文 properties 使用 Unicode 转义，兼容既有消息加载器的默认编码。

## AI 密钥初始化回归

上述带 `--ai-classpath` 的回归命令包含 `KeyringRegression`：使用仅绑定回环地址的临时 H2 TCP 数据库，4 个独立 JVM 同时首次初始化，再启动第 5 个 JVM 验证重启/扩容。覆盖提交失败不发布、跨节点解密/验签、普通设置保存和 JSON 隔离、迁移重试、旧手工密钥登记、轮换保留旧密文/证明、版本冲突、节点配置不一致、材料丢失及数据库不可用时拒绝。HTTP/H2 端口均为隔离测试端口，不启动 DataEase，不连接业务库或真实模型。

H2 证明事务和跨 JVM 回归，不代表 MySQL/GreatSQL、PostgreSQL、SQL Server、Oracle、达梦及 Kingbase 已验收；生产数据库方言、实际安装/滚动升级、网络分区和备份恢复仍需对应环境验证。

## 对话助手回归

修改开发验收入口、会话生命周期、工具、协议或资源预算前，读取 [对话助手开发说明](../../docs/ai/conversation.md)。本文保留回归命令、覆盖范围和环境限制。

`ConversationRegression.java` runs actual dialogue MVC routes and real controlled loopback model HTTP, replacing only approved scene/identity/model boundaries. It covers multiround isolation and aggregate budgets, a first SSE increment while upstream is still blocked, fast private/token bursts under a five-second deadline with a deliberately slow scene READ boundary, cursor/terminal behavior, same-session and interactive admission, stop before/after the first delta, healthy parallel-stream isolation, cancellation history exclusion, protocol/output/UTF-8 failures, finite timeout, buffered-delivery permission/configuration revocation, active close/shutdown, and capacity recovery after an abandoned client. The same runner verifies one-shot capability submission without chat history, refusal of one-shot continuation, repeated-button capacity release, typed suggestions and fresh application authorization. It includes ToolConversationRegression through the actual SQL scene and the approved external DatasourceApi seam: actual table/field tool roundtrips, invalid candidate → model correction → PASS revalidation, split arguments, correlation/follow-up transcript, safe projection, invalid/out-of-scope calls, whole-round token reservations, definitions/results input budgets, repeat bounds, stop/late results and mid-tool READ revocation. `SqlStaticValidationRegression` independently covers the public pure validation boundary with the declared syntax/reference/security corpus and cancellation/deadline refusal. It also keeps the native synchronous SDK, original controlled transport/pool, community permission parity and keyring regressions. It starts neither DataEase nor a business database and calls no real model.

```sh
python3 tests/ai-service/run.py --ai-classpath /path/to/current-runtime-classpath.txt
npm --prefix tests/browser test -- --config=playwright.ai-conversation.config.mjs
```

The actual Vue page/browser fixture covers increments, cursor replay, escaped actual tool parameters/results and statuses, visible program PASS/INVALID/UNSUPPORTED verdicts, hide/expand, stop/follow-up, aborting an in-flight poll, completion/stop races, context/identity changes, offline/pagehide/refresh and a localized timeout with incomplete output. Its request/store boundaries are synthetic; it does not establish real authentication/HMAC, DE resource permission or configured-model compatibility. Main-path acceptance must use the root-coordinated isolated development deployment, current configured compatible model and authorized datasource; record that evidence separately. Other databases/profiles, Redis/cluster streaming and licensed real-user/workspace matrices remain environment validation work.

When reusing an older `CoreApplication.jar`, `--ai-classpath` may include current compiled SDK/dependency directories as well as jars. The runner prioritizes the compiled test/source overlay and those current directories before the packaged baseline. The installed license SDK must match the current parent version. No product Maven lifecycle or dependency download is performed by the runner.

## 独立后台任务回归

```sh
uv run python tests/ai-service/run.py --task-contracts
uv run python tests/ai-service/run.py --only-tasks
```

沿同一 runner 的既有依赖前提，`--task-contracts` 是公开 MVC/JSON 的快速红绿入口：幂等受理、改输入拒绝、重试键绑定原尝试、字符串 ID、有界准入、真实后台模型池/工具、撤权读取与安全取消、连续终态提交失败恢复。测试身份和业务场景受控，传输调用真实本机合成模型 HTTP，元数据库为隔离 H2。离线 Hibernate 检查 PostgreSQL/MySQL/H2 的字符字段 DDL/JDBC 映射，不等于这些数据库全部实测。

`--only-tasks` 还运行 TaskClusterRegression：拥有随机 loopback 端口的 H2 TCP 与独立 JVM 后端节点，通过真实 HTTP 访问实际 MVC/API/runtime/JPA。覆盖同时受理/只领取一次、跨节点取消、超过前台租期无轮询完成、QUEUED 跨进程恢复、进程 STOP/CONT 与过期归属 fencing、禁止自动续跑、全部节点重启后的持久结果，以及本人/撤权拒绝与仅进行中列表。进程、数据库和临时目录均由夹具创建并清理，不启动完整 DataEase，不使用产品登录凭据或业务数据库；不冒称 XPack 多空间权限、真实模型或 Redis 模型全局限额验收。完整默认回归包含本组测试并保留原前台/SDK 回归。

### 后台解析进度时序

```sh
uv run python tests/ai-service/run.py --task-progress
# 最小合法响应：真实loopback任务HTTP、一个1KiB private帧、短回答、5秒期限、隔离JDBC每执行2ms延迟
uv run python tests/ai-service/run.py --task-progress --progress-case text-burst --progress-frames 1 --progress-private-chars 1024 --progress-delay-ms 2
```

入口使用实际任务 MVC/API、JPA 迁移/存储、runtime/transport 和真实 SQL scene；模型与 DatasourceApi 身份/元数据边界受控。`--task-progress` 比较 text/sql 两种路径的普通短文本与 512 个合法 private 帧（共 4096 字符），SQL 路径实际调用 list_tables 并续轮。断言上游已结束的合法短响应在原 5 秒允许期限内持久 COMPLETED，private 内容不输出，也不靠超限制造失败。fixture 只为自己的 H2 连接增加有限 JDBC 时延；可选 `--progress-delay-scope state|other` 隔离 attempt/node SELECT 与其他 SQL 的时序，不连接产品库。此断言检验用户可见完成/超时及结果，不以私有方法次数代替行为。默认完整和 `--only-tasks` 回归包含这组时序；快速 `--task-contracts` 可排除它。

后台私有解析/校验热循环只检查本地 run、停止和 deadline；调度器仍周期检查持久状态，并在模型发送、工具边界、最终结果与用户交付前复核完整授权/归属。持久取消/节点失效以数据库 fence 裁决，不能因为减少逐字符数据库往返而放宽迟到提交。

## 任务迁移升级回归

`uv run python tests/ai-service/run.py --task-upgrade --ai-classpath <已有依赖 classpath 文件>` 通过真实 `InitSqlListener`、JPA 版本记录仓库与隔离 H2 验证启动升级，关闭 `ddl-auto`。覆盖已完成上游数据源语义迁移 `2.48` 且尚无 AI 任务表，以及从 `2.47` 依序升级的情况：`2.49` 建任务表、`2.50` 补交付字段，保留语义数据；另覆盖 `2.49` 存量任务表缺交付列的启动补列及旧结果回填。重复启动不重复执行或破坏结果。该回归验证实际 DDL 与启动排序，不仅检查版本号；不代表已验证其他数据库上的真实升级。

## Declared resource mutex regressions

```sh
uv run python tests/ai-service/run.py --task-mutex --ai-classpath /path/to/existing-classpath.txt
uv run python tests/ai-service/run.py --task-structure --ai-classpath /path/to/existing-classpath.txt
uv run python tests/ai-service/run.py --task-structure-nodes --ai-classpath /path/to/existing-classpath.txt
uv run python tests/ai-service/run.py --task-upgrade --ai-classpath /path/to/existing-classpath.txt
AI_BROWSER_TEST_PORT=4297 npm --prefix tests/browser test -- --config=playwright.ai-conversation.config.mjs
```

`--task-mutex` uses independent real HTTP/MVC/runtime/JPA JVMs sharing an owned H2 TCP database and controlled scene, identity and loopback-model boundaries. It covers overlapping batches and races, queue rejection without residue, task/retry replay, own-task location and cross-owner privacy, different datasources/operations, undeclared SQL, accepted queue expiry, complete/cancel/execute-timeout release, held automatic-backoff claims and cancellation, node lease fencing and late old results against a replacement owner. It observes public responses rather than treating local executor state as the lock.

`--task-structure` uses the actual controlled structure scene and SQL scene with an approved external DatasourceApi/permission fixture. It verifies authoritative canonical names, metadata reads outside metadata transactions, a changed field snapshot refused before model send, and an explanation-only result. No candidate SQL, data row read or source business write is allowed by the datasource fixture. `--task-upgrade` exercises the real startup/version boundary through ordered resource migration `2.53` and repeat startup. The Vue suite loads the actual Demo component/API and covers explicit batch selection, shared current-message submission, safe busy refusal and same-owner location.

`--task-structure-nodes` persists actual SQL/structure scene contexts and reads them in 12 independent JVMs, requiring different immutable-map iteration orders. Unchanged metadata authorizes both current contexts and legacy raw-JSON fingerprints; changed field names, types, comments, column order/count, table names, datasource revision and revoked READ remain rejected. Legacy compatibility requires the exact instruction prefix, matching original raw projection hash and strict complete JSON object parsing. The fixture uses only the DatasourceApi/identity seams and temporary context files, with no model or database access. It also runs in `--task-structure`, default and `--only-tasks` suites.

These fixtures use synthetic identities and models, owned H2 and loopback ports. They do not establish real XPack licensing/workspace authorization, live database dialect compatibility, current configured-model behavior or product Redis cluster admission; record those independently. The controlled scene is resource-mutex acceptance and read-only structure explanation, not semantic generation or business delivery.

## 留存配置与正式清理回归

`uv run python tests/ai-service/run.py --retention --ai-classpath <既有依赖classpath文件>` 在独立 H2 样本上，通过公开设置/任务/审计 HTTP 与正式 `AiRetentionCleanup.clean()` 验证：默认值与管理员配置、旧请求兼容及 CAS、结果到期前后读取、通知确认、过期旧提交/重试/会话投递拒绝、混合尝试及运行/排队资源保护、交付分项提交失败、审计条目与父记录原子清理失败重跑、并发清理、晚到审计写入及定时缺口修复防复活。审计及清理结果只在隔离库读取观察。

同一入口执行 2.55 的 MySQL/PostgreSQL/H2 方言注册表迁移及部分迁移重试；承载数据库均为隔离 H2，不代表真实 MySQL/PostgreSQL 迁移。`--task-upgrade` 另通过实际启动迁移监听器验证从 2.47/2.48/2.49/2.50/2.52/2.54 顺序升级到 2.55，并保留已有期限、配置和任务事实。已安装的真实 Vue/Playwright 配置回归中含独立留存字段及保存校验；这些隔离入口均不代替真实登录、配置模型或许可环境验收。

## Dynamic AI policy closure

`--runtime` additionally verifies that a native model context cannot revive after a complete public HTTP off/on cycle and a fresh context may run.

`uv run python tests/ai-service/run.py --task-policy --ai-classpath <existing-classpath-file>` exercises the public configuration/task/usage APIs across independent JVMs on owned shared H2. It covers queue/run cancellation, exact resource release, late output, full-capacity revocation, technical backoff and tool shutdown. Pausing one JVM for the complete off/on interval verifies that an old foreground session cannot revive.

`uv run python tests/ai-service/run.py --disabled-errors --ai-classpath <existing-classpath-file>` focuses on shutdown refusal responses through actual task MVC controllers and `GlobalExceptionHandler`, using owned loopback HTTP/H2 without model sends. Global and organization shutdown each reject new tasks, retries and existing task reads with HTTP403/code70001; contract 2 includes `FORBIDDEN` and the distinct `i18n_ai_disabled` / `i18n_ai_organization_disabled` key, while legacy responses omit new metadata. It also checks administrator settings access, unaffected organizations, preserved cancellation reasons and old-session rejection after re-enable. The existing task read uses a cancelled queued attempt; completed-result delivery and independent administrator audit have separate regressions.

`--conflict-errors` uses the same actual MVC advice and isolated database to verify changed-input submissions, mismatched retry attempts, queue/user/resource admission conflicts and an active origin conversation delaying background-result import. Each uses existing business code50005/CONFLICT (HTTP409 under both contracts) and its original safe message key; literal business code409 is not an HTTP status. A controlled loopback model holds the original turn busy; failed imports preserve delivery, and later repeated imports append the completed result once without rerunning the task. The focused Vue suite (`--grep 'recovery reasons|after busy import'`) additionally checks busy-import409 does not consume the read-recovery budget, and foreground/background recovery reasons use safe localized text with unknown-code fallback in zh-CN/en/tw at360px.

Migration 2.56 adds monotonic `usage_epoch` fences to global and organization AI policy. Only shutdown increments the affected epoch; capacity, recovery and retention changes preserve valid work. Model contexts and temporary sessions bind the current epoch. `/ai/availability` exposes only the current authenticated usage state and revision; the Demo hides the assistant when unavailable and discards temporary chat/pending state when its revision changes, preserving the editor draft. Re-enabling does not resume cancelled tasks or restore history; valid unacknowledged notices retain their existing delivery boundary. Administrator configuration and audit authorization remain independent of AI usage availability.

The unmapped AI authorization SPI requires explicit `Boolean.TRUE` (`checkAiAllowed` / `checkAiCommunityAllowed`). Defaults, missing implementations and intercepted null results deny AI usage and configuration, not only the new audit endpoint. The adapter requires the new SDK common scope class; previous audit-only old-SDK compilation does not establish compatibility for it. Deploy the matching public SDK, extension adapter and core together; an old extension cannot silently authorize the new capability. `AiOwnerContext` is a read-only SDK projection with a package-private installer used only by the verified server-owned identity scope. It cannot be selected by an HTTP argument/header and never grants permissions. Current account, real membership and resource authorization still apply before and after work; changing the current browser space does not rewrite a background owner.
