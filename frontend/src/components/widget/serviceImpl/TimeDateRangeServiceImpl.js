import {
  WidgetService
} from '../service/WidgetService'
import {
  timeSection
} from '@/utils'
const leftPanel = {
  icon: 'iconfont icon-riqi',
  label: 'dedaterange.label',
  defaultClass: 'time-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      type: 'daterange',
      rangeSeparator: 'dedaterange.split_placeholder',
      startPlaceholder: 'dedaterange.from_placeholder',
      endPlaceholder: 'dedaterange.to_placeholder',
      viewIds: [],
      fieldId: '',
      dragItems: [],
      default: {
        isDynamic: false,
        dkey: 0,
        sDynamicPrefix: 1,
        sDynamicInfill: 'day',
        sDynamicSuffix: 'before',
        eDynamicPrefix: 1,
        eDynamicInfill: 'day',
        eDynamicSuffix: 'after'
      },
      showTime: false,
      accuracy: 'HH:mm',
      parameters: [],
      startParameters: [],
      endParameters: []
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

class TimeDateRangeServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, {
      name: 'timeDateRangeWidget'
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
  getStartDayOfWeek(step) {
    var now = new Date() // 当前日期
    var nowDayOfWeek = now.getDay()
    var nowDay = now.getDate() // 当前日
    var nowMonth = now.getMonth() // 当前月
    var day = nowDayOfWeek || 7
    var resultDay = nowDay + 1 - day
    if (step !== null) {
      resultDay += (step * 7)
    }
    return new Date(now.getFullYear(), nowMonth, resultDay)
  }
  getEndDayOfWeek(step) {
    var now = new Date() // 当前日期
    var nowDayOfWeek = now.getDay()
    var nowDay = now.getDate() // 当前日
    var nowMonth = now.getMonth() // 当前月
    var day = nowDayOfWeek || 7
    var resultDay = nowDay + 7 - day
    if (step !== null) {
      resultDay += (step * 7)
    }
    return new Date(now.getFullYear(), nowMonth, resultDay)
  }
  getStartDayOfMonth(step) {
    var now = new Date()
    var nowMonth = now.getMonth() // 当前月
    if (step !== null) {
      nowMonth += step
    }
    var monthStartDate = new Date(now.getFullYear(), nowMonth, 1)
    return monthStartDate
  }
  getEndDayOfMonth(step) {
    var now = new Date()
    var nowMonth = now.getMonth() // 当前月
    var days = this.getMonthDays()

    if (step !== null) {
      nowMonth += step
      days = this.getMonthDays(step)
    }

    var monthEndDate = new Date(now.getFullYear(), nowMonth, days)
    return monthEndDate
  }
  getStartQuarter(step) {
    var now = new Date()
    var nowMonth = now.getMonth()
    var startMonth = Math.floor((nowMonth / 3)) * 3
    if (step !== null) {
      startMonth += (step * 3)
    }
    return new Date(now.getFullYear(), startMonth, 1)
  }
  getEndQuarter(step) {
    var now = new Date()
    var nowMonth = now.getMonth()
    const quar = Math.floor(nowMonth / 3)
    var endMonth = quar * 3 + 2

    if (step !== null) {
      endMonth += (step * 3)
    }

    const days = (endMonth === 5 || endMonth === 8) ? 30 : 31
    return new Date(now.getFullYear(), endMonth, days)
  }
  getStartYear(step) {
    var now = new Date()
    var year = now.getFullYear()
    if (step !== null) {
      year += step
    }
    return new Date(year, 0, 1)
  }
  getEndYear(step) {
    var now = new Date()
    var year = now.getFullYear()
    if (step !== null) {
      year += step
    }
    return new Date(year, 11, 31)
  }
  /**
   * 获得本月天数
   *
   * @returns
   */
  getMonthDays(step) {
    var now = new Date()
    var nowMonth = now.getMonth() // 当前月
    if (step !== null) {
      nowMonth += step
    }
    var monthStartDate = new Date(now.getFullYear(), nowMonth, 1)
    var monthEndDate = new Date(now.getFullYear(), nowMonth + 1, 1)
    var days = (monthEndDate - monthStartDate) / (1000 * 60 * 60 * 24)
    return days
  }
  customTime(dynamicPrefix, dynamicInfill, dynamicSuffix) {
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
  dynamicDateFormNow(element) {
    const values = this.dynamicDateFormNowProxy(element)
    return this.formatDynamicTimes(values, element)
  }
  dynamicDateFormNowProxy(element) {
    if (element.options.attrs.default === null || typeof element.options.attrs.default === 'undefined' || !element.options.attrs.default.isDynamic) return null

    if (element.options.attrs.default.dkey === 0) {
      return [this.getStartDayOfWeek(0).getTime(), this.getEndDayOfWeek(0).getTime()]
    }
    if (element.options.attrs.default.dkey === 5) { // 上周
      return [this.getStartDayOfWeek(-1).getTime(), this.getEndDayOfWeek(-1).getTime()]
    }

    if (element.options.attrs.default.dkey === 1) {
      return [this.getStartDayOfMonth(0).getTime(), this.getEndDayOfMonth(0).getTime()]
    }
    if (element.options.attrs.default.dkey === 6) { // 上月
      return [this.getStartDayOfMonth(-1).getTime(), this.getEndDayOfMonth(-1).getTime()]
    }

    if (element.options.attrs.default.dkey === 2) {
      return [this.getStartQuarter(0).getTime(), this.getEndQuarter(0).getTime()]
    }
    if (element.options.attrs.default.dkey === 7) { // 上季
      return [this.getStartQuarter(-1).getTime(), this.getEndQuarter(-1).getTime()]
    }

    if (element.options.attrs.default.dkey === 3) {
      return [this.getStartYear(0).getTime(), this.getEndYear(0).getTime()]
    }
    if (element.options.attrs.default.dkey === 8) { // 上年
      return [this.getStartYear(-1).getTime(), this.getEndYear(-1).getTime()]
    }

    if (element.options.attrs.default.dkey === 4) {
      const sDynamicPrefix = parseInt(element.options.attrs.default.sDynamicPrefix)
      const sDynamicInfill = element.options.attrs.default.sDynamicInfill
      const sDynamicSuffix = element.options.attrs.default.sDynamicSuffix

      const eDynamicPrefix = parseInt(element.options.attrs.default.eDynamicPrefix)
      const eDynamicInfill = element.options.attrs.default.eDynamicInfill
      const eDynamicSuffix = element.options.attrs.default.eDynamicSuffix
      const startTime = this.customTime(sDynamicPrefix, sDynamicInfill, sDynamicSuffix)
      const endTime = this.customTime(eDynamicPrefix, eDynamicInfill, eDynamicSuffix)
      return [startTime, endTime]
    }
  }

  formatDynamicTimes(values, element) {
    if (!values?.length || !element.options.attrs.default.isDynamic) {
      return values
    }
    const baseTime = +new Date('2022-11-09 00:00:00.000')
    let labelFormat = 'yyyy-MM-dd'
    if (element.options.attrs.showTime && element.options.attrs.accuracy) {
      labelFormat = labelFormat + ' ' + element.options.attrs.accuracy
    }
    let [start, end] = values

    const attrs = element.options.attrs

    if (attrs.default.sDynamicSuffixTime && attrs.default.isDynamic && attrs.default.dkey === 4 && attrs.showTime) {
      start = attrs.default.sDynamicSuffixTime - baseTime + timeSection(start, 'date')[0]
    } else {
      start = timeSection(start, 'date', labelFormat)[0]
    }
    if (attrs.default.eDynamicSuffixTime && attrs.default.isDynamic && attrs.default.dkey === 4 && attrs.showTime) {
      end = attrs.default.eDynamicSuffixTime - baseTime + timeSection(end, 'date')[0]
    } else {
      end = timeSection(end, 'date', labelFormat)[1]
    }

    const results = [start, end]
    return results
  }
  validDynamicValue(element) {
    if (!element.options.attrs.default.isDynamic) return true
    if (element.options.attrs.default.dkey !== 4) return true
    try {
      const arr = this.dynamicDateFormNow(element)
      return arr[0] < arr[1]
    } catch (error) {
      return false
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
    const componentType = element.options.attrs.showTime ? 'datetimerange' : 'daterange'
    let labelFormat = 'yyyy-MM-dd'
    if (element.options.attrs.showTime && element.options.attrs.accuracy) {
      labelFormat = labelFormat + ' ' + element.options.attrs.accuracy
    }
    if (element.options.attrs.type === 'daterange') {
      if (values.length !== 2) {
        return null
      }
      let start = values[0]
      let end = values[1]

      start = timeSection(start, 'datetime', labelFormat)[0]
      end = timeSection(end, 'datetime', labelFormat)[1]
      const results = [start, end]
      return results
    } else {
      const value = values[0]

      return timeSection(parseFloat(value), componentType || element.options.attrs.type, labelFormat)
    }
  }
  isTimeWidget() {
    return true
  }
  formatShortValues(values) {
    if (!values || values.length === 0) {
      return []
    }
    const labelFormat = 'yyyy-MM-dd'
    let start = values[0]
    let end = values[values.length - 1]
    start = timeSection(start, 'datetime', labelFormat)[0]
    end = timeSection(end, 'datetime', labelFormat)[1]
    const results = [start, end]
    return results
  }
  shortcuts() {
    return [
      { 'text': 'dynamic_time.cweek', 'callBack': () => this.formatShortValues([this.getStartDayOfWeek(0).getTime(), this.getEndDayOfWeek(0).getTime()]) },
      { 'text': 'dynamic_month.current', 'callBack': () => this.formatShortValues([this.getStartDayOfMonth(0).getTime(), this.getEndDayOfMonth(0).getTime()]) },
      { 'text': 'dynamic_time.cquarter', 'callBack': () => this.formatShortValues([this.getStartQuarter(0).getTime(), this.getEndQuarter(0).getTime()]) },
      { 'text': 'dynamic_year.current', 'callBack': () => this.formatShortValues([this.getStartYear(0).getTime(), this.getEndYear(0).getTime()]) },

      { 'text': 'dynamic_time.lweek', 'callBack': () => this.formatShortValues([this.getStartDayOfWeek(-1).getTime(), this.getEndDayOfWeek(-1).getTime()]) },
      { 'text': 'dynamic_month.last', 'callBack': () => this.formatShortValues([this.getStartDayOfMonth(-1).getTime(), this.getEndDayOfMonth(-1).getTime()]) },
      { 'text': 'dynamic_time.lquarter', 'callBack': () => this.formatShortValues([this.getStartQuarter(-1).getTime(), this.getEndQuarter(-1).getTime()]) },
      { 'text': 'dynamic_year.last', 'callBack': () => this.formatShortValues([this.getStartYear(-1).getTime(), this.getEndYear(-1).getTime()]) }
    ]
  }
  isParamWidget() {
    return true
  }
  isRangeParamWidget() {
    return true
  }
}
const timeDateRangeServiceImpl = new TimeDateRangeServiceImpl()
export default timeDateRangeServiceImpl
