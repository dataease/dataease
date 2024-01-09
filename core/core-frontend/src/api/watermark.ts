import request from '@/config/axios'

export const searchRoleApi = (keyword: string) =>
  request.post({ url: '/role/query', data: { keyword } })
