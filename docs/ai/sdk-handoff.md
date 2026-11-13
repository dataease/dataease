# DataEase V3 基础 AI SDK 接入交付说明

本文面向通过 `AiModelProvider.current(context).call(prompt)` 接入同步模型调用的业务开发。契约基线核对日期为 2026 年 11 月 9 日；版本与签名以当前源码为准，不表示已发布独立 SDK 制品或已部署。多轮会话、工具执行与增量输出使用另一条入口，修改这些能力前读取 [对话助手开发说明](conversation.md)。

## 1 交付范围与技术栈

基础服务统一处理模型配置解析、凭据解密、身份复查、调用限制、HTTP 传输和安全错误。业务负责准备经过授权的输入，以及解释、校验、确认和保存模型结果。

| 项目           | 当前交付                                                                       |
| -------------- | ------------------------------------------------------------------------------ |
| 运行环境       | Java 21、Spring Boot 3.5.15、Spring AI 1.1.8                                   |
| 公共 Java 契约 | `sdk/api/api-base` 中的 `io.dataease.api.ai.runtime` 包                        |
| 运行实现       | `core/core-backend` 中的 `io.dataease.ai`，通过 Spring Bean 注入               |
| 模型调用       | Spring AI `ChatModel.call(Prompt)`，也可通过绑定该模型的 `ChatClient` 调用     |
| 提供商         | 当前接入 OpenAI 兼容协议；Anthropic、Gemini、Ollama 原生协议适配不属于当前交付 |
| 传输           | Spring RestClient、Apache HttpClient 5 共享连接池、有界工作线程池              |
| 并发协调       | 本地缓存部署使用 JVM；Redis 缓存或 distributed 部署使用 Redis 原子准入         |
| 元数据         | JPA 配置表；业务调用不直接读写模型配置实体                                     |
| 产品版本       | 社区版可使用；有效许可模式同时遵循既有组织与权限规则                           |

这是运行于同一 DataEase 后端进程的 Java SDK，不是独立远程 AI 网关。业务依赖项目同版本 `io.dataease:api-base` 契约；不要依赖 `AiRuntimeManage`、`AiTransport` 等实现类，不复制底座代码，不自行新增提供商 starter 或替换项目管理的依赖版本。

本文的同步 SDK 路径提供受控调用，拒绝流式调用、工具执行和媒体输入；独立的对话助手路径有自己的授权与执行边界。`AiFeature.auditReference(context)` 默认采用 `scope.reference`；使用临时会话或任务查找键作为 scope 的业务应返回可信资源及版本引用。该投影不替代当前业务授权。实际模型请求与响应现已采集为管理员审计，保留供应商 usage（未返回时为未知）及失败/取消的部分输出。`GET /ai/audit/{executionId}` 提供独立受保护的有界详情；完整管理员列表、统计页面、应用事件及业务保存事件 SDK 仍未提供。普通任务接口不返回审计正文。

## 2 公共接口

| 接口                  | 方法                                                               | 用途                                                |
| --------------------- | ------------------------------------------------------------------ | --------------------------------------------------- |
| `AiContextFactory`    | `current(String registeredFeature, AiDataScope scope)`             | 在当前真实登录请求中创建交互调用上下文              |
| `AiContextFactory`    | `task(String registeredFeature, String taskId, AiDataScope scope)` | 从受控任务恢复执行身份，创建后台调用上下文          |
| `AiModelProvider`     | `current(AiExecutionContext context)`                              | 获取绑定当前上下文的受控 `ChatModel`                |
| `AiPromptFingerprint` | `of(Prompt prompt)`                                                | 计算最终输入摘要，供输入绑定和样本确认使用          |
| `AiFeature`           | 业务实现并注册为 Spring Bean                                       | 提供功能标识、资源授权、输入检查及可选后台/样本支持 |

调用顺序：业务准备授权输入 → 创建上下文 → 获取模型 → 同步调用 → 业务校验结果 → 业务确认与短事务保存。

### 2.1 AiDataScope

```java
public record AiDataScope(
    String reference,
    String permissionVersion,
    String inputFingerprint,
    boolean samples,
    String confirmation
) {}
```

