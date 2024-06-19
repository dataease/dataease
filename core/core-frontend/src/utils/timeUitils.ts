export const getRange = (outerTimeValue, timeGranularity) => {
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
