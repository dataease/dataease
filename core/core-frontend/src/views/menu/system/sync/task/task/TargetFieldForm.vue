<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'
import { computed, nextTick, reactive, ref } from 'vue'
import { ITableField, ITaskInfoRes, IMaintenanceStructure } from '@/api/sync/syncTask'
import type { IMaintenanceFieldIssue } from '@/api/sync/syncTask'
import type { FormInstance } from 'element-plus-secondary'
import { ElMessage } from 'element-plus-secondary'
import { deepCopy } from '@/utils/utils'
import { includes } from 'lodash-es'
import { decimalParameterIssue, maintenanceFieldType, normalizeFieldType } from './fieldType'

const { t } = useI18n()
const dialogVisible = ref(false)
const editId = ref('')
const checkedIssues = ref<IMaintenanceFieldIssue[]>([])
const checkedValues = ref<Partial<ITableField>>({})
const fieldForm = ref<FormInstance>()
const props = withDefaults(
  defineProps<{
    modelValue: ITaskInfoRes
    readOnly?: boolean
    adjustmentMode?: boolean
    maintenanceStructure?: IMaintenanceStructure
    maintenanceFields?: ITableField[]
  }>(),
  {
    readOnly: false,
    adjustmentMode: false,
    modelValue: () => {
      return {} as ITaskInfoRes
    }
  }
)
const emits = defineEmits(['update:modelValue', 'saved'])
const baseline = computed(() => props.maintenanceFields?.find(field => field.id === editId.value))
const form = computed<ITaskInfoRes>({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})
const state = reactive({
  form: reactive({
    fieldSource: '',
    fieldSourceType: '',
    fieldSourceStandardType: '',
    fieldName: '',
    fieldType: '',
    fieldElementType: '',
    fieldMappingMessage: '',
    remarks: '',
    fieldPk: false,
    fieldIndex: false,
    id: '',
    fieldSize: 0,
    fieldPrecision: 0
  })
})

const spatialSourceTypes = new Set([
  'GEOMETRY',
  'GEOGRAPHY',
  'POINT',
  'LINESTRING',
  'POLYGON',
  'MULTIPOINT',
  'MULTILINESTRING',
  'MULTIPOLYGON',
  'GEOMETRYCOLLECTION',
  'GEO_POINT',
  'GEO_SHAPE'
])

/**
 * 判断当前字段是否具有空间类型语义
 */
const isSpatialSourceField = computed(() => {
  const sourceType = (state.form.fieldSourceType || '')
    .split('(')[0]
    .trim()
    .replaceAll(' ', '_')
    .toUpperCase()
  return spatialSourceTypes.has(sourceType)
})

/**
 * 普通源字段不展示数组目标类型，避免误选后在同步阶段发生类型不匹配
 */
const availableTargetFieldTypeList = computed(() => {
  const typeList = form.value.source.targetFieldTypeList || []
  if (isSpatialSourceField.value) {
    return typeList.filter(type => ['UNKNOWN', 'BYTEA', 'TEXT'].includes(type))
  }
  if (state.form.fieldElementType) {
    return typeList
  }
  return typeList.filter(type => !type.endsWith('_ARRAY'))
})

/**
 * 原生名称用于选中和展示，重新选择当前类型时保留原标准值，避免产生假变更
 */
const targetFieldType = computed<string>({
  get: () =>
    props.adjustmentMode
      ? maintenanceFieldType(state.form, baseline.value, props.maintenanceStructure)
      : state.form.fieldType,
  set: value => {
    if (value !== targetFieldType.value) state.form.fieldType = value
  }
})

const selectedSourceField = computed(() =>
  form.value.source.fieldList?.find(item => item.fieldName === state.form.fieldSource)
)
/**
 * 获取当前字段在源数据库中的原始类型
 * 历史任务未保存原始类型时，从当前源字段列表补充展示
 */
const sourceFieldType = computed(() => {
  const sourceField = selectedSourceField.value
  const raw = sourceField ? sourceField.fieldSourceType : state.form.fieldSourceType
  const standard = sourceField
    ? sourceField.fieldSourceStandardType
    : state.form.fieldSourceStandardType
  return (
    raw ||
    (standard
      ? t('sync_maintenance.standard_type', [standard])
      : t('sync_maintenance.type_unavailable'))
  )
})

