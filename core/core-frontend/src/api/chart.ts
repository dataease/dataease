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

export const getFieldByDQ = async (id): Promise<IResponse> => {
  return request.post({ url: `/datasetField/listByDQ/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const getData = async (data): Promise<IResponse> => {
  return request.post({ url: '/chartData/getData', data }).then(res => {
    return res?.data
  })
}
