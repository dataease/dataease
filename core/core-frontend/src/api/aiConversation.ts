import request from '@/config/axios'

export interface AiConversationResource {
  id: string
  name: string
}
export interface AiConversationSession {
  sessionId: string
  contextVersion: string
  scene: string
  resourceId: string
  resourceName: string
  editorId: string
  expiresAt: number
  resourceVersion: string
  draftVersion: string
}
export interface AiConversationAnswer {
  sessionId: string
  executionId: string
  contextVersion: string
  status: 'COMPLETED'
  text: string
}
const data = <T>(promise: Promise<{ data: T }>) => promise.then(result => result.data)
export const getAiAvailability = (signal?: AbortSignal) =>
  data<{ available: boolean; revision?: string | null }>(
    request.get({ url: '/ai/availability', signal, handleErrorLocally: true })
  )
export const getConversationResources = () =>
  data<AiConversationResource[]>(request.get({ url: '/ai/conversations/resources' }))
export const openConversation = (value: {
  scene: 'sql'
  resourceId: string
  editorId: string
  sql: string
  openKey?: string
}) => data<AiConversationSession>(request.post({ url: '/ai/conversations', data: value }))
export const sendConversationTurn = (id: string, contextVersion: string, text: string) =>
  data<AiConversationAnswer>(
    request.post({ url: `/ai/conversations/${id}/turns`, data: { contextVersion, text } })
  )
export const closeConversation = (id: string) =>
  request.delete({ url: `/ai/conversations/${id}`, handleErrorLocally: true })

export interface AiConversationExecution {
  sessionId: string
  executionId: string
  contextVersion: string
  status: 'RUNNING' | 'COMPLETED' | 'CANCELLED' | 'FAILED'
  deadline: number
  context?: AiConversationSession
  taskId?: string
  attemptId?: string
  attemptNumber?: number
  sourceAttemptId?: string
  retryEligible?: boolean
  recovery?: AiRecoveryProgress
}
export interface AiRecoveryProgress {
  stepId: string | null
  stepKind: string | null
  technicalRetries: number
  corrections: number
  reason: string | null
}
export interface AiConversationTool {
  callId: string
  name: string
  status: 'STARTED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED'
  parameters: Record<string, string>
  result?: string
}
export interface AiAssistanceResult {
  type: 'EXPLANATION' | 'SQL_SUGGESTION'
  text: string
  sql: string | null
  resourceId: string
  resourceName: string
  resourceVersion: string
  editorId: string
  draftVersion: string
}
export interface AiConversationEvents {
  sessionId: string
  executionId: string
  contextVersion: string
  status: AiConversationExecution['status']
  cursor: number
  result?: AiAssistanceResult
  taskId?: string
  attemptId?: string
  attemptNumber?: number
  sourceAttemptId?: string
  retryEligible?: boolean
  recovery?: AiRecoveryProgress
  events: {
    sequence: number
    type: string
    text?: string
    error?: string
    tool?: AiConversationTool
  }[]
}
export const startConversationExecution = (
  id: string,
  contextVersion: string,
  text: string,
  submissionKey?: string
) =>
  data<AiConversationExecution>(
    request.post({
      url: `/ai/conversations/${id}/executions`,
      data: { contextVersion, text, submissionKey },
      handleErrorLocally: true
    })
  )
export const invokeSqlAssistance = (value: {
  scene: 'sql'
  resourceId: string
  editorId: string
  sql: string
  text: string
  submissionKey?: string
}) =>
  data<AiConversationExecution>(
    request.post({
      url: '/ai/conversations/capabilities/sql.assist',
      data: value,
      handleErrorLocally: true
    })
  )
export const getConversationEvents = (
  id: string,
  execution: string,
  after: number,
  signal: AbortSignal
) =>
  data<AiConversationEvents>(
    request.get({
      url: `/ai/conversations/${id}/executions/${execution}/events`,
      params: { after },
      signal,
      handleErrorLocally: true
    })
  )
export const stopConversationExecution = (id: string, execution: string) =>
  data<AiConversationEvents>(
    request.post({
      url: `/ai/conversations/${id}/executions/${execution}/stop`,
      handleErrorLocally: true
    })
  )
export const checkSqlApplication = (
  id: string,
  execution: string,
  contextVersion: string,
  draftVersion: string
) =>
  data<AiAssistanceResult>(
    request.post({
      url: `/ai/conversations/${id}/executions/${execution}/check-application`,
      data: { contextVersion, draftVersion },
      handleErrorLocally: true
    })
  )

export const retryConversationExecution = (
  id: string,
  execution: string,
  expectedAttemptId: string,
  retryKey: string
) =>
  data<AiConversationExecution>(
    request.post({
      url: `/ai/conversations/${id}/executions/${execution}/retry`,
      data: { expectedAttemptId, retryKey },
      handleErrorLocally: true
    })
  )
