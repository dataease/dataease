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
}
export interface AiConversationAnswer {
  sessionId: string
  executionId: string
  contextVersion: string
  status: 'COMPLETED'
  text: string
}
const data = <T>(promise: Promise<{ data: T }>) => promise.then(result => result.data)
export const getConversationResources = () =>
  data<AiConversationResource[]>(request.get({ url: '/ai/conversations/resources' }))
export const openConversation = (value: {
  scene: 'sql'
  resourceId: string
  editorId: string
  sql: string
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
}
export interface AiConversationEvents {
  sessionId: string
  executionId: string
  contextVersion: string
  status: AiConversationExecution['status']
  cursor: number
  events: { sequence: number; type: string; text?: string; error?: string }[]
}
export const startConversationExecution = (id: string, contextVersion: string, text: string) =>
  data<AiConversationExecution>(
    request.post({
      url: `/ai/conversations/${id}/executions`,
      data: { contextVersion, text },
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
