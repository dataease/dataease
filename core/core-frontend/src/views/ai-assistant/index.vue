<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { isAxiosError } from 'axios'
import { aiFailureKey } from '@/api/aiService'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  closeConversation,
  getConversationResources,
  openConversation,
  startConversationExecution,
  getConversationEvents,
  stopConversationExecution,
  type AiConversationExecution,
  type AiConversationEvents,
  type AiConversationResource,
  type AiConversationTool,
  type AiConversationSession
} from '@/api/aiConversation'

const { t } = useI18n()
const tr = (key: string) => t(`ai_conversation.${key}`)
const failureMessage = (error: unknown) => {
  if (typeof error === 'string') return error
  if (isAxiosError(error)) {
    if (error.response?.status === 403) return t('ai_service.failure_forbidden')
    if (error.response?.status === 409) return t('ai_service.failure_conflict')
    if (error.code === 'ECONNABORTED') return t('ai_service.failure_timeout')
    if (error.code === 'ERR_NETWORK') return t('ai_service.failure_connection_failed')
  }
  return tr('failed')
}
const user = useUserStoreWithOut()
const resources = ref<AiConversationResource[]>([])
const resourceId = ref('')
const sql = ref('')
const input = ref('')
const session = ref<AiConversationSession>()
const messages = ref<
  {
    role: 'user' | 'assistant'
    text: string
    status: string
    executionId?: string
    tools?: AiConversationTool[]
  }[]