const fieldMappingMessage = computed(() => {
  const issue = decimalParameterIssue(state.form, form.value.target.type)
  if (issue) return t(issue, [state.form.fieldName, sourceFieldType.value, state.form.fieldType])
  if (state.form.fieldMappingMessage) {
    return state.form.fieldMappingMessage
  }
  return state.form.fieldType?.trim().toUpperCase() === 'UNKNOWN'
    ? t('sync_task.field_mapping_unknown_detail')
    : ''
})
const fieldMappingMessageType = computed(() =>
  state.form.fieldType?.trim().toUpperCase() === 'UNKNOWN' ||
  decimalParameterIssue(state.form, form.value.target.type)
    ? 'error'
    : 'warning'
)

/**
 * 已有字段的目标类型转换使用原目标类型作为基线，不能把源类型推荐误当作原表允许变更范围
 */
const typeRule = (type: string) => {
  if (!props.adjustmentMode || !baseline.value) return undefined
  return props.maintenanceStructure?.typeChangeRules?.[
    normalizeFieldType(baseline.value.fieldType)
  ]?.[normalizeFieldType(type)]
}
const typeDisabled = (type: string) => {
  if (!props.adjustmentMode || !baseline.value) return false
  if (normalizeFieldType(type) === normalizeFieldType(baseline.value.fieldType)) return false
  const rule = typeRule(type)
  if (rule) return rule.level === 'FORBIDDEN'
  return !props.maintenanceStructure?.typeChanges?.[
    normalizeFieldType(baseline.value.fieldType)
  ]?.includes(normalizeFieldType(type))
}
const conversionWarning = computed(() => {
  const rule = typeRule(state.form.fieldType)
  return rule?.level === 'WARNING' ? rule.reason : ''
})
const minimumSize = computed(() => {
  const sameType =
    baseline.value &&
    normalizeFieldType(baseline.value.fieldType) === normalizeFieldType(state.form.fieldType)
  return Math.max(
    typeRule(state.form.fieldType)?.minimumSize || 1,
    sameType && ['VARCHAR', 'CHAR'].includes(normalizeFieldType(state.form.fieldType))
      ? baseline.value.fieldSize || 1
      : 1
  )
})
/**
 * 直接使用数据源接口已完成映射的候选目标类型，页面不再建立跨数据库类型映射表
 */
const recommendedTargetField = computed(() => {
  const source = selectedSourceField.value
  if (!source || source.fieldType === 'UNKNOWN') return undefined
  const fieldType =
    availableTargetFieldTypeList.value.find(type => type === source.fieldType) ||
    (props.adjustmentMode
      ? availableTargetFieldTypeList.value.find(
          type => normalizeFieldType(type) === normalizeFieldType(source.fieldType)
        )
      : undefined)
  if (!fieldType) return undefined
  const type = normalizeFieldType(fieldType)
  // 已有字符字段只允许扩容，推荐定义不能缩小当前目标容量
  const size = ['CHAR', 'VARCHAR'].includes(type)
    ? Math.max(
        source.fieldSize || 1,
        typeRule(source.fieldType)?.minimumSize || 1,
        baseline.value && normalizeFieldType(baseline.value.fieldType) === type
          ? baseline.value.fieldSize || 1
          : 1
      )
    : source.fieldSize || 0
  return {
    fieldType,
    fieldSize: size,
    fieldPrecision: source.fieldPrecision || 0
  }
})
const recommendedTypeLabel = computed(() => {
  const field = recommendedTargetField.value
  if (!field) return ''
  const type = normalizeFieldType(field.fieldType)
  if (['CHAR', 'VARCHAR', 'DECIMAL'].includes(type) && field.fieldSize) {
    return `${fieldTypeLabel(field.fieldType)}(${field.fieldSize}${
      type === 'DECIMAL' ? `, ${field.fieldPrecision}` : ''
    })`
  }
  if (type === 'DATETIME' && field.fieldPrecision)
    return `${fieldTypeLabel(field.fieldType)}(${field.fieldPrecision})`
  return fieldTypeLabel(field.fieldType)
})
const recommendationDiffers = computed(() => {
  const field = recommendedTargetField.value
  if (!field) return false
  const type = normalizeFieldType(field.fieldType)
  return (
    normalizeFieldType(state.form.fieldType) !== type ||
    (['CHAR', 'VARCHAR', 'DECIMAL'].includes(type) && state.form.fieldSize !== field.fieldSize) ||
    (['DECIMAL', 'DATETIME'].includes(type) && state.form.fieldPrecision !== field.fieldPrecision)
  )
})
const recommendationBlockedReason = computed(() => {
  const field = recommendedTargetField.value
  return field && typeDisabled(field.fieldType)
    ? typeRule(field.fieldType)?.reason || t('sync_maintenance.recommendation_blocked')
    : ''
})
const changeTargetType = () => {
  clearUnsupportedFieldOptions()
  if (
    props.adjustmentMode &&
    ['VARCHAR', 'CHAR'].includes(normalizeFieldType(state.form.fieldType))
  ) {
    state.form.fieldSize = Math.max(state.form.fieldSize || 0, minimumSize.value)
  }
}
/**
 * 维护模式下已有字段只在用户明确采用推荐后覆盖类型，不能在切换源字段时静默改写目标结构
 */
