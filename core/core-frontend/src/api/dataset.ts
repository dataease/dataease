import request from '@/config/axios'
import { type Field } from '@/api/chart'
import type { BusiTreeRequest } from '@/models/tree/TreeNode'
export interface DatasetOrFolder {
  name: string
  action?: string
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

export interface DatasetDetail {
  id: string
  name: string
  componentId: string
  fields: {
    dimensionList: Array<Field>
    quotaList: Array<Field>
  }
  checkList: string[]
  list: Array<Field>
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
// edit
export const saveDatasetTree = async (data: DatasetOrFolder): Promise<IResponse> => {
  return request.post({ url: '/datasetTree/save', data }).then(res => {
    return res?.data
  })
}

// create
export const createDatasetTree = async (data: DatasetOrFolder): Promise<IResponse> => {
  return request.post({ url: '/datasetTree/create', data }).then(res => {
    return res?.data
  })
}

// rename
export const renameDatasetTree = async (data: DatasetOrFolder): Promise<IResponse> => {
  return request.post({ url: '/datasetTree/rename', data }).then(res => {
    return res?.data
  })
}

export const moveDatasetTree = async (data: DatasetOrFolder): Promise<IResponse> => {
  return request.post({ url: '/datasetTree/move', data }).then(res => {
    return res?.data
  })
}

export const getDatasetTree = async (data: BusiTreeRequest): Promise<IResponse> => {
  data.busiFlag = 'dataset'
  return request.post({ url: '/datasetTree/tree', data }).then(res => {
    return res?.data
  })
}

export const barInfoApi = async (id): Promise<IResponse> => {
  return request.get({ url: `/datasetTree/barInfo/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const delDatasetTree = async (id): Promise<IResponse> => {
  return request.post({ url: `/datasetTree/delete/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const getDatasourceList = async (): Promise<IResponse> => {
  return request.post({ url: '/datasource/tree', data: { busiFlag: 'datasource' } }).then(res => {
    return res?.data
  })
}

export const getTables = async (data): Promise<Table[]> => {
  return request.post({ url: `/datasource/getTables`, data }).then(res => {
    return res?.data
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

export const getPreviewSql = async (data): Promise<IResponse> => {
  return request.post({ url: '/datasetData/previewSql', data }).then(res => {
    return res?.data
  })
}

export const getDsDetails = async (data): Promise<DatasetDetail[]> => {
  return request.post({ url: '/datasetTree/dsDetails', data }).then(res => {
    return res?.data
  })
}
export const getSqlParams = async (data): Promise<DatasetDetail[]> => {
  return request.post({ url: '/datasetTree/getSqlParams', data }).then(res => {
    return res?.data
  })
}
export const rowPermissionList = (page: number, limit: number, datasetId: number) =>
  request.get({ url: '/dataset/rowPermissions/pager/' + datasetId + '/' + page + '/' + limit })

export const columnPermissionList = (page: number, limit: number, datasetId: number) =>
  request.get({ url: '/dataset/columnPermissions/pager/' + datasetId + '/' + page + '/' + limit })

export const rowPermissionTargetObjList = (datasetId: number, type: string) =>
  request.get({ url: '/dataset/rowPermissions/authObjs/' + datasetId + '/' + type })

export const listFieldByDatasetGroup = (datasetId: number) =>
  request.post({ url: '/datasetField/listByDatasetGroup/' + datasetId })

export const multFieldValuesForPermissions = (data = {}) => {
  return request.post({ url: '/datasetField/multFieldValuesForPermissions', data })
}

export const saveRowPermission = (data = {}) => {
  return request.post({ url: '/dataset/rowPermissions/save', data })
}

export const saveColumnPermission = (data = {}) => {
  return request.post({ url: '/dataset/columnPermissions/save', data })
}

export const deleteRowPermission = (data = {}) => {
  return request.post({ url: '/dataset/rowPermissions/delete', data })
}

export const deleteColumnPermission = (data = {}) => {
  return request.post({ url: '/dataset/columnPermissions/delete', data })
}

export const whiteListUsersForPermissions = (data = {}) => {
  return request.post({ url: '/dataset/rowPermissions/whiteListUsers', data })
}

export const saveField = async (data): Promise<DatasetDetail[]> => {
  return request.post({ url: '/datasetField/save', data }).then(res => {
    return res?.data
  })
}

export const deleteField = async (id): Promise<DatasetDetail[]> => {
  return request.post({ url: `/datasetField/delete/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const deleteFieldByChartId = async (id): Promise<DatasetDetail[]> => {
  return request.post({ url: `/datasetField/deleteByChartId/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const getEnumValue = async (data): Promise<DatasetDetail[]> => {
  return request.post({ url: '/datasetData/enumValue', data }).then(res => {
    return res?.data
  })
}

export const getFunction = async (): Promise<DatasetDetail[]> => {
  return request.post({ url: '/datasetField/getFunction', data: {} }).then(res => {
    return res?.data
  })
}
