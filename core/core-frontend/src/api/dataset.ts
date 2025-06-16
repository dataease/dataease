import request from '@/config/axios'
import {
  originNameHandle,
  originNameHandleBack,
  originNameHandleBackWithArr
} from '@/utils/CalculateFields'
import { type Field } from '@/api/chart'
import { cloneDeep } from 'lodash-es'
import type { BusiTreeRequest } from '@/models/tree/TreeNode'
import { nameTrim } from '@/utils/utils'
export interface DatasetOrFolder {
  name: string
  action?: string
  isCross?: boolean
  id?: number | string
  pid?: number | string
  nodeType: 'folder' | 'dataset'
  union?: Array<{}>
  allFields?: Array<{}>
}

export interface EnumValue {
  queryId: string
  displayId?: string
  sortId?: string
  sort?: string
  resultMode?: number
  searchText: string
  filter?: Array<{}>
}

interface Fields {
  fields: Array<{}>
  data: Array<{}>
}
export interface ParamsDetail {
  datasetGroupId: string
  type: Array<string | number>
  variableName: string
}

export interface DatasetDetail {
  id: string
  name: string
  componentId: string
  fields: {
    dimensionList: Array<Field>
    quotaList: Array<Field>
    parameterList?: Array<Field>
  }
  activelist?: string
  hasParameter?: boolean
  checkList: string[]
  list: Array<Field>
}

export interface FieldData {
  allFields: Array<{}>
  data: Fields
  total?: number
}

export interface Dataset {
  id: string
  pid: string
  name: string
  isCross?: boolean
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
  nameTrim(data)
  const copyData = cloneDeep(data)
  originNameHandle(copyData.allFields)
  return request.post({ url: '/datasetTree/save', data: copyData }).then(res => {
    if (res?.data?.allFields?.length) {
      originNameHandleBack(res?.data?.allFields)
    }
    return res?.data
  })
}

// create
export const createDatasetTree = async (data: DatasetOrFolder): Promise<IResponse> => {
  nameTrim(data)
  const copyData = cloneDeep(data)
  originNameHandle(copyData.allFields)
  return request.post({ url: '/datasetTree/create', data: copyData }).then(res => {
    if (res?.data?.allFields?.length) {
      originNameHandleBack(res?.data?.allFields)
    }
    return res?.data
  })
}

// rename
export const renameDatasetTree = async (data: DatasetOrFolder): Promise<IResponse> => {
  nameTrim(data)
  return request.post({ url: '/datasetTree/rename', data }).then(res => {
    return res?.data
  })
}

export const enumValueObj = async (data: EnumValue): Promise<Record<string, string>[]> => {
  return request.post({ url: '/datasetData/enumValueObj', data }).then(res => {
    return res?.data
  })
}

