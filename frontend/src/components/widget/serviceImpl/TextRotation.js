
import { WidgetService } from '../service/WidgetService'
const leftPanel = {
  icon: 'iconfont icon-xialakuang',
  label: 'deextrotation.label',
  defaultClass: 'text-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      multiple: false,
      placeholder: 'deextrotation.placeholder',
      viewIds: [],
      datas: [],
      key: 'id',
      label: 'text',
      value: 'id',
      fieldId: '',
      dragItems: []
    },
    value: '',
    manualModify: false,
    navTabList: [],
    fontSize: 12,
    color: '#333',
    vertical: 'directory',
    horizontal: 'center',
    heightTabs: '',
    defaultBg: '',
    highlight: '#333',
    highlightBg: '',
    heightBgImg: '',
    spacing: 0,
    pattern: 'default', // 滚动和默认
    scrollPage: 2,
    autoplay: true,
    autoTime: 1
  },
  defaultClass: 'text-filter',
  component: 'de-rotation',
  miniSizex: 1,
  miniSizey: 1
}
const drawPanel = {
  type: 'customBottm',
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
  component: 'de-rotation'
}

class TextSelectServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'textRotation' })
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
      operator: element.options.attrs.multiple ? 'in' : 'eq'
    }
    return param
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
const textBannerServiceImpl = new TextSelectServiceImpl()
export default textBannerServiceImpl
