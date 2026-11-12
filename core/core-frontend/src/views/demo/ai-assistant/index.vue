<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router_2'
import { ElMessageBox } from 'element-plus-secondary'
import { isAxiosError } from 'axios'
import { aiFailureKey } from '@/api/aiService'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  closeConversation,
  getConversationResources,
  openConversation,
  invokeSqlAssistance,
  checkSqlApplication,
  type AiAssistanceResult,
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
const guideSections = [
  { title: 'guide_capabilities', items: ['guide_dialogue', 'guide_metadata', 'guide_validation'] },
  {
    title: 'guide_boundaries',
    items: ['guide_structure_only', 'guide_sql_scope', 'guide_verdict']
  },
  {
    title: 'guide_notices',
    items: ['guide_setup', 'guide_data', 'guide_evidence', 'guide_budget']
  },
  { title: 'guide_lifecycle', items: ['guide_controls', 'guide_session'] }
]
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
const validationDetails = (tool: AiConversationTool) => {
  if (
    !['validate_sql', 'propose_sql'].includes(tool.name) ||
    tool.status !== 'SUCCEEDED' ||
    !tool.result
  )
    return
  let status = 'unknown'
  let code = 'unknown'
  try {
    const result: unknown = JSON.parse(tool.result)
    if (result && typeof result === 'object' && 'status' in result && 'code' in result) {
      const pairs = [
        ['PASS', 'verified'],
        ['INVALID', 'reference'],
        ['UNSUPPORTED', 'parse'],
        ['UNSUPPORTED', 'subset'],
        ['UNSUPPORTED', 'dialect']
      ]
      if (pairs.some(([verdict, reason]) => result.status === verdict && result.code === reason)) {
        status = String(result.status)
        code = String(result.code)
      }
    }
  } catch {
    // Unrecognized results remain escaped evidence, never translated as a trusted verdict.
  }
  return {
    verdict:
      tr(`validation_${status.toLowerCase()}`) + (status === 'unknown' ? '' : ` (${status})`),
    reason: tr(`validation_${code}`),
    scope: tr(status === 'unknown' ? 'validation_unknown_scope' : 'validation_scope')
  }
}
const user = useUserStoreWithOut()
const resources = ref<AiConversationResource[]>([])
const resourceId = ref('')
const sql = ref('')
const input = ref('')
const session = ref<AiConversationSession>()
const activity = ref<AiConversationSession>()
const contexts = new Set<string>()
const messages = ref<
  {
    role: 'user' | 'assistant'
    text: string
    status: string
    executionId?: string
    tools?: AiConversationTool[]
    target?: AiConversationSession
    sourceSql?: string
    result?: AiAssistanceResult
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
let conversationSql = ''
const applying = ref(false)
const reset = () => {
  generation++
  polling?.abort()
  polling = undefined
  execution.value = undefined
  const previous = [...contexts]
  contexts.clear()
  session.value = undefined
  activity.value = undefined
  messages.value = []
  busy.value = false
  failure.value = ''
  editorId = crypto.randomUUID()
  for (const id of previous) void closeConversation(id).catch(() => undefined)
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
watch(
  () => [user.uid, user.oid, user.proxyInfo?.proxyOid],
  () => {
    reset()
    resourceId.value = ''
    sql.value = ''
    input.value = ''
    resources.value = []
    void load()
  },
  { flush: 'sync' }
)
const send = async (single = false) => {
  if (
    busy.value ||
    (!single && !input.value.trim()) ||
    (single && !resourceId.value && !input.value.trim())
  )
    return
  busy.value = true
  failure.value = ''
  const current = ++generation
  const text = input.value.trim() || tr('single_request')
  const selectedResource = resourceId.value,
    selectedSql = sql.value
  const message = { role: 'user' as const, text, status: 'RUNNING' }
  messages.value.push(message)
  const index = messages.value.length - 1
  input.value = ''
  let active: AiConversationSession | undefined
  try {
    active = single ? undefined : session.value
    if (active && (active.resourceId !== selectedResource || conversationSql !== selectedSql))
      active = undefined
    let run: AiConversationExecution
    if (single) {
      run = await invokeSqlAssistance({
        scene: 'sql',
        resourceId: selectedResource,
        editorId,
        sql: selectedSql,
        text
      })
      active = run.context
      if (!active) throw new Error('response_invalid')
      contexts.add(active.sessionId)
    } else if (!active) {
      active = await openConversation({
        scene: 'sql',
        resourceId: selectedResource,
        editorId,
        sql: selectedSql
      })
      if (current !== generation) {
        void closeConversation(active.sessionId).catch(() => undefined)
        return
      }
      session.value = active
      conversationSql = selectedSql
      contexts.add(active.sessionId)
    }
    if (!single)
      run = await startConversationExecution(active.sessionId, active.contextVersion, text)
    if (current !== generation) {
      void closeConversation(active.sessionId).catch(() => undefined)
      return
    }
    if (run.sessionId !== active.sessionId || run.contextVersion !== active.contextVersion)
      throw new Error('scope_changed')
    execution.value = run
    activity.value = active
    messages.value[index].status = 'COMPLETED'
    messages.value.push({
      role: 'assistant',
      text: '',
      status: 'RUNNING',
      executionId: run.executionId,
      target: active,
      sourceSql: selectedSql,
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
    if (active && session.value?.sessionId === active.sessionId) session.value = undefined
    execution.value = undefined
    if (active) {
      contexts.delete(active.sessionId)
      void closeConversation(active.sessionId).catch(() => undefined)
    }
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
  if (events.status === 'COMPLETED' && events.result) message.result = events.result
}
const applySuggestion = async (index: number) => {
  const message = messages.value[index],
    target = message?.target,
    result = message?.result
  if (
    busy.value ||
    applying.value ||
    !target ||
    !result ||
    !message.executionId ||
    result.type !== 'SQL_SUGGESTION'
  )
    return
  const current = generation
  const sameDraft = () =>
    current === generation &&
    resourceId.value === target.resourceId &&
    sql.value === message.sourceSql
  failure.value = ''
  if (!sameDraft()) {
    failure.value = tr('stale_draft')
    return
  }
  applying.value = true
  try {
    const checked = await checkSqlApplication(
      target.sessionId,
      message.executionId,
      target.contextVersion,
      target.draftVersion
    )
    if (current !== generation) return
    if (
      !sameDraft() ||
      checked.type !== 'SQL_SUGGESTION' ||
      !checked.sql ||
      checked.sql !== result.sql ||
      checked.resourceId !== target.resourceId ||
      checked.resourceVersion !== target.resourceVersion ||
      checked.editorId !== target.editorId ||
      checked.draftVersion !== target.draftVersion
    ) {
      failure.value = tr('stale_draft')
      return
    }
    sql.value = checked.sql
  } catch (error) {
    if (current === generation) failure.value = failureMessage(error)
  } finally {
    applying.value = false
  }
}
const confirmInterruption = async () => {
  if (!busy.value) return true
  try {
    await ElMessageBox.confirm(tr('interrupt_message'), tr('interrupt_title'), {
      confirmButtonText: tr('interrupt'),
      cancelButtonText: tr('keep_running'),
      closeOnClickModal: false,
      type: 'warning'
    })
    return true
  } catch {
    return false
  }
}
const end = async () => {
  if (await confirmInterruption()) reset()
}
onBeforeRouteLeave(confirmInterruption)
const beforeUnload = (event: BeforeUnloadEvent) => {
  if (busy.value) {
    event.preventDefault()
    event.returnValue = ''
  }
}
const stop = async () => {
  if (!(await confirmInterruption())) return
  const active = activity.value,
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
    if (session.value?.sessionId === active.sessionId) session.value = undefined
    execution.value = undefined
    void closeConversation(active.sessionId).catch(() => undefined)
  }
}
const disconnect = () => {
  const previous = [...contexts]
  contexts.clear()
  generation++
  polling?.abort()
  polling = undefined
  execution.value = undefined
  session.value = undefined
  activity.value = undefined
  busy.value = false
  for (const message of messages.value) if (message.status === 'RUNNING') message.status = 'FAILED'
  failure.value = tr('failed')
  for (const id of previous) void closeConversation(id).catch(() => undefined)
}
onMounted(() => {
  void load()
  window.addEventListener('pagehide', reset)
  window.addEventListener('beforeunload', beforeUnload)
  window.addEventListener('offline', disconnect)
})
onBeforeUnmount(() => {
  resourceGeneration++
  window.removeEventListener('pagehide', reset)
  window.removeEventListener('beforeunload', beforeUnload)
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
      <details class="demo-guide" open>
        <summary>{{ tr('guide_title') }}</summary>
        <div class="guide-sections">
          <section v-for="guide in guideSections" :key="guide.title">
            <h2>{{ tr(guide.title) }}</h2>
            <ul>
              <li v-for="item in guide.items" :key="item">{{ tr(item) }}</li>
            </ul>
          </section>
        </div>
      </details>
      <el-form label-position="top">
        <el-form-item :label="tr('resource')">
          <el-select v-model="resourceId" :aria-label="tr('resource')" filterable clearable>
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
      <p v-if="!resourceId">{{ tr('select_target') }}</p>
      <p v-if="activity" class="context">
        {{ activity.resourceName }} · {{ activity.editorId }} · {{ activity.resourceVersion }}
      </p>
      <el-button :disabled="busy || (!resourceId && !input.trim())" @click="send(true)">{{
        tr('single')
      }}</el-button>
      <el-button @click="end">{{ tr('end') }}</el-button>
      <el-button v-if="busy" :disabled="!execution" @click="stop">{{ tr('stop') }}</el-button>
      <p role="status">{{ busy ? tr('running') : tr('ready') }}</p>
      <p v-if="failure" role="alert">{{ failure }}</p>
      <section class="messages" :aria-label="tr('conversation')" aria-live="polite">
        <article v-for="(message, index) in messages" :key="index">
          <strong>{{ tr(message.role) }}</strong>
          <p v-if="message.target" class="context">
            {{ tr('original_target') }}: {{ message.target.resourceName || tr('no_resource') }} ·
            {{ message.target.resourceVersion }} · {{ message.target.draftVersion }}
          </p>
          <span v-if="message.status === 'FAILED' || message.status === 'CANCELLED'">
            · {{ tr('incomplete') }}</span
          >
          <pre>{{ message.text }}</pre>
          <p v-if="message.result">
            {{ tr(message.result.type === 'SQL_SUGGESTION' ? 'suggestion' : 'explanation') }}
          </p>
          <pre v-if="message.result?.type === 'SQL_SUGGESTION'">{{ message.result.sql }}</pre>
          <el-button
            v-if="message.result?.type === 'SQL_SUGGESTION'"
            :disabled="busy || applying"
            @click="applySuggestion(index)"
            >{{ tr('apply') }}</el-button
          >
          <details
            v-for="tool in message.tools"
            :key="tool.callId"
            :data-tool-id="tool.callId"
            open
          >
            <summary>
              {{ tr('tool') }} · {{ tool.name }} · {{ tr(`tool_${tool.status.toLowerCase()}`) }}
              <span v-if="validationDetails(tool)"> · {{ validationDetails(tool)?.verdict }}</span>
              <span
                v-if="message.status !== 'RUNNING' && ['STARTED', 'RUNNING'].includes(tool.status)"
              >
                · {{ tr('incomplete') }}
              </span>
            </summary>
            <template v-if="validationDetails(tool)">
              <p>{{ validationDetails(tool)?.reason }}</p>
              <p class="validation-scope">{{ validationDetails(tool)?.scope }}</p>
            </template>
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
      <el-form @submit.prevent="send()">
        <el-form-item :label="tr('message')">
          <el-input
            v-model="input"
            type="textarea"
            :rows="3"
            :maxlength="30000"
            :aria-label="tr('message')"
          />
        </el-form-item>
        <el-button type="primary" native-type="submit" :disabled="busy || !input.trim()">{{
          tr('send')
        }}</el-button>
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

.demo-guide {
  padding: 16px;
  margin: 12px 0 16px;
  border: 1px solid var(--ed-border-color);
  border-radius: 6px;
}

.demo-guide summary {
  font-weight: 600;
  cursor: pointer;
}

.guide-sections {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.guide-sections h2 {
  margin: 0 0 8px;
  font-size: 14px;
}

.guide-sections ul {
  padding-left: 20px;
  margin: 0;
}

.guide-sections li {
  margin: 4px 0;
  line-height: 1.6;
  overflow-wrap: anywhere;
}

@media (width <= 680px) {
  .guide-sections {
    grid-template-columns: 1fr;
  }
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