| 字段                | 业务应如何提供                                                                         |
| ------------------- | -------------------------------------------------------------------------------------- |
| `reference`         | 非空，指向真实业务资源及其版本；例如数据集 ID 与元数据版本的组合，格式由业务定义并校验 |
| `permissionVersion` | 非空，当前权限依据的版本或摘要，由业务后端生成并在授权回调中复查                       |
| `inputFingerprint`  | 可空；非空时必须为 64 位小写十六进制 SHA-256，SDK 会与最终 Prompt 摘要比对             |
| `samples`           | 是否包含样本数据，由可信后端根据实际构造的输入确定                                     |
| `confirmation`      | 样本确认凭证；`samples=true` 时必须提供，业务负责真实性、身份、输入、版本与期限校验    |

这些字段不是前端声明即生效的授权凭证。SDK 不能根据任意文本自动判断它属于哪个数据集、是否包含样本、是否符合行列权限；业务必须在 `authorizeInput` 中校验实际输入。资源或权限没有现成版本号时，业务需建立可复查的摘要或版本依据，不能用永远不变的占位值代替。

摘要覆盖消息角色、顺序、完整文本及结构化 schema、stop 文本，不是整个 HTTP 请求的签名。构造指纹后不要继续改变绑定的输入；使用 Advisor 时，确认与指纹应覆盖其增补后的最终输入，否则 SDK 会拒绝摘要不一致的请求。

### 2.2 AiExecutionContext

上下文提供 `executionId`、`feature`、`actorId`、`organizationId`、`proxyOrganizationId`、`background`、`deadline` 和 `scope`。由工厂创建，业务不能通过自行实现接口绕过校验。

交互上下文绑定当前请求及登录/代理身份，不能带到另一个请求或自行启动的异步线程中使用。模型和由它创建的 ChatClient 同样不能缓存为跨请求单例。

同一操作的多次调用可以复用上下文，但共享不可续期的截止时间；每次 HTTP 都重新申请并发许可。`executionId` 仅为执行上下文标识，不意味着已持久化调用记录，也不提供幂等提交或自动去重。

## 3 业务注册与交互接入示例

以下示例中的 `DatasetAiAccess` 是业务需要自行实现的授权服务，不是 SDK 已提供的类型。其职责包括验证资源可见性、行列权限、资源/权限版本，以及最终输入是否与授权快照一致。

```java
import io.dataease.api.ai.runtime.AiExecutionContext;
import io.dataease.api.ai.runtime.AiFeature;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class DatasetExplainFeature implements AiFeature {
    private final DatasetAiAccess access;

    public DatasetExplainFeature(DatasetAiAccess access) {
        this.access = access;
    }

    @Override
    public String code() {
        return "dataset-explain";
    }

    @Override
    public void authorize(AiExecutionContext context) {
        access.verifyCurrentResourceAndPermissions(context);
    }

    @Override
    public void authorizeInput(AiExecutionContext context, Prompt finalPrompt) {
        access.verifyFinalInput(context, finalPrompt);
    }
}
```

功能码必须稳定且全局唯一。未注册的功能无法调用；重复功能码会导致注册失败。两个授权方法必须失败时抛出异常，不能只记录日志后放行。回调会重复执行，且可能在线程池中运行，因此需可重复、避免副作用，并直接使用传入的上下文显式校验身份，不能依赖当前线程的登录 ThreadLocal。

交互业务 Service 注入 `AiContextFactory` 和 `AiModelProvider`：

```java
// 以下代码运行于已认证的当前请求中，方法本身不加 @Transactional。
Prompt prompt = new Prompt(authorizedPromptText);
AiDataScope scope = new AiDataScope(
    resourceReference,
    currentPermissionVersion,
    AiPromptFingerprint.of(prompt),
    false,
    null
);
AiExecutionContext context = contextFactory.current("dataset-explain", scope);
ChatResponse response = modelProvider.current(context).call(prompt);
String generatedText = response.getResult().getOutput().getText();
// 业务继续校验结果，再返回给用户预览或进入独立的保存流程。
```

`authorizedPromptText` 必须由业务后端按授权结果构造；不要将前端上传的资源 ID、样本开关或未经校验的上下文直接拼入模型输入。

如使用 Spring AI ChatClient，可将获取到的受控模型传给 `ChatClient.builder(model)`。底座在模型边界检查 ChatClient 最终生成的 Prompt；业务不要安装会另行发起不受控模型调用的 Advisor。业务需要多轮历史时，应自行筛选并授权每一条历史消息，所有发送的历史均计入输入字符数。

