# SQL 草稿业务接入示例（同步 SDK）

设计同步 SQL 草稿生成功能时，在读完 [基础 AI SDK](sdk-handoff.md) 后阅读本文。以下业务类型和流程是接入示例；现有多轮 SQL 对话助手另见 [对话助手开发说明](conversation.md)。

## 场景来源与示例边界

场景来自[【AI】DataEase V3 原生 AI 能力设计与使用说明](https://www.tapd.cn/tapd_fe/40899682/story/detail/1140899682001022637)中的“14 SQL 助手”：进入数据集编辑页的 SQL 片段，输入“按区域统计已支付订单的销售额”，生成只读 SQL 草稿；核对后采用到编辑器，再沿原流程验证和保存。

下面是基于真实需求的接入示例，不是已完成的 SQL 助手功能。表名、字段、账号及返回 SQL 均为合成示例；业务 API 路径、DTO 和辅助服务用于解释接入职责，不是新增的 SDK 公共接口，也不要求后续团队照搬为最终业务设计。本次仅补充文档，不建设业务接口、表或页面。

本例采用交互调用，仅发送已授权的表/字段元数据与用户问题，不读取或发送业务记录样本。用户问题同样属于出站内容，应限制长度并检查敏感内容；不能因没有主动取样，就将用户粘贴的真实数据视为已获样本授权。

## 用户操作和前后端边界

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

## 业务先构造授权输入

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

## 注册真实业务功能

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

## 在事务外调用 SDK

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

## 本次调用中限制如何生效

假设系统保持默认设置：超时 60 秒、最大输入 30000 字符、最大输出 4096 Token、交互并发 5、后台并发 2。

1. `current(...)` 创建交互上下文，确定本次不可续期的截止时间；此后发生的排队和调用都消耗剩余预算。
2. `call(...)` 复查资源与模型配置，统计最终 Prompt；超过输入上限直接拒绝，不访问模型。业务示例未指定输出预算，因此使用当前系统上限。
3. 每次实际 HTTP 发送前重新授权并申请交互许可。已有 5 个交互请求占用许可时，本次返回并发错误，后台 2 个许可不会借给它使用。
4. 网络由线程池执行，HTTP 连接从共享连接池获取；达到截止时间只取消本请求，不关闭其他请求的连接。
5. 正常、异常及响应关闭路径释放许可。业务收到结果后完成 SQL 校验；AI 返回成功不等于 SQL 已合法、已采用或已保存。

## 失败和再次确认

| 触发条件                           | Demo 应出现的结果                                                                                    |
| ---------------------------------- | ---------------------------------------------------------------------------------------------------- |
| 用户无数据集编辑权限               | 业务拒绝，不能靠拥有 AI 配置菜单权放行                                                               |
| 初次检查通过，发送前字段权限被撤销 | 授权复查拒绝，不能继续使用旧元数据                                                                   |
| AI 生成了未授权表/字段或非只读语句 | 业务 SQL 校验失败，不回填、不执行                                                                    |
| 交互并发已满或调用超时             | 使用底座安全错误提示；不在后台无限重试                                                               |
| SQL 草稿采用后，数据集被别人修改   | 保存时沿既有版本冲突规则处理，不覆盖新版本                                                           |
| 用户取消或关闭生成界面             | 不自动采用/保存；如业务提供主动取消能力，需要自行贯通取消信号，不能假定关闭页面会中断服务端 SDK 调用 |

本 Demo 未发送样本，因此没有样本确认凭证；如后续要读取样例行辅助生成，需接入[基础 SDK 的样本确认流程](sdk-handoff.md#42-样本确认)及实际行列权限/脱敏，不能简单将 `samples=false` 改为 `true`。

验收时分别确认“草稿已生成”“已采用到编辑器”“已保存数据集”，三者不混为一个成功状态。基础服务当前不为这些动作写调用记录或业务事件；后续如需要记录，应另行补充正式契约。
