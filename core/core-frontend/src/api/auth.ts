import request from '@/config/axios'

export const queryUserApi = (isSystem?: boolean) =>
  isSystem ? request.get({ url: '/user/query' }) : request.get({ url: '/user/byCurOrg' })

export const queryRoleApi = () => request.get({ url: '/role/query' })

export const resourceTreeApi = (flag: string, system?: boolean) => {
  const param = {
    flag,
    system: !!system
  }
  return request.post({ url: '/auth/busiResource', data: param })
}

export const subjectPermissionApi = data => request.post({ url: '/auth/subjectPermission', data })

export const subjectPermissionSaveApi = data =>
  request.post({ url: '/auth/saveSubjectPermission', data })

export const resourcePermissionApi = data => request.post({ url: '/auth/resourcePermission', data })

export const resourcePermissionSaveApi = data =>
  request.post({ url: '/auth/saveResourcePermission', data })

export const subjectTreeApi = (data: {
  system: boolean
  type: number
  lazy?: boolean
  pid?: number
}) => request.post({ url: '/auth/subjectTree', data })
