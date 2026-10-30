import dayjs from 'dayjs'
import type { ManipulateType } from 'dayjs'
import { getCustomTime, getDynamicRange } from './time-format'
import { getCustomRange } from './time-format-dayjs'

export interface ClockOffset {
  mode: 'fixed' | 'relative'
  hours: number | string | null
  minutes: number | string | null
  seconds: number | string | null
  around: 'f' | 'b'
}

export interface DynamicDefaultConfig {
  displayType?: string
  timeType?: string
  defaultValueCheck?: boolean
  timeGranularity: string
  timeGranularityMultiple?: string
  relativeToCurrent: string
  relativeToCurrentRange?: string
  timeNum: number
  relativeToCurrentType: ManipulateType | 'date'
  around: string
  arbitraryTime?: Date
  timeNumRange?: number
  relativeToCurrentTypeRange?: ManipulateType | 'date'
  aroundRange?: string
  arbitraryTimeRange?: Date
  clockOffset?: ClockOffset
  clockOffsetRange?: ClockOffset
}

export const createClockOffset = (): ClockOffset => ({
  mode: 'relative',
  hours: 0,
  minutes: 0,
  seconds: 0,
  around: 'f'
})

export const isCustomClockDefault = (config: DynamicDefaultConfig) =>
  config.displayType === '7'
    ? config.timeGranularityMultiple === 'datetimerange' &&
      config.relativeToCurrentRange === 'custom'
    : config.timeGranularity === 'datetime' && config.relativeToCurrent === 'custom'

export class DynamicTimeError extends Error {
  constructor(public key: string) {
    super(key)
  }
}

const invalid = () => {
  throw new DynamicTimeError('dynamic_time.invalid_offset')
}
const integer = (value: unknown, max = Number.MAX_SAFE_INTEGER) => {
  // Do not coerce empty input to zero, or truncate decimals and out-of-range units.
  if ((typeof value !== 'number' && typeof value !== 'string') || String(value).trim() === '')
    invalid()
  const number = Number(value)
  if (!Number.isSafeInteger(number) || number < 0 || number > max) invalid()
  return number
}

const customClock = (config: DynamicDefaultConfig, baseline: Date, end: boolean) => {
  const amount = integer(end ? config.timeNumRange : config.timeNum)
  const unit = end ? config.relativeToCurrentTypeRange : config.relativeToCurrentType
  const direction = end ? config.aroundRange : config.around
  if (!['year', 'month', 'date', 'day'].includes(unit) || !['f', 'b'].includes(direction)) invalid()
  const date = dayjs(baseline)
    .millisecond(0)
    [direction === 'f' ? 'subtract' : 'add'](
      amount,
      unit === 'date' ? 'day' : (unit as ManipulateType)
    )
  const offset = end ? config.clockOffsetRange : config.clockOffset
  let result: Date
  if (offset?.mode === 'relative') {
    const seconds =
      integer(offset.hours) * 3600 + integer(offset.minutes, 59) * 60 + integer(offset.seconds, 59)
    if (!Number.isSafeInteger(seconds) || !['f', 'b'].includes(offset.around)) invalid()
    result = new Date(date.valueOf() + (offset.around === 'f' ? -1 : 1) * seconds * 1000)
  } else {
    if (offset && offset.mode !== 'fixed') invalid()
    const clock = dayjs(end ? config.arbitraryTimeRange : config.arbitraryTime)
    if (!(end ? config.arbitraryTimeRange : config.arbitraryTime) || !clock.isValid()) invalid()
    result = date.hour(clock.hour()).minute(clock.minute()).second(clock.second()).toDate()
  }
  if (!date.isValid() || !Number.isFinite(result.getTime())) invalid()
  return result
}

// Preview, first load, reset and report rendering use the same rules. The caller
// supplies one baseline for both endpoints; saved previews are never the source.
export const evaluateDynamicDefault = (
  config: DynamicDefaultConfig,
  baseline = new Date(),
  scene: 'config' | 'panel' = 'panel'
): Date | [Date, Date] => {
  const range = config.displayType === '7'
  if (isCustomClockDefault(config)) {
    const start = customClock(config, baseline, false)
    if (!range) return start
    const end = customClock(config, baseline, true)
    if (+start > +end) throw new DynamicTimeError('v_query.the_start_time')
    return [start, end]
  }
  if (!range)
    return new Date(getDynamicRange({ ...config, arbitraryTime: config.arbitraryTime })[0])
  if (config.relativeToCurrentRange && config.relativeToCurrentRange !== 'custom') {
    return getCustomRange(config.relativeToCurrentRange)
  }
  return [
    getCustomTime(
      config.timeNum,
      config.relativeToCurrentType,
      config.timeGranularity,
      config.around,
      config.arbitraryTime,
      config.timeGranularityMultiple,
      `start-${scene}`,
      baseline
    ),
    getCustomTime(
      config.timeNumRange,
      config.relativeToCurrentTypeRange,
      config.timeGranularity,
      config.aroundRange,
      config.arbitraryTimeRange,
      config.timeGranularityMultiple,
      `end-${scene}`,
      baseline
    )
  ]
}

export const validateDynamicDefault = (config: DynamicDefaultConfig, baseline = new Date()) => {
  if (!config.defaultValueCheck || config.timeType !== 'dynamic' || !isCustomClockDefault(config))
    return ''
  try {
    evaluateDynamicDefault(config, baseline, 'config')
    return ''
  } catch (error) {
    if (error instanceof DynamicTimeError) return error.key
    throw error
  }
}

// Old configurations allowed clearing these controls. Preserve their former
// runtime meaning, while keeping editor/save validation strict for all rules.
export const evaluateRuntimeDynamicDefault = (
  config: DynamicDefaultConfig,
  baseline = new Date()
) => {
  if (!isCustomClockDefault(config)) return evaluateDynamicDefault(config, baseline)
  const legacyStart = !config.clockOffset
  const legacyEnd = !config.clockOffsetRange
  return evaluateDynamicDefault(
    {
      ...config,
      timeNum: legacyStart && config.timeNum == null ? 0 : config.timeNum,
      arbitraryTime: legacyStart && !config.arbitraryTime ? baseline : config.arbitraryTime,
      timeNumRange: legacyEnd && config.timeNumRange == null ? 0 : config.timeNumRange,
      arbitraryTimeRange:
        legacyEnd && !config.arbitraryTimeRange ? baseline : config.arbitraryTimeRange
    },
    baseline
  )
}
