import request from '@/config/axios'

// 获取权限路由
export const getRoleRouters = async (): Promise<IResponse> => {
  return request.get({ url: '/getRoleRouters' }).then(res => res.data)
}