const useRecommendedType = () => {
  if (props.readOnly || !recommendedTargetField.value || recommendationBlockedReason.value) return
  Object.assign(state.form, recommendedTargetField.value)
  changeTargetType()
  fieldForm.value?.clearValidate(['fieldType', 'fieldSize', 'fieldPrecision'])
}
const nameMaxLength = computed(() =>
  props.adjustmentMode ? props.maintenanceStructure?.fieldNameMaxLength : 32
)
const commentMaxLength = computed(() =>
  props.adjustmentMode ? props.maintenanceStructure?.fieldCommentMaxLength : 255
)
const sizeMax = computed(() =>
  props.adjustmentMode
    ? props.maintenanceStructure?.sizeLimits?.[normalizeFieldType(state.form.fieldType)]
    : form.value.target.type === 'doris' && normalizeFieldType(state.form.fieldType) === 'DECIMAL'
    ? 38
    : 100000000
)
const precisionMax = computed(() =>
  props.adjustmentMode
    ? props.maintenanceStructure?.precisionLimits?.[normalizeFieldType(state.form.fieldType)]
    : form.value.target.type === 'doris' && normalizeFieldType(state.form.fieldType) === 'DECIMAL'
    ? state.form.fieldSize || 38
    : 20
)

const indexDatasource = computed(() =>
  form.value.target.dsList?.find(ds => ds.id === form.value.target.datasourceId)
)
const indexLabel = computed(() =>
  indexDatasource.value?.fieldIndexType
    ? `${indexDatasource.value.fieldIndexType} ${t('sync_task.field_index')}`
    : t('sync_task.field_index')
)
const isIndexFieldDisabled = (field: ITableField) =>
  !indexDatasource.value?.fieldIndexType ||
  !indexDatasource.value.supportedIndexFieldTypes?.some(
    type => normalizeFieldType(type) === normalizeFieldType(field.fieldType)
  )
const indexDisabledReason = (field: ITableField) => {
  if (!indexDatasource.value?.supportedIndexFieldTypes)
    return t('sync_task.index_capability_unavailable')
  if (!indexDatasource.value.fieldIndexType) return t('sync_task.index_not_implemented')
  return isIndexFieldDisabled(field) ? t('sync_task.index_type_unsupported') : ''
}

const unsupportedKeyFieldTypes = computed(
  () =>
    form.value.target.dsList?.find(ds => ds.id === form.value.target.datasourceId)
      ?.unsupportedKeyFieldTypes
)
const isKeyFieldDisabled = (field: ITableField) =>
  !unsupportedKeyFieldTypes.value ||
  unsupportedKeyFieldTypes.value.some(
    type => normalizeFieldType(type) === normalizeFieldType(field.fieldType)
  )

// 能力已加载时，允许主动取消历史不兼容勾选，但不允许重新选中
const isFieldOptionDisabled = (field: ITableField, option: 'fieldPk' | 'fieldIndex') => {
  if (props.readOnly || (props.adjustmentMode && option === 'fieldPk')) return true
  const capability =
    option === 'fieldPk'
      ? unsupportedKeyFieldTypes.value
      : indexDatasource.value?.supportedIndexFieldTypes
  const unsupported = option === 'fieldPk' ? isKeyFieldDisabled(field) : isIndexFieldDisabled(field)
  return !capability || (unsupported && !field[option])
}

