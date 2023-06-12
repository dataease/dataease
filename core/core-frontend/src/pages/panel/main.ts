import { createApp } from 'vue'
import '@/style/index.less'
import '@/plugins/svg-icon'
import 'normalize.css/normalize.css'
import App from './App.vue'
import { setupI18n } from '@/plugins/vue-i18n'
import { setupStore } from '@/store'
import { setupElementPlus } from '@/plugins/element-plus'
import { useUserStoreWithOut } from '@/store/modules/user'

const setupAll = async (dom: string, componentName: string) => {
  const app = createApp(App, { componentName })
  await setupI18n(app)
  setupStore(app)
  setupElementPlus(app)
  app.mount(dom)
}

interface Options {
  container: string
  lng: 'zh' | 'en'
  theme: 'default'
}

const defaultOptions = {
  container: '#designer',
  lng: 'zh',
  theme: 'default'
}

class DataEaseBi {
  baseUrl: string
  token: string
  type: 'DashboardEditor' | 'View'
  dvId: string
  pid: string
  deOptions: Options

  create(type, options) {
    this.type = type
    this.token = options.token
    this.baseUrl = options.baseUrl
    this.dvId = options.dvId
    this.pid = options.pid
  }

  initialize(options: Options) {
    this.deOptions = { ...defaultOptions, ...options }
    setupAll(this.deOptions.container, this.type)
    const appStore = useUserStoreWithOut()
    appStore.setToken(this.token)
  }

  destory() {
    window.DataEaseBi = null
  }
}

window.DataEaseBi = new DataEaseBi()
