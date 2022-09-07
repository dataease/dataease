import { WidgetService } from '../service/WidgetService'
import {
  timeSection
} from '@/utils'
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
      parameters: [],
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
  }

  initFilterDialog() {
    const value = JSON.parse(JSON.stringify(dialogPanel))
    return value
  }
  customValue() {
    return 2
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

    const now = new Date()
    const nowYear = now.getFullYear()
    if (element.options.attrs.default.dkey === 0) {
      return new Date(nowYear, 0, 1).getTime()
    }

    if (element.options.attrs.default.dkey === 1) {
      return new Date(nowYear - 1, 0, 1).getTime()
    }

    if (element.options.attrs.default.dkey === 2) {
      const dynamicPrefix = parseInt(element.options.attrs.default.dynamicPrefix)
      const dynamicSuffix = element.options.attrs.default.dynamicSuffix

      return new Date(dynamicSuffix === 'before' ? (nowYear - dynamicPrefix) : (nowYear + dynamicPrefix), 0, 1).getTime()
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
      return timeSection(parseFloat(value), element.options.attrs.type)
    }
  }
}
const timeYearServiceImpl = new TimeYearServiceImpl()
export default timeYearServiceImpl
