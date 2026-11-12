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
  result?: AiAssistanceResult | null
}
export interface AiTaskSubmit {
  submissionKey: string
  capability: 'sql.assist'
  scene: 'sql'
  resourceId: string
  editorId: string
  sql: string
  text: string
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
