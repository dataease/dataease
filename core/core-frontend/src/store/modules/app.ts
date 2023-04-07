import { defineStore } from 'pinia'
import { store } from '../index'

interface AppState {
  size: boolean
  pageLoading: boolean
  title: string
  dekey: string
}

export const useAppStore = defineStore('app', {
  state: (): AppState => {
    return {
      size: true, // 尺寸图标
      pageLoading: false, // 路由跳转loading
      title: 'DataEase',
      dekey: 'DataEaseKey'
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

    setDekey(dekey: string) {
      this.dekey = dekey
    }
  }
})

export const useAppStoreWithOut = () => {
  return useAppStore(store)
}
