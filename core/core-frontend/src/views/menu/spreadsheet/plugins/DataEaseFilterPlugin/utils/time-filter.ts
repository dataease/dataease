import dayjs, { type Dayjs } from 'dayjs'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterRelativeTime,
  SpreadsheetFilterTimeFilterRange,
  SpreadsheetFilterTimeGranularity,
  SpreadsheetFilterTimeRangeGranularity
} from '../../../types/plugin'

type TimeGranularity = SpreadsheetFilterTimeGranularity | SpreadsheetFilterTimeRangeGranularity

const UNIT_MAP = {
  year: 'year',
  month: 'month',
  day: 'day',
  hour: 'hour',
  minute: 'minute',
  second: 'second'
} as const

export const getTimePickerType = (granularity: TimeGranularity) => granularity

export const getTimeValueFormat = (granularity: TimeGranularity) => {
  if (granularity === 'year' || granularity === 'yearrange') return 'YYYY'
  if (granularity === 'month' || granularity === 'monthrange') return 'YYYY-MM'
  if (granularity === 'datetime' || granularity === 'datetimerange') {
    return 'YYYY-MM-DD HH:mm:ss'
  }
  return 'YYYY-MM-DD'
}

export const getSingleRelativeOptions = (granularity: string) => {
  const base = (granularity || '').replace('range', '')
  if (base === 'year') {
    return [
      { label: '今年', value: 'thisYear' },
      { label: '去年', value: 'lastYear' },
      { label: '自定义', value: 'custom' }
    ]
  }
  if (base === 'month') {
    return [
      { label: '本月', value: 'thisMonth' },
      { label: '上月', value: 'lastMonth' },
      { label: '自定义', value: 'custom' }
    ]
  }
  return [
    { label: '今天', value: 'today' },
    { label: '昨天', value: 'yesterday' },
    { label: '月初', value: 'monthBeginning' },
    { label: '月底', value: 'monthEnd' },
    { label: '年初', value: 'yearBeginning' },
    { label: '自定义', value: 'custom' }
  ]
}

export const getRangeRelativeOptions = (granularity: string) => {
  const base = (granularity || '').replace('range', '')
  if (base === 'year') {
    return [
      { label: '今年', value: 'thisYear' },
      { label: '去年', value: 'lastYear' },
      { label: '自定义', value: 'custom' }
    ]
  }
  if (base === 'month') {
    return [
      { label: '本月', value: 'thisMonth' },
      { label: '上月', value: 'lastMonth' },
      { label: '本季度', value: 'thisQuarter' },
      { label: '最近3个月', value: 'LastThreeMonths' },
      { label: '最近6个月', value: 'LastSixMonths' },
      { label: '最近12个月', value: 'LastTwelveMonths' },
      { label: '年初至本月', value: 'YearToThisMonth' },
      { label: '年初至上月末', value: 'YearToLastMonthEnd' },
      { label: '自定义', value: 'custom' }
    ]
  }
  return [
    { label: '今天', value: 'today' },
    { label: '昨天', value: 'yesterday' },
    { label: '本周', value: 'thisWeek' },
    { label: '本月', value: 'thisMonth' },
    { label: '最近3天', value: 'LastThreeDays' },
    { label: '月初至今', value: 'monthBeginning' },
    { label: '年初至今', value: 'yearBeginning' },
    { label: '年初至上月末', value: 'YearToLastMonthEnd' },
    { label: '月初至昨天', value: 'monthToYesterday' },
    { label: '完整上月', value: 'LastMonthFull' },
    { label: '自定义', value: 'custom' }
  ]
}

export const getAllowedUnits = (granularity: string) => {
  const base = (granularity || '').replace('range', '')
  if (base === 'year') return ['year']
  if (base === 'month') return ['year', 'month']
  return ['year', 'month', 'day']
}

