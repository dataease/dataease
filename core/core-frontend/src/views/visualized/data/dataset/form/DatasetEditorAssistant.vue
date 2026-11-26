<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { uuid } from 'vue-uuid'
import AssistantProcess from './AssistantProcess.vue'
import AssistantAnswer from './AssistantAnswer.vue'
import { applyProcessEvent, type AnswerProcess } from './assistantProcess'
import useClipboard from 'vue-clipboard3'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { isRequestError } from '@/config/axios/error'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import { followConversationSocket } from '@/api/aiConversationDelivery'
import { aiFailureKey } from '@/api/aiService'
import {
  checkSqlApplication,
  closeConversation,
  getAiAvailability,
  getConversationEvents,
  openConversation,
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
  executionId?: string
  result?: AiAssistanceResult
  status: string
}
const available = ref(false)
const expanded = ref(false)
const input = ref('')
const composing = ref(false)
const failure = ref('')
const turns = ref<Turn[]>([])
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

const sessions = new Set<string>()
let latestSession: AiConversationSession | undefined
let request: AbortController | undefined
let availabilityRead: AbortController | undefined
let availabilityTimer: number | undefined
let expiryTimer: number | undefined
let version = 0
let disposed = false

const close = (id: string) => void closeConversation(id).catch(() => undefined)
const clearHistory = () => {
  window.clearTimeout(expiryTimer)
  sessions.forEach(close)
  sessions.clear()
  latestSession = undefined
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
  () => [user.uid, user.oid, user.proxyInfo?.proxyOid],
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
  document.addEventListener('visibilitychange', refresh)
})
onBeforeUnmount(() => {
  disposed = true
  cancelAvailability()
  window.removeEventListener('focus', refresh)
  document.removeEventListener('visibilitychange', refresh)
  reset()
})

const accessLost = (error: unknown) =>
  isRequestError(error) &&
  ([401, 403, 404].includes(error.response?.status) ||
    ('messageKey' in error && error.messageKey === 'i18n_ai_scope_changed'))
