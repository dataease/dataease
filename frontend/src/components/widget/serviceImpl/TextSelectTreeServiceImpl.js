
import { WidgetService } from '../service/WidgetService'
const leftPanel = {
  icon: 'iconfont icon-xialashu',
  label: 'detextselectTree.label',
  defaultClass: 'text-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      multiple: false,
      placeholder: 'detextselectTree.placeholder',
      viewIds: [],
      parameters: [],
      datas: [],
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
  defaultClass: 'text-filter',
  component: 'de-select-tree',
  miniSizex: 1,
  miniSizey: 1
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
    color: '',
    hPosition: 'left',
    vPosition: 'center'
  },
  component: 'de-select-tree'
}

class TextSelectTreeServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'textSelectTreeWidget' })
    super(options)
    this.filterDialog = true
    this.showSwitch = true
    this.isTree = true
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
      return field['deType'] === 0
    })
  }

  optionDatas(datas) {
    if (!datas) return null
    return datas.filter(item => !!item).map(item => {
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
      operator: element.options.attrs.multiple ? 'in' : 'eq',
      isTree: true
    }
    if (param.value && param.value.length) {
      param.value = param.value.map(val => val.replaceAll('-de-', ','))
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
}
const textSelectTreeServiceImpl = new TextSelectTreeServiceImpl()
export default textSelectTreeServiceImpl
