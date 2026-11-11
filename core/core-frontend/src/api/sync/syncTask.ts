import request from '@/config/axios'

export interface IGetTaskInfoReq {
  id?: string
  name?: string
}

export interface ITaskInfoInsertReq {
  [key: string]: any
}

export interface ISchedulerOption {
  interval: number
  unit: string
}

export interface ISource {
  datasource?: { id: string; name: string }
  type: string
  query: string
  tables: string
  datasourceId: string
  tableExtract: string
  dsTableList?: IDsTable[]
  dsList?: { id: string; name: string }[]
  fieldList?: ITableField[]
  targetFieldTypeList?: string[]
  incrementCheckbox?: string
  incrementField?: string
  esQuery?: string
}

export interface ITableField {
  id: string
  fieldSource: string
  fieldSourceType?: string
  fieldSourceStandardType?: string
  fieldName: string
  fieldType: string
  fieldElementType?: string
  fieldMappingMessage?: string
  remarks: string
  fieldSize: number
  fieldPrecision: number
  fieldPk: boolean
  fieldIndex: boolean
}

export interface ITargetProperty {
  /**
   * 容错率
   */
  faultToleranceRate: number
  /**
   * 启用分区on
   */
  partitionEnable: string
  /**
   * 分区类型
   * DateRange 日期
   * NumberRange 数值
   * List 列
   */
  partitionType: string
  /**
   * 启用动态分区on
   */
  dynamicPartitionEnable: string
  /**
   * 动态分区结束偏移量
   */
  dynamicPartitionEnd: number
  /**
   * 动态分区间隔单位
   * HOUR WEEK DAY MONTH YEAR
   */
  dynamicPartitionTimeUnit: string
  /**
   * 手动分区列值
   * 多个以','隔开
   */
  manualPartitionColumnValue: string
  /**
   * 手动分区数值区间开始值
   */
  manualPartitionStart: number
  /**
   * 手动分区数值区间结束值
   */
  manualPartitionEnd: number
  /**
   * 手动分区数值区间间隔
   */
  manualPartitionInterval: number
  /**
   * 手动分区日期区间
   * 2023-09-08 - 2023-09-15
   */
  manualPartitionTimeRange: string
  /**
   * 手动分区日期区间间隔单位
   */
  manualPartitionTimeUnit: string
  /**
   * 分区字段
   */
  partitionColumn: string

  [key: string]: any
}

export interface ITarget {
  datasource?: { id: string; name: string }
  createTable?: string
  type: string
  fieldList: ITableField[]
  tableName: string
  datasourceId: string
  targetProperty: string
  fieldMappingVersion?: number
  dsList?: {
    id: string
    name: string
    unsupportedKeyFieldTypes?: string[]
    fieldIndexType?: string
    supportedIndexFieldTypes?: string[]
  }[]
  multipleSelection?: ITableField[]
  property: ITargetProperty
  incrementSync: string
  incrementField: string
  incrementFieldType: string
  remarks: string
  faultToleranceRate: number
  incrementOffset: number
  incrementOffsetUnit: string
}

export type SinkKeyPolicy = 'OPTIONAL' | 'REQUIRED' | 'PROVIDER_GENERATED'

export class ITaskInfoRes {
  maintenanceState?: string
  maintenanceNeedsAttention?: boolean
  logStatus?: string
  editable?: boolean
  editableCheckFailed?: boolean
  sourceDatasourceMissing?: boolean
  targetDatasourceMissing?: boolean
  targetTableCheckStatus?: 'AVAILABLE' | 'DATASOURCE_MISSING' | 'TABLE_MISSING' | 'CHECK_FAILED'
  targetTableCheckError?: string

  id: string

  name: string

  schedulerType: string

  schedulerConf: string

  schedulerOption: ISchedulerOption

  taskKey: string

  desc: string

  executorTimeout: number

  executorFailRetryCount: number

  source: ISource

  target: ITarget

  status: string
  startTime: string
  stopTime: string

  constructor(
    id: string,
    name: string,
    schedulerType: string,
    schedulerConf: string,
    taskKey: string,
    desc: string,
    executorTimeout: number,
    executorFailRetryCount: number,
    source: ISource,
    target: ITarget,
    status: string,
    startTime: string,
    stopTime: string,
    schedulerOption: ISchedulerOption
  ) {
    this.id = id
    this.name = name
    this.schedulerType = schedulerType
    this.schedulerConf = schedulerConf
    this.taskKey = taskKey
    this.desc = desc
    this.executorTimeout = executorTimeout
    this.executorFailRetryCount = executorFailRetryCount
    this.source = source
    this.target = target
    this.status = status
    this.startTime = startTime
    this.stopTime = stopTime
    this.schedulerOption = schedulerOption
  }
}

export interface ITaskInfoUpdateReq {
  [key: string]: any
}

export interface IDsTable {
  datasourceId: string
  name: string
  remark: string
  enableCheck: string
  datasetPath: string
}

