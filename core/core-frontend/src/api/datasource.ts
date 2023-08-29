import request from '@/config/axios'
import type { Node } from '@/views/visualized/data/datasource/index.vue'
export interface DatasetOrFolder {
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

type IrNode = Omit<Node, 'configuration' | 'apiConfigurationStr'> & {
  configuration: string
  apiConfigurationStr: string
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

export const listDatasources = data => {
  return request
    .post({ url: '/datasource/tree', data: { ...data, ...{ busiFlag: 'datasource' } } })
    .then(res => {
      return res?.data
    })
}

export const listDatasourceType = async (data = {}): Promise<IResponse> => {
  return request.post({ url: '/datasource/types', data }).then(res => {
    return res?.data
  })
}
export const getTableField = (id: number, table: string) =>
  request.get({ url: '/datasource/getTableField/' + id + '/' + table })

export const listDatasourceTables = async (data = {}): Promise<IResponse> => {
  return request.post({ url: '/datasource/getTables', data }).then(res => {
    return res
  })
}

export const getSchema = (data = {}) => {
  return request.post({ url: '/datasource/getSchema', data })
}

export const previewData = (data = {}) => {
  return request.post({ url: '/datasource/previewData', data }).then(res => {
    return res?.data
  })
}
export const validate = (data = {}) => {
  return request.post({ url: '/datasource/validate', data })
}

export const latestUse = async (data = {}) => {
  return request.post({ url: '/datasource/latestUse', data })
}

export const validateById = (id: number) => request.get({ url: '/datasource/validate/' + id })

export const save = async (data = {}): Promise<Dataset> => {
  return request.post({ url: '/datasource/save', data }).then(res => {
    return res?.data
  })
}

export const checkApiItem = async (data = {}): Promise<IResponse> => {
  return request.post({ url: '/datasource/checkApiDatasource', data }).then(res => {
    return res
  })
}

export const getDatasetTree = async (data = {}): Promise<IResponse> => {
  return request
    .post({ url: '/datasetTree/tree', data: { ...data, ...{ busiFlag: 'dataset' } } })
    .then(res => {
      return res?.data
    })
}

export const deleteById = (id: number) => request.get({ url: '/datasource/delete/' + id })

export const getById = (id: number) => request.get({ url: '/datasource/get/' + id })

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

export const uploadFile = async (data): Promise<IResponse> => {
  return request
    .post({
      url: '/datasource/uploadFile',
      data,
      headersType: 'multipart/form-data;'
    })
    .then(res => {
      return res
    })
}
