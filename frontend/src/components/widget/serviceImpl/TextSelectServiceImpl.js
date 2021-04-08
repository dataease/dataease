import { WidgetService } from '../service/WidgetService'
const defaultOptions = {
  name: 'textSelectWidget',
  icon: 'iconfont icon-xialakuang',
  label: '文本下拉',
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
  component: 'de-select',
  filterDialog: true
}

class TextSelectServiceImpl extends WidgetService {
  constructor(options) {
    Object.assign(options, defaultOptions)
    super(options)
  }

  initWidget() {
    // console.log('this is first initWidget')
  }
  toDrawWidget() {
    // console.log('this is first toDrawWidget')
  }
  // 移动到画布之前回掉
  beforeToDraw() {

  }

  setOptionDatas(data) {
    this.options.attrs.datas = data
  }

  filterFieldMethod(fields) {
    return fields.filter(field => {
      return field['deType'] === 0
    })
  }
}
const textSelectServiceImpl = new TextSelectServiceImpl({ name: 'textSelectWidget' })
export default textSelectServiceImpl
