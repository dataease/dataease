<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { uuid } from 'vue-uuid'
import AssistantProcess from './AssistantProcess.vue'
import AssistantAnswer from './AssistantAnswer.vue'
import AssistantFailure from './AssistantFailure.vue'
import type { AnswerFailure } from './assistantFailure'
import { applyProcessEvent, type AnswerProcess } from './assistantProcess'
import useClipboard from 'vue-clipboard3'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { isRequestError } from '@/config/axios/error'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import { followConversation } from '@/api/aiConversationDelivery'
import { aiFailureKey } from '@/api/aiService'
import {
  checkSqlApplication,
  closeConversation,
  getAiAvailability,
  getConversationEvents,
  openConversation,
  retryConversationExecution,
  startConversationExecution,
  stopConversationExecution,
  type AiAssistanceResult,
  type AiAnswerMetering,
  type AiCalcFieldDraft,
  type AiCalcFieldSuggestion,
  type AiConversationEvents,
  type AiSqlPreview,
  type AiConversationSession
} from '@/api/aiConversation'

const props = defineProps<{
  host: HTMLElement
  isCross: boolean
  systemVariableNames: string[]
  resourceId: string
  resourceLabel: string
  draftRevision: number
  preview?: AiSqlPreview & { sql: string; sourceLabel: string }
  calcFieldDraft?: () => AiCalcFieldDraft
  readDraft: () => string
}>()
const emit = defineEmits<{
  (event: 'apply', query: string): void
  (event: 'apply-formula', formula: AiCalcFieldSuggestion): void
  (event: 'invalidate-preview'): void
}>()
const { t } = useI18n()
const tr = (key: string) => t(`ai_conversation.${key}`)
const canApplyCalcField = (validation: AiCalcFieldSuggestion['validation']) =>
  validation === 'DETAIL_PASS' || validation === 'AGGREGATE_PLACEHOLDER'
const validationLabel = (validation: AiCalcFieldSuggestion['validation']) => {
  if (validation === 'AGGREGATE_PLACEHOLDER') return tr('aggregate_placeholder')
  return tr(validation === 'DETAIL_PASS' ? 'detail_pass' : 'calc_failed')
}
const elapsedLabel = (ms: number) =>
  tr('elapsed_time') + ' ' + (ms < 1000 ? ms + ' ms' : ms / 1000 + ' s')
const title = () => tr(props.calcFieldDraft ? 'calc_title' : 'editor_title')
const newDraftLabel = () => tr(props.calcFieldDraft ? 'new_formula' : 'new_query')
const suggestionText = (result?: AiAssistanceResult) => result?.calcField?.expression ?? result?.sql
const failedFormula = (result?: AiAssistanceResult) =>
  result?.calcField?.validation === 'INVALID' || result?.calcField?.validation === 'PREVIEW_FAILED'
const { toClipboard } = useClipboard()
const user = useUserStoreWithOut()
let editorId = uuid.v4()
interface Turn {
  questionId: string
  basis: string
  baseSql: string
  calcScope?: string
  draftRevision: number
  question: string
  answer: string
  choices?: string[]
  selectedChoice?: string
  process: AnswerProcess
  metering?: AiAnswerMetering
  session?: AiConversationSession
  cursor?: number
  deadline?: number
  recovering?: boolean
  executionId?: string
  attemptId?: string
  retryEligible?: boolean
  admit?: (read: AbortController) => Promise<void>
  stopRequested?: boolean
  retryKey?: string
  isRetry?: boolean
  issue?: AnswerFailure
  recovery?: AiConversationEvents['recovery']
  result?: AiAssistanceResult
  status: string
}
const available = ref(false)
const expanded = ref(false)
const input = ref('')
const composing = ref(false)
const failure = ref('')
const boundaryIssue = ref<AnswerFailure>()
const turns = ref<Turn[]>([])
const questionGroups = computed(() => {
  const groups = new Map<string, { turn: Turn; index: number }[]>()
  turns.value.forEach((turn, index) => {
    const group = groups.get(turn.questionId)
    if (group) group.push({ turn, index })
    else groups.set(turn.questionId, [{ turn, index }])
  })
  return [...groups.values()]
})
// Positive numbers identify displayed turns.
const selectedBasis = ref<number | 'preview'>()
const includeBasis = ref(true)
const showFullBasis = ref(false)
const busy = ref(false)
const applying = ref(false)
const conversation = ref<HTMLElement>()

if (props.calcFieldDraft) {
  watch(
    [expanded, () => turns.value.length, () => turns.value.at(-1)?.cursor, busy],
    async ([open, count], [wasOpen, previousCount]) => {
      if (!open) return
      const log = conversation.value
      // Follow incoming output only while the author is reading the latest turn.
      if (
        wasOpen &&
        count === previousCount &&
        log &&
        log.scrollHeight - log.clientHeight - log.scrollTop > 4
      )
        return
      await nextTick()
      if (conversation.value) conversation.value.scrollTop = conversation.value.scrollHeight
    }
  )
}

let latestSession: AiConversationSession | undefined
const pendingRetry = ref<Turn>()
let latestRetryExecution: string | undefined
let request: AbortController | undefined
let availabilityRead: AbortController | undefined
let availabilityTimer: number | undefined
let expiryTimer: number | undefined
let version = 0
let disposed = false

const identityScope = () =>
  JSON.stringify([user.uid, user.oid, user.proxyInfo?.proxy, user.proxyInfo?.proxyOid])
