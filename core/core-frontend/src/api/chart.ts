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
}

export const getFieldByDQ = async (id, chartId): Promise<IResponse> => {
  return request.post({ url: `/chart/listByDQ/${id}/${chartId}`, data: {} }).then(res => {
    return res?.data
  })
}

// 通过视图对象获取数据
export const getData = async (data): Promise<IResponse> => {
  delete data.data
  return request.post({ url: '/chartData/getData', data }).then(res => {
    return res?.data
  })
}

// 通过视图id获取数据
export const getChart = async (id): Promise<IResponse> => {
  return request.post({ url: `/chart/getChart/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

// 单个视图保存测试
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

export const checkSameDataSet = (viewIdSource, viewIdTarget) =>
  request.get({ url: '/chart/view/checkSameDataSet/' + viewIdSource + '/' + viewIdTarget })

// const getRequestChart = data => {
//   data.xaxis = JSON.stringify(data.xaxis)
//   data.xaxisExt = JSON.stringify(data.xaxisExt)
//   data.yaxis = JSON.stringify(data.yaxis)
//   data.yaxisExt = JSON.stringify(data.yaxisExt)
//   data.extStack = JSON.stringify(data.extStack)
//   data.extBubble = JSON.stringify(data.extBubble)
//
//   data.drillFields = JSON.stringify(data.drillFields)
//   data.viewFields = JSON.stringify(data.viewFields)
//
//   data.customFilter = JSON.stringify(data.customFilter)
//   data.customAttr = JSON.stringify(data.customAttr)
//   data.customStyle = JSON.stringify(data.customStyle)
//
//   data.senior = JSON.stringify(data.senior)
//   return data
// }
//
// const getResponseChart = chart => {
//   chart.xaxis = JSON.parse(chart.xaxis)
//   chart.xaxisExt = JSON.parse(chart.xaxisExt)
//   chart.yaxis = JSON.parse(chart.yaxis)
//   chart.yaxisExt = JSON.parse(chart.yaxisExt)
//   chart.extStack = JSON.parse(chart.extStack)
//   chart.extBubble = JSON.parse(chart.extBubble)
//
//   chart.drillFields = JSON.parse(chart.drillFields)
//   chart.viewFields = JSON.parse(chart.viewFields)
//
//   chart.customFilter = JSON.parse(chart.customFilter)
//   chart.customAttr = JSON.parse(chart.customAttr)
//   chart.customStyle = JSON.parse(chart.customStyle)
//
//   chart.senior = JSON.parse(chart.senior)
//   return chart
// }
