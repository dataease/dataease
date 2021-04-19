import store from '@/store'
export const commonStyle = {
  rotate: 0,
  opacity: 1
}

export const commonAttr = {
  animations: [],
  events: {},
  groupStyle: {}, // 当一个组件成为 Group 的子组件时使用
  isLock: false // 是否锁定组件
}
export class WidgetService {
  constructor(options) {
    this.options = options
    this.name = options.name
    // this.leftPanelPath = 'application/addLeftWidget'
    // this.dialogPanelPath = 'application/addDialogWidget'
    // this.drawPanelPath = 'application/addDrawWidget'
    this.storeWidget()
  }
  /**
   * 存储数据到本地
   * @param {本地存储路径} path
   * @param {要存储的数据} data
   */
  store(path, data) {
    store.dispatch(path, data)
  }

  getLeftPanel() {
    return this.initLeftPanel()
  }

  getDialogPanel() {
    return this.initFilterDialog()
  }

  getDrawPanel() {
    let drawPanel = this.initDrawPanel()
    drawPanel.serviceName = this.options.name
    drawPanel.style = Object.assign(drawPanel.style, commonStyle)
    drawPanel = Object.assign(drawPanel, commonAttr)
    if (this.filterDialog) {
      const dialogOptions = this.getDialogPanel()
      drawPanel = Object.assign(drawPanel, dialogOptions)
    }
    return drawPanel
  }
  storeWidget() {
    this.store('application/loadBean', { key: this.name, value: this })
  }
}
