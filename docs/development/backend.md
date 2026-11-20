# 后端、持久化与数据查询规范

适用于 SDK、核心后端及与 XPack 交互的公共契约；全局边界见 [AGENTS.md](../../AGENTS.md)。以下源码路径均相对于仓库根目录。

## 后端分层与接口规范

典型调用方向：`sdk API → server → manage → Repository / Provider`。

- `server/` 实现 SDK API，负责 HTTP 边界、参数校验和调用编排；可复用业务规则放入 `manage/` 或现有业务服务。
- `dao/auto/`、`dao/ext/` 放持久化和查询相关代码。部分 `mapper` 目录实际使用 JPA，不得仅因目录名新增 MyBatis 实现。
- 新增或修改接口时，同步检查 SDK 方法、请求/响应模型、实现类、前端封装、权限注解及扩展实现；保持现有路径和序列化兼容性。
- 复用已有 `DEException`、错误码、统一响应和国际化机制，不新增平行的响应包装。不通过捕获所有异常并返回成功或空数据隐藏业务失败。
- 修改错误响应、前端错误消费或认证错误透传前，读取 [错误契约与兼容迁移](../../sdk/common/ERROR_CONTRACT.md)。接入同步 AI 模型调用前，读取 [基础 AI SDK](../ai/sdk-handoff.md)；修改多轮对话、工具执行或增量输出前，读取 [对话助手开发说明](../ai/conversation.md)。
- 数据库实体与 API 模型各司其职；避免直接暴露包含内部状态或敏感字段的实体。
- 新增 Java 代码采用 4 空格缩进，沿用所在模块的注入与命名风格；JPA、Servlet、Validation 等使用 `jakarta.*`。不要批量替换所有 `javax.*`：JCache 当前仍使用 `javax.cache`，JDK 中的 `javax.sql` 等也不属于 Jakarta 迁移范围。
- 对跨多次写入的业务操作明确事务边界；不要让外部数据源查询、网络调用和长时间导出无必要地占用元数据库事务。
- JDBC 连接、结果集、文件流等资源必须可靠关闭；异步任务复用现有调度与生命周期管理机制。
- 异步调用按需显式传递用户、组织及审计上下文，不假定请求上下文自动跨线程传播。使用 `IPUtils`、`CommunityUtils` 等 ThreadLocal 时，按现有生命周期在 `finally` 中清理，避免线程池复用串用上下文。

## 持久化、字段更新与迁移

- 系统元数据主要使用 Spring Data JPA / QueryDSL。新逻辑沿用 JPA Repository、Specification 或 QueryDSL，不引入另一套 ORM。
- 实体使用 `jakarta.persistence`，优先 `@Getter` / `@Setter`，避免对 JPA 实体直接使用可能引入关联递归的 `@Data`。新增表和字段补充含义、长度、可空性及必要注释。
- 业务资源 ID 遵循现有 `Long` / `SnowflakeUtil` 约定，不擅自改为数据库自增。版本记录等特殊实体沿用自身规则。
- 修改保存逻辑前阅读 `core/core-backend/src/main/java/io/dataease/config/JpaUpdateNonNullAspect.java`：它拦截 `save`、`saveAndFlush`、`saveAll`、`saveAllAndFlush`，对已存在实体合并非空属性。
- 不假设向 `save()` 传 `null` 就能清空字段。需要清空时，用明确的 QueryDSL 更新或带 `@Modifying` 的 JPQL 等现有可控方式，在事务中执行，并验证持久化上下文和后续读取结果。区分“未提供字段”和“显式清空”。
- 非空合并切面不等于并发控制或完整的 PATCH 语义；受管实体脏检查、批量更新和绕过 Repository 的路径需分别验证。`saveAll` 也会逐项尝试读取旧实体，批量操作应检查查询次数，不能直接假定只有一次批量 SQL。
- 查询兼顾项目支持的数据库，留意大小写、保留字、布尔值、分页、日期和空值语义。数据库特有 SQL 必须进入现有方言/Provider 分支，不散落在通用业务代码中。
- 数据库变更同时检查实体、QueryDSL 使用处和版本化 `SqlBlock`。`InitSqlListener` 会执行迁移，部分配置启用 `ddl-auto: update`；不能据此省略历史数据迁移和兼容设计。
- 新迁移应具备安全重试能力，检查版本组、执行顺序、默认值和存量数据；不要改写已执行迁移来替代新增迁移。
- 启动应用可能执行迁移并写入配置或业务数据。验证前确认使用隔离的开发/测试环境，不能为代码验证直接连接并改动生产或用户业务数据。

## 数据查询、图表与权限

核心业务链：`数据源 → 数据集 → 图表 → 仪表板/大屏`。

图表取数阅读入口：

1. `core/core-frontend/src/api/chart.ts` 的 `getData()`。
2. SDK `ChartDataApi` 与后端 `ChartDataServer`。
3. `ChartDataManage.calcData()`：字段、数据集权限、行列权限、筛选和钻取编排。
4. `chart/charts/` 中的 Handler、`engine/` 中的 SQL 转换逻辑及数据源 Provider。
5. 前端 `views/chart/components/js/panel/` 中的渲染实现。

- 区分元数据持久化和分析数据查询；分析数据走既有 Provider / Calcite / JDBC 链路，不用元数据库 Repository 替代。
- 图表类型专属计算优先放入对应 Handler；方言和连接相关行为放入对应 Provider；不要持续向公共编排方法堆叠类型分支。
- 修改数据查询时保留字段编解码、计算字段、筛选、排序、聚合、钻取、行列权限和脱敏语义。导出、预览、嵌入与分享是否受影响必须检查。
- SQL 值优先参数化；表名、字段名、排序等不能绑定的结构输入，应通过已有元数据、白名单和方言转义构造，不能直接拼接未校验输入。
- 前端隐藏菜单或按钮不等于鉴权。后端必须检查实际资源、操作和访问身份；复用 `DePermit`、`DeLinkPermit` 及既有权限管理逻辑。
- 权限变更同时审查 SDK 契约、核心调用、`substitute/permissions` 和 XPack 实现。保留条件 Bean 与版本差异，禁止用替补实现或默认放行掩盖权限失败。
- 分享链接、嵌入 Token、管理员代理等访问上下文沿用现有传递与校验方式；缓存键应保留所需用户、组织、权限和筛选维度，防止跨身份复用结果。
- 缓存复用 `DECacheService` / `CacheUtils`，检查写入后的失效路径及实际实现。JCache 动态缓存的正 TTL 按每次写入的键生效，非正时长沿用该缓存创建时的默认期限；XML/native 预建缓存保留自身过期策略。Redis 非正时长不设过期。修改缓存前核对 `sdk/common` 中的 `DefaultCacheImpl`、`DynamicCacheExpiryPolicy` 和 `RedisCacheImpl`，区分 TTL、对象引用和序列化语义；回归入口见 [缓存兼容性说明](../../tests/cache-compatibility/README.md)。
- 资源 ID、权限编码、组件配置和图表类型属于持久化契约。修改时考虑旧仪表板、模板、复制和导入导出的兼容性。