export const resetTimeFilterRangeOnGranularityChange = (
  range: SpreadsheetFilterTimeFilterRange | undefined,
  granularity: string
) => {
  if (!range) return
  const singleOptions = getSingleRelativeOptions(granularity)
  const rangeOptions = getRangeRelativeOptions(granularity)
  const allowedUnits = getAllowedUnits(granularity)
  const fallbackUnit = allowedUnits[allowedUnits.length - 1] || 'year'

  // 重置“开始于”动态时间
  if (range.start?.dynamic) {
    if (!singleOptions.some(opt => opt.value === range.start!.dynamic!.relativeToCurrent)) {
      range.start.dynamic.relativeToCurrent = singleOptions[0]?.value || 'custom'
    }
    if (!allowedUnits.includes(range.start.dynamic.unit)) {
      range.start.dynamic.unit = fallbackUnit as any
    }
  }

  // 重置“结束于”动态时间
  if (range.end?.dynamic) {
    if (!singleOptions.some(opt => opt.value === range.end!.dynamic!.relativeToCurrent)) {
      range.end.dynamic.relativeToCurrent = singleOptions[0]?.value || 'custom'
    }
    if (!allowedUnits.includes(range.end.dynamic.unit)) {
      range.end.dynamic.unit = fallbackUnit as any
    }
  }

  // 重置“时间区间”动态时间
  if (range.relativeToCurrentRange) {
    if (!rangeOptions.some(opt => opt.value === range.relativeToCurrentRange)) {
      range.relativeToCurrentRange = rangeOptions[0]?.value || 'custom'
    }
  }
}

export const resetTimeConditionOnGranularityChange = (
  condition: SpreadsheetFilterCondition
) => {
  if (!condition) return
  const isRange = condition.displayType === 'timeRange'
  const granularity = isRange ? condition.timeRangeGranularity : condition.timeGranularity
  const singleOptions = getSingleRelativeOptions(granularity)
  const rangeOptions = getRangeRelativeOptions(granularity)
  const allowedUnits = getAllowedUnits(granularity)
  const fallbackUnit = allowedUnits[allowedUnits.length - 1] || 'year'

  // 1. 重置筛选范围（开始于、结束于、时间区间）
  if (condition.timeFilterRange) {
    resetTimeFilterRangeOnGranularityChange(condition.timeFilterRange, granularity)
  }

  // 2. 重置默认值动态时间
  if (isRange) {
    if (condition.timeRangeDynamicDefault?.start) {
      if (!rangeOptions.some(opt => opt.value === condition.timeRangeDynamicDefault.start.relativeToCurrent)) {
        condition.timeRangeDynamicDefault.start.relativeToCurrent = rangeOptions[0]?.value || 'custom'
      }
      if (!allowedUnits.includes(condition.timeRangeDynamicDefault.start.unit)) {
        condition.timeRangeDynamicDefault.start.unit = fallbackUnit as any
      }
    }
    if (condition.timeRangeDynamicDefault?.end) {
      if (!rangeOptions.some(opt => opt.value === condition.timeRangeDynamicDefault.end.relativeToCurrent)) {
        condition.timeRangeDynamicDefault.end.relativeToCurrent = rangeOptions[0]?.value || 'custom'
      }
      if (!allowedUnits.includes(condition.timeRangeDynamicDefault.end.unit)) {
        condition.timeRangeDynamicDefault.end.unit = fallbackUnit as any
      }
    }
  } else {
    if (condition.timeDynamicDefault?.offset) {
      if (!singleOptions.some(opt => opt.value === condition.timeDynamicDefault.offset.relativeToCurrent)) {
        condition.timeDynamicDefault.offset.relativeToCurrent = singleOptions[0]?.value || 'custom'
      }
      if (!allowedUnits.includes(condition.timeDynamicDefault.offset.unit)) {
        condition.timeDynamicDefault.offset.unit = fallbackUnit as any
      }
    }
  }
}

export const preventInvalidNumberKeys = (e: KeyboardEvent) => {
  if (['e', 'E', '+', '-', '.'].includes(e.key)) {
    e.preventDefault()
  }
}

export const onPasteNumber = (e: ClipboardEvent) => {
  const pasteData = e.clipboardData?.getData('text') || ''
  if (!/^\d+$/.test(pasteData)) {
    e.preventDefault()
  }
}

