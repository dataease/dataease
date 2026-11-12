import request from '@/config/axios'
import type { AiAssistanceResult } from './aiConversation'

export interface AiTask {
  taskId: string
  attemptId: string
  attemptNumber: number
  status: 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'INTERRUPTED'
  resourceId: string | null
  resourceName: string | null
  modelId?: string | null
  modelRevision?: number | null
  modelIdentifier?: string | null
  errorCode?: string | null
  finishedAt?: number | null
  result?: AiAssistanceResult | null
}
export interface AiTaskOrigin {
  sessionId: string
  contextVersion: string
}
export interface AiTaskNotice {
  taskId: string
  attemptId: string
  attemptNumber: number
  status: AiTask['status']
  errorCode?: string | null
  expiresAt: number
}
export interface AiTaskSubmit {
  submissionKey: string
  capability: 'sql.assist'
  scene: 'sql'
  resourceId: string
  editorId: string
  sql: string
  text: string
  origin?: AiTaskOrigin
}
const data = <T>(promise: Promise<{ data: T }>) => promise.then(result => result.data)
export const submitAiTask = (value: AiTaskSubmit) =>
  data<AiTask>(request.post({ url: '/ai/tasks', data: value, handleErrorLocally: true }))
export const activeAiTasks = () =>
  data<AiTask[]>(request.get({ url: '/ai/tasks', handleErrorLocally: true }))
export const getAiTask = (id: string) =>
  data<AiTask>(request.get({ url: `/ai/tasks/${id}`, handleErrorLocally: true }))
export const cancelAiTask = (id: string) =>
  data<AiTask>(request.post({ url: `/ai/tasks/${id}/cancel`, handleErrorLocally: true }))
export const retryAiTask = (id: string, expectedAttemptId: string, retryKey: string) =>
  data<AiTask>(
    request.post({
      url: `/ai/tasks/${id}/retry`,
      data: { expectedAttemptId, retryKey },
      handleErrorLocally: true
    })
  )

export const pendingAiTaskNotices = () =>
  data<AiTaskNotice[]>(request.get({ url: '/ai/tasks/notices', handleErrorLocally: true }))
export const acknowledgeAiTaskNotice = (attemptId: string) =>
  request.post({ url: `/ai/tasks/notices/${attemptId}/ack`, handleErrorLocally: true })
export const deliverAiTaskConversation = (id: string, origin: AiTaskOrigin) =>
  data<AiTask>(
    request.post({ url: `/ai/tasks/${id}/conversation`, data: origin, handleErrorLocally: true })
  )
