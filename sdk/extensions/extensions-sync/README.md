# 同步插件驱动冲突处理

仅同步源、目标插件走本流程，普通数据源、图表、填报插件仍使用原安装逻辑。

上传包先暂存并检查 DE 与执行器文件。缺失安装，相同内容复用，同一 JDBC 驱动内容不同则逐项询问保留或覆盖。驱动身份根据 JDBC SPI、文件名和匹配的 Maven 标识识别；版本仅用于展示，内容按 SHA-256 比较。

- 保留：两侧都完全跳过对应驱动，不统一版本，也不清理原有重复文件，继续安装插件
- 覆盖：先处理 DE，再传到执行器；写入上传文件后删除已识别的同类旧文件
- 关闭：取消安装并清理暂存文件
- 连接器及其他依赖：缺失安装、相同复用、冲突报错，不参与 JDBC 覆盖选择

前端显示插件配置中的数据库名称，为空时使用类型；展示驱动文件、当前版本、上传版本，不展示文件路径。两侧当前版本不同时区分来源。覆盖影响使用同一驱动的源、目标插件，完成后提示重启相关服务。

所有文件操作和插件保存成功才提示安装完成。中途失败报告阶段，不备份、不回滚、不自动重试；管理员核对实际文件后重新上传。暂存确认有效期 30 分钟，服务重启后需重新上传。

## 目录与升级

DE 使用 `/opt/dataease3.0/data/plugin/sync-drivers/<type>/{lib,connectors}`，复用已有插件目录挂载；执行器使用 `seatunnel/{lib,connectors}`，通过现有地址的 inspect/apply 接口处理上传文件，不依赖共享目录。

旧插件默认路径映射到新目录，明确配置的自定义路径保持不变。本功能不自动迁移：升级前将历史 `/opt/dataease3.0/drivers/plugin/sync` 或 `/opt/dataease3.0/data/driver/plugin/sync` 中实际使用的文件复制到新根目录，保留相对布局并人工核对重复版本。先保留旧文件再删除旧容器，不能根据包内版本推断历史选择。

插件启动只注册和加载已确定的驱动，不解包覆盖运行文件。DE 启动及任务提交不跨服务补齐依赖。执行器保留原本的本地复制：任务启动前从执行器自身可读的 `/opt/dataease3.0/drivers/plugin/sync` 递归读取 JAR，向本机 SeaTunnel 补入缺失项；同一驱动已存在则跳过，源目录不存在则不处理。源中同一缺失依赖有不同内容时要求先确认文件，不按遍历顺序选版本。驱动缺失且本地源不可用时，需手工放入正确目录或重新上传插件安装。卸载插件不删除其他插件可能使用的依赖。

## 边界

不做多版本隔离、自动兼容推断、运行任务管理或热切换。更新需避开相关运行任务并重启服务；文件操作成功不代表新驱动兼容所有旧插件。保留可能留下两侧差异，外部修改可能重新引入重复文件。版本差异和已有重复文件不由任务启动自动修复；缺失项仅可从执行器本地源补入。

`SyncPluginInfoLoader` 是普通类加载器下读取插件描述的独立兼容修复，PG 插件驱动升级及版本配置也是独立变更，不因清理此流程而删除。

## 实现职责

- `PluginInstallCheck`、`PluginInstallConfirm`：上传检查结果和用户确认参数，属于公开 API 契约
- `dependencyConfirm.ts`：展示冲突并提交选择，支持取消，不在前端选择驱动版本
- `SyncDependencyDirectory`：DE 侧的文件识别、比较及替换规则
- `SyncDependencyTransfer`：跨服务传输清单、文件内容和选择，不包含任务运行管理
- `SyncPluginInfoLoader`：读取插件描述与图标，独立解决类加载器兼容问题

后端安装编排与执行器接口通过兼容的 HTTP JSON 字段协作，不在 SDK 中实现插件业务持久化。驱动目录由 SDK 解析，历史 `dependencyLayout` 字段不再声明或参与逻辑，描述文件中的该字段由现有 JSON 配置忽略。

## 非 JDBC 连接器接入

### 非 JDBC 接入示例：MongoDB

本例说明如何扩展新插件，不表示当前 PG 插件已支持 MongoDB，也不表示已完成 MongoDB 任务验证。连接器版本须匹配实际部署的 SeaTunnel 引擎及 DE 定制内容，不能仅按版本号推断兼容性。

