import { WidgetService } from '../service/WidgetService'
const defaultOptions = {
  name: 'textInputWidget',
  icon: 'iconfont icon-shuru',
  label: '文本搜索',
  style: {
    width: 100,
    height: 34,
    borderWidth: '',
    borderColor: '',
    borderRadius: '',
    fontSize: 14,
    fontWeight: 500,
    lineHeight: '',
    letterSpacing: 0,
    textAlign: '',
    color: '',
    backgroundColor: ''
  },
  defaultClass: 'text-filter',
  component: 'de-input-search',
  options: {
    refId: '1234567890',
    attrs: {
      placeholder: '请输入关键字'
    },
    value: ''
  }
}

class TextInputServiceImpl extends WidgetService {
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
  dynamicStype() {
    return {
      'background-color': 'rgba(35,190,239,.1)'
    }
  }
//   dynamicIconStype() {
//     return {
//       color: '#23beef'
//     }
//   }
}
const textInputServiceImpl = new TextInputServiceImpl({ name: 'textInputWidget' })
export default textInputServiceImpl
