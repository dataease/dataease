<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElInput, ElMessage } from 'element-plus-secondary'
import { CaretBottom, CaretRight, Plus } from '@element-plus/icons-vue'
import HandleMore from '@/components/handle-more/src/HandleMore.vue'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import Icon from '@/components/icon-custom/src/Icon.vue'
import deleteIcon from '@/assets/svg/icon_delete-trash_outlined.svg'
import renameIcon from '@/assets/svg/icon_rename_outlined.svg'
import moreIcon from '@/assets/svg/icon_more-vertical_outlined.svg'
import infoIcon from '@/assets/svg/icon_info_outlined.svg'
import filterIcon from '@/assets/svg/filter-params.svg'
import datasetIcon from '@/assets/svg/icon_dataset.svg'
import detailIcon from '@/assets/svg/table-info.svg'
import pivotIcon from '@/assets/svg/table-pivot.svg'
import { iconFieldMap } from '@/components/icon-group/field-list'
import { fieldType } from '@/utils/attr'
import { presentError } from '@/config/axios/errorPresenter'
import { useI18n } from '@/hooks/web/useI18n'
import { findOuterParamsSnapshot, saveOuterParams } from '../api/outer-params'
import type {
  SpreadsheetOuterParam,
  SpreadsheetOuterParamsConfig,
  SpreadsheetOuterParamsContext,
  SpreadsheetOuterParamsTarget
} from '../types/outer-params'

const props = defineProps<{
  modelValue: boolean
  spreadsheetId: string
  context: SpreadsheetOuterParamsContext
}>()
const emit = defineEmits<{
  'update:modelValue': [visible: boolean]
  saved: [config: SpreadsheetOuterParamsConfig]
}>()
const { t } = useI18n()
const draft = ref<SpreadsheetOuterParamsConfig>()
const loading = ref(false)
const saving = ref(false)
const loadFailed = ref(false)
const activeIndex = ref(0)
const editingIndex = ref<number | null>(null)
const paramNameInput = ref<InstanceType<typeof ElInput>>()
let requestVersion = 0
let loadController: AbortController | undefined

const visible = computed({
  get: () => props.modelValue,
  set: value => {
    if (!saving.value) emit('update:modelValue', value)
  }
})
const activeParam = computed(() => draft.value?.outerParamsInfoArray[activeIndex.value])
// 树节点序号仅用于界面选择，不写入服务端生成的参数 ID。
const paramNodes = computed(() =>
  (draft.value?.outerParamsInfoArray || []).map((param, index) => ({
    index,
    param,
    label: param.paramName || t('visualization.no_setting_params_name_tip')
  }))
)
const paramMenu = computed(() => [
  {
    label: t('visualization.rename'),
    svgName: renameIcon,
    command: 'rename',
    disabled: saving.value
  },
  {
    label: t('visualization.delete'),
    svgName: deleteIcon,
    command: 'delete',
    disabled: saving.value
  }
])
const conditions = computed(() => new Map(props.context.conditions.map(item => [item.id, item])))
const dataAreas = computed(() => new Map(props.context.dataAreas.map(item => [item.id, item])))

const loadConfig = async () => {
  const version = ++requestVersion
  loadController?.abort()
  draft.value = undefined
  loadFailed.value = false
  loading.value = false
  saving.value = false
  activeIndex.value = 0
  editingIndex.value = null
  if (!props.modelValue || !props.spreadsheetId) return
  const controller = new AbortController()
  loadController = controller
  loading.value = true
  try {
    const config = await findOuterParamsSnapshot(props.spreadsheetId, controller.signal)
    // 关闭、切换资源或重新打开后，旧请求不能覆盖当前编辑草稿。
    if (version !== requestVersion) return
    draft.value = config
  } catch (error) {
    if (controller.signal.aborted || version !== requestVersion) return
    loadFailed.value = true
    presentError(error, t('spreadsheet.outer_params_load_error'))
  } finally {
    if (version === requestVersion) loading.value = false
  }
}

watch(() => [props.modelValue, props.spreadsheetId], loadConfig, { immediate: true })
onBeforeUnmount(() => {
  requestVersion++
  loadController?.abort()
})

