import request from '@/config/axios'

export const getTaskLogListApi = (current: number, size: number, data: any) => {
  return request.post({
    url: `/sync/task/log/pager/${current}/${size}`,
    data: data
  })
}

export const removeApi = (logId: string) => {
  return request.post({ url: `/sync/task/log/delete/${logId}` })
}

export const precheckTaskLogApi = (logId: string) => {
  return request.get({ url: `/sync/task/log/precheck/${logId}` })
}

export const getTaskLogDetailApi = (logId: string, fromLineNum: number) => {
  return request.get({ url: `/sync/task/log/detail/${logId}/${fromLineNum}` })
}

export const clear = (clearData: {}) => {
  return request.post({ url: `/sync/task/log/clear`, data: clearData })
}

export const terminationTaskApi = (logId: string) => {
  return request.post({ url: `/sync/task/log/terminationTask/${logId}`, data: {} })
}
