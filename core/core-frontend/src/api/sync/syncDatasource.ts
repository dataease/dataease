import request from '@/config/axios'
import { DataEaseError, errorBody } from '@/config/axios/error'

const hasConnectionDiagnostic = (error: DataEaseError) => {
  const data = errorBody(error.response?.data).data
  return (
    data !== null &&
    typeof data === 'object' &&
    'connectionDiagnostic' in data &&
    typeof data.connectionDiagnostic === 'string' &&
    data.connectionDiagnostic.length > 0
  )
}

export const connectionChangeConfirmation = (error: DataEaseError) => {
  const data = errorBody(error.response?.data).data
  if (
    data !== null &&
    typeof data === 'object' &&
    'confirmationRequired' in data &&
    data.confirmationRequired === true &&
    'affectedTaskCount' in data &&
    typeof data.affectedTaskCount === 'number' &&
    Number.isInteger(data.affectedTaskCount) &&
    data.affectedTaskCount > 0
  ) {
    return data.affectedTaskCount
  }
  return undefined
}

export const sourceDsPageApi = (page: number, limit: number, data) => {
  return request.post({ url: `/sync/datasource/source/pager/${page}/${limit}`, data })
}

export const targetDsPageApi = (page: number, limit: number, data) => {
  return request.post({ url: `/sync/datasource/target/pager/${page}/${limit}`, data })
}
export const latestUseApi = (sourceType: string) => {
  return request.post({ url: `/sync/datasource/latestUse/${sourceType}`, data: {} })
}

export const validateApi = (data, handleErrorLocally = false) => {
  return request.post({
    url: '/sync/datasource/validate',
    data,
    handleErrorLocally: handleErrorLocally && hasConnectionDiagnostic
  })
}

export const getSchemaApi = (data, handleErrorLocally = false) => {
  return request.post({
    url: '/sync/datasource/getSchema',
    data,
    handleErrorLocally: handleErrorLocally && hasConnectionDiagnostic
  })
}

export const saveApi = (data, handleErrorLocally = false) => {
  return request.post({
    url: '/sync/datasource/save',
    data,
    handleErrorLocally: handleErrorLocally && hasConnectionDiagnostic
  })
}

export const getByIdApi = (id: string) => {
  return request.get({ url: `/sync/datasource/get/${id}` })
}

export const updateApi = (data, handleErrorLocally = false) => {
  return request.post({
    url: '/sync/datasource/update',
    data,
    handleErrorLocally:
      handleErrorLocally &&
      (error => hasConnectionDiagnostic(error) || connectionChangeConfirmation(error) !== undefined)
  })
}

export const deleteByIdApi = (id: string) => {
  return request.post({ url: `/sync/datasource/delete/${id}` })
}

export const batchDelApi = (ids: string[]) => {
  return request.post({ url: `/sync/datasource/batchDel`, data: ids })
}

/**
 * 获取源数据库字段集合以及目标数据库数据类型集合
 */
export const getFieldListApi = (data, handleErrorLocally = false) => {
  return request.post({
    url: `/sync/datasource/fields`,
    data,
    handleErrorLocally: handleErrorLocally && hasConnectionDiagnostic
  })
}

export const validateByIdApi = (id: string, handleErrorLocally = false) => {
  return request.get({
    url: `/sync/datasource/validate/${id}`,
    handleErrorLocally: handleErrorLocally && hasConnectionDiagnostic
  })
}

export const loadSyncPlugin = () => {
  return request.get({ url: '/xpackComponent/syncPlugins' })
}
