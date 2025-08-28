import { useI18n } from '@/hooks/web/useI18n'
import dayjs from 'dayjs'
import type { ManipulateType, QUnitType } from 'dayjs'
import quarterOfYear from 'dayjs/plugin/quarterOfYear'
type ManipulateTypeWithQuarter = ManipulateType | 'quarter'
const { t } = useI18n()
dayjs.extend(quarterOfYear)

function getThisStart(val = 'month' as ManipulateTypeWithQuarter) {
  return new Date(dayjs().startOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getThisEnd(val = 'month' as ManipulateTypeWithQuarter) {
  return new Date(dayjs().endOf(val).format('YYYY/MM/DD HH:mm:ss'))
}

function getLastStart(val = 'month' as ManipulateTypeWithQuarter) {
  return new Date(
    dayjs()
      .subtract(1, val as QUnitType)
      .startOf(val)
      .format('YYYY/MM/DD HH:mm:ss')
  )
}

function getLastEnd(val = 'month' as ManipulateTypeWithQuarter) {
  return new Date(
    dayjs()
      .subtract(1, val as QUnitType)
      .endOf(val)
      .format('YYYY/MM/DD HH:mm:ss')
  )
}

function getNextStart(val = 'month' as ManipulateTypeWithQuarter) {
  return new Date(
    dayjs()
      .add(1, val as QUnitType)
      .startOf(val)
      .format('YYYY/MM/DD HH:mm:ss')
  )
}

function getNextEnd(val = 'month' as ManipulateTypeWithQuarter) {
  return new Date(
    dayjs()
      .add(1, val as QUnitType)
      .endOf(val)
      .format('YYYY/MM/DD HH:mm:ss')
  )
}

// eslint-disable-next-line
let callback: Function = () => {}
const shortcuts = [
  {
    text: 'dynamic_time.cweek',
    onClick: ({ emit }) => {
      const startTime = new Date(+new Date(getThisStart('week')) + 24 * 1000 * 3600)
      const endTime = new Date(+new Date(getThisEnd('week')) + 24 * 1000 * 3600)
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'dynamic_month.current',
    onClick: ({ emit }) => {
      const startTime = getThisStart('month')
      const endTime = getThisEnd('month')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'dynamic_time.cquarter',
    onClick: ({ emit }) => {
      const startTime = getThisStart('quarter')
      const endTime = getThisEnd('quarter')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'dynamic_year.current',
    onClick: ({ emit }) => {
      const startTime = getThisStart('year')
      const endTime = getThisEnd('year')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },

  {
    text: 'dynamic_time.lweek',
    onClick: ({ emit }) => {
      const startTime = new Date(+new Date(getLastStart('week')) + 24 * 1000 * 3600)
      const endTime = new Date(+new Date(getLastEnd('week')) + 24 * 1000 * 3600)
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'dynamic_month.last',
    onClick: ({ emit }) => {
      const startTime = getLastStart('month')
      const endTime = getLastEnd('month')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'dynamic_time.lquarter',
    onClick: ({ emit }) => {
      const startTime = getLastStart('quarter')
      const endTime = getLastEnd('quarter')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'dynamic_year.last',
    onClick: ({ emit }) => {
      const startTime = getLastStart('year')
      const endTime = getLastEnd('year')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'common.next_week',
    onClick: ({ emit }) => {
      const startTime = getNextStart('week')
      const endTime = getNextEnd('week')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'common.next_month',
    onClick: ({ emit }) => {
      const startTime = getNextStart('month')
      const endTime = getNextEnd('month')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'common.next_quarter',
    onClick: ({ emit }) => {
      const startTime = getNextStart('quarter')
      const endTime = getNextEnd('quarter')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  },
  {
    text: 'common.next_year',
    onClick: ({ emit }) => {
      const startTime = getNextStart('year')
      const endTime = getNextEnd('year')
      if (callback([startTime, endTime])) return
      emit('pick', [dayjs(startTime), dayjs(endTime)])
    }
  }
]

shortcuts.forEach(ele => {
  ele.text = t(ele.text)
})

const useShortcuts = cb => {
  callback = cb
  return {
    shortcuts
  }
}

export { useShortcuts }
