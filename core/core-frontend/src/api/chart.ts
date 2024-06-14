import request from '@/config/axios'

export interface Field {
  id: number | string
  datasourceId: number | string
  datasetTableId: number | string
  datasetGroupId: number | string
  originName: string
  name: string
  dataeaseName: string
  groupType: string
  type: string
  deType: number
  deExtractType: number
  extField: number
  checked: boolean
  fieldShortName: string
  desensitized: boolean
}

export interface ComponentInfo {
  id: string
  name: string
  deType: number
  type: string
  datasetId: string
}

export const getFieldByDQ = async (id, chartId, data): Promise<IResponse> => {
  return request.post({ url: `/chart/listByDQ/${id}/${chartId}`, data: data }).then(res => {
    return res?.data
  })
}

export const copyChartField = async (id, chartId): Promise<IResponse> => {
  return request.post({ url: `/chart/copyField/${id}/${chartId}`, data: {} }).then(res => {
    return res?.data
  })
}

export const deleteChartField = async (id): Promise<IResponse> => {
  return request.post({ url: `/chart/deleteField/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const deleteChartFieldByChartId = async (chartId): Promise<IResponse> => {
  return request.post({ url: `/chart/deleteFieldByChart/${chartId}`, data: {} }).then(res => {
    return res?.data
  })
}

// 通过图表对象获取数据
export const getData = async (data): Promise<IResponse> => {
  delete data.data
  return request.post({ url: '/chartData/getData', data }).then(res => {
    if (res.code === 0) {
      return res?.data
    } else {
      return res
    }
  })
}

export const innerExportDetails = async (data): Promise<IResponse> => {
  return request.post({
    url: '/chartData/innerExportDetails',
    method: 'post',
    data: data,
    loading: true,
    responseType: 'blob'
  })
}

// 通过图表id获取数据
export const getChart = async (id): Promise<IResponse> => {
  return request.post({ url: `/chart/getChart/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

// 单个图表保存测试
export const saveChart = async (data): Promise<IResponse> => {
  delete data.data
  return request.post({ url: '/chart/save', data }).then(res => {
    return res?.data
  })
}

// 获取单个字段枚举值
export const getFieldData = async (fieldId, fieldType, data): Promise<IResponse> => {
  delete data.data
  return request
    .post({ url: `/chartData/getFieldData/${fieldId}/${fieldType}`, data })
    .then(res => {
      return res
    })
}

export const getChartDetail = async (id: string): Promise<IResponse> => {
  return request.post({ url: `chart/getDetail/${id}`, data: {} }).then(res => {
    return res
  })
}

export const checkSameDataSet = async (viewIdSource, viewIdTarget) =>
  request.get({ url: '/chart/checkSameDataSet/' + viewIdSource + '/' + viewIdTarget })
