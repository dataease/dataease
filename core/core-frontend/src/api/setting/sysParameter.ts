import request from '@/config/axios'

export const queryMapKeyApi = () => request.get({ url: '/sysParameter/queryOnlineMap' })
export const saveMapKeyApi = data => request.post({ url: '/sysParameter/saveOnlineMap', data })
// http://ip:port/de2api/subscription/dvId/resourceId
export const subscription = (dvId, resourceId) =>
  request.post({ url: `/subscription/${dvId}/${resourceId}` })
