import dayjs from 'dayjs'
import { toRaw } from 'vue'

const present = (value: unknown) => value !== null && value !== undefined && value !== ''
const hasValue = (value: any) => (Array.isArray(value) ? value.some(present) : present(value))
const copyValue = (value: any): any =>
  Array.isArray(value)
    ? value.map(copyValue)
    : value instanceof Date
    ? new Date(value.getTime())
    : value

// Capture the same resolved values used by each query, including later cascade refreshes.
// Display labels stay separate from mapped query IDs. Never resolve relative dates here.
const resolvedDefaults = new WeakMap<object, Record<string, any>>()
export const rememberQueryDefault = (item: any, value: unknown, firstLoad = true) => {
  const type = String(item.displayType)
  const text = firstLoad ? 'defaultConditionValue' : 'conditionValue'
  const displayedValue = ['1', '7'].includes(type)
    ? value
    : firstLoad
    ? item.defaultValue
    : item.selectValue
  resolvedDefaults.set(toRaw(item), {
    name: item.name,
    displayType: type,
    defaultValueCheck: item.defaultValueCheck || hasValue(value),
    timeType: item.timeType,
    timeGranularity: item.timeGranularity,
    timeGranularityMultiple: item.timeGranularityMultiple,
    conditionType: item.conditionType,
    defaultConditionValueF: item[`${text}F`],
    defaultConditionValueS: item[`${text}S`],
    defaultConditionValueOperatorF: item[`${text}OperatorF`],
    defaultConditionValueOperatorS: item[`${text}OperatorS`],
    defaultNumValueStart: firstLoad ? item.defaultNumValueStart : item.numValueStart,
    defaultNumValueEnd: firstLoad ? item.defaultNumValueEnd : item.numValueEnd,
    defaultValue: copyValue(hasValue(value) ? displayedValue : []),
    // A mapped query value without its display label must not leak an internal ID.
    unresolvedLabel: hasValue(value) && !hasValue(displayedValue) && !['8', '22'].includes(type)
  })
}

const dateFormats = {
  year: 'YYYY',
  month: 'YYYY-MM',
  date: 'YYYY-MM-DD',
  datetime: 'YYYY-MM-DD HH:mm:ss',
  yearrange: 'YYYY',
  monthrange: 'YYYY-MM',
  daterange: 'YYYY-MM-DD',
  datetimerange: 'YYYY-MM-DD HH:mm:ss'
}

export const queryDefaultSnapshot = (components: any[], t: (key: string) => string): string => {
  const lines: string[] = []
  const visit = (list: any[]) =>
    list.forEach(component => {
      const type = component.innerType || component.component
      if (type === 'Group') visit(component.propValue || [])
      if (type === 'DeTabs')
        (component.propValue || []).forEach(tab => visit(tab.componentData || []))
      if (type !== 'VQuery') return
      ;(component.propValue || []).forEach(source => {
        const resolved = resolvedDefaults.get(toRaw(source))
        const item = resolved || source
        const missing = () => lines.push(`${item.name}：${t('report.filter.snapshot_unrecorded')}`)
        const displayType = String(item.displayType)
        let text = ''
        if (!resolved && item.defaultValueFirstItem && item.defaultValueCheck) {
          missing()
          return
        }
        if (item.unresolvedLabel) {
          missing()
          return
        }
        if (displayType === '8') {
          const parts = [
            [item.defaultConditionValueF, item.defaultConditionValueOperatorF || 'eq'],
            ...([1, 2].includes(item.conditionType)
              ? [[item.defaultConditionValueS, item.defaultConditionValueOperatorS || 'like']]
              : [])
          ]
            .filter(([value]) => present(value))
            .map(
              ([value, operator]) =>
                `${t(operator === 'eq' ? 'v_query.exact_match' : 'v_query.fuzzy_match')}：${value}`
            )
          text = parts.join(` ${t(item.conditionType === 2 ? 'chart.or' : 'chart.and')} `)
        } else if (item.defaultValueCheck) {
          if (displayType === '22') {
            const start = item.defaultNumValueStart
            const end = item.defaultNumValueEnd
            text =
              present(start) && present(end)
                ? `${start} ~ ${end}`
                : present(start)
                ? `≥ ${start}`
                : present(end)
                ? `≤ ${end}`
                : ''
          } else if (['1', '7'].includes(displayType)) {
            const value = item.defaultValue
            if (item.timeType === 'dynamic' && !resolved) {
              missing()
              return
            }
            const values = (Array.isArray(value) ? value : [value]).filter(present)
            const format =
              dateFormats[displayType === '1' ? item.timeGranularity : item.timeGranularityMultiple]
            if (values.some(value => !dayjs(value).isValid()) || (values.length && !format)) {
              missing()
              return
            }
            text = (displayType === '1' ? values.slice(0, 1) : values)
              .map(value => dayjs(value).format(format))
              .join(' ~ ')
          } else if (['0', '2', '5', '9'].includes(displayType)) {
            const values = (
              Array.isArray(item.defaultValue) ? item.defaultValue : [item.defaultValue]
            ).filter(present)
            if (values.some(value => typeof value === 'object')) {
              missing()
              return
            }
            text = values
              .map(value =>
                value === '_empty_$'
                  ? t('v_query.empty_data')
                  : displayType === '9'
                  ? String(value).split('-de-').join(' / ')
                  : String(value)
              )
              .join('、')
          } else {
            missing()
            return
          }
        }
        if (!text && item.defaultValueCheck) text = t('report.filter.snapshot_not_applied')
        if (text) lines.push(`${item.name}：${text}`)
      })
    })
  visit(components)
  return lines.join('；')
}
