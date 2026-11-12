<script setup lang="ts">
import { isRequestError } from '@/config/axios/error'
import { computed, onBeforeUnmount, reactive, ref, watchEffect } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import {
  applyTaskMaintenanceApi,
  getTaskMaintenanceApi,
  getTaskMaintenanceStatusApi,
  getTaskMaintenanceDetailsApi,
  readTaskMaintenanceSourceApi,
  previewTaskMaintenanceApi,
  resumeTaskMaintenanceApi,
  recheckTaskMaintenanceApi,
  finishTaskMaintenanceApi
} from '@/api/sync/syncTask'
import type {
  IFieldAdjustmentChange,
  IMaintenanceFieldIssue,
  IMaintenancePlan,
  ITableField,
  ITaskInfoRes,
  ITaskMaintenance,
  ITaskMaintenanceRequest
} from '@/api/sync/syncTask'
import { deepCopy } from '@/utils/utils'
import generateID from '@/utils/generateID'
import { useI18n } from '@/hooks/web/useI18n'
import TargetFields from './TargetFields.vue'
import TargetFieldForm from './TargetFieldForm.vue'
import FullRefreshDialog from './FullRefreshDialog.vue'
import ExecutionRecovery from './ExecutionRecovery.vue'
import FieldChangeDetails from './FieldChangeDetails.vue'
import FieldExecutionResults from './FieldExecutionResults.vue'
import { maintenanceFieldType, normalizeFieldType } from './fieldType'
import { fieldSignature as signature, getFieldChanges } from './fieldChanges'

const { t } = useI18n()
const emit = defineEmits<{
  (event: 'changed'): void
  (event: 'openTaskLog', taskId: string): void
}>()
const visible = ref(false)
const activeTab = ref('current')
const rulesVisible = ref(false)
const queryVisible = ref(false)
const queryEditorVisible = ref(false)
const originalSourceQuery = ref('')
const sourceQuery = ref('')
const queryInput = ref('')
const originalSource =
  ref<
    Pick<
      ITaskMaintenance,
      | 'sourceQuery'
      | 'sourceFields'
      | 'fieldTypes'
      | 'sourceFingerprint'
      | 'blockedReason'
      | 'blockedCode'
    >
  >()
const detailsVisible = ref('')
const changesVisible = ref(false)
const uncertainOperation = ref('')
const pendingSubmissions = new Map<string, string>()
const loading = ref(false)
const error = ref('')
const showError = (cause: unknown) => {
  error.value =
    isRequestError(cause) && cause.response?.status === 404
      ? t('sync_maintenance.backend_unavailable')
      : typeof cause === 'string'
      ? cause
      : cause instanceof Error
      ? cause.message
      : t('sync_maintenance.read_failed')
}
const taskName = ref('')
const context = ref<ITaskMaintenance>()
const preview = ref<ITaskMaintenance>()
const validationIssues = ref<IMaintenanceFieldIssue[]>([])
const validationBlockers = ref<string[]>([])
const confirmation = ref(false)
const precisionAlignment = ref(false)
const exclusive = ref(false)
const deleteConfirmed = ref(false)
const riskConfirmed = ref(false)
const initializationConfirmed = ref(false)
const drawer = ref<InstanceType<typeof TargetFieldForm>>()
const fullDialog = ref<InstanceType<typeof FullRefreshDialog>>()
const original = ref<ITableField[]>([])
const removed = ref<ITableField[]>([])
const form = ref<ITaskInfoRes>()
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const currentPagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const protectedExpanded = ref<string[]>([])
let pollTimer: ReturnType<typeof setTimeout> | undefined
let pollFailures = 0
let trackingDatabase = false
let viewVersion = 0
let pollVersion = 0
/**
 * 同一任务重新打开也属于新会话，旧响应不能覆盖新草稿或重启已关闭窗口的轮询
 */
const isCurrentView = (version: number) => visible.value && version === viewVersion
const stopPolling = () => {
  clearTimeout(pollTimer)
  pollVersion++
}
/**
 * 始终以打开时的已保存字段为基线计算最终差异，新增后删除等相互抵消的编辑不会提交
 */
const changes = computed<IFieldAdjustmentChange[]>(() => {
  if (!form.value) return []
  const result: IFieldAdjustmentChange[] = []
  original.value.forEach(before => {
    const after = form.value?.target.fieldList.find(field => field.id === before.id)
    if (!after) result.push({ action: 'DELETE', originalTargetName: before.fieldName })
    else if (signature(before) !== signature(after))
      result.push({
        action: 'UPDATE',
        originalTargetName: before.fieldName,
        after: deepCopy(after)
      })
  })
  form.value.target.fieldList.forEach(after => {
    if (!original.value.some(before => before.id === after.id))
      result.push({ action: 'ADD', after: deepCopy(after) })
  })
  return result
})
const queryChanged = computed(() => sourceQuery.value !== originalSourceQuery.value)
const hasChanges = computed(() => changes.value.length > 0 || queryChanged.value)
const previewChanges = computed(() =>
  changes.value.map(change => ({
    before: original.value.find(field => field.fieldName === change.originalTargetName),
    after: change.after
  }))
)
const hasDeletedFields = computed(() => changes.value.some(change => change.action === 'DELETE'))
const affectsDownstream = computed(() =>
  changes.value.some(
    change =>
      change.action === 'DELETE' ||
      (change.action === 'UPDATE' && change.originalTargetName !== change.after.fieldName)
  )
)
const pendingConfirmations = computed(
  () =>
    Number(!exclusive.value) +
    Number(!!preview.value?.plan.requiresReinitialization && !initializationConfirmed.value) +
    Number(!!preview.value?.plan.riskConfirmationRequired && !riskConfirmed.value) +
    Number(hasDeletedFields.value && !deleteConfirmed.value)
)
const confirmationHint = computed(() =>
  preview.value?.plan.blockers.length
    ? t('sync_maintenance.confirm_blocked', [preview.value.plan.blockers.length])
    : pendingConfirmations.value
    ? t('sync_maintenance.confirm_pending', [pendingConfirmations.value])
    : t('sync_maintenance.confirm_ready')
)
const submitted = computed(() => ['APPLYING', 'RECOVERY_REQUIRED'].includes(context.value?.state))
const savingConfiguration = computed(() => context.value?.phase === 'CONFIGURATION')
/**
 * 按本次操作快照生成变更对比，利用改名步骤关联前后字段，区别当前配置与原始修改意图
 */
const operationChanges = computed(() =>
  getFieldChanges(
    context.value?.beforeFields || [],
    context.value?.afterFields || [],
    context.value?.plan?.steps || []
  )
)
const operationFailed = computed(
  () =>
    !!context.value?.error ||
    context.value?.plan?.steps.some(step => ['FAILED', 'UNKNOWN'].includes(step.state))
)
/**
 * 结果未知只允许查询，不将连接中断当作可重新提交的明确失败
 */
const unknownResult = computed(() =>
  context.value?.plan?.steps.some(step => step.state === 'UNKNOWN')
)
const resultTitle = computed(() => {
  if (context.value?.state === 'DONE') return t('sync_maintenance.success_title')
  if (context.value?.state === 'RECOVERY_REQUIRED')
    return t('sync_maintenance.recovery_required_title')
  if (context.value?.state === 'REINIT_REQUIRED') {
    if (context.value.executionRecovery) return t('sync_maintenance.init_unknown')
    const status = context.value.initializationStatus
    return t(
      ['FAIL', 'TERMINATION'].includes(status)
        ? 'sync_maintenance.initialization_failed_title'
        : status === 'RUNNING'
        ? 'sync_maintenance.initialization_running_title'
        : status === 'CONNECTION_LOST'
        ? 'sync_maintenance.initialization_unknown_title'
        : 'sync_maintenance.initialization_pending_title'
    )
  }
  const steps = context.value?.plan?.steps || []
  if (steps.some(step => step.state === 'UNKNOWN'))
    return t('sync_maintenance.unknown_result_title')
  if (operationFailed.value) {
    if (steps.every(step => step.state === 'DONE'))
      return t('sync_maintenance.config_save_failed_title')
    return t(
      steps.some(step => step.state === 'DONE')
        ? 'sync_maintenance.partial_apply_title'
        : 'sync_maintenance.schema_failed_title'
    )
  }
  if (savingConfiguration.value) return t('sync_maintenance.configuration_pending_title')
  if (context.value?.processing === false) return t('sync_maintenance.execution_pending_title')
  return t('sync_maintenance.applying')
})
/**
 * 按真实失败阶段说明同步限制，不用同一段恢复文案覆盖结构、配置和初始化结果
 */