export const getDatasourceListByTypeApi = (type: string, datasourceRole: number) => {
  return request.get({ url: `/sync/datasource/list/${type}`, params: { datasourceRole } })
}
export const getTaskInfoListApi = (current: number, size: number, data) => {
  return request.post({ url: `/sync/task/pager/${current}/${size}`, data: data })
}

export const executeOneApi = (id: string) => {
  return request.get({ url: `/sync/task/execute/${id}` })
}

export const startTaskApi = (id: string) => {
  return request.get({ url: `/sync/task/start/${id}` })
}

export const stopTaskApi = (id: string) => {
  return request.get({ url: `/sync/task/stop/${id}` })
}

export const addApi = (data: ITaskInfoInsertReq) => {
  return request.post({ url: `/sync/task/add`, data: data })
}

export const removeApi = (taskId: string) => {
  return request.post({ url: `/sync/task/remove/${taskId}` })
}

export const batchRemoveApi = (taskIds: string[]) => {
  return request.post({ url: `/sync/task/batch/del`, data: taskIds })
}

export const modifyApi = (data: ITaskInfoUpdateReq) => {
  return request.post({ url: `/sync/task/update`, data: data })
}

export const findTaskInfoByIdApi = (taskId: string) => {
  return request.get({ url: `/sync/task/get/${taskId}` })
}

export const getSinkKeyPolicyApi = (target: ITarget) => {
  return request.post({ url: '/sync/task/target/keyPolicy', data: target })
}

export const getDatasourceTableListApi = (dsId: string) => {
  return request.get({ url: `/sync/datasource/table/list/${dsId}` })
}

/**
 * 维护只发送最终字段差异，不使用原任务编辑接口的完整配置保存流程
 */
export interface IFieldAdjustmentChange {
  action: 'ADD' | 'UPDATE' | 'DELETE'
  originalTargetName?: string
  after?: ITableField
}

/**
 * 当前连接的目标能力由插件返回，类型转换限制与源到目标类型推荐分别处理
 */
export interface IMaintenanceStructure {
  version?: string
  blockedReason?: string
  fullRefresh: boolean
  operations: string[]
  columns: {
    field: ITableField
    nativeType?: string
    nullable: boolean
    protectedReason?: string
  }[]
  indexes: Record<string, string>
  indexType?: string
  indexFieldTypes: string[]
  rules?: {
    item: string
    supported?: string
    restricted?: string
    source?: 'DATABASE' | 'PLUGIN' | 'TASK'
    officialText?: string
    documentationUrl?: string
    applicableVersion?: string
  }[]
  fieldNameMaxLength?: number
  fieldCommentMaxLength?: number
  sizeLimits?: Record<string, number>
  precisionLimits?: Record<string, number>
  typeChanges?: Record<string, string[]>
  typeChangeRules?: Record<
    string,
    Record<string, { level: string; reason: string; minimumSize?: number }>
  >
}

/**
 * 服务端返回字段与属性标识，页面不从本地化错误文字中猜测定位信息
 */
export interface IMaintenanceFieldIssue {
  originalTargetName?: string
  fieldName?: string
  property: string
  reason: string
}

/**
 * 预检查结果包含可定位的问题、风险和有序步骤，存在 blockers 时不允许应用任何一步
 */
export interface IMaintenancePlan {
  steps: {
    id: string
    action: string
    before?: ITableField
    after?: ITableField
    state: string
    error?: string
  }[]
  warnings: string[]
  blockers: string[]
  fieldIssues?: IMaintenanceFieldIssue[]
  requiresReinitialization: boolean
  riskConfirmationRequired?: boolean
}

/**
 * 上下文、轻量进度和按需详情共用响应类型；当前保存字段与本次操作快照不能互相替代
 */
export interface ITaskMaintenance {
  taskId: string
  taskName?: string
  sourceName?: string
  targetName?: string
  targetTable?: string
  sourceType?: string
  targetType?: string
  sourceMode?: 'TABLE' | 'QUERY'
  sourceTable?: string
  sourceQuery?: string
  operationId?: string
  state: string
  /** 服务端是否仍在推进调整，旧接口未提供时保留原展示行为 */
  processing?: boolean
  phase?: 'SCHEMA' | 'CONFIGURATION' | 'RECOVERY' | 'INITIALIZATION' | 'DONE'
  blockedReason?: string
  blockedCode?:
    | 'UNSUPPORTED_TARGET'
    | 'TARGET_READ_FAILED'
    | 'SCHEMA_MISMATCH'
    | 'PRECISION_UNSPECIFIED'
    | 'SOURCE_READ_FAILED'
    | 'TASK_RUNNING'
  error?: string
  logId?: string
  configFingerprint: string
  schemaFingerprint: string
  sourceFingerprint: string
  planFingerprint?: string
  fields: ITableField[]
  beforeFields?: ITableField[]
  afterFields?: ITableField[]
  structureVerified?: boolean
  initializationStatus?: string
  initializationError?: string
  sourceFields: ITableField[]
  fieldTypes: string[]
  mappingNotes?: Record<string, string>
  structure?: IMaintenanceStructure
  plan?: IMaintenancePlan
  progress?: IMaintenancePlan['steps']
  executionRecovery?: IExecutionRecovery
}

