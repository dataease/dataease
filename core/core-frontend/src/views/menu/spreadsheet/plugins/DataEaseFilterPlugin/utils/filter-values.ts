import { cloneDeep } from 'lodash-es'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterConfig,
  SpreadsheetFilterTextSearchValue,
  SpreadsheetFilterTreePath
} from '../../../types/plugin'
import {
  enumSpreadsheetFilterValueObj,
  getSpreadsheetFilterEnumValue,
  getSpreadsheetFilterFieldTree
} from '../../../api/filter-option'
import { normalizeSpreadsheetFilterTextSearchClauses } from './filter-condition-rules'
import { resolveDynamicTimeDefault } from './time-filter'

export type SpreadsheetFilterValueMap = Record<string, unknown>

const getCustomSortValueKey = (value: unknown) =>
  `${typeof value}:${String(value ?? '').toLowerCase()}`

export const sortSpreadsheetFilterDatasetRows = (
  rows: Record<string, unknown>[],
  condition: SpreadsheetFilterCondition
) => {
  const sortFieldId = condition.sortFieldId
  if (condition.sortType !== 'customSort' || !sortFieldId || !condition.sortList?.length) {
    return rows
  }

  const sortRanks = new Map(
    condition.sortList.map((value, index) => [getCustomSortValueKey(value), index])
  )
  // 未加入自定义列表的值放在已配置值之后，并维持接口返回的相对顺序。
  return rows
    .map((row, index) => ({ row, index }))
    .sort((left, right) => {
      const leftRank = sortRanks.get(getCustomSortValueKey(left.row[sortFieldId]))
      const rightRank = sortRanks.get(getCustomSortValueKey(right.row[sortFieldId]))
      if (leftRank === undefined && rightRank === undefined) return left.index - right.index
      if (leftRank === undefined) return 1
      if (rightRank === undefined) return -1
      return leftRank - rightRank || left.index - right.index
    })
    .map(item => item.row)
}

export interface SpreadsheetFilterValidationIssue {
  condition: SpreadsheetFilterCondition
  type: 'required' | 'incomplete-range' | 'invalid-number-range' | 'incomplete-text'
}

const hasOwnValue = (values: SpreadsheetFilterValueMap, conditionId: string) =>
  Object.prototype.hasOwnProperty.call(values, conditionId)

export const isSpreadsheetFilterEmptyValue = (value: unknown): boolean => {
  if (Array.isArray(value)) {
    return value.length === 0 || value.every(item => isSpreadsheetFilterEmptyValue(item))
  }
  return value === undefined || value === null || value === ''
}

export const getSpreadsheetFilterTextSearchValue = (
  condition: SpreadsheetFilterCondition,
  useDefault: boolean
): SpreadsheetFilterTextSearchValue => ({
  conditionType: condition.textSearchConditionType,
  clauses: normalizeSpreadsheetFilterTextSearchClauses(
    useDefault ? condition.textSearchDefaultClauses : [],
    condition.textSearchConditionType
  )
})

export const getSpreadsheetFilterEmptyValue = (
  condition: SpreadsheetFilterCondition
): unknown => {
  if (condition.displayType === 'textSearch') {
    return getSpreadsheetFilterTextSearchValue(condition, false)
  }
  if (condition.displayType === 'treeSelect') {
    return []
  }
  if (condition.multiple || ['numberRange', 'timeRange'].includes(condition.displayType)) {
    return []
  }
  return ''
}

export const getSpreadsheetFilterDefaultValue = (
  condition: SpreadsheetFilterCondition
): unknown => {
  if (condition.displayType === 'textSearch') {
    return getSpreadsheetFilterTextSearchValue(condition, true)
  }
  if (!condition.defaultValueEnabled) {
    return getSpreadsheetFilterEmptyValue(condition)
  }
  if (
    ['time', 'timeRange'].includes(condition.displayType) &&
    condition.timeDefaultType === 'dynamic'
  ) {
    return resolveDynamicTimeDefault(condition)
  }
  return cloneDeep(condition.defaultValue ?? getSpreadsheetFilterEmptyValue(condition))
}

