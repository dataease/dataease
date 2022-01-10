import {
  WidgetService
} from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-ri',
  label: 'dedate.label',
  defaultClass: 'time-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      type: 'date',
      placeholder: 'dedate.placeholder',
      viewIds: [],
      fieldId: '',
      dragItems: [],
      default: {
        isDynamic: false,
        dkey: 0,
        dynamicPrefix: 1,
        dynamicInfill: 'day',
        dynamicSuffix: 'before'
      }
    },
    value: ''
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

class TimeDateServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, {
      name: 'timeDateWidget'
    })
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
  dynamicDateFormNow(element) {
    if (element.options.attrs.default === null || typeof element.options.attrs.default === 'undefined' || !element.options.attrs.default.isDynamic) return null

    if (element.options.attrs.default.dkey === 0) {
      return Date.now()
    }

    if (element.options.attrs.default.dkey === 1) {
      const oneday = 24 * 3600 * 1000
      return Date.now() - oneday
    }

    if (element.options.attrs.default.dkey === 2) {
      const now = new Date()
      const nowMonth = now.getMonth()
      var nowYear = now.getFullYear()
      return new Date(nowYear, nowMonth, 1).getTime()
    }

    if (element.options.attrs.default.dkey === 3) {
      const dynamicPrefix = parseInt(element.options.attrs.default.dynamicPrefix)
      const dynamicInfill = element.options.attrs.default.dynamicInfill
      const dynamicSuffix = element.options.attrs.default.dynamicSuffix

      if (dynamicInfill === 'day') {
        const oneday = 24 * 3600 * 1000
        const step = oneday * dynamicPrefix
        return dynamicSuffix === 'before' ? (Date.now() - step) : (Date.now() + step)
      }
      if (dynamicInfill === 'week') {
        const oneday = 24 * 3600 * 1000
        const step = oneday * dynamicPrefix * 7
        return dynamicSuffix === 'before' ? (Date.now() - step) : (Date.now() + step)
      }
      if (dynamicInfill === 'month') {
        const now = new Date()
        const nowMonth = now.getMonth()
        const nowYear = now.getFullYear()
        const nowDate = now.getDate()

        if (dynamicSuffix === 'before') {
          return new Date(nowYear, nowMonth - dynamicPrefix, nowDate).getTime()
        } else {
          return new Date(nowYear, nowMonth + dynamicPrefix, nowDate).getTime()
        }
      }
      if (dynamicInfill === 'year') {
        const now = new Date()
        const nowMonth = now.getMonth()
        const nowYear = now.getFullYear()
        const nowDate = now.getDate()

        return new Date(dynamicSuffix === 'before' ? (nowYear - dynamicPrefix) : (nowYear + dynamicPrefix), nowMonth, nowDate).getTime()
      }
    }
  }
}
const timeDateServiceImpl = new TimeDateServiceImpl({
  name: 'timeDateWidget'
})
export default timeDateServiceImpl