const allowsFilter = (target: SpreadsheetOuterParamsTarget) => {
  const condition = conditions.value.get(target.targetRefId)
  return condition && ['textSelect', 'numberSelect', 'treeSelect'].includes(condition.displayType)
}
const targetExists = (target: SpreadsheetOuterParamsTarget) => {
  if (target.targetType === 'query') return conditions.value.has(target.targetRefId)
  return dataAreas.value.has(target.targetRefId)
}
const targetFields = (target: SpreadsheetOuterParamsTarget) => {
  return dataAreas.value.get(target.targetRefId)?.fields || []
}
const fieldExists = (target: SpreadsheetOuterParamsTarget) => {
  return targetFields(target).some(field => field.id === target.targetFieldId)
}
const targetKey = (target: SpreadsheetOuterParamsTarget) => {
  const detail = target.targetType === 'query' ? target.matchMode : target.targetFieldId
  return JSON.stringify([target.targetType, target.targetRefId, detail])
}
const targetCounts = computed(() => {
  const counts = new Map<string, number>()
  draft.value?.outerParamsInfoArray.forEach(param => {
    param.targetList.forEach(target => {
      if (!target.targetRefId) return
      const key = targetKey(target)
      counts.set(key, (counts.get(key) || 0) + 1)
    })
  })
  return counts
})
const targetError = (target: SpreadsheetOuterParamsTarget) => {
  if (!targetExists(target)) return t('spreadsheet.outer_params_invalid_target')
  if (target.targetType === 'query') {
    if (
      (target.matchMode !== 'self' && target.matchMode !== 'filter') ||
      target.targetFieldId !== null ||
      (target.matchMode === 'filter' && !allowsFilter(target))
    ) {
      return t('spreadsheet.outer_params_invalid_target')
    }
  } else if (!fieldExists(target) || target.matchMode !== null) {
    return t('spreadsheet.outer_params_invalid_target')
  }
  if ((targetCounts.value.get(targetKey(target)) || 0) > 1) {
    return t('spreadsheet.outer_params_duplicate_target')
  }
  return ''
}
const nameError = (param: SpreadsheetOuterParam) => {
  const name = param.paramName.trim()
  const normalizedName = name.toLowerCase()
  const matches = draft.value?.outerParamsInfoArray.filter(
    item => item.paramName.trim().toLowerCase() === normalizedName
  )
  if (!name || name.length > 255 || (matches?.length || 0) > 1) {
    return t('spreadsheet.outer_params_invalid_name')
  }
  return ''
}
const defaultError = (param: SpreadsheetOuterParam) => {
  const value = param.defaultValue?.trim()
  if (!value && !param.enabledDefault) return ''
  try {
    const defaults = value ? JSON.parse(value) : undefined
    if (Array.isArray(defaults)) {
      // 保存时排除运行时无法解析的对象和嵌套数组，避免发布后才报错。
      const valid = defaults.every((item: unknown) => {
        if (typeof item === 'number') return Number.isFinite(item)
        return item === null || typeof item === 'string' || typeof item === 'boolean'
      })
      if (valid) return ''
    }
  } catch {
    // 默认值沿用看板的 JSON 数组契约，不在这里解释目标字段类型。
  }
  return t('spreadsheet.outer_params_invalid_default')
}
const paramInvalid = (param: SpreadsheetOuterParam) => {
  return !!nameError(param) || !!defaultError(param) || param.targetList.some(targetError)
}
const invalid = computed(() => draft.value?.outerParamsInfoArray.some(paramInvalid) || false)

const selectParam = (index: number) => {
  if (!saving.value) activeIndex.value = index
}
const renameParam = async (index: number) => {
  if (saving.value) return
  activeIndex.value = index
  editingIndex.value = index
  await nextTick()
  paramNameInput.value?.focus()
}
const addParam = () => {
  if (!draft.value || saving.value) return
  draft.value.outerParamsInfoArray.push({
    paramName: '',
    checked: true,
    required: false,
    enabledDefault: false,
    defaultValue: null,
    targetList: []
  })
  renameParam(draft.value.outerParamsInfoArray.length - 1)
}
const removeParam = (index: number) => {
  if (saving.value) return
  editingIndex.value = null
  draft.value?.outerParamsInfoArray.splice(index, 1)
  if (activeIndex.value >= index) activeIndex.value = Math.max(0, activeIndex.value - 1)
}
const handleParamCommand = (command: string | number | object, index: number) => {
  if (command === 'rename') renameParam(index)
  if (command === 'delete') removeParam(index)
}

