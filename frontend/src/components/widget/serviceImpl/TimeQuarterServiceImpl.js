import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-jidu',
  label: '季度',
  defaultClass: 'time-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      placeholder: '请选择季度'
    },
    value: ''
  },
  parameters: [],
  defaultClass: 'time-filter',
  component: 'de-quarter'
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
  component: 'de-quarter',
  miniSizex: 1,
  miniSizey: 1
}

class TimeQuarterServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'timeQuarterWidget' })
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
}
const timeQuarterServiceImpl = new TimeQuarterServiceImpl()
export default timeQuarterServiceImpl