## 4 后台任务与样本数据

### 4.1 后台任务

业务的 `AiFeature` 需要实现 `taskIdentity(String taskId)`，从可信任务存储加载任务与执行身份，返回：

```java
new AiFeature.TaskIdentity(
    actorId, organizationId, proxyOrganizationId, identityVersion
);
```

`identityVersion` 是业务维护的任务执行身份版本。SDK 会在调用中重新恢复并比较身份，任务更换执行人、组织或代理身份后旧上下文应失效。业务还需在授权回调中检查任务是否仍允许执行及其资源权限；任务取消等业务状态不会由 SDK 自动识别。

在业务已有任务执行器内调用：

```java
AiExecutionContext context = contextFactory.task(
    "dataset-explain", trustedTaskId, authorizedScope
);
ChatResponse response = modelProvider.current(context).call(prompt);
```

不要从前端接收 `background=true` 切换并发池，也不要把交互上下文跨线程传递。后台任务使用独立并发额度，当前禁止发送样本。社区模式使用真实账号身份，不接受组织或代理参数。

### 4.2 样本确认

交互调用发送样本需同时满足：全局允许样本、业务当前资源/数据权限通过、scope 带最终输入指纹与确认凭证，并由 `checkSampleConfirmation(context)` 验证确认仍有效。

确认凭证的签发与存储由业务实现，至少绑定身份、资源及权限版本、输入摘要和期限。默认 `checkSampleConfirmation` 会拒绝样本；仅开启全局开关不能代替业务确认。组织、行列权限或输入发生变化时应重新确认。

## 5 五项调用限制

| 配置           | 当前默认值    | SDK 执行机制                                                                                     |
| -------------- | ------------- | ------------------------------------------------------------------------------------------------ |
| 请求超时       | 60 秒         | 上下文创建时设置截止时间；排队和网络请求共用预算，调用时还受当前配置约束；到期取消当前 HTTP 请求 |
| 最大输入字符   | 跟随模型（0） | 配置正上限时，对最终所有消息文本、schema、stop 文本按 Unicode code point 计数；超限拒绝，不截断  |
| 最大输出 Token | 跟随模型（0） | 默认省略输出 Token 参数；业务可指定正值，管理员配置正上限时不得超过；自定义上限按每次请求使用    |
| 交互并发       | 5             | 每次实际 HTTP 申请交互许可，达到上限立即拒绝                                                     |
| 后台并发       | 2             | 与交互分开的后台许可池，达到上限立即拒绝                                                         |

这些是部署范围的两类并发额度，不是每个用户或组织各有一份。模型发现、模型测试计入交互并发；一次模型测试的基础生成及结构化验证共用截止时间。

JVM 模式使用同步保护的许可集合；Redis/集群模式使用 Lua 原子准入，节点共享计数。许可在响应关闭或异常时释放，并有期限兜底；Redis 协调不可用时拒绝新调用，不回退到节点各自计数。配置更新期间使用版本与准入屏障协调，未生效状态会阻断调用。

线程池为 40 个工作线程、40 个排队位置；HTTP 池总连接数及单目标上限均为 40。两者保护进程资源，不替代上述业务并发限制。所有调用不能自行选择 URL、API Key、并发类别或放宽上限。

已有正数配置继续作为自定义限制；配置页支持 K/M（K=1024，M=1048576）。输入字符数不等于模型上下文 Token；跟随模型不自动发现容量。模型参数能力测试仍使用至多配置值或默认 4096 Token 的合成探针，不影响运行请求跟随模型。

输入另有 4 MiB 序列化字节上限，响应有 4 MiB 读取上限。输出 Token 预算由供应商执行，SDK 不承诺供应商实际计算量和计费量；超时或取消也不能证明上游已停止计算。Redis 异步复制故障切换同样不能承诺任何故障下的绝对物理并发上限。

## 6 选模与参数规则

SDK 根据已保存配置自动选择模型：社区模式使用全局默认；有效许可模式按组织策略选择继承默认、指定模型或禁用。调用还会检查总开关、模型可用性、身份与部署模式状态。配置菜单权限用于管理配置，普通业务调用需要业务功能授权，不要求用户拥有配置菜单权限。

