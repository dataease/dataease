
import { DrawWidgetService } from '../service/DrawWidgetService'

const leftPanel = {
//   name: 'text-select',
  icon: 'iconfont icon-xialakuang',
  label: '文本下拉',
  defaultClass: 'text-filter'
}

const dialogPanel = {
  options: {
    refId: '1234567890',
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

class MySelectImpl extends DrawWidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'mySelectWidget' })
    super(options)
    this.filterDialog = true
    this.showSwitch = true
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
      return field['deType'] === 0
    })
  }

  optionDatas(datas) {
    if (!datas) return null
    return datas.map(item => {
      return {
        id: item,
        text: item
      }
    })
  }
}
const mySelectImpl = new MySelectImpl()
export default mySelectImpl
