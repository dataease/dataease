import { defineStore } from 'pinia'
import { store } from '../index'
interface AppState {
  type: string
  token: string
  busiFlag: string
  baseUrl: string
  dvId: string
  pid: string
  chartId: string
  resourceId: string
}

export const userStore = defineStore('embedded', {
  state: (): AppState => {
    return {
      type: '',
      token: '',
      busiFlag: '',
      baseUrl: '',
      dvId: '',
      pid: '',
      chartId: '',
      resourceId: ''
    }
  },
  getters: {
    getType(): string {
      return this.type
    },
    getToken(): string {
      return this.token
    },
    getBusiFlag(): string {
      return this.busiFlag
    },
    getBaseUrl(): string {
      return this.baseUrl
    },
    getDvId(): string {
      return this.dvId
    },
    getPid(): string {
      return this.pid
    },
    getChartId(): string {
      return this.chartId
    },
    getResourceId(): string {
      return this.resourceId
    }
  },
  actions: {
    setType(type: string) {
      this.type = type
    },
    setToken(token: string) {
      this.token = token
    },
    setBusiFlag(busiFlag: string) {
      this.busiFlag = busiFlag
    },
    setBaseUrl(baseUrl: string) {
      this.baseUrl = baseUrl
    },
    setDvId(dvId: string) {
      this.dvId = dvId
    },
    setPid(pid: string) {
      this.pid = pid
    },
    setChartId(chartId: string) {
      this.chartId = chartId
    },
    setResourceId(resourceId: string) {
      this.resourceId = resourceId
    }
  }
})

export const useEmbedded = () => {
  return userStore(store)
}
