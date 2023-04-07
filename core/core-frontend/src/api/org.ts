import request from '@/config/axios'

export const searchApi = (keyword: string) =>
  request.post({ url: '/org/page/tree', data: { keyword } })

export const saveApi = data => request.post({ url: '/org/page/create', data })
export const updateApi = data => request.post({ url: '/org/page/edit', data })
export const resourceExistApi = oid => request.get({ url: '/org/resourceExist/' + oid })
export const deleteApi = oid => request.post({ url: '/org/page/delete/' + oid })