export interface IExecutionRecovery {
  logId: string
  status: string
  kind?: 'REPLACEMENT' | 'EXECUTION'
  reason?: string
  configFingerprint: string
  lastChecked?: number
  checks: number
  manualRequired: boolean
  canResolve: boolean
}

export interface IExecutionRecoveryRequest {
  taskId: string
  logId: string
  configFingerprint: string
  result?: 'SUCCESS' | 'FAIL'
  note?: string
  confirmed?: boolean
}

/**
 * 替表核对不重发语句，人工核实必须提交指定运行和依据，错误留在当前恢复弹窗
 */
export const recheckExecutionApi = (data: IExecutionRecoveryRequest) =>
  request.post<Promise<{ data: ITaskMaintenance }>>({
    url: '/sync/task/maintenance/execution/recheck',
    handleErrorLocally: true,
    data
  })

export const resolveExecutionApi = (data: IExecutionRecoveryRequest) =>
  request.post<Promise<{ data: ITaskMaintenance }>>({
    url: '/sync/task/maintenance/execution/resolve',
    handleErrorLocally: true,
    data
  })

/**
 * 操作编号配合任务编号保证幂等，指纹确保正式应用前配置、结构、来源和计划仍与确认时一致
 */
export interface ITaskMaintenanceRequest {
  taskId: string
  operationId: string
  configFingerprint?: string
  schemaFingerprint?: string
  sourceFingerprint?: string
  planFingerprint?: string
  changes: IFieldAdjustmentChange[]
  confirmed?: boolean
  deleteConfirmed?: boolean
  exclusiveTargetConfirmed?: boolean
  riskConfirmed?: boolean
  alignTargetPrecision?: boolean
}

/**
 * 读取保存配置和当前能力，失败原因在弹窗本地呈现，避免统一提示遮蔽恢复入口
 */
export const getTaskMaintenanceApi = (id: string, fullRefresh = false) =>
  request.get<Promise<{ data: ITaskMaintenance }>>({
    handleErrorLocally: true,
    url: `/sync/task/maintenance/${id}`,
    ...(fullRefresh ? { params: { fullRefresh: true } } : {})
  })

/**
 * 整批预检查只生成计划和错误定位，不修改目标结构或暂停原任务
 */
export const previewTaskMaintenanceApi = (data: ITaskMaintenanceRequest) =>
  request.post<Promise<{ data: ITaskMaintenance }>>({
    handleErrorLocally: true,
    url: '/sync/task/maintenance/preview',
    data
  })

/**
 * 提交已确认计划，服务端重新检查后启动后台调整
 */
export const applyTaskMaintenanceApi = (data: ITaskMaintenanceRequest) =>
  request.post<Promise<{ data: ITaskMaintenance }>>({
    handleErrorLocally: true,
    url: '/sync/task/maintenance/apply',
    data
  })

/**
 * 继续已有操作，已完成步骤由服务端跳过，前端不重新计算或提交 DDL
 */
export const resumeTaskMaintenanceApi = (data: ITaskMaintenanceRequest) =>
  request.post<Promise<{ data: ITaskMaintenance }>>({
    handleErrorLocally: true,
    url: '/sync/task/maintenance/resume',
    data
  })

/**
 * 只核对已有数据库作业与结构，可以补记结果，不重新提交结构变更
 */
export const recheckTaskMaintenanceApi = (data: ITaskMaintenanceRequest) =>
  request.post<Promise<{ data: ITaskMaintenance }>>({
    handleErrorLocally: true,
    url: '/sync/task/maintenance/recheck',
    data
  })

/**
 * 结束剩余步骤前保留已完成修改并恢复一致配置，不等于回滚
 */
export const finishTaskMaintenanceApi = (data: ITaskMaintenanceRequest) =>
  request.post<Promise<{ data: ITaskMaintenance }>>({
    handleErrorLocally: true,
    url: '/sync/task/maintenance/finish',
    data
  })

/**
 * 使用当前生效配置执行一次全量，字段草稿不会被隐式应用
 */
export const fullRefreshTaskApi = (data: ITaskMaintenanceRequest) =>
  request.post<Promise<{ data: ITaskMaintenance }>>({
    handleErrorLocally: true,
    url: '/sync/task/maintenance/full',
    data
  })

/**
 * 轮询轻量状态，不反复加载完整字段和大体积恢复快照
 */
export const getTaskMaintenanceStatusApi = (id: string) =>
  request.get<Promise<{ data: ITaskMaintenance }>>({
    handleErrorLocally: true,
    url: `/sync/task/maintenance/status/${id}`
  })

/**
 * 用户查看变更或恢复草稿时按需获取完整操作快照
 */
export const getTaskMaintenanceDetailsApi = (id: string) =>
  request.get<Promise<{ data: ITaskMaintenance }>>({
    handleErrorLocally: true,
    url: `/sync/task/maintenance/details/${id}`
  })
