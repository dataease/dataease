import request from '@/config/axios'
import type { IMaintenancePlan, ITableField } from './syncTask'

export type TaskLogType = 'DATA_SYNC' | 'FIELD_CHANGE'

export interface TaskEventLog {
  id: string
  jobId: string
  jobName: string
  taskType: TaskLogType
  executorStartTime: number | null
  executorEndTime: number | null
  status: string
  deletable: boolean
}

export interface TaskFieldChangeLog extends TaskEventLog {
  operationId: string
  operatorName: string | null
  targetTable: string | null
  beforeFields: ITableField[]
  afterFields: ITableField[]
  progress: IMaintenancePlan['steps']
  error: string
  initializationLogId: string
  initializationStatus: string
  initializationError: string
  successorLogId: string
}

export interface TaskEventLogQuery {
  taskId?: string
  keyword?: string
  taskTypes?: TaskLogType[]
  status?: string[]
  lastExecuteTime?: string[]
}

export const getTaskEventLogListApi = (current: number, size: number, data: TaskEventLogQuery) => {
  return request.post<Promise<{ data: { records: TaskEventLog[]; total: number } }>>({
    url: `/sync/task/log/events/pager/${current}/${size}`,
    data
  })
}

export const getTaskFieldChangeLogApi = (logId: string) => {
  return request.get<Promise<{ data: TaskFieldChangeLog }>>({
    url: `/sync/task/log/changes/${logId}`
  })
}

export const removeTaskEventLogApi = (taskType: TaskLogType, logId: string) => {
  return request.post({ url: `/sync/task/log/events/delete/${taskType}/${logId}` })
}

export const getTaskLogListApi = (current: number, size: number, data: any) => {
  return request.post({
    url: `/sync/task/log/pager/${current}/${size}`,
    data: data
  })
}

export const removeApi = (logId: string) => {
  return request.post({ url: `/sync/task/log/delete/${logId}` })
}

export const checkTaskLogConnectionApi = (logId: string) => {
  return request.get({ url: `/sync/task/log/connection/${logId}` })
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

export const clearEventLogs = (data: { jobId: string; clearType: string }) => {
  return request.post<Promise<{ data: TaskLogClearResult }>>({
    url: '/sync/task/log/events/clear',
    data
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
