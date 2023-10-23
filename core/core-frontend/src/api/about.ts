import request from '@/config/axios'

export const validateApi = data => request.post({ url: '/license/validate', data })
export const buildVersionApi = () => request.get({ url: '/license/version' })
export const updateInfoApi = data => request.post({ url: '/license/update', data })
