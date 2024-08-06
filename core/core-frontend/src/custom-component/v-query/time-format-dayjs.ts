import dayjs from 'dayjs'
import type { ManipulateType } from 'dayjs'
function getThisStart(val = 'month' as ManipulateType) {
  return new Date(dayjs().startOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getThisEnd(val = 'month' as ManipulateType) {
  return new Date(dayjs().endOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getLastStart(val = 'month' as ManipulateType) {
  return new Date(dayjs().subtract(1, val).startOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getLastEnd(val = 'month' as ManipulateType) {
  return new Date(dayjs().subtract(1, val).endOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getAround(val = 'month' as ManipulateType, type = 'add', num = 0) {
  return new Date(dayjs()[type](num, val).startOf('day').format('YYYY/MM/DD HH:mm:ss'))
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
    case 'LastThreeMonths':
      return [
        new Date(dayjs().subtract(3, 'month').startOf('month').format('YYYY/MM/DD HH:mm:ss')),
        getThisEnd('day')
      ]
    case 'LastSixMonths':
      return [
        new Date(dayjs().subtract(6, 'month').startOf('month').format('YYYY/MM/DD HH:mm:ss')),
        getThisEnd('day')
      ]
    case 'LastTwelveMonths':
      return [
        new Date(dayjs().subtract(12, 'month').startOf('month').format('YYYY/MM/DD HH:mm:ss')),
        getThisEnd('day')
      ]
    case 'today':
      return [getThisStart('day'), getThisEnd('day')]
    case 'yesterday':
      return [getLastStart('day'), getLastEnd('day')]
    case 'LastThreeDays':
      return [
        new Date(dayjs().subtract(3, 'day').startOf('day').format('YYYY/MM/DD HH:mm:ss')),
        getThisEnd('day')
      ]
    case 'monthBeginning':
      return [getThisStart('month'), getThisEnd('day')]
    case 'yearBeginning':
      return [getThisStart('year'), getThisEnd('day')]
    default:
      return [new Date(), new Date()]
  }
}
export { getThisStart, getThisEnd, getLastStart, getLastEnd, getAround, getCustomRange }