const sessions = new Map<string, ReturnType<typeof identityScope>>()
const uncertainResponse = (error: unknown) => {
  if (!isRequestError(error)) return false
  if (!error.response) {
    const code = 'transportCode' in error ? error.transportCode : error.code
    return ['ERR_NETWORK', 'ECONNABORTED', 'ETIMEDOUT', 'ECONNRESET'].includes(String(code))
  }
  if ('messageKey' in error && error.messageKey)
    return error.messageKey === 'i18n_ai_execution_route_unavailable'
  return [502, 503, 504].includes(error.response.status)
}
async function replayOperation<T>(operation: () => Promise<T>, current: () => boolean): Promise<T> {
  for (let attempt = 0; ; attempt++) {
    try {
      return await operation()
    } catch (error) {
      if (attempt >= 2 || !current() || !uncertainResponse(error)) throw error
      await new Promise(resolve => window.setTimeout(resolve, attempt === 0 ? 200 : 800))
      if (!current()) throw error
    }
  }
}
const close = (id: string, owner = identityScope()) => {
  const sameOwner = () => owner === identityScope()
  if (sameOwner())
    void replayOperation(() => closeConversation(id), sameOwner).catch(() => undefined)
}
const clearHistory = () => {
  window.clearTimeout(expiryTimer)
  sessions.forEach((owner, id) => close(id, owner))
  sessions.clear()
  boundaryIssue.value = undefined
  pendingRetry.value = undefined
  latestSession = undefined
  latestRetryExecution = undefined
  selectedBasis.value = undefined
  includeBasis.value = true
  showFullBasis.value = false
  turns.value = []
}
const newConversation = () => {
  version++
  // A delayed open for the old editorId must never retire the new conversation on the server.
  editorId = uuid.v4()
  request?.abort()
  request = undefined
  clearHistory()
  busy.value = applying.value = false
  input.value = failure.value = ''
}
const reset = () => {
  newConversation()
  expanded.value = false
  emit('invalidate-preview')
}
const refresh = async () => {
  if (disposed || document.hidden || availabilityRead) return
  window.clearTimeout(availabilityTimer)
  const read = new AbortController()
  availabilityRead = read
  const timer = window.setTimeout(() => read.abort(), 5000)
  try {
    const status = await getAiAvailability(read.signal)
    if (availabilityRead !== read) return
    if (!status.available && available.value) reset()
    available.value = status.available
  } catch (error) {
    if (
      availabilityRead === read &&
      isRequestError(error) &&
      [401, 403].includes(error.response?.status)
    ) {
      reset()
      available.value = false
    }
  } finally {
    window.clearTimeout(timer)
    if (availabilityRead === read) {
      availabilityRead = undefined
      availabilityTimer = window.setTimeout(refresh, 30000)
    }
  }
}
const cancelAvailability = () => {
  window.clearTimeout(availabilityTimer)
  availabilityRead?.abort()
  availabilityRead = undefined
}
watch(
  () => [user.uid, user.oid, user.proxyInfo?.proxy, user.proxyInfo?.proxyOid],
  () => {
    cancelAvailability()
    reset()
    available.value = false
    void refresh()
  },
  { flush: 'sync' }
)
onMounted(() => {
  void refresh()
  window.addEventListener('focus', refresh)
  window.addEventListener('online', resumeLast)
  document.addEventListener('visibilitychange', refresh)
})
onBeforeUnmount(() => {
  disposed = true
  cancelAvailability()
  window.removeEventListener('focus', refresh)
  window.removeEventListener('online', resumeLast)
  document.removeEventListener('visibilitychange', refresh)
  reset()
})

const accessLost = (error: unknown) =>
  isRequestError(error) &&
  ([401, 403, 404].includes(error.response?.status) ||
    ('messageKey' in error && error.messageKey === 'i18n_ai_scope_changed'))
