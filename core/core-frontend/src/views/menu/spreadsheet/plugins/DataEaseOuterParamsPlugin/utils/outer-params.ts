import { useI18n } from '@/hooks/web/useI18n'
import type {
  SpreadsheetOuterParamsConfig,
  SpreadsheetOuterParamsRuntime
} from '../../../types/outer-params'

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
