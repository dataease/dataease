import request from '@/config/axios'

export const queryUserApi = data => request.post({ url: '/user/byCurOrg', data })
export const queryRoleApi = data => request.post({ url: '/role/byCurOrg', data })

export const resourceTreeApi = (flag: string) => request.get({ url: '/auth/busiResource/' + flag })

export const menuTreeApi = () => request.get({ url: '/auth/menuResource' })