const resultDescription = computed(() => {
  if (context.value?.state === 'REINIT_REQUIRED') {
    const status = context.value.initializationStatus
    return t(
      status === 'RUNNING'
        ? 'sync_maintenance.collapse_help'
        : ['FAIL', 'TERMINATION'].includes(status)
        ? 'sync_maintenance.initialization_failed_help'
        : status === 'CONNECTION_LOST'
        ? 'sync_maintenance.initialization_unknown_help'
        : 'sync_maintenance.initialization_pending_help'
    )
  }
  if (unknownResult.value) return t('sync_maintenance.response_unknown_help')
  if (context.value?.state === 'RECOVERY_REQUIRED')
    return t('sync_maintenance.recovery_required_help')
  if (savingConfiguration.value)
    return t(
      context.value.plan.requiresReinitialization
        ? 'sync_maintenance.configuration_init_help'
        : 'sync_maintenance.configuration_pending_help'
    )
  return t(
    processing.value
      ? 'sync_maintenance.collapse_help'
      : 'sync_maintenance.structure_incomplete_help'
  )
})
const databaseVersion = computed(
  () => context.value?.structure?.version?.match(/\d+\.\d+(?:\.\d+)?/)?.[0]
)
const failureSteps = computed(
  () => context.value?.plan?.steps.filter(step => step.state !== 'DONE') || []
)
const operationError = computed(
  () => context.value?.error || context.value?.initializationError || ''
)
const summarizeError = (message: string) => {
  const firstLine = message.split('\n')[0]
  return firstLine.length > 200 ? firstLine.slice(0, 200) + '…' : firstLine
}
const failureSummary = computed(() => summarizeError(operationError.value))
const appliedTitle = computed(() => {
  if (context.value?.plan?.steps.some(step => step.state === 'SKIPPED'))
    return t('sync_maintenance.ended_summary')
  if (!context.value?.structureVerified || context.value.blockedReason)
    return t('sync_maintenance.last_completed')
  if (context.value.beforeFields && context.value.afterFields)
    return t('sync_maintenance.applied_count', [operationChanges.value.length])
  return t('sync_maintenance.applied_brief')
})
const appliedDescription = computed(() => {
  const operation = context.value
  if (!operation?.plan) return t('sync_maintenance.sync_result_logs')
  if (!operation.plan.requiresReinitialization) return t('sync_maintenance.adjustment_without_sync')
  return t(
    operation.initializationStatus === 'SUCCESS'
      ? 'sync_maintenance.initialization_completed'
      : 'sync_maintenance.initialization_not_verified'
  )
})
const configurationApplied = computed(() =>
  ['DONE', 'REINIT_REQUIRED'].includes(context.value?.state)
)
const initializationText = computed(() => {
  if (!context.value?.plan?.requiresReinitialization) return t('sync_maintenance.init_not_required')
  const status = context.value.initializationStatus
  if (status === 'SUCCESS') return t('sync_maintenance.init_success')
  if (status === 'RUNNING') return t('sync_maintenance.init_running')
  if (['FAIL', 'TERMINATION'].includes(status)) return t('sync_maintenance.init_failed')
  if (status) return t('sync_maintenance.init_unknown')
  return t('sync_maintenance.init_pending')
})
const initializationRetryable = computed(
  () =>
    context.value?.state === 'REINIT_REQUIRED' &&
    !hasChanges.value &&
    !context.value.blockedReason &&
    !['RUNNING', 'CONNECTION_LOST'].includes(context.value.initializationStatus)
)
/**
 * 编辑门禁只反映服务端维护状态及读取结果，不能把连接中断当作已结束并允许覆盖配置
 */
const locked = computed(
  () =>
    loading.value ||
    queryEditorVisible.value ||
    !!uncertainOperation.value ||
    submitted.value ||
    ['RUNNING', 'CONNECTION_LOST'].includes(context.value?.initializationStatus) ||
    (!!context.value?.blockedReason &&
      !(precisionAlignment.value && context.value.blockedCode === 'PRECISION_UNSPECIFIED'))
)
const queryEditable = computed(
  () =>
    context.value?.sourceMode === 'QUERY' &&
    context.value.sourceQuerySupported &&
    context.value.structureVerified &&
    !precisionAlignment.value &&
    !submitted.value &&
    !uncertainOperation.value &&
    !context.value.executionRecovery &&
    !['RUNNING', 'CONNECTION_LOST'].includes(context.value.initializationStatus) &&
    (!context.value.blockedReason || context.value.blockedCode === 'SOURCE_READ_FAILED')
)
const processing = computed(
  () =>
    (context.value?.state === 'APPLYING' &&
      context.value.processing !== false &&
      !operationFailed.value &&
      !savingConfiguration.value) ||
    (context.value?.state === 'REINIT_REQUIRED' &&
      !context.value.executionRecovery &&
      context.value.initializationStatus === 'RUNNING')
)
const resumeLabel = computed(() => {
  if (context.value?.state === 'RECOVERY_REQUIRED') return t('sync_maintenance.resume_original')
  if (!savingConfiguration.value) return t('sync_maintenance.resume')
  if (context.value.plan.requiresReinitialization) return t('sync_maintenance.save_and_initialize')
  return t(
    operationFailed.value
      ? 'sync_maintenance.retry_configuration'
      : 'sync_maintenance.save_configuration'
  )
})
const sourceType = (row: ITableField) => {
  if (!row.fieldSource) return t('sync_maintenance.generated_field')
  const source = context.value?.sourceFields?.find(field => field.fieldName === row.fieldSource)
  const raw = source ? source.fieldSourceType : row.fieldSourceType
  const standard = source ? source.fieldSourceStandardType : row.fieldSourceStandardType
  const type =
    raw ||
    (standard
      ? t('sync_maintenance.standard_type', [standard])
      : t('sync_maintenance.type_unavailable'))
  return source ? type : t('sync_maintenance.saved_type', [type])
}
const missingSource = (row: ITableField) =>
  !!context.value?.sourceFingerprint &&
  !!row.fieldSource &&
  !form.value?.source.fieldList.some(field => field.fieldName === row.fieldSource)
const missingFields = computed(() => (form.value?.target.fieldList || []).filter(missingSource))
const targetType = (row: ITableField) => {
  const structure = context.value?.structure
  if (normalizeFieldType(row.fieldType) === 'DECIMAL') {
    const type = maintenanceFieldType(
      row,
      original.value.find(field => field.id === row.id),
      structure
    )
    return row.fieldSize > 0
      ? `${type}(${row.fieldSize}, ${row.fieldPrecision})`
      : `${type}（${t('sync_maintenance.precision_unspecified')}）`
  }
  const size = structure?.sizeLimits?.[row.fieldType] ? row.fieldSize : 0
  const precision = structure?.precisionLimits?.[row.fieldType] ? row.fieldPrecision : 0
  return (
    maintenanceFieldType(
      row,
      original.value.find(field => field.id === row.id),
      structure
    ) + (size ? `(${size}${precision ? `, ${precision}` : ''})` : precision ? `(${precision})` : '')
  )
}
const actualTargetType = (row: ITableField) => {
  const column = context.value?.structure?.columns.find(
    item => item.field.fieldName.toLowerCase() === row.fieldName.toLowerCase()
  )
  return column
    ? column.nativeType?.toUpperCase() || targetType(column.field)
    : t('sync_maintenance.actual_column_missing')
}
const showActualDefinition = computed(
  () =>
    activeTab.value === 'current' && !!context.value?.structure && !context.value.structureVerified
)
const pendingDelete = (row: ITableField) => removed.value.some(field => field.id === row.id)
const protectedReason = (row: ITableField) =>
  context.value?.structure?.columns.find(
    column =>
      column.field.fieldName ===
      (original.value.find(field => field.id === row.id)?.fieldName || row.fieldName)
  )?.protectedReason
const editable = (row: ITableField) =>
  !locked.value && !precisionAlignment.value && !protectedReason(row)
const deletable = (row: ITableField) =>
  editable(row) && context.value.structure?.operations.includes('DELETE')

const isProtected = (row: ITableField) =>
  !!protectedReason(row) || row.fieldPk || row.fieldName.startsWith('_DE_SYNC_')
const ordinaryCurrent = computed(() => original.value.filter(row => !isProtected(row)))
const ordinaryDraft = computed(() =>
  [...(form.value?.target.fieldList || []), ...removed.value].filter(row => !isProtected(row))
)
const protectedFields = computed(() =>
  (precisionAlignment.value ? form.value?.target.fieldList || [] : original.value).filter(
    isProtected
  )
)
const currentRows = computed(() =>
  ordinaryCurrent.value.slice(
    (currentPagination.currentPage - 1) * currentPagination.pageSize,
    currentPagination.currentPage * currentPagination.pageSize
  )
)
const rows = computed(() => {
  const all = ordinaryDraft.value
  return all.slice(
    (pagination.currentPage - 1) * pagination.pageSize,
    pagination.currentPage * pagination.pageSize
  )
})
const sizeChange = (size: number) => {
  const config = activeTab.value === 'draft' ? pagination : currentPagination
  config.currentPage = Math.min(config.currentPage, Math.max(1, Math.ceil(config.total / size)))
  config.pageSize = size
}
watchEffect(() => {
  pagination.total = ordinaryDraft.value.length
  currentPagination.total = ordinaryCurrent.value.length
})
const columns = computed(() =>
  [
    ['fieldSource', 'source_field'],
    ['fieldName', 'name'],
    ['fieldType', 'field_type'],
    ['fieldSize', 'field_length'],
    ['fieldPrecision', 'field_precision'],
    ['fieldPk', 'field_key'],
    ['fieldIndex', 'field_index'],
    ['remarks', 'field_comment']
  ].map(([model, key]) => ({
    model,
    label: t(`sync_task.${key}`),
    type: model === 'fieldPk' || model === 'fieldIndex' ? 'checkbox' : 'input'
  }))
)
const optionDisabled = (row: ITableField, option: 'fieldPk' | 'fieldIndex') =>
  option === 'fieldPk' ||
  !editable(row) ||
  pendingDelete(row) ||
  !context.value?.structure?.indexFieldTypes.some(type => type === row.fieldType)
const optionHint = (row: ITableField, option: 'fieldPk' | 'fieldIndex') =>
  protectedReason(row) ||
  (option === 'fieldPk'
    ? t('sync_maintenance.special')
    : !locked.value && optionDisabled(row, option)
    ? t('sync_task.index_type_unsupported')
    : '')
const rowStatus = (row: ITableField) => {
  if (pendingDelete(row)) return t('sync_maintenance.deleted')
  if (missingSource(row)) return t('sync_maintenance.source_missing_status')
  const before = original.value.find(field => field.id === row.id)
  if (!before) return t('sync_maintenance.added')
  if (signature(before) !== signature(row)) return t('sync_maintenance.modified')
  return ''
}
/**
 * 改名后的草稿仍用原字段定位，新增字段才按新名称定位
 */
const issuesFor = (row: ITableField) => {
  const before = original.value.find(field => field.id === row.id)
  return validationIssues.value.filter(issue =>
    issue.originalTargetName
      ? issue.originalTargetName === (before?.fieldName || row.fieldName)
      : issue.fieldName === row.fieldName
  )
}
const fieldErrors = (row: ITableField) => [
  ...(missingSource(row) && !pendingDelete(row)
    ? [t('sync_maintenance.source_missing_field', [row.fieldSource])]
    : []),
  ...(activeTab.value === 'draft' ? issuesFor(row).map(issue => issue.reason) : [])
]
/**
 * 修改后的行需要重新检查，清除该行的旧结果而保留其他行的问题
 */
