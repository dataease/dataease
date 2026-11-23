# Datasource save/update 四阶段编排拆解

> 范围：`core/core-backend` 的 `DatasourceServer.save:335` / `update:468`。
> 目标：区分配置提交、外部执行、结果确认三个边界，失败可定位、可恢复。

## 1. 当前步骤顺序与副作用（盘点）

### save（首次创建）

| 类型 | 步骤顺序 | 副作用 | 失败后果（当前） |
| --- | --- | --- | --- |
| 普通数据库 | 权限校验 → 配置还原 → Excel 校验 → preCheck → 状态探测 → `innerSave` → 异步 `calciteProvider.update` | 元数据写、连接池更新（异步） | `innerSave` 后异步失败只写 `status=Error`，用户已看到成功 |
| Excel | 同上 → `innerSave` → 逐表 `createEngineTable` → `extractExcelData` | 元数据、引擎表、导入 | 建表/导入失败抛异常，但元数据已提交（无回滚） |
| ExcelRemote | 同上 → `innerSave` → 逐表建表 → `insert` 任务 → `addSchedule` | 元数据、引擎表、调度 | 调度失败但元数据/表已建 |
| API | 同上 → `innerSave` → 拉 API 表 → 建引擎表 → `insert` 任务 → `addSchedule` | 元数据、引擎表、调度 | 同上 |

### update（更新）

| 类型 | 步骤顺序 | 副作用 | 失败后果（当前） |
| --- | --- | --- | --- |
| 普通数据库 | 配置还原 → Excel 校验 → preCheck → 状态探测 → `checkName` → `innerEdit` → 异步 `calciteProvider.update` | 元数据、连接池（异步） | 同上 |
| Excel | `extractExcelData` → `mergeSheets` → `checkName` → `innerEdit` | 导入、文件快照、元数据 | 导入失败则元数据未改；但 `extractExcelData` 可能已部分写引擎表 |
| ExcelRemote / API | 拉取新旧表 diff → 计算 `toCreate/toDelete`（结构变化也走删+建）→ `update` 任务 → 删表 → 建表 → `deleteSchedule` → `addSchedule` → `checkName` → `innerEdit` | 调度、引擎表、元数据 | 删表/建表/调度任意失败都抛异常，元数据未写；但已删除的表无法恢复 |

## 2. 目标四阶段模型

```
Plan    → 预校验 + 生成执行计划（纯计算，不产生副作用）
Commit  → 短事务：写元数据（innerSave/innerEdit）+ 落执行记录（executionId/configVersion/steps/status）
Execute → 事务提交后：外部建表、导入、调度 upsert、连接池更新（幂等步骤，逐步回写执行记录）
Confirm → 回写数据源 status、任务状态；失败时记录可定位的失败步骤
```

规则：

1. `DatasourceServer.save/update` 对外签名与返回值不变。
2. 元数据事务只包含 `innerSave/innerEdit` 与执行记录写入；外部操作一律在事务提交后执行。
3. 每个外部步骤写入执行记录（step/status/result），进程重启后可扫描未完成记录重试或标记失败。
4. 破坏性替换（删表/重导）采用 prepare-then-switch：先建临时资源、成功后切换、失败清理临时资源。

## 3. 失败矩阵（目标状态）

| 失败点 | 用户可见 | 恢复入口 |
| --- | --- | --- |
| 配置提交失败 | 保存失败 | 无残留：执行记录 `COMMIT_FAILED` |
| 外部建表失败 | 保存成功、状态 Error | 执行记录 `TABLE_CREATE_FAILED`，重试该步骤 |
| 导入失败 | 保存成功、状态 Error | 执行记录 `IMPORT_FAILED`，重试导入 |
| 调度失败 | 保存成功、状态 Error | 执行记录 `SCHEDULE_FAILED`，重试 upsert |
| 连接池更新失败 | 保存成功、状态 Error | 执行记录 `CALCITE_UPDATE_FAILED`，重试 |
| 进程重启 | — | 启动扫描未完成执行记录，续跑或标记失败 |

## 4. 执行记录模型（建议）

`CoreDatasourceSaveExecution`：

- `id`（executionId）
- `dsId`
- `configVersion`（配置版本号，可先用 updateTime 或 hash）
- `step`（PLAN/COMMIT/TABLE_CREATE/IMPORT/SCHEDULE/CALCITE_UPDATE/CONFIRM）
- `status`（PENDING/SUCCESS/FAILED）
- `result`（失败原因或步骤结果 JSON）
- `retryCount`
- `createTime` / `updateTime`

## 5. 逐类型策略

- 普通数据库：Commit 后 `calciteProvider.update`；失败写 `status=Error` 并记录。
- Excel：建表与导入幂等；replace 先准备临时表/文件，成功后切换；add_scope 沿用既有追加语义。
- ExcelRemote：建表 + 调度 upsert 幂等。
- API：`toCreate/toDelete/toRecreate` 计划化，删表/建表/调度逐步回写；调度 upsert 幂等。
