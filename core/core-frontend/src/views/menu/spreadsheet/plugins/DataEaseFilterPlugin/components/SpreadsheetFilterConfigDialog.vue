<script setup lang="ts">
import { Delete, EditPen, Hide, InfoFilled, MoreFilled, Plus, View } from '@element-plus/icons-vue'
import { cloneDeep } from 'lodash-es'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import draggable from 'vuedraggable'
import { ElMessage } from 'element-plus-secondary'
import { getDatasetTree, getDsDetailsWithPerm } from '@/api/dataset'
import detailTableIcon from '@/assets/svg/chart-light/icon_common-table_light.svg'
import pivotTableIcon from '@/assets/svg/chart-light/icon_pivot-table_light.svg'
import dvFolder from '@/assets/svg/dv-folder.svg'
import DatasetIcon from '@/assets/svg/icon_dataset.svg'
import organizationIcon from '@/assets/svg/icon_organization_outlined.svg'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { fieldType } from '@/utils/attr'
import { iconFieldMap } from '@/components/icon-group/field-list'
import SpreadsheetFilterRenderer from './renderers/SpreadsheetFilterRenderer.vue'
import SpreadsheetFilterCustomSortDialog from './config/SpreadsheetFilterCustomSortDialog.vue'
import SpreadsheetFilterTextSearchConfig from './config/SpreadsheetFilterTextSearchConfig.vue'
import SpreadsheetFilterTreeConfig from './config/SpreadsheetFilterTreeConfig.vue'
import SpreadsheetFilterTimeConfig from './config/SpreadsheetFilterTimeConfig.vue'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterConfig,
  SpreadsheetFilterLinkedField
} from '../../../types/plugin'
import {
  getSpreadsheetFilterDisplayTypeOptions,
  getSpreadsheetFilterFieldTypeState,
  filterSpreadsheetFilterDatasetFields,
  isSpreadsheetFilterDatasetFieldAllowed,
  normalizeSpreadsheetFilterTextSearchClauses,
  normalizeSpreadsheetFilterConditionByRules,
  shouldShowSpreadsheetFilterDatasetFields,
  shouldShowSpreadsheetFilterOptionSource,
  supportsSpreadsheetFilterDatasetOptionSource
} from '../utils/filter-condition-rules'
import {
  getSpreadsheetFilterEmptyValue,
  isSpreadsheetFilterEmptyValue
} from '../utils/filter-values'
import type {
  SpreadsheetFilterAvailableField,
  SpreadsheetFilterAvailablePlugin
} from '../utils/events'

const props = defineProps<{
  config: SpreadsheetFilterConfig
  availablePlugins?: SpreadsheetFilterAvailablePlugin[]
  selectedConditionId?: string
  initialAction?: 'add'
  onSave: (config: SpreadsheetFilterConfig) => void
  onCancel: () => void
}>()

const shouldFilterUnavailableLinkedFields = props.availablePlugins !== undefined
const availablePluginIds = new Set<string>()
props.availablePlugins?.forEach(plugin => availablePluginIds.add(String(plugin.pluginId)))

const filterAvailableLinkedFields = (fields: SpreadsheetFilterLinkedField[] = []) => {
  if (!shouldFilterUnavailableLinkedFields) return fields
  return fields.filter(field => availablePluginIds.has(String(field.pluginId)))
}

const createLocalConfig = () => {
  const config = cloneDeep(props.config)
  // 表格实例删除后可能残留旧关联，配置面板只保留当前仍存在的实例关联。
  config.conditions.forEach(condition => {
    condition.linkedFields = filterAvailableLinkedFields(condition.linkedFields)
    condition.treeLevelMappings.forEach(mapping => {
      mapping.linkedFields = filterAvailableLinkedFields(mapping.linkedFields)
    })
  })
  return config
}

const localConfig = ref<SpreadsheetFilterConfig>(createLocalConfig())
const activeConditionId = ref(props.selectedConditionId || localConfig.value.conditions[0]?.id || '')
const editingConditionId = ref('')
const editingConditionName = ref('')
const conditionNameInputRef = ref()
const datasetTree = ref<any[]>([])
const optionDatasetFields = ref<SpreadsheetFilterAvailableField[]>([])
const datasetFieldCache = new Map<string, SpreadsheetFilterAvailableField[]>()
const manualPopoverRef = ref()
const manualOptionDraft = ref<Array<string | number>>([])
const customSortDialogVisible = ref(false)
const customSortSelectionPending = ref(false)
const sortTypeBeforeChange = ref<SpreadsheetFilterCondition['sortType']>()
const activeTreeLevelIndex = ref(0)
const expandedTreeConditions = ref<Record<string, boolean>>(
  Object.fromEntries(
    localConfig.value.conditions
      .filter(condition => condition.displayType === 'treeSelect')
      .map(condition => [condition.id, true])
  )
)
const activeFieldTabs = ref<Record<string, 'd' | 'q'>>(
  Object.fromEntries((props.availablePlugins || []).map(plugin => [plugin.pluginId, 'd']))
)

const conditionCount = computed(() => localConfig.value.conditions.length)
const activeCondition = computed(() =>
  localConfig.value.conditions.find(condition => condition.id === activeConditionId.value)
)
const getActiveLinkedFields = (): SpreadsheetFilterLinkedField[] => {
  const condition = activeCondition.value
  if (!condition) return []
  if (condition.displayType !== 'treeSelect' || activeTreeLevelIndex.value === 0) {
    return condition.linkedFields
  }
  const treeField = condition.treeFields[activeTreeLevelIndex.value]
  if (!treeField) return []
  return condition.treeLevelMappings.find(
    mapping => String(mapping.treeFieldId) === String(treeField.fieldId)
  )?.linkedFields || []
}

const initializeTreeDatasetFromFirstLinkedField = (condition: SpreadsheetFilterCondition) => {
  if (condition.displayType !== 'treeSelect' || condition.treeDatasetId) return
  const firstLinkedField = condition.linkedFields.find(
    field => field.datasetId !== undefined && field.datasetId !== null && field.datasetId !== ''
  )
  if (!firstLinkedField) return
  condition.treeDatasetId = firstLinkedField.datasetId
  condition.treeDatasetName = firstLinkedField.datasetName
}

const setActiveLinkedFields = (linkedFields: SpreadsheetFilterLinkedField[]) => {
  const condition = activeCondition.value
  if (!condition) return
  if (
    !condition.defaultValueEnabled &&
    !isSpreadsheetFilterEmptyValue(condition.selectValue)
  ) {
    // 未要求默认值时，关联字段操作后不再保留原先的选中值。
    condition.selectValue = getSpreadsheetFilterEmptyValue(condition)
  }
  if (condition.displayType !== 'treeSelect' || activeTreeLevelIndex.value === 0) {
    condition.linkedFields = linkedFields
    initializeTreeDatasetFromFirstLinkedField(condition)
    const firstTreeField = condition.treeFields[0]
    if (firstTreeField) {
      const mapping = condition.treeLevelMappings.find(
        item => String(item.treeFieldId) === String(firstTreeField.fieldId)
      )
      if (mapping) mapping.linkedFields = linkedFields
    }
    return
  }
  const treeField = condition.treeFields[activeTreeLevelIndex.value]
  if (!treeField) return
  const mapping = condition.treeLevelMappings.find(
    item => String(item.treeFieldId) === String(treeField.fieldId)
  )
  if (mapping) {
    mapping.linkedFields = linkedFields
  } else {
    condition.treeLevelMappings.push({ treeFieldId: treeField.fieldId, linkedFields })
  }
}

