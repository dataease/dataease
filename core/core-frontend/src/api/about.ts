import request from '@/config/axios'

export const validateApi = data => request.post({ url: '/license/validate', data })
export const buildVersionApi = () => request.get({ url: '/license/version' })
export const updateInfoApi = data => request.post({ url: '/license/update', data })

export const checkFreeApi = () => request.get({ url: '/rmonitor/existFree' })
export const syncFreeApi = () => request.post({ url: '/rmonitor/sync' })
export const delFreeApi = () => request.post({ url: '/rmonitor/delete' })