const publicFailure = (error: unknown, fallback: string) => {
  if (!isRequestError(error)) return { reason: fallback, requestId: undefined }
  const body = error.response?.data
  const code = 'messageKey' in error ? error.messageKey?.replace(/^i18n_ai_/, '') : undefined
  const key = aiFailureKey(code || null)
  let reason = key === 'test_failed' ? fallback : t('ai_service.' + key)
  if (
    'category' in error &&
    error.category &&
    body &&
    typeof body === 'object' &&
    'msg' in body &&
    typeof body.msg === 'string' &&
    body.msg === error.message
  )
    reason = body.msg
  const id = 'requestId' in error ? error.requestId : undefined
  return { reason, requestId: id && /^[a-f0-9]{32}$/i.test(id) ? id : undefined }
}
const fail = (error: unknown, turn?: Turn) => {
  if (accessLost(error) || (error instanceof Error && error.message === 'scope_changed')) {
    emit('invalidate-preview')
    clearHistory()
  }
  let message = tr('failed')
  if (isRequestError(error) && error.response?.status === 403)
    message = t('ai_service.failure_forbidden')
  if (
    isRequestError(error) &&
    'messageKey' in error &&
    error.messageKey === 'i18n_ai_calc_sql_manual'
  )
    message = tr('calc_sql_manual')
  const diagnostic = publicFailure(error, message)
  if (turn) {
    const issue: AnswerFailure = {
      step: turn.executionId ? 'progress_step' : 'submission_step',
      ...diagnostic
    }
    if (turns.value.includes(turn)) turn.issue = issue
    else boundaryIssue.value = issue
  } else failure.value = diagnostic.reason
  void refresh()
}
// Displayed turn numbers also work as references in a free-text follow-up.
const referencedTurn = (question: string, latest: number) => {
  // Answer ordinals address evidence. Only proposal ordinals choose a generation basis.
  question = question.replace(
    /(?:第\s*[0-9一二三四五六七八九十兩两]+|上一?)\s*[轮輪版条條次个個]?\s*(?:回答|答复|答覆|回复|回覆)/g,
    ''
  )
  const digits = question.match(
    /(?:建议|建議|suggestion|proposal)\s*#?\s*(\d+)|第\s*(\d+)\s*[轮輪版条條]/i
  )
  if (digits) return Number(digits[1] ?? digits[2])
  const ordinal = question.match(/第([一二三四五六七八九十]+)[轮輪版条條]/)?.[1]
  if (ordinal) {
    const numbers = '零一二三四五六七八九'
    const [tens, ones] = ordinal.split('十')
    return ones === undefined
      ? numbers.indexOf(tens)
      : (tens ? numbers.indexOf(tens) : 1) * 10 + numbers.indexOf(ones)
  }
  if (/\bfirst (?:proposal|suggestion|version)\b/i.test(question)) return 1
  if (/上一[轮輪版条條]|(?:previous|last) (?:suggestion|proposal)/i.test(question)) return latest
}
const selectBasis = (basis: number | 'preview') => {
  selectedBasis.value = basis
  includeBasis.value = true
}
const chooseBasis = (question: string) => {
  const revision = props.draftRevision
  if (
    /(?:^|[，,。.!！?？;；]\s*)(?:(?:请|請|帮我|幫我|麻烦|麻煩|这次|這次)\s*)*(?:另写|另寫|从头写|從頭寫|重新写一|重新寫一)|(?:^|[,.!?]\s*)(?:please\s+)?(?:from scratch|start (?:over|afresh|from scratch)|(?:write|rewrite|generate|create)\b[^.!?]*?\bfrom scratch)/i.test(
      question
    )
  ) {
    return { label: newDraftLabel(), sql: '' }
  }
  const latest = turns.value.reduce(
    (last, turn, i) => (suggestionText(turn.result) ? i + 1 : last),
    0
  )
  const requested = selectedBasis.value ?? referencedTurn(question, latest)
  const preview = props.preview
  const asksAboutPreview =
    /预览|預覽|报错|報錯|出错|出錯|错误|錯誤|失败|失敗|(?:为什么|為什麼|为何|為何|怎么|怎麼).*(?:重复|重複)|刚才.*运行|剛才.*執行|\b(preview|error|failed|failure|duplicate[sd]?)\b/i.test(
      question
    )
  if (
    preview &&
    props.resourceId &&
    (requested === 'preview' ||
      preview.outcome !== 'SUCCESS' ||
      (preview.resourceId === props.resourceId && preview.isCross === props.isCross)) &&
    (requested === 'preview' || (requested === undefined && asksAboutPreview))
  ) {
    const { sql, sourceLabel, ...facts } = preview
    const label =
      tr(preview.outcome === 'SUCCESS' ? 'successful_preview' : 'failed_preview') +
      ' · ' +
      (sourceLabel || preview.resourceId) +
      ' · ' +
      (preview.isCross ? 'Calcite' : t('common.single_source')) +
      ' · ' +
      tr('draft_revision') +
      ' ' +
      preview.revision +
      (revision !== preview.revision ? ' · ' + tr('preview_draft_changed') : '')
    return {
      label,
      sql,
      preview: {
        ...facts,
        currentResourceId: props.resourceId,
        currentIsCross: props.isCross,
        currentRevision: revision
      }
    }
  }
  if (requested === 'preview') return
  const last = turns.value.at(-1)
  if (requested === undefined && last?.isRetry && last.choices) {
    if (last.draftRevision === revision && last.session?.resourceId === props.resourceId) {
      return { label: last.basis, sql: last.baseSql }
    }
    const sql = props.readDraft()
    return { label: sql ? tr('editor_draft') : newDraftLabel(), sql }
  }
  const index = (requested ?? latest) - 1
  const prior = turns.value[index]
  if (requested !== undefined && !suggestionText(prior?.result)) return
  if (
    suggestionText(prior?.result) &&
    (requested !== undefined ||
      (prior.session?.resourceId === props.resourceId && prior.draftRevision === revision))
  ) {
    return {
      label: tr('numbered_suggestion') + ' #' + (index + 1),
      sql: suggestionText(prior.result)!,
      prior
    }
  }
  const sql = props.readDraft()
  return { label: sql ? tr('editor_draft') : newDraftLabel(), sql }
}
const currentBasis = computed(() => chooseBasis(input.value.trim()))
const leaveBasis = (event: FocusEvent | MouseEvent) => {
  const container = event.currentTarget as HTMLElement
  const focus = event.type === 'focusout' ? event.relatedTarget : document.activeElement
  if (!(focus instanceof Node) || !container.contains(focus)) showFullBasis.value = false
}
const firstLine = (text: string) => {
  const lines = text.split(/\r\n|\r|\n/)
  const index = lines.findIndex(line => line.trim())
  return index < 0 ? '' : lines[index].trim() + (index < lines.length - 1 ? '…' : '')
}
watch(
  () => props.preview,
  () => {
    if (selectedBasis.value === 'preview') selectedBasis.value = undefined
  }
)
// The draft formula and its proposed attributes may change after a turn. References,
// parameters, node bindings and the execution mode must still match the validated snapshot.
const calcScope = (draft?: AiCalcFieldDraft) => {
  if (!draft) return undefined
  const { allFields, ...dataset } = draft.dataset
  return JSON.stringify([
    draft.fieldId,
    dataset,
    allFields.map(field =>
      field.id === draft.fieldId
        ? { id: field.id, params: field.params, type: draft.preserveType ? field.deType : null }
        : field
    )
  ])
}
const checkedSuggestion = async (turn: Turn, target: string, referenceOnly = false) => {
  const session = turn.session!
  if (props.calcFieldDraft && turn.calcScope !== calcScope(props.calcFieldDraft())) {
    throw new Error('scope_changed')
  }
  let checked: AiAssistanceResult
  if (referenceOnly && failedFormula(turn.result)) {
    const page = await getConversationEvents(
      session.sessionId,
      turn.executionId!,
      turn.cursor ?? 0,
      new AbortController().signal
    )
    if (
      page.status !== 'COMPLETED' ||
      !page.result ||
      !failedFormula(page.result) ||
      page.executionId !== turn.executionId ||
      page.sessionId !== session.sessionId ||
      page.contextVersion !== session.contextVersion ||
      target !== props.resourceId ||
      session.resourceId !== target ||
      turn.calcScope !== calcScope(props.calcFieldDraft?.())
    ) {
      throw new Error('scope_changed')
    }
    checked = page.result
  } else {
    checked = await checkSqlApplication(
      session.sessionId,
      turn.executionId!,
      session.contextVersion,
      session.draftVersion,
      target
    )
  }
  if (
    checked.type !== (props.calcFieldDraft ? 'CALC_FIELD_SUGGESTION' : 'SQL_SUGGESTION') ||
    !suggestionText(checked) ||
    suggestionText(checked) !== suggestionText(turn.result) ||
    checked.editorId !== editorId ||
    checked.resourceVersion !== session.resourceVersion ||
    checked.resourceId !== session.resourceId ||
    checked.draftVersion !== session.draftVersion
  ) {
    throw new Error('scope_changed')
  }
  return checked
}
// Only a question followed by a final numbered list is an invitation to choose.
const clarification = (text: string) => {
  const lines = text.trimEnd().split(/\r?\n/)
  const choices: string[] = []
  let number = 0
  while (lines.length) {
    const match = /^\s*(\d+)[.)、]\s*(\S.*)$/.exec(lines[lines.length - 1])
    if (!match || (number && Number(match[1]) !== number - 1)) break
    choices.unshift(match[2].trimEnd())
    number = Number(match[1])
    lines.pop()
  }
  const question = lines.join('\n').trimEnd()
  if (number === 1 && choices.length >= 2 && /[?？؟]$/.test(question)) {
    return { question, choices }
  }
}
const applyPage = (turn: Turn, page: AiConversationEvents) => {
  if (
    page.sessionId !== turn.session?.sessionId ||
    page.executionId !== turn.executionId ||
    page.contextVersion !== turn.session.contextVersion
  )
    throw new Error('scope_changed')
  if (turn.status !== 'FAILED' && turn.issue?.step === 'progress_step') turn.issue = undefined
  turn.attemptId = page.attemptId ?? turn.attemptId
  turn.retryEligible = page.retryEligible
  turn.recovery = page.recovery ?? turn.recovery
  for (const event of page.events) {
    if (event.sequence <= (turn.cursor ?? 0)) continue
    applyProcessEvent(turn, event)
    turn.cursor = event.sequence
    if (event.type === 'FAILED') {
      const key = aiFailureKey(event.error ?? null)
      const tool = turn.process.entries.filter(entry => entry.tool).at(-1)?.tool
      const kind = turn.recovery?.stepKind
      let step: AnswerFailure['step'] = 'execution_step'
      let reason = key === 'test_failed' ? tr('unknown_failure') : t(`ai_service.${key}`)
      if (event.error === 'connection_interrupted') {
        step = 'progress_step'
        reason = tr('connection_interrupted')
      } else if (kind === 'TOOL') step = 'tool_step'
      else if (kind === 'MODEL') step = 'model_step'
      turn.issue = { step, reason, tool: kind === 'TOOL' ? tool?.name : undefined }
    }
  }
  if (page.metering) turn.metering = page.metering
  turn.cursor = page.cursor
  turn.status = page.status
  if (page.status === 'COMPLETED' && page.result) {
    if (turn.isRetry) {
      latestSession = turn.session
      latestRetryExecution = turn.executionId
    }
    turn.result = page.result
    if (failedFormula(turn.result)) {
      turn.issue = {
        step: 'validation_step',
        reason: tr(turn.result.calcField?.reason || 'unknown_failure')
      }
    }
    const options = page.result.type === 'EXPLANATION' && clarification(page.result.text)
    turn.answer = options ? options.question : page.result.text
    if (options) turn.choices = options.choices
  }
  if (page.status === 'CANCELLED' && page.retryEligible) {
    turn.issue = { step: 'stopped_step', reason: tr('incomplete') }
  }
  if (page.status === 'FAILED') {
    turn.issue ??= { step: 'execution_step', reason: tr('unknown_failure') }
    void refresh()
  }
}
const unreachable = (turn: Turn) => {
  turn.status = 'UNREACHABLE'
  turn.issue = { step: 'progress_step', reason: tr('progress_unreachable') }
}
const poll = async (turn: Turn, read: AbortController) => {
  try {
    const finished = await followConversation({
      session: turn.session!,
      executionId: turn.executionId!,
      cursor: () => turn.cursor ?? 0,
      update: page => applyPage(turn, page),
      signal: read.signal,
      modelDeadline: turn.deadline,
      readFirst: turn.status === 'UNREACHABLE',
      recovering: value => {
        turn.recovering = value
      },
      terminalReadError: error => {
        const status = isRequestError(error) && error.response?.status
        return (
          accessLost(error) || Boolean(status && status >= 400 && status < 500 && status !== 429)
        )
      }
    })
    if (!finished && !read.signal.aborted) unreachable(turn)
  } finally {
    turn.recovering = false
  }
}
const admissionError = (turn: Turn, error: unknown) => {
  if (turn.admit && uncertainResponse(error)) {
    turn.status = 'UNREACHABLE'
    turn.issue = { step: 'progress_step', reason: tr('submission_unknown') }
  } else {
    turn.admit = undefined
    turn.status = 'FAILED'
    fail(error, turn)
  }
}
const resumeLast = () => {
  if (pendingRetry.value) {
    void retryAnswer(pendingRetry.value)
    return
  }
  const turn = turns.value.at(-1)
  if (turn?.status === 'UNREACHABLE') void resume(turn)
}
const resume = async (turn: Turn) => {
  if (busy.value || turn.status !== 'UNREACHABLE') return
  const read = new AbortController()
  request = read
  busy.value = true
  failure.value = ''
  try {
    if (turn.admit) await turn.admit(read)
    else await poll(turn, read)
  } catch (error) {
    if (!read.signal.aborted) admissionError(turn, error)
  } finally {
    if (!read.signal.aborted) {
      busy.value = false
      request = undefined
    }
  }
}
const send = async () => {
  const question = input.value.trim()
  if (
    !question ||
    pendingRetry.value ||
    busy.value ||
    applying.value ||
    !available.value ||
    turns.value.at(-1)?.status === 'UNREACHABLE'
  )
    return
  const basis = includeBasis.value ? currentBasis.value : { label: newDraftLabel(), sql: '' }
  if (!basis) {
    failure.value = tr('unavailable_reference')
    return
  }
  const token = version
  const read = new AbortController()
  request = read
  const current = () => token === version && !read.signal.aborted
  selectedBasis.value = undefined
  includeBasis.value = true
  showFullBasis.value = false
  const source = props.resourceId
  const calcField = props.calcFieldDraft?.()
  const isCross = props.isCross
  const previous = turns.value.at(-1)
  if (previous?.choices) {
    previous.selectedChoice = previous.choices.find(
      (label, index) => question === label || question === String(index + 1)
    )
  }
  const index =
    turns.value.push({
      questionId: uuid.v4(),
      question,
      basis: basis.label,
      baseSql: basis.sql,
      calcScope: calcScope(calcField),
      draftRevision: props.draftRevision,
      process: { entries: [] },
      answer: '',
      status: 'RUNNING'
    }) - 1
  const turn = turns.value[index]
  busy.value = true
  boundaryIssue.value = undefined
  failure.value = input.value = ''
  try {
    const sql = basis.prior
      ? suggestionText(await checkedSuggestion(basis.prior, source, true))!
      : basis.sql
    if (!current()) return
    let opening = {
      scene: calcField ? ('dataset-calc-field' as const) : ('dataset-sql' as const),
      calcField,
      resourceId: basis.preview?.resourceId ?? source,
      isCross: basis.preview?.isCross ?? isCross,
      preview: basis.preview,
      systemVariableNames: calcField
        ? undefined
        : [...new Set(props.systemVariableNames)]
            .sort(
              (a, b) =>
                Number(question.includes(b) || sql.includes(b)) -
                Number(question.includes(a) || sql.includes(a))
            )
            .slice(0, 64),
      editorId,
      sql,
      openKey: uuid.v4(),
      previousSessionId: latestSession?.sessionId,
      previousExecutionId: latestRetryExecution
    }
    const owner = identityScope()
    const submissionKey = uuid.v4()
    let submitted = false
    turn.admit = async read => {
      const sameOwner = () => owner === identityScope()
      const active = () => token === version && sameOwner()
      const open = () => replayOperation(() => openConversation(opening), sameOwner)
      let session = turn.session
      if (!session) {
        try {
          session = await open()
        } catch (error) {
          if (
            !active() ||
            turn.stopRequested ||
            opening.previousExecutionId ||
            basis.prior ||
            basis.preview ||
            !opening.previousSessionId ||
            latestSession?.resourceId === source ||
            !accessLost(error)
          )
            throw error
          clearHistory()
          turns.value.push(turn)
          failure.value = tr('history_unavailable')
          opening = { ...opening, previousSessionId: undefined, openKey: uuid.v4() }
          session = await open()
        }
        if (!active()) {
          close(session.sessionId, owner)
          return
        }
        turn.session = latestSession = session
        latestRetryExecution = undefined
        sessions.set(session.sessionId, owner)
        window.clearTimeout(expiryTimer)
        expiryTimer = window.setTimeout(() => {
          const wasExpanded = expanded.value
          reset()
          expanded.value = wasExpanded
          failure.value = tr('history_unavailable')
        }, Math.max(0, session.expiresAt - Date.now()))
      }
      if (turn.stopRequested && !submitted) {
        turn.admit = undefined
        turn.status = 'CANCELLED'
        return
      }
      submitted = true
      const { sessionId, contextVersion } = session
      const run = await replayOperation(
        () => startConversationExecution(sessionId, contextVersion, question, submissionKey),
        active
      )
      if (!active()) {
        close(sessionId, owner)
        return
      }
      turn.admit = undefined
      turn.executionId = run.executionId
      turn.attemptId = run.attemptId
      turn.deadline = run.deadline
      if (turn.stopRequested) await stopRun(turn, token)
      else await poll(turn, read)
    }
    await turn.admit(read)
  } catch (error) {
    if (current()) admissionError(turn, error)
  } finally {
    if (current() || (token === version && request === read && !turn.executionId)) {
      busy.value = false
      request = undefined
    }
  }
}
const retryAnswer = async (source: Turn) => {
  const session = source.session
  if (
    busy.value ||
    applying.value ||
    turns.value.at(-1)?.status === 'UNREACHABLE' ||
    (pendingRetry.value && pendingRetry.value !== source) ||
    !source.retryEligible ||
    !source.attemptId ||
    !source.executionId ||
    !session
  )
    return
  const token = version
  const read = new AbortController()
  busy.value = true
  request = read
  const key = (source.retryKey ??= uuid.v4())
  let next: Turn | undefined
  try {
    pendingRetry.value = source
    const owner = identityScope()
    const { executionId, attemptId } = source
    const run = await replayOperation(
      () => retryConversationExecution(session.sessionId, executionId!, attemptId!, key),
      () => token === version && owner === identityScope()
    )
    if (token !== version) {
      close(session.sessionId, owner)
      return
    }
    pendingRetry.value = undefined
    source.retryEligible = false
    const index =
      turns.value.push({
        isRetry: true,
        questionId: source.questionId,
        question: source.question,
        basis: source.basis,
        baseSql: source.baseSql,
        draftRevision: source.draftRevision,
        calcScope: source.calcScope,
        session,
        executionId: run.executionId,
        attemptId: run.attemptId,
        deadline: run.deadline,
        process: { entries: [] },
        answer: '',
        status: 'RUNNING'
      }) - 1
    next = turns.value[index]
    if (source.stopRequested) await stopRun(next, token)
    else await poll(next, read)
  } catch (error) {
    if (token !== version || read.signal.aborted) return
    if (!uncertainResponse(error)) {
      source.retryKey = undefined
      source.stopRequested = false
    }
    if (!next && source.issue) {
      const diagnostic = publicFailure(
        error,
        uncertainResponse(error) ? tr('submission_unknown') : tr('failed')
      )
      if (accessLost(error)) {
        fail(error)
        failure.value = ''
        boundaryIssue.value = { step: 'submission_step', ...diagnostic }
      } else {
        source.issue.retryMessage = diagnostic.reason
        source.issue.retryRequestId = diagnostic.requestId
      }
    } else if (next && !accessLost(error)) unreachable(next)
    else fail(error)
  } finally {
    if (pendingRetry.value === source && !source.retryKey) pendingRetry.value = undefined
    if (token === version && request === read && !read.signal.aborted) {
      busy.value = false
      request = undefined
    }
  }
}
const answerChoice = (turn: Turn, choice: string) => {
  if (turn !== turns.value.at(-1) || busy.value || applying.value) return
  input.value = choice
  void send()
}
const keydown = (event: KeyboardEvent) => {
  if (
    event.key !== 'Enter' ||
    event.shiftKey ||
    event.isComposing ||
    composing.value ||
    event.keyCode === 229
  )
    return
  event.preventDefault()
  if (!event.repeat) void send()
}
const stopRun = async (turn: Turn, token: number) => {
  turn.status = 'STOPPING'
  try {
    let page = await stopConversationExecution(turn.session!.sessionId, turn.executionId!)
    if (token !== version) return
    if (page.status === 'RUNNING') throw new Error('response_invalid')
    const after = turn.cursor ?? 0
    if (page.cursor > after && page.events[0]?.sequence !== after + 1) {
      page = await getConversationEvents(
        turn.session!.sessionId,
        turn.executionId!,
        after,
        new AbortController().signal
      )
      if (token !== version) return
    }
    failure.value = ''
    applyPage(turn, page)
  } catch (error) {
    if (token === version) {
      if (accessLost(error)) fail(error)
      else unreachable(turn)
    }
  } finally {
    if (token === version) {
      busy.value = false
      request = undefined
    }
  }
}
const stop = async () => {
  const turn = turns.value.at(-1)
  if (
    !turn ||
    turn.status === 'STOPPING' ||
    (!busy.value && !pendingRetry.value && turn.status !== 'UNREACHABLE')
  )
    return
  if (pendingRetry.value) {
    pendingRetry.value.stopRequested = true
    if (!busy.value) await retryAnswer(pendingRetry.value)
    return
  }
  if (turn.admit) {
    turn.stopRequested = true
    if (!busy.value) await resume(turn)
    return
  }
  request?.abort()
  if (!turn.session || !turn.executionId) {
    turn.status = 'CANCELLED'
    return
  }
  busy.value = true
  await stopRun(turn, version)
}
const copySuggestion = async (turn: Turn) => {
  if (busy.value || applying.value || !suggestionText(turn.result)) return
  const token = version
  applying.value = true
  try {
    const result = await checkedSuggestion(turn, props.resourceId, true)
    const text = suggestionText(result)
    if (token !== version || !text) return
    await toClipboard(text)
    ElMessage.success(t('data_set.copied_successfully'))
  } catch (error) {
    if (token === version) fail(error)
  } finally {
    if (token === version) applying.value = false
  }
}
const apply = async (turn: Turn) => {
  const { session, executionId, result } = turn
  if (busy.value || applying.value || !session || !executionId || !suggestionText(result)) return
  const token = version
  const draft = props.readDraft()
  const source = props.resourceId
  const revision = props.draftRevision
  applying.value = true
  failure.value = ''
  try {
    const warnings = []
    if (source !== session.resourceId) {
      warnings.push(
        tr('source_copy') +
          ' ' +
          session.resourceName +
          ' (' +
          session.resourceId +
          ') → ' +
          (props.resourceLabel || source) +
          ' (' +
          source +
          ')'
      )
    }
    if (revision !== turn.draftRevision) warnings.push(tr('overwrite_edits'))
    else if (draft.trim() && draft !== suggestionText(result))
      warnings.push(tr(props.calcFieldDraft ? 'replace_formula' : 'replace_draft'))
    if (warnings.length) {
      try {
        await ElMessageBox.confirm(warnings.join(' '), title(), {
          confirmButtonText: t('common.sure'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        })
      } catch {
        return
      }
    }
    if (token !== version) return
    const checked = await checkedSuggestion(turn, source)
    if (token !== version) return
    if (
      draft !== props.readDraft() ||
      source !== props.resourceId ||
      revision !== props.draftRevision ||
      (props.calcFieldDraft && turn.calcScope !== calcScope(props.calcFieldDraft()))
    ) {
      failure.value = tr('stale_draft')
      return
    }
    if (checked.calcField) emit('apply-formula', checked.calcField)
    else if (checked.sql) emit('apply', checked.sql)
  } catch (error) {
    if (token === version) fail(error)
  } finally {
    if (token === version) applying.value = false
  }
}
</script>

<template>
  <el-button
    v-if="available"
    text
    data-testid="dataset-sql-assistant-toggle"
    :aria-expanded="expanded"
    @click="expanded = !expanded"
  >
    {{ title() }}
  </el-button>
  <Teleport :to="host">
    <aside
      v-if="available && expanded"
      class="dataset-sql-assistant"
      data-testid="dataset-sql-assistant-panel"
      :aria-label="title()"
    >
      <div class="assistant-heading">
        <strong>{{ title() }}</strong>
        <el-button text @click="newConversation">{{ tr('new_conversation') }}</el-button>
      </div>
      <div ref="conversation" class="conversation" role="log">
        <article v-for="group in questionGroups" :key="group[0].turn.questionId">
          <p class="question">{{ group[0].turn.question }}</p>
          <component
            :is="attempt < group.length - 1 ? 'details' : 'div'"
            v-for="({ turn, index }, attempt) in group"
            :key="index"
          >
            <summary v-if="attempt < group.length - 1">
              {{ tr('previous_answer') }} ·
              {{ turn.issue ? tr(turn.issue.step) : tr('incomplete') }}
            </summary>
            <details v-if="turn.baseSql.trim()">
              <summary>{{ tr('generated_from') }} · {{ turn.basis }}</summary>
              <pre>{{ turn.baseSql }}</pre>
              <small v-if="turn.session"
                >{{ turn.session.resourceName }} ({{ turn.session.resourceId }})</small
              >
            </details>
            <AssistantProcess
              :process="turn.process"
              :elapsed="turn.metering && elapsedLabel(turn.metering.elapsedMillis)"
              :status="turn.status"
              :session-id="turn.session?.sessionId"
              :execution-id="turn.executionId"
              @update:open="turn.process.open = $event"
              @detail-error="error => accessLost(error) && fail(error)"
            />
            <AssistantAnswer
              v-if="turn.answer"
              :text="turn.answer"
              :suggestion="suggestionText(turn.result)"
            />
            <div v-if="turn.choices" class="clarification-choices">
              <el-button
                v-for="(choice, i) in turn.choices"
                :key="i"
                :aria-pressed="turn.selectedChoice === choice"
                :type="turn.selectedChoice === choice ? 'primary' : undefined"
                :disabled="index !== turns.length - 1 || busy || applying"
                @click="answerChoice(turn, choice)"
                >{{ choice }}</el-button
              >
            </div>
            <p v-if="turn.recovering" role="status">{{ tr('editor_recovering') }}</p>
            <p v-else-if="turn.status === 'RUNNING'">{{ tr('running') }}</p>
            <p v-if="turn.status === 'CANCELLED'">{{ tr('incomplete') }}</p>
            <p v-if="turn.recovery?.technicalRetries || turn.recovery?.corrections">
              {{ tr('technical_retries') }} {{ turn.recovery.technicalRetries }} ·
              {{ tr('corrections') }} {{ turn.recovery.corrections }}
            </p>
            <AssistantFailure
              v-if="turn.issue"
              :issue="turn.issue"
              :execution-id="turn.executionId"
              :can-retry="turn.retryEligible && !!turn.attemptId"
              :disabled="busy || applying"
              :no-formula="
                !!calcFieldDraft &&
                turn.status === 'FAILED' &&
                turn.issue.step === 'model_step' &&
                !turn.process.entries.some(entry => entry.tool?.name === 'propose_calc_field')
              "
              @retry="retryAnswer(turn)"
            />
            <template v-if="turn.result?.type === 'CALC_FIELD_SUGGESTION' && turn.result.calcField">
              <small>{{ tr('numbered_suggestion') }} #{{ index + 1 }}</small>
              <pre>{{ turn.result.calcField.expression }}</pre>
              <span>{{ validationLabel(turn.result.calcField.validation) }}</span>
              <el-button text :disabled="busy || applying" @click="copySuggestion(turn)">
                {{ tr('copy_formula') }}
              </el-button>
              <el-button
                :disabled="busy || applying || !canApplyCalcField(turn.result.calcField.validation)"
                @click="apply(turn)"
                >{{ tr('apply') }}</el-button
              >
            </template>
            <template v-if="turn.result?.type === 'SQL_SUGGESTION' && turn.result.sql">
              <details>
                <summary>{{ tr('sql_evidence') }}</summary>
                <p v-if="!turn.result.evidence?.length">{{ tr('no_sql_evidence') }}</p>
                <div v-for="table in turn.result.evidence" :key="table.table">
                  <strong>{{ table.table }}</strong>
                  <small>{{ tr(table.inspected ? 'verified_structure' : 'sql_reference') }}</small>
                  <p v-if="table.description">{{ table.description }}</p>
                  <p v-if="table.grain">{{ table.grain }}</p>
                  <div v-for="relation in table.relations" :key="JSON.stringify(relation)">
                    <p>
                      {{ tr('confirmed_join') }} · {{ relation.name }} · {{ relation.cardinality }}
                    </p>
                    <code
                      >{{ relation.leftTable }} {{ relation.joinType }} JOIN
                      {{ relation.rightTable }}</code
                    >
                    <p v-if="relation.actual">
                      {{ tr('actual_join') }} ·
                      <code
                        >{{ relation.actual.leftTable }} {{ relation.actual.joinType }} JOIN
                        {{ relation.actual.rightTable }}</code
                      >
                    </p>
                    <p
                      v-for="pair in relation.conditions"
                      :key="pair.leftField + '=' + pair.rightField"
                    >
                      {{ relation.leftTable }}.{{ pair.leftField }} = {{ relation.rightTable }}.{{
                        pair.rightField
                      }}
                    </p>
                  </div>
                  <ul v-if="table.fields.length">
                    <li v-for="field in table.fields" :key="field.name">
                      <code>{{ field.name }}</code> {{ field.type }}
                      <span v-if="field.comment && field.comment !== field.name">{{
                        field.comment
                      }}</span>
                      <p v-if="field.businessName || field.description">
                        {{ tr('confirmed_meaning') }} · {{ field.businessName }}
                        {{ field.description }}
                      </p>
                      <p v-for="value in field.values" :key="value.value">
                        {{ value.value }} → {{ value.meaning }}
                      </p>
                    </li>
                  </ul>
                </div>
              </details>
              <small
                >{{ tr('numbered_suggestion') }} #{{ index + 1 }} · {{ tr('unrun_sql') }}</small
              >
              <pre>{{ turn.result.sql }}</pre>
              <el-button text :disabled="busy || applying" @click="copySuggestion(turn)">
                {{ tr('copy_sql') }}
              </el-button>
              <el-button
                data-testid="dataset-sql-assistant-apply"
                :disabled="busy || applying"
                @click="apply(turn)"
                >{{ tr('apply') }}</el-button
              >
            </template>
            <el-button
              v-if="suggestionText(turn.result)"
              text
              :disabled="busy || applying"
              data-testid="dataset-sql-assistant-reference"
              @click="selectBasis(index + 1)"
              >{{ tr('refer_to_suggestion') }}</el-button
            >
            <footer v-if="turn.metering" class="answer-metering">
              <span v-if="!turn.process.entries.length">{{
                elapsedLabel(turn.metering.elapsedMillis)
              }}</span>
              <span v-if="turn.metering.usageStatus === 'MISSING'">{{ tr('usage_missing') }}</span>
              <template v-else>
                <span>{{ tr('input_tokens') }} {{ turn.metering.inputTokens ?? '—' }}</span>
                <span>{{ tr('output_tokens') }} {{ turn.metering.outputTokens ?? '—' }}</span>
                <span v-if="turn.metering.usageStatus === 'PARTIAL'">{{
                  tr('usage_partial')
                }}</span>
              </template>
            </footer>
          </component>
        </article>
      </div>
      <AssistantFailure
        v-if="boundaryIssue"
        :issue="boundaryIssue"
        :disabled="false"
        :no-formula="false"
      />
      <p v-if="failure" role="alert">{{ failure }}</p>
      <div v-if="(props.preview || selectedBasis !== undefined) && !busy">
        <el-button
          v-if="props.preview && selectedBasis === undefined"
          text
          @click="selectBasis('preview')"
          >{{ tr('refer_to_preview') }}</el-button
        >
        <el-tag v-if="selectedBasis !== undefined" closable @close="selectedBasis = undefined">
          {{
            selectedBasis === 'preview'
              ? tr('refer_to_preview')
              : tr('numbered_suggestion') + ' #' + selectedBasis
          }}
        </el-tag>
      </div>
      <div v-if="!busy && currentBasis?.sql.trim()" class="basis-row">
        <label>
          <input v-model="includeBasis" type="checkbox" />
          {{ tr('include_basis') }}
        </label>
        <div
          class="basis-content"
          @mouseenter="showFullBasis = true"
          @mouseleave="leaveBasis"
          @focusin="showFullBasis = true"
          @focusout="leaveBasis"
          @keydown.esc.stop="showFullBasis = false"
        >
          <button
            type="button"
            class="basis-preview"
            :aria-label="tr('view_full_basis') + ' · ' + currentBasis.label"
            :aria-expanded="showFullBasis"
            :aria-describedby="showFullBasis ? editorId + '-basis' : undefined"
            @click="showFullBasis = true"
          >
            <span>{{ currentBasis.label }}</span>
            <code>{{ firstLine(currentBasis.sql) }}</code>
          </button>
          <pre
            v-if="showFullBasis"
            role="tooltip"
            class="basis-full"
            tabindex="0"
            :id="editorId + '-basis'"
            >{{ currentBasis.sql }}</pre
          >
        </div>
      </div>
      <el-input
        type="textarea"
        data-testid="dataset-sql-assistant-input"
        v-model="input"
        :aria-label="tr('message')"
        :placeholder="tr('message')"
        @compositionstart="composing = true"
        @compositionend="composing = false"
        @keydown="keydown"
      />
      <small>{{ tr('send_shortcut') }}</small>
      <el-button
        v-if="!busy && (pendingRetry || turns.at(-1)?.status === 'UNREACHABLE')"
        data-testid="dataset-sql-assistant-resume"
        @click="resumeLast"
        >{{
          tr(pendingRetry || turns.at(-1)?.admit ? 'resume_submission' : 'resume_progress')
        }}</el-button
      >
      <el-button
        v-if="busy || pendingRetry || turns.at(-1)?.status === 'UNREACHABLE'"
        data-testid="dataset-sql-assistant-stop"
        @click="stop"
        >{{ tr('stop') }}</el-button
      >
      <el-button
        v-else
        data-testid="dataset-sql-assistant-send"
        type="primary"
        :disabled="!input.trim() || applying"
        @click="send"
        >{{ tr('send') }}</el-button
      >
    </aside>
  </Teleport>
</template>

<style scoped lang="less">
.answer-metering {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  font-size: 12px;
}

.dataset-sql-assistant {
  width: min(360px, 36vw);
  height: 100%;
  box-sizing: border-box;
  padding: 16px;
  border-left: 1px solid var(--ed-border-color);
  display: flex;
  flex-direction: column;
  gap: 12px;

  .assistant-heading {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .basis-row {
    display: flex;
    gap: 8px;
    align-items: center;
    label {
      display: inline-flex;
      align-items: center;
      white-space: nowrap;
      cursor: pointer;
    }
  }
  .basis-content {
    position: relative;
    flex: 1;
    min-width: 0;
  }
  .basis-preview {
    width: 100%;
    min-width: 0;
    padding: 4px 8px;
    text-align: left;
    background: var(--ed-fill-color-light);
    color: inherit;
    border: 1px solid var(--ed-border-color);
    border-radius: 4px;
    cursor: pointer;
    span,
    code {
      display: block;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
  }
  .basis-full {
    position: absolute;
    bottom: 100%;
    right: 0;
    width: max-content;
    max-width: min(440px, 80vw);
    max-height: 35vh;
    overflow: auto;
    margin: 0;
    padding: 8px;
    z-index: 1;
    background: var(--ed-bg-color-overlay);
    border: 1px solid var(--ed-border-color);
    border-radius: 4px;
  }
  .conversation {
    flex: 1;
    overflow: auto;
    min-height: 0;
  }
  p,
  pre {
    white-space: pre-wrap;
    overflow-wrap: anywhere;
  }
  .question {
    font-weight: 600;
  }
}
</style>
