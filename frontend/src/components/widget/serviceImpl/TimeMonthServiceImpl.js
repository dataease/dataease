import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-yue',
  label: 'deyearmonth.label',
  defaultClass: 'time-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      type: 'month',
      placeholder: 'deyearmonth.placeholder',
      viewIds: [],
      fieldId: '',
      dragItems: [],
      default: {
        isDynamic: false,
        dkey: 0,
        dynamicPrefix: 1,
        dynamicInfill: 'month',
        dynamicSuffix: 'before',
        radioOptions: [{ value: false, text: 'dynamic_month.fix' }, { value: true, text: 'dynamic_month.dynamic' }],
        relativeOptions: [
          { value: 0, text: 'dynamic_month.current' },
          { value: 1, text: 'dynamic_month.last' },
          { value: 2, text: 'dynamic_month.firstOfYear' },
          { value: 3, text: 'dynamic_time.custom' }
        ],
        custom: {
          unitsOptions: [
            { value: 'month', text: 'dynamic_time.month' }
          ],
          limits: [0, 10]
        }
      }
    },
    value: '',
    manualModify: false
  },
  defaultClass: 'time-filter',
  component: 'de-date'
}
const drawPanel = {
  type: 'custom',
  style: {
    width: 300,
    // height: 47,
    height: 90,
    fontSize: 14,
    fontWeight: 500,
    lineHeight: '',
    letterSpacing: 0,
    textAlign: '',
    color: ''
  },
  component: 'de-date'
}

class TimeMonthServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'timeMonthWidget' })
    super(options)
    this.filterDialog = true
    this.showSwitch = false
  }

  initLeftPanel() {
    const value = JSON.parse(JSON.stringify(leftPanel))
    return value
    // console.log('this is first initWidget')
  }

  initFilterDialog() {
    const value = JSON.parse(JSON.stringify(dialogPanel))
    return value
  }

  initDrawPanel() {
    const value = JSON.parse(JSON.stringify(drawPanel))
    return value
  }
  filterFieldMethod(fields) {
    return fields.filter(field => {
      return field['deType'] === 1
    })
  }
  defaultSetting() {
    return dialogPanel.options.attrs.default
  }
  dynamicDateFormNow(element) {
    const now = new Date()
    const nowMonth = now.getMonth()
    const nowYear = now.getFullYear()
    if (element.options.attrs.default === null || typeof element.options.attrs.default === 'undefined' || !element.options.attrs.default.isDynamic) return null

    if (element.options.attrs.default.dkey === 0) {
      return new Date(nowYear, nowMonth, 1).getTime()
    }

    if (element.options.attrs.default.dkey === 1) {
      return new Date(nowYear, nowMonth - 1, 1).getTime()
    }

    if (element.options.attrs.default.dkey === 2) {
      return new Date(nowYear, 0, 1).getTime()
    }

    if (element.options.attrs.default.dkey === 3) {
      const dynamicPrefix = parseInt(element.options.attrs.default.dynamicPrefix)
      const dynamicSuffix = element.options.attrs.default.dynamicSuffix

      if (dynamicSuffix === 'before') {
        return new Date(nowYear, nowMonth - dynamicPrefix, 1).getTime()
      } else {
        return new Date(nowYear, nowMonth + dynamicPrefix, 1).getTime()
      }
    }
  }
}
const timeMonthServiceImpl = new TimeMonthServiceImpl()
export default timeMonthServiceImpl
