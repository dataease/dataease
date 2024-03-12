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

export { getThisStart, getThisEnd, getLastStart, getLastEnd, getAround }
