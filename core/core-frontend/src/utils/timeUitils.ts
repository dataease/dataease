// 时分秒和月日无法确定绝对日期区间，保留原值，由后端按展示粒度匹配。
const NO_DATE_GRANULARITY = ['H_m_s', 'M_d']

export const getRange = (outerTimeValue, timeGranularity) => {
  // y_Q（年季度，如 2026-3）与 y_W（年周，如 2026-44）不属于 Date 可解析的格式，
  // 必须在进入 Invalid Date 判断前先换算，否则会被当成字符串原样返回，
  // 下游据此判定「不是时间区间」而降级为等值匹配，导致联动不到数据。
  if (timeGranularity === 'y_Q') {
    return getQuarterEnd(outerTimeValue)
  }
  if (timeGranularity === 'y_W') {
    return getWeekEnd(outerTimeValue)
  }
  if (NO_DATE_GRANULARITY.includes(timeGranularity)) {
    return outerTimeValue
  }
  const localRange = getLocalDisplayRange(outerTimeValue, timeGranularity)
  if (localRange !== undefined) {
    return localRange
  }
  const selectValue = timeGranularity === 'y_M_d_H' ? outerTimeValue + ':' : outerTimeValue
  if (new Date(selectValue).toString() === 'Invalid Date') {
    return selectValue
  }
  switch (timeGranularity) {
    case 'year':
    case 'y':
      return getYearEnd(selectValue)
    case 'month':
    case 'y_M':
      return getMonthEnd(selectValue)
    case 'date':
    case 'y_M_d':
      return getDayEnd(selectValue)
    case 'hour':
    case 'y_M_d_H':
      return getHourEnd(selectValue)
    case 'minute':
    case 'y_M_d_H_m':
      return getMinuteEnd(selectValue)
    case 'y_M_d_H_m_s':
      return getSecondEnd(selectValue)
    case 'datetime':
      return [+new Date(selectValue), +new Date(selectValue)]
    default:
      return selectValue
  }
}

export const getTimeBegin = (selectValue, timeGranularity) => {
  switch (timeGranularity) {
    case 'year':
      return getYearEnd(selectValue)
    case 'month':
      return getMonthEnd(selectValue)
    case 'date':
      return getDayEnd(selectValue)
    default:
      return selectValue
  }
}

// 图表展示值没有时区，按本地日历解析，避免 Date 将 YYYY-MM-DD 当成 UTC。
// 其他输入（时间戳、Date、带时区 ISO 等）仍沿用原有时间点转换逻辑。
const getLocalDisplayRange = (value, granularity) => {
  if (typeof value !== 'string') {
    return undefined
  }
  const precision = {
    y: 1,
    y_M: 2,
    y_M_d: 3,
    y_M_d_H: 4,
    y_M_d_H_m: 5,
    y_M_d_H_m_s: 6,
    datetime: 6
  }[granularity]
  if (!precision) {
    return undefined
  }
  const matched =
    /^(\d{4})(?:([-/])(\d{1,2})(?:\2(\d{1,2})(?:[ T](\d{1,2})(?::(\d{1,2})(?::(\d{1,2}))?)?)?)?)?$/.exec(
      value
    )
  if (!matched) {
    return undefined
  }
  const parts = [matched[1], ...matched.slice(3)]
  if (parts.filter(part => part !== undefined).length !== precision) {
    return undefined
  }
  const [year, month = 1, day = 1, hour = 0, minute = 0, second = 0] = parts.map(part =>
    part === undefined ? undefined : Number(part)
  )
  const start = new Date(0)
  start.setFullYear(year, month - 1, day)
  start.setHours(hour, minute, second, 0)
  if (
    start.getFullYear() !== year ||
    start.getMonth() !== month - 1 ||
    start.getDate() !== day ||
    (precision >= 4 && start.getHours() !== hour) ||
    start.getMinutes() !== minute ||
    start.getSeconds() !== second
  ) {
    return value
  }
  if (granularity === 'datetime') {
    return [+start, +start]
  }
  const end = new Date(+start)
  switch (precision) {
    case 1:
      end.setFullYear(year + 1, 0, 1)
      end.setHours(0, 0, 0, 0)
      break
    case 2:
      end.setMonth(month, 1)
      end.setHours(0, 0, 0, 0)
      break
    case 3:
      end.setDate(day + 1)
      end.setHours(0, 0, 0, 0)
      break
    case 4:
      return [+start, +start + 60 * 60 * 1000 - 1000]
    case 5:
      return [+start, +start + 60 * 1000 - 1000]
    case 6:
      return [+start, +start + 999]
  }
  return [+start, +end - 1000]
}

