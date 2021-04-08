import { WidgetService } from '../service/WidgetService'
const defaultOptions = {
  name: 'timeMonthWidget',
  icon: 'iconfont icon-yue',
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
  defaultClass: 'time-filter',
  component: 'de-date',
  filterDialog: true
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
  filterFieldMethod(fields) {
    return fields.filter(field => {
      return field['deType'] === 1
    })
  }
}
const timeMonthServiceImpl = new TimeMonthServiceImpl({ name: 'timeMonthWidget' })
export default timeMonthServiceImpl