const selectedLinkedFieldCount = computed(() => getActiveLinkedFields().length)
const activeConditionTypeState = computed(() =>
  getSpreadsheetFilterFieldTypeState(activeCondition.value?.linkedFields || [])
)
const activeConditionConfigurable = computed(
  () => activeConditionTypeState.value.hasField && !activeConditionTypeState.value.hasMixedType
)
const activeDisplayTypeOptions = computed(() =>
  getSpreadsheetFilterDisplayTypeOptions(activeCondition.value)
)
const showOptionSource = computed(() => shouldShowSpreadsheetFilterOptionSource(activeCondition.value))
const showDatasetFields = computed(() => shouldShowSpreadsheetFilterDatasetFields(activeCondition.value))
const supportsDatasetOptionSource = computed(() =>
  supportsSpreadsheetFilterDatasetOptionSource(activeCondition.value)
)
const queryDatasetFields = computed(() =>
  filterSpreadsheetFilterDatasetFields(activeCondition.value, optionDatasetFields.value, 'query')
)
const displayDatasetFields = computed(() =>
  filterSpreadsheetFilterDatasetFields(activeCondition.value, optionDatasetFields.value, 'display')
)
const sortDatasetFields = computed(() =>
  filterSpreadsheetFilterDatasetFields(activeCondition.value, optionDatasetFields.value, 'sort')
)
const customSortAvailable = computed(() =>
  !!activeCondition.value?.displayFieldId &&
  !!activeCondition.value?.sortFieldId &&
  String(activeCondition.value.displayFieldId) === String(activeCondition.value.sortFieldId)
)
const availablePluginRows = computed(() => props.availablePlugins || [])
const selectablePluginRows = computed(() =>
  availablePluginRows.value.filter(row => getCompatibleDatasetFields(row).some(field => !field.desensitized))
)
const allFieldsSelected = computed({
  get: () =>
    !!selectablePluginRows.value.length &&
    selectablePluginRows.value.every(row => !!getSelectedFieldId(row.pluginId)),
  set: checked => {
    if (!activeCondition.value) {
      return
    }
    let linkedFields: SpreadsheetFilterLinkedField[] = []
    if (checked) {
      linkedFields = [...getActiveLinkedFields()]
      const selectedPluginIds = new Set(linkedFields.map(field => field.pluginId))
      selectablePluginRows.value.forEach(row => {
        if (selectedPluginIds.has(row.pluginId)) return
        const field = getCompatibleDatasetFields(row).find(item => !item.desensitized)
        if (!field) return
        // 全选仅补齐尚未关联的图表，已有字段及其顺序必须保持不变。
        linkedFields.push(toLinkedField(row, field))
        selectedPluginIds.add(row.pluginId)
      })
    }
    setActiveLinkedFields(linkedFields)
    if (activeTreeLevelIndex.value !== 0) return
    // 全选与单个字段选择使用同一套类型规则，避免日期字段仍保留文本展示类型。
    normalizeSpreadsheetFilterConditionByRules(activeCondition.value)
    syncActiveConditionFieldOptions()
  }
})

const optionSourceOptions = [
  { label: '自动', value: 'auto' },
  { label: '选择数据集', value: 'dataset' },
  { label: '手动输入', value: 'manual' }
] as const

const displayFormOptions = [
  { label: '下拉展示', value: 'dropdown' },
  { label: '平铺展示', value: 'tile' }
] as const

const datasetTreeProps = {
  label: 'name',
  children: 'children',
  value: 'id',
  isLeaf: (node: any) => !!node.leaf || !node.children?.length
}

