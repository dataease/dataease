function getThisYear() {
  return new Date(`${new Date().getFullYear()}`)
}

function getlastYear() {
  return new Date(`${new Date().getFullYear() - 1}`)
}

function getThisMonth() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/${date.getMonth() + 1}`)
}

function getLastMonth() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/${date.getMonth()}`)
}

function getToday() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`)
}

function getYesterday() {
  const date = new Date()
  return new Date(date.getTime() - 24 * 60 * 60 * 1000)
}

function getMonthBeginning() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/${date.getMonth() + 1}/1`)
}

function getYearBeginning() {
  const date = new Date()
  return new Date(`${date.getFullYear()}/1/1`)
}

function getCustomTime(timeNum: number, timeType: string, around: string, arbitraryTime?: string) {
  const date = new Date()
  const num = around === 'f' ? -timeNum : timeNum
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()

  let resultYear = timeType === 'year' ? year + num : year
  let resultMonth = timeType === 'month' ? month + num : month
  if (resultMonth > 12) {
    resultYear += parseInt(`${resultMonth / 12}`)
    resultMonth = resultMonth % 12
  }

  if (resultMonth < 0) {
    resultYear += parseInt(`${resultMonth / 12}`) - 1
    resultMonth = month + (resultMonth % 12)
  }

  if (resultMonth === 0) {
    resultYear += parseInt(`${resultMonth / 12}`) - 1
    resultMonth = 12
  }
  const resultDate =
    timeType === 'date' ? new Date(date.getTime() + 24 * 60 * 60 * 1000 * num).getDate() : day
  if (timeType === 'date') {
    resultMonth = new Date(date.getTime() + 24 * 60 * 60 * 1000 * num).getMonth() + 1
    resultYear = new Date(date.getTime() + 24 * 60 * 60 * 1000 * num).getFullYear()
  }

  if (!!arbitraryTime) {
    const time = new Date(arbitraryTime)
    time.setFullYear(resultYear)
    time.setMonth(resultMonth)
    time.setDate(resultDate)
    return time
  }
  return new Date(`${resultYear}/${resultMonth}/${resultDate}`)
}

export {
  getThisYear,
  getlastYear,
  getThisMonth,
  getLastMonth,
  getToday,
  getYesterday,
  getMonthBeginning,
  getYearBeginning,
  getCustomTime
}