const fieldOptionHint = (field: ITableField, option: 'fieldPk' | 'fieldIndex') => {
  const unsupported = option === 'fieldPk' ? isKeyFieldDisabled(field) : isIndexFieldDisabled(field)
  if (unsupported && field[option] && !isFieldOptionDisabled(field, option)) {
    return t('sync_task.unsupported_option_uncheck')
  }
  if (option === 'fieldIndex') return indexDisabledReason(field)
  if (!unsupportedKeyFieldTypes.value) return t('sync_task.key_policy_load_failed')
  return unsupported ? t('sync_task.key_type_unsupported') : ''
}

// 仅响应用户选择类型/源字段，打开历史字段或刷新能力信息时保留原始勾选
const clearUnsupportedFieldOptions = () => {
  if (props.readOnly) return
  if (indexDatasource.value?.supportedIndexFieldTypes && isIndexFieldDisabled(state.form))
    state.form.fieldIndex = false
  if (unsupportedKeyFieldTypes.value && isKeyFieldDisabled(state.form)) {
    state.form.fieldPk = false
  }
}

/**
 * 数组类型使用 PostgreSQL 原生写法展示，保存值仍使用稳定的内部标识
 */
const fieldTypeLabel = (type: string) => {
  if (!type.endsWith('_ARRAY')) {
    return type
  }
  return `${type.replace(/_ARRAY$/, '').replace(/_/g, ' ')}[]`
}
const rules = computed(() => ({
  fieldSource: {
    required: true,
    message: t('common.required'),
    trigger: 'blur'
  },
  fieldName: [
    {
      required: true,
      message: t('common.required'),
      trigger: 'change'
    },
    {
      max: nameMaxLength.value,
      message: t('sync_task.input_limit', [nameMaxLength.value]),
      trigger: 'change'
    }
  ],
  remarks: [
    {
      max: commentMaxLength.value,
      message: t('sync_task.input_limit', [commentMaxLength.value]),
      trigger: 'change'
    }
  ],
  fieldType: {
    required: true,
    message: t('common.required'),
    trigger: 'blur'
  },
  fieldSize: {
    required: true,
    message: t('common.required'),
    trigger: 'blur'
  },
  fieldPrecision: {
    required: true,
    message: t('common.required'),
    trigger: 'blur'
  }
}))
/**
 * 将整批检查的问题定位到抽屉中的具体属性，属性修改后清除该项旧检查结果
 */
const fieldError = (property: keyof typeof state.form) =>
  state.form[property] === checkedValues.value[property]
    ? checkedIssues.value
        .filter(issue => issue.property === property)
        .map(issue => issue.reason)
        .join('；')
    : ''

/**
 * 普通编辑与错误定位复用同一抽屉，服务端错误不改变字段草稿
 */
const showDialog = (row?: ITableField, issues: IMaintenanceFieldIssue[] = []) => {
  dialogVisible.value = true
  editId.value = row?.id || ''
  checkedIssues.value = issues
  checkedValues.value = row ? deepCopy(row) : {}
  fieldForm.value?.clearValidate()
  if (props.adjustmentMode && !row) {
    state.form = {
      fieldSource: '',
      fieldSourceType: '',
      fieldSourceStandardType: '',
      fieldName: '',
      fieldType: '',
      fieldElementType: '',
      fieldMappingMessage: '',
      remarks: '',
      fieldPk: false,
      fieldIndex: false,
      id: '',
      fieldSize: 0,
      fieldPrecision: 0
    }
  }
  if (editId.value != '') {
    state.form = deepCopy(row)
  }
  if (issues.length) nextTick(() => fieldForm.value?.scrollToField(issues[0].property))
}
/**
 * 新增字段采用来源映射后的候选类型，维护模式下已有字段更换来源时保留原目标类型
 * @param val
 */
