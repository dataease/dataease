import request from '@/config/axios'
export interface DatesetOrFolder {
  name: string
  id?: number | string
  pid?: number | string
  nodeType: 'folder' | 'dataset'
  union?: Array<{}>
  allFields?: Array<{}>
}

interface Fields {
  fields: Array<{}>
  data: Array<{}>
}

export interface FieldData {
  allFields: Array<{}>
  data: Fields
}

export interface Dataset {
  id: string
  pid: string
  name: string
  union?: Array<{}>
  allFields?: Array<{}>
}

export interface Table {
  datasourceId: string
  name: string
  tableName: string
  type: string
  unableCheck?: boolean
}
// 获取权限路
export const saveDatasetTree = async (data: DatesetOrFolder): Promise<IResponse> => {
  return request.post({ url: '/datasetTree/save', data }).then(res => {
    return res?.data
  })
}

export const getDatasetTree = async (data = {}): Promise<IResponse> => {
  return request.post({ url: '/datasetTree/tree', data }).then(res => {
    return res?.data
  })
}

export const delDatasetTree = async (id): Promise<IResponse> => {
  return request.post({ url: `/datasetTree/delete/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const getDatasourceList = async (): Promise<IResponse> => {
  return request.post({ url: '/datasource/list', data: {} }).then(res => {
    return res?.data
  })
}

export const getTables = async (id): Promise<IResponse> => {
  return request.post({ url: `/datasource/getTables/${id}`, data: {} }).then(res => {
    return res?.data as Table[]
  })
}

export const getTableField = async (data): Promise<IResponse> => {
  return request.post({ url: '/datasetData/tableField', data }).then(res => {
    return res?.data
  })
}

export const getPreviewData = async (data): Promise<IResponse> => {
  return request.post({ url: '/datasetData/previewData', data }).then(res => {
    return res?.data
  })
}

export const getDatasetPreview = async (id): Promise<FieldData> => {
  return request.post({ url: `/datasetTree/get/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const getDatasetDetails = async (id): Promise<Dataset> => {
  return request.post({ url: `/datasetTree/details/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const tableUpdate = async (data): Promise<IResponse> => {
  return request.post({ url: '/dataset/table/update', data }).then(res => {
    return res?.data
  })
}
