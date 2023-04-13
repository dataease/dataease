import request from '@/config/axios'

// 获取权限路由
export const getDatasourceList = async (): Promise<IResponse> => {
  return request.post({ url: '/f/datasource/list' }).then(res => {
    return res?.data
  })
}
