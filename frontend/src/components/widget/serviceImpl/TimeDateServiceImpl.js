import {
  WidgetService
} from '../service/WidgetService'
import {
  timeSection
} from '@/utils'
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
      parameters: [],
      dragItems: [],
      default: {
        isDynamic: false,
        dkey: 0,
        dynamicPrefix: 1,
        dynamicInfill: 'day',
        dynamicSuffix: 'before',
        radioOptions: [{ value: false, text: 'dynamic_time.fix' }, { value: true, text: 'dynamic_time.dynamic' }],
        relativeOptions: [
          { value: 0, text: 'dynamic_time.today' },
          { value: 1, text: 'dynamic_time.yesterday' },
          { value: 2, text: 'dynamic_time.firstOfMonth' },
          { value: 4, text: 'dynamic_time.firstOfYear' },
          { value: 3, text: 'dynamic_time.custom' }
        ],
        custom: {
          unitsOptions: [
            { value: 'day', text: 'dynamic_time.date' },
            { value: 'week', text: 'dynamic_time.week' },
            { value: 'month', text: 'dynamic_time.month' },
            { value: 'year', text: 'dynamic_time.year' }
          ],
          limits: [1, 12]
        }
      },
      showTime: false,
      accuracy: 'HH:mm'

    },
    value: '',
    manualModify: false
  },
  defaultClass: 'time-filter',
  component: 'de-date',
  miniSizex: 1,
  miniSizey: 1
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
  customValue() {
    return 3
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
    if (element.options.attrs.default.dkey === 4) {
      const now = new Date()
      const nowYear = now.getFullYear()
      return new Date(nowYear, 0, 1).getTime()
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
  getParam(element) {
    let timeArr = []
    if (element.options.attrs.default && element.options.attrs.default.isDynamic) {
      let value = this.dynamicDateFormNow(element)
      value = this.formatFilterValue(value)
      timeArr = this.formatValues(value, element)
    } else {
      let value = this.fillValueDerfault(element)
      value = this.formatFilterValue(value)
      timeArr = this.formatValues(value, element)
    }
    const param = {
      component: element,
      value: timeArr,
      operator: 'between'
    }
    return param
  }
  fillValueDerfault(element) {
    const defaultV = element.options.value === null ? '' : element.options.value.toString()
    if (element.options.attrs.type === 'daterange') {
      if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV ===
          '[object Object]') {
        return []
      }
      return defaultV.split(',').map(item => parseFloat(item))
    } else {
      if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV ===
          '[object Object]') {
        return null
      }
      return parseFloat(defaultV.split(',')[0])
    }
  }
  formatFilterValue(values) {
    if (values === null) return []
    if (Array.isArray(values)) return values
    return [values]
  }
  formatValues(values, element) {
    if (!values || values.length === 0) {
      return []
    }
    if (element.options.attrs.type === 'daterange') {
      if (values.length !== 2) {
        return null
      }
      let start = values[0]
      let end = values[1]
      start = timeSection(start, 'date')[0]
      end = timeSection(end, 'date')[1]
      const results = [start, end]
      return results
    } else {
      const value = values[0]
      const componentType = element.options.attrs.showTime ? 'datetime' : 'date'
      let labelFormat = 'yyyy-MM-dd'
      if (element.options.attrs.showTime && element.options.attrs.accuracy) {
        labelFormat = labelFormat + ' ' + element.options.attrs.accuracy
      }

      return timeSection(parseFloat(value), componentType || element.options.attrs.type, labelFormat)
    }
  }
  isTimeWidget() {
    return true
  }
}
const timeDateServiceImpl = new TimeDateServiceImpl({
  name: 'timeDateWidget'
})
export default timeDateServiceImpl
