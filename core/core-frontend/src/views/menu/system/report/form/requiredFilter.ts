import dayjs from 'dayjs'
import {
  evaluateRuntimeDynamicDefault,
  type DynamicDefaultConfig
} from '@/custom-component/v-query/dynamic-default-time'

interface ReportFilter extends DynamicDefaultConfig {
  id: string | number
  name: string
  required?: boolean
  defaultValue?: unknown
  defaultNumValueStart?: unknown
  defaultNumValueEnd?: unknown
  defaultConditionValueF?: unknown
  defaultConditionValueS?: unknown
  conditionType?: number
  optionValueSource?: number
  defaultValueFirstItem?: boolean
}

const present = (value: unknown): boolean => {
  if (Array.isArray(value)) return value.length > 0 && value.every(present)
  return value !== null && value !== undefined && value !== ''
}

export const hasRequiredFilterValue = (filter: ReportFilter): boolean => {
  if (filter.displayType === '8') {
    return (
      present(filter.defaultConditionValueF) &&
      (!filter.conditionType || present(filter.defaultConditionValueS))
    )
  }
  if (!filter.defaultValueCheck) return false
  if (filter.displayType === '22') {
    return present(filter.defaultNumValueStart) && present(filter.defaultNumValueEnd)
  }
  if (['1', '7'].includes(filter.displayType)) {
    if (filter.timeType === 'dynamic') {
      try {
        const value = evaluateRuntimeDynamicDefault(filter)
        return (Array.isArray(value) ? value : [value]).every(date => dayjs(date).isValid())
      } catch {
        return false
      }
    }
    const value = filter.defaultValue
    const validDate = (date: unknown) =>
      present(date) && dayjs(date as string | number | Date).isValid()
    return filter.displayType === '7'
      ? Array.isArray(value) && value.length === 2 && value.every(validDate)
      : !Array.isArray(value) && validDate(value)
  }
  return (
    present(filter.defaultValue) ||
    (filter.displayType === '0' && filter.optionValueSource === 1 && !!filter.defaultValueFirstItem)
  )
}

// Required flags come from the resource, values from the confirmed report override.
export const missingRequiredFilters = (source: ReportFilter[], effective: ReportFilter[]) => {
  const values = new Map(effective.map(filter => [String(filter.id), filter]))
  return source
    .filter(
      filter => filter.required && !hasRequiredFilterValue(values.get(String(filter.id)) || filter)
    )
    .map(filter => filter.name)
}