const changeFieldSource = (val: string) => {
  const sourceFieldObj = form.value.source.fieldList
    ?.filter(item => item.fieldName === val)
    .map(item => item)[0]
  if (sourceFieldObj) {
    if (props.adjustmentMode && editId.value) {
      state.form.fieldSource = val
      state.form.fieldSourceType = sourceFieldObj.fieldSourceType || ''
      state.form.fieldSourceStandardType = sourceFieldObj.fieldSourceStandardType || ''
      state.form.fieldElementType = sourceFieldObj.fieldElementType || ''
      state.form.fieldMappingMessage = sourceFieldObj.fieldMappingMessage || ''
      return
    }
    state.form = deepCopy(sourceFieldObj)
    state.form.fieldSource = val
    if (props.adjustmentMode) {
      state.form.fieldPk = false
      if (recommendedTargetField.value) Object.assign(state.form, recommendedTargetField.value)
      else state.form.fieldType = ''
    }
    if (editId.value != '') {
      state.form.id = editId.value
    }
    clearUnsupportedFieldOptions()
  }
}

const resetForm = () => {
  fieldForm.value?.resetFields()
  dialogVisible.value = false
}
/**
 * 这里只更新父组件字段草稿，维护模式下的确认修改不调用数据库或保存任务配置
 */
const saveForm = () => {
  if (props.adjustmentMode && props.readOnly) return
  if (decimalParameterIssue(state.form, form.value.target.type)) {
    ElMessage.warning(fieldMappingMessage.value)
    return
  }
  if (fieldForm.value) {
    if (props.adjustmentMode && typeDisabled(state.form.fieldType)) return
    if (
      props.adjustmentMode &&
      ['VARCHAR', 'CHAR'].includes(normalizeFieldType(state.form.fieldType)) &&
      state.form.fieldSize < minimumSize.value
    ) {
      ElMessage.warning(t('sync_maintenance.minimum_size', [minimumSize.value]))
      return
    }
    const repeat = form.value.target.fieldList
      .filter(item => {
        // 新增时不校验 ID
        if (editId.value === '') {
          return props.adjustmentMode
            ? item.fieldName.toLowerCase() === state.form.fieldName.toLowerCase()
            : item.fieldName === state.form.fieldName
        } else {
          return (
            item.id != editId.value &&
            (props.adjustmentMode
              ? item.fieldName.toLowerCase() === state.form.fieldName.toLowerCase()
              : item.fieldName === state.form.fieldName)
          )
        }
      })
      .map(item => item)[0]
    const repeatSourceField = form.value.target.fieldList
      .filter(item => {
        // 新增时不校验ID
        if (editId.value === '') {
          return item.fieldSource === state.form.fieldSource
        } else {
          return item.id != editId.value && item.fieldSource === state.form.fieldSource
        }
      })
      .map(item => item)[0]
    if (/^[0-9]/.test(state.form.fieldName)) {
      ElMessage.warning(t('sync_task.cannot_begin_with_number'))
      return
    }
    if (repeatSourceField) {
      ElMessage.warning(t('sync_task.duplicate_field_tip', [state.form.fieldSource]))
      return
    }
    if (repeat) {
      ElMessage.warning(t('sync_task.duplicate_name_error', [state.form.fieldName]))
      return
    }
    fieldForm.value.validate(valid => {
      if (valid) {
        if (editId.value === '') {
          form.value.target.fieldList.push(deepCopy(state.form))
        } else {
          form.value.target.fieldList.forEach((item, index) => {
            if (item.id === editId.value) {
              form.value.target.fieldList[index] = deepCopy(state.form)
              if (!showFieldSize(state.form)) {
                form.value.target.fieldList[index].fieldSize = undefined
              }
            }
          })
        }
        if (props.adjustmentMode) {
          ElMessage.success(t('sync_maintenance.draft_saved'))
          emits('saved', deepCopy(state.form))
        }
        resetForm()
        dialogVisible.value = false
      }
    })
  }
}

/**
 * 根据目标类型决定长度是否有效，维护模式只展示 CHAR、VARCHAR 和 DECIMAL 的容量参数
 * STRING 与 VARCHAR 不同，不提供可调整的长度输入
 */
