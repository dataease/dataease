import { WidgetService } from '../service/WidgetService'
const defaultOptions = {
  name: 'timeDateRangeWidget',
  icon: null,
  label: '日期范围',
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
      type: 'daterange',
      rangeSeparator: '至',
      startPlaceholder: '开始日期',
      endPlaceholder: '结束日期'
    },
    value: ''
  },
  component: 'de-date'
}

class TimeDateRangeServiceImpl extends WidgetService {
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
const timeDateRangeServiceImpl = new TimeDateRangeServiceImpl({ name: 'timeDateRangeWidget' })
export default timeDateRangeServiceImpl
