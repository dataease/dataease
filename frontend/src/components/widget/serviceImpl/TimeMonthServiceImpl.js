import { WidgetService } from '../service/WidgetService'
const defaultOptions = {
  name: 'timeMonthWidget',
  icon: null,
  label: '年月',
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
      type: 'month',
      placeholder: '请选择年月'
    },
    value: ''
  },
  component: 'de-date'
}

class TimeMonthServiceImpl extends WidgetService {
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
const timeMonthServiceImpl = new TimeMonthServiceImpl({ name: 'timeMonthWidget' })
export default timeMonthServiceImpl
