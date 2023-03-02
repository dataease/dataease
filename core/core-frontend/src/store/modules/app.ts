import { defineStore } from 'pinia'
import { store } from '../index'

interface AppState {
  size: boolean
}

export const useAppStore = defineStore('app', {
  state: (): AppState => {
    return {
      size: true // 尺寸图标
    }
  },
  getters: {
    getSize(): boolean {
      return this.size
    }
  },
  actions: {
    setSize(size: boolean) {
      this.size = size
    }
  }
})

export const useAppStoreWithOut = () => {
  return useAppStore(store)
}