const createCondition = (): SpreadsheetFilterCondition => {
  return {
    id: `condition_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    name: '未命名',
    visible: true,
    required: false,
    displayType: 'textSelect',
    optionSource: 'auto',
    manualOptions: [],
    displayForm: 'dropdown',
    optionCountMode: 'default',
    queryMode: 'click',
    multiple: false,
    defaultValueEnabled: false,
    defaultValueFirstItem: false,
    showRule: 'always',
    defaultValue: '',
    selectValue: '',
    textSearchConditionType: 'single',
    hideTextSearchConditionSwitch: false,
    textSearchDefaultClauses: [
      { operator: 'eq', value: '' },
      { operator: 'like', value: '' }
    ],
    treeFields: [],
    treeLevelMappings: [],
    timeGranularity: 'date',
    timeRangeGranularity: 'daterange',
    timeDefaultType: 'fixed',
    timeDynamicDefault: { offset: { value: 0, unit: 'day', direction: 'before', relativeToCurrent: 'custom' } },
    timeRangeDynamicDefault: {
      start: { value: 7, unit: 'day', direction: 'before', relativeToCurrent: 'custom' },
      end: { value: 0, unit: 'day', direction: 'before', relativeToCurrent: 'custom' }
    },
    timeFilterRangeEnabled: false,
    timeFilterRange: { intervalType: 'none' },
    linkedFields: []
  }
}

const addCondition = () => {
  const condition = createCondition()
  localConfig.value.conditions.push(condition)
  activeConditionId.value = condition.id
}

const selectCondition = (condition: SpreadsheetFilterCondition) => {
  activeConditionId.value = condition.id
  activeTreeLevelIndex.value = 0
}

const activateTreeLevel = (index: number) => {
  if (!activeCondition.value?.treeFields[index]) return
  const treeField = activeCondition.value.treeFields[index]
  const hasMapping = activeCondition.value.treeLevelMappings.some(
    mapping => String(mapping.treeFieldId) === String(treeField.fieldId)
  )
  if (!hasMapping) initializeTreeLevelMappings([treeField.fieldId])
  activeTreeLevelIndex.value = index
}

const removeCondition = (condition: SpreadsheetFilterCondition) => {
  const index = localConfig.value.conditions.findIndex(item => item.id === condition.id)
  if (index < 0) {
    return
  }
  localConfig.value.conditions.splice(index, 1)
  if (activeConditionId.value === condition.id) {
    activeConditionId.value =
      localConfig.value.conditions[Math.max(index - 1, 0)]?.id || localConfig.value.conditions[0]?.id || ''
  }
}

const getFieldKey = (field: Pick<SpreadsheetFilterLinkedField, 'pluginId' | 'fieldId'>) =>
  `${field.pluginId}__${field.fieldId}`

const toLinkedField = (
  row: SpreadsheetFilterAvailablePlugin,
  field: SpreadsheetFilterAvailableField
): SpreadsheetFilterLinkedField => ({
  pluginId: row.pluginId,
  pluginName: row.pluginName,
  fieldId: field.fieldId,
  fieldName: field.fieldName,
  datasetId: row.datasetId,
  datasetName: row.datasetName,
  groupType: field.groupType,
  deType: field.deType
})

const initializeTreeLevelMappings = (newTreeFieldIds: Array<string | number>) => {
  const condition = activeCondition.value
  if (!condition || condition.displayType !== 'treeSelect') return
  const existingMappings = new Map(
    condition.treeLevelMappings.map(mapping => [String(mapping.treeFieldId), mapping])
  )
  const newTreeFieldIdSet = new Set(newTreeFieldIds.map(String))
  condition.treeLevelMappings = condition.treeFields.map((treeField, index) => {
    if (index === 0) {
      return { treeFieldId: treeField.fieldId, linkedFields: [...condition.linkedFields] }
    }
    const existing = existingMappings.get(String(treeField.fieldId))
    if (existing && !newTreeFieldIdSet.has(String(treeField.fieldId))) return existing
    const linkedFields = condition.linkedFields.map(linkedField => {
      const plugin = props.availablePlugins.find(item => item.pluginId === linkedField.pluginId)
      const sameField = plugin?.fields.find(
        field =>
          field.groupType === 'd' &&
          Number(field.deType) === 0 &&
          !field.desensitized &&
          String(field.fieldId) === String(treeField.fieldId)
      )
      return plugin && sameField ? toLinkedField(plugin, sameField) : { ...linkedField }
    })
    return { treeFieldId: treeField.fieldId, linkedFields }
  })
}

const getSelectedFieldId = (pluginId: string) =>
  getActiveLinkedFields().find(field => field.pluginId === pluginId)?.fieldId

const hasDatasetId = (datasetId?: string | number) =>
  datasetId !== undefined && datasetId !== null && datasetId !== ''

const getDefaultLinkedFieldId = (row: SpreadsheetFilterAvailablePlugin) => {
  const availableFields = getCompatibleDatasetFields(row).filter(field => !field.desensitized)
  const fallbackFieldId = availableFields[0]?.fieldId
  if (!hasDatasetId(row.datasetId)) return fallbackFieldId

  // 同数据集图表优先复用第一个已关联字段，保持多图表过滤字段一致。
  const sameDatasetLinkedField = getActiveLinkedFields().find(
    field =>
      hasDatasetId(field.datasetId) && String(field.datasetId) === String(row.datasetId)
  )
  if (!sameDatasetLinkedField) return fallbackFieldId

  const sameField = availableFields.find(
    field => String(field.fieldId) === String(sameDatasetLinkedField.fieldId)
  )
  return sameField?.fieldId ?? fallbackFieldId
}

const selectPluginField = (row: SpreadsheetFilterAvailablePlugin, fieldId?: unknown) => {
  if (!activeCondition.value) {
    return
  }
  const linkedFields = getActiveLinkedFields().filter(item => item.pluginId !== row.pluginId)
  const field = getCompatibleDatasetFields(row).find(
    item => !item.desensitized && String(item.fieldId) === String(fieldId)
  )
  if (field) {
    linkedFields.push(toLinkedField(row, field))
  }
  setActiveLinkedFields(linkedFields)
  if (activeTreeLevelIndex.value === 0) {
    normalizeSpreadsheetFilterConditionByRules(activeCondition.value)
    syncActiveConditionFieldOptions()
  }
}

const getPluginIcon = (pluginType: SpreadsheetFilterAvailablePlugin['pluginType']) =>
  pluginType === 'pivot' ? pivotTableIcon : detailTableIcon

const getActiveFieldTab = (row: SpreadsheetFilterAvailablePlugin) =>
  activeFieldTabs.value[row.pluginId] || 'd'

const getCompatibleDatasetFields = (row: SpreadsheetFilterAvailablePlugin) => {
  const condition = activeCondition.value
  if (condition?.displayType !== 'treeSelect') return row.fields
  const treeField = condition.treeFields[activeTreeLevelIndex.value]
  if (!treeField) return row.fields
  return row.fields.filter(field => {
    const sourceType = Number(treeField.deType)
    const targetType = Number(field.deType)
    return [2, 3].includes(sourceType) && [2, 3].includes(targetType)
      ? true
      : sourceType === targetType
  })
}

const getVisibleDatasetFields = (row: SpreadsheetFilterAvailablePlugin) =>
  getCompatibleDatasetFields(row).filter(field => field.groupType === getActiveFieldTab(row))

const getFieldIconType = (field?: SpreadsheetFilterAvailableField) =>
  fieldType[field?.deType ?? 0] || 'text'

const getFieldIcon = (field?: SpreadsheetFilterAvailableField) =>
  iconFieldMap[getFieldIconType(field)] || iconFieldMap.text

const getSelectedDatasetField = (row: SpreadsheetFilterAvailablePlugin) => {
  const selectedFieldId = getSelectedFieldId(row.pluginId)
  return row.fields.find(field => String(field.fieldId) === String(selectedFieldId))
}

const handleFieldSelectVisible = (row: SpreadsheetFilterAvailablePlugin, visible: boolean) => {
  if (!visible) {
    return
  }
  const selectedFieldId = getSelectedFieldId(row.pluginId)
  const selectedField = row.fields.find(field => String(field.fieldId) === String(selectedFieldId))
  activeFieldTabs.value[row.pluginId] = selectedField?.groupType || activeFieldTabs.value[row.pluginId] || 'd'
}

const syncSelectedFieldTabs = () => {
  for (const row of availablePluginRows.value) {
    const selectedFieldId = getSelectedFieldId(row.pluginId)
    const selectedField = row.fields.find(
      field => String(field.fieldId) === String(selectedFieldId)
    )
    if (selectedField?.groupType) {
      activeFieldTabs.value[row.pluginId] = selectedField.groupType
    }
  }
}

watch(
  [activeConditionId, () => activeTreeLevelIndex.value, availablePluginRows],
  syncSelectedFieldTabs,
  { immediate: true, flush: 'sync' }
)

const toggleConditionVisible = (condition: SpreadsheetFilterCondition) => {
  condition.visible = !condition.visible
}

const isTreeConditionExpanded = (conditionId: string) =>
  expandedTreeConditions.value[conditionId] !== false

const toggleTreeConditionExpanded = (conditionId: string) => {
  const expanded = !isTreeConditionExpanded(conditionId)
  expandedTreeConditions.value[conditionId] = expanded
  if (!expanded && activeConditionId.value === conditionId && activeTreeLevelIndex.value > 0) {
    activeTreeLevelIndex.value = 0
  }
}

const startRenameCondition = (condition: SpreadsheetFilterCondition) => {
  activeConditionId.value = condition.id
  editingConditionId.value = condition.id
  editingConditionName.value = condition.name
  nextTick(() => {
    const inputComponent = Array.isArray(conditionNameInputRef.value)
      ? conditionNameInputRef.value[0]
      : conditionNameInputRef.value
    const input = inputComponent?.input as HTMLInputElement | undefined
    input?.focus()
    input?.select()
  })
}

const finishRenameCondition = () => {
  const condition = localConfig.value.conditions.find(item => item.id === editingConditionId.value)
  const nextName = editingConditionName.value.trim()
  if (condition && nextName) {
    condition.name = nextName
  }
  editingConditionId.value = ''
  editingConditionName.value = ''
}

const cancelRenameCondition = () => {
  editingConditionId.value = ''
  editingConditionName.value = ''
}

const handleConditionCommand = (
  command: string,
  condition: SpreadsheetFilterCondition
) => {
  if (command === 'delete') {
    removeCondition(condition)
    return
  }
  startRenameCondition(condition)
}

const clearLinkedFields = () => {
  if (!activeCondition.value) {
    return
  }
  setActiveLinkedFields([])
  if (activeTreeLevelIndex.value > 0) return
  activeCondition.value.queryFieldId = undefined
  activeCondition.value.queryFieldName = undefined
  activeCondition.value.displayFieldId = undefined
  activeCondition.value.displayFieldName = undefined
  activeCondition.value.sortFieldId = undefined
  activeCondition.value.sortFieldName = undefined
  activeCondition.value.sortType = undefined
  activeCondition.value.sortList = undefined
  normalizeSpreadsheetFilterConditionByRules(activeCondition.value)
}

const syncActiveConditionFieldOptions = () => {
  if (!activeCondition.value) {
    return
  }
  if (!shouldShowSpreadsheetFilterDatasetFields(activeCondition.value)) {
    activeCondition.value.queryFieldId = undefined
    activeCondition.value.queryFieldName = undefined
    activeCondition.value.displayFieldId = undefined
    activeCondition.value.displayFieldName = undefined
    activeCondition.value.sortFieldId = undefined
    activeCondition.value.sortFieldName = undefined
    activeCondition.value.sortType = undefined
    activeCondition.value.sortList = undefined
    return
  }
  clearInvalidOptionDatasetFields()
}

const handleDisplayTypeChange = () => {
  if (!activeCondition.value) {
    return
  }
  normalizeSpreadsheetFilterConditionByRules(activeCondition.value)
  initializeTreeDatasetFromFirstLinkedField(activeCondition.value)
  clearInvalidOptionDatasetFields()
}

const handleOptionSourceChange = () => {
  if (!activeCondition.value) {
    return
  }
  normalizeSpreadsheetFilterConditionByRules(activeCondition.value)
  if (activeCondition.value.optionSource === 'dataset') {
    void initializeOptionDatasetFromFirstLinkedField()
  } else {
    syncActiveConditionFieldOptions()
    activeCondition.value.sortType = undefined
    activeCondition.value.sortList = undefined
  }
  activeCondition.value.defaultValue = activeCondition.value.multiple ? [] : ''
  activeCondition.value.defaultValueFirstItem = false
}

const handleMultipleChange = () => {
  if (!activeCondition.value) return
  activeCondition.value.defaultValue = activeCondition.value.multiple ? [] : ''
  activeCondition.value.defaultValueFirstItem = false
}

const updateConditionFieldName = (target: 'query' | 'display' | 'sort', fieldId: unknown) => {
  if (!activeCondition.value) {
    return
  }
  const field = optionDatasetFields.value.find(item => String(item.fieldId) === String(fieldId))
  if (target === 'query') {
    activeCondition.value.queryFieldName = field?.fieldName
  } else if (target === 'display') {
    activeCondition.value.displayFieldName = field?.fieldName
    if (!customSortAvailable.value && activeCondition.value.sortType === 'customSort') {
      activeCondition.value.sortType = undefined
      activeCondition.value.sortList = undefined
    }
  } else {
    activeCondition.value.sortFieldName = field?.fieldName
    // 自定义排序值只属于原排序字段，字段切换后统一从升序重新开始。
    activeCondition.value.sortType = field ? 'asc' : undefined
    activeCondition.value.sortList = undefined
  }
  activeCondition.value.defaultValue = activeCondition.value.multiple ? [] : ''
  activeCondition.value.defaultValueFirstItem = false
}

const handleSortTypeVisibleChange = (visible: boolean) => {
  if (visible) sortTypeBeforeChange.value = activeCondition.value?.sortType
}

const handleSortTypeChange = (sortType: SpreadsheetFilterCondition['sortType']) => {
  if (!activeCondition.value) return
  if (sortType === 'customSort') return
  activeCondition.value.sortList = undefined
}

const handleCustomSortOptionClick = () => {
  nextTick(() => {
    if (activeCondition.value?.sortType !== 'customSort') return
    customSortSelectionPending.value = sortTypeBeforeChange.value !== 'customSort'
    customSortDialogVisible.value = true
  })
}

const handleCustomSortDialogVisibleChange = (visible: boolean) => {
  customSortDialogVisible.value = visible
  if (visible || !customSortSelectionPending.value || !activeCondition.value) return
  // 首次选择自定义排序后取消弹窗时，恢复进入弹窗前的排序类型。
  activeCondition.value.sortType = sortTypeBeforeChange.value || 'asc'
  customSortSelectionPending.value = false
}

const confirmCustomSort = (values: Array<string | number>) => {
  if (!activeCondition.value) return
  activeCondition.value.sortList = values
  activeCondition.value.sortType = 'customSort'
  customSortSelectionPending.value = false
}

const clearInvalidOptionDatasetFields = () => {
  const condition = activeCondition.value
  if (!condition || condition.optionSource !== 'dataset') return
  let changed = false
  const clearRole = (role: 'query' | 'display' | 'sort') => {
    const idKey = `${role}FieldId` as 'queryFieldId' | 'displayFieldId' | 'sortFieldId'
    const nameKey = `${role}FieldName` as 'queryFieldName' | 'displayFieldName' | 'sortFieldName'
    const fieldId = condition[idKey]
    if (fieldId === undefined || fieldId === null || fieldId === '') return
    const field = optionDatasetFields.value.find(item => String(item.fieldId) === String(fieldId))
    if (
      field &&
      !field.desensitized &&
      isSpreadsheetFilterDatasetFieldAllowed(condition, field, role)
    ) return
    condition[idKey] = undefined
    condition[nameKey] = undefined
    changed = true
  }
  clearRole('query')
  clearRole('display')
  clearRole('sort')
  if (!condition.sortFieldId) {
    condition.sortType = undefined
    condition.sortList = undefined
  }
  if (changed) {
    condition.defaultValue = condition.multiple ? [] : ''
    condition.defaultValueFirstItem = false
  }
}

const resetOptionDatasetConfig = () => {
  if (!activeCondition.value) return
  activeCondition.value.queryFieldId = undefined
  activeCondition.value.queryFieldName = undefined
  activeCondition.value.displayFieldId = undefined
  activeCondition.value.displayFieldName = undefined
  activeCondition.value.sortFieldId = undefined
  activeCondition.value.sortFieldName = undefined
  activeCondition.value.sortType = undefined
  activeCondition.value.sortList = undefined
  activeCondition.value.defaultValue = activeCondition.value.multiple ? [] : ''
  activeCondition.value.defaultValueFirstItem = false
}

const loadOptionDatasetFields = async (datasetId?: string | number) => {
  if (datasetId === undefined || datasetId === null || datasetId === '') {
    optionDatasetFields.value = []
    return
  }
  const key = String(datasetId)
  if (datasetFieldCache.has(key)) {
    optionDatasetFields.value = datasetFieldCache.get(key) || []
    clearInvalidOptionDatasetFields()
    return
  }
  try {
    const details = await getDsDetailsWithPerm([datasetId])
    const fields = details?.[0]?.fields
    const nextFields: SpreadsheetFilterAvailableField[] = [
      ...(fields?.dimensionList || []).map((field: any) => ({
        fieldId: field.id,
        fieldName: field.name || field.dataeaseName,
        groupType: 'd' as const,
        deType: field.deType,
        desensitized: field.desensitized
      })),
      ...(fields?.quotaList || []).map((field: any) => ({
        fieldId: field.id,
        fieldName: field.name || field.dataeaseName,
        groupType: 'q' as const,
        deType: field.deType,
        desensitized: field.desensitized
      }))
    ]
    datasetFieldCache.set(key, nextFields)
    optionDatasetFields.value = nextFields
    clearInvalidOptionDatasetFields()
  } catch {
    optionDatasetFields.value = []
  }
}

const findDatasetNode = (nodes: any[], datasetId: string | number): any => {
  for (const node of nodes) {
    if (String(node.id) === String(datasetId)) return node
    const child = findDatasetNode(node.children || [], datasetId)
    if (child) return child
  }
}

const handleOptionDatasetChange = async (datasetId: string | number) => {
  if (!activeCondition.value) return
  activeCondition.value.optionDatasetName = findDatasetNode(datasetTree.value, datasetId)?.name
  resetOptionDatasetConfig()
  await loadOptionDatasetFields(datasetId)
}

const initializeOptionDatasetFromFirstLinkedField = async () => {
  const condition = activeCondition.value
  if (!condition) return
  const firstLinkedField = condition.linkedFields[0]
  const hasDataset = firstLinkedField?.datasetId !== undefined &&
    firstLinkedField.datasetId !== null &&
    firstLinkedField.datasetId !== ''
  const hasField = firstLinkedField?.fieldId !== undefined &&
    firstLinkedField.fieldId !== null &&
    firstLinkedField.fieldId !== ''
  if (!firstLinkedField || !hasDataset || !hasField) {
    resetOptionDatasetConfig()
    return
  }

  condition.optionDatasetId = firstLinkedField.datasetId
  condition.optionDatasetName = firstLinkedField.datasetName
  resetOptionDatasetConfig()
  await loadOptionDatasetFields(firstLinkedField.datasetId)
  if (activeCondition.value !== condition || condition.optionSource !== 'dataset') return

  const linkedField = optionDatasetFields.value.find(
    field => String(field.fieldId) === String(firstLinkedField.fieldId)
  )
  if (!linkedField || linkedField.desensitized) return

  // 数据集来源初始配置与第一个关联图表保持一致，避免用户重复选择同一字段。
  condition.queryFieldId = linkedField.fieldId
  condition.queryFieldName = linkedField.fieldName
  condition.displayFieldId = linkedField.fieldId
  condition.displayFieldName = linkedField.fieldName
  condition.sortFieldId = linkedField.fieldId
  condition.sortFieldName = linkedField.fieldName
  condition.sortType = 'asc'
}

const openManualOptions = () => {
  manualOptionDraft.value = cloneDeep(activeCondition.value?.manualOptions?.length
    ? activeCondition.value.manualOptions
    : [''])
}

const cancelManualOptions = () => {
  manualPopoverRef.value?.hide?.()
}

const confirmManualOptions = () => {
  if (!activeCondition.value) return
  const values = manualOptionDraft.value.map(value =>
    typeof value === 'string' ? value.trim() : value
  )
  if (values.some(value => value === '')) {
    ElMessage.warning('选项值不能为空')
    return
  }
  const optionValueSet = new Set<string>()
  const uniqueValues = values.filter(value => {
    // 运行时统一按字符串值查询，手动选项也按相同语义去重并保留首次出现项。
    const optionValue = String(value)
    if (optionValueSet.has(optionValue)) return false
    optionValueSet.add(optionValue)
    return true
  })
  activeCondition.value.manualOptions = uniqueValues
  activeCondition.value.defaultValue = activeCondition.value.multiple ? [] : ''
  activeCondition.value.defaultValueFirstItem = false
  manualPopoverRef.value?.hide?.()
}

const canRenderDefaultPreview = computed(() => {
  const condition = activeCondition.value
  if (!condition?.defaultValueEnabled) return false
  if (condition.optionSource === 'auto') return !!condition.linkedFields.length
  if (condition.optionSource === 'manual') return !!condition.manualOptions.length
  return !!(
    condition.optionDatasetId &&
    condition.queryFieldId &&
    condition.displayFieldId
  )
})

const defaultPreviewStyle = computed(() => ({
  '--dataease-filter-primary-color': localConfig.value.style.button.primaryColor
}))

const hasLinkedChartField = (fields: SpreadsheetFilterLinkedField[] = []) =>
  fields.some(field =>
    !!field.pluginId &&
    field.fieldId !== undefined &&
    field.fieldId !== null &&
    field.fieldId !== ''
  )

const hasMissingLinkedChartField = (condition: SpreadsheetFilterCondition) => {
  if (!hasLinkedChartField(condition.linkedFields)) return true
  if (condition.displayType !== 'treeSelect') return false
  return condition.treeFields.slice(1).some(treeField => {
    const mapping = condition.treeLevelMappings.find(
      item => String(item.treeFieldId) === String(treeField.fieldId)
    )
    return !hasLinkedChartField(mapping?.linkedFields)
  })
}

const hasEffectiveDefaultValue = (condition: SpreadsheetFilterCondition) => {
  if (['time', 'timeRange'].includes(condition.displayType)) {
    if (!condition.defaultValueEnabled) return false
    if (condition.timeDefaultType === 'dynamic') return true
    return condition.displayType === 'timeRange'
      ? Array.isArray(condition.defaultValue) &&
        condition.defaultValue.length === 2 &&
        condition.defaultValue.every(value => !isSpreadsheetFilterEmptyValue(value))
      : !isSpreadsheetFilterEmptyValue(condition.defaultValue)
  }
  if (condition.displayType === 'textSearch') {
    const clauses = normalizeSpreadsheetFilterTextSearchClauses(
      condition.textSearchDefaultClauses,
      condition.textSearchConditionType
    )
    return clauses.every(clause => !!clause.value.trim())
  }
  if (condition.displayType === 'treeSelect') {
    if (!condition.defaultValueEnabled || !condition.treeFields.length) return false
    if (condition.defaultValueFirstItem) return true
    return Array.isArray(condition.defaultValue) && condition.defaultValue.length > 0
  }
  if (condition.displayType === 'numberRange') {
    return condition.defaultValueEnabled &&
      Array.isArray(condition.defaultValue) &&
      condition.defaultValue.length === 2 &&
      condition.defaultValue.every(value => !isSpreadsheetFilterEmptyValue(value))
  }
  if (!condition.defaultValueEnabled) return false
  const sourceReady = condition.optionSource === 'auto'
    ? !!condition.linkedFields.length
    : condition.optionSource === 'manual'
      ? !!condition.manualOptions.length
      : !!(
        condition.optionDatasetId &&
        condition.queryFieldId &&
        condition.displayFieldId
      )
  if (condition.defaultValueFirstItem && shouldShowSpreadsheetFilterOptionSource(condition)) {
    return sourceReady
  }
  return !isSpreadsheetFilterEmptyValue(condition.defaultValue)
}

const save = () => {
  localConfig.value.conditions.forEach(condition => normalizeSpreadsheetFilterConditionByRules(condition))
  const invalidTreeCondition = localConfig.value.conditions.find(condition => {
    if (condition.displayType !== 'treeSelect') return false
    if (!condition.treeDatasetId || !condition.treeFields.length) return true
    return condition.treeFields.some(field => {
      const mapping = condition.treeLevelMappings.find(
        item => String(item.treeFieldId) === String(field.fieldId)
      )
      return !hasLinkedChartField(mapping?.linkedFields)
    })
  })
  if (invalidTreeCondition) {
    activeConditionId.value = invalidTreeCondition.id
    const invalidLevel = invalidTreeCondition.treeFields.findIndex(field => {
      const mapping = invalidTreeCondition.treeLevelMappings.find(
        item => String(item.treeFieldId) === String(field.fieldId)
      )
      return !hasLinkedChartField(mapping?.linkedFields)
    })
    activeTreeLevelIndex.value = Math.max(0, invalidLevel)
    ElMessage.warning(
      !invalidTreeCondition.treeDatasetId || !invalidTreeCondition.treeFields.length
        ? '请完成下拉树结构配置'
        : '请为每个下拉树层级关联图表字段'
    )
    return
  }
  const unlinkedCondition = localConfig.value.conditions.find(
    condition => !hasLinkedChartField(condition.linkedFields)
  )
  if (unlinkedCondition) {
    activeConditionId.value = unlinkedCondition.id
    ElMessage.warning('请为每个查询条件至少关联一个图表字段')
    return
  }
  const inconsistentCondition = localConfig.value.conditions.find(condition => {
    if (getSpreadsheetFilterFieldTypeState(condition.linkedFields).hasMixedType) return true
    return condition.displayType === 'treeSelect' && condition.treeLevelMappings.some(
      mapping => getSpreadsheetFilterFieldTypeState(mapping.linkedFields).hasMixedType
    )
  })
  if (inconsistentCondition) {
    activeConditionId.value = inconsistentCondition.id
    ElMessage.error('所选字段类型不一致，无法进行查询配置')
    return
  }
  const invalidDatasetCondition = localConfig.value.conditions.find(condition =>
    shouldShowSpreadsheetFilterDatasetFields(condition) &&
    (!condition.optionDatasetId || !condition.queryFieldId || !condition.displayFieldId)
  )
  if (invalidDatasetCondition) {
    activeConditionId.value = invalidDatasetCondition.id
    ElMessage.warning('请选择数据集、查询字段和显示字段')
    return
  }
  const invalidDefaultValueCondition = localConfig.value.conditions.find(
    condition => condition.defaultValueEnabled && !hasEffectiveDefaultValue(condition)
  )
  if (invalidDefaultValueCondition) {
    activeConditionId.value = invalidDefaultValueCondition.id
    ElMessage.warning('请设置组件的默认值')
    return
  }
  const invalidRequiredCondition = localConfig.value.conditions.find(
    condition => condition.required && !hasEffectiveDefaultValue(condition)
  )
  if (invalidRequiredCondition) {
    activeConditionId.value = invalidRequiredCondition.id
    ElMessage.warning('必填项必须设置默认值')
    return
  }
  props.onSave(cloneDeep(localConfig.value))
}

onMounted(async () => {
  try {
    const tree = await getDatasetTree({})
    datasetTree.value = tree?.[0]?.children || []
  } catch {
    datasetTree.value = []
  }
  await loadOptionDatasetFields(activeCondition.value?.optionDatasetId)
})

watch(activeConditionId, () => {
  void loadOptionDatasetFields(activeCondition.value?.optionDatasetId)
})

if (props.initialAction === 'add') {
  addCondition()
} else if (
  props.selectedConditionId &&
  !localConfig.value.conditions.some(condition => condition.id === props.selectedConditionId)
) {
  activeConditionId.value = localConfig.value.conditions[0]?.id || ''
}
</script>

<template>
  <div class="spreadsheet-filter-config-dialog">
    <div class="spreadsheet-filter-config-dialog__body">
      <aside class="spreadsheet-filter-config-dialog__condition-list">
        <div class="spreadsheet-filter-config-dialog__panel-title">
          <span>查询条件</span>
          <el-button text type="primary" @click="addCondition">+</el-button>
        </div>
        <div class="spreadsheet-filter-config-dialog__condition-items">
          <div v-if="!conditionCount" class="spreadsheet-filter-config-dialog__empty">
            暂无查询条件
          </div>
          <draggable
            v-model="localConfig.conditions"
            item-key="id"
            handle=".spreadsheet-filter-config-dialog__drag"
            ghost-class="spreadsheet-filter-config-dialog__condition-ghost"
          >
            <template #item="{ element: condition }">
              <div class="spreadsheet-filter-config-dialog__condition-group">
                <div
                  :class="[
                    'spreadsheet-filter-config-dialog__condition-item',
                    activeConditionId === condition.id && activeTreeLevelIndex === 0 && 'is-active'
                  ]"
                  @click="selectCondition(condition)"
                >
                <span class="spreadsheet-filter-config-dialog__drag">
                  <svg viewBox="0 0 16 16" aria-hidden="true">
                    <circle cx="6" cy="4" r="1" />
                    <circle cx="10" cy="4" r="1" />
                    <circle cx="6" cy="8" r="1" />
                    <circle cx="10" cy="8" r="1" />
                    <circle cx="6" cy="12" r="1" />
                    <circle cx="10" cy="12" r="1" />
                  </svg>
                </span>
                <el-input
                  v-if="editingConditionId === condition.id"
                  ref="conditionNameInputRef"
                  v-model="editingConditionName"
                  class="spreadsheet-filter-config-dialog__condition-name-input"
                  maxlength="50"
                  @click.stop
                  @dblclick.stop
                  @keydown.enter.prevent="finishRenameCondition"
                  @keydown.esc.stop.prevent="cancelRenameCondition"
                  @blur="finishRenameCondition"
                />
                <span
                  v-else
                  class="spreadsheet-filter-config-dialog__condition-name"
                  @dblclick.stop="startRenameCondition(condition)"
                >
                  {{ condition.name }}
                </span>
                <el-button
                  v-if="condition.displayType === 'treeSelect' && condition.treeFields.length > 1"
                  text
                  :class="[
                    'spreadsheet-filter-config-dialog__condition-action',
                    'spreadsheet-filter-config-dialog__tree-toggle',
                    isTreeConditionExpanded(condition.id) && 'is-expanded'
                  ]"
                  :title="isTreeConditionExpanded(condition.id) ? '收起子树' : '展开子树'"
                  @click.stop="toggleTreeConditionExpanded(condition.id)"
                >
                  <component :is="organizationIcon" class="spreadsheet-filter-config-dialog__tree-toggle-icon" />
                </el-button>
                <el-tooltip
                  v-if="hasMissingLinkedChartField(condition)"
                  content="请关联图表字段"
                  placement="top"
                >
                  <el-icon class="spreadsheet-filter-config-dialog__unlinked-warning">
                    <InfoFilled />
                  </el-icon>
                </el-tooltip>
                <el-dropdown
                  trigger="click"
                  @command="command => handleConditionCommand(command, condition)"
                  @click.stop
                >
                  <el-button text class="spreadsheet-filter-config-dialog__condition-action">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="rename">重命名</el-dropdown-item>
                      <el-dropdown-item command="delete">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button
                  text
                  class="spreadsheet-filter-config-dialog__condition-action"
                  @click.stop="toggleConditionVisible(condition)"
                >
                  <el-icon>
                    <View v-if="condition.visible" />
                    <Hide v-else />
                  </el-icon>
                </el-button>
                </div>
                <template v-if="isTreeConditionExpanded(condition.id)">
                  <button
                    v-for="(treeField, treeIndex) in condition.treeFields.slice(1)"
                    :key="treeField.fieldId"
                    type="button"
                    :class="[
                      'spreadsheet-filter-config-dialog__tree-level-item',
                      activeConditionId === condition.id && activeTreeLevelIndex === treeIndex + 1 && 'is-active'
                    ]"
                    :style="{ paddingLeft: `${32 + treeIndex * 16}px` }"
                    @click.stop="selectCondition(condition); activateTreeLevel(treeIndex + 1)"
                  >
                    {{ treeField.fieldName }}
                  </button>
                </template>
              </div>
            </template>
          </draggable>
        </div>
      </aside>

      <section class="spreadsheet-filter-config-dialog__field-panel">
        <div class="spreadsheet-filter-config-dialog__panel-title">
          <span>关联图表及字段</span>
          <span class="spreadsheet-filter-config-dialog__sub-title">
            已选{{ selectedLinkedFieldCount }}个字段
          </span>
        </div>
        <div class="spreadsheet-filter-config-dialog__field-toolbar">
          <el-checkbox v-model="allFieldsSelected" :disabled="!activeCondition || !selectablePluginRows.length">
            全选
          </el-checkbox>
          <el-button
            text
            type="primary"
            :disabled="!activeCondition || !selectedLinkedFieldCount"
            @click="clearLinkedFields"
          >
            清空选入字段
          </el-button>
        </div>
        <div v-if="!availablePluginRows.length" class="spreadsheet-filter-config-dialog__field-empty">
          当前没有可关联字段
        </div>
        <div v-else-if="!activeCondition" class="spreadsheet-filter-config-dialog__field-empty">
          请先选择查询条件
        </div>
        <div v-else class="spreadsheet-filter-config-dialog__plugin-list">
          <div
            v-for="row in availablePluginRows"
            :key="row.pluginId"
            class="spreadsheet-filter-config-dialog__plugin-row"
          >
            <el-checkbox
              :model-value="!!getSelectedFieldId(row.pluginId)"
              @change="checked => selectPluginField(row, checked ? getDefaultLinkedFieldId(row) : undefined)"
            />
            <span class="spreadsheet-filter-config-dialog__plugin-name">
              <component
                :is="getPluginIcon(row.pluginType)"
                class="spreadsheet-filter-config-dialog__plugin-type-icon"
              />
              <span>{{ row.pluginName }}</span>
            </span>
            <span class="spreadsheet-filter-config-dialog__dataset-name">{{ row.datasetName }}</span>
            <el-select
              :model-value="getSelectedFieldId(row.pluginId)"
              :placeholder="row.fields.length ? '请选择字段' : '暂无可用字段'"
              class="spreadsheet-filter-config-dialog__plugin-field-select"
              popper-class="spreadsheet-filter-field-select-popper"
              clearable
              filterable
              :disabled="!row.fields.length"
              @change="fieldId => selectPluginField(row, fieldId)"
              @clear="selectPluginField(row)"
              @visible-change="visible => handleFieldSelectVisible(row, visible)"
            >
              <template #prefix>
                <el-icon
                  v-if="getSelectedDatasetField(row)"
                  class="spreadsheet-filter-config-dialog__field-type-icon"
                >
                  <Icon :class-name="`field-icon-${getFieldIconType(getSelectedDatasetField(row))}`">
                    <component
                      :is="getFieldIcon(getSelectedDatasetField(row))"
                      class="svg-icon"
                      :class="`field-icon-${getFieldIconType(getSelectedDatasetField(row))}`"
                    />
                  </Icon>
                </el-icon>
              </template>
              <template #header>
                <el-tabs
                  v-model="activeFieldTabs[row.pluginId]"
                  stretch
                  class="spreadsheet-filter-config-dialog__field-tabs"
                  @click.stop
                >
                  <el-tab-pane label="维度" name="d" />
                  <el-tab-pane :disabled="activeCondition && activeCondition.displayType === 'treeSelect'" label="指标" name="q" />
                </el-tabs>
              </template>
              <el-option
                v-for="field in getVisibleDatasetFields(row)"
                :key="getFieldKey({ pluginId: row.pluginId, fieldId: field.fieldId })"
                :label="field.fieldName"
                :value="field.fieldId"
                :disabled="field.desensitized"
                :title="field.desensitized ? '该字段已脱敏，不能作为查询条件' : ''"
              >
                <div class="spreadsheet-filter-config-dialog__field-option">
                  <el-icon class="spreadsheet-filter-config-dialog__field-type-icon">
                    <Icon :class-name="`field-icon-${getFieldIconType(field)}`">
                      <component
                        :is="getFieldIcon(field)"
                        class="svg-icon"
                        :class="`field-icon-${getFieldIconType(field)}`"
                      />
                    </Icon>
                  </el-icon>
                  <span>{{ field.fieldName }}</span>
                </div>
              </el-option>
            </el-select>
          </div>
        </div>
      </section>

      <section class="spreadsheet-filter-config-dialog__config-panel">
        <div class="spreadsheet-filter-config-dialog__panel-title">
          <span>查询条件配置</span>
          <el-checkbox
            v-if="activeCondition"
            v-model="activeCondition.required"
            :disabled="activeCondition.displayType === 'treeSelect' && activeTreeLevelIndex > 0"
          >
            设为必填项
          </el-checkbox>
        </div>

        <template
          v-if="
            activeCondition &&
            activeConditionConfigurable &&
            !(activeCondition.displayType === 'treeSelect' && activeTreeLevelIndex > 0)
          "
        >
          <el-scrollbar class="spreadsheet-filter-config-dialog__config-scrollbar">
            <el-form class="spreadsheet-filter-config-dialog__config-form" label-width="88px" label-position="left">
            <el-form-item label="展示类型">
              <el-select v-model="activeCondition.displayType" @change="handleDisplayTypeChange">
                <el-option
                  v-for="option in activeDisplayTypeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                  :disabled="option.disabled"
                />
              </el-select>
            </el-form-item>
            <SpreadsheetFilterTextSearchConfig
              v-if="activeCondition.displayType === 'textSearch'"
              :condition="activeCondition"
            />
            <SpreadsheetFilterTreeConfig
              v-if="activeCondition.displayType === 'treeSelect'"
              :condition="activeCondition"
              :dataset-tree="datasetTree"
              :primary-color="localConfig.style.button.primaryColor"
              @activate-level="index => activateTreeLevel(index)"
              @tree-fields-change="initializeTreeLevelMappings"
            />
            <SpreadsheetFilterTimeConfig
              v-if="['time', 'timeRange'].includes(activeCondition.displayType)"
              :condition="activeCondition"
            />
            <el-form-item v-if="showOptionSource" label="选项值来源">
              <el-radio-group v-model="activeCondition.optionSource" @change="handleOptionSourceChange">
                <el-radio
                  v-for="option in optionSourceOptions"
                  :key="option.value"
                  :label="option.value"
                  :disabled="option.value === 'dataset' && !supportsDatasetOptionSource"
                >
                  {{ option.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              v-if="showOptionSource && activeCondition.optionSource === 'manual'"
              label=""
            >
              <el-popover
                ref="manualPopoverRef"
                placement="bottom-start"
                popper-class="spreadsheet-filter-manual-options"
                :width="358"
                trigger="click"
                @show="openManualOptions"
              >
                <template #reference>
                  <el-button text type="primary">
                    <el-icon><EditPen /></el-icon>
                    编辑
                  </el-button>
                </template>
                <div class="spreadsheet-filter-config-dialog__manual-panel">
                  <div class="spreadsheet-filter-config-dialog__manual-title">手工输入</div>
                  <div class="spreadsheet-filter-config-dialog__manual-label">选项值</div>
                  <div class="spreadsheet-filter-config-dialog__manual-list">
                    <div
                      v-for="(_, index) in manualOptionDraft"
                      :key="index"
                      class="spreadsheet-filter-config-dialog__manual-item"
                    >
                      <el-input v-model="manualOptionDraft[index]" maxlength="64" />
                      <el-button
                        v-if="manualOptionDraft.length > 1"
                        text
                        @click="manualOptionDraft.splice(index, 1)"
                      >
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </div>
                  </div>
                  <el-button text type="primary" @click="manualOptionDraft.push('')">
                    <el-icon><Plus /></el-icon>
                    添加选项值
                  </el-button>
                  <div class="spreadsheet-filter-config-dialog__manual-footer">
                    <el-button @click="cancelManualOptions">取消</el-button>
                    <el-button type="primary" @click="confirmManualOptions">确定</el-button>
                  </div>
                </div>
              </el-popover>
            </el-form-item>
            <template v-if="showDatasetFields">
              <el-form-item label="">
                <el-tree-select
                  v-model="activeCondition.optionDatasetId"
                  :data="datasetTree"
                  :props="datasetTreeProps"
                  placeholder="请选择数据集"
                  class="spreadsheet-filter-config-dialog__full-control"
                  filterable
                  :render-after-expand="false"
                  @change="handleOptionDatasetChange"
                >
                  <template #default="{ node, data }">
                    <div class="spreadsheet-filter-config-dialog__dataset-tree-node">
                      <el-icon>
                        <Icon v-if="!data.leaf" name="dv-folder">
                          <dvFolder class="svg-icon" />
                        </Icon>
                        <Icon v-else name="icon_dataset">
                          <DatasetIcon class="svg-icon" />
                        </Icon>
                      </el-icon>
                      <span :title="node.label">{{ node.label }}</span>
                    </div>
                  </template>
                </el-tree-select>
              </el-form-item>
              <el-form-item label="查询字段" class="spreadsheet-filter-config-dialog__dataset-field-item">
                <el-select
                  v-model="activeCondition.queryFieldId"
                  placeholder="请选择查询字段"
                  class="spreadsheet-filter-config-dialog__full-control"
                  @change="value => updateConditionFieldName('query', value)"
                >
                  <template #prefix>
                    <el-icon v-if="activeCondition.queryFieldId" class="spreadsheet-filter-config-dialog__field-type-icon">
                      <Icon>
                        <component
                          :is="getFieldIcon(optionDatasetFields.find(field => String(field.fieldId) === String(activeCondition?.queryFieldId)))"
                          class="svg-icon"
                        />
                      </Icon>
                    </el-icon>
                  </template>
                  <el-option
                    v-for="field in queryDatasetFields"
                    :key="field.fieldId"
                    :label="field.fieldName"
                    :value="field.fieldId"
                    :disabled="field.desensitized"
                  >
                    <div
                      class="spreadsheet-filter-config-dialog__field-option"
                      :title="field.desensitized ? '该字段已脱敏，不能作为查询条件' : ''"
                    >
                      <el-icon class="spreadsheet-filter-config-dialog__field-type-icon">
                        <Icon><component :is="getFieldIcon(field)" class="svg-icon" /></Icon>
                      </el-icon>
                      <span>{{ field.fieldName }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="显示字段" class="spreadsheet-filter-config-dialog__dataset-field-item">
                <el-select
                  v-model="activeCondition.displayFieldId"
                  placeholder="请选择显示字段"
                  class="spreadsheet-filter-config-dialog__full-control"
                  @change="value => updateConditionFieldName('display', value)"
                >
                  <template #prefix>
                    <el-icon v-if="activeCondition.displayFieldId" class="spreadsheet-filter-config-dialog__field-type-icon">
                      <Icon>
                        <component
                          :is="getFieldIcon(optionDatasetFields.find(field => String(field.fieldId) === String(activeCondition?.displayFieldId)))"
                          class="svg-icon"
                        />
                      </Icon>
                    </el-icon>
                  </template>
                  <el-option
                    v-for="field in displayDatasetFields"
                    :key="field.fieldId"
                    :label="field.fieldName"
                    :value="field.fieldId"
                    :disabled="field.desensitized"
                  >
                    <div
                      class="spreadsheet-filter-config-dialog__field-option"
                      :title="field.desensitized ? '该字段已脱敏，不能作为查询条件' : ''"
                    >
                      <el-icon class="spreadsheet-filter-config-dialog__field-type-icon">
                        <Icon><component :is="getFieldIcon(field)" class="svg-icon" /></Icon>
                      </el-icon>
                      <span>{{ field.fieldName }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item
                label=""
                class="spreadsheet-filter-config-dialog__dataset-field-item spreadsheet-filter-config-dialog__dataset-sort-item"
              >
                <div class="spreadsheet-filter-config-dialog__sort-config">
                  <div class="spreadsheet-filter-config-dialog__sort-label">排序字段</div>
                  <div class="spreadsheet-filter-config-dialog__inline-controls spreadsheet-filter-config-dialog__sort-controls">
                    <el-select
                      v-model="activeCondition.sortFieldId"
                      placeholder="请选择排序字段"
                      class="spreadsheet-filter-config-dialog__sort-field"
                      clearable
                      @change="value => updateConditionFieldName('sort', value)"
                    >
                      <template #prefix>
                        <el-icon v-if="activeCondition.sortFieldId" class="spreadsheet-filter-config-dialog__field-type-icon">
                          <Icon>
                            <component
                              :is="getFieldIcon(optionDatasetFields.find(field => String(field.fieldId) === String(activeCondition?.sortFieldId)))"
                              class="svg-icon"
                            />
                          </Icon>
                        </el-icon>
                      </template>
                      <el-option
                        v-for="field in sortDatasetFields"
                        :key="field.fieldId"
                        :label="field.fieldName"
                        :value="field.fieldId"
                        :disabled="field.desensitized"
                      >
                        <div
                          class="spreadsheet-filter-config-dialog__field-option"
                          :title="field.desensitized ? '该字段已脱敏，不能作为查询条件' : ''"
                        >
                          <el-icon class="spreadsheet-filter-config-dialog__field-type-icon">
                            <Icon><component :is="getFieldIcon(field)" class="svg-icon" /></Icon>
                          </el-icon>
                          <span>{{ field.fieldName }}</span>
                        </div>
                      </el-option>
                    </el-select>
                    <el-select
                      v-model="activeCondition.sortType"
                      placeholder="请选择"
                      class="spreadsheet-filter-config-dialog__sort-type"
                      :disabled="!activeCondition.sortFieldId"
                      @visible-change="handleSortTypeVisibleChange"
                      @change="handleSortTypeChange"
                    >
                      <el-option label="升序" value="asc" />
                      <el-option label="降序" value="desc" />
                      <el-option
                        label="自定义排序"
                        value="customSort"
                        :disabled="!customSortAvailable"
                        @click="handleCustomSortOptionClick"
                      />
                    </el-select>
                  </div>
                </div>
              </el-form-item>
            </template>
            <el-form-item v-if="showOptionSource" label="展示形式">
              <el-radio-group v-model="activeCondition.displayForm">
                <el-radio
                  v-for="option in displayFormOptions"
                  :key="option.value"
                  :label="option.value"
                >
                  {{ option.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="showOptionSource" label="选项值数量">
              <el-radio-group v-model="activeCondition.optionCountMode">
                <el-radio label="default">
                  默认
                  <el-tooltip content="最多展示1000个选项" placement="top">
                    <el-icon class="spreadsheet-filter-config-dialog__info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </el-radio>
                <el-radio label="all">全部</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="showOptionSource" label="选项类型">
              <el-radio-group v-model="activeCondition.multiple" @change="handleMultipleChange">
                <el-radio :label="false">单选</el-radio>
                <el-radio :label="true">多选</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              v-if="!['textSearch', 'treeSelect', 'time', 'timeRange'].includes(activeCondition.displayType)"
              label=""
            >
              <el-checkbox v-model="activeCondition.defaultValueEnabled">设置默认值</el-checkbox>
            </el-form-item>
            <template
              v-if="
                !['textSearch', 'treeSelect', 'time', 'timeRange'].includes(activeCondition.displayType) &&
                activeCondition.defaultValueEnabled
              "
            >
              <el-form-item v-if="showOptionSource" label="">
                <el-checkbox v-model="activeCondition.defaultValueFirstItem">首项</el-checkbox>
              </el-form-item>
              <el-form-item v-if="canRenderDefaultPreview" label="">
                <SpreadsheetFilterRenderer
                  v-model="activeCondition.defaultValue"
                  :condition="activeCondition"
                  :style="defaultPreviewStyle"
                  is-config
                  :disabled="activeCondition.defaultValueFirstItem"
                  class="spreadsheet-filter-config-dialog__default-preview"
                />
              </el-form-item>
            </template>
            </el-form>
          </el-scrollbar>
        </template>

        <div
          v-else-if="activeCondition?.displayType === 'treeSelect' && activeTreeLevelIndex > 0"
          class="spreadsheet-filter-config-dialog__config-type-error"
        >
          <empty-background
            description="除层级一外,其他层级无需进行查询条件配置"
            img-type="error"
          />
        </div>
        <div v-else-if="activeCondition && !activeConditionTypeState.hasField" class="spreadsheet-filter-config-dialog__config-empty">
          请先在中间区域选择关联字段
        </div>
        <div
          v-else-if="activeCondition && activeConditionTypeState.hasMixedType"
          class="spreadsheet-filter-config-dialog__config-type-error"
        >
          <empty-background description="所选字段类型不一致，无法进行查询配置" img-type="error" />
        </div>
        <div v-else class="spreadsheet-filter-config-dialog__config-empty">
          请选择或新增查询条件
        </div>
      </section>
    </div>

    <div class="spreadsheet-filter-config-dialog__footer">
      <div>
        <el-button @click="props.onCancel">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </div>
    </div>
    <SpreadsheetFilterCustomSortDialog
      v-if="activeCondition"
      :model-value="customSortDialogVisible"
      :condition="activeCondition"
      @update:model-value="handleCustomSortDialogVisibleChange"
      @confirm="confirmCustomSort"
    />
  </div>
</template>

<style scoped lang="less">
.spreadsheet-filter-config-dialog {
  width: 100%;
  min-width: 0;
  max-width: 100%;
  height: 560px;
  min-height: 560px;
  max-height: 560px;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;

  &__body {
    min-height: 0;
    flex: 1;
    display: grid;
    grid-template-columns: 208px minmax(0, 1fr) 500px;
    border-top: 1px solid #e5e6eb;
    border-bottom: 1px solid #e5e6eb;
  }

  &__condition-list,
  &__field-panel,
  &__config-panel {
    min-width: 0;
    height: 100%;
    padding: 16px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  &__condition-list {
    padding: 16px 0;
    background: #f5f6f7;
    border-right: 1px solid #e5e6eb;

    .spreadsheet-filter-config-dialog__panel-title {
      padding: 0 16px;
      box-sizing: border-box;
    }

    .spreadsheet-filter-config-dialog__empty {
      margin: 0 16px;
    }
  }

  &__field-panel {
    border-right: 1px solid #e5e6eb;
  }

  &__panel-title {
    height: 32px;
    color: #1f2329;
    font-size: 14px;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    flex-shrink: 0;
  }

  &__sub-title {
    color: #8f959e;
    font-size: 12px;
    font-weight: 400;
  }

  &__condition-items {
    flex: 1;
    min-height: 0;
    overflow: auto;
  }

  &__condition-item {
    height: 40px;
    flex-shrink: 0;
    margin-top: 8px;
    padding: 0;
    color: #1f2329;
    font-size: 14px;
    display: flex;
    align-items: center;
    cursor: pointer;

    &.is-active {
      background: var(--ed-color-primary-light-9);

      .spreadsheet-filter-config-dialog__condition-name {
        color: var(--ed-color-primary);
        font-weight: 500;
      }
    }
  }

  &__tree-level-item {
    width: 100%;
    height: 40px;
    padding: 0 16px 0 48px;
    border: 0;
    background: transparent;
    color: #1f2329;
    font: inherit;
    text-align: left;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: pointer;

    &.is-active {
      background: var(--ed-color-primary-light-9);
      color: var(--ed-color-primary);
    }
  }

  &__drag {
    width: 16px;
    height: 40px;
    flex-shrink: 0;
    color: #1f2329;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    cursor: grab;

    &:active {
      cursor: grabbing;
    }

    svg {
      width: 16px;
      height: 16px;
      fill: currentColor;
    }
  }

  &__condition-name {
    min-width: 0;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__condition-name-input {
    min-width: 0;
    flex: 1;
    margin-right: 4px;

    :deep(.ed-input__wrapper) {
      padding: 0 8px;
    }
  }

  &__condition-action {
    width: 24px;
    height: 24px;
    padding: 0;
    color: #646a73;
  }

  &__unlinked-warning {
    width: 16px;
    height: 16px;
    flex: 0 0 16px;
    color: var(--ed-color-danger, #f54a45);
    font-size: 16px;
  }

  &__tree-toggle {
    &.is-expanded {
      color: var(--ed-color-primary);
    }
  }

  &__tree-toggle-icon {
    width: 16px;
    height: 16px;
    fill: currentColor;

    :deep(path) {
      fill: currentColor;
    }
  }

  &__condition-ghost {
    opacity: 0.5;
  }

  &__empty,
  &__field-empty,
  &__config-empty {
    height: 96px;
    color: #8f959e;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 0 16px;
    box-sizing: border-box;
  }

  &__field-toolbar {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__field-empty {
    margin-top: 16px;
  }

  &__plugin-list {
    flex: 1;
    min-height: 0;
    height: auto;
    overflow: auto;
    padding: 8px 0;
    box-sizing: border-box;
  }

  &__plugin-row {
    height: 40px;
    display: grid;
    grid-template-columns: 28px minmax(104px, 1fr) minmax(96px, 120px) 172px;
    align-items: center;
    column-gap: 8px;
    padding: 0 12px;

    &:hover {
      background: #f5f7fa;
    }
  }

  &__plugin-name,
  &__dataset-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__plugin-name {
    color: #303133;
    display: flex;
    align-items: center;
    gap: 4px;

    span {
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &__plugin-type-icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
  }

  &__dataset-name {
    color: #606266;
  }

  &__plugin-field-select {
    width: 172px;

    :deep(.ed-select__prefix::after) {
      display: none;
    }
  }

  &__field-tabs {
    --ed-tabs-header-height: 32px;

    :deep(.ed-tabs__header) {
      margin-bottom: 0;
    }
  }

  &__field-option {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 8px;

    span {
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &__field-type-icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
    color: var(--ed-color-primary);

    :deep(.svg-icon) {
      width: 16px;
      height: 16px;
    }
  }

  &__info-icon {
    margin-left: 4px;
    color: #646a73;
  }

  &__default-preview {
    width: 100%;
  }

  &__manual-panel {
    min-height: 373px;
    padding: 16px 16px 64px;
    box-sizing: border-box;
    position: relative;
  }

  &__manual-title {
    margin-bottom: 24px;
    color: #1f2329;
    font-size: 16px;
    font-weight: 500;
  }

  &__manual-label {
    margin-bottom: 8px;
    color: #1f2329;
    font-size: 14px;
  }

  &__manual-list {
    max-height: 214px;
    overflow-y: auto;
  }

  &__manual-item {
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 6px;

    .ed-input {
      flex: 1;
    }
  }

  &__manual-footer {
    height: 64px;
    padding: 16px;
    border-top: 1px solid rgba(31, 35, 41, 0.15);
    box-sizing: border-box;
    position: absolute;
    right: 0;
    bottom: 0;
    left: 0;
    display: flex;
    justify-content: flex-end;
  }

  &__config-scrollbar {
    margin-top: 16px;
    flex: 1;
    min-height: 0;
  }

  &__config-form {
    padding-right: 8px;
  }

  &__full-control {
    width: 100%;
  }

  &__dataset-field-item {
    width: calc(100% - 88px);
    margin-left: 88px;

    :deep(.ed-select__prefix::after) {
      display: none;
    }
  }

  &__dataset-tree-node {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 8px;

    .ed-icon {
      width: 18px;
      height: 18px;
      flex-shrink: 0;
    }

    span {
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &__inline-controls {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__sort-config {
    width: 100%;
  }

  &__dataset-sort-item {
    :deep(.ed-form-item__label) {
      display: none;
    }

    :deep(.ed-form-item__content) {
      width: 100%;
      margin-left: 0 !important;
    }
  }

  &__sort-label {
    height: 22px;
    margin-bottom: 8px;
    color: #1f2329;
    font-size: 14px;
    line-height: 22px;
  }

  &__sort-controls {
    width: 100%;
  }

  &__sort-field {
    flex: 1;
    min-width: 0;
  }

  &__sort-type {
    width: 96px;
    flex-shrink: 0;
  }

  &__section-title {
    height: 40px;
    margin: 16px 0 12px;
    padding: 0 12px;
    background: #f5f5f7;
    color: #1f2329;
    font-size: 13px;
    display: flex;
    align-items: center;
  }

  &__config-empty {
    margin-top: 16px;
  }

  &__config-type-error {
    flex: 1;
    min-height: 0;
  }

  &__footer {
    height: 64px;
    flex-shrink: 0;
    padding: 12px 16px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    box-sizing: border-box;

    > div {
      margin-left: auto;
    }
  }
}

:global(.spreadsheet-filter-field-select-popper .ed-select-dropdown__header) {
  padding: 0 8px !important;
  border-bottom: 0 !important;
}

:global(.spreadsheet-filter-manual-options) {
  padding: 0 !important;
}
</style>
