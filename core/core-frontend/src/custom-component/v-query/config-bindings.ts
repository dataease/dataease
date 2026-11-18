// 配置态操作旧字段；不改变画布的持久化格式，也不处理运行态候选值。
export interface QueryConfigBindings {
  checkedFields?: string[]
  checkedFieldsMap?: Record<string, string>
  checkedFieldsMapArr?: Record<string, string[]>
  checkedFieldsMapStart?: Record<string, string>
  checkedFieldsMapEnd?: Record<string, string>
  checkedFieldsMapArrNum?: Record<string, string[]>
  checkedFieldsMapStartNum?: Record<string, string>
  checkedFieldsMapEndNum?: Record<string, string>
  parametersArr?: Record<string, unknown[]>
}

interface QuerySourceConfig {
  field?: { id?: string; chartId?: string }
  displayId?: string
  sortId?: string
  multiple?: boolean
  defaultValueCheck?: boolean
  optionValueSource?: number
  valueSource?: unknown[]
}

export type RangeBindingKind = 'time' | 'number'
type BindingPatch = Partial<QueryConfigBindings>
const rangeKeys = {
  time: {
    ids: 'checkedFieldsMapArr',
    start: 'checkedFieldsMapStart',
    end: 'checkedFieldsMapEnd'
  },
  number: {
    ids: 'checkedFieldsMapArrNum',
    start: 'checkedFieldsMapStartNum',
    end: 'checkedFieldsMapEndNum'
  }
} as const

export const readRangeBinding = (
  config: QueryConfigBindings,
  componentId: string,
  kind: RangeBindingKind
) => {
  const keys = rangeKeys[kind]
  return {
    fieldIds: [...(config[keys.ids]?.[componentId] || [])],
    start: config[keys.start]?.[componentId],
    end: config[keys.end]?.[componentId]
  }
}

const rangePatch = (
  config: QueryConfigBindings,
  componentId: string,
  kind: RangeBindingKind,
  fieldIds: string[],
  start: string | undefined,
  end: string | undefined
): BindingPatch => {
  const keys = rangeKeys[kind]
  return {
    [keys.ids]: { ...config[keys.ids], [componentId]: fieldIds },
    [keys.start]: { ...config[keys.start], [componentId]: start },
    [keys.end]: { ...config[keys.end], [componentId]: end }
  }
}

const clearOtherRange = (
  config: QueryConfigBindings,
  componentId: string,
  kind: RangeBindingKind
): BindingPatch => {
  const otherKind = kind === 'time' ? 'number' : 'time'
  const { fieldIds, start, end } = readRangeBinding(config, componentId, otherKind)
  // 也清除历史配置中数组已空、起止字段仍残留的冲突状态。
  return fieldIds.length || start || end
    ? rangePatch(config, componentId, otherKind, [], '', '')
    : {}
}

export const createRangeSelectionPatch = (
  config: QueryConfigBindings,
  componentId: string,
  kind: RangeBindingKind,
  fieldIds: string[]
): BindingPatch => {
  const previous = readRangeBinding(config, componentId, kind)
  let { start, end } = previous
  if (previous.start) end = fieldIds.find(id => id !== previous.start)
  if (previous.end) start = fieldIds.find(id => id !== previous.end)
  if (!fieldIds.length) start = end = ''
  return {
    ...clearOtherRange(config, componentId, kind),
    ...rangePatch(config, componentId, kind, [...fieldIds], start, end),
    ...(!fieldIds.length
      ? {
          checkedFieldsMap: { ...config.checkedFieldsMap, [componentId]: '' },
          parametersArr: { ...config.parametersArr, [componentId]: [] }
        }
      : {})
  }
}

export const createRangeParameterPatch = (
  config: QueryConfigBindings,
  componentId: string,
  kind: RangeBindingKind,
  fieldId: string,
  endpoint: 0 | 1 | 2
): BindingPatch => {
  const previous = readRangeBinding(config, componentId, kind)
  if (endpoint === 0) {
    return {
      ...clearOtherRange(config, componentId, kind),
      ...rangePatch(config, componentId, kind, [], '', ''),
      checkedFieldsMap: { ...config.checkedFieldsMap, [componentId]: fieldId }
    }
  }
  const fieldIds = [...new Set([...previous.fieldIds, fieldId])]
  const other =
    fieldIds.length === 1
      ? ''
      : fieldIds.length === 2
      ? fieldIds.find(id => id !== fieldId)
      : fieldId
  return {
    ...clearOtherRange(config, componentId, kind),
    ...rangePatch(
      config,
      componentId,
      kind,
      fieldIds,
      endpoint === 1 ? fieldId : other,
      endpoint === 2 ? fieldId : other
    )
  }
}

export const hasQueryBindings = (config: QueryConfigBindings, mode: 'some' | 'every') => {
  const ids = config.checkedFields || []
  return !!ids.length && ids[mode](id => !!config.checkedFieldsMap?.[id])
}

export const getSourceBindingError = (
  config: QuerySourceConfig,
  manualValues: 'raw' | 'nonempty' = 'raw'
): 'missing-field' | 'missing-manual-values' | undefined => {
  if (config.optionValueSource === 1 && !config.field?.id) return 'missing-field'
  if (
    config.optionValueSource === 2 &&
    !(manualValues === 'nonempty' ? config.valueSource?.filter(Boolean) : config.valueSource)
      ?.length
  )
    return 'missing-manual-values'
}

export const createDatasetSourcePatch = (config: QuerySourceConfig) => ({
  field: { ...config.field, id: '', chartId: undefined },
  displayId: '',
  sortId: ''
})

export const createFieldSourcePatch = (config: QuerySourceConfig, chartId?: string) => ({
  field: { ...config.field, chartId },
  ...(config.defaultValueCheck
    ? {
        defaultValue: config.multiple ? [] : undefined,
        displayId: config.displayId || config.field?.id
      }
    : {})
})

export const createOptionSourcePatch = (config: QuerySourceConfig) => {
  const value = config.multiple ? [] : undefined
  return { defaultValue: value, selectValue: value }
}
