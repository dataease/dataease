import request from '@/config/axios'

export const validateApi = data => request.post({ url: '/user/byCurOrg', data })
export const buildVersionApi = data => request.post({ url: '/role/byCurOrg', data })
export const updateInfoApi = data => request.post({ url: '/role/byCurOrg', data })
