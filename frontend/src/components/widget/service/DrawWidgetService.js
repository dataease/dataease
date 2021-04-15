import store from '@/store'
import { uuid } from 'vue-uuid'
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
export class DrawWidgetService {
  constructor(options) {
    this.options = options
    this.name = options.name
    this.leftPanelPath = 'application/addLeftWidget'
    this.dialogPanelPath = 'application/addDialogWidget'
    this.drawPanelPath = 'application/addDrawWidget'
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
  uuid() {
    return uuid.v1()
  }
  setLeftPanel(uuid, leftPanel) {
    this.store(this.leftPanelPath, { uuid: uuid, leftPanel: leftPanel })
  }
  getLeftPanel(uuid) {
    if (!store.getters.leftWidgetMap[uuid]) {
      this.initLeftPanel && this.initLeftPanel(uuid)
    }
    return store.getters.leftWidgetMap[uuid]
  }

  setDialogPanel(uuid, dialogPanel) {
    this.store(this.dialogPanelPath, { uuid: uuid, dialogPanel: dialogPanel })
  }
  getDialogPanel(uuid) {
    if (!store.getters.dialogWidgetMap[uuid]) {
      this.initFilterDialog && this.initFilterDialog(uuid)
    }
    return store.getters.dialogWidgetMap[uuid]
  }

  setDrawPanel(uuid, drawPanel) {
    if (!store.getters.drawWidgetMap[uuid]) { // 第一次
      drawPanel.style = Object.assign(drawPanel.style, commonStyle)
      drawPanel = Object.assign(drawPanel, commonAttr)
      if (this.initFilterDialog) { // 需要弹窗
        const dialogOptions = this.getDialogPanel(uuid)
        drawPanel = Object.assign(drawPanel, dialogOptions)
      }
    }
    this.store(this.drawPanelPath, { uuid: uuid, drawPanel: drawPanel })
  }

  getDrawPanel(uuid) {
    if (!store.getters.drawWidgetMap[uuid]) {
      this.initDrawPanel && this.initDrawPanel(uuid)
    }
    return store.getters.drawWidgetMap[uuid]
  }

  storeWidget() {
    // store.dispatch('application/loadBean', { key: this.name, value: this })
    this.store('application/loadBean', { key: this.name, value: this })
  }
  initWidget() {
    console.log('this is initWidget')
  }
  toDrawWidget() {
    console.log('this is toDrawWidget')
  }
}
