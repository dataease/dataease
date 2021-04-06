import { WidgetService } from '../service/WidgetService'
const defaultOptions = {
  name: 'timeYearWidget',
  icon: 'iconfont icon-nian',
  label: '年份',
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
      type: 'year',
      placeholder: '请选择年份'
    },
    value: ''
  },
  defaultClass: 'time-filter',
  component: 'de-date',
  filterDialog: true
}

class TimeYearServiceImpl extends WidgetService {
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
const timeYearServiceImpl = new TimeYearServiceImpl({ name: 'timeYearWidget' })
export default timeYearServiceImpl
