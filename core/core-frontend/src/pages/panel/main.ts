import { createApp } from 'vue'
import '@/style.less'
import '@/plugins/svg-icon'
import 'normalize.css/normalize.css'
import App from './App.vue'
import { setupI18n } from '@/plugins/vue-i18n'
import { setupStore } from '@/store'
import { setupRouterPanel } from '@/router'
import { setupElementPlus } from '@/plugins/element-plus'

const setupAll = async (dom: string) => {
  const app = createApp(App)
  await setupI18n(app)
  setupStore(app)
  setupRouterPanel(app)
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
  type: 'dashboard' | 'view'
  deOptions: Options

  create(type, options) {
    this.type = type
    this.token = options.token
    this.baseUrl = options.baseUrl
  }

  initialize(options: Options) {
    this.deOptions = { ...defaultOptions, ...options }
    setupAll(this.deOptions.container)
  }
}

window.DataEaseBi = new DataEaseBi()