const showFieldSize = field => {
  if (
    props.adjustmentMode &&
    !['CHAR', 'VARCHAR', 'DECIMAL'].includes(normalizeFieldType(field.fieldType))
  )
    return false
  if (field.fieldType?.endsWith('_ARRAY')) {
    return false
  }
  switch (field.fieldType) {
    case 'STRING':
    case 'BOOLEAN':
    case 'MAP':
    case 'BITMAP':
    case 'HLL':
    case 'ARRAY':
    case 'STRUCT':
    case 'QUANTILE_STATE':
    case 'JSON':
    case 'JSONB':
    case 'AGG_STATE':
    case 'DATE':
    case 'DATEV2':
    case 'DATETIME':
    case 'DATETIMEV2':
    case 'FLOAT':
    case 'DOUBLE':
      return false
    default:
  }
  return true
}

/**
 * 日期时间精度和小数精度按有效类型分别展示，避免将源字段的无关精度带入字符串目标
 * @param fieldType
 */
const showFieldPrecision = (fieldType: string) => {
  switch (fieldType) {
    case 'DATETIME':
    case 'DATETIMEV2':
    case 'DECIMAL':
    case 'DECIMALV3':
      return true
  }
  return false
}

const targetFieldFormLoading = ref(false)
defineExpose({
  showDialog,
  editId,
  targetFieldFormLoading
})
</script>

