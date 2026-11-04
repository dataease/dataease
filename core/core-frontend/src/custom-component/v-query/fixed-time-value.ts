import dayjs from 'dayjs'

// 旧版 toLocaleString() 没有保存区域标记，兼容中文上午/下午及英文历史格式。
const normalizeLegacyTime = (value: unknown): unknown => {
  if (typeof value !== 'string') return value
  const chinese = value.match(
    /^(\d{4})\/(\d{1,2})\/(\d{1,2})\s+(上午|下午)(\d{1,2}):(\d{2}):(\d{2})$/
  )
  const english = value.match(
    /^(\d{1,2})\/(\d{1,2})\/(\d{4}),?\s+(\d{1,2}):(\d{2}):(\d{2})(?:\s+(AM|PM))?$/i
  )
  const match = chinese || english
  if (!match) return value
  const period = chinese ? match[4] : match[7]?.toUpperCase()
  const year = Number(chinese ? match[1] : match[3])
  let monthFirst = true
  if (english) {
    const first = Number(match[1])
    const second = Number(match[2])
    if (first > 12) {
      monthFirst = false
    } else if (second <= 12 && first !== second) {
      // AM/PM 只表示时制，不能证明日月顺序；没有保存区域信息时保留原文。
      return value
    }
  }
  const month = Number(chinese ? match[2] : monthFirst ? match[1] : match[2])
  const day = Number(chinese ? match[3] : monthFirst ? match[2] : match[1])
  let hour = Number(chinese ? match[5] : match[4])
  const minute = Number(chinese ? match[6] : match[5])
  const second = Number(chinese ? match[7] : match[6])
  if (period) {
    if (hour < 1 || hour > 12) return value
    hour = (hour % 12) + (['下午', 'PM'].includes(period) ? 12 : 0)
  }
  const date = new Date(year, month - 1, day, hour, minute, second)
  if (
    date.getFullYear() !== year ||
    date.getMonth() !== month - 1 ||
    date.getDate() !== day ||
    date.getHours() !== hour ||
    date.getMinutes() !== minute ||
    date.getSeconds() !== second
  ) {
    return value
  }
  return dayjs(date).format('YYYY/MM/DD HH:mm:ss')
}

const normalizeValue = (value: unknown): unknown => {
  if (!Array.isArray(value)) return normalizeLegacyTime(value)
  const normalized = value.map(normalizeLegacyTime)
  return normalized.some((item, index) => item !== value[index]) ? normalized : value
}

export const normalizeFixedTimeConfig = (config: {
  displayType?: string
  timeType?: string
  defaultValue?: unknown
  selectValue?: unknown
}) => {
  if (!['1', '7'].includes(config.displayType || '') || config.timeType === 'dynamic') return
  config.defaultValue = normalizeValue(config.defaultValue)
  config.selectValue = normalizeValue(config.selectValue)
}

export class FixedTimeError extends Error {
  readonly key = 'v_query.fixed_time_invalid'
}

// 不把无法确定的历史值交给 Date/dayjs 再猜一次；原始配置保留供用户重新选择。
export const hasUnresolvedFixedTimeValue = (value: unknown): boolean => {
  const normalized = normalizeValue(value)
  if (Array.isArray(normalized)) return normalized.some(hasUnresolvedFixedTimeValue)
  return (
    typeof normalized === 'string' &&
    (/^\d{1,2}[/.\-]\d{1,2}[/.\-]\d{4}(?:[,\s]|$)/.test(normalized) ||
      /^(?:\d{4})\/\d{1,2}\/\d{1,2}\s+(?:上午|下午)/.test(normalized))
  )
}

export const readFixedTimeValue = (value: unknown): unknown => {
  const normalized = normalizeValue(value)
  if (hasUnresolvedFixedTimeValue(normalized)) throw new FixedTimeError()
  return normalized
}