const fieldSaved = (row: ITableField) => {
  const previous = issuesFor(row)
  validationIssues.value = validationIssues.value.filter(issue => !previous.includes(issue))
  validationBlockers.value = []
  preview.value = undefined
}
/**
 * 大量字段分页时先跳转问题所在页，再复用字段抽屉展示具体属性错误
 */
const locateIssue = () => {
  const index = ordinaryDraft.value.findIndex(row => fieldErrors(row).length)
  if (index < 0) {
    if (missingFields.value.length) protectedExpanded.value = ['protected']
    return
  }
  pagination.currentPage = Math.floor(index / pagination.pageSize) + 1
  const row = ordinaryDraft.value[index]
  if (!pendingDelete(row) && !missingSource(row)) edit(row)
}
/**
 * 读取保存配置并复制成独立草稿，概览省略字段编辑专用元数据
 * 复用字段表单时只提供所需能力，避免携带或伪造数据源连接配置
 */
const load = async (id: string, version: number, overview = false) => {
  const polling = pollVersion
  const response = await getTaskMaintenanceApi(id, overview)
  if (!isCurrentView(version) || polling !== pollVersion) return false
  // 读取受阻时只更新门禁，保留同一套编辑基线和草稿
  if (activeTab.value === 'draft' && response.data.blockedReason) {
    context.value.blockedReason = response.data.blockedReason
    context.value.blockedCode = response.data.blockedCode
    return false
  }
  context.value = response.data
  originalSource.value = {
    sourceQuery: response.data.sourceQuery,
    sourceFields: response.data.sourceFields,
    fieldTypes: response.data.fieldTypes,
    sourceFingerprint: response.data.sourceFingerprint,
    blockedReason: response.data.blockedReason,
    blockedCode: response.data.blockedCode
  }
  originalSourceQuery.value = response.data.sourceQuery || ''
  sourceQuery.value = originalSourceQuery.value
  queryInput.value = sourceQuery.value
  queryEditorVisible.value = false
  original.value = (response.data.fields || []).map(field => ({ ...field, id: generateID() }))
  protectedExpanded.value = ordinaryCurrent.value.length ? [] : ['protected']
  removed.value = []
  pagination.currentPage = 1
  currentPagination.currentPage = 1
  form.value = {
    source: {
      fieldList: (response.data.sourceFields || []).map(field => ({ ...field, id: generateID() })),
      targetFieldTypeList: response.data.fieldTypes
    },
    target: {
      type: response.data.targetType,
      fieldList: deepCopy(original.value),
      datasourceId: 'maintenance',
      dsList: [
        {
          id: 'maintenance',
          name: '',
          unsupportedKeyFieldTypes: [],
          fieldIndexType: response.data.structure?.indexType,
          supportedIndexFieldTypes: response.data.structure?.indexFieldTypes || []
        }
      ]
    }
  } as ITaskInfoRes
  return true
}
/**
 * 恢复后重读完整上下文，清除旧的同步限制提示，读取失败仍留在当前任务展示
 */
