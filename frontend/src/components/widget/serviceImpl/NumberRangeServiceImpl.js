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
      viewIds: [],
      fieldId: '',
      dragItems: []
    },
    value: '',
    manualModify: false
  },
  defaultClass: 'tree-filter',
  component: 'de-number-range'
}
const drawPanel = {
  type: 'custom',
  style: {
    width: 300,
    height: 90,
    fontSize: 14,
    fontWeight: 500,
    lineHeight: '',
    letterSpacing: 0,
    textAlign: '',
    color: ''
  },
  component: 'de-number-range',
  miniSizex: 1,
  miniSizey: 2
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
      return field['deType'] === 2 || field['deType'] === 3
    })
  }

  getParam(element) {
    if (element.options.value && element.options.value.length > 0) {
      const values = element.options.value
      const min = values[0]
      let max = null
      if (values.length > 1) {
        max = values[1]
      }
      const param = {
        component: element,
        value: [min, max],
        operator: 'between'
      }
      if (min && max) {
        return param
      }
      if (!min && !max) {
        param.value = []
        return param
      }
      if (min) {
        param.value = [min]
        param.operator = 'ge'
        return param
      }
      if (max) {
        param.value = [max]
        param.operator = 'le'
        return param
      }
    }
    return {
      component: element,
      value: [],
      operator: 'eq'
    }
  }
}
const numberRangeServiceImpl = new NumberRangeServiceImpl()
export default numberRangeServiceImpl
