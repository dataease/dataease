import { defineStore } from 'pinia'
import { store } from '../index'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
import { modelApi } from '@/api/login'
import { xpackModelApi } from '@/api/plugin'
interface AppState {
  size: boolean
  pageLoading: boolean
  title: string
  dekey: string
  desktop: boolean
  isDataEaseBi: boolean
  isIframe: boolean
  embeddedTab: boolean
  arrowSide: boolean
  xpackValid: boolean
  xpackValidLoaded: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => {
    return {
      size: true, // 尺寸图标
      pageLoading: false, // 路由跳转loading
      title: '',
      dekey: 'DataEaseKey',
      isDataEaseBi: false,
      isIframe: false,
      embeddedTab: false,
      desktop: false,
      arrowSide: false,
      xpackValid: false,
      xpackValidLoaded: false
    }
  },
  getters: {
    getSize(): boolean {
      return this.size
    },
    getArrowSide(): boolean {
      return this.arrowSide
    },
    getPageLoading(): boolean {
      return this.pageLoading
    },
    getTitle(): string {
      return this.title
    },
    getIsDataEaseBi(): boolean {
      return this.isDataEaseBi
    },
    getIsIframe(): boolean {
      return this.isIframe
    },
    getEmbeddedTab(): boolean {
      return this.embeddedTab
    },
    getDekey(): string {
      return this.dekey
    },
    getDesktop(): string {
      return this.desktop
    },
    getXpackValid(): boolean {
      return this.xpackValid
    }
  },
  actions: {
    async setAppModel() {
      const res = await modelApi()
      const data = res.data
      this.desktop = data
      wsCache.set('app.desktop', this.desktop)
    },
    async setXpackValid() {
      if (this.xpackValidLoaded) {
        return
      }
      this.xpackValidLoaded = true
      const res = await xpackModelApi()
      this.xpackValid = res.data || false
    },
    async refreshXpackValid() {
      this.xpackValidLoaded = false
      await this.setXpackValid()
    },
    setSize(size: boolean) {
      this.size = size
    },
    setArrowSide(ArrowSide: boolean) {
      this.arrowSide = ArrowSide
    },
    setIsDataEaseBi(isDataEaseBi: boolean) {
      this.isDataEaseBi = isDataEaseBi
    },
    setIsIframe(isIframe: boolean) {
      this.isIframe = isIframe
    },
    setEmbeddedTab(embeddedTab: boolean) {
      this.embeddedTab = embeddedTab
    },
    setPageLoading(pageLoading: boolean) {
      this.pageLoading = pageLoading
    },
    setTitle(title: string) {
      this.title = title
      document.title = title
    },

    setDekey(dekey: string) {
      this.dekey = dekey
    },
    setDesktop(desktop: boolean) {
      wsCache.set('app.desktop', desktop)
      this.desktop = desktop
    }
  }
})

export const useAppStoreWithOut = () => {
  return useAppStore(store)
}