const getYearEnd = timestamp => {
  const time = new Date(timestamp)
  return [
    +new Date(time.getFullYear(), 0, 1),
    +new Date(time.getFullYear(), 11, 31) + 60 * 1000 * 60 * 24 - 1000
  ]
}

const getMonthEnd = timestamp => {
  const time = new Date(timestamp)
  const date = new Date(time.getFullYear(), time.getMonth(), 1)
  date.setDate(1)
  date.setMonth(date.getMonth() + 1)
  return [+new Date(time.getFullYear(), time.getMonth(), 1), +new Date(date.getTime() - 1000)]
}

const getDayEnd = timestamp => {
  const utcTime = getUtcTime(timestamp)
  return [+utcTime, +utcTime + 60 * 1000 * 60 * 24 - 1000]
}

const getHourEnd = timestamp => {
  return [+new Date(timestamp), +new Date(timestamp) + 60 * 1000 * 60 - 1000]
}

const getMinuteEnd = timestamp => {
  return [+new Date(timestamp), +new Date(timestamp) + 60 * 1000 - 1000]
}

const getSecondEnd = timestamp => {
  return [+new Date(timestamp), +new Date(timestamp) + 999]
}

const getYearBegin = timestamp => {
  const time = new Date(timestamp)
  return +new Date(time.getFullYear(), 0, 1)
}

const getMonthBegin = timestamp => {
  const time = new Date(timestamp)
  const date = new Date(time.getFullYear(), time.getMonth(), 1)
  date.setDate(1)
  date.setMonth(date.getMonth() + 1)
  return +new Date(time.getFullYear(), time.getMonth(), 1)
}

const getDayBegin = timestamp => {
  return +new Date(timestamp)
}

const getUtcTime = timestamp => {
  if (timestamp) {
    const time = new Date(timestamp)
    const utcDate = new Date(
      time.getUTCFullYear(),
      time.getUTCMonth(),
      time.getUTCDate(),
      time.getUTCHours(),
      time.getUTCMinutes(),
      time.getUTCSeconds()
    )
    return utcDate
  } else {
    return timestamp
  }
}

// 年季度：后端 CONCAT(year,'-',quarter) 产出形如 "2026-3"（季度取值 1-4）
// 按自然季度换算为秒精度的闭区间
const getQuarterEnd = value => {
  const matched = /^(\d{4})[-/](\d{1,2})$/.exec(`${value}`)
  if (!matched) {
    return value
  }
  const year = Number(matched[1])
  const quarter = Number(matched[2])
  if (year < 1000 || quarter < 1 || quarter > 4) {
    return value
  }
  const start = +new Date(year, (quarter - 1) * 3, 1)
  const end = +new Date(year, quarter * 3, 1) - 1000
  return [start, end]
}

// %Y 是日历年，%u 是 MySQL WEEK(date, 1)，第 00 周和跨年周只包含当前年份的日期。
const getWeekEnd = value => {
  const matched = /^(\d{4})[-/](\d{1,2})$/.exec(`${value}`)
  if (!matched) {
    return value
  }
  const year = Number(matched[1])
  const week = Number(matched[2])
  if (year < 1000 || week > 53) {
    return value
  }
  // 含 1 月 4 日的那一周即为该年第 1 周，取其周一
  const jan4 = new Date(year, 0, 4)
  const offset = (jan4.getDay() + 6) % 7
  const weekStart = new Date(year, 0, 4 - offset + (week - 1) * 7)
  const weekEnd = new Date(year, 0, 4 - offset + week * 7)
  const yearStart = +new Date(year, 0, 1)
  const yearEnd = +new Date(year + 1, 0, 1)
  const start = Math.max(+weekStart, yearStart)
  const end = Math.min(+weekEnd, yearEnd)
  return start < end ? [start, end - 1000] : value
}
