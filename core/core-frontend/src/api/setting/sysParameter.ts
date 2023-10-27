import request from '@/config/axios'

export const queryMapKeyApi = () => request.get({ url: '/sysParameter/queryOnlineMap' })
export const saveMapKeyApi = data => request.post({ url: '/sysParameter/saveOnlineMap', data })
