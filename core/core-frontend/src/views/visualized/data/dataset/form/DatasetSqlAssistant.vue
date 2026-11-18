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

const props = defineProps<{ host: HTMLElement; resourceId: string; readDraft: () => string }>()
const emit = defineEmits<{ (event: 'apply', query: string): void }>()
const { t } = useI18n()
const tr = (key: string) => t(`ai_conversation.${key}`)
const user = useUserStoreWithOut()
const editorId = uuid.v4()
interface Turn {
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
const busy = ref(false)
const applying = ref(false)
const sessions = new Set<string>()
let latestSession: AiConversationSession | undefined
let request: AbortController | undefined
let availabilityRead: AbortController | undefined
let availabilityTimer: number | undefined
let version = 0
let disposed = false

const close = (id: string) => void closeConversation(id).catch(() => undefined)
const reset = () => {
  version++
  request?.abort()
  request = undefined
  sessions.forEach(close)
  sessions.clear()
  latestSession = undefined
  turns.value = []
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

const fail = (error: unknown) => {
  failure.value =
    isRequestError(error) && error.response?.status === 403
      ? t('ai_service.failure_forbidden')
      : tr('failed')
  void refresh()
}
const send = async () => {
  const question = input.value.trim()
  if (!question || busy.value || applying.value || !available.value) return
  const token = version
  const read = new AbortController()
  request = read
  const current = () => token === version && !read.signal.aborted
  const index = turns.value.push({ question, answer: '', status: 'RUNNING' }) - 1
  const turn = turns.value[index]
  busy.value = true
  failure.value = input.value = ''
  try {
    const session = await openConversation({
      scene: 'dataset-sql',
      resourceId: props.resourceId,
      editorId,
      sql: props.readDraft(),
      openKey: uuid.v4(),
      previousSessionId: latestSession?.sessionId
    })
    if (!current()) {
      close(session.sessionId)
      return
    }
    turn.session = latestSession = session
    sessions.add(session.sessionId)
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
  applying.value = true
  failure.value = ''
  try {
    if (source !== session.resourceId) {
      failure.value = tr('stale_draft')
      return
    }
    if (draft.trim() && draft !== result.sql) {
      try {
        await ElMessageBox.confirm(tr('replace_draft'), tr('editor_title'), {
          confirmButtonText: t('common.sure'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        })
      } catch {
        return
      }
    }
    if (token !== version) return
    const checked = await checkSqlApplication(
      session.sessionId,
      executionId,
      session.contextVersion,
      session.draftVersion
    )
    if (token !== version) return
    if (
      draft !== props.readDraft() ||
      source !== props.resourceId ||
      checked.type !== 'SQL_SUGGESTION' ||
      checked.sql !== result.sql ||
      checked.editorId !== editorId ||
      checked.resourceVersion !== session.resourceVersion ||
      checked.resourceId !== source ||
      checked.draftVersion !== session.draftVersion
    ) {
      failure.value = tr('stale_draft')
      return
    }
    emit('apply', checked.sql)
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
            <small>{{ tr('unrun_sql') }}</small>
            <pre>{{ turn.result.sql }}</pre>
            <el-button
              data-testid="dataset-sql-assistant-apply"
              :disabled="busy || applying"
              @click="apply(turn)"
              >{{ tr('apply') }}</el-button
            >
          </template>
        </article>
      </div>
      <p v-if="failure" role="alert">{{ failure }}</p>
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