type DataArea = SpreadsheetOuterParamsContext['dataAreas'][number]
interface FieldBindingRow {
  key: number
  fieldId: string | null
  expanded: boolean
  targets: SpreadsheetOuterParamsTarget[]
}
interface DatasetBindingGroup {
  key: string
  name: string
  areas: DataArea[]
  fields: DataArea['fields']
  rows: FieldBindingRow[]
}
const queryRows = ref<Array<{ key: number; target: SpreadsheetOuterParamsTarget }>>([])
const datasetGroups = ref<DatasetBindingGroup[]>([])
const bindingStates = new WeakMap<
  SpreadsheetOuterParam,
  {
    queries: Array<{ key: number; target: SpreadsheetOuterParamsTarget }>
    datasets: DatasetBindingGroup[]
  }
>()
const queryExpanded = ref(true)
const datasetsExpanded = ref(true)
let bindingRowKey = 0

const areaGroupKey = (area: DataArea) => {
  if (area.datasetId) return `dataset:${area.datasetId}`
  return `area:${area.id}`
}
const createFieldRow = (fieldId: string | null = null): FieldBindingRow => ({
  key: bindingRowKey++,
  fieldId,
  expanded: true,
  targets: []
})
watch(
  activeParam,
  param => {
    queryRows.value = []
    datasetGroups.value = []
    if (!param) return
    // 保留尚未选择条件或勾选区域的界面状态，但不把空绑定写入配置。
    const previous = bindingStates.get(param)
    if (previous) {
      queryRows.value = previous.queries
      datasetGroups.value = previous.datasets
      return
    }
    const groups = new Map<string, DatasetBindingGroup>()
    props.context.dataAreas.forEach(area => {
      const key = areaGroupKey(area)
      let group = groups.get(key)
      if (!group) {
        group = { key, name: area.datasetName || area.name, areas: [], fields: [], rows: [] }
        groups.set(key, group)
      }
      group.areas.push(area)
      area.fields.forEach(field => {
        if (!group.fields.some(item => item.id === field.id)) group.fields.push(field)
      })
    })
    // 只生成展示分组，不在加载时压缩多绑定或删除失效目标。
    param.targetList.forEach(target => {
      if (target.targetType === 'query') {
        queryRows.value.push({ key: bindingRowKey++, target })
        return
      }
      const area = dataAreas.value.get(target.targetRefId)
      const key = area ? areaGroupKey(area) : `missing:${target.targetRefId}`
      let group = groups.get(key)
      if (!group) {
        group = {
          key,
          name: t('spreadsheet.outer_params_invalid_target'),
          areas: [],
          fields: [],
          rows: []
        }
        groups.set(key, group)
      }
      let row = group.rows.find(item => item.fieldId === target.targetFieldId)
      if (!row) {
        row = createFieldRow(target.targetFieldId)
        group.rows.push(row)
      }
      row.targets.push(target)
    })
    if (!queryRows.value.length && props.context.conditions.length) {
      queryRows.value.push({
        key: bindingRowKey++,
        target: { targetType: 'query', targetRefId: '', targetFieldId: null, matchMode: 'self' }
      })
    }
    groups.forEach(group => {
      if (!group.rows.length) group.rows.push(createFieldRow())
    })
    datasetGroups.value = [...groups.values()]
    bindingStates.set(param, { queries: queryRows.value, datasets: datasetGroups.value })
  },
  { immediate: true }
)

