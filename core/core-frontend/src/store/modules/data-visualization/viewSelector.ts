import { defineStore } from 'pinia'
import { store } from '@/store/index'

interface ViewSelectorState {
  enable: boolean
  viewIdList: string[]
}

export const viewSelectorStore = defineStore('viewSelectorStore', {
  state: (): ViewSelectorState => {
    return {
      enable: false,
      viewIdList: []
    }
  },
  getters: {
    getEnable(): boolean {
      return this.enable
    },
    getViewIdList(): string[] {
      return this.viewIdList
    }
  },
  actions: {
    setEnable(enable: boolean) {
      this.enable = enable
    },
    remove(id?: string) {
      let len = this.viewIdList.length
      if (!len) {
        return
      }
      if (!id) {
        this.viewIdList = []
        return
      }
      while (len--) {
        if (this.viewIdList[len] === id) {
          this.viewIdList.splice(len, 1)
        }
      }
    },
    add(id: string) {
      if (!this.viewIdList.includes(id)) {
        this.viewIdList.push(id)
      }
    },
    clear() {
      this.enable = false
      this.viewIdList = []
    }
  }
})

export const useViewSelectorStoreWithOut = () => {
  return viewSelectorStore(store)
}