<template>
  <el-drawer
    :title="editId === '' ? t('sync_task.add_field') : t('sync_task.edit_field')"
    v-model="dialogVisible"
    modal-class="target-field-drawer"
    size="min(600px, 100vw)"
    direction="rtl"
  >
    <template #default>
      <div class="target-body">
        <el-form
          ref="fieldForm"
          require-asterisk-position="right"
          :model="state.form"
          :rules="rules"
          label-width="80px"
          label-position="top"
          v-loading="targetFieldFormLoading"
        >
          <el-form-item
            :label="t('sync_task.source_field')"
            prop="fieldSource"
            :error="fieldError('fieldSource')"
            :show-message="!fieldError('fieldSource')"
            :class="{ 'source-field-item': adjustmentMode }"
          >
            <el-select
              v-model="state.form.fieldSource"
              :filterable="true"
              :placeholder="t('sync_task.please_choose')"
              @change="changeFieldSource"
            >
              <el-option
                v-for="item in form.source.fieldList"
                :key="item.fieldName"
                :label="
                  adjustmentMode
                    ? `${item.fieldName} · ${
                        item.fieldSourceType ||
                        item.fieldSourceStandardType ||
                        t('sync_maintenance.type_unavailable')
                      }`
                    : item.fieldName
                "
                :value="item.fieldName"
                :disabled="
                  adjustmentMode &&
                  form.target.fieldList.some(
                    field => field.id !== editId && field.fieldSource === item.fieldName
                  )
                "
              >
              </el-option>
            </el-select>
            <!-- 服务端错误独立呈现，避免输入回填触发的本地校验将其清除 -->
            <p v-if="fieldError('fieldSource')" class="field-server-error">
              {{ fieldError('fieldSource') }}
            </p>
          </el-form-item>
          <p v-if="adjustmentMode" class="source-type-summary">
            {{ t('sync_task.source_field_type') }}：{{ sourceFieldType }}
          </p>
          <el-form-item v-else :label="t('sync_task.source_field_type')">
            <el-input :model-value="sourceFieldType" readonly disabled />
          </el-form-item>
          <el-form-item
            :label="t('sync_task.name')"
            prop="fieldName"
            :error="fieldError('fieldName')"
            :show-message="!fieldError('fieldName')"
          >
            <div class="target-name-input">
              <el-input
                v-model="state.form.fieldName"
                :maxlength="nameMaxLength"
                :placeholder="t('sync_task.please_enter')"
              ></el-input>
              <el-button
                v-if="
                  adjustmentMode &&
                  state.form.fieldSource &&
                  state.form.fieldName !== state.form.fieldSource
                "
                @click="state.form.fieldName = state.form.fieldSource"
                >{{ t('sync_maintenance.use_source_name') }}</el-button
              >
            </div>
            <p v-if="adjustmentMode && nameMaxLength" class="field-help">
              {{ t('sync_maintenance.name_help', [nameMaxLength]) }}
            </p>
            <p v-if="fieldError('fieldName')" class="field-server-error">
              {{ fieldError('fieldName') }}
            </p>
          </el-form-item>
          <el-form-item
            :label="t('sync_task.field_type')"
            prop="fieldType"
            :error="fieldError('fieldType')"
            :show-message="!fieldError('fieldType')"
          >
            <el-alert
              v-if="fieldMappingMessage"
              :title="fieldMappingMessage"
              :type="fieldMappingMessageType"
              :closable="false"
              show-icon
              class="field-mapping-alert"
            />
            <el-select
              v-model="targetFieldType"
              @change="changeTargetType"
              :filterable="true"
              :placeholder="t('sync_task.please_choose')"
            >
              <el-option
                v-if="
                  adjustmentMode &&
                  targetFieldType &&
                  !availableTargetFieldTypeList.includes(targetFieldType)
                "
                :label="fieldTypeLabel(targetFieldType)"
                :value="targetFieldType"
                disabled
              />
              <el-option
                v-for="(item, index) in availableTargetFieldTypeList"
                :key="index"
                :label="fieldTypeLabel(item)"
                :value="item"
                :disabled="typeDisabled(item)"
                :title="typeRule(item)?.reason"
              >
                <span>{{ fieldTypeLabel(item) }}</span>
                <span
                  v-if="
                    adjustmentMode && (typeDisabled(item) || typeRule(item)?.level === 'WARNING')
                  "
                  class="type-option-reason"
                >
                  {{
                    t(
                      typeDisabled(item)
                        ? 'sync_maintenance.conversion_unavailable'
                        : 'sync_maintenance.conversion_risk'
                    )
                  }}
                </span>
              </el-option>
            </el-select>
            <div v-if="adjustmentMode && recommendedTargetField" class="type-recommendation">
              <span>{{ t('sync_maintenance.recommended_type', [recommendedTypeLabel]) }}</span>
              <el-button
                v-if="recommendationDiffers"
                link
                type="primary"
                :disabled="readOnly || !!recommendationBlockedReason"
                :title="recommendationBlockedReason"
                @click="useRecommendedType"
                >{{ t('sync_maintenance.use_recommended_type') }}</el-button
              >
            </div>
            <p
              v-else-if="adjustmentMode && state.form.fieldSource && !fieldMappingMessage"
              class="field-help"
            >
              {{ t('sync_maintenance.no_recommended_type') }}
            </p>
            <p v-if="adjustmentMode && recommendationBlockedReason" class="field-help">
              {{ recommendationBlockedReason }}
            </p>
            <el-alert
              v-if="conversionWarning"
              :title="conversionWarning"
              type="warning"
              :closable="false"
              show-icon
              class="conversion-warning"
            />
            <p v-if="adjustmentMode" class="field-help">
              {{
                t(editId ? 'sync_maintenance.existing_type_help' : 'sync_maintenance.new_type_help')
              }}
            </p>
            <p v-if="fieldError('fieldType')" class="field-server-error">
              {{ fieldError('fieldType') }}
            </p>
          </el-form-item>
          <el-form-item
            :label="t('sync_task.field_comment')"
            prop="remarks"
            :error="fieldError('remarks')"
            :show-message="!fieldError('remarks')"
          >
            <el-input
              v-model="state.form.remarks"
              :maxlength="commentMaxLength"
              :placeholder="t('sync_task.please_enter')"
            ></el-input>
            <p v-if="fieldError('remarks')" class="field-server-error">
              {{ fieldError('remarks') }}
            </p>
          </el-form-item>
          <el-form-item
            :label="t('sync_task.field_length')"
            prop="fieldSize"
            :error="fieldError('fieldSize')"
            :show-message="!fieldError('fieldSize')"
            v-if="showFieldSize(state.form)"
          >
            <div
              v-if="adjustmentMode && state.form.fieldType?.startsWith('DECIMAL')"
              class="decimal-help"
            >
              {{ t('sync_maintenance.decimal_help') }}
            </div>
            <el-input-number
              v-model="state.form.fieldSize"
              controls-position="right"
              autocomplete="off"
              type="number"
              @mousewheel.prevent
              :min="adjustmentMode ? minimumSize : 0"
              :max="sizeMax"
            />
            <p v-if="adjustmentMode && sizeMax" class="field-help">
              {{ t('sync_maintenance.size_range', [minimumSize, sizeMax]) }}
            </p>
            <p v-if="fieldError('fieldSize')" class="field-server-error">
              {{ fieldError('fieldSize') }}
            </p>
          </el-form-item>
          <el-form-item
            :label="t('sync_task.field_precision')"
            prop="fieldPrecision"
            :error="fieldError('fieldPrecision')"
            :show-message="!fieldError('fieldPrecision')"
            v-if="showFieldPrecision(state.form.fieldType)"
          >
            <el-input-number
              v-model="state.form.fieldPrecision"
              controls-position="right"
              autocomplete="off"
              type="number"
              @mousewheel.prevent
              :min="0"
              :max="precisionMax"
            />
            <p v-if="fieldError('fieldPrecision')" class="field-server-error">
              {{ fieldError('fieldPrecision') }}
            </p>
          </el-form-item>
          <el-form-item v-if="!adjustmentMode" prop="fieldPk">
            <el-checkbox
              v-model="state.form.fieldPk"
              :disabled="isFieldOptionDisabled(state.form, 'fieldPk')"
              :title="fieldOptionHint(state.form, 'fieldPk')"
              :label="t('sync_task.field_key')"
            />
          </el-form-item>
          <el-form-item
            v-if="!adjustmentMode || maintenanceStructure?.indexType"
            prop="fieldIndex"
            :error="fieldError('fieldIndex')"
            :show-message="!fieldError('fieldIndex')"
          >
            <el-checkbox
              v-model="state.form.fieldIndex"
              :label="indexLabel"
              :disabled="isFieldOptionDisabled(state.form, 'fieldIndex')"
              :title="fieldOptionHint(state.form, 'fieldIndex')"
            />
            <p v-if="fieldError('fieldIndex')" class="field-server-error">
              {{ fieldError('fieldIndex') }}
            </p>
          </el-form-item>
        </el-form>
      </div>
    </template>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm"> {{ t('sync_datasource.cancel') }}</el-button>
        <el-button type="primary" @click="saveForm">
          {{ t(adjustmentMode ? 'sync_maintenance.confirm_field' : 'sync_datasource.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>

<style lang="less">
.target-field-drawer {
  .target-name-input {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    width: 100%;
    .ed-input {
      min-width: min(180px, 100%);
      flex: 1;
    }
    .ed-button {
      flex-shrink: 0;
    }
  }
  .source-field-item {
    margin-bottom: 8px;
  }
  .source-type-summary {
    margin: 0 0 24px;
    color: var(--ed-text-color-secondary);
    font-size: 12px;
    line-height: 20px;
    overflow-wrap: anywhere;
  }
  .conversion-warning {
    margin-top: 12px;
  }
  .type-recommendation {
    display: flex;
    align-items: baseline;
    flex-wrap: wrap;
    gap: 8px 16px;
    width: 100%;
    margin-top: 8px;
    color: var(--ed-text-color-secondary);
    font-size: 12px;
    line-height: 20px;
  }
  .field-help {
    width: 100%;
    margin: 8px 0 0;
    color: var(--ed-text-color-secondary);
    font-size: 12px;
    line-height: 20px;
  }
  .field-server-error {
    width: 100%;
    margin: 8px 0 0;
    color: var(--ed-color-danger);
    font-size: 12px;
    line-height: 20px;
    overflow-wrap: anywhere;
  }
  .decimal-help {
    width: 100%;
    margin-bottom: 8px;
    line-height: 20px;
  }
  .ed-alert__title {
    line-height: 22px;
    overflow-wrap: anywhere;
  }
  .ed-drawer__body {
    min-height: 0;
    overflow-y: auto;
    padding: 16px 24px 80px !important;

    .target-body {
      padding-bottom: 8px;

      .ed-checkbox.ed-checkbox--default {
        align-items: flex-start;
        height: auto;
        .ed-checkbox__input {
          margin-top: 4px;
        }
        .ed-checkbox__label {
          white-space: normal;
          line-height: 22px;
        }
      }

      .field-mapping-alert {
        margin-bottom: 12px;
      }
    }
  }
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    flex-wrap: wrap;
    gap: 12px;
    .ed-button + .ed-button {
      margin-left: 0;
    }
  }
}
.type-option-reason {
  float: right;
  margin-left: 16px;
  font-size: 12px;
  color: var(--ed-text-color-secondary);
}
</style>
