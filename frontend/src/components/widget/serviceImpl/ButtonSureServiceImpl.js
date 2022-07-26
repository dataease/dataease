import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-chaxunsousuo',
  label: 'desearchbutton.label',
  defaultClass: 'time-filter'
}

const drawPanel = {
  type: 'custom-button',
  style: {
    width: 150,
    height: 60,
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
      round: false,
      plain: true,
      customRange: false,
      filterIds: [],
      autoTrigger: true
    },
    value: '查询'
  },
  component: 'de-button',
  miniSizex: 1,
  miniSizey: 1
}

class ButtonSureServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'buttonSureWidget' })
    super(options)
    this.filterDialog = false
    this.buttonDialog = true
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
