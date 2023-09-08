import store from '@/store'
export class ApplicationContext {
  static getService(name) {
    if (!name) {
      return null
    }
    const bean = store.getters.beanMap[name]
    return bean
  }
}
