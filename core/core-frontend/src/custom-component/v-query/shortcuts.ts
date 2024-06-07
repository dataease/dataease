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

// eslint-disable-next-line
let callback: Function = () => {}
const shortcuts = [
  {
    text: 'dynamic_time.cweek',
    onClick: ({ attrs, slots, emit }) => {
      if (
        callback([
          new Date(+new Date(getThisStart('week')) + 24 * 1000 * 3600),
          new Date(+new Date(getThisEnd('week')) + 24 * 1000 * 3600)
        ])
      )
        return
      emit('pick', [
        dayjs(new Date(+new Date(getThisStart('week')) + 24 * 1000 * 3600)),
        dayjs(new Date(+new Date(getThisEnd('week')) + 24 * 1000 * 3600))
      ])
    }
  },
  {
    text: 'dynamic_month.current',
    value: () => {
      return [getThisStart('month'), getThisEnd('month')]
    },
    onClick: () => {
      console.log(113)
    }
  },
  {
    text: 'dynamic_time.cquarter',
    value: () => {
      return [getThisStart('quarter'), getThisEnd('quarter')]
    },
    onClick: () => {
      console.log(113)
    }
  },
  {
    text: 'dynamic_year.current',
    value: () => {
      return [getThisStart('year'), getThisEnd('year')]
    }
  },

  {
    text: 'dynamic_time.lweek',
    value: () => {
      return [
        new Date(+new Date(getLastStart('week')) + 24 * 1000 * 3600),
        new Date(+new Date(getLastEnd('week')) + 24 * 1000 * 3600)
      ]
    }
  },
  {
    text: 'dynamic_month.last',
    value: () => {
      return [getLastStart('month'), getLastEnd('month')]
    }
  },
  {
    text: 'dynamic_time.lquarter',
    value: () => {
      return [getLastStart('quarter'), getLastEnd('quarter')]
    }
  },
  {
    text: 'dynamic_year.last',
    value: () => {
      return [getLastStart('year'), getLastEnd('year')]
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
