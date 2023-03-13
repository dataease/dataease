import { defineStore } from 'pinia'
import { store } from '../index'

interface AppState {
  size: boolean
  pageLoading: boolean
  token: string
  title: string
}

export const useAppStore = defineStore('app', {
  state: (): AppState => {
    return {
      size: true, // 尺寸图标
      pageLoading: false, // 路由跳转loading
      token: 'Authorization',
      title: 'DataEase'
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
    getToken(): string {
      return this.token
    }
  },
  actions: {
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
    setToken(token: string) {
      this.token = token
    }
  }
})

export const useAppStoreWithOut = () => {
  return useAppStore(store)
}
