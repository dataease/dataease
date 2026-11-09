# DataEase V3 基础 AI SDK 接入交付说明

本文面向后续 AI 业务需求开发团队，说明当前源码提供的调用契约、基础服务负责的限制，以及业务需要实现的授权与结果处理逻辑。契约核对日期为 2026 年 11 月 9 日；以当前工作区实现为依据，不表示已经发布独立 SDK 制品或部署到验收环境。

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

当前提供模型管理、使用设置及受控调用。调用记录目前只有菜单和说明占位页，尚未交付记录采集、尝试/步骤/请求响应正文存储、查询、应用事件或业务保存事件 SDK。流式调用、工具执行、媒体输入、Agent 和 RAG 也不在当前交付范围。

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

| 配置           | 当前默认值 | SDK 执行机制                                                                                     |
| -------------- | ---------- | ------------------------------------------------------------------------------------------------ |
| 请求超时       | 60 秒      | 上下文创建时设置截止时间；排队和网络请求共用预算，调用时还受当前配置约束；到期取消当前 HTTP 请求 |
| 最大输入字符   | 30000      | 对最终所有消息文本、schema、stop 文本按 Unicode code point 计数；超限拒绝，不截断                |
| 最大输出 Token | 4096       | 未指定则使用配置预算；业务可指定更小正值，超过上限拒绝；按模型能力设置输出 Token 参数            |
| 交互并发       | 5          | 每次实际 HTTP 申请交互许可，达到上限立即拒绝                                                     |
| 后台并发       | 2          | 与交互分开的后台许可池，达到上限立即拒绝                                                         |

这些是部署范围的两类并发额度，不是每个用户或组织各有一份。模型发现、模型测试计入交互并发；一次模型测试的基础生成及结构化验证共用截止时间。

JVM 模式使用同步保护的许可集合；Redis/集群模式使用 Lua 原子准入，节点共享计数。许可在响应关闭或异常时释放，并有期限兜底；Redis 协调不可用时拒绝新调用，不回退到节点各自计数。配置更新期间使用版本与准入屏障协调，未生效状态会阻断调用。

线程池为 40 个工作线程、40 个排队位置；HTTP 池总连接数及单目标上限均为 40。两者保护进程资源，不替代上述业务并发限制。所有调用不能自行选择 URL、API Key、并发类别或放宽上限。

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

运行前需完成 AI 服务端 key ring、active key 配置；Redis 模式还需部署内一致、部署间隔离的 namespace。真实密钥由部署管理，不写入业务代码或文档。模型地址、API Key、测试、默认模型、总开关和调用限制由配置页面维护，业务无需取得或缓存凭据。

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
- 隔离验证及部署参数说明：`tests/ai-service/README.md`。

SDK 路径内的限制由基础服务强制执行，调用参数不能关闭或放宽。它不是进程级安全沙箱：业务自行创建 HTTP/模型客户端仍可绕过这条调用路径，因此统一接入需要落实到依赖边界和代码审查。

## 11 后续需求场景 Demo SQL 助手

### 11.1 场景来源与示例边界

