import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterDisplayType,
  SpreadsheetFilterLinkedField,
  SpreadsheetFilterTextSearchClause,
  SpreadsheetFilterTextSearchConditionType
} from '../../../types/plugin'

export interface SpreadsheetFilterDisplayTypeOption {
  label: string
  value: SpreadsheetFilterDisplayType
  disabled: boolean
}

export interface SpreadsheetFilterFieldTypeState {
  hasField: boolean
  hasMixedType: boolean
  fieldKind?: 'text' | 'number' | 'date'
}

const DISPLAY_TYPE_OPTIONS: Array<{
  label: string
  value: SpreadsheetFilterDisplayType
  kinds: Array<'text' | 'number' | 'date'>
}> = [
  { label: '文本下拉', value: 'textSelect', kinds: ['text'] },
  { label: '文本搜索', value: 'textSearch', kinds: ['text'] },
  { label: '下拉树', value: 'treeSelect', kinds: ['text'] },
  { label: '数字下拉', value: 'numberSelect', kinds: ['number'] },
  { label: '数字范围', value: 'numberRange', kinds: ['number'] },
  { label: '时间', value: 'time', kinds: ['date'] },
  { label: '时间范围', value: 'timeRange', kinds: ['date'] }
]

const LEGACY_DISPLAY_TYPE_MAP: Record<string, SpreadsheetFilterDisplayType> = {
  select: 'textSelect',
  text: 'textSearch',
  number: 'numberSelect',
  0: 'textSelect',
  8: 'textSearch',
  9: 'treeSelect',
  2: 'numberSelect',
  5: 'numberSelect',
  22: 'numberRange',
  1: 'time',
  7: 'timeRange'
}

const DISPLAY_TYPE_VALUES = DISPLAY_TYPE_OPTIONS.map(option => option.value)

export const normalizeSpreadsheetFilterTextSearchClauses = (
  clauses: Partial<SpreadsheetFilterTextSearchClause>[] | undefined,
  conditionType: SpreadsheetFilterTextSearchConditionType = 'single'
): SpreadsheetFilterTextSearchClause[] => {
  const defaults: SpreadsheetFilterTextSearchClause[] = [
    { operator: 'eq', value: '' },
    { operator: 'like', value: '' }
  ]
  const count = conditionType === 'single' ? 1 : 2
  return defaults.slice(0, count).map((fallback, index) => {
    const clause = clauses?.[index]
    const operator = clause?.operator
    // 两种合法匹配方式都需要保留，避免第二个条件被默认的模糊匹配覆盖。
    let normalizedOperator = fallback.operator
    if (operator === 'eq' || operator === 'like') {
      normalizedOperator = operator
    }
    return {
      operator: normalizedOperator,
      value: String(clause?.value ?? '')
    }
  })
}

export const normalizeSpreadsheetFilterDisplayType = (
  displayType?: string
): SpreadsheetFilterDisplayType =>
  LEGACY_DISPLAY_TYPE_MAP[displayType || ''] ||
  (DISPLAY_TYPE_VALUES.includes(displayType as SpreadsheetFilterDisplayType)
    ? (displayType as SpreadsheetFilterDisplayType)
    : 'textSelect')

export const getSpreadsheetFilterFieldKind = (
  field?: Pick<SpreadsheetFilterLinkedField, 'deType' | 'groupType'>
): 'text' | 'number' | 'date' => {
  if ([2, 3, 4].includes(Number(field?.deType)) || field?.groupType === 'q') {
    return 'number'
  }
  if (Number(field?.deType) === 1) {
    return 'date'
  }
  return 'text'
}

export const getSpreadsheetFilterFieldTypeState = (
  fields: SpreadsheetFilterLinkedField[] = []
): SpreadsheetFilterFieldTypeState => {
  const kinds = [...new Set(fields.map(getSpreadsheetFilterFieldKind))]
  const getCompatibilityType = (field?: SpreadsheetFilterLinkedField) =>
    [2, 3].includes(Number(field?.deType)) ? 'number-2-3' : String(field?.deType ?? '')
  const firstCompatibilityType = getCompatibilityType(fields[0])
  const hasMixedType = fields
    .slice(1)
    .some(field => getCompatibilityType(field) !== firstCompatibilityType)
  return {
    hasField: !!fields.length,
    hasMixedType,
    fieldKind: !hasMixedType && kinds.length === 1 ? kinds[0] : undefined
  }
}

export const getSpreadsheetFilterDisplayTypeOptions = (
  condition?: SpreadsheetFilterCondition
): SpreadsheetFilterDisplayTypeOption[] => {
  const typeState = getSpreadsheetFilterFieldTypeState(condition?.linkedFields)
  return DISPLAY_TYPE_OPTIONS.map(option => ({
    label: option.label,
    value: option.value,
    disabled: !typeState.hasField || typeState.hasMixedType || !option.kinds.includes(typeState.fieldKind!)
  }))
}

export const getFirstEnabledSpreadsheetFilterDisplayType = (
  condition?: SpreadsheetFilterCondition
): SpreadsheetFilterDisplayType | undefined =>
  getSpreadsheetFilterDisplayTypeOptions(condition).find(option => !option.disabled)?.value

export const isSpreadsheetFilterSelectType = (displayType?: SpreadsheetFilterDisplayType) =>
  ['textSelect', 'numberSelect'].includes(displayType || 'textSelect')

export const shouldShowSpreadsheetFilterOptionSource = (condition?: SpreadsheetFilterCondition) =>
  isSpreadsheetFilterSelectType(condition?.displayType)

export const supportsSpreadsheetFilterDatasetOptionSource = (
  condition?: SpreadsheetFilterCondition
) => ['textSelect', 'numberSelect'].includes(condition?.displayType || '')

