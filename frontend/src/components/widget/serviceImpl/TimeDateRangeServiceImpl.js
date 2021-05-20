import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-riqi',
  label: '日期范围',
  defaultClass: 'time-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      type: 'daterange',
      rangeSeparator: '至',
      startPlaceholder: '开始日期',
      endPlaceholder: '结束日期',
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
    height: 35,
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
    Object.assign(options, { name: 'timeDateRangeWidget' })
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
const timeDateRangeServiceImpl = new TimeDateRangeServiceImpl()
export default timeDateRangeServiceImpl
