import { defineStore } from 'pinia'
import { store } from '../index'

interface RequestState {
  loadingMap: {
    [key: string]: number
  }
}

export const useRequestStore = defineStore('request', {
  state: (): RequestState => {
    return {
      loadingMap: {}
    }
  },
  actions: {
    setLoadingMap(value: object) {
      this.loadingMap = value
    },
    addLoading(key: string) {
      if (Object.prototype.hasOwnProperty.call(this.loadingMap, key)) {
        const map = this.loadingMap
        map[key] += 1
        this.loadingMap = map
      } else {
        const nMap = {}
        nMap[key] = 1
        this.loadingMap = nMap
      }
    },
    reduceLoading(key: string) {
      if (this.loadingMap) {
        const map = this.loadingMap
        map[key] -= 1
        this.loadingMap = map
      }
    }
  }
})

export const useRequestStoreWithOut = () => {
  return useRequestStore(store)
}
