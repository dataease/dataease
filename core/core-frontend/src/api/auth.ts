import request from '@/config/axios'

export const queryUserApi = data => request.post({ url: '/user/byCurOrg', data })
export const queryUserOptionsApi = () => request.get({ url: '/user/query' })
export const queryRoleApi = data => request.post({ url: '/role/byCurOrg', data })

export const resourceTreeApi = (flag: string, isSystem?: boolean) => {
  const param = {
    flag,
    isSystem: !!isSystem
  }
  return request.post({ url: '/auth/busiResource', data: param })
}

export const menuTreeApi = () => request.get({ url: '/auth/menuResource' })

export const subjectPermissionApi = data => request.post({ url: '/auth/subjectPermission', data })

export const subjectPermissionSaveApi = data =>
  request.post({ url: '/auth/saveSubjectPermission', data })

export const resourcePermissionApi = data => request.post({ url: '/auth/resourcePermission', data })

export const resourcePermissionSaveApi = data =>
  request.post({ url: '/auth/saveResourcePermission', data })
