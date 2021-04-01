import { WidgetService } from '../service/WidgetService'
const defaultOptions = {
  name: 'timeQuarterWidget',
  icon: 'iconfont icon-jidu',
  label: '季度',
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

      placeholder: '请选择季度'
    },
    value: ''
  },
  defaultClass: 'time-filter',
  component: 'de-quarter'
}

class TimeQuarterServiceImpl extends WidgetService {
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
}
const timeQuarterServiceImpl = new TimeQuarterServiceImpl({ name: 'timeQuarterWidget' })
export default timeQuarterServiceImpl
