import request from '@/config/axios'

export const searchApi = data => request.get({ url: '/org/page/tree', data })
export const saveApi = data => request.post({ url: '/org/page/create', data })
export const updateApi = data => request.post({ url: '/org/page/edit', data })
export const deleteApi = data => request.post({ url: '/org/page/delete', data })
