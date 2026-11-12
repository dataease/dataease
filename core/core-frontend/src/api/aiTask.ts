import request from '@/config/axios'
import type { AiAssistanceResult, AiRecoveryProgress } from './aiConversation'

export interface AiTask {
  taskId: string
  attemptId: string
  attemptNumber: number
  executionId?: string | null
  sourceAttemptId?: string | null
  retryEligible?: boolean
  recovery?: AiRecoveryProgress | null
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
  retryEligible?: boolean
}
export interface AiTaskSubmit {
  submissionKey: string
  capability: 'sql.assist' | 'structure-mutex.assist'
  scene: 'sql' | 'structure-mutex'
  selectedTables?: string[]
  resourceId: string
  editorId: string
  sql: string
  text: string
  origin?: AiTaskOrigin
}
const data = <T>(promise: Promise<{ data: T }>) => promise.then(result => result.data)
export const submitAiTask = (value: AiTaskSubmit) =>
  data<AiTask>(request.post({ url: '/ai/tasks', data: value, handleErrorLocally: true }))
export const aiTaskTables = (resourceId: string, signal?: AbortSignal) =>
  data<string[]>(
    request.get({
      url: `/ai/tasks/resources/${resourceId}/tables`,
      signal,
      handleErrorLocally: true
    })
  )
export const activeAiTasks = (signal?: AbortSignal) =>
  data<AiTask[]>(request.get({ url: '/ai/tasks', signal, handleErrorLocally: true }))
export const getAiTask = (id: string, signal?: AbortSignal) =>
  data<AiTask>(request.get({ url: `/ai/tasks/${id}`, signal, handleErrorLocally: true }))
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

export const pendingAiTaskNotices = (signal?: AbortSignal) =>
  data<AiTaskNotice[]>(request.get({ url: '/ai/tasks/notices', signal, handleErrorLocally: true }))
export const acknowledgeAiTaskNotice = (attemptId: string) =>
  request.post({ url: `/ai/tasks/notices/${attemptId}/ack`, handleErrorLocally: true })
export const deliverAiTaskConversation = (id: string, origin: AiTaskOrigin) =>
  data<AiTask>(
    request.post({ url: `/ai/tasks/${id}/conversation`, data: origin, handleErrorLocally: true })
  )