export const enumValueDs = async (data: any): Promise<Record<string, string>[]> => {
  return request.post({ url: '/datasetData/enumValueDs', data }).then(res => {
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

export const exportDatasetData = (data = {}) => {
  return request.post({
    url: '/datasetTree/exportDataset',
    method: 'post',
    data: data,
    loading: true,
    responseType: 'blob'
  })
}

export const exportLimit = async (): Promise<boolean> => {
  return request.post({ url: `/exportCenter/exportLimit`, data: {} }).then(res => {
    return res?.data
  })
}

export const perDelete = async (id): Promise<boolean> => {
  return request.post({ url: `/datasetTree/perDelete/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const getDatasourceList = async (weight?: number): Promise<IResponse> => {
  const data = { busiFlag: 'datasource' }
  if (weight) {
    data['weight'] = weight
  }
  return request.post({ url: '/datasource/tree', data }).then(res => {
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
  const copyData = cloneDeep(data)
  originNameHandle(copyData.allFields)
  return request.post({ url: '/datasetData/previewData', data: copyData }).then(res => {
    if (res?.data?.allFields?.length) {
      originNameHandleBack(res?.data?.allFields)
    }

    if (res?.data?.data?.fields?.length) {
      originNameHandleBack(res?.data?.data?.fields)
    }
    return res?.data
  })
}

export const getDatasetPreview = async (id): Promise<FieldData> => {
  return request.post({ url: `/datasetTree/get/${id}`, data: {} }).then(res => {
    return res?.data
  })
}

export const getDatasetTotal = async (id): Promise<FieldData> => {
  return request.post({ url: `/datasetData/getDatasetTotal`, data: { id: id } }).then(res => {
    return res?.data
  })
}

export const getDatasetDetails = async (id): Promise<Dataset> => {
  return request.post({ url: `/datasetTree/details/${id}`, data: {} }).then(res => {
    if (res?.data?.allFields?.length) {
      originNameHandleBack(res?.data?.allFields)
    }
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
export const getDsDetailsWithPerm = async (data): Promise<DatasetDetail[]> => {
  return request.post({ url: '/datasetTree/detailWithPerm', data }).then(res => {
    ;(res?.data || []).forEach(ele => {
      originNameHandleBackWithArr(ele, ['dimensionList', 'quotaList'])
    })
    return res?.data
  })
}
export const getSqlParams = async (data): Promise<ParamsDetail[]> => {
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

export const listFieldByDatasetGroup = (datasetId: number) => {
  return request.post({ url: '/datasetField/listByDatasetGroup/' + datasetId }).then(res => {
    originNameHandleBack(res?.data)
    return res
  })
}

export const multFieldValuesForPermissions = (data = {}) => {
  return request.post({ url: '/datasetField/multFieldValuesForPermissions', data })
}

export const listFieldsWithPermissions = (datasetId: number) => {
  return request.get({ url: '/datasetField/listWithPermissions/' + datasetId }).then(res => {
    originNameHandleBack(res?.data)
    return res
  })
}

export const copilotFields = (datasetId: number) => {
  return request.post({ url: '/datasetField/copilotFields/' + datasetId })
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

export const exportTasksRecords = () =>
  request.post({ url: `/exportCenter/exportTasks/records`, data: {} })

export const exportTasks = (page: number, limit: number, status: string) =>
  request.post({ url: `/exportCenter/exportTasks/${status}/${page}/${limit}`, data: {} })

export const exportRetry = async (id): Promise<IResponse> => {
  return request.post({ url: '/exportCenter/retry/' + id, data: {} }).then(res => {
    return res?.data
  })
}

export const downloadFile = async (id): Promise<Blob> => {
  return request.get({ url: 'exportCenter/download/' + id, responseType: 'blob' }).then(res => {
    return res?.data
  })
}

export const exportDelete = async (id): Promise<IResponse> => {
  return request.get({ url: '/exportCenter/delete/' + id }).then(res => {
    return res?.data
  })
}

export const generateDownloadUri = async (id): Promise<IResponse> => {
  return request.get({ url: '/exportCenter/generateDownloadUri/' + id }).then(res => {
    return res?.data
  })
}

export const exportDeleteAll = async (type, data): Promise<IResponse> => {
  return request.post({ url: '/exportCenter/deleteAll/' + type, data }).then(res => {
    return res?.data
  })
}

export const exportDeletePost = async (data): Promise<IResponse> => {
  return request.post({ url: '/exportCenter/delete', data }).then(res => {
    return res?.data
  })
}

export const listByDsIds = async (data): Promise<IResponse> => {
  return request.post({ url: 'datasetField/listByDsIds', data }).then(res => {
    return res?.data
  })
}

export const getFieldTree = async (data): Promise<IResponse> => {
  return request.post({ url: 'datasetData/getFieldTree', data }).then(res => {
    return res?.data
  })
}

export const copilotChat = async (data): Promise<IResponse> => {
  return request.post({ url: '/copilot/chat', data }).then(res => {
    return res?.data
  })
}

export const getListCopilot = async (): Promise<IResponse> => {
  return request.post({ url: '/copilot/getList' }).then(res => {
    return res?.data
  })
}

export const clearAllCopilot = async (): Promise<IResponse> => {
  return request.post({ url: '/copilot/clearAll' }).then(res => {
    return res?.data
  })
}
