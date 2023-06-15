const suffix = `${import.meta.env.VITE_VERSION}-dataease`

const dom = document.querySelector('head')
const cb = dom.appendChild.bind(dom)

const formatterUrl = <T extends Node>(node: T, prefix: string) => {
  if (['SCRIPT', 'LINK'].includes(node.nodeName)) {
    let url = ''
    if (node instanceof HTMLLinkElement) {
      url = node.href
    } else if (node instanceof HTMLScriptElement) {
      url = node.src
    }
    if (url.includes(suffix)) {
      const currentUrlprefix = new URL(url).origin
      const newUrl = url.replace(currentUrlprefix, prefix)
      if (node instanceof HTMLLinkElement) {
        node.href = newUrl
      } else if (node instanceof HTMLScriptElement) {
        node.src = newUrl
      }
    }
  }
  return node
}

const getPrefix = (): string => {
  let prefix = ''
  Array.from(document.querySelector('head').children).some(ele => {
    if (['SCRIPT', 'LINK'].includes(ele.nodeName)) {
      let url = ''
      if (ele instanceof HTMLLinkElement) {
        url = ele.href
      } else if (ele instanceof HTMLScriptElement) {
        url = ele.src
      }
      if (url.includes(suffix)) {
        prefix = new URL(url).origin
        return true
      }
    }
  })
  return prefix
}

document.querySelector('head').appendChild = <T extends Node>(node: T) => {
  const newNode = formatterUrl(node, getPrefix())
  cb(newNode)
  return newNode
}
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

  destroy() {
    window.DataEaseBi = null
  }
}

window.DataEaseBi = new DataEaseBi()