export const getSpreadsheetFilterInitialValues = (
  config?: SpreadsheetFilterConfig
): SpreadsheetFilterValueMap => {
  const values: SpreadsheetFilterValueMap = {}
  config?.conditions.forEach(condition => {
    if (
      condition.defaultValueEnabled &&
      condition.defaultValueFirstItem &&
      !isSpreadsheetFilterEmptyValue(condition.selectValue)
    ) {
      values[condition.id] = cloneDeep(condition.selectValue)
      return
    }
    if (condition.displayType === 'textSearch' || condition.defaultValueEnabled) {
      values[condition.id] = getSpreadsheetFilterDefaultValue(condition)
      return
    }
    values[condition.id] = !isSpreadsheetFilterEmptyValue(condition.selectValue)
      ? cloneDeep(condition.selectValue)
      : getSpreadsheetFilterEmptyValue(condition)
  })
  return values
}

export const getSpreadsheetFilterSelectedValues = (
  config?: SpreadsheetFilterConfig
): SpreadsheetFilterValueMap => {
  const values: SpreadsheetFilterValueMap = {}
  config?.conditions.forEach(condition => {
    values[condition.id] = condition.selectValue !== undefined && condition.selectValue !== null
      ? cloneDeep(condition.selectValue)
      : getSpreadsheetFilterDefaultValue(condition)
  })
  return values
}

const resolveSelectFirstItem = async (
  condition: SpreadsheetFilterCondition
): Promise<unknown> => {
  if (condition.optionSource === 'manual') {
    const first = condition.manualOptions.find(value => value !== undefined && value !== null)
    return first === undefined ? getSpreadsheetFilterEmptyValue(condition) : String(first)
  }

  const resultMode = condition.optionCountMode === 'all' ? 1 : 0
  if (condition.optionSource === 'dataset') {
    const queryId = condition.queryFieldId
    const displayId = condition.displayFieldId
    const sortId = condition.sortFieldId
    if (!queryId || !displayId) {
      return getSpreadsheetFilterEmptyValue(condition)
    }
    const rows = await enumSpreadsheetFilterValueObj({
      queryId,
      displayId,
      sortId,
      sort: condition.sortType === 'customSort' ? 'asc' : condition.sortType || 'asc',
      resultMode,
      searchText: ''
    })
    const sortedRows = sortSpreadsheetFilterDatasetRows(rows, condition)
    const first = sortedRows.find(row => {
      const value = row[queryId]
      return value !== undefined && value !== null
    })
    return first ? String(first[queryId]) : getSpreadsheetFilterEmptyValue(condition)
  }

  const fieldIds = condition.linkedFields
    .map(field => field.fieldId)
    .filter(fieldId => fieldId !== undefined && fieldId !== null && fieldId !== '')
  if (!fieldIds.length) {
    return getSpreadsheetFilterEmptyValue(condition)
  }
  const values = await getSpreadsheetFilterEnumValue({ fieldIds, resultMode })
  const first = values.find(value => value !== undefined && value !== null)
  return first === undefined ? getSpreadsheetFilterEmptyValue(condition) : String(first)
}

const resolveTreeFirstItem = async (
  condition: SpreadsheetFilterCondition
): Promise<unknown> => {
  const fieldIds = condition.treeFields
    .map(field => field.fieldId)
    .filter(fieldId => fieldId !== undefined && fieldId !== null && fieldId !== '')
  if (!fieldIds.length) {
    return getSpreadsheetFilterEmptyValue(condition)
  }
  const rows = await getSpreadsheetFilterFieldTree({
    fieldIds,
    resultMode: condition.optionCountMode === 'all' ? 1 : 0
  })
  const first = rows[0]
  const value = first?.id ?? first?.value ?? first?.text ?? first?.label
  if (value === undefined || value === null || value === '') {
    return getSpreadsheetFilterEmptyValue(condition)
  }
  const path: SpreadsheetFilterTreePath = [{
    treeFieldId: condition.treeFields[0].fieldId,
    value: value as string | number
  }]
  return condition.multiple ? [path] : path
}

