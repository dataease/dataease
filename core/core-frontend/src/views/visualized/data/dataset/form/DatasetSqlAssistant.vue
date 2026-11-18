<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { uuid } from 'vue-uuid'
import { ElMessageBox } from 'element-plus-secondary'
import { isRequestError } from '@/config/axios/error'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
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
  type AiConversationSession
} from '@/api/aiConversation'

const props = defineProps<{
  host: HTMLElement
  resourceId: string
  resourceLabel: string
  draftRevision: number
  readDraft: () => string
}>()
const emit = defineEmits<{ (event: 'apply', query: string): void }>()
const { t } = useI18n()
const tr = (key: string) => t(`ai_conversation.${key}`)
const user = useUserStoreWithOut()
const editorId = uuid.v4()
interface Turn {
  basis: string
  baseSql: string
  draftRevision: number
  question: string
  answer: string
  session?: AiConversationSession
  executionId?: string
  result?: AiAssistanceResult
  status: string
}
const available = ref(false)
const expanded = ref(false)
const input = ref('')
const failure = ref('')
const turns = ref<Turn[]>([])
// Zero means an explicitly fresh query. Positive numbers refer to displayed turn numbers.
const selectedBasis = ref<number>()
const busy = ref(false)
const applying = ref(false)
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
  turns.value = []
}
const reset = () => {
  version++
  request?.abort()
  request = undefined
  clearHistory()
  busy.value = applying.value = expanded.value = false
  input.value = failure.value = ''
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
  if (accessLost(error)) clearHistory()
  failure.value =
    isRequestError(error) && error.response?.status === 403
      ? t('ai_service.failure_forbidden')
      : tr('failed')
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
const chooseBasis = (question: string) => {
  if (
    selectedBasis.value === 0 ||
    (selectedBasis.value === undefined &&
      /(?:^|[，,。.!！]\s*)(?:请|請|please\s+)?(?:另写|另寫|从头写|從頭寫|重新写一|重新寫一|from scratch|start (?:over|afresh))/i.test(
        question
      ))
  ) {
    return { label: tr('new_query'), sql: '' }
  }
  const latest = turns.value.reduce((last, turn, i) => (turn.result?.sql ? i + 1 : last), 0)
  const requested = selectedBasis.value ?? referencedTurn(question, latest)
  const index = (requested ?? latest) - 1
  const prior = turns.value[index]
  if (requested !== undefined && !prior?.result?.sql) throw new Error('unavailable_reference')
  if (
    prior?.result?.sql &&
    (requested !== undefined ||
      (prior.session?.resourceId === props.resourceId &&
        prior.draftRevision === props.draftRevision))
  ) {
    return { label: tr('numbered_suggestion') + ' #' + (index + 1), sql: prior.result.sql, prior }
  }
  const sql = props.readDraft()
  return { label: tr(sql ? 'editor_draft' : 'new_query'), sql }
}
const checkedSuggestion = async (turn: Turn, target: string) => {
  const session = turn.session!
  const checked = await checkSqlApplication(
    session.sessionId,
    turn.executionId!,
    session.contextVersion,
    session.draftVersion,
    target
  )
  if (
    checked.type !== 'SQL_SUGGESTION' ||
    !checked.sql ||
    checked.sql !== turn.result?.sql ||
    checked.editorId !== editorId ||
    checked.resourceVersion !== session.resourceVersion ||
    checked.resourceId !== session.resourceId ||
    checked.draftVersion !== session.draftVersion
  ) {
    throw new Error('scope_changed')
  }
  return checked.sql
}
const send = async () => {
  const question = input.value.trim()
  if (!question || busy.value || applying.value || !available.value) return
  let basis: ReturnType<typeof chooseBasis>
  try {
    basis = chooseBasis(question)
  } catch {
    failure.value = tr('unavailable_reference')
    return
  }
  const token = version
  const read = new AbortController()
  request = read
  const current = () => token === version && !read.signal.aborted
  selectedBasis.value = undefined
  const source = props.resourceId
  const index =
    turns.value.push({
      question,
      basis: basis.label,
      baseSql: basis.sql,
      draftRevision: props.draftRevision,
      answer: '',
      status: 'RUNNING'
    }) - 1
  const turn = turns.value[index]
  busy.value = true
  failure.value = input.value = ''
  try {
    const sql = basis.prior ? await checkedSuggestion(basis.prior, source) : basis.sql
    if (!current()) return
    const opening = {
      scene: 'dataset-sql' as const,
      resourceId: source,
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
    if (!current()) {
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
    const run = await startConversationExecution(
      session.sessionId,
      session.contextVersion,
      question,
      uuid.v4()
    )
    if (!current()) {
      void stopConversationExecution(session.sessionId, run.executionId).catch(() => undefined)
      return
    }
    turn.executionId = run.executionId
    let cursor = 0
    while (current()) {
      const page = await getConversationEvents(
        session.sessionId,
        run.executionId,
        cursor,
        read.signal
      )
      if (!current()) return
      if (
        page.sessionId !== session.sessionId ||
        page.executionId !== run.executionId ||
        page.contextVersion !== session.contextVersion
      )
        throw new Error('scope_changed')
      for (const event of page.events) {
        if (event.sequence <= cursor) continue
        if (event.type === 'TEXT') turn.answer += event.text ?? ''
        if (event.type === 'FAILED') {
          const key = aiFailureKey(event.error ?? null)
          failure.value = key === 'test_failed' ? tr('failed') : t(`ai_service.${key}`)
        }
      }
      cursor = page.cursor
      turn.status = page.status
      if (page.status !== 'RUNNING') {
        if (page.status === 'COMPLETED' && page.result) {
          turn.result = page.result
          turn.answer = page.result.text
        }
        if (page.status === 'FAILED') {
          failure.value ||= tr('failed')
          void refresh()
        }
        return
      }
      await new Promise(resolve => window.setTimeout(resolve, 500))
    }
  } catch (error) {
    if (current()) {
      turn.status = 'FAILED'
      fail(error)
    }
  } finally {
    if (current()) {
      busy.value = false
      request = undefined
    }
  }
}
const stop = async () => {
  if (!busy.value || !request) return
  const turn = turns.value.at(-1)
  if (!turn) return
  request.abort()
  turn.status = 'CANCELLED'
  const token = version
  try {
    if (turn.session && turn.executionId)
      await stopConversationExecution(turn.session.sessionId, turn.executionId)
  } catch (error) {
    if (token === version) fail(error)
  } finally {
    if (token === version) busy.value = false
  }
}
const apply = async (turn: Turn) => {
  const { session, executionId, result } = turn
  if (busy.value || applying.value || !session || !executionId || !result?.sql) return
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
    else if (draft.trim() && draft !== result.sql) warnings.push(tr('replace_draft'))
    if (warnings.length) {
      try {
        await ElMessageBox.confirm(warnings.join(' '), tr('editor_title'), {
          confirmButtonText: t('common.sure'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        })
      } catch {
        return
      }
    }
    if (token !== version) return
    const sql = await checkedSuggestion(turn, source)
    if (token !== version) return
    if (
      draft !== props.readDraft() ||
      source !== props.resourceId ||
      revision !== props.draftRevision
    ) {
      failure.value = tr('stale_draft')
      return
    }
    emit('apply', sql)
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
    {{ tr('editor_title') }}
  </el-button>
  <Teleport :to="host">
    <aside
      v-if="available && expanded"
      class="dataset-sql-assistant"
      data-testid="dataset-sql-assistant-panel"
      :aria-label="tr('editor_title')"
    >
      <strong>{{ tr('editor_title') }}</strong>
      <div class="conversation" role="log">
        <article v-for="(turn, index) in turns" :key="index">
          <p class="question">{{ turn.question }}</p>
          <p v-if="turn.answer">{{ turn.answer }}</p>
          <p v-if="turn.status === 'RUNNING'">{{ tr('running') }}</p>
          <p v-if="turn.status === 'CANCELLED'">{{ tr('incomplete') }}</p>
          <template v-if="turn.result?.type === 'SQL_SUGGESTION' && turn.result.sql">
            <small>{{ tr('numbered_suggestion') }} #{{ index + 1 }} · {{ tr('unrun_sql') }}</small>
            <details>
              <summary>{{ tr('generated_from') }} · {{ turn.basis }}</summary>
              <pre v-if="turn.baseSql">{{ turn.baseSql }}</pre>
              <small>{{ turn.session?.resourceName }} ({{ turn.session?.resourceId }})</small>
            </details>
            <pre>{{ turn.result.sql }}</pre>
            <el-button
              data-testid="dataset-sql-assistant-apply"
              :disabled="busy || applying"
              @click="apply(turn)"
              >{{ tr('apply') }}</el-button
            >
            <el-button
              text
              :disabled="busy || applying"
              data-testid="dataset-sql-assistant-reference"
              @click="selectedBasis = index + 1"
              >{{ tr('refer_to_suggestion') }}</el-button
            >
          </template>
        </article>
      </div>
      <p v-if="failure" role="alert">{{ failure }}</p>
      <div v-if="turns.length && !busy">
        <el-button
          v-if="selectedBasis === undefined"
          text
          data-testid="dataset-sql-assistant-new-query"
          @click="selectedBasis = 0"
          >{{ tr('new_query') }}</el-button
        >
        <el-tag v-else closable @close="selectedBasis = undefined">
          {{ selectedBasis ? tr('numbered_suggestion') + ' #' + selectedBasis : tr('new_query') }}
        </el-tag>
      </div>
      <el-input
        type="textarea"
        data-testid="dataset-sql-assistant-input"
        v-model="input"
        :aria-label="tr('message')"
        :placeholder="tr('message')"
        @keydown.ctrl.enter.prevent="send"
      />
      <el-button v-if="busy" data-testid="dataset-sql-assistant-stop" @click="stop">{{
        tr('stop')
      }}</el-button>
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
.dataset-sql-assistant {
  width: min(360px, 36vw);
  height: 100%;
  box-sizing: border-box;
  padding: 16px;
  border-left: 1px solid var(--ed-border-color);
  display: flex;
  flex-direction: column;
  gap: 12px;

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
