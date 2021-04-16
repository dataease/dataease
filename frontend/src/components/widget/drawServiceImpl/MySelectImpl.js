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
    width: 200,
    height: 22,
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
  }

  initLeftPanel(uuid) {
    uuid = uuid || this.uuid()
    this.setLeftPanel(uuid, leftPanel)
    return uuid
    // console.log('this is first initWidget')
  }

  initFilterDialog(uuid) {
    uuid = uuid || this.uuid()
    this.setDialogPanel(uuid, dialogPanel)
    return uuid
  }

  initDrawPanel(uuid) {
    uuid = uuid || this.uuid()
    this.setDrawPanel(uuid, drawPanel)
    return uuid
  }

  toDrawWidget() {
    // console.log('this is first toDrawWidget')
  }
  // 移动到画布之前回掉
  beforeToDraw() {

  }

  setOptionDatas(uuid, data) {
    const dialogPanel = this.getDialogPanel(uuid)
    dialogPanel.options.attrs.datas = data
    this.setDialogPanel(uuid, dialogPanel)
  }

  filterFieldMethod(fields) {
    return fields.filter(field => {
      return field['deType'] === 0
    })
  }
}
const mySelectImpl = new MySelectImpl()
export default mySelectImpl
