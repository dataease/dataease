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
  opt: string
  createType: string
  templateParams: string
  jumpInfoParam: string
  outerUrl: string
  datasourceId: string
  tableName: string
  datasetId: string
  datasetCopyId: string
  datasetPid: string
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
      resourceId: '',
      opt: '',
      createType: '',
      templateParams: '',
      outerUrl: '',
      jumpInfoParam: '',
      datasourceId: '',
      tableName: '',
      datasetId: '',
      datasetCopyId: '',
      datasetPid: ''
    }
  },
  getters: {
    getType(): string {
      return this.type
    },
    getJumpInfoParam(): string {
      return this.jumpInfoParam
    },
    getOuterUrl(): string {
      return this.outerUrl
    },
    getCreateType(): string {
      return this.createType
    },
    getTemplateParams(): string {
      return this.templateParams
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
    getOpt(): string {
      return this.opt
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
    setdatasetPid(datasetPid: string) {
      this.datasetPid = datasetPid
    },
    setDatasourceId(datasourceId: string) {
      this.datasourceId = datasourceId
    },
    setTableName(tableName: string) {
      this.tableName = tableName
    },
    setDatasetId(datasetId: string) {
      this.datasetId = datasetId
    },
    setDatasetCopyId(datasetCopyId: string) {
      this.datasetCopyId = datasetCopyId
    },
    setOuterUrl(outerUrl: string) {
      this.outerUrl = outerUrl
    },
    setJumpInfoParam(jumpInfoParam: string) {
      this.jumpInfoParam = jumpInfoParam
    },
    setCreateType(createType: string) {
      this.createType = createType
    },
    setTemplateParams(templateParams: string) {
      this.templateParams = templateParams
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
    setOpt(opt: string) {
      this.opt = opt
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
    },
    clearState() {
      this.setPid('')
      this.setOpt('')
      this.setCreateType('')
      this.setTemplateParams('')
      this.setResourceId('')
      this.setDvId('')
      this.setJumpInfoParam('')
      this.setOuterUrl('')
      this.setDatasourceId('')
      this.setTableName('')
      this.setDatasetId('')
      this.setDatasetCopyId('')
      this.setdatasetPid('')
    }
  }
})

export const useEmbedded = () => {
  return userStore(store)
}