const removeTargets = (targets: SpreadsheetOuterParamsTarget[]) => {
  if (!activeParam.value) return
  const removed = new Set(targets)
  activeParam.value.targetList = activeParam.value.targetList.filter(target => !removed.has(target))
}
const queryRowError = (target: SpreadsheetOuterParamsTarget) => {
  if (!activeParam.value?.targetList.includes(target)) return ''
  return targetError(target)
}
const changeQueryCondition = (target: SpreadsheetOuterParamsTarget) => {
  if (!activeParam.value || saving.value) return
  if (!target.targetRefId) {
    target.targetRefId = ''
    removeTargets([target])
    delete target.targetId
    return
  }
  target.targetFieldId = null
  if (!activeParam.value.targetList.includes(target)) activeParam.value.targetList.push(target)
}
const changeQueryMode = (target: SpreadsheetOuterParamsTarget) => {
  const invalidMode = target.matchMode !== 'self' && target.matchMode !== 'filter'
  if (invalidMode) target.matchMode = null
  if (invalidMode || (target.matchMode === 'filter' && !allowsFilter(target))) {
    // 与看板一致：切为候选过滤时，不支持的查询条件需要重新选择。
    target.targetRefId = ''
  }
  changeQueryCondition(target)
}
const conditionDisabled = (
  target: SpreadsheetOuterParamsTarget,
  condition: SpreadsheetOuterParamsContext['conditions'][number]
) => {
  if (target.matchMode === 'self') return false
  if (target.matchMode !== 'filter') return true
  return !['textSelect', 'numberSelect', 'treeSelect'].includes(condition.displayType)
}
const areaSupportsField = (area: DataArea, row: FieldBindingRow) => {
  return !!row.fieldId && area.fields.some(field => field.id === row.fieldId)
}
const areaChecked = (row: FieldBindingRow, areaId: string) => {
  return row.targets.some(target => target.targetRefId === areaId)
}
const changeField = (row: FieldBindingRow) => {
  if (saving.value) return
  if (!row.fieldId) {
    removeTargets(row.targets)
    row.targets = []
    return
  }
  // 改字段保留绑定 ID；不兼容的已选区域留在界面报错，必须由用户取消。
  row.targets.forEach(target => {
    target.targetFieldId = row.fieldId
    target.matchMode = null
  })
}
const changeAreaChecked = (row: FieldBindingRow, area: DataArea, checked: boolean) => {
  if (!activeParam.value || saving.value) return
  if (!checked) {
    const removed = row.targets.filter(target => target.targetRefId === area.id)
    removeTargets(removed)
    row.targets = row.targets.filter(target => target.targetRefId !== area.id)
    return
  }
  if (!areaSupportsField(area, row) || areaChecked(row, area.id)) return
  const target: SpreadsheetOuterParamsTarget = {
    targetType: 'field',
    targetRefId: area.id,
    targetFieldId: row.fieldId,
    matchMode: null
  }
  row.targets.push(target)
  activeParam.value.targetList.push(target)
}
const eligibleAreas = (group: DatasetBindingGroup, row: FieldBindingRow) => {
  return group.areas.filter(area => areaSupportsField(area, row))
}
const allAreasChecked = (group: DatasetBindingGroup, row: FieldBindingRow) => {
  const areas = eligibleAreas(group, row)
  return areas.length > 0 && areas.every(area => areaChecked(row, area.id))
}
const someAreasChecked = (group: DatasetBindingGroup, row: FieldBindingRow) => {
  const areas = eligibleAreas(group, row)
  return areas.some(area => areaChecked(row, area.id)) && !allAreasChecked(group, row)
}
const changeAllAreas = (group: DatasetBindingGroup, row: FieldBindingRow, checked: boolean) => {
  eligibleAreas(group, row).forEach(area => changeAreaChecked(row, area, checked))
}
const removeMissingTarget = (row: FieldBindingRow, target: SpreadsheetOuterParamsTarget) => {
  if (saving.value) return
  removeTargets([target])
  row.targets = row.targets.filter(item => item !== target)
}
const fieldRowErrors = (row: FieldBindingRow) => {
  return [...new Set(row.targets.map(targetError).filter(Boolean))]
}
const fieldLabel = (group: DatasetBindingGroup, field: DataArea['fields'][number]) => {
  if (!group.fields.some(item => item.id !== field.id && item.name === field.name))
    return field.name
  const owners = group.areas.filter(area => area.fields.some(item => item.id === field.id))
  return `${field.name} (${owners.map(area => area.name).join(', ')})`
}
const fieldIconType = (deType?: number) => {
  const type = fieldType[deType ?? 0] as keyof typeof iconFieldMap
  return iconFieldMap[type] ? type : 'text'
}
const save = async () => {
  if (!draft.value || loading.value || saving.value || invalid.value) return
  const version = requestVersion
  const payload: SpreadsheetOuterParamsConfig = {
    spreadsheetId: props.spreadsheetId,
    checked: draft.value.checked,
    outerParamsInfoArray: draft.value.outerParamsInfoArray.map(param => ({
      ...param,
      paramName: param.paramName.trim(),
      defaultValue: param.defaultValue?.trim() || null,
      targetList: param.targetList.map(target => ({ ...target }))
    }))
  }
  saving.value = true
  try {
    const config = await saveOuterParams(payload)
    if (version !== requestVersion) return
    emit('saved', config)
    emit('update:modelValue', false)
    ElMessage.success(t('common.save_success'))
  } catch (error) {
    if (version === requestVersion) presentError(error, t('spreadsheet.outer_params_save_error'))
  } finally {
    if (version === requestVersion) saving.value = false
  }
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="t('visualization.outer_param_set')"
    width="80vw"
    top="10vh"
    class="spreadsheet-outer-params-dialog"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    :close-on-press-escape="!saving"
    :show-close="!saving"
  >
    <div class="outer-params-content">
      <div v-loading="loading || saving" class="preview">
        <div v-if="loadFailed" class="load-error">
          <el-alert
            type="error"
            :closable="false"
            :title="t('spreadsheet.outer_params_load_error')"
          />
          <el-button class="retry-button" @click="loadConfig">
            {{ t('spreadsheet.retry') }}
          </el-button>
        </div>
        <el-row v-else-if="draft" class="preview-columns">
          <el-col :span="6" class="preview-left">
            <div class="tree-head">
              <span class="head-text">{{ t('visualization.params_list') }}</span>
              <el-button
                type="primary"
                :icon="Plus"
                text
                :disabled="saving"
                :aria-label="t('common.add')"
                @click="addParam"
              />
            </div>
            <el-tree
              class="custom-tree"
              menu
              :data="paramNodes"
              node-key="index"
              :current-node-key="activeIndex"
              highlight-current
              @node-click="data => selectParam(data.index)"
            >
              <template #default="{ data }">
                <span class="custom-tree-node" :class="{ invalid: paramInvalid(data.param) }">
                  <el-input
                    v-if="editingIndex === data.index"
                    ref="paramNameInput"
                    v-model="data.param.paramName"
                    class="param-name-input"
                    :maxlength="255"
                    :disabled="saving"
                    :placeholder="t('visualization.input_param_name')"
                    :aria-label="t('common.name')"
                    @click.stop
                    @keydown.stop
                    @keydown.enter.prevent="editingIndex = null"
                    @blur="editingIndex = null"
                  />
                  <template v-else>
                    <span class="auth-span" @click.stop>
                      <el-checkbox
                        v-model="data.param.checked"
                        :disabled="saving"
                        :aria-label="t('commons.enable')"
                      />
                    </span>
                    <span
                      class="tree-select-field"
                      :title="nameError(data.param) || data.param.paramName"
                      @dblclick.stop="renameParam(data.index)"
                    >
                      {{ data.label }}
                    </span>
                    <handle-more
                      class="icon-more"
                      :menu-list="paramMenu"
                      :icon-name="moreIcon"
                      placement="bottom-start"
                      @handle-command="command => handleParamCommand(command, data.index)"
                    />
                  </template>
                </span>
              </template>
            </el-tree>
            <div v-if="activeParam && nameError(activeParam)" class="param-name-error">
              {{ nameError(activeParam) }}
            </div>
          </el-col>
          <el-col :span="13" class="preview-show">
            <template v-if="activeParam">
              <div class="new-params-title">
                {{ t('visualization.select_params_connect_component') }}
              </div>
              <el-form class="binding-content" :disabled="saving">
                <div v-if="queryRows.length" class="query-section">
                  <div class="query-grid section-header">
                    <button
                      type="button"
                      class="expand-toggle"
                      :aria-expanded="queryExpanded"
                      :aria-label="t('visualization.filter_component')"
                      @click="queryExpanded = !queryExpanded"
                    >
                      <el-icon><CaretBottom v-if="queryExpanded" /><CaretRight v-else /></el-icon>
                    </button>
                    <span>{{ t('visualization.filter_component') }}</span>
                    <span class="query-mode-heading">
                      {{ t('visualization.outer_params_type') }}
                      <el-tooltip
                        :content="t('spreadsheet.outer_params_filter_tips')"
                        placement="bottom"
                      >
                        <el-icon class="hint-icon">
                          <Icon><component :is="infoIcon" class="svg-icon" /></Icon>
                        </el-icon>
                      </el-tooltip>
                    </span>
                    <span>{{ t('visualization.connection_condition') }}</span>
                  </div>
                  <div
                    v-for="row in queryRows"
                    v-show="queryExpanded"
                    :key="row.key"
                    class="query-grid query-binding-row"
                  >
                    <span />
                    <span class="target-name" :title="t('visualization.filter_component')">
                      <Icon><component :is="filterIcon" class="svg-icon target-icon" /></Icon>
                      <span class="ellipsis">{{ t('visualization.filter_component') }}</span>
                    </span>
                    <el-select
                      v-model="row.target.matchMode"
                      clearable
                      :placeholder="t('v_query.select_query_condition')"
                      :aria-label="t('visualization.outer_params_type')"
                      @change="changeQueryMode(row.target)"
                    >
                      <el-option value="self" :label="t('visualization.outer_params_type_self')" />
                      <el-option
                        value="filter"
                        :label="t('visualization.outer_params_type_filter')"
                      />
                    </el-select>
                    <el-select
                      v-model="row.target.targetRefId"
                      filterable
                      clearable
                      :placeholder="t('v_query.select_query_condition')"
                      @change="changeQueryCondition(row.target)"
                    >
                      <el-option
                        v-if="row.target.targetRefId && !targetExists(row.target)"
                        :value="row.target.targetRefId"
                        :label="t('spreadsheet.outer_params_invalid_target')"
                        disabled
                      />
                      <el-option
                        v-for="condition in context.conditions"
                        :key="condition.id"
                        :value="condition.id"
                        :label="condition.name"
                        :disabled="conditionDisabled(row.target, condition)"
                      />
                    </el-select>
                    <div v-if="queryRowError(row.target)" class="binding-error query-error">
                      {{ queryRowError(row.target) }}
                    </div>
                  </div>
                </div>
                <div v-if="datasetGroups.length" class="dataset-section">
                  <div class="dataset-grid section-header">
                    <button
                      type="button"
                      class="expand-toggle"
                      :aria-expanded="datasetsExpanded"
                      :aria-label="t('visualization.dataset')"
                      @click="datasetsExpanded = !datasetsExpanded"
                    >
                      <el-icon
                        ><CaretBottom v-if="datasetsExpanded" /><CaretRight v-else
                      /></el-icon>
                    </button>
                    <span>{{ t('visualization.dataset') }}</span>
                    <span>{{ t('visualization.fields') }}</span>
                  </div>
                  <div v-show="datasetsExpanded">
                    <div v-for="group in datasetGroups" :key="group.key">
                      <div v-for="row in group.rows" :key="row.key" class="dataset-binding-row">
                        <div class="dataset-grid">
                          <button
                            type="button"
                            class="expand-toggle"
                            :aria-expanded="row.expanded"
                            :aria-label="group.name"
                            @click="row.expanded = !row.expanded"
                          >
                            <el-icon
                              ><CaretBottom v-if="row.expanded" /><CaretRight v-else
                            /></el-icon>
                          </button>
                          <span class="target-name" :title="group.name">
                            <Icon><component :is="datasetIcon" class="svg-icon" /></Icon>
                            <span class="ellipsis">{{ group.name }}</span>
                          </span>
                          <el-select
                            v-model="row.fieldId"
                            filterable
                            clearable
                            :placeholder="t('common.selectText')"
                            :aria-label="t('visualization.fields')"
                            @change="changeField(row)"
                          >
                            <el-option
                              v-if="
                                row.fieldId && !group.fields.some(field => field.id === row.fieldId)
                              "
                              :value="row.fieldId"
                              :label="t('spreadsheet.outer_params_invalid_target')"
                              disabled
                            />
                            <el-option
                              v-for="field in group.fields"
                              :key="field.id"
                              :value="field.id"
                              :label="fieldLabel(group, field)"
                            >
                              <Icon>
                                <!-- 下拉弹层挂到 body，图标尺寸不能依赖弹窗内的后代样式。 -->
                                <component
                                  :is="iconFieldMap[fieldIconType(field.deType)]"
                                  class="svg-icon"
                                  :class="`field-icon-${fieldIconType(field.deType)}`"
                                  style="width: 14px; height: 14px; margin-right: 4px"
                                />
                              </Icon>
                              <span>{{ fieldLabel(group, field) }}</span>
                            </el-option>
                          </el-select>
                        </div>
                        <div
                          v-for="error in fieldRowErrors(row)"
                          :key="error"
                          class="binding-error"
                        >
                          {{ error }}
                        </div>
                        <div v-show="row.expanded" class="data-area-content">
                          <div class="data-area-heading">
                            <span>{{ t('spreadsheet.outer_params_select_data_areas') }}</span>
                            <span class="heading-divider" />
                            <el-checkbox
                              :model-value="allAreasChecked(group, row)"
                              :indeterminate="someAreasChecked(group, row)"
                              :disabled="!eligibleAreas(group, row).length"
                              @change="checked => changeAllAreas(group, row, !!checked)"
                            >
                              {{ t('visualization.select_all') }}
                            </el-checkbox>
                          </div>
                          <div class="data-area-list">
                            <el-checkbox
                              v-for="area in group.areas"
                              :key="area.id"
                              class="data-area-item"
                              :class="{
                                'invalid-area':
                                  areaChecked(row, area.id) && !areaSupportsField(area, row)
                              }"
                              :model-value="areaChecked(row, area.id)"
                              :disabled="
                                !areaSupportsField(area, row) && !areaChecked(row, area.id)
                              "
                              @change="checked => changeAreaChecked(row, area, !!checked)"
                            >
                              <span class="target-name" :title="area.name">
                                <Icon>
                                  <component
                                    :is="area.pluginType === 'pivot' ? pivotIcon : detailIcon"
                                    class="svg-icon target-icon"
                                  />
                                </Icon>
                                <span class="ellipsis">{{ area.name }}</span>
                              </span>
                            </el-checkbox>
                            <el-checkbox
                              v-for="(target, index) in row.targets.filter(
                                item => !dataAreas.has(item.targetRefId)
                              )"
                              :key="target.targetId || index"
                              class="data-area-item invalid-area missing-area"
                              :model-value="true"
                              :title="target.targetRefId"
                              @change="removeMissingTarget(row, target)"
                            >
                              {{ t('spreadsheet.outer_params_invalid_target') }}
                            </el-checkbox>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-form>
            </template>
            <empty-background
              v-else
              :description="t('visualization.setting_params_tips')"
              img-type="noneWhite"
            />
          </el-col>
          <el-col :span="5" class="params-attach-setting">
            <template v-if="activeParam">
              <div class="new-params-title">{{ t('visualization.setting_params') }}</div>
              <el-form class="params-attach-content" :disabled="saving">
                <el-checkbox v-model="activeParam.required">
                  {{ t('visualization.required') }}
                </el-checkbox>
                <div class="default-value-setting">
                  <el-checkbox v-model="activeParam.enabledDefault">
                    {{ t('visualization.default_value') }}
                  </el-checkbox>
                  <el-tooltip placement="bottom">
                    <template #content>
                      {{ t('spreadsheet.outer_params_default_tips') }}
                    </template>
                    <el-icon class="hint-icon">
                      <Icon><component :is="infoIcon" class="svg-icon" /></Icon>
                    </el-icon>
                  </el-tooltip>
                </div>
                <el-form-item :error="defaultError(activeParam)">
                  <el-input
                    v-model="activeParam.defaultValue"
                    type="textarea"
                    :autosize="{ minRows: 4, maxRows: 8 }"
                    :placeholder="t('visualization.default_value_tips3')"
                  />
                </el-form-item>
              </el-form>
            </template>
          </el-col>
        </el-row>
      </div>
      <div class="root-class">
        <el-checkbox v-if="draft" v-model="draft.checked" :disabled="saving">
          {{ t('spreadsheet.outer_params_enable') }}
        </el-checkbox>
        <span v-if="invalid" class="validation-message">
          {{ t('spreadsheet.outer_params_fix_errors') }}
        </span>
        <div class="footer-actions">
          <el-button :disabled="saving" @click="visible = false">
            {{ t('commons.cancel') }}
          </el-button>
          <el-button
            type="primary"
            :loading="saving"
            :disabled="loading || !draft || invalid"
            @click="save"
          >
            {{ t('commons.confirm') }}
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<style lang="less">
.spreadsheet-outer-params-dialog {
  max-width: calc(100vw - 32px);

  .ed-dialog__header {
    padding: 20px 20px 0;
  }

  .ed-dialog__title {
    font-size: 14px;
  }

  .ed-dialog__body {
    padding: 10px 20px 20px;
  }

  .outer-params-content {
    height: 550px;
  }

  .preview {
    height: 470px;
    margin-top: 5px;
    overflow: hidden;
    border: 1px solid #e6e6e6;
    border-radius: 6px;
  }

  .preview-columns {
    height: 100%;
  }

  .preview-left,
  .preview-show,
  .params-attach-setting {
    min-width: 0;
    height: 100%;
  }

  .preview-left {
    display: flex;
    flex-direction: column;
    background: #f5f6f7;
  }

  .preview-show,
  .params-attach-setting {
    overflow-y: auto;
    border-left: 1px solid #e6e6e6;
  }

  .tree-head {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: space-between;
    height: 40px;
    padding: 0 16px;
    border-bottom: 1px solid #e6e6e6;
  }

  .head-text,
  .new-params-title {
    color: #1f2329;
    font-size: 14px;
    font-weight: 500;
  }

  .custom-tree {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    background: none;

    .ed-tree-node__expand-icon {
      display: none;
    }

    .ed-tree-node.is-current > .ed-tree-node__content {
      background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
    }
  }

  .custom-tree-node {
    display: flex;
    flex: 1;
    align-items: center;
    min-width: 0;
    font-size: 14px;

    &.invalid .tree-select-field {
      color: var(--ed-color-danger);
    }

    .icon-more {
      margin: 0 15px 0 8px;
      opacity: 0;
    }

    &:hover .icon-more,
    &:focus-within .icon-more {
      opacity: 1;
    }
  }

  .auth-span {
    display: flex;
    margin: 0 8px 0 16px;
  }

  .tree-select-field {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .param-name-input {
    margin: 0 15px;
  }

  .param-name-error {
    padding: 8px 16px;
  }

  .new-params-title {
    height: 56px;
    padding: 16px;
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  }

  .query-section,
  .dataset-section,
  .params-attach-content {
    padding: 16px;
  }

  .query-section {
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  }

  .binding-content .ed-select {
    min-width: 0;
    width: 100%;
  }

  .section-header {
    font-size: 14px;
  }

  .query-grid {
    display: grid;
    grid-template-columns: 16px minmax(80px, 0.75fr) minmax(100px, 1fr) minmax(120px, 1.2fr);
    align-items: center;
    gap: 8px;
  }

  .query-mode-heading {
    display: flex;
    align-items: center;
  }

  .query-binding-row,
  .dataset-binding-row {
    margin-top: 12px;
  }

  .query-error {
    grid-column: 2 / -1;
  }

  .dataset-grid {
    display: grid;
    grid-template-columns: 16px minmax(0, 1fr) minmax(0, 1fr);
    align-items: center;
    gap: 8px;
  }

  .expand-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 16px;
    padding: 0;
    color: #646a73;
    cursor: pointer;
    background: transparent;
    border: 0;
    border-radius: 6px;

    &:hover {
      background: rgba(31, 35, 41, 0.1);
    }
  }

  .target-name {
    display: flex;
    align-items: center;
    gap: 4px;
    min-width: 0;

    .svg-icon {
      flex-shrink: 0;
    }
  }

  .ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .target-icon {
    width: 22px;
    height: 14px;
    color: var(--ed-color-primary, #3370ff);
  }

  .data-area-content {
    margin: 8px 0 0 24px;
    padding: 12px;
    background: #f5f6f7;
    border-radius: 6px;
  }

  .data-area-heading {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #646a73;
    font-size: 14px;
    font-weight: 500;
  }

  .heading-divider {
    flex-shrink: 0;
    width: 1px;
    height: 14px;
    background: rgba(31, 35, 41, 0.15);
  }

  .data-area-list {
    display: flex;
    flex-wrap: wrap;
  }

  .data-area-item {
    width: 50%;
    min-width: 0;
    height: 32px;
    margin-right: 0;

    .ed-checkbox__label {
      flex: 1;
      min-width: 0;
      font-size: 12px;
    }
  }

  .invalid-area .ed-checkbox__label {
    color: var(--ed-color-danger);
  }

  .missing-area {
    width: 100%;
  }

  .default-value-setting {
    display: flex;
    align-items: center;
  }

  .hint-icon {
    margin-left: 4px;
    color: #646a73;
    font-size: 14px;
    cursor: pointer;
  }

  .binding-error,
  .param-name-error,
  .validation-message {
    color: var(--ed-color-danger);
    font-size: 12px;
  }

  .binding-error {
    margin-top: 4px;
  }

  .load-error {
    padding: 16px;
  }

  .retry-button {
    margin-top: 12px;
  }

  .root-class {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 15px 0 5px;
  }

  .footer-actions {
    display: flex;
    flex-shrink: 0;
    margin-left: auto;
  }
}
</style>
