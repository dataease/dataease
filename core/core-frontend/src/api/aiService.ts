import request from '@/config/axios'

export interface AiTaskScheduling {
  maxRunning: number
  maxQueued: number
  maxQueuedPerUser: number
  maxWaitSeconds: number
}
export interface AiRecoverySettings {
  stepRetries: number
  attemptRetries: number
  initialDelayMillis: number
}
export interface AiRetentionSettings {
  auditDays: number
  deliveryDays: number
}
export interface AiSettings {
  retention?: AiRetentionSettings
  taskScheduling?: AiTaskScheduling
  recovery?: AiRecoverySettings
  enabled: boolean
  defaultModelId: string | null
  allowSamples: boolean
  requestTimeoutSeconds: number
  maxInputChars: number
  maxOutputTokens: number
  interactiveConcurrency: number
  backgroundConcurrency: number
  revision: number
  rowVersion: number
  persistedRevision: number
  appliedRevision: number
  runtimeState: string
  organizationPolicyAvailable: boolean
}
export interface AiModel {
  id: string
  name: string
  provider: string
  baseUrl: string
  modelIdentifier: string
  keyConfigured: boolean
  enabled: boolean
  validated: boolean
  configRevision: number
  rowVersion: number
  processingLocation: string
  processingNote: string | null
  outputTokenParameter: 'MAX_TOKENS' | 'MAX_COMPLETION_TOKENS'
  lastTestStatus: string
  lastTestTime: number | null
  lastTestErrorCode: string | null
  organizationReferences: number
  historicalOrganizationReferences: number
  organizationNames: string[]
  systemDefault: boolean
}
export interface AiCandidate {
  id: string
  expectedVersion?: number
  expectedConfigRevision?: number
  draftHandle?: string
  name: string
  provider: string
  baseUrl: string
  apiKey: string
  modelIdentifier: string
  processingLocation: string
  processingNote: string
  testProof?: string
}
export interface AiOrg {
  id: string
  name: string
  parentName: string
  mode: 'INHERIT' | 'SPECIFIED' | 'DISABLED'
  modelId: string | null
  rowVersion: number | null
  effectiveModel: string | null
  unavailableReason: string | null
}
export interface AiImpact {
  token: string
  systemDefault: boolean
  organizationCount: number
  organizations: string[]
}
const data = <T>(promise: Promise<{ data: T }>) => promise.then(result => result.data)
export const getAiSettings = () => data<AiSettings>(request.get({ url: '/ai/settings' }))
export const saveAiSettings = (value: AiSettings, impactToken?: string) =>
  data<AiSettings>(
    request.post({
      url: '/ai/settings',
      data: { ...value, expectedVersion: value.rowVersion, impactToken }
    })
  )
export const getAiModels = (search = '', page = 1, size = 10) =>
  data<{ items: AiModel[]; total: number }>(
    request.get({ url: '/ai/models', params: { search, page, size } })
  )
export const getAiModel = (id: string) => data<AiModel>(request.get({ url: `/ai/models/${id}` }))
export const createAiDraft = () =>
  data<{ id: string; draftHandle: string }>(request.post({ url: '/ai/models/drafts' }))
export const discoverAiModels = (candidate: AiCandidate, signal: AbortSignal) =>
  data<string[]>(request.post({ url: '/ai/models/discover', data: candidate, signal }))
export const testAiCandidate = (candidate: AiCandidate, signal: AbortSignal) =>
  data<{ basic: boolean; structured: boolean; errorCode: string | null; testProof: string | null }>(
    request.post({ url: '/ai/models/test', data: candidate, signal })
  )
export const testSavedAiModel = (id: string) =>
  data<{ basic: boolean; structured: boolean; errorCode: string | null }>(
    request.post({ url: `/ai/models/${id}/test` })
  )
export const saveAiModel = (candidate: AiCandidate, editing: boolean) =>
  data<AiModel>(
    editing
      ? request.post({ url: `/ai/models/${candidate.id}`, data: candidate })
      : request.post({ url: '/ai/models', data: candidate })
  )
export const getAiImpact = (id: string, action: 'DISABLE' | 'DELETE') =>
  data<AiImpact>(request.get({ url: `/ai/models/${id}/impact`, params: { action } }))
export const getAiSettingsImpact = (value: AiSettings) =>
  data<AiImpact>(
    request.get({
      url: '/ai/settings/impact',
      params: { defaultModelId: value.defaultModelId, enabled: value.enabled }
    })
  )
export const setAiModelEnabled = (model: AiModel, enabled: boolean, impactToken?: string) =>
  request.post({
    url: `/ai/models/${model.id}/enabled`,
    data: { expectedVersion: model.rowVersion, enabled, impactToken }
  })
export const deleteAiModel = (model: AiModel) =>
  request.delete({ url: `/ai/models/${model.id}`, data: { expectedVersion: model.rowVersion } })
export const getAiOrgs = (search = '') =>
  data<{ items: AiOrg[]; total: number; available: boolean }>(
    request.get({ url: '/ai/org-policies', params: { search } })
  )
export const saveAiOrgPolicies = (organizations: AiOrg[], mode: string, modelId: string | null) =>
  request.post({
    url: '/ai/org-policies/batch',
    data: {
      organizations: organizations.map(org => ({ id: org.id, expectedVersion: org.rowVersion })),
      mode,
      modelId: mode === 'SPECIFIED' ? modelId : null
    }
  })

const safeFailureCodes = new Set([
  'max_tokens_unsupported',
  'provider_authentication',
  'provider_balance',
  'provider_rate_limit',
  'provider_unavailable',
  'outcome_unknown',
  'provider_rejected',
  'forbidden',
  'conflict',
  'invalid',
  'key_unavailable',
  'proof_invalid',
  'configuration_unavailable',
  'model_unavailable',
  'address_invalid',
  'dns_failed',
  'connection_failed',
  'tls_failed',
  'policy_blocked',
  'concurrency',
  'task_queue_full',
  'task_user_queue_full',
  'queue_timeout',
  'timeout',
  'cancelled',
  'upstream_failed',
  'response_invalid',
  'unsupported',
  'output_limit',
  'input_limit',
  'needs_test',
  'referenced',
  'historical_referenced',
  'disabled',
  'organization_disabled',
  'samples_denied',
  'scope_changed',
  'transaction_active'
])
export const aiFailureKey = (code: string | null) =>
  code && safeFailureCodes.has(code) ? `failure_${code}` : 'test_failed'
