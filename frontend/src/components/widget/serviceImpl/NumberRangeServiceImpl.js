import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-zuoce-qujian',
  label: '数值区间',
  defaultClass: 'text-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      placeholder: '请输入整数',
      viewIds: []
    },
    value: ''
  },
  defaultClass: 'text-filter',
  component: 'de-number-range'
}
const drawPanel = {
  type: 'custom',
  style: {
    width: 500,
    // height: 45.5,
    height: 90,
    fontSize: 14,
    fontWeight: 500,
    lineHeight: '',
    letterSpacing: 0,
    textAlign: '',
    color: ''
  },
  component: 'de-number-range'
}

class NumberRangeServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'numberRangeWidget' })
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
      return field['deType'] === 2
    })
  }
}
const numberRangeServiceImpl = new NumberRangeServiceImpl()
export default numberRangeServiceImpl
