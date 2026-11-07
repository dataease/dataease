// 只有时分秒、没有日期部分，无法确定绝对时间区间，保持原值不参与区间联动
const NO_DATE_GRANULARITY = ['H_m_s']

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
    case 'M_d':
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
// 按自然季度换算成左闭右开再回退 1ms 的闭区间
const getQuarterEnd = value => {
  const matched = /^(\d{4})-(\d{1,2})$/.exec(`${value}`)
  if (!matched) {
    return value
  }
  const year = Number(matched[1])
  const quarter = Number(matched[2])
  if (quarter < 1 || quarter > 4) {
    return value
  }
  const start = +new Date(year, (quarter - 1) * 3, 1)
  const end = +new Date(year, quarter * 3, 1) - 1000
  return [start, end]
}

// 年周：后端 %Y-%u 产出形如 "2026-44"，%u 是周一为起始的周序号（00-53）
// MySQL 的 %u 等价于 WEEK(date, 1)：第 1 周是「当年第一个包含 4 天以上的周」，
// 也就是 ISO 8601 口径 —— 含 1 月 4 日的那一周为第 1 周。
// 因此必须以 1 月 4 日为基准回退到当周周一，不能用 1 月 1 日，
// 否则在「1 月 1 日落在周五~周日」的年份会整体偏移一周（如 2027 年）。
const getWeekEnd = value => {
  const matched = /^(\d{4})-(\d{1,2})$/.exec(`${value}`)
  if (!matched) {
    return value
  }
  const year = Number(matched[1])
  const week = Number(matched[2])
  if (week < 1 || week > 53) {
    return value
  }
  // 含 1 月 4 日的那一周即为该年第 1 周，取其周一
  const jan4 = new Date(year, 0, 4)
  const offset = (jan4.getDay() + 6) % 7
  const firstWeekMonday = +new Date(year, 0, 4 - offset)
  const start = firstWeekMonday + (week - 1) * 7 * 24 * 60 * 60 * 1000
  const end = start + 7 * 24 * 60 * 60 * 1000 - 1000
  return [start, end]
}
