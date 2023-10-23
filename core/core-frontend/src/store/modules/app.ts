import { defineStore } from 'pinia'
import { store } from '../index'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
import { modelApi } from '@/api/login'
interface AppState {
  size: boolean
  pageLoading: boolean
  title: string
  dekey: string
  desktop: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => {
    return {
      size: true, // 尺寸图标
      pageLoading: false, // 路由跳转loading
      title: 'DataEase',
      dekey: 'DataEaseKey',
      desktop: false
    }
  },
  getters: {
    getSize(): boolean {
      return this.size
    },
    getPageLoading(): boolean {
      return this.pageLoading
    },
    getTitle(): string {
      return this.title
    },

    getDekey(): string {
      return this.dekey
    },
    getDesktop(): string {
      return this.desktop
    }
  },
  actions: {
    async setAppModel() {
      const res = await modelApi()
      const data = res.data
      this.desktop = data
      wsCache.set('app.desktop', this.desktop)
    },
    setSize(size: boolean) {
      this.size = size
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
