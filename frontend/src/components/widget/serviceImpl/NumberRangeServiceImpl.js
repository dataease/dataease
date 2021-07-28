import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-zuoce-qujian',
  label: 'denumberrange.label',
  defaultClass: 'tree-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      placeholder_min: 'denumberrange.please_key_min',
      placeholder_max: 'denumberrange.please_key_max',
      viewIds: []
    },
    value: ''
  },
  defaultClass: 'tree-filter',
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
