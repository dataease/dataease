
import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-xialakuang',
  label: '数字下拉',
  defaultClass: 'text-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      multiple: false,
      placeholder: '请选择',
      datas: [],
      key: 'id',
      label: 'text',
      value: 'id'
    },
    value: ''
  },
  defaultClass: 'text-filter',
  component: 'de-select'
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
      return field['deType'] === 2
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
}
const numberSelectServiceImpl = new NumberSelectServiceImpl()
export default numberSelectServiceImpl