const resolveConditionDefaultValue = async (
  condition: SpreadsheetFilterCondition
): Promise<unknown> => {
  if (!condition.defaultValueFirstItem) {
    return getSpreadsheetFilterDefaultValue(condition)
  }
  if (condition.displayType === 'treeSelect') {
    return resolveTreeFirstItem(condition)
  }
  if (['textSelect', 'numberSelect'].includes(condition.displayType)) {
    const first = await resolveSelectFirstItem(condition)
    return condition.multiple && !isSpreadsheetFilterEmptyValue(first) ? [first] : first
  }
  return getSpreadsheetFilterDefaultValue(condition)
}

export const resolveSpreadsheetFilterValues = async (
  config: SpreadsheetFilterConfig,
  currentValues: SpreadsheetFilterValueMap = {},
  useStoredSelection = true
): Promise<SpreadsheetFilterValueMap> => {
  const entries = await Promise.all(config.conditions.map(async condition => {
    if (condition.displayType === 'textSearch' || condition.defaultValueEnabled) {
      try {
        return [condition.id, await resolveConditionDefaultValue(condition)] as const
      } catch (error) {
        return [condition.id, getSpreadsheetFilterDefaultValue(condition)] as const
      }
    }
    if (hasOwnValue(currentValues, condition.id)) {
      return [condition.id, cloneDeep(currentValues[condition.id])] as const
    }
    if (useStoredSelection && !isSpreadsheetFilterEmptyValue(condition.selectValue)) {
      return [condition.id, cloneDeep(condition.selectValue)] as const
    }
    return [condition.id, getSpreadsheetFilterEmptyValue(condition)] as const
  }))
  return Object.fromEntries(entries)
}

export const getSpreadsheetFilterValidationIssue = (
  config: SpreadsheetFilterConfig | undefined,
  values: SpreadsheetFilterValueMap
): SpreadsheetFilterValidationIssue | undefined => {
  return config?.conditions.reduce<SpreadsheetFilterValidationIssue | undefined>((issue, condition) => {
    if (issue) return issue
    const value = values[condition.id]
    if (condition.displayType === 'textSearch') {
      const textValue = value as Partial<SpreadsheetFilterTextSearchValue> | undefined
      const clauses = normalizeSpreadsheetFilterTextSearchClauses(
        textValue?.clauses,
        condition.textSearchConditionType
      )
      const filledCount = clauses.filter(clause => !!clause.value.trim()).length
      if (filledCount > 0 && filledCount < clauses.length) {
        return { condition, type: 'incomplete-text' }
      }
      if (condition.required && filledCount !== clauses.length) {
        return { condition, type: 'required' }
      }
      return undefined
    }
    if (['numberRange', 'timeRange'].includes(condition.displayType)) {
      const range = Array.isArray(value) ? value.slice(0, 2) : []
      const filledCount = range.filter(item => !isSpreadsheetFilterEmptyValue(item)).length
      if (filledCount === 1) {
        return { condition, type: 'incomplete-range' }
      }
      if (condition.required && filledCount !== 2) {
        return { condition, type: 'required' }
      }
      if (
        condition.displayType === 'numberRange' &&
        filledCount === 2 &&
        Number(range[0]) > Number(range[1])
      ) {
        return { condition, type: 'invalid-number-range' }
      }
      return undefined
    }
    if (condition.required && isSpreadsheetFilterEmptyValue(value)) {
      return { condition, type: 'required' }
    }
    return undefined
  }, undefined)
}
