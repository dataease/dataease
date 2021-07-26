
import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-xialakuang',
  label: 'denumbergridselect.label',
  defaultClass: 'tree-filter'
}

const dialogPanel = {
  options: {
    attrs: {
      multiple: false,
      placeholder: 'denumbergridselect.placeholder',
      viewIds: [],
      datas: [],
      key: 'id',
      label: 'text',
      value: 'id'
    },
    value: ''
  },
  defaultClass: 'tree-filter',
  component: 'de-select-grid'
}
const drawPanel = {
  type: 'custom',
  style: {
    width: 300,
    height: 300,
    fontSize: 14,
    fontWeight: 500,
    lineHeight: '',
    letterSpacing: 0,
    textAlign: '',
    color: ''
  },
  component: 'de-select-grid'
}

class NumberSelectGridServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'numberSelectGridWidget' })
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
const numberSelectGridServiceImpl = new NumberSelectGridServiceImpl()
export default numberSelectGridServiceImpl