在新插件后端的资源目录按需放置依赖，`<version>` 替换为核对后的实际版本：

```text
src/main/resources/sync/
├── connectors/
│   └── connector-mongodb-<version>.jar
└── lib/
    └── <必要的额外运行库>.jar
```

`lib` 没有额外依赖时可省略；连接器已经包含的库不要重复放，也不要携带 SeaTunnel 核心库或 DE 插件主包。检查最终插件 JAR 中仍保留 `sync/connectors/` 和 `sync/lib/` 路径。执行器已提供且满足需求的连接器可不重复打包，但这意味着部署时必须保留该依赖。

新插件须实现数据库连接检查、元数据读取及对应 Source/Sink 配置生成，不能直接保留 PG 的 JDBC 连接和 SQL 逻辑。任务配置使用连接器实际标识 `MongoDB`，所需参数按选定连接器实现配置；插件描述中的数据库类型（例如 `mongodb`）与任务中的连接器标识用途不同，不能互相替代。

核对执行器的 `seatunnel/connectors/plugin-mapping.properties`：

```properties
seatunnel.source.MongoDB = connector-mongodb
seatunnel.sink.MongoDB = connector-mongodb
```

当前执行器文件已有这两项，接入该示例无需重复添加；其他连接器应检查实际部署文件，只补其支持的源或目标映射。映射左侧须与任务配置及连接器返回的标识一致，右侧为连接器 JAR 的 artifactId（不带版本和 `.jar`），不要通过改名绕过冲突检查。

自研连接器还需保留所用 SeaTunnel API 对应的 `META-INF/services/` 注册文件，例如 `org.apache.seatunnel.api.source.SeaTunnelSource`、`org.apache.seatunnel.api.sink.SeaTunnelSink` 或相应的 Table Factory 服务；文件内容为实际实现类全名，按连接器实现选择，并非四项都必须提供。重新打包时不要丢失这些 SPI 资源。上传成功不代表 SPI、映射或任务参数有效。

上传流程只处理 `sync/` 中的 JAR，不安装其中的 properties 配置，不自动修改执行器映射、不生成 SPI、不下载传递依赖，也不验证引擎兼容性。映射缺失时由开发者随执行器配置维护，不新增自动注册或连接器覆盖弹窗。

确认安装后，默认 DE 文件位于 `/opt/dataease3.0/data/plugin/sync-drivers/mongodb/{lib,connectors}`，执行器文件位于其 JVM 工作目录下的 `seatunnel/{lib,connectors}`。已识别为同一连接器时，缺失安装、单份相同内容复用，内容不同或已有多份则报错，不能用 JDBC 的覆盖选择处理。非 JDBC 插件并不会因为名称包含“驱动”就获得 JDBC 覆盖行为。

开发验证使用隔离环境：先核对最终插件包内容、映射和 SPI，再检查首次上传及重复上传的文件结果，最后验证实际支持的源读取或目标写入。冲突场景应明确报错且不盲目覆盖；不要用真实业务数据库验证示例。文件安装检查不能代替任务运行验证。

### 文件处理代码入口

- `SyncDependencyDirectory.stage`：提取 `sync/` 下的 JAR，按显式子目录分类；历史平铺文件根据 `connector-` 名称或 SeaTunnel SPI 识别连接器
- `SyncDependencyDirectory.inspect`：读取文件身份、展示版本及校验值；识别为连接器不等于验证它能被引擎加载
- `SyncDependencyDirectory.plan/apply`：检查并写入文件，连接器及普通依赖冲突直接报错
- `SyncDependencyTransfer`：传输元数据与 JAR 内容，执行器使用自身实现按兼容规则安装至本机 SeaTunnel 目录

连接器身份主要依据文件名及匹配的 Maven 标识，不能识别任意改名后的语义重复实现；应保留规范命名并人工核对自研连接器。DE 保存连接器文件用于安装依赖管理，实际任务由执行器 SeaTunnel 加载，不会自动让 DE 插件获得数据库访问能力。

执行器不引入本 SDK；检查、上传的对象在执行器中独立定义。SDK 只保留 DE 使用的暂存、文件检查和请求编码，执行器本地缺失复制及上传解码由执行器自身维护。协议字段与文件身份规则变更需核对两侧兼容性，不要求共享 Java 类或绑定 DE SDK 版本。