>([])
const busy = ref(false)
const failure = ref('')
const hidden = ref(false)
const execution = ref<AiConversationExecution>()
let polling: AbortController | undefined
let generation = 0
let resourceGeneration = 0
let editorId = crypto.randomUUID()
const reset = () => {
  generation++
  polling?.abort()
  polling = undefined
  execution.value = undefined
  const previous = session.value
  session.value = undefined
  messages.value = []
  busy.value = false
  failure.value = ''
  editorId = crypto.randomUUID()
  if (previous) void closeConversation(previous.sessionId).catch(() => undefined)
}
const load = async () => {
  const current = ++resourceGeneration
  try {
    const result = await getConversationResources()
    if (current === resourceGeneration) resources.value = result
  } catch (error) {
    if (current === resourceGeneration) failure.value = failureMessage(error)
  }
}
watch([resourceId, sql], reset, { flush: 'sync' })
watch(
  () => [user.uid, user.oid, user.proxyInfo?.proxyOid],
  () => {
    reset()
    resourceId.value = ''
    resources.value = []
    void load()
  },
  { flush: 'sync' }
)
const send = async () => {
  if (busy.value || !resourceId.value || !input.value.trim()) return
  busy.value = true
  failure.value = ''
  const current = ++generation
  const text = input.value
  const message = { role: 'user' as const, text, status: 'RUNNING' }
  messages.value.push(message)
  const index = messages.value.length - 1
  input.value = ''
  try {
    let active = session.value
    if (!active) {
      active = await openConversation({
        scene: 'sql',
        resourceId: resourceId.value,
        editorId,
        sql: sql.value
      })
      if (current !== generation) {
        void closeConversation(active.sessionId).catch(() => undefined)
        return
      }
      session.value = active
    }
    const run = await startConversationExecution(active.sessionId, active.contextVersion, text)
    if (current !== generation) {
      void closeConversation(active.sessionId).catch(() => undefined)
      return
    }
    if (run.sessionId !== active.sessionId || run.contextVersion !== active.contextVersion)
      throw new Error('scope_changed')
    execution.value = run
    messages.value[index].status = 'COMPLETED'
    messages.value.push({
      role: 'assistant',
      text: '',
      status: 'RUNNING',
      executionId: run.executionId,
      tools: []
    })
    const answerIndex = messages.value.length - 1
    let cursor = 0
    polling = new AbortController()
    while (current === generation && busy.value) {
      const events = await getConversationEvents(
        active.sessionId,
        run.executionId,
        cursor,
        polling.signal
      )
      if (current !== generation || execution.value?.executionId !== run.executionId) return
      if (
        events.sessionId !== active.sessionId ||
        events.executionId !== run.executionId ||
        events.contextVersion !== active.contextVersion
      )
        throw new Error('scope_changed')
      applyEvents(events, answerIndex, cursor)
      cursor = Math.max(cursor, events.cursor)
      if (events.status !== 'RUNNING') break
      await new Promise<void>(resolve => {
        const signal = polling?.signal
        const done = () => {
          window.clearTimeout(timer)
          signal?.removeEventListener('abort', done)
          resolve()
        }
        const timer = window.setTimeout(done, 250)
        signal?.addEventListener('abort', done, { once: true })
        if (signal?.aborted) done()
      })
    }
  } catch (error) {
    if (current !== generation || !busy.value) return
    const last = messages.value[messages.value.length - 1]
    if (last?.status === 'RUNNING') last.status = 'FAILED'
    if (last?.role !== 'assistant') messages.value[index].status = 'FAILED'
    const previous = session.value
    session.value = undefined
    execution.value = undefined
    if (previous) void closeConversation(previous.sessionId).catch(() => undefined)
    failure.value = failureMessage(error)
  } finally {
    if (current === generation) {
      busy.value = false
      execution.value = undefined
      polling?.abort()
      polling = undefined
    }
  }
}
const applyEvents = (events: AiConversationEvents, index: number, cursor: number) => {
  const message = messages.value[index]
  if (!message || message.status !== 'RUNNING') return
  for (const event of events.events) {
    if (event.sequence <= cursor) continue
    cursor = event.sequence
    if (event.type === 'TEXT' && event.text) message.text += event.text
    else if (event.type === 'TOOL' && event.tool) {
      const tool = event.tool
      if (!['STARTED', 'RUNNING', 'SUCCEEDED', 'FAILED'].includes(tool.status)) continue
      const tools = (message.tools ??= [])
      const existing = tools.findIndex(item => item.callId === tool.callId)
      if (existing >= 0) tools[existing] = tool
      else tools.push(tool)
    } else if (event.type === 'FAILED') {
      const key = aiFailureKey(event.error ?? null)
      failure.value = key === 'test_failed' ? tr('failed') : t(`ai_service.${key}`)
    }
  }
  message.status = events.status
}
const stop = async () => {
  const active = session.value,
    run = execution.value,
    current = generation
  if (!active || !run) return
  try {
    const events = await stopConversationExecution(active.sessionId, run.executionId)
    if (current !== generation || execution.value?.executionId !== run.executionId) return
    if (
      events.sessionId !== active.sessionId ||
      events.executionId !== run.executionId ||
      events.contextVersion !== active.contextVersion
    )
      throw new Error('scope_changed')
    // Already completed: the normal cursor poll must drain any text missed before stop.
    if (events.status === 'COMPLETED') return
    if (events.status === 'RUNNING') throw new Error('response_invalid')
    if (events.status === 'FAILED') applyEvents(events, messages.value.length - 1, 0)
    const message = messages.value[messages.value.length - 1]
    if (message?.status === 'RUNNING') message.status = events.status
    busy.value = false
    polling?.abort()
  } catch (error) {
    if (current !== generation) return
    const message = messages.value[messages.value.length - 1]
    if (message?.status === 'RUNNING') message.status = 'FAILED'
    failure.value = failureMessage(error)
    busy.value = false
    polling?.abort()
    session.value = undefined
    execution.value = undefined
    void closeConversation(active.sessionId).catch(() => undefined)
  }
}
const disconnect = () => {
  const active = session.value
  generation++
  polling?.abort()
  polling = undefined
  execution.value = undefined
  session.value = undefined
  busy.value = false
  for (const message of messages.value) if (message.status === 'RUNNING') message.status = 'FAILED'
  failure.value = tr('failed')
  if (active) void closeConversation(active.sessionId).catch(() => undefined)
}
onMounted(() => {
  void load()
  window.addEventListener('pagehide', reset)
  window.addEventListener('offline', disconnect)
})
onBeforeUnmount(() => {
  resourceGeneration++
  window.removeEventListener('pagehide', reset)
  window.removeEventListener('offline', disconnect)
  reset()
})
</script>

