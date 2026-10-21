import { defineStore } from 'pinia'
import { store } from '../index'

export interface CachedRequest {
  resolve: (token: string) => void
  reject: (reason: unknown) => void
}

interface RequestState {
  loadingMap: {
    [key: string]: number
  }
  cachedRequestList: CachedRequest[]
}

export const useRequestStore = defineStore('request', {
  state: (): RequestState => {
    return {
      loadingMap: {},
      cachedRequestList: []
    }
  },
  getters: {
    getRequestList(): CachedRequest[] {
      return this.cachedRequestList
    }
  },
  actions: {
    setLoadingMap(value: object) {
      this.loadingMap = value
    },
    resetLoadingMap() {
      for (const key in this.loadingMap) {
        this.loadingMap[key] = 0
      }
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
    },
    addCacheRequest(fun: CachedRequest) {
      this.cachedRequestList.push(fun)
    },
    cleanCacheRequest() {
      this.cachedRequestList = []
    }
  }
})

export const useRequestStoreWithOut = () => {
  return useRequestStore(store)
}
