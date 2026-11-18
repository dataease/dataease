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
  readDraft: () => string
}>()
const emit = defineEmits<{
  (event: 'apply', query: string): void
  (event: 'invalidate-preview'): void
}>()
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
  cursor?: number
  executionId?: string
  result?: AiAssistanceResult
  status: string
}
const available = ref(false)
const expanded = ref(false)
const input = ref('')
const failure = ref('')
const turns = ref<Turn[]>([])
// Zero starts a new query. Positive numbers identify displayed turns.
const selectedBasis = ref<number | 'preview'>()
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
  emit('invalidate-preview')
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
  if (accessLost(error)) {
    emit('invalidate-preview')
    clearHistory()
  }
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
      (props.draftRevision !== preview.revision ? ' · ' + tr('preview_draft_changed') : '')
    return {
      label,
      sql,
      preview: {
        ...facts,
        currentResourceId: props.resourceId,
        currentIsCross: props.isCross,
        currentRevision: props.draftRevision
      }
    }
  }
  if (requested === 'preview') throw new Error('unavailable_reference')
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
watch(
  () => props.preview,
  () => {
    if (selectedBasis.value === 'preview') selectedBasis.value = undefined
  }
)
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
const applyPage = (turn: Turn, page: AiConversationEvents) => {
  if (
    page.sessionId !== turn.session?.sessionId ||
    page.executionId !== turn.executionId ||
    page.contextVersion !== turn.session.contextVersion
  )
    throw new Error('scope_changed')
  for (const event of page.events) {
    if (event.sequence <= (turn.cursor ?? 0)) continue
    if (event.type === 'TEXT') turn.answer += event.text ?? ''
    if (event.type === 'FAILED') {
      const key = aiFailureKey(event.error ?? null)
      failure.value = key === 'test_failed' ? tr('failed') : t(`ai_service.${key}`)
    }
  }
  turn.cursor = page.cursor
  turn.status = page.status
  if (page.status === 'COMPLETED' && page.result) {
    turn.result = page.result
    turn.answer = page.result.text
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
  const isCross = props.isCross
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
      resourceId: basis.preview?.resourceId ?? source,
      isCross: basis.preview?.isCross ?? isCross,
      preview: basis.preview,
      systemVariableNames: [...new Set(props.systemVariableNames)]
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
          <details v-if="turn.result">
            <summary>{{ tr('generated_from') }} · {{ turn.basis }}</summary>
            <pre v-if="turn.baseSql">{{ turn.baseSql }}</pre>
            <small>{{ turn.session?.resourceName }} ({{ turn.session?.resourceId }})</small>
          </details>
          <template v-if="turn.result?.type === 'SQL_SUGGESTION' && turn.result.sql">
            <small>{{ tr('numbered_suggestion') }} #{{ index + 1 }} · {{ tr('unrun_sql') }}</small>
            <pre>{{ turn.result.sql }}</pre>
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
      <div v-if="(turns.length || props.preview) && !busy">
        <el-button
          v-if="props.preview && selectedBasis === undefined"
          text
          @click="selectedBasis = 'preview'"
          >{{ tr('refer_to_preview') }}</el-button
        >
        <el-button
          v-if="selectedBasis === undefined"
          text
          data-testid="dataset-sql-assistant-new-query"
          @click="selectedBasis = 0"
          >{{ tr('new_query') }}</el-button
        >
        <el-tag v-else closable @close="selectedBasis = undefined">
          {{
            selectedBasis === 'preview'
              ? tr('refer_to_preview')
              : selectedBasis
              ? tr('numbered_suggestion') + ' #' + selectedBasis
              : tr('new_query')
          }}
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