const recoveryUpdated = async () => {
  if (!visible.value || !context.value) return
  const version = viewVersion
  const id = context.value.taskId
  stopPolling()
  loading.value = true
  try {
    await load(id, version)
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 轻量轮询按步骤编号合并结果，保留详情接口已经加载的字段快照，避免用摘要覆盖完整计划
 */
const mergeStatus = (state: ITaskMaintenance) => {
  if (!context.value) return
  context.value = {
    ...context.value,
    state: state.state,
    phase: state.phase,
    processing: state.processing,
    operationId: state.operationId,
    plan:
      state.plan ||
      (state.progress && context.value.plan
        ? {
            ...context.value.plan,
            steps: context.value.plan.steps.map(step => {
              const progress = state.progress.find(value => value.id === step.id)
              return progress ? { ...step, state: progress.state, error: progress.error } : step
            })
          }
        : context.value.plan),
    error: state.error,
    logId: state.logId,
    beforeFields: state.beforeFields ?? context.value.beforeFields,
    afterFields: state.afterFields ?? context.value.afterFields,
    beforeSourceQuery: state.beforeSourceQuery ?? context.value.beforeSourceQuery,
    afterSourceQuery: state.afterSourceQuery ?? context.value.afterSourceQuery,
    initializationStatus: state.initializationStatus,
    initializationError: state.initializationError,
    executionRecovery: state.executionRecovery,
    blockedReason: state.blockedReason || context.value.blockedReason
  }
}
/**
 * 常规进度读取与结果未知时的只读核对共用轮询，关闭窗口仅停止页面查询
 */
const poll = async () => {
  stopPolling()
  if (!visible.value || !context.value) return
  const version = viewVersion
  const polling = pollVersion
  const isCurrentPoll = () => isCurrentView(version) && polling === pollVersion
  try {
    const previousState = context.value.state
    const previousBlockedReason = context.value.blockedReason
    const state = (await getTaskMaintenanceStatusApi(context.value.taskId)).data
    if (!isCurrentPoll()) return
    if (uncertainOperation.value) {
      if (state.operationId !== uncertainOperation.value) return
      uncertainOperation.value = ''
      pendingSubmissions.delete(state.taskId)
      confirmation.value = false
      discardDraft()
      activeTab.value = 'result'
      error.value = ''
    }
    mergeStatus(state)
    if (!trackingDatabase && !unknownResult.value) pollFailures = 0
    // 响应中断后自动进行有限次只读核对，数据库仍在执行时持续跟踪，不重新提交修改
    if (
      submitted.value &&
      !savingConfiguration.value &&
      (unknownResult.value || trackingDatabase)
    ) {
      trackingDatabase = true
      const checked = await recheckTaskMaintenanceApi({
        taskId: context.value.taskId,
        operationId: context.value.operationId,
        changes: []
      })
      if (!isCurrentPoll()) return
      mergeStatus(checked.data)
      pollFailures = unknownResult.value ? pollFailures + 1 : 0
      trackingDatabase = context.value.plan.steps.some(step =>
        ['WAITING', 'UNKNOWN'].includes(step.state)
      )
    }
    if (previousState !== state.state) emit('changed')
    if (
      state.state !== 'DONE' &&
      ((state.state === 'REINIT_REQUIRED' && previousState === 'APPLYING') ||
        (previousBlockedReason && !state.blockedReason))
    ) {
      if (!(await load(state.taskId, version)) || !isCurrentPoll()) return
    }
    if (state.state === 'DONE') {
      if (!(await load(state.taskId, version)) || !isCurrentPoll()) return
      activeTab.value = 'current'
      detailsVisible.value = ''
      if (!context.value?.blockedReason) return
    }
  } catch (cause) {
    if (!isCurrentPoll()) return
    showError(cause)
    // 查询失败只重试读取，达到上限保留具体原因与手动核对入口，不把超时判作执行失败
    if (++pollFailures < 3) pollTimer = setTimeout(poll, 2000)
    return
  }
  if (
    (context.value?.state === 'APPLYING' &&
      context.value.processing !== false &&
      !operationFailed.value) ||
    processing.value ||
    !!uncertainOperation.value ||
    (trackingDatabase && pollFailures < 3)
  )
    pollTimer = setTimeout(poll, 2000)
}
/**
 * 每次打开重新读取服务器基线，不继承其他任务的检查结果或轮询次数
 */
const open = async (id: string, name: string) => {
  stopPolling()
  const version = ++viewVersion
  pollFailures = 0
  trackingDatabase = false
  visible.value = true
  activeTab.value = 'current'
  rulesVisible.value = false
  queryVisible.value = false
  queryEditorVisible.value = false
  originalSourceQuery.value = ''
  sourceQuery.value = ''
  queryInput.value = ''
  originalSource.value = undefined
  detailsVisible.value = ''
  changesVisible.value = false
  uncertainOperation.value = pendingSubmissions.get(id) || ''
  confirmation.value = false
  precisionAlignment.value = false
  preview.value = undefined
  validationIssues.value = []
  validationBlockers.value = []
  taskName.value = name
  form.value = undefined
  context.value = undefined
  error.value = ''
  loading.value = true
  try {
    // 复用只读概览参数，仅跳过字段编辑元数据，不会发起全量同步
    if (!(await load(id, version, true)) || !isCurrentView(version)) return
    if (uncertainOperation.value && context.value.operationId === uncertainOperation.value) {
      uncertainOperation.value = ''
      pendingSubmissions.delete(id)
    }
    if (['APPLYING', 'RECOVERY_REQUIRED', 'REINIT_REQUIRED'].includes(context.value?.state))
      activeTab.value = 'result'
    if (context.value?.state === 'APPLYING' || context.value?.state === 'REINIT_REQUIRED')
      pollTimer = setTimeout(poll, 2000)
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 失败后允许重新读取完整上下文，仍须通过原编辑门禁才能进入草稿
 * 不沿用概览的字段和指纹作为编辑基线
 */
const startEditing = async () => {
  if (loading.value || !visible.value || !context.value) return
  precisionAlignment.value = false
  const version = viewVersion
  const taskId = context.value.taskId
  stopPolling()
  loading.value = true
  error.value = ''
  try {
    if (!(await load(taskId, version)) || !isCurrentView(version)) return
    validationIssues.value = []
    validationBlockers.value = []
    preview.value = undefined
    confirmation.value = false
    exclusive.value = false
    deleteConfirmed.value = false
    riskConfirmed.value = false
    initializationConfirmed.value = false
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
    return
  } finally {
    if (isCurrentView(version)) {
      loading.value = false
      if (context.value?.state === 'APPLYING' || context.value?.state === 'REINIT_REQUIRED')
        pollTimer = setTimeout(poll, 2000)
    }
  }
  if (!isCurrentView(version) || !context.value) return
  if (!locked.value) {
    activeTab.value = 'draft'
  } else if (
    submitted.value ||
    (context.value.state === 'REINIT_REQUIRED' &&
      ['RUNNING', 'CONNECTION_LOST'].includes(context.value.initializationStatus))
  ) {
    activeTab.value = 'result'
  }
}
/**
 * 查询编辑使用独立草稿，读取成功前不替换已核实的源字段和目标字段草稿
 */
const editSourceQuery = () => {
  if (loading.value || !queryEditable.value) return
  queryInput.value = sourceQuery.value
  queryEditorVisible.value = true
  queryVisible.value = false
  confirmation.value = false
}
const cancelSourceQuery = () => {
  queryInput.value = sourceQuery.value
  queryEditorVisible.value = false
}
const readSourceQuery = async () => {
  if (loading.value || !queryEditable.value || !queryInput.value.trim()) return
  const version = viewVersion
  stopPolling()
  loading.value = true
  error.value = ''
  try {
    const response = await readTaskMaintenanceSourceApi({
      taskId: context.value.taskId,
      operationId: generateID(),
      configFingerprint: context.value.configFingerprint,
      schemaFingerprint: context.value.schemaFingerprint,
      sourceQuery: queryInput.value,
      changes: []
    })
    if (!isCurrentView(version)) return
    if (response.data.blockedReason) {
      error.value = response.data.blockedReason
      return
    }
    context.value = response.data
    sourceQuery.value = response.data.sourceQuery
    form.value.source.fieldList = (response.data.sourceFields || []).map(field => ({
      ...field,
      id: generateID()
    }))
    form.value.source.targetFieldTypeList = response.data.fieldTypes
    queryEditorVisible.value = false
    activeTab.value = 'draft'
    validationIssues.value = []
    validationBlockers.value = []
    preview.value = undefined
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 历史缺失精度只生成配置校准草稿，预检查和正式提交仍由服务端核对实际结构与全部字段
 */
const startPrecisionAlignment = async () => {
  if (loading.value || !context.value || context.value.blockedCode !== 'PRECISION_UNSPECIFIED')
    return
  const version = viewVersion
  loading.value = true
  error.value = ''
  try {
    if (!(await load(context.value.taskId, version)) || !isCurrentView(version)) return
    if (context.value.blockedCode !== 'PRECISION_UNSPECIFIED' || !context.value.sourceFingerprint)
      return
    form.value.target.fieldList.forEach(field => {
      if (normalizeFieldType(field.fieldType) !== 'DECIMAL' || field.fieldSize > 0) return
      const actual = context.value.structure.columns.find(
        column => column.field.fieldName.toLowerCase() === field.fieldName.toLowerCase()
      )?.field
      if (actual) {
        field.fieldSize = actual.fieldSize
        field.fieldPrecision = actual.fieldPrecision
      }
    })
    precisionAlignment.value = true
    activeTab.value = 'draft'
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
  if (isCurrentView(version) && precisionAlignment.value) await inspect()
}
/**
 * 删除仍是本地草稿操作，清除该行旧检查结果，提交时由服务端重新校验整批
 */
const remove = (row: ITableField) => {
  if (!deletable(row) || !form.value) return
  form.value.target.fieldList = form.value.target.fieldList.filter(field => field.id !== row.id)
  if (original.value.some(field => field.id === row.id)) removed.value.push(deepCopy(row))
  fieldSaved(row)
}
const undo = (row: ITableField) => {
  if (!form.value || locked.value) return
  if (
    form.value.target.fieldList.some(
      field =>
        field.fieldName.toLowerCase() === row.fieldName.toLowerCase() ||
        field.fieldSource === row.fieldSource
    )
  ) {
    ElMessage.warning(t('sync_maintenance.restore_conflict'))
    return
  }
  removed.value = removed.value.filter(field => field.id !== row.id)
  form.value.target.fieldList.push(row)
}
/**
 * 普通编辑和问题定位使用同一抽屉，维护执行中的字段仍由现有门禁保护
 */
const edit = (row?: ITableField) => {
  if (
    locked.value ||
    (row && (!editable(row) || !context.value.structure?.operations.includes('UPDATE')))
  )
    return
  drawer.value?.showDialog(row, row ? issuesFor(row) : [])
}
/**
 * 只提交当前差异和读取时的指纹，不把调度设置、连接信息或完整任务交给维护接口覆盖
 */
const request = (operationId: string): ITaskMaintenanceRequest => ({
  taskId: context.value.taskId,
  operationId,
  configFingerprint: context.value.configFingerprint,
  schemaFingerprint: context.value.schemaFingerprint,
  sourceFingerprint: context.value.sourceFingerprint,
  sourceQuery: queryChanged.value ? sourceQuery.value : undefined,
  changes: changes.value,
  alignTargetPrecision: precisionAlignment.value || undefined
})
/**
 * 预检查失败留在编辑页保留全部草稿，通过后才进入执行确认，不在此处修改任务配置
 */
const inspect = async () => {
  if (!hasChanges.value || locked.value) return
  const version = viewVersion
  loading.value = true
  error.value = ''
  try {
    const response = await previewTaskMaintenanceApi(request(generateID()))
    if (!isCurrentView(version)) return
    preview.value = response.data
    // 阻断结果留在编辑页，旧插件仅返回整批原因时仍明确显示，不能进入执行确认
    validationIssues.value = preview.value.plan.fieldIssues || []
    validationBlockers.value = preview.value.plan.blockers.filter(
      reason => !validationIssues.value.some(issue => issue.reason === reason)
    )
    if (preview.value.plan.blockers.length) {
      confirmation.value = false
      const index = ordinaryDraft.value.findIndex(row => issuesFor(row).length)
      if (index >= 0) pagination.currentPage = Math.floor(index / pagination.pageSize) + 1
      else if (protectedFields.value.some(field => issuesFor(field).length))
        protectedExpanded.value = ['protected']
      return
    }
    exclusive.value = false
    deleteConfirmed.value = false
    riskConfirmed.value = false
    initializationConfirmed.value = false
    confirmation.value = true
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 只有检查通过且风险逐项确认后才提交，响应中断时保存操作编号并转为核对结果，禁止直接重发
 */
const apply = async () => {
  if (
    !preview.value ||
    pendingConfirmations.value ||
    preview.value.plan.blockers.length ||
    loading.value
  )
    return
  const version = viewVersion
  const submittedRequest = request(preview.value.operationId)
  stopPolling()
  loading.value = true
  try {
    const result = (
      await applyTaskMaintenanceApi({
        ...submittedRequest,
        planFingerprint: preview.value.planFingerprint,
        confirmed: true,
        exclusiveTargetConfirmed: true,
        deleteConfirmed: deleteConfirmed.value,
        riskConfirmed: riskConfirmed.value
      })
    ).data
    if (!isCurrentView(version)) return
    mergeStatus(result)
    precisionAlignment.value = false
    queryEditorVisible.value = false
    sourceQuery.value = originalSourceQuery.value
    form.value.target.fieldList = deepCopy(original.value)
    removed.value = []
    confirmation.value = false
    activeTab.value = result.state === 'DONE' ? 'current' : 'result'
    if (result.state === 'DONE') {
      if (!(await load(result.taskId, version)) || !isCurrentView(version)) return
    }
    emit('changed')
    pollTimer = setTimeout(poll, 1000)
  } catch (cause) {
    const uncertain = isRequestError(cause) && (!cause.response || cause.response.status >= 500)
    // 提交已离开页面仍保留原任务的核对编号，不能把请求中断当作允许重新提交
    if (uncertain) pendingSubmissions.set(submittedRequest.taskId, submittedRequest.operationId)
    if (!isCurrentView(version)) return
    showError(cause)
    if (uncertain) {
      uncertainOperation.value = submittedRequest.operationId
      confirmation.value = false
      activeTab.value = 'result'
      emit('changed')
    }
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 继续已有操作或重试保存配置，恢复原调整必须再次说明会继续未完成的计划
 */
const resume = async () => {
  if (!context.value?.operationId || loading.value) return
  const version = viewVersion
  const taskId = context.value.taskId
  const operationId = context.value.operationId
  const restarting = context.value.state === 'RECOVERY_REQUIRED'
  if (restarting) {
    try {
      await ElMessageBox.confirm(t('sync_maintenance.resume_original_help'), {
        type: 'warning',
        confirmButtonText: t('sync_maintenance.resume_original'),
        cancelButtonText: t('common.cancel')
      })
    } catch {
      return
    }
  }
  if (!isCurrentView(version) || context.value.operationId !== operationId) return
  stopPolling()
  loading.value = true
  error.value = ''
  try {
    const result = (
      await resumeTaskMaintenanceApi({
        taskId,
        operationId,
        changes: [],
        confirmed: restarting
      })
    ).data
    if (!isCurrentView(version)) return
    mergeStatus(result)
    pollTimer = setTimeout(poll, 1000)
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 取消编辑、重新读取和关闭窗口共用草稿确认，已提交或已生效的修改不参与本地撤销
 */
const confirmDiscard = async () => {
  if (
    ((activeTab.value === 'draft' && hasChanges.value) ||
      (queryEditorVisible.value && queryInput.value !== sourceQuery.value)) &&
    !uncertainOperation.value
  ) {
    try {
      await ElMessageBox.confirm(t('sync_maintenance.discard'), {
        type: 'warning',
        title: t('sync_maintenance.cancel_editing_title'),
        confirmButtonText: t('sync_maintenance.discard_changes'),
        cancelButtonText: t('sync_maintenance.continue_editing')
      })
    } catch {
      return false
    }
  }
  return true
}
/**
 * 确认放弃草稿后重新读取，读取失败时保留原编辑内容
 */
const reloadFields = async () => {
  const version = viewVersion
  if (loading.value || !(await confirmDiscard())) return
  if (!isCurrentView(version)) return
  await startEditing()
}
const close = async () => {
  const version = viewVersion
  if (loading.value || !(await confirmDiscard())) return false
  if (!isCurrentView(version)) return false
  viewVersion++
  stopPolling()
  visible.value = false
  return true
}
const showAllLogs = async () => {
  const taskId = context.value?.taskId
  if (taskId && (await close())) emit('openTaskLog', taskId)
}
const fullSubmitted = (taskId: string) => {
  if (!visible.value || context.value?.taskId !== taskId) return
  activeTab.value = 'result'
  emit('changed')
  poll()
}
/**
 * 放弃尚未提交的草稿，同时清除失效的预览与问题定位信息
 */
const discardDraft = () => {
  if (!form.value || submitted.value) return
  precisionAlignment.value = false
  queryEditorVisible.value = false
  sourceQuery.value = originalSourceQuery.value
  queryInput.value = sourceQuery.value
  if (originalSource.value) {
    Object.assign(context.value, originalSource.value)
    form.value.source.fieldList = (originalSource.value.sourceFields || []).map(field => ({
      ...field,
      id: generateID()
    }))
    form.value.source.targetFieldTypeList = originalSource.value.fieldTypes
  }
  form.value.target.fieldList = deepCopy(original.value)
  removed.value = []
  validationIssues.value = []
  validationBlockers.value = []
  preview.value = undefined
  error.value = ''
}
/**
 * 只取消当前未提交的编辑并返回配置页，不调用结束调整或数据库回滚接口
 */
const cancelEditing = async () => {
  const version = viewVersion
  if (loading.value || !(await confirmDiscard())) return
  if (!isCurrentView(version)) return
  discardDraft()
  activeTab.value = 'current'
}
/**
 * 只在用户查看时加载完整变更，操作编号已更新则先刷新上下文，避免展示另一轮调整的快照
 */
const showChanges = async () => {
  const version = viewVersion
  loading.value = true
  error.value = ''
  try {
    const details = (await getTaskMaintenanceDetailsApi(context.value.taskId)).data
    if (!isCurrentView(version)) return
    if (details.operationId !== context.value.operationId) {
      if (!(await load(context.value.taskId, version)) || !isCurrentView(version)) return
      if (['APPLYING', 'RECOVERY_REQUIRED', 'REINIT_REQUIRED'].includes(context.value.state))
        activeTab.value = 'result'
    } else {
      mergeStatus(details)
      changesVisible.value = true
    }
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 用户重新检查时重置读取次数，只核对现有操作，不重发字段修改
 */
const inspectStatus = async () => {
  if (!submitted.value) return poll()
  const version = viewVersion
  stopPolling()
  loading.value = true
  error.value = ''
  pollFailures = 0
  try {
    const response = await recheckTaskMaintenanceApi({
      taskId: context.value.taskId,
      operationId: context.value.operationId,
      changes: []
    })
    if (!isCurrentView(version)) return
    mergeStatus(response.data)
    trackingDatabase = context.value.plan.steps.some(step =>
      ['WAITING', 'UNKNOWN'].includes(step.state)
    )
    if (trackingDatabase) pollTimer = setTimeout(poll, 2000)
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 先核实实际结果并恢复有效配置，再结束或返回编辑；剩余意图仅恢复为草稿
 */
const finishAdjustment = async (editRemaining = false) => {
  const version = viewVersion
  const taskId = context.value.taskId
  const operationId = context.value.operationId
  try {
    await ElMessageBox.confirm(
      t(editRemaining ? 'sync_maintenance.edit_remaining_help' : 'sync_maintenance.finish_help'),
      {
        type: 'warning',
        title: t(
          editRemaining ? 'sync_maintenance.return_editing_title' : 'sync_maintenance.finish_title'
        ),
        confirmButtonText: t(
          editRemaining ? 'sync_maintenance.back_edit' : 'sync_maintenance.keep_and_finish'
        ),
        cancelButtonText: t('sync_maintenance.continue_viewing')
      }
    )
  } catch {
    return
  }
  if (!isCurrentView(version) || context.value.operationId !== operationId) return
  stopPolling()
  loading.value = true
  error.value = ''
  try {
    // 上下文接口省略前后快照，返回修改时按需读取原意图，避免恢复配置后丢失未完成修改
    if (editRemaining) {
      const details = await getTaskMaintenanceDetailsApi(taskId)
      if (!isCurrentView(version)) return
      if (details.data.operationId !== operationId) {
        await load(taskId, version)
        return
      }
      mergeStatus(details.data)
    }
    const pending: IFieldAdjustmentChange[] = editRemaining
      ? operationChanges.value.map(change => ({
          action: change.before ? (change.after ? 'UPDATE' : 'DELETE') : 'ADD',
          originalTargetName: change.before?.fieldName,
          after: change.after ? deepCopy(change.after) : undefined
        }))
      : []
    const result = (
      await finishTaskMaintenanceApi({
        taskId,
        operationId,
        changes: [],
        confirmed: true
      })
    ).data
    if (!isCurrentView(version)) return
    if (!(await load(result.taskId, version)) || !isCurrentView(version)) return
    if (editRemaining && ['DONE', 'REINIT_REQUIRED'].includes(result.state))
      restorePendingChanges(pending, result.plan.steps)
    activeTab.value = ['DONE', 'REINIT_REQUIRED'].includes(result.state)
      ? editRemaining && !context.value.blockedReason
        ? 'draft'
        : 'current'
      : 'result'
    emit('changed')
  } catch (cause) {
    if (isCurrentView(version)) showError(cause)
  } finally {
    if (isCurrentView(version)) loading.value = false
  }
}
/**
 * 成功的步骤作为当前基线，未完成的属性继续作为草稿；这里只恢复编辑意图，不提交 DDL
 */
const restorePendingChanges = (
  pending: IFieldAdjustmentChange[],
  steps: IMaintenancePlan['steps']
) => {
  for (const change of pending) {
    let name = change.originalTargetName || change.after?.fieldName
    steps
      .filter(step => step.action === 'RENAME' && step.state === 'DONE')
      .forEach(step => {
        if (step.before.fieldName === name) name = step.after.fieldName
      })
    const row = form.value.target.fieldList.find(field => field.fieldName === name)
    if (change.action === 'DELETE') {
      if (row) {
        form.value.target.fieldList = form.value.target.fieldList.filter(field => field !== row)
        removed.value.push(deepCopy(row))
      }
    } else if (row) {
      form.value.target.fieldList.splice(form.value.target.fieldList.indexOf(row), 1, {
        ...deepCopy(change.after),
        id: row.id
      })
    } else if (change.action === 'ADD') {
      form.value.target.fieldList.push({ ...deepCopy(change.after), id: generateID() })
    } else {
      throw new Error(t('sync_maintenance.restore_conflict'))
    }
  }
}
onBeforeUnmount(() => {
  viewVersion++
  stopPolling()
})
defineExpose({ open })
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="t('sync_maintenance.title')"
    width="min(1000px, 94vw)"
    class="field-adjustment-dialog"
    align-center
    :close-on-click-modal="false"
    :close-on-press-escape="!loading"
    :show-close="!loading"
    :before-close="close"
  >
    <div v-loading="loading" class="adjustment-body">
      <div class="task-context">
        <div class="task-heading">
          <strong class="ellipsis" :title="context?.taskName || taskName">{{
            context?.taskName || taskName
          }}</strong>
          <el-button link type="primary" @click="rulesVisible = true">{{
            t('sync_maintenance.rules_entry')
          }}</el-button>
        </div>
        <div class="connection-context">
          <div class="connection-card">
            <div class="connection-line">
              <span class="context-label">{{ t('sync_maintenance.source_connection') }}</span>
              <span class="ellipsis" :title="context?.sourceName">{{
                context?.sourceName || '-'
              }}</span>
              <span class="context-label">{{ context?.sourceType }}</span>
            </div>
            <div class="connection-line">
              <span class="context-label">{{
                t(
                  context?.sourceMode === 'TABLE'
                    ? 'sync_maintenance.source_table'
                    : 'sync_maintenance.query_source'
                )
              }}</span>
              <span
                v-if="context?.sourceMode === 'TABLE'"
                class="ellipsis"
                :title="context.sourceTable"
                >{{ context.sourceTable || '-' }}</span
              >
              <el-button
                v-if="context?.sourceQuery"
                link
                type="primary"
                @click="queryVisible = !queryVisible"
              >
                {{
                  t(queryVisible ? 'sync_maintenance.hide_query' : 'sync_maintenance.view_query')
                }}
              </el-button>
              <el-button
                v-if="queryEditable && !queryEditorVisible"
                link
                type="primary"
                :disabled="loading"
                @click="editSourceQuery"
                >{{ t('sync_maintenance.edit_source_query') }}</el-button
              >
            </div>
          </div>
          <div class="connection-card">
            <div class="connection-line">
              <span class="context-label">{{ t('sync_maintenance.target_connection') }}</span>
              <span class="ellipsis" :title="context?.targetName">{{
                context?.targetName || '-'
              }}</span>
              <span class="context-label">{{ context?.targetType }}</span>
            </div>
            <div class="connection-line">
              <span class="context-label">{{ t('sync_maintenance.target_table') }}</span>
              <span class="ellipsis" :title="context?.targetTable">{{
                context?.targetTable || '-'
              }}</span>
            </div>
          </div>
        </div>
        <div v-if="queryVisible && !queryEditorVisible" class="query-block">
          <div class="query-content">
            <pre>{{ context?.sourceQuery }}</pre>
          </div>
          <p class="context-label query-help">{{ t('sync_maintenance.query_readonly') }}</p>
        </div>
        <div v-if="queryEditorVisible" class="query-block">
          <el-input
            v-model="queryInput"
            type="textarea"
            :rows="6"
            :aria-label="t('sync_maintenance.source_query_draft')"
            :disabled="loading"
          />
          <p class="context-label query-help">{{ t('sync_maintenance.query_edit_help') }}</p>
          <div class="query-actions">
            <el-button :disabled="loading" @click="cancelSourceQuery">{{
              t('sync_maintenance.cancel_query_edit')
            }}</el-button>
            <el-button
              type="primary"
              :disabled="loading || !queryInput.trim()"
              @click="readSourceQuery"
              >{{ t('sync_maintenance.read_query_fields') }}</el-button
            >
          </div>
        </div>
      </div>
      <div class="adjustment-content">
        <el-alert
          v-if="precisionAlignment"
          :title="t('sync_maintenance.precision_alignment_help')"
          type="info"
          :closable="false"
          show-icon
        />
        <ExecutionRecovery
          v-if="context?.executionRecovery"
          :key="context.taskId + context.executionRecovery.logId"
          :task-id="context.taskId"
          :task-name="context.taskName"
          :target-table="context.targetTable"
          :active="visible"
          :recovery="context.executionRecovery"
          @updated="recoveryUpdated"
        />
        <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
        <div
          v-if="activeTab === 'draft' && (validationIssues.length || validationBlockers.length)"
          class="validation-summary"
        >
          <el-alert
            :title="t('sync_maintenance.check_failed')"
            :description="t('sync_maintenance.check_failed_help')"
            type="error"
            :closable="false"
            show-icon
          />
          <el-button v-if="validationIssues.length" link type="primary" @click="locateIssue">{{
            t('sync_maintenance.locate_issue')
          }}</el-button>
          <p v-for="reason in validationBlockers" :key="reason" class="validation-reason">
            {{ reason }}
          </p>
        </div>
        <el-alert
          v-if="
            context?.blockedReason &&
            !context.executionRecovery &&
            !submitted &&
            !uncertainOperation
          "
          :title="context.blockedReason"
          :description="
            context.blockedCode === 'SOURCE_READ_FAILED' && context.sourceQuerySupported
              ? t('sync_maintenance.query_read_failed_help')
              : undefined
          "
          type="warning"
          :closable="false"
          show-icon
        />
        <el-alert
          v-if="queryChanged && activeTab === 'draft'"
          :title="t('sync_maintenance.query_pending')"
          type="warning"
          :closable="false"
          show-icon
        />
        <div v-if="missingFields.length" class="validation-summary">
          <el-alert
            :title="t('sync_maintenance.source_missing_help')"
            type="warning"
            :closable="false"
            show-icon
          />
          <el-button v-if="activeTab === 'draft'" link type="primary" @click="locateIssue">{{
            t('sync_maintenance.locate_issue')
          }}</el-button>
        </div>
        <template v-if="uncertainOperation">
          <el-alert
            :title="t('sync_maintenance.response_unknown')"
            :description="t('sync_maintenance.response_unknown_help')"
            type="warning"
            :closable="false"
            show-icon
          />
          <el-collapse class="technical-details">
            <el-collapse-item :title="t('sync_maintenance.technical_details')" name="record">
              {{ t('sync_maintenance.operation_record') }}：{{ uncertainOperation }}
            </el-collapse-item>
          </el-collapse>
        </template>
        <template v-else-if="activeTab === 'result' && context?.state !== 'DONE' && context?.plan">
          <el-alert
            :title="resultTitle"
            :description="resultDescription"
            :type="operationFailed || context.state === 'REINIT_REQUIRED' ? 'warning' : 'info'"
            :closable="false"
            show-icon
          />
          <ol
            v-if="!processing && (savingConfiguration || configurationApplied)"
            class="operation-stages"
          >
            <li>
              {{ t('sync_maintenance.schema_stage')
              }}<span>{{
                t(
                  context.plan.steps.every(step => ['DONE', 'SKIPPED'].includes(step.state))
                    ? 'sync_maintenance.step_done'
                    : 'sync_maintenance.not_completed'
                )
              }}</span>
            </li>
            <li>
              {{ t('sync_maintenance.configuration_stage')
              }}<span>{{
                t(
                  configurationApplied
                    ? 'sync_maintenance.config_updated'
                    : 'sync_maintenance.config_not_updated'
                )
              }}</span>
            </li>
            <li v-if="context.plan.requiresReinitialization">
              {{ t('sync_maintenance.initialization_stage') }}<span>{{ initializationText }}</span>
            </li>
          </ol>
          <el-alert
            v-if="operationError && !failureSteps.some(step => step.error === operationError)"
            :title="failureSummary"
            type="error"
            :closable="false"
            show-icon
          />
          <p v-if="processing" class="current-stage">
            {{ t('sync_maintenance.current_stage') }}：{{
              configurationApplied
                ? initializationText
                : context.plan.steps.every(step => step.state === 'DONE')
                ? t('sync_maintenance.configuration_stage')
                : t('sync_maintenance.schema_stage')
            }}
          </p>
          <FieldExecutionResults
            v-if="
              !processing &&
              !savingConfiguration &&
              !configurationApplied &&
              context.plan.steps.length
            "
            :steps="context.plan.steps"
            :summarize-error="summarizeError"
          />
          <el-collapse
            v-if="operationFailed || context.initializationError"
            v-model="detailsVisible"
            class="technical-details"
            accordion
          >
            <el-collapse-item :title="t('sync_maintenance.technical_details')" name="record">
              <p class="context-label">
                {{ t('sync_maintenance.operation_record') }}：{{ context.operationId }}
              </p>
              <pre v-if="operationError" class="error-detail">{{ operationError }}</pre>
              <el-table :data="context.plan.steps" class="change-comparison" max-height="240">
                <el-table-column :label="t('sync_maintenance.adjustment_step')" min-width="180"
                  ><template #default="{ row }"
                    >{{ t(`sync_maintenance.action_${row.action.toLowerCase()}`) }} ·
                    {{ row.after?.fieldName || row.before?.fieldName }}</template
                  ></el-table-column
                >
                <el-table-column :label="t('sync_maintenance.execution_result')" width="140"
                  ><template #default="{ row }">{{
                    t(`sync_maintenance.step_${row.state.toLowerCase()}`)
                  }}</template></el-table-column
                >
                <el-table-column :label="t('sync_maintenance.failure_reason')" min-width="240"
                  ><template #default="{ row }">{{ row.error || '-' }}</template></el-table-column
                >
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </template>
        <template v-else-if="form">
          <el-alert
            v-if="activeTab === 'current' && context?.state === 'REINIT_REQUIRED'"
            :title="resultTitle"
            :description="resultDescription"
            type="warning"
            :closable="false"
            show-icon
          />
          <div v-if="activeTab === 'current' && context?.state === 'DONE'" class="applied-summary">
            <el-alert
              :title="appliedTitle"
              :description="appliedDescription"
              type="success"
              :closable="false"
              show-icon
            />
            <div class="applied-summary-actions">
              <el-button link type="primary" :disabled="loading" @click="showChanges">{{
                t('sync_maintenance.view_changes')
              }}</el-button>
              <el-button link type="primary" :disabled="loading" @click="showAllLogs">{{
                t('sync_task_log.view_more')
              }}</el-button>
            </div>
          </div>
          <p v-if="!context?.structureVerified" class="context-label">
            {{ t('sync_maintenance.unverified') }}
          </p>
          <div class="adjustment-actions">
            <span class="table-caption">{{
              t(
                activeTab === 'draft'
                  ? 'sync_maintenance.editing_fields'
                  : 'sync_maintenance.current_fields'
              )
            }}</span>
            <el-button
              v-if="activeTab === 'draft' && !precisionAlignment"
              :disabled="locked || !context.structure?.operations.includes('ADD')"
              @click="edit()"
              >{{ t('sync_task.add_field') }}</el-button
            >
          </div>
          <TargetFields
            v-if="activeTab === 'draft' ? ordinaryDraft.length : ordinaryCurrent.length"
            :rows="activeTab === 'draft' ? rows : currentRows"
            :columns="columns"
            :pagination="activeTab === 'draft' ? pagination : currentPagination"
            :index-label="t('sync_task.field_index')"
            :option-disabled="optionDisabled"
            :option-hint="optionHint"
            :editable="row => editable(row) && context.structure?.operations.includes('UPDATE')"
            :deletable="deletable"
            :status="rowStatus"
            :deleted="pendingDelete"
            :show-actions="activeTab === 'draft' && !precisionAlignment"
            :compact="true"
            :source-type="sourceType"
            :target-type="targetType"
            :actual-target-type="showActualDefinition ? actualTargetType : undefined"
            :protected-hint="protectedReason"
            :field-errors="fieldErrors"
            @page="
              activeTab === 'draft'
                ? (pagination.currentPage = $event)
                : (currentPagination.currentPage = $event)
            "
            @size="sizeChange"
            @edit="edit"
            @delete="remove"
            @undo="undo"
          />
          <p v-else class="context-label">
            {{
              t(
                protectedFields.length
                  ? 'sync_maintenance.only_protected_fields'
                  : 'sync_maintenance.no_configured_fields'
              )
            }}
          </p>
          <el-collapse
            v-if="protectedFields.length"
            v-model="protectedExpanded"
            class="protected-fields"
          >
            <el-collapse-item
              :title="t('sync_maintenance.protected_fields', [protectedFields.length])"
              name="protected"
            >
              <p class="context-label">{{ t('sync_maintenance.special') }}</p>
              <div v-for="field in protectedFields" :key="field.fieldName" class="protected-field">
                <span>{{ field.fieldName }}</span
                ><span class="context-label"
                  >{{ showActualDefinition ? t('sync_maintenance.saved_definition') + '：' : ''
                  }}{{ targetType(field) }}</span
                >
                <span v-if="showActualDefinition" class="context-label"
                  >{{ t('sync_maintenance.actual_definition') }}：{{
                    actualTargetType(field)
                  }}</span
                >
                <p v-for="message in fieldErrors(field)" :key="message" class="validation-reason">
                  {{ message }}
                </p>
              </div>
            </el-collapse-item>
          </el-collapse>
          <p v-if="!context.operationId && activeTab === 'current'" class="context-label">
            {{ t('sync_maintenance.no_record') }}
          </p>
        </template>
      </div>
      <TargetFieldForm
        v-if="form"
        ref="drawer"
        v-model="form"
        :adjustment-mode="true"
        :read-only="locked"
        :maintenance-structure="context.structure"
        :maintenance-fields="original"
        @saved="fieldSaved"
      />
    </div>
    <template #footer>
      <div class="adjustment-footer">
        <span v-if="activeTab === 'draft'" class="footer-note">{{
          t(
            queryChanged
              ? 'sync_maintenance.query_pending_count'
              : hasChanges
              ? 'sync_maintenance.pending_count'
              : 'sync_maintenance.no_changes',
            [changes.length]
          )
        }}</span>
        <el-button
          v-if="
            activeTab !== 'draft' &&
            !(
              activeTab === 'result' &&
              ((submitted && operationFailed && !savingConfiguration && !unknownResult) ||
                (context?.state === 'REINIT_REQUIRED' && !processing))
            )
          "
          :disabled="loading"
          @click="close"
          >{{ t(processing ? 'sync_maintenance.collapse' : 'sync_maintenance.close') }}</el-button
        >
        <el-button
          v-if="uncertainOperation"
          type="primary"
          :disabled="loading"
          @click="inspectStatus"
          >{{ t('sync_maintenance.check_result') }}</el-button
        >
        <template v-else-if="activeTab === 'draft'">
          <el-button :disabled="loading" @click="cancelEditing">{{
            t('sync_maintenance.discard_draft')
          }}</el-button>
          <el-button v-if="!precisionAlignment" :disabled="loading" @click="reloadFields">{{
            t('sync_maintenance.reload')
          }}</el-button>
          <el-button type="primary" :disabled="locked || !hasChanges" @click="inspect">{{
            t('sync_maintenance.check_all')
          }}</el-button>
        </template>
        <template v-else-if="submitted">
          <template v-if="!processing">
            <el-button
              v-if="unknownResult"
              :disabled="loading"
              type="primary"
              @click="inspectStatus"
              >{{ t('sync_maintenance.check_status') }}</el-button
            >
            <el-button
              v-else-if="savingConfiguration"
              :disabled="loading"
              type="primary"
              @click="resume"
              >{{ resumeLabel }}</el-button
            >
            <el-button
              v-else-if="!operationFailed && context.state === 'APPLYING'"
              :disabled="loading"
              type="primary"
              @click="resume"
              >{{ t('sync_maintenance.resume') }}</el-button
            >
            <template v-else>
              <el-button
                class="end-adjustment"
                link
                :disabled="loading"
                @click="finishAdjustment()"
                >{{ t('sync_maintenance.keep_completed') }}</el-button
              >
              <el-button :disabled="loading" type="primary" @click="finishAdjustment(true)">{{
                t('sync_maintenance.back_edit')
              }}</el-button>
            </template>
          </template>
        </template>
        <template v-else-if="context?.state === 'REINIT_REQUIRED' && !context.executionRecovery">
          <template v-if="!processing">
            <template v-if="activeTab === 'current'">
              <el-button :disabled="loading" @click="activeTab = 'result'">{{
                t('sync_maintenance.back_result')
              }}</el-button>
              <el-button :disabled="loading" type="primary" @click="startEditing">{{
                t('sync_maintenance.start_adjustment')
              }}</el-button>
            </template>
            <template v-else>
              <el-button :disabled="loading" @click="activeTab = 'current'">{{
                t('sync_maintenance.view_current_configuration')
              }}</el-button>
              <el-button
                v-if="context.initializationStatus !== 'CONNECTION_LOST'"
                type="primary"
                :disabled="loading || !initializationRetryable"
                @click="fullDialog?.open(context.taskId)"
                >{{ t('sync_maintenance.retry_initialization') }}</el-button
              >
            </template>
          </template>
        </template>
        <el-button v-else-if="activeTab === 'result'" @click="activeTab = 'current'">{{
          t('sync_maintenance.back_fields')
        }}</el-button>
        <el-button
          v-else-if="context?.blockedCode === 'PRECISION_UNSPECIFIED' && !context.executionRecovery"
          type="primary"
          :disabled="loading"
          @click="startPrecisionAlignment"
          >{{ t('sync_maintenance.align_precision') }}</el-button
        >
        <el-button v-else type="primary" :disabled="loading" @click="startEditing">{{
          t('sync_maintenance.start_adjustment')
        }}</el-button>
      </div>
    </template>
  </el-dialog>
  <el-dialog
    v-model="confirmation"
    :title="t('sync_maintenance.confirm')"
    width="min(700px, 94vw)"
    class="field-confirmation-dialog"
    align-center
    :close-on-click-modal="false"
    :close-on-press-escape="!loading"
    :show-close="!loading"
  >
    <template #header>
      <div class="confirmation-heading">{{ t('sync_maintenance.confirm') }}</div>
      <div class="confirmation-context">
        <span class="context-label">{{ t('sync_maintenance.task_name') }}</span>
        <span class="ellipsis" :title="context?.taskName || taskName">{{
          context?.taskName || taskName
        }}</span>
        <span class="context-label">{{ t('sync_maintenance.target_connection') }}</span>
        <span class="ellipsis" :title="context?.targetName">{{ context?.targetName }}</span>
      </div>
      <div class="confirmation-context">
        <span class="context-label">{{ t('sync_maintenance.target_table') }}</span>
        <span class="ellipsis" :title="context?.targetTable">{{ context?.targetTable }}</span>
      </div>
    </template>
    <div v-if="preview" class="confirmation-body">
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
      <el-alert
        v-for="blocker in preview.plan.blockers"
        :key="blocker"
        :title="blocker"
        type="error"
        :closable="false"
        show-icon
      />
      <section
        class="confirmation-impact"
        :class="{
          'requires-attention':
            preview.plan.requiresReinitialization || preview.plan.warnings.length
        }"
      >
        <div class="impact-title">
          {{
            t(
              precisionAlignment
                ? 'sync_maintenance.align_precision'
                : preview.plan.requiresReinitialization
                ? 'sync_maintenance.confirm_init_title'
                : 'sync_maintenance.confirm_impact_title'
            )
          }}
        </div>
        <p v-if="!precisionAlignment">
          {{
            t(
              preview.plan.requiresReinitialization
                ? 'sync_maintenance.history_replace'
                : hasDeletedFields
                ? 'sync_maintenance.confirm_delete_impact'
                : preview.plan.riskConfirmationRequired
                ? 'sync_maintenance.history_preserve'
                : 'sync_maintenance.confirm_preserve_impact'
            )
          }}
        </p>
        <p v-if="!precisionAlignment">
          {{
            t(
              preview.plan.requiresReinitialization
                ? 'sync_maintenance.confirm_init_schedule'
                : 'sync_maintenance.confirm_adjust_schedule'
            )
          }}
        </p>
        <p v-for="warning in preview.plan.warnings" :key="warning">{{ warning }}</p>
        <details v-if="!precisionAlignment" class="impact-details">
          <summary>{{ t('sync_maintenance.confirm_more_impact') }}</summary>
          <p v-if="affectsDownstream">{{ t('sync_maintenance.downstream_help') }}</p>
          <p>{{ t('sync_maintenance.confirm_execution_flow') }}</p>
          <p>{{ t('sync_maintenance.failure_help') }}</p>
          <p>{{ t('sync_maintenance.collapse_help') }}</p>
        </details>
      </section>
      <FieldChangeDetails
        :changes="previewChanges"
        :mapping-notes="preview.mappingNotes"
        :before-source-query="preview.beforeSourceQuery"
        :after-source-query="preview.afterSourceQuery"
      />
    </div>
    <template #footer>
      <div v-if="preview" class="confirmation-footer">
        <div v-if="!preview.plan.blockers.length" class="confirmation-options">
          <el-checkbox
            v-if="preview.plan.requiresReinitialization"
            v-model="initializationConfirmed"
            class="confirmation-option"
            >{{ t('sync_maintenance.confirm_initialization') }}</el-checkbox
          >
          <el-checkbox
            v-if="preview.plan.riskConfirmationRequired"
            v-model="riskConfirmed"
            class="confirmation-option"
            >{{
              t(
                precisionAlignment
                  ? 'sync_maintenance.confirm_precision_alignment'
                  : 'sync_maintenance.confirm_conversion_short'
              )
            }}</el-checkbox
          >
          <el-checkbox v-model="exclusive" class="confirmation-option">{{
            t('sync_maintenance.exclusive')
          }}</el-checkbox>
          <el-checkbox
            v-if="hasDeletedFields"
            v-model="deleteConfirmed"
            class="confirmation-option"
            >{{ t('sync_maintenance.delete_confirm') }}</el-checkbox
          >
        </div>
        <div class="confirmation-actions">
          <span class="context-label confirmation-hint" aria-live="polite">{{
            confirmationHint
          }}</span>
          <el-button :disabled="loading" @click="confirmation = false">{{
            t('sync_maintenance.back_edit')
          }}</el-button>
          <el-button
            type="primary"
            :disabled="loading || !!pendingConfirmations || !!preview.plan.blockers.length"
            @click="apply"
            >{{
              t(
                preview.plan.requiresReinitialization
                  ? 'sync_maintenance.apply_reinit'
                  : 'sync_maintenance.apply'
              )
            }}</el-button
          >
        </div>
      </div>
    </template>
  </el-dialog>
  <el-dialog
    v-model="changesVisible"
    :title="t('sync_maintenance.changes_title')"
    width="min(760px, 94vw)"
    class="maintenance-secondary-dialog"
    align-center
  >
    <FieldChangeDetails
      :changes="operationChanges"
      :before-source-query="context?.beforeSourceQuery"
      :after-source-query="context?.afterSourceQuery"
    />
    <template #footer
      ><el-button @click="changesVisible = false">{{
        t('sync_maintenance.close')
      }}</el-button></template
    >
  </el-dialog>
  <el-dialog
    v-model="rulesVisible"
    :title="t('sync_maintenance.rules_entry')"
    width="min(640px, 94vw)"
    class="maintenance-secondary-dialog maintenance-rules-dialog"
    align-center
  >
    <template #header>
      <div class="rules-heading">{{ t('sync_maintenance.rules_entry') }}</div>
      <div class="rule-provider">
        <div class="connection-line">
          <span class="context-label">{{ t('sync_maintenance.target_datasource') }}</span>
          <span class="ellipsis" :title="context?.targetName">{{
            context?.targetName || '-'
          }}</span>
        </div>
        <div class="connection-line">
          <span class="context-label">{{ t('sync_maintenance.database_type') }}</span>
          <span>{{ context?.targetType || '-' }}</span>
          <span class="context-label">{{ t('sync_maintenance.database_version') }}</span>
          <span :title="context?.structure?.version">{{ databaseVersion || '-' }}</span>
        </div>
      </div>
      <p v-if="context?.structure?.rules?.length" class="rules-intro context-label">
        {{ t('sync_maintenance.rules_expand_hint') }}
      </p>
    </template>
    <template v-if="context?.structure?.rules?.length">
      <el-collapse accordion class="plugin-rules">
        <el-collapse-item
          v-for="(rule, index) in context.structure.rules"
          :key="index"
          :name="index"
        >
          <template #title>
            <span class="rule-title">
              <span>{{ rule.item }}</span>
              <el-tag size="small" type="info">{{
                t(
                  rule.source === 'DATABASE'
                    ? 'sync_maintenance.rule_source_database'
                    : rule.source === 'TASK'
                    ? 'sync_maintenance.rule_source_task'
                    : 'sync_maintenance.rule_source_plugin'
                )
              }}</el-tag>
            </span>
          </template>
          <template v-if="rule.officialText">
            <p class="context-label">{{ t('sync_maintenance.official_excerpt') }}</p>
            <blockquote class="official-excerpt">{{ rule.officialText }}</blockquote>
          </template>
          <p v-if="rule.supported">
            <span class="context-label">{{ t('sync_maintenance.rule_supported') }}：</span
            >{{ rule.supported }}
          </p>
          <p v-if="rule.restricted">
            <span class="context-label">{{ t('sync_maintenance.rule_restricted') }}：</span
            >{{ rule.restricted }}
          </p>
          <p v-if="rule.documentationUrl?.startsWith('https://')" class="rule-documentation">
            <span v-if="rule.applicableVersion" class="context-label">{{
              rule.applicableVersion
            }}</span>
            <el-link
              :href="rule.documentationUrl"
              target="_blank"
              rel="noopener noreferrer"
              type="primary"
              >{{ t('sync_maintenance.official_documentation') }}</el-link
            >
          </p>
        </el-collapse-item>
      </el-collapse>
    </template>
    <el-alert
      v-else
      :title="t('sync_maintenance.rules_unavailable')"
      :description="t('sync_maintenance.plugin_rules_help')"
      type="info"
      :closable="false"
      show-icon
    />
    <template #footer>
      <div class="rules-footer">
        <p class="rules-effect">{{ t('sync_maintenance.rules_effect_brief') }}</p>
        <el-button @click="rulesVisible = false">{{ t('sync_maintenance.close') }}</el-button>
      </div>
    </template>
  </el-dialog>
  <FullRefreshDialog ref="fullDialog" @submitted="fullSubmitted" />
</template>

<style scoped lang="less">
// 检查摘要、定位入口和字段表保持独立间距，避免原因文字贴住表格或按钮
.validation-summary {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}
.validation-reason {
  margin: 0;
  color: var(--ed-color-danger);
  line-height: 22px;
}
.confirmation-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  > * {
    flex-shrink: 0;
  }
}
.confirmation-heading {
  padding-right: 32px;
  font-size: 16px;
  font-weight: 500;
  line-height: 24px;
}
.confirmation-context {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 8px;
  min-width: 0;
  font-size: 12px;
  font-weight: normal;
  line-height: 20px;
  .context-label {
    flex-shrink: 0;
  }
  .ellipsis {
    flex: 1;
  }
}
.confirmation-impact {
  padding: 14px 16px;
  border-radius: 4px;
  background: var(--ed-fill-color-light);
  line-height: 22px;
  overflow-wrap: anywhere;
  p {
    margin: 8px 0 0;
    font-size: 12px;
  }
  &.requires-attention {
    color: var(--ed-color-warning-dark-2);
    background: var(--ed-color-warning-light-9);
  }
}
.impact-title {
  font-weight: 500;
}
.impact-details {
  margin-top: 8px;
  font-size: 12px;
  summary {
    cursor: pointer;
  }
}
.confirmation-footer {
  text-align: left;
}
.confirmation-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}
.confirmation-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  .confirmation-hint {
    margin-right: auto;
  }
  :deep(.ed-button + .ed-button) {
    margin-left: 0;
  }
}
.adjustment-body {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}
.adjustment-content {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
  overflow: auto;
  padding: 16px 8px 8px 0;
  > * {
    flex-shrink: 0;
  }
}
.task-context {
  flex-shrink: 0;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--ed-border-color);
}
.task-heading,
.connection-line {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.task-heading {
  justify-content: space-between;
  gap: 20px;
  :deep(.ed-button) {
    flex-shrink: 0;
  }
}
.ellipsis {
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.connection-context {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 24px;
  margin-top: 12px;
}
.connection-card {
  min-width: 0;
}
.connection-line {
  min-height: 28px;
  .context-label,
  :deep(.ed-button) {
    flex-shrink: 0;
  }
}
.context-label {
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  line-height: 20px;
}
.context-value {
  color: var(--ed-text-color-primary);
}
.query-content {
  background: var(--ed-fill-color-light);
  padding: 12px;
  margin-top: 12px;
  pre {
    margin: 0;
    max-height: 120px;
    overflow: auto;
    line-height: 22px;
    white-space: pre-wrap;
    overflow-wrap: anywhere;
  }
}
.query-help {
  margin: 8px 0 0;
  line-height: 1.6;
}
.query-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
.adjustment-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.applied-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  :deep(.ed-alert) {
    flex: 1;
    min-width: 0;
  }
  :deep(.ed-button) {
    flex-shrink: 0;
  }
}
.end-adjustment {
  margin-right: auto;
}
.applied-summary-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
  :deep(.ed-button + .ed-button) {
    margin-left: 0;
  }
}
.technical-details {
  :deep(.ed-collapse-item__content) {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 12px 0 16px;
  }
}
.error-detail {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
.rules-heading {
  padding-right: 32px;
  font-size: 16px;
  font-weight: 500;
  line-height: 24px;
}
.rule-provider {
  margin-top: 12px;
  font-size: 14px;
  font-weight: normal;
  .context-label:first-child {
    min-width: 60px;
  }
  .context-label:not(:first-child) {
    margin-left: 12px;
  }
}
.rules-intro {
  margin: 8px 0 0;
  font-weight: normal;
}
.plugin-rules {
  margin-top: 0;
}
.plugin-rules :deep(.ed-collapse-item__content) {
  padding: 12px 16px 16px;
}
.rules-effect {
  flex: 1;
  min-width: 0;
  margin: 0;
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  line-height: 20px;
  text-align: left;
}
.rules-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  :deep(.ed-button) {
    flex-shrink: 0;
  }
}
.plugin-rules p {
  margin: 8px 0;
  line-height: 1.6;
  overflow-wrap: anywhere;
}
.rule-title,
.rule-documentation {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 12px;
}
.official-excerpt {
  margin: 8px 0 16px;
  padding: 8px 12px;
  border-left: 3px solid var(--ed-border-color);
  line-height: 1.6;
  white-space: pre-line;
  overflow-wrap: anywhere;
}
.operation-stages {
  margin: 0;
  padding: 0;
  list-style: none;
  li {
    padding: 14px 0;
    display: flex;
    justify-content: space-between;
    gap: 16px;
    border-bottom: 1px solid var(--ed-border-color);
    span {
      color: var(--ed-text-color-secondary);
    }
  }
}
.change-comparison :deep(.cell) {
  overflow-wrap: anywhere;
  white-space: normal;
}
.adjustment-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  :deep(.ed-button + .ed-button) {
    margin-left: 0;
  }
}
.confirmation-option {
  display: flex;
  align-items: flex-start;
  height: auto;
  margin: 0;
  :deep(.ed-checkbox__input) {
    margin-top: 4px;
  }
  :deep(.ed-checkbox__label) {
    white-space: normal;
    line-height: 22px;
  }
}
.protected-field {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  overflow-wrap: anywhere;
  padding: 6px 0;
  line-height: 22px;
  > span {
    min-width: 0;
  }
  .validation-reason {
    flex-basis: 100%;
    margin: 0;
  }
}
.footer-note {
  margin-right: auto;
  font-size: 12px;
  color: var(--ed-text-color-secondary);
  line-height: 20px;
  flex-basis: 200px;
  flex-grow: 1;
}
@media (max-width: 600px) {
  .confirmation-actions .confirmation-hint {
    flex-basis: 100%;
  }
  .connection-context {
    grid-template-columns: 1fr;
  }
  .operation-stages li {
    flex-direction: column;
    gap: 4px;
  }
  .applied-summary {
    flex-wrap: wrap;
    :deep(.ed-alert) {
      flex-basis: 100%;
    }
  }
}
</style>

<style lang="less">
.field-adjustment-dialog,
.field-confirmation-dialog,
.maintenance-secondary-dialog {
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 48px);
  .ed-dialog__header,
  .ed-dialog__footer {
    flex-shrink: 0;
  }
  .ed-dialog__body {
    min-height: 0;
    overflow-y: auto;
  }
  .ed-dialog__footer {
    padding-top: 16px;
  }
  .ed-alert__title,
  .ed-alert__description {
    overflow-wrap: anywhere;
    line-height: 22px;
  }
}
.field-adjustment-dialog .ed-dialog__body {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.field-confirmation-dialog {
  .ed-dialog__header {
    padding-bottom: 16px;
    border-bottom: 1px solid var(--ed-border-color);
  }
  .ed-dialog__footer {
    margin-top: 16px;
    border-top: 1px solid var(--ed-border-color);
  }
  .ed-dialog__body {
    padding-top: 16px;
  }
}
.maintenance-secondary-dialog {
  max-height: 70vh;
}
// 规则列表独占正文滚动区，头部连接信息和底部说明通过插槽固定，不跟随规则展开而移出视野
.maintenance-rules-dialog {
  .ed-dialog__header {
    padding-bottom: 16px;
    border-bottom: 1px solid var(--ed-border-color);
  }
  .ed-dialog__body {
    padding-top: 16px;
  }
  .ed-dialog__footer {
    border-top: 1px solid var(--ed-border-color);
  }
}
</style>