export const resolveRelativeTime = (
  relative: SpreadsheetFilterRelativeTime,
  base: Dayjs = dayjs()
) => {
  const preset = relative?.relativeToCurrent
  let result = base
  if (preset === 'thisYear') result = base.startOf('year')
  else if (preset === 'lastYear') result = base.subtract(1, 'year').startOf('year')
  else if (preset === 'thisMonth') result = base.startOf('month')
  else if (preset === 'lastMonth') result = base.subtract(1, 'month').startOf('month')
  else if (preset === 'today') result = base.startOf('day')
  else if (preset === 'yesterday') result = base.subtract(1, 'day').startOf('day')
  else if (preset === 'monthBeginning') result = base.startOf('month')
  else if (preset === 'monthEnd') result = base.endOf('month')
  else if (preset === 'yearBeginning') result = base.startOf('year')
  else {
    const amount = Math.abs(Number(relative?.value || 0))
    const signedAmount = relative?.direction
      ? (relative.direction === 'before' ? -amount : amount)
      : Number(relative?.value || 0)
    result = base.add(signedAmount, UNIT_MAP[relative?.unit || 'day'])
  }
  if ((!preset || preset === 'custom') && relative?.time) {
    const [hour, minute, second] = relative.time.split(':').map(Number)
    result = result.hour(hour || 0).minute(minute || 0).second(second || 0)
  }
  return result
}

const resolveBoundary = (
  boundary: SpreadsheetFilterTimeFilterRange['start'],
  base: Dayjs
) => {
  if (!boundary) return undefined
  if (boundary.type === 'dynamic' && boundary.dynamic) {
    return resolveRelativeTime(boundary.dynamic, base)
  }
  return boundary.value ? dayjs(boundary.value) : undefined
}

const resolvePresetRange = (preset: string, now: Dayjs): [Dayjs, Dayjs] => {
  if (preset === 'thisYear') return [now.startOf('year'), now.endOf('year')]
  if (preset === 'lastYear') {
    const lastYear = now.subtract(1, 'year')
    return [lastYear.startOf('year'), lastYear.endOf('year')]
  }
  if (preset === 'thisMonth') return [now.startOf('month'), now.endOf('month')]
  if (preset === 'lastMonth') {
    const lastMonth = now.subtract(1, 'month')
    return [lastMonth.startOf('month'), lastMonth.endOf('month')]
  }
  if (preset === 'thisQuarter') {
    const start = now.month(Math.floor(now.month() / 3) * 3).startOf('month')
    return [start, start.add(2, 'month').endOf('month')]
  }
  if (preset === 'LastThreeMonths') return [now.subtract(2, 'month').startOf('month'), now.endOf('month')]
  if (preset === 'LastSixMonths') return [now.subtract(5, 'month').startOf('month'), now.endOf('month')]
  if (preset === 'LastTwelveMonths') return [now.subtract(11, 'month').startOf('month'), now.endOf('month')]
  if (preset === 'YearToThisMonth') return [now.startOf('year'), now.endOf('month')]
  if (preset === 'YearToLastMonthEnd') return [now.startOf('year'), now.subtract(1, 'month').endOf('month')]
  if (preset === 'thisWeek') return [now.startOf('week'), now.endOf('week')]
  if (preset === 'LastThreeDays') return [now.subtract(2, 'day').startOf('day'), now.endOf('day')]
  if (preset === 'monthBeginning') return [now.startOf('month'), now.endOf('day')]
  if (preset === 'yearBeginning') return [now.startOf('year'), now.endOf('day')]
  if (preset === 'monthToYesterday') return [now.startOf('month'), now.subtract(1, 'day').endOf('day')]
  const day = preset === 'yesterday' ? now.subtract(1, 'day') : now
  return [day.startOf('day'), day.endOf('day')]
}

