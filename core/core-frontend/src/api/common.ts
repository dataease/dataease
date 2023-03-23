import request from '@/config/axios'

// 获取权限路由
export const getRoleRouters = async (): Promise<IResponse> => {
  return request.get({ url: '/menu/query' }).then(res => {
    return res?.data
  })
}