每次发送前复查身份、资源/数据权限、配置及模型凭据版本。发生变化会拒绝旧调用，不在执行途中自动换模型或切换身份。已发往上游的请求不会因后续配置变化而获得供应商侧撤销保证。

允许文本 User、System、Assistant 消息。参数只接受当前实现支持的 `DefaultChatOptions` 或 `OpenAiChatOptions`，包括温度、topP、惩罚项、stop、输出预算和支持的结构化格式。模型标识固定为解析结果，n 固定为 1；禁止工具/工具执行、媒体、认证头、extraBody 及未知扩展。

通用输出预算优先使用 `ChatOptions.maxTokens`，底座按成功测试记录映射到 `max_tokens` 或 `max_completion_tokens`。不要同时设置两个字段。显式提供商参数需要与模型能力一致。

模型配置测试采用 JSON Object 并校验固定测试结果；这不代表所有模型均支持任意 JSON Schema。业务使用结构化输出时仍需确认供应商能力，并在收到结果后进行自己的 schema、语义和业务安全校验。SDK 只拒绝空响应、工具调用、截断等已实现异常情况，不会自动执行模型生成的 SQL 或自动验证所有业务 JSON。

## 7 线程与事务边界

SDK 的 Java 方法对业务是同步调用：业务线程等待结果，底座内部将 HTTP 工作交给受控线程池。调用前的业务逻辑并不会自动进入该线程池。

当前实现明确拒绝在调用线程已有活动事务时调用模型，返回事务相关错误，避免网络等待占用元数据库事务。外层 `@Transactional` 方法、自调用导致事务未按预期拆分等情况都需要业务检查。

推荐顺序是：短事务读取必要快照并结束 → 事务外调用 AI → 校验与用户确认 → 独立短事务重新鉴权、检查资源版本并保存结果。最后一次保存应通过独立 Spring Bean 的事务方法等正常代理入口执行，不能依赖同类自调用启动事务。

不要跨线程传递事务 ThreadLocal、JDBC 连接、EntityManager 或 JPA 受管实体；传递不可变 ID、版本和经授权的必要数据。底座工作线程会清理请求、身份、语言和日志上下文，业务授权回调不能假定原请求上下文自动传播。

基础 AI 调用与业务保存不在同一事务内，供应商请求也无法随业务数据库回滚。当前没有 `executeAction`、`recordSaved` 或调用记录与业务保存原子提交 API；如后续需要这些能力，应单独确定契约，不能依赖尚未实现的设计名称。

## 8 错误处理与重试

SDK 通过项目既有 `DEException` 体系抛出安全错误；业务 HTTP 边界可沿用全局异常处理，不将失败包装成成功或空文本，不向前端返回供应商原始正文、认证头、URL 或异常堆栈。

| 错误类别                                  | 业务处理                                               |
| ----------------------------------------- | ------------------------------------------------------ |
| 未授权、数据范围/配置版本变化             | 重新检查身份与资源；必要时重新加载或重新确认           |
| 输入/输出预算超限、不支持参数             | 修正输入或调用参数；不要原样重试                       |
| 并发上限、超时、取消                      | 向用户说明本次失败；如提供重试按钮，由明确的新操作触发 |
| 服务关闭、组织禁用、无可用默认模型        | 引导有权限人员检查配置                                 |
| DNS、连接、TLS、供应商认证/限流/余额/拒绝 | 展示底座安全提示；由配置维护人员排查                   |

底座禁用 Spring AI 和 HTTP 通用隐藏重试。管理页面模型测试中仅对明确不支持 `max_tokens` 的错误有受控参数协商；正常业务调用不做该协商。业务不要叠加无界 Spring Retry 或自行循环刷新上下文规避截止时间。

当前稳定公共接口没有单独暴露可供业务依赖的 AI 错误枚举；实现类中的 `AiErrors.Failure.safeCode()` 不属于 `api-base` 公共契约。后续如需要机器可读错误分支，应先补充公共错误契约，不让业务直接引用核心实现类或依赖翻译文案匹配。

## 9 部署交接与验收

