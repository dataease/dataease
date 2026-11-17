import { cloneDeep } from 'lodash-es'
import dayjs from 'dayjs'
import { useI18n } from '@/hooks/web/useI18n'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterConfig,
  SpreadsheetFilterTreePath
} from '../../../types/plugin'
import { enumSpreadsheetFilterValueObj } from '../../../api/filter-option'
import { getSpreadsheetFilterEmptyValue } from './filter-values'
import { getTimeValueFormat } from './time-filter'
import { normalizeSpreadsheetFilterTextSearchClauses } from './filter-condition-rules'
import {
  isSpreadsheetOuterParamTreePathAllowed,
  matchesSpreadsheetOuterParamOption
} from './outer-param-options'

const invalidQueryValue = (name: string): Error =>
  new Error(`${useI18n().t('spreadsheet.invalid_query_value')} [${name}]`)

const toTreeValue = (condition: SpreadsheetFilterCondition, values: string[]) => {
  const paths: SpreadsheetFilterTreePath[] = values.map(value => {
    const levels = value.includes('-de-') ? value.split('-de-') : value.split(',')
    if (!condition.treeFields.length || levels.length > condition.treeFields.length) {
      throw invalidQueryValue(condition.name)
    }
    return levels.map((level, index) => ({
      treeFieldId: condition.treeFields[index].fieldId,
      value: level
    }))
  })
  return condition.multiple ? paths : paths[0] || []
}

const toQueryValue = (condition: SpreadsheetFilterCondition, values: string[]): unknown => {
  if (!values.length) return getSpreadsheetFilterEmptyValue(condition)
  if (condition.displayType === 'treeSelect') return toTreeValue(condition, values)
  if (condition.displayType === 'textSearch') {
    const clauses = normalizeSpreadsheetFilterTextSearchClauses(
      condition.textSearchDefaultClauses,
      condition.textSearchConditionType
    )
    if (clauses.length > 1 && values.length !== clauses.length)
      throw invalidQueryValue(condition.name)
    condition.textSearchDefaultClauses = clauses.map((clause, index) => ({
      ...clause,
      value: clauses.length === 1 ? values.join(',') : values[index]
    }))
    return {
      conditionType: condition.textSearchConditionType,
      clauses: cloneDeep(condition.textSearchDefaultClauses)
    }
  }
  if (condition.displayType === 'numberRange') {
    if (
      values.length !== 2 ||
      values.some(value => !value.trim() || !Number.isFinite(Number(value))) ||
      Number(values[0]) > Number(values[1])
    ) {
      throw invalidQueryValue(condition.name)
    }
    return values
  }
  if (condition.displayType === 'time' || condition.displayType === 'timeRange') {
    const range = condition.displayType === 'timeRange'
    if (range && values.length !== 2) throw invalidQueryValue(condition.name)
    const format = getTimeValueFormat(
      range ? condition.timeRangeGranularity : condition.timeGranularity
    )
    const dates = values.slice(0, range ? 2 : 1).map(value => {
      const date = dayjs(/^\d{13}$/.test(value) ? Number(value) : value)
      if (!date.isValid()) throw invalidQueryValue(condition.name)
      return date.format(format)
    })
    if (range && dayjs(dates[0]).isAfter(dayjs(dates[1]))) throw invalidQueryValue(condition.name)
    return range ? dates : dates[0]
  }
  return condition.multiple ? values : values[0]
}

const filterQueryValue = (
  condition: SpreadsheetFilterCondition,
  value: unknown,
  allowedValues: unknown[]
): unknown => {
  if (!condition.optionFilter?.length) return value
  if (condition.displayType === 'treeSelect') {
    let paths: SpreadsheetFilterTreePath[] = []
    if (Array.isArray(value)) {
      paths = condition.multiple ? value : [value]
    }
    const filtered = paths.filter(
      path =>
        Array.isArray(path) && isSpreadsheetOuterParamTreePathAllowed(path, condition.optionFilter)
    )
    return condition.multiple ? filtered : filtered[0] || []
  }
  const values = Array.isArray(value) ? value : [value]
  // 映射查询按展示值裁剪候选，真正的查询值仍保持 queryId 对应的值。
  const filtered = values.filter(
    item => allowedValues.length > 0 && matchesSpreadsheetOuterParamOption(item, allowedValues)
  )
  return condition.multiple ? filtered : filtered[0] ?? ''
}

export const applySpreadsheetQueryParamsToFilterConfig = async (
  config: SpreadsheetFilterConfig,
  queryValues: Record<string, { self?: string[]; filter?: string[] }>
): Promise<SpreadsheetFilterConfig> => {
  const conditions = await Promise.all(
    config.conditions.map(async original => {
      const params = queryValues[original.id]
      if (!params) return original
      const condition = cloneDeep(original)
      if (params.filter) {
        condition.optionFilter = params.filter.map(value =>
          condition.displayType === 'treeSelect' ? value.replace(/,/g, '-de-') : value
        )
      }
      const queryId = condition.queryFieldId
      const displayId = condition.displayFieldId
      const mapped =
        ['textSelect', 'numberSelect'].includes(condition.displayType) &&
        condition.optionSource === 'dataset' &&
        queryId !== undefined &&
        displayId !== undefined &&
        String(queryId) !== String(displayId)
      let rows: Record<string, unknown>[] = []
      if (
        mapped &&
        (params.self?.length || params.filter?.length) &&
        queryId !== undefined &&
        displayId !== undefined
      ) {
        rows = await enumSpreadsheetFilterValueObj({
          queryId,
          displayId,
          resultMode: 1,
          searchText: ''
        })
      }
      if (params.self) {
        let values = params.self
        if (mapped && values.length && queryId !== undefined && displayId !== undefined) {
          values = values.map(value => {
            if (rows.some(row => matchesSpreadsheetOuterParamOption(row[queryId], [value]))) {
              return value
            }
            const row = rows.find(item =>
              matchesSpreadsheetOuterParamOption(item[displayId], [value])
            )
            return row ? String(row[queryId]) : value
          })
        }
        condition.defaultValueEnabled = true
        condition.defaultValueFirstItem = false
        condition.timeDefaultType = 'fixed'
        condition.defaultValue = toQueryValue(condition, values)
        if (condition.displayType === 'textSearch' && !values.length) {
          condition.textSearchDefaultClauses = normalizeSpreadsheetFilterTextSearchClauses(
            [],
            condition.textSearchConditionType
          )
        }
        condition.selectValue = cloneDeep(condition.defaultValue)
      }
      if (condition.optionFilter?.length) {
        let allowed: unknown[] = condition.optionFilter
        if (mapped && queryId !== undefined && displayId !== undefined) {
          allowed = rows
            .filter(row =>
              matchesSpreadsheetOuterParamOption(row[displayId], condition.optionFilter)
            )
            .map(row => row[queryId])
        }
        condition.defaultValue = filterQueryValue(condition, condition.defaultValue, allowed)
        condition.selectValue = filterQueryValue(condition, condition.selectValue, allowed)
      }
      return condition
    })
  )
  return { ...config, conditions }
}
