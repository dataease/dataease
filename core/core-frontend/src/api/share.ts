import request from '@/config/axios'

export interface ShareSubInfo {
  exist: boolean
  uuid: string
  expired: boolean
  pwdRequired: boolean
}

// 公共链接场景：查询 tab 内嵌子资源是否已开启公共链接
export function getSubShareInfo(resourceId: string): Promise<ShareSubInfo> {
  return request
    .get({
      url: '/share/subShareInfo/' + resourceId
    })
    .then(res => res?.data as ShareSubInfo)
}
