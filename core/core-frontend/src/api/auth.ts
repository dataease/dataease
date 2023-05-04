import request from '@/config/axios'

export const queryUserApi = data => request.post({ url: '/user/byCurOrg', data })
export const queryRoleApi = data => request.post({ url: '/role/byCurOrg', data })

export const resourceTreeApi = (flag: string) => request.get({ url: '/auth/busiResource/' + flag })

export const menuTreeApi = () => request.get({ url: '/auth/menuResource' })

export const resourcePerApi = data => request.post({ url: '/auth/busiPermission', data })

export const menuPerApi = data => request.post({ url: '/auth/menuPermission', data })

export const busiPerSaveApi = data => request.post({ url: '/auth/saveBusiPer', data })
export const menuPerSaveApi = data => request.post({ url: '/auth/saveMenuPer', data })

export const resourceTargetPerApi = data =>
  request.post({ url: '/auth/busiTargetPermission', data })

export const menuTargetPerApi = data => request.post({ url: '/auth/menuTargetPermission', data })