export const resolveTimeFilterBounds = (
  range: SpreadsheetFilterTimeFilterRange,
  base: Dayjs = dayjs()
) => {
  const type = range?.intervalType || 'none'
  if (type === 'timeInterval' && range.relativeToCurrentRange && range.relativeToCurrentRange !== 'custom') {
    const [start, end] = resolvePresetRange(range.relativeToCurrentRange, base)
    return { start, end }
  }
  return {
    start: ['start', 'timeInterval'].includes(type) ? resolveBoundary(range.start, base) : undefined,
    end: ['end', 'timeInterval'].includes(type) ? resolveBoundary(range.end, base) : undefined
  }
}

export const isTimeDisabled = (
  value: Date,
  condition: SpreadsheetFilterCondition,
  selectedRange?: [string, string]
) => {
  if (!condition.timeFilterRangeEnabled) return false
  const current = dayjs(value)
  const { start, end } = resolveTimeFilterBounds(condition.timeFilterRange)
  if (start && current.isBefore(start, 'day')) return true
  if (end && current.isAfter(end, 'day')) return true

  const maximum = condition.timeFilterRange.maximumSingleQuery
  if (!maximum || !selectedRange?.[0]) return false
  const anchor = dayjs(selectedRange[0])
  const upper = resolveRelativeTime(maximum, anchor)
  const lower = anchor.subtract(Number(maximum.value || 0), UNIT_MAP[maximum.unit || 'day'])
  return current.isBefore(lower, 'day') || current.isAfter(upper, 'day')
}

export const resolveDynamicTimeDefault = (condition: SpreadsheetFilterCondition) => {
  if (condition.displayType === 'timeRange') {
    const preset = condition.timeRangeDynamicDefault.start.relativeToCurrent
    if (preset && preset !== 'custom') {
      const [start, end] = preset === 'LastMonthFull'
        ? [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')]
        : resolvePresetRange(preset, dayjs())
      return [
        start.format(getTimeValueFormat(condition.timeRangeGranularity)),
        end.format(getTimeValueFormat(condition.timeRangeGranularity))
      ]
    }
    return [
      resolveRelativeTime(condition.timeRangeDynamicDefault.start).format(
        getTimeValueFormat(condition.timeRangeGranularity)
      ),
      resolveRelativeTime(condition.timeRangeDynamicDefault.end).format(
        getTimeValueFormat(condition.timeRangeGranularity)
      )
    ]
  }
  let value = resolveRelativeTime(condition.timeDynamicDefault.offset)
  if (
    condition.timeGranularity === 'datetime' &&
    condition.timeDynamicDefault.offset.relativeToCurrent === 'custom' &&
    condition.timeDynamicDefault.time
  ) {
    const [hour, minute, second] = condition.timeDynamicDefault.time.split(':').map(Number)
    value = value.hour(hour || 0).minute(minute || 0).second(second || 0)
  }
  return value.format(getTimeValueFormat(condition.timeGranularity))
}

export const normalizeTimeQueryValue = (
  condition: SpreadsheetFilterCondition,
  value: unknown
): unknown => {
  if (condition.displayType === 'timeRange') {
    if (!Array.isArray(value) || value.length < 2) return value
    const granularity = condition.timeRangeGranularity
    if (granularity === 'datetimerange') {
      return value.slice(0, 2).map(item => dayjs(String(item)).millisecond(0).valueOf())
    }
    const startUnit = granularity === 'yearrange' ? 'year' : granularity === 'monthrange' ? 'month' : 'day'
    const endUnit = startUnit
    return [
      dayjs(String(value[0])).startOf(startUnit).valueOf(),
      dayjs(String(value[1])).endOf(endUnit).millisecond(0).valueOf()
    ]
  }
  if (condition.displayType !== 'time' || value === undefined || value === null || value === '') {
    return value
  }
  const granularity = condition.timeGranularity
  const unit = granularity === 'year' ? 'year' : granularity === 'month' ? 'month' : granularity === 'date' ? 'day' : undefined
  if (!unit) {
    const instant = dayjs(String(value)).millisecond(0).valueOf()
    return [instant, instant]
  }
  return [
    dayjs(String(value)).startOf(unit).valueOf(),
    dayjs(String(value)).endOf(unit).millisecond(0).valueOf()
  ]
}
