import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-nian',
  label: '年份',
  defaultClass: 'time-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      type: 'year',
      placeholder: '请选择年份',
      viewIds: []
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
    height: 47,
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
}
const timeYearServiceImpl = new TimeYearServiceImpl()
export default timeYearServiceImpl
