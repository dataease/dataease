<script setup lang="ts">
import BackgroundTasks from './BackgroundTasks.vue'
import { getAiTask, type AiTask, type AiTaskOrigin } from '@/api/aiTask'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave } from 'vue-router_2'
import { ElMessageBox } from 'element-plus-secondary'
import { isAxiosError } from 'axios'
import { isRequestError } from '@/config/axios/error'
import { aiFailureKey } from '@/api/aiService'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  closeConversation,
  retryConversationExecution,
  type AiRecoveryProgress,
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
    taskId?: string
    attemptId?: string
    tools?: AiConversationTool[]
    target?: AiConversationSession
    sourceSql?: string
    result?: AiAssistanceResult
    run?: AiConversationExecution
    retryKey?: string
    recovery?: AiRecoveryProgress
    cursor?: number
    errorCode?: string
  }[]
>([])
const busy = ref(false)
const failure = ref('')
const hidden = ref(false)
const execution = ref<AiConversationExecution>()
let polling: AbortController | undefined
let generation = 0
let resourceGeneration = 0
const editorId = crypto.randomUUID()
let conversationSql = ''
const backgroundDelivered = (task: AiTask, question: string, origin: AiTaskOrigin) => {
  const target = session.value
  if (
    !target ||
    target.sessionId !== origin.sessionId ||
    target.contextVersion !== origin.contextVersion ||
    !task.result
  )
    return
  messages.value.push({ role: 'user', text: question, status: 'COMPLETED' })
  messages.value.push({
    role: 'assistant',
    text: task.result.text,
    status: 'COMPLETED',
    result: task.result,
    target,
    sourceSql: conversationSql,
    taskId: task.taskId,
    attemptId: task.attemptId
  })
}
type Submission = {
  single: boolean
  text: string
  resourceId: string
  sql: string
  editorId: string
  openKey: string
  submissionKey: string
  expiresAt: number
  target?: AiConversationSession
  userIndex?: number
}
const pendingSubmissions = ref<Submission[]>([])
const pendingSubmission = computed(() => pendingSubmissions.value[0])
const matchingSubmission = (single: boolean) =>
  pendingSubmissions.value.find(
    item =>
      item.single === single &&
      item.text === (input.value.trim() || tr('single_request')) &&
      item.resourceId === resourceId.value &&
      item.sql === sql.value &&
      item.editorId === editorId
  )

