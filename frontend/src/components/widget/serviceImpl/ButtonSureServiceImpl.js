import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-chaxunsousuo',
  label: '确定',
  defaultClass: 'time-filter'
}

const drawPanel = {
  type: 'custom',
  style: {
    width: 300,
    height: 47,
    fontSize: 14,
    fontWeight: 500,
    lineHeight: '',
    letterSpacing: 0,
    textAlign: '',
    color: ''
  },
  options: {
    attrs: {
      type: 'primary',
      round: true
    },
    value: '测试按钮'
  },
  component: 'de-button'
}

class ButtonSureServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'buttonSureWidget' })
    super(options)
    this.filterDialog = false
    this.showSwitch = false
  }

  initLeftPanel() {
    const value = JSON.parse(JSON.stringify(leftPanel))
    return value
  }

  initDrawPanel() {
    const value = JSON.parse(JSON.stringify(drawPanel))
    return value
  }
}
const buttonSureServiceImpl = new ButtonSureServiceImpl()
export default buttonSureServiceImpl