全新部署未显式配置 AI key ring/active key 时，服务端在迁移后锁定现有 `core_ai_setting` 行，一次生成独立密钥并供同部署节点共享；已有手工配置必须保留原 keyId、全部旧密钥和 active key，缺失或不一致时拒绝 AI 操作，不重新生成覆盖。密钥不通过 SDK 或配置接口返回。数据库模式的密钥与密文同库；需要分离存储时保留外置 Secret 模式。首次安装/升级先由单节点完成迁移，再启动其他节点。轮换及配套备份恢复见 [AI 服务验证说明](../../tests/ai-service/README.md)，本次没有新增轮换 HTTP 入口。Redis 模式还需部署内一致、部署间隔离的 namespace。真实密钥由部署管理，不写入业务代码或文档。模型地址、API Key、测试、默认模型、总开关和调用限制由配置页面维护，业务无需取得或缓存凭据。

Redis 许可结构全新初始化或丢失后，会进入恢复保护期；当前最长请求期限 300 秒加 10 秒宽限，期间可能拒绝调用。不要通过清 Redis、改 namespace 或降级 JVM 来规避保护期。

后续团队联调至少覆盖以下用例：

- 已授权资源成功调用；未授权资源、已撤销权限、过期资源版本被拒绝。
- 最终 Prompt（含历史和模板增补）超限被拒绝，业务指定输出预算超限被拒绝。
- 交互与后台并发分别达到上限时正确拒绝，正常/异常/超时后可恢复调用。
- 样本未确认、确认过期、输入变化、全局禁止样本以及后台样本被拒绝。
- 服务关闭、默认模型不可用、组织禁用和身份/配置变更按规则拒绝。
- 事务内调用被拒绝；事务外生成后，保存阶段重新检查权限与版本。
- 供应商失败使用安全提示，不输出 Prompt、样本、Key 或原始错误正文。
- 不缓存跨身份模型，不另建绕过 SDK 的客户端，不将 AI 输出直接执行或直接保存为可信业务指令。

现有隔离回归已覆盖受控 Spring AI 调用、输入预算、上下文隔离、授权撤销、社区模式、JVM/Redis 准入，以及共享连接复用和并行超时/取消隔离。当前交付不包含真实付费供应商全矩阵、所有元数据库、完整产品权限矩阵或 Redis 故障切换验收结果；后续业务场景需按实际部署完成联调。

## 10 源码与验证入口

- 公共契约：`sdk/api/api-base/src/main/java/io/dataease/api/ai/runtime/`。
- 调用编排：`core/core-backend/src/main/java/io/dataease/ai/manage/AiRuntimeManage.java`。
- 输入与参数边界：`core/core-backend/src/main/java/io/dataease/ai/support/AiPromptPolicy.java`。
- 并发准入：`core/core-backend/src/main/java/io/dataease/ai/support/AiAdmission.java`。
- 传输与取消：`core/core-backend/src/main/java/io/dataease/ai/support/AiTransport.java`。
- 隔离验证及部署参数说明：[AI 服务验证说明](../../tests/ai-service/README.md)。

SDK 路径内的限制由基础服务强制执行，调用参数不能关闭或放宽。它不是进程级安全沙箱：业务自行创建 HTTP/模型客户端仍可绕过这条调用路径，因此统一接入需要落实到依赖边界和代码审查。

## SQL 草稿业务示例

设计同步 SQL 草稿生成、结果校验和用户采用流程时，读取 [SQL 草稿业务接入示例](sql-draft-example.md)。该示例与现有多轮对话助手的入口和能力分别维护。

## 动态关闭的上下文边界

迁移 `2.56` 增加全局与适用空间的关闭专用版本。SDK 工厂创建的上下文绑定当时的版本；全局或该空间关闭后，即使另一节点未观察到关闭且随后已重新开启，旧上下文也不得继续模型调用。业务须明确创建新尝试及新上下文，不复用被撤销的模型或 ChatClient。容量、恢复和留存调整不增加关闭版本；同次尝试仍固定实际模型及配置/凭据版本。

新版核心只接受内部 Boolean 授权的显式允许值。SDK common / permissions、授权扩展和核心须匹配交付：新核心配旧扩展时，AI 生成及配置可能均按默认拒绝处理；新增扩展依赖新版 SDK common 的服务端身份投影，不适用此前仅审计 checker 的旧 SDK 编译兼容结论。该投影不允许 HTTP 参数或请求头选择，也不替代当前账号、成员及业务资源权限验证。
