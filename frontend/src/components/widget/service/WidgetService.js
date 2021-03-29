import store from '@/store'
export class WidgetService {
  constructor(options) {
    this.name = options.name
    console.log('init parent class WidgetService')
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
