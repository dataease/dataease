import dayjs from 'dayjs'
import type { ManipulateType, QUnitType } from 'dayjs'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'
type ManipulateTypeWithQuarter = ManipulateType | 'quarter'
dayjs.extend(quarterOfYear)

function getThisStart(val = 'month' as ManipulateTypeWithQuarter) {
  return new Date(
    dayjs()
      .startOf(val as QUnitType)
      .format('YYYY/MM/DD HH:mm:ss')
  )
}

function getThisEnd(val = 'month' as ManipulateTypeWithQuarter) {
  return new Date(
    dayjs()
      .endOf(val as QUnitType)
      .format('YYYY/MM/DD HH:mm:ss')
  )
}

function getLastStart(val = 'month' as ManipulateType) {
  return new Date(dayjs().subtract(1, val).startOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getLastEnd(val = 'month' as ManipulateType) {
  return new Date(dayjs().subtract(1, val).endOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getYearToLastMonthEnd(): [Date, Date] {
  const start = getThisStart('year')
  const end = getLastEnd('month')
  if (+start > +end) {
    return [start, getThisEnd('day')]
  }
  return [start, end]
}

function getAround(val = 'month' as ManipulateType, type = 'add', num = 0) {
  if (val === 'week') {
    return new Date(dayjs().endOf('week').add(1, 'day').endOf('day').format('YYYY/MM/DD HH:mm:ss'))
  }
  return new Date(dayjs()[type](num, val).endOf('day').format('YYYY/MM/DD HH:mm:ss'))
}

function getAroundStart(val = 'month' as ManipulateType, type = 'add', num = 0) {
  if (val === 'week') {
    return new Date(
      dayjs().startOf('week').add(1, 'day').startOf('day').format('YYYY/MM/DD HH:mm:ss')
    )
  }
  return new Date(dayjs()[type](num, val).startOf('day').format('YYYY/MM/DD HH:mm:ss'))
}

function getThisWeek(): [Date, Date] {
  return [
    new Date(dayjs().startOf('week').add(1, 'day').startOf('day').format('YYYY/MM/DD HH:mm:ss')),
    new Date(dayjs().endOf('week').add(1, 'day').endOf('day').format('YYYY/MM/DD HH:mm:ss'))
  ]
}

function getCustomRange(relativeToCurrentRange: string): [Date, Date] {
  switch (relativeToCurrentRange) {
    case 'thisYear':
      return [getThisStart('year'), getThisEnd('year')]
    case 'lastYear':
      return [getLastStart('year'), getLastEnd('year')]
    case 'thisMonth':
      return [getThisStart('month'), getThisEnd('month')]
    case 'lastMonth':
      return [getLastStart('month'), getLastEnd('month')]
    case 'thisQuarter':
      return [getThisStart('quarter'), getThisEnd('quarter')]
    case 'thisWeek':
      return getThisWeek()
    case 'LastThreeMonths':
      return [
        new Date(dayjs().subtract(2, 'month').startOf('month').format('YYYY/MM/DD HH:mm:ss')),
        getThisEnd('day')
      ]
    case 'LastSixMonths':
      return [
        new Date(dayjs().subtract(5, 'month').startOf('month').format('YYYY/MM/DD HH:mm:ss')),
        getThisEnd('day')
      ]
    case 'LastTwelveMonths':
      return [
        new Date(dayjs().subtract(11, 'month').startOf('month').format('YYYY/MM/DD HH:mm:ss')),
        getThisEnd('day')
      ]
    case 'YearToThisMonth':
      return [new Date(dayjs().startOf('year').format('YYYY/MM/DD HH:mm:ss')), getThisEnd('month')]
    case 'YearToLastMonthEnd':
      return getYearToLastMonthEnd()
    case 'monthToYesterday':
      const sm = new Date(dayjs().startOf('month').format('YYYY/MM/DD HH:mm:ss'))
      const ld = getLastEnd('day')
      if (+sm > +ld) {
        return [sm, getThisEnd('day')]
      }
      return [sm, ld]
    case 'today':
      return [getThisStart('day'), getThisEnd('day')]
    case 'yesterday':
      return [getLastStart('day'), getLastEnd('day')]
    case 'LastThreeDays':
      return [
        new Date(dayjs().subtract(2, 'day').startOf('day').format('YYYY/MM/DD HH:mm:ss')),
        getThisEnd('day')
      ]
    case 'LastMonthFull':
      return [getLastStart('month'), getLastEnd('month')]
    case 'monthBeginning':
      return [getThisStart('month'), getThisEnd('day')]
    case 'yearBeginning':
      return [getThisStart('year'), getThisEnd('day')]
    default:
      return [new Date(), new Date()]
  }
}
export {
  getThisStart,
  getThisEnd,
  getLastStart,
  getLastEnd,
  getYearToLastMonthEnd,
  getAround,
  getCustomRange,
  getAroundStart
}
