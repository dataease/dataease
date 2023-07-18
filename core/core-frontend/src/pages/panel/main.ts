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
import { App, createApp } from 'vue'
import '@/style/index.less'
import '@/plugins/svg-icon'
import 'normalize.css/normalize.css'
import AppElement from './App.vue'
import { setupI18n } from '@/plugins/vue-i18n'
import { setupStore } from '@/store'
import { useUserStoreWithOut } from '@/store/modules/user'
import { setupElementPlus } from '@/plugins/element-plus'

const setupAll = async (dom: string, componentName: string): Promise<App<Element>> => {
  const app = createApp(AppElement, { componentName })
  await setupI18n(app)
  setupStore(app)
  setupElementPlus(app)
  const userStore = useUserStoreWithOut()
  await userStore.setUser()
  app.mount(dom)
  return app
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
  type: 'DashboardEditor' | 'VisualizationEditor' | 'ViewWrapper' | 'Dashboard'
  dvId: string
  resourceId: string
  pid: string
  chartId: string
  deOptions: Options
  vm: App<Element>

  create(type, options) {
    this.type = type
    this.token = options.token
    this.baseUrl = options.baseUrl
    this.dvId = options.dvId
    this.pid = options.pid
    this.chartId = options.chartId
    this.resourceId = options.resourceId
  }

  async initialize(options: Options) {
    this.deOptions = { ...defaultOptions, ...options }
    this.vm = await setupAll(this.deOptions.container, this.type)
  }

  destroy() {
    const userStore = useUserStoreWithOut()
    userStore.clear()
    this.vm.unmount()
    this.vm = null
  }
}

window.DataEaseBi = new DataEaseBi()
