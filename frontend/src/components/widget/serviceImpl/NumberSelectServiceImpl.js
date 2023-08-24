
import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-xialakuang',
  label: 'denumberselect.label',
  defaultClass: 'tree-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      multiple: false,
      placeholder: 'denumberselect.placeholder',
      data: [],
      viewIds: [],
      parameters: [],
      key: 'id',
      label: 'text',
      value: 'id',
      fieldId: '',
      dragItems: [],
      sort: {}
    },
    value: '',
    manualModify: false
  },
  defaultClass: 'tree-filter',
  component: 'de-select',
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
  component: 'de-select'
}

class NumberSelectServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'numberSelectWidget' })
    super(options)
    this.filterDialog = true
    this.showSwitch = true
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

  optionData(data) {
    if (!data) return null
    return data.filter(item => !!item).map(item => {
      return {
        id: item,
        text: item
      }
    })
  }
  getParam(element) {
    const value = this.fillValueDerfault(element)
    const param = {
      component: element,
      value: !value ? [] : Array.isArray(value) ? value : value.toString().split(','),
      operator: element.options.attrs.multiple ? 'in' : 'eq'
    }
    return param
  }

  isSortWidget() {
    return true
  }
  fillValueDerfault(element) {
    const defaultV = element.options.value === null ? '' : element.options.value.toString()
    if (element.options.attrs.multiple) {
      if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return []
      return defaultV.split(',')
    } else {
      if (defaultV === null || typeof defaultV === 'undefined' || defaultV === '' || defaultV === '[object Object]') return null
      return defaultV.split(',')[0]
    }
  }
  isParamWidget() {
    return true
  }
}
const numberSelectServiceImpl = new NumberSelectServiceImpl()
export default numberSelectServiceImpl