场景来自[【AI】DataEase V3 原生 AI 能力设计与使用说明](https://www.tapd.cn/tapd_fe/40899682/story/detail/1140899682001022637)中的“14 SQL 助手”：进入数据集编辑页的 SQL 片段，输入“按区域统计已支付订单的销售额”，生成只读 SQL 草稿；核对后采用到编辑器，再沿原流程验证和保存。

下面是基于真实需求的接入示例，不是已完成的 SQL 助手功能。表名、字段、账号及返回 SQL 均为合成示例；业务 API 路径、DTO 和辅助服务用于解释接入职责，不是新增的 SDK 公共接口，也不要求后续团队照搬为最终业务设计。本次仅补充文档，不建设业务接口、表或页面。

本例采用交互调用，仅发送已授权的表/字段元数据与用户问题，不读取或发送业务记录样本。用户问题同样属于出站内容，应限制长度并检查敏感内容；不能因没有主动取样，就将用户粘贴的真实数据视为已获样本授权。

### 11.2 用户操作和前后端边界

假设用户甲有销售数据集的编辑权限，可使用当前组织对应的默认模型。用户输入：

> 按区域统计已支付订单的销售额。

| 阶段           | 业务处理                                                          | SDK 处理                                                        |
| -------------- | ----------------------------------------------------------------- | --------------------------------------------------------------- |
| 点击生成 SQL   | 检查数据集编辑、数据源及字段访问权限，准备必要元数据和最终 Prompt | 创建可信交互上下文，检查 AI 配置、模型、输入/输出预算并发出请求 |
| 接收生成结果   | 解析并校验 SQL，只将允许的草稿返回编辑器预览区                    | 返回模型文本或安全错误，不解释 SQL 业务含义                     |
| 点击采用       | 把已确认草稿回填 SQL 编辑区，保留原有编辑交互                     | 无须再次调用模型，不产生“应用事件”持久化                        |
| 点击验证或预览 | 重新鉴权，通过现有查询入口执行受控只读验证，保留行列权限与脱敏    | 不执行 SQL，也不代替查询权限检查                                |
| 点击保存       | 通过既有数据集保存入口重新鉴权、检查版本并在短事务中保存          | 不参与业务保存事务，不写调用记录或保存事件                      |

例如，业务生成入口可表示为以下请求；实际开发时复用或确定正式接口，不把本示意路径作为既有接口调用：

```http
POST /<业务前缀>/datasets/{datasetId}/ai/sql-draft
Content-Type: application/json

{
  "question": "按区域统计已支付订单的销售额",
  "expectedDatasetVersion": "dataset-v17"
}
```

请求不传用户 ID、组织 ID、后台标志、模型 ID、URL、API Key 或“已经授权”的布尔值。身份来自既有认证，资源和权限依据由业务后端加载。

### 11.3 业务先构造授权输入

合成元数据示例：

```text
数据库方言：MySQL
允许引用的表：sales_orders
允许引用的字段：region、sales_amount、order_status
指标定义：销售额为 sales_amount 的合计
已支付订单条件：order_status = 'PAID'
要求：只返回单条只读 SELECT；仅使用上述授权对象；不要输出 Markdown 或解释。
问题：按区域统计已支付订单的销售额。
```

字段名、指标口径、已支付状态及数据库方言必须来自已核实的业务元数据，不由模型凭空推断。只发送当前任务需要且用户有权使用的内容，不携带数据库连接密码或其他数据集的 schema。模型提示词中的“只读 SELECT”只是生成约束，不能替代业务侧解析和校验。

业务准备一个不可变 `SqlDraftInput` 快照，至少包含数据集引用及版本、权限依据版本、最终 Prompt 和摘要，并绑定可信执行人及有效组织/代理身份。快照的生成、有效期和跨线程只读访问由业务实现；不能依赖 AI 工作线程继承登录 ThreadLocal，也不要把 JPA 受管实体放入快照。

### 11.4 注册真实业务功能

以下 `SqlDraftAuthorization` 为业务协作接口示意，需要后续团队实现，不属于基础 SDK。这里的两个方法不能是空实现：

```java
import io.dataease.api.ai.runtime.AiExecutionContext;
import io.dataease.api.ai.runtime.AiFeature;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class SqlDraftFeature implements AiFeature {
    private final SqlDraftAuthorization authorization;

    public SqlDraftFeature(SqlDraftAuthorization authorization) {
        this.authorization = authorization;
    }

    @Override
    public String code() {
        return "dataset-sql-draft";
    }

    @Override
    public void authorize(AiExecutionContext context) {
        authorization.verifyCurrentAccessAndVersions(context);
    }

    @Override
    public void authorizeInput(AiExecutionContext context, Prompt finalPrompt) {
        authorization.verifyAuthorizedPrompt(context, finalPrompt);
    }
}
```

`verifyCurrentAccessAndVersions` 使用 context 中的执行人、组织、代理及 scope，重新验证数据集/数据源访问、编辑与字段权限和当前版本；不能只判断“用户已登录”。`verifyAuthorizedPrompt` 从业务受信快照核对最终消息、问题、方言和允许字段，确保没有增加未授权内容。资源版本、权限、业务快照或输入不一致均应抛出项目业务异常。

这两个方法在业务线程及 AI 工作线程内都必须可用、可重复调用。SDK 不负责理解 `reference` 的编码方式，业务需要自行解析或定位真实资源；只填写版本字符串不会自动完成授权。

### 11.5 在事务外调用 SDK

以下编排方法不加 `@Transactional`，并确保调用它的上层也没有活动事务。示例辅助类型和方法属于后续业务，SDK 接口使用当前真实签名。

```java
import io.dataease.api.ai.runtime.AiContextFactory;
import io.dataease.api.ai.runtime.AiDataScope;
import io.dataease.api.ai.runtime.AiExecutionContext;
import io.dataease.api.ai.runtime.AiModelProvider;
import io.dataease.api.ai.runtime.AiPromptFingerprint;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

// Service 通过构造器注入 contextFactory、modelProvider 和业务协作服务。
public SqlDraftResult generateSqlDraft(SqlDraftCommand command) {
    // 内部使用必要的短事务读取授权快照；返回前已结束事务。
    SqlDraftInput input = sqlDraftInputs.prepareAuthorizedMetadata(command);
    try {
        Prompt prompt = input.prompt();
        AiDataScope scope = new AiDataScope(
            input.resourceReference(),
            input.permissionVersion(),
            AiPromptFingerprint.of(prompt),
            false,
            null
        );
        AiExecutionContext context = contextFactory.current("dataset-sql-draft", scope);
        ChatResponse response = modelProvider.current(context).call(prompt);
        String generated = response.getResult().getOutput().getText();

        // 真正解析 SQL，验证单条只读语句、授权对象和当前数据库方言。
        // 不能只用 startsWith("SELECT") 或正则判断安全性。
        String validatedSql = sqlDraftValidator.validate(generated, input);

        // 返回前再确认授权与版本，避免把已失效快照的结果当作当前草稿。
        sqlDraftInputs.verifyStillAuthorized(input, context);
        return new SqlDraftResult(validatedSql);
    } finally {
        // 清理本次业务快照的临时访问句柄；具体机制由业务实现。
        sqlDraftInputs.release(input);
    }
}
```

辅助契约的最低职责如下，最终实现应复用 DE 既有业务服务：

| 示例协作对象            | 必须完成的业务职责                                                                          |
| ----------------------- | ------------------------------------------------------------------------------------------- |
| `SqlDraftCommand`       | 数据集 ID、用户问题和预期版本；问题有长度/内容检查，资源不能只信任前端                      |
| `SqlDraftInput`         | 不可变、已授权的元数据与最终 Prompt；不包含凭据、未授权字段或受管实体                       |
| `sqlDraftInputs`        | 生成并维护本次快照，使授权回调可按可信 scope 定位；验证当前身份/资源/权限；最终清理临时句柄 |
| `SqlDraftAuthorization` | 在线程上下文不传播的条件下，按 SDK context 对资源与最终输入做重复校验                       |
| `sqlDraftValidator`     | 按实际方言解析 SQL；限制语句种类、表、字段、函数及危险结构；失败拒绝，不自动执行            |
| `SqlDraftResult`        | 仅返回已通过业务校验的草稿；不把模型原始响应和内部授权快照直接透传给前端                    |

这里不要求新增数据库快照表或 SDK 记录表。业务快照机制需覆盖重复授权回调及调用取消后的清理；超时后底座工作线程可能尚在退出，快照已清理时回调必须安全拒绝，不能回退放行。

模型可能返回如下合成草稿：

```sql
SELECT region, SUM(sales_amount) AS sales_amount
FROM sales_orders
WHERE order_status = 'PAID'
GROUP BY region
```

该 SQL 仅演示预期输出，不是可直接执行的授权凭证。列权限、行过滤及脱敏仍由正式查询入口实施；不要让模型生成的 SELECT 直接连接数据库绕过现有链路。用户在编辑器修改 SQL 后，应对修改后的内容重新验证。

### 11.6 本次调用中限制如何生效

假设系统保持默认设置：超时 60 秒、最大输入 30000 字符、最大输出 4096 Token、交互并发 5、后台并发 2。

1. `current(...)` 创建交互上下文，确定本次不可续期的截止时间；此后发生的排队和调用都消耗剩余预算。
2. `call(...)` 复查资源与模型配置，统计最终 Prompt；超过输入上限直接拒绝，不访问模型。业务示例未指定输出预算，因此使用当前系统上限。
3. 每次实际 HTTP 发送前重新授权并申请交互许可。已有 5 个交互请求占用许可时，本次返回并发错误，后台 2 个许可不会借给它使用。
4. 网络由线程池执行，HTTP 连接从共享连接池获取；达到截止时间只取消本请求，不关闭其他请求的连接。
5. 正常、异常及响应关闭路径释放许可。业务收到结果后完成 SQL 校验；AI 返回成功不等于 SQL 已合法、已采用或已保存。

### 11.7 失败和再次确认

| 触发条件                           | Demo 应出现的结果                                                                                    |
| ---------------------------------- | ---------------------------------------------------------------------------------------------------- |
| 用户无数据集编辑权限               | 业务拒绝，不能靠拥有 AI 配置菜单权放行                                                               |
| 初次检查通过，发送前字段权限被撤销 | 授权复查拒绝，不能继续使用旧元数据                                                                   |
| AI 生成了未授权表/字段或非只读语句 | 业务 SQL 校验失败，不回填、不执行                                                                    |
| 交互并发已满或调用超时             | 使用底座安全错误提示；不在后台无限重试                                                               |
| SQL 草稿采用后，数据集被别人修改   | 保存时沿既有版本冲突规则处理，不覆盖新版本                                                           |
| 用户取消或关闭生成界面             | 不自动采用/保存；如业务提供主动取消能力，需要自行贯通取消信号，不能假定关闭页面会中断服务端 SDK 调用 |

本 Demo 未发送样本，因此没有样本确认凭证；如后续要读取样例行辅助生成，需接入第 4 节的样本确认流程及实际行列权限/脱敏，不能简单将 `samples=false` 改为 `true`。

验收时分别确认“草稿已生成”“已采用到编辑器”“已保存数据集”，三者不混为一个成功状态。基础服务当前不为这些动作写调用记录或业务事件；后续如需要记录，应另行补充正式契约。