const applying = ref(false)
const reset = () => {
  generation++
  pendingSubmissions.value = []
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
const waitForRead = (millis: number, signal?: AbortSignal) =>
  new Promise<void>(resolve => {
    const done = () => {
      window.clearTimeout(timer)
      signal?.removeEventListener('abort', done)
      resolve()
    }
    const timer = window.setTimeout(done, millis)
    signal?.addEventListener('abort', done, { once: true })
    if (signal?.aborted) done()
  })
const pollRun = async (
  run: AiConversationExecution,
  active: AiConversationSession,
  index: number,
  current: number
) => {
  let cursor = messages.value[index].cursor ?? 0
  let recoveries = 0
  let recoveryStarted = 0
  polling = new AbortController()
  while (current === generation && busy.value) {
    let events: AiConversationEvents
    try {
      const request = new AbortController()
      const parent = polling.signal
      const abort = () => request.abort()
      parent.addEventListener('abort', abort, { once: true })
      const timer = recoveryStarted
        ? window.setTimeout(abort, Math.max(1, 10000 - (Date.now() - recoveryStarted)))
        : undefined
      try {
        events = await getConversationEvents(
          active.sessionId,
          run.executionId,
          cursor,
          request.signal
        )
      } finally {
        window.clearTimeout(timer)
        parent.removeEventListener('abort', abort)
      }
    } catch (error) {
      if (current !== generation || polling.signal.aborted) return
      const status = isRequestError(error) ? error.response?.status : undefined
      if (status && status < 500) throw error
      recoveryStarted ||= Date.now()
      if (recoveries >= 3 || Date.now() - recoveryStarted >= 10000) {
        messages.value[index].status = 'UNREACHABLE'
        failure.value = tr('progress_unreachable')
        return
      }
      recoveries++
      failure.value = tr('progress_recovering') + ` (${recoveries}/3)`
      await waitForRead(
        Math.min(recoveries * 1000, Math.max(0, 10000 - (Date.now() - recoveryStarted))),
        polling.signal
      )
      continue
    }
    if (current !== generation || execution.value?.executionId !== run.executionId) return
    if (
      events.sessionId !== active.sessionId ||
      events.executionId !== run.executionId ||
      events.contextVersion !== active.contextVersion
    )
      throw new Error('scope_changed')
    failure.value = ''
    applyEvents(events, index, cursor)
    cursor = Math.max(cursor, events.cursor)
    messages.value[index].cursor = cursor
    if (events.status !== 'RUNNING') break
    await waitForRead(250, polling.signal)
  }
}
const observe = async (
  run: AiConversationExecution,
  active: AiConversationSession,
  sourceSql: string,
  current: number
) => {
  execution.value = run
  activity.value = active
  messages.value.push({
    role: 'assistant',
    text: '',
    status: 'RUNNING',
    executionId: run.executionId,
    target: active,
    sourceSql,
    tools: [],
    run
  })
  await pollRun(run, active, messages.value.length - 1, current)
}
const finishReading = (current: number) => {
  if (current !== generation) return
  busy.value = false
  if (messages.value[messages.value.length - 1]?.status !== 'UNREACHABLE')
    execution.value = undefined
  polling?.abort()
  polling = undefined
}
const send = async (single = false, resume = false) => {
  if (execution.value && !busy.value) return
  if (
    busy.value ||
    (!resume &&
      ((!single && !input.value.trim()) || (single && !resourceId.value && !input.value.trim())))
  )
    return
  const matched = resume ? pendingSubmission.value : matchingSubmission(single)
  const reconciling = Boolean(matched)
  const submission: Submission | undefined = matched ?? {
    single,
    text: input.value.trim() || tr('single_request'),
    resourceId: resourceId.value,
    sql: sql.value,
    editorId,
    openKey: crypto.randomUUID(),
    submissionKey: crypto.randomUUID(),
    expiresAt: Date.now() + 30 * 60 * 1000
  }
  if (!submission) return
  if (Date.now() >= submission.expiresAt) {
    pendingSubmissions.value = pendingSubmissions.value.filter(
      item => item.submissionKey !== submission.submissionKey
    )
    failure.value = tr('submission_expired')
    return
  }
  if (!matched) {
    if (pendingSubmissions.value.length >= 10) {
      failure.value = tr('submission_limit')
      return
    }
    pendingSubmissions.value.push(submission)
  }
  busy.value = true
  failure.value = ''
  const current = ++generation
  if (!reconciling || submission.userIndex == null) {
    messages.value.push({ role: 'user', text: submission.text, status: 'RUNNING' })
    submission.userIndex = messages.value.length - 1
  }
  const index = submission.userIndex
  messages.value[index].status = 'RUNNING'
  if (!reconciling) input.value = ''
  let active = submission.target ?? (submission.single ? undefined : session.value)
  if (active && (active.resourceId !== submission.resourceId || conversationSql !== submission.sql))
    active = undefined
  try {
    let run: AiConversationExecution
    if (submission.single) {
      run = await invokeSqlAssistance({
        scene: 'sql',
        resourceId: submission.resourceId,
        editorId: submission.editorId,
        sql: submission.sql,
        text: submission.text,
        submissionKey: submission.submissionKey
      })
      active = run.context
      if (!active) throw new Error('response_invalid')
      contexts.add(active.sessionId)
    } else {
      if (!active) {
        active = await openConversation({
          scene: 'sql',
          resourceId: submission.resourceId,
          editorId: submission.editorId,
          sql: submission.sql,
          openKey: submission.openKey
        })
        if (current !== generation) {
          void closeConversation(active.sessionId).catch(() => undefined)
          return
        }
        session.value = active
        conversationSql = submission.sql
        contexts.add(active.sessionId)
      }
      submission.expiresAt = Math.min(submission.expiresAt, active.expiresAt)
      submission.target = active
      run = await startConversationExecution(
        active.sessionId,
        active.contextVersion,
        submission.text,
        submission.submissionKey
      )
    }
    if (current !== generation) {
      void closeConversation(active.sessionId).catch(() => undefined)
      return
    }
    if (run.sessionId !== active.sessionId || run.contextVersion !== active.contextVersion)
      throw new Error('scope_changed')
    pendingSubmissions.value = pendingSubmissions.value.filter(
      item => item.submissionKey !== submission.submissionKey
    )
    messages.value[index].status = 'COMPLETED'
    await observe(run, active, submission.sql, current)
  } catch (error) {
    if (current !== generation || !busy.value) return
    const last = messages.value[messages.value.length - 1]
    // A failed read or lost acceptance response is not an authoritative server execution failure.
    if (last?.status === 'RUNNING') last.status = 'UNREACHABLE'
    const status = isRequestError(error) ? error.response?.status : undefined
    if (status && status < 500) {
      pendingSubmissions.value = pendingSubmissions.value.filter(
        item => item.submissionKey !== submission.submissionKey
      )
      messages.value[index].status = 'REJECTED'
      if (!reconciling && !input.value) input.value = submission.text
    }
    failure.value = pendingSubmissions.value.some(
      item => item.submissionKey === submission.submissionKey
    )
      ? tr('submission_unknown')
      : failureMessage(error)
  } finally {
    finishReading(current)
  }
}
const retry = async (index: number) => {
  const source = messages.value[index],
    target = source?.target,
    original = source?.run
  if (
    busy.value ||
    !target ||
    !original?.attemptId ||
    !original.retryEligible ||
    !source.executionId
  )
    return
  if (
    [
      'provider_authentication',
      'provider_balance',
      'configuration_unavailable',
      'model_unavailable',
      'disabled',
      'organization_disabled',
      'conflict'
    ].includes(source.errorCode ?? '')
  ) {
    try {
      await ElMessageBox.confirm(tr('retry_configuration_confirm'), tr('retry_attempt'), {
        type: 'warning',
        closeOnClickModal: false
      })
    } catch {
      return
    }
  }
  source.retryKey ||= crypto.randomUUID()
  busy.value = true
  failure.value = ''
  const current = ++generation
  try {
    const run = await retryConversationExecution(
      target.sessionId,
      source.executionId,
      original.attemptId,
      source.retryKey
    )
    if (current !== generation) return
    original.retryEligible = false
    await observe(run, target, source.sourceSql ?? '', current)
  } catch {
    if (current === generation) failure.value = tr('retry_unknown')
  } finally {
    finishReading(current)
  }
}
const resumeProgress = async (index: number) => {
  const message = messages.value[index]
  if (busy.value || !message?.run || !message.target) return
  busy.value = true
  failure.value = ''
  message.status = 'RUNNING'
  const current = ++generation
  execution.value = message.run
  try {
    await pollRun(message.run, message.target, index, current)
  } catch (error) {
    if (current === generation) {
      message.status = 'UNREACHABLE'
      failure.value = failureMessage(error)
    }
  } finally {
    finishReading(current)
  }
}
const copyExecution = (id: string) =>
  navigator.clipboard.writeText(id).catch(() => {
    failure.value = tr('copy_failed')
  })
const applyEvents = (events: AiConversationEvents, index: number, cursor: number) => {
  const message = messages.value[index]
  if (!message || !['RUNNING', 'UNREACHABLE'].includes(message.status)) return
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
      message.errorCode = event.error
      const key = aiFailureKey(event.error ?? null)
      failure.value = key === 'test_failed' ? tr('failed') : t(`ai_service.${key}`)
    }
  }
  message.status = events.status
  if (message.run) {
    message.run.retryEligible = events.retryEligible
    message.run.attemptId = events.attemptId ?? message.run.attemptId
  }
  message.recovery = events.recovery
  if (events.status === 'COMPLETED' && events.result) {
    message.result = events.result
    if (typeof events.result.text === 'string') message.text = events.result.text
  }
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
    (!message.executionId && !message.taskId) ||
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
    const task = message.taskId ? await getAiTask(message.taskId) : undefined
    if (task && (task.attemptId !== message.attemptId || task.status !== 'COMPLETED'))
      throw new Error('scope_changed')
    const checked =
      task?.result ??
      (await checkSqlApplication(
        target.sessionId,
        message.executionId!,
        target.contextVersion,
        target.draftVersion
      ))
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
    if (events.status === 'COMPLETED') {
      if (!busy.value) void resumeProgress(messages.value.length - 1)
      return
    }
    if (events.status === 'RUNNING') throw new Error('response_invalid')
    applyEvents(
      events,
      messages.value.length - 1,
      messages.value[messages.value.length - 1]?.cursor ?? 0
    )
    const message = messages.value[messages.value.length - 1]
    if (message?.status === 'RUNNING') message.status = events.status
    busy.value = false
    polling?.abort()
    execution.value = undefined
  } catch (error) {
    if (current !== generation) return
    const message = messages.value[messages.value.length - 1]
    if (message?.status === 'RUNNING') message.status = 'UNREACHABLE'
    failure.value = tr('progress_unreachable')
    busy.value = false
    polling?.abort()
    // An unknown stop response can be reconciled by reading this same execution.
  }
}
const offline = () => {
  if (busy.value) failure.value = tr('progress_recovering')
}
onMounted(() => {
  void load()
  window.addEventListener('pagehide', reset)
  window.addEventListener('beforeunload', beforeUnload)
  window.addEventListener('offline', offline)
})
onBeforeUnmount(() => {
  resourceGeneration++
  window.removeEventListener('pagehide', reset)
  window.removeEventListener('beforeunload', beforeUnload)
  window.removeEventListener('offline', offline)
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
      <el-button
        :disabled="
          busy || !!execution || !!matchingSubmission(true) || (!resourceId && !input.trim())
        "
        @click="send(true)"
        >{{ tr('single') }}</el-button
      >
      <BackgroundTasks
        :resource-id="resourceId"
        :editor-id="editorId"
        :sql="sql"
        :text="input"
        :origin="
          session && session.resourceId === resourceId && conversationSql === sql
            ? { sessionId: session.sessionId, contextVersion: session.contextVersion }
            : undefined
        "
        @delivered="backgroundDelivered"
      />
      <el-button @click="end">{{ tr('end') }}</el-button>
      <el-button v-if="busy || execution" :disabled="!execution" @click="stop">{{
        tr('stop')
      }}</el-button>
      <p role="status">{{ busy ? tr('running') : tr('ready') }}</p>
      <p v-if="failure" role="alert">{{ failure }}</p>
      <el-button
        v-if="pendingSubmission"
        :disabled="busy"
        @click="send(pendingSubmission.single, true)"
        >{{ tr('resume_submission') }}</el-button
      >
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
          <p v-if="message.role === 'assistant' && message.status === 'UNREACHABLE'">
            {{ tr('progress_unreachable') }}
          </p>
          <el-button
            v-if="message.status === 'UNREACHABLE' && message.run"
            :disabled="busy"
            @click="resumeProgress(index)"
            >{{ tr('resume_progress') }}</el-button
          >
          <el-button v-if="message.run?.retryEligible" :disabled="busy" @click="retry(index)">{{
            tr('retry_attempt')
          }}</el-button>
          <p v-if="message.recovery">
            {{ tr('current_step') }}: {{ message.recovery.stepKind }} ·
            {{ tr('technical_retries') }} {{ message.recovery.technicalRetries }} ·
            {{ tr('corrections') }} {{ message.recovery.corrections
            }}<span v-if="message.recovery.reason"> · {{ message.recovery.reason }}</span>
          </p>
          <small v-if="message.executionId">{{ message.executionId }}</small>
          <el-button v-if="message.executionId" link @click="copyExecution(message.executionId)">{{
            tr('copy_execution')
          }}</el-button>
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
        <el-button
          type="primary"
          native-type="submit"
          :disabled="busy || !!execution || !!matchingSubmission(false) || !input.trim()"
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
