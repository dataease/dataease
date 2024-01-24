import request from '@/config/axios'

export const sourceDsPageApi = (page: number, limit: number, data) => {
  return request.post({ url: `/sync/datasource/source/pager/${page}/${limit}`, data })
}

export const targetDsPageApi = (page: number, limit: number, data) => {
  return request.post({ url: `/sync/datasource/target/pager/${page}/${limit}`, data })
}
export const latestUseApi = (sourceType: string) => {
  return request.post({ url: `/sync/datasource/latestUse/${sourceType}`, data: {} })
}

export const validateApi = data => {
  return request.post({ url: '/sync/datasource/validate', data })
}

export const getSchemaApi = data => {
  return request.post({ url: '/sync/datasource/getSchema', data })
}

export const saveApi = data => {
  return request.post({ url: '/sync/datasource/save', data })
}

export const getByIdApi = (id: string) => {
  return request.get({ url: `/sync/datasource/get/${id}` })
}

export const updateApi = data => {
  return request.post({ url: '/sync/datasource/update', data })
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
export const getFieldListApi = data => {
  return request.post({ url: `/sync/datasource/fields`, data })
}
