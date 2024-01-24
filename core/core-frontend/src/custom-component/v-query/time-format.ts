function getThisYear() {
  return new Date(`${new Date().getFullYear()}/1`)
}

function getLastYear() {
  return new Date(`${new Date().getFullYear() - 1}/1`)
}

function getNextYear() {
  return new Date(`${new Date().getFullYear() + 1}/1`)
}

function getThisMonth() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/${date.getMonth() + 1}`)
}

function getLastMonth() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/${date.getMonth()}`)
}

function getNextMonth() {
  const date = getCustomTime(1, 'month', 'month', 'b')
  return new Date(`${date.getFullYear()}/${date.getMonth() + 1}`)
}

function getToday() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`)
}

function getYesterday() {
  const date = new Date(new Date().getTime() - 24 * 60 * 60 * 1000)
  return new Date(`${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`)
}

function getMonthBeginning() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/${date.getMonth() + 1}/1`)
}

function getYearBeginning() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/1/1`)
}

function getYearMonthRange(result, flag, sort) {
  const [direction, scene] = (sort || '').split('-')
  const [dateTimeType] = (flag || '').split('range')
  if (direction === 'start') {
    return result
  } else if (direction === 'end') {
    if (scene === 'config') {
      return result
    } else if (scene === 'panel') {
      return new Date(
        +getCustomTime(1, dateTimeType, dateTimeType, 'b', null, flag, 'start-config', result) -
          1000
      )
    }
  }
}

function getCustomTime(
  timeNum: number,
  timeType: string,
  timeGranularity: string,
  around: string,
  arbitraryTime?: Date,
  timeGranularityMultiple?: string,
  sort?: string,
  withDate?: Date
) {
  const date = withDate ? new Date(withDate) : new Date()
  const num = around === 'f' ? -timeNum : timeNum
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()

  let resultYear = timeType === 'year' ? year + num : year
  let resultMonth = timeType === 'month' ? month + num : month
  if (resultMonth > 12) {
    resultYear += parseInt(`${resultMonth / 12}`)
    resultMonth = resultMonth % 12
  } else if (resultMonth < 0) {
    resultYear += parseInt(`${resultMonth / 12}`) - 1
    resultMonth = month + (resultMonth % 12)
  } else if (resultMonth === 0) {
    resultYear += parseInt(`${resultMonth / 12}`) - 1
    resultMonth = 12
  }
  const resultDate =
    timeType === 'date' ? new Date(date.getTime() + 24 * 60 * 60 * 1000 * num).getDate() : day
  if (timeType === 'date') {
    resultMonth = new Date(date.getTime() + 24 * 60 * 60 * 1000 * num).getMonth() + 1
    resultYear = new Date(date.getTime() + 24 * 60 * 60 * 1000 * num).getFullYear()
  }

  switch (timeGranularityMultiple) {
    case 'monthrange':
      return getYearMonthRange(new Date(`${resultYear}/${resultMonth}/1`), 'monthrange', sort)
    case 'yearrange':
      return getYearMonthRange(new Date(`${resultYear}/1`), 'yearrange', sort)
    default:
      break
  }

  if (!!arbitraryTime) {
    const time = new Date(arbitraryTime)
    time.setFullYear(resultYear)
    time.setMonth(resultMonth - 1)
    time.setDate(resultDate)
    return time
  }

  switch (timeGranularity) {
    case 'year':
      return new Date(`${resultYear}/1`)
    case 'month':
      return new Date(`${resultYear}/${resultMonth}/1`)
    case 'date':
      return new Date(`${resultYear}/${resultMonth}/${resultDate}`)
    case 'monthrange':
      return new Date(`${resultYear}/${resultMonth}/1`)
    case 'yearrange':
      return new Date(`${resultYear}/1`)
    default:
      break
  }
}

function getDynamicRange({
  relativeToCurrent,
  timeNum,
  relativeToCurrentType,
  around,
  arbitraryTime,
  timeGranularity
}) {
  let selectValue = null
  if (relativeToCurrent === 'custom') {
    const startTime = getCustomTime(timeNum, relativeToCurrentType, timeGranularity, around)
    const endTime = getCustomTime(
      timeNum + (around === 'f' ? -1 : 1),
      relativeToCurrentType,
      timeGranularity,
      around
    )
    switch (timeGranularity) {
      case 'year':
        selectValue = [startTime.getTime(), endTime.getTime() - 1000]
        break
      case 'month':
        selectValue = [startTime.getTime(), endTime.getTime() - 1000]
        break
      case 'date':
        const dateVal = getCustomTime(timeNum, relativeToCurrentType, timeGranularity, around)
        selectValue = [dateVal.getTime(), dateVal.getTime() + 24 * 3600 * 1000 - 1000]
        break
      case 'datetime':
        const datetimeVal = getCustomTime(
          timeNum,
          relativeToCurrentType,
          timeGranularity,
          around,
          arbitraryTime
        )
        selectValue = [datetimeVal.getTime(), datetimeVal.getTime()]
        break
      default:
        break
    }
  } else {
    const isDateTime = timeGranularity === 'datetime'
    switch (relativeToCurrent) {
      case 'thisYear':
        selectValue = [getThisYear().getTime(), getNextYear().getTime() - 1000]
        break
      case 'lastYear':
        selectValue = [getLastYear().getTime(), getYearBeginning().getTime() - 1000]
        break
      case 'thisMonth':
        selectValue = [getThisMonth().getTime(), getNextMonth().getTime() - 1000]
        break
      case 'lastMonth':
        selectValue = [getLastMonth().getTime(), getMonthBeginning().getTime() - 1000]
        break
      case 'today':
        const todayVal = getToday().getTime()
        selectValue = [todayVal, isDateTime ? todayVal : todayVal + 24 * 3600 * 1000 - 1000]
        break
      case 'yesterday':
        const yesterdayVal = getYesterday().getTime()
        selectValue = [
          yesterdayVal,
          isDateTime ? yesterdayVal : yesterdayVal + 24 * 3600 * 1000 - 1000
        ]
        break
      case 'monthBeginning':
        const monthBeginningVal = getMonthBeginning().getTime()
        selectValue = [
          monthBeginningVal,
          isDateTime ? monthBeginningVal : monthBeginningVal + 24 * 3600 * 1000 - 1000
        ]
        break
      case 'yearBeginning':
        const yearBeginningVal = getYearBeginning().getTime()
        selectValue = [
          yearBeginningVal,
          isDateTime ? yearBeginningVal : yearBeginningVal + 24 * 3600 * 1000 - 1000
        ]
        break

      default:
        break
    }
  }

  return selectValue
}

export {
  getThisYear,
  getLastYear,
  getThisMonth,
  getLastMonth,
  getToday,
  getYesterday,
  getMonthBeginning,
  getYearBeginning,
  getCustomTime,
  getDynamicRange
}
