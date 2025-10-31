# DataEase API 接口文档

本文档整理了 DataEase 前端项目中所有的 API 接口定义和使用方法。

## 目录

- [认证与权限管理](#认证与权限管理)
- [用户管理](#用户管理)
- [组织管理](#组织管理)
- [数据源管理](#数据源管理)
- [数据集管理](#数据集管理)
- [图表管理](#图表管理)
- [可视化管理](#可视化管理)
- [模板管理](#模板管理)
- [登录认证](#登录认证)
- [系统设置](#系统设置)
- [插件管理](#插件管理)
- [消息中心](#消息中心)
- [字体管理](#字体管理)
- [水印管理](#水印管理)
- [通用接口](#通用接口)

---

## 认证与权限管理

### 用户查询
- **接口**: `POST /user/byCurOrg`
- **方法**: `queryUserApi(data)`
- **描述**: 根据当前组织查询用户

### 用户选项
- **接口**: `GET /user/org/option`
- **方法**: `queryUserOptionsApi()`
- **描述**: 获取用户组织选项

### 角色查询
- **接口**: `POST /role/byCurOrg`
- **方法**: `queryRoleApi(data)`
- **描述**: 根据当前组织查询角色

### 资源树
- **接口**: `GET /auth/busiResource/{flag}`
- **方法**: `resourceTreeApi(flag: string)`
- **描述**: 获取业务资源树

### 菜单树
- **接口**: `GET /auth/menuResource`
- **方法**: `menuTreeApi()`
- **描述**: 获取菜单资源树

### 权限管理
- **接口**: `POST /auth/busiPermission`
- **方法**: `resourcePerApi(data)`
- **描述**: 获取资源权限

- **接口**: `POST /auth/menuPermission`
- **方法**: `menuPerApi(data)`
- **描述**: 获取菜单权限

- **接口**: `POST /auth/saveBusiPer`
- **方法**: `busiPerSaveApi(data)`
- **描述**: 保存业务权限

- **接口**: `POST /auth/saveMenuPer`
- **方法**: `menuPerSaveApi(data)`
- **描述**: 保存菜单权限

### 目标权限管理
- **接口**: `POST /auth/busiTargetPermission`
- **方法**: `resourceTargetPerApi(data)`
- **描述**: 获取资源目标权限
- **接口**: `POST /auth/menuTargetPermission`
- **方法**: `menuTargetPerApi(data)`
- **描述**: 获取菜单目标权限
- **接口**: `POST /auth/saveBusiTargetPer`
- **方法**: `busiTargetPerSaveApi(data)`
- **描述**: 保存业务目标权限
- **接口**: `POST /auth/saveMenuTargetPer`
- **方法**: `menuTargetPerSaveApi(data)`
- **描述**: 保存菜单目标权限

---

## 用户管理

### 组织相关
- **接口**: `POST /org/mounted`
- **方法**: `mountedOrg(keyword?: string)`
- **描述**: 挂载组织

- **接口**: `POST /user/switch/{id}`
- **方法**: `switchOrg(id: number | string)`
- **描述**: 切换组织

### 用户信息
- **接口**: `GET /user/info`
- **方法**: `userInfo()`
- **描述**: 获取用户信息

- **接口**: `GET /user/personInfo`
- **方法**: `personInfoApi()`
- **描述**: 个人信息

- **接口**: `GET /user/ipInfo`
- **方法**: `ipInfoApi()`
- **描述**: IP信息

- **接口**: `GET /user/personSysVariableInfo/{uid}`
- **方法**: `personSysVariableInfoApi(uid)`
- **描述**: 个人系统变量

### 用户管理
- **接口**: `POST /user/pager/{page}/{limit}`
- **方法**: `userPageApi(page, limit, data)`
- **描述**: 用户分页查询
- **参数**: `page: number, limit: number, data: { keyword?: string, orgId?: number }`

- **接口**: `POST /user/create`
- **方法**: `userCreateApi(data)`
- **描述**: 创建用户
- **参数**: `{ username: string, password: string, email?: string, phone?: string, nickName?: string, enabled?: boolean }`

- **接口**: `POST /user/edit`
- **方法**: `userEditApi(data)`
- **描述**: 编辑用户
- **参数**: `{ id: number, username: string, email?: string, phone?: string, nickName?: string, enabled?: boolean }`

- **接口**: `POST /user/personEdit`
- **方法**: `personEditApi(data)`
- **描述**: 个人编辑
- **参数**: `{ nickName?: string, email?: string, phone?: string }`

- **接口**: `POST /user/delete/{uid}`
- **方法**: `userDelApi(uid)`
- **描述**: 删除用户
- **参数**: `uid: number`

- **接口**: `GET /user/queryById/{uid}`
- **方法**: `queryFormApi(uid)`
- **描述**: 查询用户
- **参数**: `uid: number`

- **接口**: `POST /user/batchDel`
- **方法**: `batchDelApi(data)`
- **描述**: 批量删除
- **参数**: `{ userIds: number[] }`

### 角色管理
- **接口**: `POST /role/query`
- **方法**: `searchRoleApi(keyword: string)`
- **描述**: 角色搜索
- **参数**: `keyword: string`

- **接口**: `POST /role/create`
- **方法**: `roleCreateApi(data)`
- **描述**: 创建角色
- **参数**: `{ name: string, description?: string, permissions?: string[] }`

- **接口**: `POST /role/edit`
- **方法**: `roleEditApi(data)`
- **描述**: 编辑角色
- **参数**: `{ id: number, name: string, description?: string, permissions?: string[] }`

- **接口**: `GET /role/detail/{rid}`
- **方法**: `roleDetailApi(rid)`
- **描述**: 角色详情
- **参数**: `rid: number`

- **接口**: `POST /role/delete/{rid}`
- **方法**: `roleDelApi(rid)`
- **描述**: 删除角色
- **参数**: `rid: number`

### 用户角色关联
- **接口**: `POST /user/role/option`
- **方法**: `userOptionForRoleApi(data)`
- **描述**: 用户角色选项
- **接口**: `POST /user/role/selected/{page}/{limit}`
- **方法**: `userSelectedForRoleApi(page, limit, data)`
- **描述**: 已选用户
- **接口**: `POST /role/user/option`
- **方法**: `roleOptionForUserApi(data)`
- **描述**: 角色用户选项
- **接口**: `POST /role/mountUser`
- **方法**: `mountUserApi(data)`
- **描述**: 挂载用户
- **接口**: `POST /role/unMountUser`
- **方法**: `unMountUserApi(data)`
- **描述**: 卸载用户
- **接口**: `POST /role/beforeUnmountInfo`
- **方法**: `beforeUnmountInfoApi(data)`
- **描述**: 卸载前信息

### 外部用户
- **接口**: `GET /role/searchExternalUser/{keyword}`
- **方法**: `searchExternalUserApi(keyword)`
- **描述**: 搜索外部用户
- **接口**: `POST /role/mountExternalUser`
- **方法**: `mountExternalUserApi(data)`
- **描述**: 挂载外部用户

### 其他功能
- **接口**: `POST /user/switchLanguage`
- **方法**: `switchLangApi(data)`
- **描述**: 切换语言
- **接口**: `POST /user/excelTemplate`
- **方法**: `downExcelTemplateApi()`
- **描述**: 下载Excel模板
- **接口**: `POST /user/batchImport`
- **方法**: `importUserApi(data)`
- **描述**: 导入用户
- **接口**: `GET /user/errorRecord/{key}`
- **方法**: `downErrorRecordApi(key)`
- **描述**: 下载错误记录
- **接口**: `POST /user/clearError/{key}`
- **方法**: `clearErrorApi(key)`
- **描述**: 清除错误
- **接口**: `GET /user/defaultPwd`
- **方法**: `defaultPwdApi()`
- **描述**: 默认密码
- **接口**: `POST /user/resetPwd/{uid}`
- **方法**: `resetPwdApi(uid)`
- **描述**: 重置密码
- **接口**: `POST /user/enable`
- **方法**: `switchEnableApi(data)`
- **描述**: 启用/禁用

---

## 组织管理

- **接口**: `POST /org/page/tree`
- **方法**: `searchApi(data)`
- **描述**: 组织树查询
- **接口**: `POST /org/page/create`
- **方法**: `saveApi(data)`
- **描述**: 创建组织
- **接口**: `POST /org/page/edit`
- **方法**: `updateApi(data)`
- **描述**: 编辑组织
- **接口**: `GET /org/resourceExist/{oid}`
- **方法**: `resourceExistApi(oid)`
- **描述**: 资源存在检查
- **接口**: `POST /org/page/delete/{oid}`
- **方法**: `deleteApi(oid)`
- **描述**: 删除组织

---

## 数据源管理

### 数据源基础操作
- **接口**: `POST /datasource/tree`
- **方法**: `listDatasources(data)`
- **描述**: 数据源列表
- **参数**: `{ busiFlag?: string, leaf?: boolean }`

- **接口**: `POST /datasource/types`
- **方法**: `listDatasourceType(data)`
- **描述**: 数据源类型
- **参数**: `{}`

- **接口**: `POST /datasource/tables`
- **方法**: `listDatasourceTables(data)`
- **描述**: 数据源表列表
- **参数**: `{ datasourceId: string }`

- **接口**: `POST /datasource/getTableStatus`
- **方法**: `getTableStatus(data)`
- **描述**: 表状态
- **参数**: `{ datasourceId: string, tableName: string }`

- **接口**: `POST /datasource/getSchema`
- **方法**: `getSchema(data)`
- **描述**: 获取Schema
- **参数**: `{ datasourceId: string }`

- **接口**: `POST /datasource/previewData`
- **方法**: `previewData(data)`
- **描述**: 预览数据
- **参数**: `{ datasourceId: string, tableName: string, limit?: number }`

### 数据源管理
- **接口**: `POST /datasource/save`
- **方法**: `save(data)`
- **描述**: 保存数据源
- **参数**: `{ name: string, type: string, configuration: string, pid?: string }`

- **接口**: `POST /datasource/update`
- **方法**: `update(data)`
- **描述**: 更新数据源
- **参数**: `{ id: string, name: string, type: string, configuration: string }`

- **接口**: `POST /datasource/move`
- **方法**: `move(data)`
- **描述**: 移动数据源
- **参数**: `{ id: string, pid: string, nodeType: string }`

- **接口**: `POST /datasource/rename`
- **方法**: `reName(data)`
- **描述**: 重命名
- **参数**: `{ id: string, name: string }`
- **接口**: `POST /datasource/createFolder`
- **方法**: `createFolder(data)`
- **描述**: 创建文件夹
- **参数**: `{ name: string, pid?: string, nodeType: 'folder' }`

- **接口**: `GET /datasource/delete/{id}`
- **方法**: `deleteById(id)`
- **描述**: 删除数据源
- **参数**: `id: string`

- **接口**: `GET /datasource/get/{id}`
- **方法**: `getById(id)`
- **描述**: 获取数据源
- **参数**: `id: string`

- **接口**: `GET /datasource/hidePw/{id}`
- **方法**: `getHidePwById(id)`
- **描述**: 隐藏密码获取
- **参数**: `id: string`

- **接口**: `GET /datasource/getSimpleDs/{id}`
- **方法**: `getSimpleDs(id)`
- **描述**: 简单数据源
- **参数**: `id: string`

### 数据源验证
- **接口**: `POST /datasource/validate`
- **方法**: `validate(data)`
- **描述**: 验证数据源
- **接口**: `GET /datasource/validate/{id}`
- **方法**: `validateById(id)`
- **描述**: 根据ID验证
- **接口**: `POST /datasource/perDelete/{id}`
- **方法**: `perDeleteDatasource(id)`
- **描述**: 权限删除检查
- **接口**: `POST /datasource/checkRepeat`
- **方法**: `checkRepeat(data)`
- **描述**: 重复检查
- **接口**: `POST /datasource/checkApiItem`
- **方法**: `checkApiItem(data)`
- **描述**: API项检查

### 表字段操作
- **接口**: `POST /datasource/getTableField`
- **方法**: `getTableField(data)`
- **描述**: 获取表字段
- **接口**: `POST /datasource/syncApiTable`
- **方法**: `syncApiTable(data)`
- **描述**: 同步API表
- **接口**: `POST /datasource/syncApiDs`
- **方法**: `syncApiDs(data)`
- **描述**: 同步API数据源

### 文件操作
- **接口**: `POST /datasource/uploadFile`
- **方法**: `uploadFile(data)`
- **描述**: 上传文件
- **接口**: `POST /datasource/loadRemoteFile`
- **方法**: `loadRemoteFile(data)`
- **描述**: 加载远程文件

### 其他功能
- **接口**: `POST /datasource/getDatasetTree`
- **方法**: `getDatasetTree(data)`
- **描述**: 数据集树
- **接口**: `POST /datasource/getDsTree`
- **方法**: `getDsTree(data)`
- **描述**: 数据源树
- **接口**: `POST /datasource/latestUse`
- **方法**: `latestUse(data)`
- **描述**: 最近使用
- **接口**: `POST /datasource/listSyncRecord/{dsId}/{page}/{limit}`
- **方法**: `listSyncRecord(page, limit, dsId)`
- **描述**: 同步记录
- **接口**: `GET /datasource/isShowFinishPage`
- **方法**: `isShowFinishPage()`
- **描述**: 显示完成页
- **接口**: `POST /datasource/setShowFinishPage`
- **方法**: `setShowFinishPage(data)`
- **描述**: 设置显示完成页
- **接口**: `GET /engine/getEngine`
- **方法**: `getDeEngine()`
- **描述**: 获取引擎
- **接口**: `GET /engine/supportSetKey`
- **方法**: `supportSetKey()`
- **描述**: 支持设置密钥

---

## 数据集管理

### 数据集树操作
- **接口**: `POST /datasetTree/save`
- **方法**: `saveDatasetTree(data)`
- **描述**: 保存数据集树
- **参数**: `{ name: string, pid?: string, nodeType: 'dataset'|'folder', union?: Array<{}>, allFields?: Array<{}> }`

- **接口**: `POST /datasetTree/create`
- **方法**: `createDatasetTree(data)`
- **描述**: 创建数据集树
- **参数**: `{ name: string, pid?: string, nodeType: 'dataset'|'folder', union?: Array<{}>, allFields?: Array<{}> }`

- **接口**: `POST /datasetTree/rename`
- **方法**: `renameDatasetTree(data)`
- **描述**: 重命名数据集树
- **参数**: `{ id: string, name: string }`

- **接口**: `POST /datasetTree/move`
- **方法**: `moveDatasetTree(data)`
- **描述**: 移动数据集树
- **参数**: `{ id: string, pid: string, nodeType: string }`

- **接口**: `POST /datasetTree/tree`
- **方法**: `getDatasetTree(data)`
- **描述**: 获取数据集树
- **参数**: `{ busiFlag: 'dataset' }`

- **接口**: `POST /datasetTree/delete/{id}`
- **方法**: `delDatasetTree(id)`
- **描述**: 删除数据集树
- **参数**: `id: string`

### 数据集数据操作
- **接口**: `POST /dataset/enumValueObj`
- **方法**: `enumValueObj(data)`
- **描述**: 枚举值对象
- **接口**: `POST /dataset/enumValueDs`
- **方法**: `enumValueDs(data)`
- **描述**: 枚举值数据源
- **接口**: `POST /dataset/previewData`
- **方法**: `getPreviewData(data)`
- **描述**: 获取预览数据
- **接口**: `GET /dataset/preview/{id}`
- **方法**: `getDatasetPreview(id)`
- **描述**: 数据集预览
- **接口**: `GET /dataset/total/{id}`
- **方法**: `getDatasetTotal(id)`
- **描述**: 数据集总数
- **接口**: `GET /dataset/details/{id}`
- **方法**: `getDatasetDetails(id)`
- **描述**: 数据集详情
- **接口**: `POST /dataset/detailsWithPerm`
- **方法**: `getDsDetailsWithPerm(data)`
- **描述**: 数据集详情(带权限)
- **接口**: `POST /dataset/getSqlParams`
- **方法**: `getSqlParams(data)`
- **描述**: SQL参数

### 表和字段操作
- **接口**: `POST /datasource/list`
- **方法**: `getDatasourceList(weight?)`
- **描述**: 获取数据源列表
- **接口**: `POST /datasource/getTables`
- **方法**: `getTables(data)`
- **描述**: 获取表
- **接口**: `POST /datasource/getTableField`
- **方法**: `getTableField(data)`
- **描述**: 获取表字段
- **接口**: `POST /dataset/tableUpdate`
- **方法**: `tableUpdate(data)`
- **描述**: 表更新
- **接口**: `POST /dataset/previewSql`
- **方法**: `getPreviewSql(data)`
- **描述**: 预览SQL
- **接口**: `GET /dataset/barInfo/{id}`
- **方法**: `barInfoApi(id)`
- **描述**: 条形图信息

### 字段管理
- **接口**: `POST /dataset/saveField`
- **方法**: `saveField(data)`
- **描述**: 保存字段
- **接口**: `POST /dataset/deleteField/{id}`
- **方法**: `deleteField(id)`
- **描述**: 删除字段
- **接口**: `POST /dataset/deleteFieldByChart/{id}`
- **方法**: `deleteFieldByChartId(id)`
- **描述**: 根据图表ID删除字段
- **接口**: `POST /dataset/getEnumValue`
- **方法**: `getEnumValue(data)`
- **描述**: 获取枚举值
- **接口**: `GET /dataset/getFunction`
- **方法**: `getFunction()`
- **描述**: 获取函数
- **接口**: `GET /dataset/listFieldByDatasetGroup/{datasetId}`
- **方法**: `listFieldByDatasetGroup(datasetId)`
- **描述**: 根据数据集组列出字段
- **接口**: `GET /dataset/listFieldsWithPermissions/{datasetId}`
- **方法**: `listFieldsWithPermissions(datasetId)`
- **描述**: 带权限的字段列表
- **接口**: `GET /dataset/copilotFields/{datasetId}`
- **方法**: `copilotFields(datasetId)`
- **描述**: Copilot字段
- **接口**: `POST /dataset/getFieldTree`
- **方法**: `getFieldTree(data)`
- **描述**: 字段树

### 权限管理
- **接口**: `GET /dataset/rowPermissions/pager/{datasetId}/{page}/{limit}`
- **方法**: `rowPermissionList(page, limit, datasetId)`
- **描述**: 行权限列表
- **接口**: `GET /dataset/columnPermissions/pager/{datasetId}/{page}/{limit}`
- **方法**: `columnPermissionList(page, limit, datasetId)`
- **描述**: 列权限列表
- **接口**: `GET /dataset/rowPermissions/authObjs/{datasetId}/{type}`
- **方法**: `rowPermissionTargetObjList(datasetId, type)`
- **描述**: 行权限目标对象
- **接口**: `POST /dataset/multFieldValuesForPermissions`
- **方法**: `multFieldValuesForPermissions(data)`
- **描述**: 多字段值权限
- **接口**: `POST /dataset/whiteListUsersForPermissions`
- **方法**: `whiteListUsersForPermissions(data)`
- **描述**: 权限白名单用户
- **接口**: `POST /dataset/saveRowPermission`
- **方法**: `saveRowPermission(data)`
- **描述**: 保存行权限
- **接口**: `POST /dataset/saveColumnPermission`
- **方法**: `saveColumnPermission(data)`
- **描述**: 保存列权限
- **接口**: `POST /dataset/deleteRowPermission`
- **方法**: `deleteRowPermission(data)`
- **描述**: 删除行权限
- **接口**: `POST /dataset/deleteColumnPermission`
- **方法**: `deleteColumnPermission(data)`
- **描述**: 删除列权限

### 导出功能
- **接口**: `POST /dataset/export`
- **方法**: `exportDatasetData(data)`
- **描述**: 导出数据集数据

- **接口**: `GET /dataset/exportLimit`
- **方法**: `exportLimit()`
- **描述**: 导出限制

- **接口**: `GET /dataset/perDelete/{id}`
- **方法**: `perDelete(id)`
- **描述**: 权限删除

- **接口**: `POST /exportCenter/exportTasks/records`
- **方法**: `exportTasksRecords()`
- **描述**: 导出任务记录

- **接口**: `POST /exportCenter/exportTasks/{status}/{page}/{limit}`
- **方法**: `exportTasks(page, limit, status)`
- **描述**: 导出任务

- **接口**: `POST /exportCenter/exportRetry/{id}`
- **方法**: `exportRetry(id)`
- **描述**: 导出重试

- **接口**: `GET /exportCenter/downloadFile/{id}`
- **方法**: `downloadFile(id)`
- **描述**: 下载文件

- **接口**: `POST /exportCenter/exportDelete/{id}`
- **方法**: `exportDelete(id)`
- **描述**: 导出删除

- **接口**: `POST /exportCenter/generateDownloadUri/{id}`
- **方法**: `generateDownloadUri(id)`
- **描述**: 生成下载URI

- **接口**: `POST /exportCenter/exportDeleteAll/{type}`
- **方法**: `exportDeleteAll(type, data)`
- **描述**: 批量删除导出

- **接口**: `POST /exportCenter/exportDeletePost`
- **方法**: `exportDeletePost(data)`
- **描述**: 导出删除POST

### Copilot功能
- **接口**: `POST /dataset/copilotChat`
- **方法**: `copilotChat(data)`
- **描述**: Copilot聊天

- **接口**: `GET /dataset/getListCopilot`
- **方法**: `getListCopilot()`
- **描述**: 获取Copilot列表

- **接口**: `POST /dataset/clearAllCopilot`
- **方法**: `clearAllCopilot()`
- **描述**: 清除所有Copilot

### 其他功能
- **接口**: `POST /dataset/listByDsIds`
- **方法**: `listByDsIds(data)`
- **描述**: 根据数据源ID列表

---

## 图表管理

### 字段操作
- **接口**: `POST /chart/listByDQ/{id}/{chartId}`
- **方法**: `getFieldByDQ(id, chartId, data)`
- **描述**: 根据DQ获取字段
- **参数**: `id: string, chartId: string, data: {}`

- **接口**: `POST /chart/copyField/{id}/{chartId}`
- **方法**: `copyChartField(id, chartId)`
- **描述**: 复制图表字段
- **参数**: `id: string, chartId: string`

- **接口**: `POST /chart/deleteField/{id}`
- **方法**: `deleteChartField(id)`
- **描述**: 删除图表字段
- **参数**: `id: string`

- **接口**: `POST /chart/deleteFieldByChart/{chartId}`
- **方法**: `deleteChartFieldByChartId(chartId)`
- **描述**: 根据图表ID删除字段
- **参数**: `chartId: string`

### 图表数据
- **接口**: `POST /chartData/getData`
- **方法**: `getData(data)`
- **描述**: 获取数据
- **参数**: `{ xAxis?: Field[], yAxis?: Field[], extStack?: Field[], extLabel?: Field[], extTooltip?: Field[], extColor?: Field[], view: ChartObj }`

- **接口**: `POST /chartData/innerExportDetails`
- **方法**: `innerExportDetails(data)`
- **描述**: 内部导出详情
- **参数**: `{ view: ChartObj, exportType: string }`

- **接口**: `POST /chartData/innerExportDataSetDetails`
- **方法**: `innerExportDataSetDetails(data)`
- **描述**: 内部导出数据集详情
- **参数**: `{ datasetId: string, exportType: string }`

- **接口**: `POST /chart/getFieldData`
- **方法**: `getFieldData({fieldId, fieldType, data})`
- **描述**: 获取字段数据
- **参数**: `{ fieldId: string, fieldType: string, data: {} }`

- **接口**: `POST /chart/getDrillFieldData`
- **方法**: `getDrillFieldData({fieldId, data})`
- **描述**: 获取钻取字段数据
- **参数**: `{ fieldId: string, data: {} }`

### 图表管理
- **接口**: `GET /chart/get/{id}`
- **方法**: `getChart(id)`
- **描述**: 获取图表

- **接口**: `POST /chart/save`
- **方法**: `saveChart(data)`
- **描述**: 保存图表

- **接口**: `GET /chart/detail/{id}`
- **方法**: `getChartDetail(id)`
- **描述**: 获取图表详情

- **接口**: `GET /chart/checkSameDataSet/{viewIdSource}/{viewIdTarget}`
- **方法**: `checkSameDataSet(viewIdSource, viewIdTarget)`
- **描述**: 检查相同数据集

---

## 可视化管理

### 基础操作
- **接口**: `GET /dataVisualization/findCopyResource/{dvId}/{busiFlag}`
- **方法**: `findCopyResource(dvId, busiFlag)`
- **描述**: 查找复制资源

- **接口**: `POST /dataVisualization/findById`
- **方法**: `findById(dvId, busiFlag, attachInfo)`
- **描述**: 根据ID查找

- **接口**: `GET /dataVisualization/updateCheckVersion/{dvId}`
- **方法**: `updateCheckVersion(dvId)`
- **描述**: 更新检查版本

- **接口**: `POST /dataVisualization/tree`
- **方法**: `queryTreeApi(data)`
- **描述**: 查询树

- **接口**: `POST /dataVisualization/queryBusiTree`
- **方法**: `queryBusiTreeApi(data)`
- **描述**: 查询业务树

- **接口**: `GET /dataVisualization/findDvType/{dvId}`
- **方法**: `findDvType(dvId)`
- **描述**: 查找DV类型

### 保存和更新
- **接口**: `POST /dataVisualization/save`
- **方法**: `save(data)`
- **描述**: 保存

- **接口**: `POST /dataVisualization/checkCanvasChange`
- **方法**: `checkCanvasChange(data)`
- **描述**: 检查画布变更

- **接口**: `POST /dataVisualization/saveCanvas`
- **方法**: `saveCanvas(data)`
- **描述**: 保存画布

- **接口**: `POST /dataVisualization/updatePublishStatus`
- **方法**: `updatePublishStatus(data)`
- **描述**: 更新发布状态

- **接口**: `POST /dataVisualization/recoverToPublished`
- **方法**: `recoverToPublished(data)`
- **描述**: 恢复到已发布

- **接口**: `POST /dataVisualization/appCanvasNameCheck`
- **方法**: `appCanvasNameCheck(data)`
- **描述**: 应用画布名称检查

- **接口**: `POST /dataVisualization/updateBase`
- **方法**: `updateBase(data)`
- **描述**: 更新基础信息

- **接口**: `POST /dataVisualization/updateCanvas`
- **方法**: `updateCanvas(data)`
- **描述**: 更新画布

### 资源操作
- **接口**: `POST /dataVisualization/move`
- **方法**: `moveResource(data)`
- **描述**: 移动资源

- **接口**: `POST /dataVisualization/copy`
- **方法**: `copyResource(data)`
- **描述**: 复制资源

- **接口**: `POST /dataVisualization/deleteLogic/{dvId}/{busiFlag}`
- **方法**: `deleteLogic(dvId, busiFlag)`
- **描述**: 逻辑删除

- **接口**: `POST /dataVisualization/nameCheck`
- **方法**: `dvNameCheck(data)`
- **描述**: 名称检查

### 主题管理
- **接口**: `POST /visualizationSubject/querySubjectWithGroup`
- **方法**: `querySubjectWithGroupApi(data)`
- **描述**: 查询主题和组

- **接口**: `POST /visualizationSubject/update`
- **方法**: `saveOrUpdateSubject(data)`
- **描述**: 保存或更新主题

- **接口**: `POST /visualizationSubject/delete/{id}`
- **方法**: `deleteSubject(id)`
- **描述**: 删除主题

### 存储和压缩
- **接口**: `POST /dataVisualization/store`
- **方法**: `storeApi(data)`
- **描述**: 存储

- **接口**: `GET /dataVisualization/storeStatus/{id}`
- **方法**: `storeStatusApi(id)`
- **描述**: 存储状态

- **接口**: `POST /dataVisualization/decompression`
- **方法**: `decompression(data)`
- **描述**: 解压缩

### 详情和组件
- **接口**: `POST /dataVisualization/viewDetailList/{dvId}`
- **方法**: `viewDetailList(dvId)`
- **描述**: 视图详情列表

- **接口**: `POST /dataVisualization/getComponentInfo/{dvId}`
- **方法**: `getComponentInfo(dvId)`
- **描述**: 获取组件信息

- **接口**: `POST /dataVisualization/export2AppCheck`
- **方法**: `export2AppCheck(params)`
- **描述**: 导出到应用检查

- **接口**: `POST /dataVisualization/queryOuterParamsDsInfo/{dvId}`
- **方法**: `queryOuterParamsDsInfo(dvId)`
- **描述**: 查询外部参数数据源信息

- **接口**: `POST /dataVisualization/queryShareBase`
- **方法**: `queryShareBaseApi()`
- **描述**: 查询共享基础

### 导出日志
- **接口**: `POST /dataVisualization/exportLogApp`
- **方法**: `exportLogApp(data)`
- **描述**: 导出应用日志

- **接口**: `POST /dataVisualization/exportLogTemplate`
- **方法**: `exportLogTemplate(data)`
- **描述**: 导出模板日志

- **接口**: `POST /dataVisualization/exportLogPDF`
- **方法**: `exportLogPDF(data)`
- **描述**: 导出PDF日志

- **接口**: `POST /dataVisualization/exportLogImg`
- **方法**: `exportLogImg(data)`
- **描述**: 导出图片日志

---

## 模板管理

### 基础操作
- **接口**: `POST /templateManage/save`
- **方法**: `save(data)`
- **描述**: 保存模板

- **接口**: `POST /templateManage/delete/{id}/{categoryId}`
- **方法**: `templateDelete(id, categoryId)`
- **描述**: 删除模板

- **接口**: `POST /templateManage/deleteCategory/{id}`
- **方法**: `deleteCategory(id)`
- **描述**: 删除分类

- **接口**: `POST /templateManage/templateList`
- **方法**: `showTemplateList(data)`
- **描述**: 显示模板列表

- **接口**: `GET /templateManage/findOne/{id}`
- **方法**: `findOne(id)`
- **描述**: 查找单个

- **接口**: `POST /templateManage/find`
- **方法**: `find(data)`
- **描述**: 查找

- **接口**: `POST /templateManage/findCategories`
- **方法**: `findCategories(data)`
- **描述**: 查找分类

### 名称检查
- **接口**: `POST /templateManage/nameCheck`
- **方法**: `nameCheck(data)`
- **描述**: 名称检查

- **接口**: `POST /templateManage/categoryTemplateNameCheck`
- **方法**: `categoryTemplateNameCheck(data)`
- **描述**: 分类模板名称检查

- **接口**: `POST /templateManage/checkCategoryTemplateBatchNames`
- **方法**: `checkCategoryTemplateBatchNames(data)`
- **描述**: 批量名称检查

### 批量操作
- **接口**: `POST /templateManage/batchDelete`
- **方法**: `batchDelete(data)`
- **描述**: 批量删除

- **接口**: `POST /templateManage/batchUpdate`
- **方法**: `batchUpdate(data)`
- **描述**: 批量更新

- **接口**: `POST /templateManage/findCategoriesByTemplateIds`
- **方法**: `findCategoriesByTemplateIds(data)`
- **描述**: 根据模板ID查找分类

---

## 登录认证

- **接口**: `POST /login/localLogin`
- **方法**: `loginApi(data)`
- **描述**: 本地登录
- **参数**: `{ username: string, password: string, remember?: boolean }`

- **接口**: `GET /dekey`
- **方法**: `queryDekey()`
- **描述**: 查询密钥
- **参数**: 无

- **接口**: `GET /symmetricKey`
- **方法**: `querySymmetricKey()`
- **描述**: 查询对称密钥
- **参数**: 无

- **接口**: `GET /model`
- **方法**: `modelApi()`
- **描述**: 模型
- **参数**: 无

- **接口**: `POST /login/platformLogin/{origin}`
- **方法**: `platformLoginApi(origin)`
- **描述**: 平台登录
- **参数**: `origin: string`

- **接口**: `GET /logout`
- **方法**: `logoutApi()`
- **描述**: 登出
- **参数**: 无

- **接口**: `GET /login/refresh`
- **方法**: `refreshApi(time?)`
- **描述**: 刷新
- **参数**: `time?: number`

- **接口**: `GET /sysParameter/ui`
- **方法**: `uiLoadApi()`
- **描述**: UI加载
- **参数**: 无

- **接口**: `GET /sysParameter/defaultLogin`
- **方法**: `loginCategoryApi()`
- **描述**: 登录分类
- **参数**: 无

---

## 系统设置

- **接口**: `GET /sysParameter/queryOnlineMap`
- **方法**: `queryMapKeyApi()`
- **描述**: 查询地图密钥
- **接口**: `GET /sysParameter/queryOnlineMap/{type}`
- **方法**: `queryMapKeyApiByType(type)`
- **描述**: 根据类型查询地图密钥
- **接口**: `POST /sysParameter/saveOnlineMap`
- **方法**: `saveMapKeyApi(data)`
- **描述**: 保存地图密钥

---

## 插件管理

- **接口**: `GET /xpackComponent/content/{key}`
- **方法**: `load(key)`
- **描述**: 加载插件
- **接口**: `GET /xpackComponent/contentPlugin/{key}`
- **方法**: `loadPluginApi(key)`
- **描述**: 加载插件API
- **接口**: `GET /DEXPack.umd.js`
- **方法**: `loadDistributed()`
- **描述**: 加载分布式
- **接口**: `GET /xpackModel`
- **方法**: `xpackModelApi()`
- **描述**: Xpack模型

---

## 消息中心

- **接口**: `POST /msg-center/count`
- **方法**: `msgCountApi()`
- **描述**: 消息计数

---

## 字体管理

- **接口**: `GET /typeface/listFont`
- **方法**: `list()`
- **描述**: 字体列表
- **接口**: `POST /typeface/create`
- **方法**: `create(data)`
- **描述**: 创建字体
- **接口**: `POST /typeface/edit`
- **方法**: `edit(data)`
- **描述**: 编辑字体
- **接口**: `POST /typeface/delete/{id}`
- **方法**: `deleteById(id)`
- **描述**: 删除字体
- **接口**: `GET /typeface/defaultFont`
- **方法**: `defaultFont()`
- **描述**: 默认字体
- **接口**: `POST /typeface/uploadFontFile`
- **方法**: `uploadFontFile(data)`
- **描述**: 上传字体文件

---

## 水印管理

- **接口**: `POST /watermark/save`
- **方法**: `watermarkSave(params)`
- **描述**: 保存水印
- **接口**: `GET /watermark/find`
- **方法**: `watermarkFind()`
- **描述**: 查找水印

---

## 通用接口

- **接口**: `GET /menu/query`
- **方法**: `getRoleRouters()`
- **描述**: 获取权限路由
- **接口**: `GET /sysParameter/defaultSettings`
- **方法**: `getDefaultSettings()`
- **描述**: 获取默认设置

---

## 接口类型定义

### 字段类型 (Field)
```typescript
export interface Field {
  id: number | string
  datasourceId: number | string
  datasetTableId: number | string
  datasetGroupId: number | string
  originName: string
  name: string
  dataeaseName: string
  groupType: string
  type: string
  deType: number
  deExtractType: number
  extField: number
  checked: boolean
  fieldShortName: string
  desensitized: boolean
}
```

### 组件信息 (ComponentInfo)
```typescript
export interface ComponentInfo {
  id: string
  name: string
  deType: number
  type: string
  datasetId: string
}
```

### 数据集或文件夹 (DatasetOrFolder)
```typescript
export interface DatasetOrFolder {
  name: string
  action?: string
  isCross?: boolean
  id?: number | string
  pid?: number | string
  nodeType: 'folder' | 'dataset'
  union?: Array<{}>
  allFields?: Array<{}>
}
```

### 枚举值 (EnumValue)
```typescript
export interface EnumValue {
  queryId: string
  displayId?: string
  sortId?: string
  sort?: string
  resultMode?: number
  searchText: string
  filter?: Array<{}>
}
```

### 字体 (Font)
```typescript
export interface Font {
  id: string
  name: string
  fileName: string
  isDefault: boolean
  isBuiltin?: boolean
}
```

### 资源或文件夹 (ResourceOrFolder)
```typescript
export interface ResourceOrFolder {
  name: string
  id?: number | string
  pid?: number | string
  nodeType: 'folder' | 'leaf'
  type: string
  mobileLayout: boolean
  status: boolean
}
```

---

## 注意事项

1. 所有接口都使用统一的 `request` 实例，来自 `@/config/axios`
2. 大部分接口返回 `Promise<IResponse>` 类型
3. 部分接口支持加载状态控制 (`loading: true/false`)
4. 文件上传接口使用 `multipart/form-data` 头部类型
5. 部分接口支持 `responseType: 'blob'` 用于文件下载
6. 接口中的数据处理包含字段名称转换 (`originNameHandle` 系列函数)

---

*文档生成时间: 2024年*
*基于 DataEase 前端 API 文件自动生成*