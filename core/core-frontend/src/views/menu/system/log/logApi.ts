import request from '@/config/axios'

export const optionsApi = () => request.get({ url: '/log/options' })

export const mountedOrg = (keyword?: string) => request.post({ url: '/org/mounted', data: { keyword } })
  
export const allUserApi = (keyword?: string) => request.post({ url: '/user/all', data: { keyword } })

export const queryApi = (i: number, j: number, data) => request.post({ url: `/log/pager/${i}/${j}`, data })

export const exportApi = (data) => request.post({ url: '/log/export', data })

export const queryUserApi = (data) => request.post({ url: '/user/subOrgUser', data })

export const queryAdminOrgApi = () => request.get({ url: '/org/subOrgs' })