export const shouldShowSpreadsheetFilterDatasetFields = (condition?: SpreadsheetFilterCondition) =>
  supportsSpreadsheetFilterDatasetOptionSource(condition) && condition?.optionSource === 'dataset'

type SpreadsheetFilterDatasetFieldRole = 'query' | 'display' | 'sort'
type SpreadsheetFilterDatasetFieldLike = {
  fieldId: string | number
  deType?: number
  desensitized?: boolean
}

const getDatasetRestrictionKind = (
  condition?: SpreadsheetFilterCondition
): 'text' | 'number' | 'location' => {
  if (Number(condition?.linkedFields[0]?.deType) === 5) return 'location'
  return condition?.displayType === 'numberSelect' ? 'number' : 'text'
}

export const isSpreadsheetFilterDatasetFieldAllowed = (
  condition: SpreadsheetFilterCondition | undefined,
  field: SpreadsheetFilterDatasetFieldLike | undefined,
  role: SpreadsheetFilterDatasetFieldRole
) => {
  if (!field) return false
  if (role === 'sort') return true
  const deType = Number(field.deType)
  const kind = getDatasetRestrictionKind(condition)
  if (kind === 'location') return deType === 5
  if (kind === 'number') {
    return role === 'query' ? [2, 3, 4].includes(deType) : [0, 2, 3, 4].includes(deType)
  }
  return role === 'query'
    ? [0, 7].includes(deType)
    : [0, 2, 3, 4, 7].includes(deType)
}

export const filterSpreadsheetFilterDatasetFields = <T extends SpreadsheetFilterDatasetFieldLike>(
  condition: SpreadsheetFilterCondition | undefined,
  fields: T[],
  role: SpreadsheetFilterDatasetFieldRole
) => fields.filter(field => isSpreadsheetFilterDatasetFieldAllowed(condition, field, role))

export const normalizeSpreadsheetFilterConditionByRules = (
  condition: SpreadsheetFilterCondition
): SpreadsheetFilterCondition => {
  condition.displayType = normalizeSpreadsheetFilterDisplayType(condition.displayType)
  condition.textSearchConditionType = ['and', 'or'].includes(condition.textSearchConditionType)
    ? condition.textSearchConditionType
    : 'single'
  condition.textSearchDefaultClauses = normalizeSpreadsheetFilterTextSearchClauses(
    condition.textSearchDefaultClauses,
    condition.textSearchConditionType
  )
  condition.timeGranularity = ['year', 'month', 'date', 'datetime'].includes(
    condition.timeGranularity
  )
    ? condition.timeGranularity
    : 'date'
  condition.timeRangeGranularity = [
    'yearrange',
    'monthrange',
    'daterange',
    'datetimerange'
  ].includes(condition.timeRangeGranularity)
    ? condition.timeRangeGranularity
    : 'daterange'
  condition.timeDefaultType = condition.timeDefaultType === 'dynamic' ? 'dynamic' : 'fixed'
  condition.timeDynamicDefault ||= {
    offset: { value: 0, unit: 'day', direction: 'before', relativeToCurrent: 'custom' }
  }
  condition.timeRangeDynamicDefault ||= {
    start: { value: 7, unit: 'day', direction: 'before', relativeToCurrent: 'custom' },
    end: { value: 0, unit: 'day', direction: 'before', relativeToCurrent: 'custom' }
  }
  condition.timeFilterRangeEnabled = !!condition.timeFilterRangeEnabled
  condition.timeFilterRange ||= { intervalType: 'none' }
  condition.treeFields = (condition.treeFields || []).slice(0, 5).map((field, order) => ({
    ...field,
    order
  }))
  const treeFieldIds = new Set(condition.treeFields.map(field => String(field.fieldId)))
  condition.treeLevelMappings = (condition.treeLevelMappings || [])
    .filter(mapping => treeFieldIds.has(String(mapping.treeFieldId)))
  if (condition.treeFields.length) {
    const firstTreeFieldId = condition.treeFields[0].fieldId
    const firstMapping = condition.treeLevelMappings.find(
      mapping => String(mapping.treeFieldId) === String(firstTreeFieldId)
    )
    if (firstMapping) {
      firstMapping.linkedFields = condition.linkedFields
    } else {
      condition.treeLevelMappings.unshift({
        treeFieldId: firstTreeFieldId,
        linkedFields: condition.linkedFields
      })
    }
  }
  const firstEnabledDisplayType = getFirstEnabledSpreadsheetFilterDisplayType(condition)
  const displayTypeDisabled = getSpreadsheetFilterDisplayTypeOptions(condition).some(
    option => option.value === condition.displayType && option.disabled
  )
  if (firstEnabledDisplayType && displayTypeDisabled) {
    condition.displayType = firstEnabledDisplayType
  }
  if (!shouldShowSpreadsheetFilterOptionSource(condition)) {
    condition.optionSource = 'auto'
    condition.queryFieldId = undefined
    condition.queryFieldName = undefined
    condition.displayFieldId = undefined
    condition.displayFieldName = undefined
    condition.sortFieldId = undefined
    condition.sortFieldName = undefined
  }
  if (
    condition.optionSource === 'dataset' &&
    !supportsSpreadsheetFilterDatasetOptionSource(condition)
  ) {
    condition.optionSource = 'auto'
  }
  if (!shouldShowSpreadsheetFilterDatasetFields(condition)) {
    condition.optionDatasetId = undefined
    condition.optionDatasetName = undefined
    condition.queryFieldId = undefined
    condition.queryFieldName = undefined
    condition.displayFieldId = undefined
    condition.displayFieldName = undefined
    condition.sortFieldId = undefined
    condition.sortFieldName = undefined
  }
  return condition
}
