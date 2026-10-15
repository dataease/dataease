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

export const getTaskLogDetailApi = (logId: string, fromLineNum: number) => {
  return request.get({ url: `/sync/task/log/detail/${logId}/${fromLineNum}` })
}

export interface TaskLogClearResult {
  id: string
  status: 'QUEUED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'UNKNOWN'
  total: number
  message: string
  createdTime: number
  updatedTime: number
}

export const clear = (clearData: { jobId: string; clearType: string }) => {
  return request.post<Promise<{ data: TaskLogClearResult }>>({
    url: '/sync/task/log/clear',
    data: clearData
  })
}

export const getClearResult = () => {
  return request.get<Promise<{ data: TaskLogClearResult | null }>>({
    url: '/sync/task/log/clear/result',
    handleErrorLocally: true
  })
}

export const terminationTaskApi = (logId: string) => {
  return request.post({ url: `/sync/task/log/terminationTask/${logId}`, data: {} })
}