<template>
  <main class="ai-conversation">
    <h1>{{ tr('title') }}</h1>
    <p>{{ tr('description') }}</p>
    <el-button @click="hidden = !hidden">{{ hidden ? tr('expand') : tr('hide') }}</el-button>
    <div v-show="!hidden">
      <el-form label-position="top">
        <el-form-item :label="tr('resource')">
          <el-select v-model="resourceId" :aria-label="tr('resource')" filterable>
            <el-option
              v-for="resource in resources"
              :key="resource.id"
              :value="resource.id"
              :label="resource.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="tr('sql')">
          <el-input
            v-model="sql"
            type="textarea"
            :rows="4"
            :maxlength="30000"
            :aria-label="tr('sql')"
          />
        </el-form-item>
      </el-form>
      <p v-if="session" class="context">{{ session.resourceName }} · {{ session.editorId }}</p>
      <el-button @click="reset">{{ tr('end') }}</el-button>
      <el-button v-if="busy" :disabled="!execution" @click="stop">{{ tr('stop') }}</el-button>
      <p role="status">{{ busy ? tr('running') : tr('ready') }}</p>
      <p v-if="failure" role="alert">{{ failure }}</p>
      <section class="messages" :aria-label="tr('conversation')" aria-live="polite">
        <article v-for="(message, index) in messages" :key="index">
          <strong>{{ tr(message.role) }}</strong>
          <span v-if="message.status === 'FAILED' || message.status === 'CANCELLED'">
            · {{ tr('incomplete') }}</span
          >
          <pre>{{ message.text }}</pre>
          <details
            v-for="tool in message.tools"
            :key="tool.callId"
            :data-tool-id="tool.callId"
            open
          >
            <summary>
              {{ tr('tool') }} · {{ tool.name }} · {{ tr(`tool_${tool.status.toLowerCase()}`) }}
              <span
                v-if="message.status !== 'RUNNING' && ['STARTED', 'RUNNING'].includes(tool.status)"
              >
                · {{ tr('incomplete') }}
              </span>
            </summary>
            <div>{{ tr('tool_parameters') }}</div>
            <pre>{{ JSON.stringify(tool.parameters, null, 2) }}</pre>
            <template v-if="tool.result">
              <div>{{ tr('tool_result') }}</div>
              <pre>{{ tool.result }}</pre>
            </template>
          </details>
          <small v-if="message.executionId">{{ message.executionId }}</small>
        </article>
      </section>
      <el-form @submit.prevent="send">
        <el-form-item :label="tr('message')">
          <el-input
            v-model="input"
            type="textarea"
            :rows="3"
            :maxlength="30000"
            :aria-label="tr('message')"
          />
        </el-form-item>
        <el-button
          type="primary"
          native-type="submit"
          :disabled="busy || !resourceId || !input.trim()"
          >{{ tr('send') }}</el-button
        >
      </el-form>
    </div>
  </main>
</template>

<style scoped>
.ai-conversation {
  max-width: 960px;
  padding: 24px;
  margin: auto;
  color: var(--ed-text-color-primary);
}

.context,
small {
  overflow-wrap: anywhere;
  color: var(--ed-text-color-secondary);
}

.messages article {
  padding: 16px;
  margin: 12px 0;
  border: 1px solid var(--ed-border-color);
  border-radius: 6px;
}

pre {
  font: inherit;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

[role='alert'] {
  color: var(--ed-color-danger);
}
</style>
