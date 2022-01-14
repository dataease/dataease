import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-nian',
  label: 'deyear.label',
  defaultClass: 'time-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      type: 'year',
      placeholder: 'deyear.placeholder',
      viewIds: [],
      fieldId: '',
      dragItems: [],
      default: {
        isDynamic: false,
        dkey: 0,
        dynamicPrefix: 1,
        dynamicInfill: 'year',
        dynamicSuffix: 'before',
        radioOptions: [{ value: false, text: 'dynamic_year.fix' }, { value: true, text: 'dynamic_year.dynamic' }],
        relativeOptions: [
          { value: 0, text: 'dynamic_year.current' },
          { value: 1, text: 'dynamic_year.last' },
          { value: 2, text: 'dynamic_time.custom' }
        ],
        custom: {
          unitsOptions: [
            { value: 'year', text: 'dynamic_time.year' }
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

class TimeYearServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'timeYearWidget' })
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
    if (element.options.attrs.default === null || typeof element.options.attrs.default === 'undefined' || !element.options.attrs.default.isDynamic) return null

    if (element.options.attrs.default.dkey === 0) {
      return Date.now()
    }

    if (element.options.attrs.default.dkey === 1) {
      const now = new Date()
      const nowYear = now.getFullYear()
      const nowMonth = now.getMonth()
      const nowDate = now.getDate()
      return new Date(nowYear - 1, nowMonth, nowDate).getTime()
    }

    if (element.options.attrs.default.dkey === 2) {
      const dynamicPrefix = parseInt(element.options.attrs.default.dynamicPrefix)
      const dynamicSuffix = element.options.attrs.default.dynamicSuffix

      const now = new Date()
      const nowMonth = now.getMonth()
      const nowYear = now.getFullYear()
      const nowDate = now.getDate()

      return new Date(dynamicSuffix === 'before' ? (nowYear - dynamicPrefix) : (nowYear + dynamicPrefix), nowMonth, nowDate).getTime()
    }
  }
}
const timeYearServiceImpl = new TimeYearServiceImpl()
export default timeYearServiceImpl
