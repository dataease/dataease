import request from '@/config/axios'

export const queryMapKeyApi = () => request.get({ url: '/sysParameter/queryOnlineMap' })
export const queryMapKeyApiByType = (type: string) =>
  request.get({ url: `/sysParameter/queryOnlineMap/${type}` })
export const saveMapKeyApi = data => request.post({ url: '/sysParameter/saveOnlineMap', data })
