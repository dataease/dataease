import dayjs from 'dayjs'
function getThisStart(val = 'month') {
  return new Date(dayjs().startOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getThisEnd(val = 'month') {
  return new Date(dayjs().endOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getLastStart(val = 'month') {
  return new Date(dayjs().subtract(1, val).startOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getLastEnd(val = 'month') {
  return new Date(dayjs().subtract(1, val).endOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getAround(val = 'month', type = 'add', num = 0) {
  return new Date(dayjs()[type](num, val).startOf('day').format('YYYY/MM/DD HH:mm:ss'))
}

export { getThisStart, getThisEnd, getLastStart, getLastEnd, getAround }
