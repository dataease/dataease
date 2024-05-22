import { defineStore } from 'pinia'
import { store } from '../index'
interface AppState {
  type: string
  token: string
  busiFlag: string
  outerParams: string
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
      outerParams: '',
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
    getOuterParams(): string {
      return this.outerParams
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
    },
    getIframeData(): any {
      return {
        embeddedToken: this.token,
        busiFlag: this.busiFlag,
        outerParams: this.outerParams,
        type: this.type,
        dvId: this.dvId,
        chartId: this.chartId,
        pid: this.pid,
        resourceId: this.resourceId
      }
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
    setOuterParams(outerParams: string) {
      this.outerParams = outerParams
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
    },
    setIframeData(data: any) {
      this.type = data['type']
      this.token = data['embeddedToken']
      this.busiFlag = data['busiFlag']
      this.outerParams = data['outerParams']
      this.dvId = data['dvId']
      this.chartId = data['chartId']
      this.pid = data['pid']
      this.resourceId = data['resourceId']
    }
  }
})

export const useEmbedded = () => {
  return userStore(store)
}
