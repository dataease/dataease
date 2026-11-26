import request from '@/config/axios'

export interface AiConversationResource {
  id: string
  name: string
}
export type SqlPreviewOutcome =
  | 'SUCCESS'
  | 'UNKNOWN_COLUMN'
  | 'UNKNOWN_TABLE'
  | 'AMBIGUOUS_COLUMN'
  | 'SYNTAX'
  | 'TIMEOUT'
  | 'FAILED'
export interface AiSqlPreview {
  resourceId: string
  isCross: boolean
  outcome: SqlPreviewOutcome
  revision: number
}
export interface AiCalcFieldDraft {
  preserveType?: boolean
  fieldId: string
  dataset: {
    id: string | null
    pid: string
    isCross: boolean
    allFields: { id?: string; params?: unknown; deType?: number }[]
    union: unknown[]
  }
}
export interface AiCalcFieldSuggestion {
  boundExpression?: string | null
  expression: string
  name: string
  groupType: 'd' | 'q'
  deType: number
  validation: 'DETAIL_PASS' | 'AGGREGATE_PLACEHOLDER' | 'INVALID' | 'PREVIEW_FAILED'
  reason?:
    | 'invalid_formula'
    | 'formula_execution_failed'
    | 'dataset_preview_failed'
    | 'preview_unavailable'
    | 'no_preview_rows'
    | ''
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
  scene: 'sql' | 'dataset-sql' | 'dataset-calc-field'
  calcField?: AiCalcFieldDraft
  resourceId: string
  editorId: string
  sql: string
  isCross?: boolean
  systemVariableNames?: string[]
  preview?: AiSqlPreview & {
    currentRevision: number
    currentResourceId?: string
    currentIsCross?: boolean
  }
  openKey?: string
  previousSessionId?: string
  previousExecutionId?: string
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
export const aiRecoveryReasonKey = (reason: string) => {
  switch (reason) {
    case 'connection_failed':
    case 'provider_rate_limit':
    case 'provider_unavailable':
    case 'model_correction':
      return `ai_conversation.recovery_reason_${reason}`
    default:
      return 'ai_conversation.recovery_reason_retry'
  }
}
export interface AiConversationTool {
  stepNumber?: number
  fieldCount?: number
  callId: string
  name: string
  status: 'STARTED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED'
  parameters: Record<string, string>
  result?: string
  resultSequence?: number
  outcome?: string
}
export interface AiSqlTableEvidence {
  table: string
  relations?: {
    name: string
    leftTable: string
    rightTable: string
    cardinality: string
    joinType: string
    actual?: { leftTable: string; joinType: string; rightTable: string }
    conditions: { leftField: string; rightField: string }[]
  }[]
  inspected: boolean
  description?: string | null
  grain?: string | null
  fields: {
    name: string
    type: string
    comment?: string | null
    businessName?: string | null
    description?: string | null
    values: { value: string; meaning: string }[]
  }[]
}
export interface AiAssistanceResult {
  evidence?: AiSqlTableEvidence[] | null
  calcField?: AiCalcFieldSuggestion
  type: 'EXPLANATION' | 'SQL_SUGGESTION' | 'CALC_FIELD_SUGGESTION'
  text: string
  sql: string | null
  resourceId: string
  resourceName: string
  resourceVersion: string
  editorId: string
  draftVersion: string
}
export interface AiAnswerMetering {
  elapsedMillis: number
  inputTokens: number | null
  outputTokens: number | null
  usageStatus: 'COMPLETE' | 'PARTIAL' | 'MISSING'
}
export interface AiConversationEvents {
  metering?: AiAnswerMetering | null
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
export interface AiGenerationBasis {
  kind: 'DRAFT' | 'SUGGESTION' | 'MANUAL_PREVIEW' | 'NONE'
  number?: number
  matchesDraft: boolean
}
export const startConversationExecution = (
  id: string,
  contextVersion: string,
  text: string,
  submissionKey?: string,
  generationBasis?: AiGenerationBasis
) =>
  data<AiConversationExecution>(
    request.post({
      url: `/ai/conversations/${id}/executions`,
      data: { contextVersion, text, submissionKey, generationBasis },
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
export const getConversationToolDetail = (
  id: string,
  execution: string,
  sequence: number,
  signal: AbortSignal
) =>
  data<AiConversationTool>(
    request.get({
      url: `/ai/conversations/${id}/executions/${execution}/tools/${sequence}`,
      signal,
      handleErrorLocally: true
    })
  )
export const bindConversationSubscription = (
  session: AiConversationSession,
  execution: string,
  subscription: string,
  after: number,
  signal: AbortSignal
) =>
  request.post({
    url: `/ai/conversations/${session.sessionId}/executions/${execution}/subscriptions`,
    data: { subscription, editorId: session.editorId, after },
    signal,
    handleErrorLocally: true
  })

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
  draftVersion: string,
  targetResourceId?: string
) =>
  data<AiAssistanceResult>(
    request.post({
      url: `/ai/conversations/${id}/executions/${execution}/check-application`,
      data: { contextVersion, draftVersion, targetResourceId },
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
