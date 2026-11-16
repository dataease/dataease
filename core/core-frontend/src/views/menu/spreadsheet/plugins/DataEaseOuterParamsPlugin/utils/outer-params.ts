import { cloneDeep } from 'lodash-es'
import dayjs from 'dayjs'
import { useI18n } from '@/hooks/web/useI18n'
import type {
  SpreadsheetOuterParamsConfig,
  SpreadsheetOuterParamsRuntime
} from '../../../types/outer-params'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterConfig,
  SpreadsheetFilterTreePath
} from '../../../types/plugin'
import { enumSpreadsheetFilterValueObj } from '../../../api/filter-option'
import { getSpreadsheetFilterEmptyValue } from '../../DataEaseFilterPlugin/utils/filter-values'
import { getTimeValueFormat } from '../../DataEaseFilterPlugin/utils/time-filter'
import { normalizeSpreadsheetFilterTextSearchClauses } from '../../DataEaseFilterPlugin/utils/filter-condition-rules'
import {
  isSpreadsheetOuterParamTreePathAllowed,
  matchesSpreadsheetOuterParamOption
} from '../../DataEaseFilterPlugin/utils/outer-param-options'

// shortcut: 当前查询后端不支持空值及前后缀操作符，后端支持后再扩展白名单。
const FIELD_OPERATORS = new Set([
  'eq',
  'not_eq',
  'lt',
  'le',
  'gt',
  'ge',
  'in',
  'not in',
  'like',
  'not like',
  'between'
])

const invalidParam = (name: string): Error => {
  const { t } = useI18n()
  return new Error(`${t('visualization.outer_param_decode_error')} [${name}]`)
}

const normalizeValues = (value: unknown, name: string): string[] => {
  const values = Array.isArray(value) ? value : [value]
  return values.flatMap(item => {
    if (item === undefined || item === null || item === '') return []
    if (!['string', 'number', 'boolean'].includes(typeof item)) throw invalidParam(name)
    if (typeof item === 'number' && !Number.isFinite(item)) throw invalidParam(name)
    return [String(item)]
  })
}

export const resolveSpreadsheetOuterParams = (
  config: SpreadsheetOuterParamsConfig,
  attachParams: Record<string, unknown> = {}
): SpreadsheetOuterParamsRuntime => {
  const runtime: SpreadsheetOuterParamsRuntime = {
    queryValues: Object.create(null),
    fieldFilters: Object.create(null)
  }
  if (!config.checked) return runtime
  const version = attachParams.outerParamsVersion ?? 'v1'
  if (version !== 'v1' && version !== 'v2') throw invalidParam('outerParamsVersion')

  config.outerParamsInfoArray
    .filter(param => param.checked)
    .forEach(param => {
      const name = param.paramName
      let rawValue = Object.prototype.hasOwnProperty.call(attachParams, name)
        ? attachParams[name]
        : undefined
      let operator = 'in'
      if (version === 'v2' && rawValue !== undefined && rawValue !== null) {
        if (typeof rawValue !== 'object' || Array.isArray(rawValue)) throw invalidParam(name)
        const item = rawValue as { operator?: unknown; value?: unknown }
        if (item.operator !== undefined) {
          if (typeof item.operator !== 'string') throw invalidParam(name)
          operator = item.operator
        }
        rawValue = item.value
      }
      let values = normalizeValues(rawValue, name)
      // 必填检查先于参数默认值；数值 0 和布尔 false 都属于已传值。
      if (!values.length && param.required) {
        const { t } = useI18n()
        throw new Error(`${t('visualization.no_params_tips')} [${name}]`)
      }
      if (!values.length && param.enabledDefault && param.defaultValue) {
        try {
          const defaults = JSON.parse(param.defaultValue)
          if (!Array.isArray(defaults)) throw invalidParam(name)
          values = normalizeValues(defaults, name)
          operator = 'in'
        } catch {
          throw invalidParam(name)
        }
      }
      if (!values.length) return
      const clear = values.length === 1 && values[0] === 'DE_EMPTY'
      param.targetList.forEach(target => {
        if (target.targetType === 'query') {
          const entry = runtime.queryValues[target.targetRefId] || {}
          const mode = target.matchMode === 'filter' ? 'filter' : 'self'
          // 与仪表板一致，仅赋值绑定将 DE_EMPTY 作为清空标记；候选过滤仍按传入值筛选。
          entry[mode] = mode === 'self' && clear ? [] : values
          runtime.queryValues[target.targetRefId] = entry
          return
        }
        if (clear || !target.targetFieldId) return
        const compound = /^(eq|like)-(and|or)-(eq|like)$/.test(operator)
        if (!FIELD_OPERATORS.has(operator) && !compound) throw invalidParam(name)
        const count = operator === 'between' || compound ? 2 : 1
        if (!['in', 'not in'].includes(operator) && values.length !== count)
          throw invalidParam(name)
        // 单值等值用 IN 保持相同语义，并兼容后端布尔字段的字面量处理。
        let fieldOperator = operator
        if (operator === 'eq') fieldOperator = 'in'
        if (operator === 'not_eq') fieldOperator = 'not in'
        const filters = runtime.fieldFilters[target.targetRefId] || []
        filters.push({
          fieldId: target.targetFieldId,
          operator: fieldOperator,
          value: values,
          parameters: [],
          isTree: false
        })
        runtime.fieldFilters[target.targetRefId] = filters
      })
    })
  return runtime
}

const toTreeValue = (condition: SpreadsheetFilterCondition, values: string[]) => {
  const paths: SpreadsheetFilterTreePath[] = values.map(value => {
    const levels = value.includes('-de-') ? value.split('-de-') : value.split(',')
    if (!condition.treeFields.length || levels.length > condition.treeFields.length) {
      throw invalidParam(condition.name)
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
    if (clauses.length > 1 && values.length !== clauses.length) throw invalidParam(condition.name)
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
      throw invalidParam(condition.name)
    }
    return values
  }
  if (condition.displayType === 'time' || condition.displayType === 'timeRange') {
    const range = condition.displayType === 'timeRange'
    if (range && values.length !== 2) throw invalidParam(condition.name)
    const format = getTimeValueFormat(
      range ? condition.timeRangeGranularity : condition.timeGranularity
    )
    const dates = values.slice(0, range ? 2 : 1).map(value => {
      const date = dayjs(/^\d{13}$/.test(value) ? Number(value) : value)
      if (!date.isValid()) throw invalidParam(condition.name)
      return date.format(format)
    })
    if (range && dayjs(dates[0]).isAfter(dayjs(dates[1]))) throw invalidParam(condition.name)
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

export const applySpreadsheetOuterParamsToFilterConfig = async (
  config: SpreadsheetFilterConfig,
  runtime: SpreadsheetOuterParamsRuntime
): Promise<SpreadsheetFilterConfig> => {
  const conditions = await Promise.all(
    config.conditions.map(async original => {
      const params = runtime.queryValues[original.id]
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
