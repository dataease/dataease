import request from '@/config/axios'

export const mountedOrg = (keyword: string) =>
  request.post({ url: '/org/mounted', data: { keyword } })

export const switchOrg = (id: number | string) => request.post({ url: '/user/switch/' + id })

export const userInfo = () => request.get({ url: '/user/info' })

export const searchRoleApi = (keyword: string) =>
  request.post({ url: '/role/query', data: { keyword } })

export const userOptionForRoleApi = data => request.post({ url: '/user/role/option', data })

export const userSelectedForRoleApi = data => request.post({ url: '/user/role/selected', data })

export const userPageApi = (page: number, limit: number, data) =>
  request.post({ url: '/user/pager/' + page + '/' + limit, data })

export const userCreateApi = data => request.post({ url: '/user/create', data })

export const userEditApi = data => request.post({ url: '/user/edit', data })

export const roleOptionForUserApi = data => request.post({ url: '/role/user/option', data })

export const userDelApi = uid => request.post({ url: '/user/delete/' + uid })

export const queryFormApi = uid => request.get({ url: '/user/queryById/' + uid })

export const roleCreateApi = data => request.post({ url: '/role/create', data })

export const roleEditApi = data => request.post({ url: '/role/edit', data })

export const roleDetailApi = rid => request.get({ url: '/role/detail/' + rid })

export const roleDelApi = rid => request.post({ url: '/role/delete/' + rid })