const fail = (error: unknown) => {
  if (accessLost(error) || (error instanceof Error && error.message === 'scope_changed')) {
    emit('invalidate-preview')
    clearHistory()
  }
  failure.value =
    isRequestError(error) && error.response?.status === 403
      ? t('ai_service.failure_forbidden')
      : tr('failed')
  if (
    isRequestError(error) &&
    'messageKey' in error &&
    error.messageKey === 'i18n_ai_calc_sql_manual'
  )
    failure.value = tr('calc_sql_manual')
  void refresh()
}
// Displayed turn numbers also work as references in a free-text follow-up.
const referencedTurn = (question: string, latest: number) => {
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
  for (const event of page.events) {
    if (event.sequence <= (turn.cursor ?? 0)) continue
    applyProcessEvent(turn, event)
    turn.cursor = event.sequence
    if (event.type === 'FAILED') {
      const key = aiFailureKey(event.error ?? null)
      failure.value = key === 'test_failed' ? tr('failed') : t(`ai_service.${key}`)
    }
  }
  if (page.metering) turn.metering = page.metering
  turn.cursor = page.cursor
  turn.status = page.status
  if (page.status === 'COMPLETED' && page.result) {
    turn.result = page.result
    const options = page.result.type === 'EXPLANATION' && clarification(page.result.text)
    turn.answer = options ? options.question : page.result.text
    if (options) turn.choices = options.choices
  }
  if (page.status === 'FAILED') {
    failure.value ||= tr('failed')
    void refresh()
  }
}
const unreachable = (turn: Turn) => {
  turn.status = 'UNREACHABLE'
  failure.value = tr('progress_unreachable')
}
const poll = async (turn: Turn, read: AbortController) => {
  const finished = await followConversationSocket({
    session: turn.session!,
    executionId: turn.executionId!,
    cursor: () => turn.cursor ?? 0,
    update: page => applyPage(turn, page),
    signal: read.signal
  })
  if (finished) return
  let failures = 0
  while (!read.signal.aborted) {
    let page: AiConversationEvents
    const attempt = new AbortController()
    const abort = () => attempt.abort()
    read.signal.addEventListener('abort', abort, { once: true })
    const timer = window.setTimeout(abort, 2500)
    try {
      page = await getConversationEvents(
        turn.session!.sessionId,
        turn.executionId!,
        turn.cursor ?? 0,
        attempt.signal
      )
    } catch (error) {
      if (read.signal.aborted) return
      const status = isRequestError(error) && error.response?.status
      if (accessLost(error) || (status && status < 500)) throw error
      if (++failures > 3) {
        unreachable(turn)
        return
      }
      failure.value = tr('progress_recovering') + ` (${failures}/3)`
      await new Promise(resolve => window.setTimeout(resolve, failures * 400))
      continue
    } finally {
      window.clearTimeout(timer)
      read.signal.removeEventListener('abort', abort)
    }
    if (read.signal.aborted) return
    if (failures) failure.value = ''
    failures = 0
    applyPage(turn, page)
    if (page.status !== 'RUNNING') return
    await new Promise(resolve => window.setTimeout(resolve, 500))
  }
}
const resume = async (turn: Turn) => {
  if (busy.value || turn.status !== 'UNREACHABLE') return
  const read = new AbortController()
  request = read
  busy.value = true
  failure.value = ''
  try {
    await poll(turn, read)
  } catch (error) {
    if (!read.signal.aborted) fail(error)
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
  failure.value = input.value = ''
  try {
    const sql = basis.prior
      ? suggestionText(await checkedSuggestion(basis.prior, source, true))!
      : basis.sql
    if (!current()) return
    const opening = {
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
      previousSessionId: latestSession?.sessionId
    }
    let session: AiConversationSession
    try {
      session = await openConversation(opening)
    } catch (error) {
      // An inaccessible source in the retained history must not block a fresh question on B.
      if (
        !current() ||
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
      session = await openConversation({
        ...opening,
        previousSessionId: undefined,
        openKey: uuid.v4()
      })
    }
    if (token !== version) {
      close(session.sessionId)
      return
    }
    turn.session = latestSession = session
    sessions.add(session.sessionId)
    window.clearTimeout(expiryTimer)
    expiryTimer = window.setTimeout(() => {
      const wasExpanded = expanded.value
      reset()
      expanded.value = wasExpanded
      failure.value = tr('history_unavailable')
    }, Math.max(0, session.expiresAt - Date.now()))
    // Opening the continuation retires its predecessor even when the user stops before submission.
    if (!current()) return
    const run = await startConversationExecution(
      session.sessionId,
      session.contextVersion,
      question,
      uuid.v4()
    )
    if (token !== version) {
      await stopConversationExecution(session.sessionId, run.executionId).catch(() => undefined)
      return
    }
    turn.executionId = run.executionId
    if (!current()) {
      await stopRun(turn, token)
      return
    }
    await poll(turn, read)
  } catch (error) {
    if (current()) {
      turn.status = 'FAILED'
      fail(error)
    }
  } finally {
    if (current() || (token === version && request === read && !turn.executionId)) {
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
    const page = await stopConversationExecution(turn.session!.sessionId, turn.executionId!)
    if (token !== version) return
    if (page.status === 'RUNNING') throw new Error('response_invalid')
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
  if (!turn || turn.status === 'STOPPING' || (!busy.value && turn.status !== 'UNREACHABLE')) return
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
        <article v-for="(turn, index) in turns" :key="index">
          <p class="question">{{ turn.question }}</p>
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
          <p v-if="turn.status === 'RUNNING'">{{ tr('running') }}</p>
          <p v-if="turn.status === 'CANCELLED'">{{ tr('incomplete') }}</p>
          <template v-if="turn.result?.type === 'CALC_FIELD_SUGGESTION' && turn.result.calcField">
            <small>{{ tr('numbered_suggestion') }} #{{ index + 1 }}</small>
            <pre>{{ turn.result.calcField.expression }}</pre>
            <span>{{ validationLabel(turn.result.calcField.validation) }}</span>
            <p v-if="failedFormula(turn.result) && turn.result.calcField.reason">
              {{ tr(turn.result.calcField.reason) }}
            </p>
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
            <small>{{ tr('numbered_suggestion') }} #{{ index + 1 }} · {{ tr('unrun_sql') }}</small>
            <pre>{{ turn.result.sql }}</pre>
            <el-button text :disabled="busy || applying" @click="copySuggestion(turn)">
              {{ tr('copy_sql') }}
            </el-button>
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
              <span v-if="turn.metering.usageStatus === 'PARTIAL'">{{ tr('usage_partial') }}</span>
            </template>
          </footer>
        </article>
      </div>
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
        v-if="!busy && turns.at(-1)?.status === 'UNREACHABLE'"
        data-testid="dataset-sql-assistant-resume"
        @click="resume(turns.at(-1)!)"
        >{{ tr('resume_progress') }}</el-button
      >
      <el-button
        v-if="busy || turns.at(-1)?.status === 'UNREACHABLE'"
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
