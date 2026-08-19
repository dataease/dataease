import request from '@/config/axios'


export const reportGridApi = (current: number, size: number, data) => {
  return request.post({ url: `/report/pager/${current}/${size}`, data })
}

export const reportCreateApi = data => {
  return request.post({ url: '/report/create', data })
}

export const reportUpdateApi = data => {
  return request.post({ url: '/report/update', data })
}

export const reportFireApi = taskId => {
  return request.post({ url: `/report/fireNow/${taskId}` })
}

export const reportStopApi = taskId => {
  return request.post({ url: `/report/stop/${taskId}` })
}

export const reportStartApi = taskId => {
  return request.post({ url: `/report/start/${taskId}` })
}

export const reportDelApi = taskIdList => {
  return request.post({ url: '/report/delete', data: taskIdList })
}

export const reportInfoApi = taskId => {
  return request.get({ url: `/report/info/${taskId}` })
}

export const queryUserApi = () => {
  return request.get({ url: `/user/org/option` })
}

export const instanceGridApi = (current: number, size: number, data) => {
  return request.post({ url: `/report/logPager/${current}/${size}`, data })
}

export const instanceDelApi = data => {
  return request.post({ url: '/report/deleteLog', data })
}

export const logMsgApi = data => {
  return request.post({ url: '/report/logMsg', data })
}

export const isOrgAdminApi = () => {
  return request.get({ url: `/user/orgAdmin` })
}

export const viewOptionApi = (resourceId: string) => {
  return request.get({ url: `/chart/viewOption/${resourceId}` })
}

export const queryCategoryStatusApi = () => {
  const url = `/setting/authentication/status`
  return request.get({ url })
}

export const larkGroupOptionApi = () => {
  const url = '/lark/getGroup'
  return request.get({ url })
}

export const larksuiteGroupOptionApi = () => {
  const url = '/larksuite/getGroup'
  return request.get({ url })
}

export const dingtalkGroupOptionApi = () => {
  const url = '/dingtalk/getGroup'
  return request.get({ url })
}