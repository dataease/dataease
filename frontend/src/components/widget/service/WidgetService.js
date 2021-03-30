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
    this.name = options.name
    options = { ...commonAttr, ...options }
    Object.assign(this, options)
    this.style = { ...commonStyle, ...options.style }
    this.storeWidget()
  }
  storeWidget() {
    store.dispatch('application/loadBean', { key: this.name, value: this })
  }
  initWidget() {
    console.log('this is initWidget')
  }
  toDrawWidget() {
    console.log('this is toDrawWidget')
  }
}
