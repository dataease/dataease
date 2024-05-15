import request from '@/config/axios'

export const variableCreateApi = data => request.post({ url: '/sysVariable/create', data })

export const variableEditApi = data => request.post({ url: '/sysVariable/edit', data })

export const variableDetailApi = id => request.get({ url: '/sysVariable/detail/' + id })

export const variableDeletelApi = id => request.get({ url: '/sysVariable/delete/' + id })

export const searchVariableApi = async data => request.post({ url: '/sysVariable/query', data })

export const valueSelectedForVariableApi = (page: number, limit: number, data) =>
  request.post({ url: `/sysVariable/value/selected/${page}/${limit}`, data })

export const valueForVariable = id => request.get({ url: '/sysVariable/value/selected/' + id })

export const variableValueCreateApi = data =>
  request.post({ url: '/sysVariable/value/create', data })

export const variableValueDeletelApi = id => request.get({ url: '/sysVariable/value/delete/' + id })

export const variableValueEditApi = data => request.post({ url: '/sysVariable/value/edit', data })

export const batchDelApi = data => request.post({ url: '/sysVariable/value/batchDel', data })
