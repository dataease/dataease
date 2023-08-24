import { WidgetService } from '../service/WidgetService'

const leftPanel = {
  icon: 'iconfont icon-icon_clear_outlined',
  label: 'desresetbutton.label',
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
      type: '',
      round: false,
      plain: true

    },
    value: '清空'
  },
  component: 'de-reset-button',
  miniSizex: 1,
  miniSizey: 1
}

class ButtonResetServiceImpl extends WidgetService {
  constructor(options = {}) {
    Object.assign(options, { name: 'buttonResetWidget' })
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
const buttonResetServiceImpl = new ButtonResetServiceImpl()
export default buttonResetServiceImpl
