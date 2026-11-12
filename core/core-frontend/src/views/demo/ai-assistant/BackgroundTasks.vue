<script setup lang="ts">
import { h, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  activeAiTasks,
  pendingAiTaskNotices,
  acknowledgeAiTaskNotice,
  deliverAiTaskConversation,
  type AiTaskNotice,
  type AiTaskOrigin,
  cancelAiTask,
  getAiTask,
  retryAiTask,
  submitAiTask,
  type AiTask,
  type AiTaskSubmit
} from '@/api/aiTask'

const props = defineProps<{
  resourceId: string
  editorId: string
  sql: string
  text: string
  origin?: AiTaskOrigin
}>()
const emit = defineEmits<{ delivered: [task: AiTask, question: string, origin: AiTaskOrigin] }>()
const conversationDelivered = new Set<string>()
const origins = new Map<string, { origin: AiTaskOrigin; question: string }>()
const { t } = useI18n()
const tr = (key: string) => t(`ai_conversation.${key}`)
const requestFailure = (error: unknown) => {
  const key = error && typeof error === 'object' && 'messageKey' in error ? error.messageKey : null
  if (key === 'i18n_ai_task_queue_full') return t('ai_service.failure_task_queue_full')
  if (key === 'i18n_ai_task_user_queue_full') return t('ai_service.failure_task_user_queue_full')
  return tr('task_request_failed')
}
const user = useUserStoreWithOut()
const tasks = ref<AiTask[]>([])
const pending = ref(false)
const failure = ref('')
const loadingFailed = ref(false)
let generation = 0
let timer: ReturnType<typeof setTimeout> | undefined
let submission: { fingerprint: string; value: AiTaskSubmit; taskId?: string } | undefined
const retryKeys = new Map<string, string>()
const active = (task: AiTask) => ['QUEUED', 'RUNNING'].includes(task.status)
const finished = new Set<string>()
const toasts = new Map<string, ReturnType<typeof ElMessage>>()
const closeToasts = () => {
  toasts.forEach(toast => toast.close())
  toasts.clear()
  finished.clear()
}
// Delivery IDs only: no result bodies, questions, chat state or user-facing message list.
const firstToast = (task: AiTask | AiTaskNotice) => {
  const key = 'de-ai-toasts:' + JSON.stringify([user.uid, user.oid, user.proxyInfo?.proxyOid])
  try {
    const stored: unknown = JSON.parse(localStorage.getItem(key) || '[]')
    const now = Date.now()
    const entries: [string, number][] = Array.isArray(stored)
      ? stored.filter(
          (entry): entry is [string, number] =>
            Array.isArray(entry) &&
            typeof entry[0] === 'string' &&
            /^\d{1,20}$/.test(entry[0]) &&
            typeof entry[1] === 'number' &&
            entry[1] > now
        )
      : []
    const seen = entries.some(([id]) => id === task.attemptId)
    if (!seen)
      entries.push([
        task.attemptId,
        'expiresAt' in task ? task.expiresAt : (task.finishedAt || now) + 7 * 86400000
      ])
    localStorage.setItem(key, JSON.stringify(entries.slice(-1000)))
    return !seen
  } catch {
    // Browsers that block storage still have the durable server ack and mounted-page deduplication.
    return true
  }
}
const notify = (task: AiTask | AiTaskNotice) => {
  if (finished.has(task.attemptId)) return
  finished.add(task.attemptId)
  if (finished.size > 50) finished.delete(finished.values().next().value!)
  if (!firstToast(task)) return
  if (toasts.size >= 10) {
    const oldest = toasts.keys().next().value!
    toasts.get(oldest)?.close()
    toasts.delete(oldest)
  }
  const current = generation
  const retry = ['FAILED', 'CANCELLED', 'INTERRUPTED'].includes(task.status)
  const message =
    task.status === 'COMPLETED'
      ? tr('task_completed')
      : tr(`task_status_${task.status.toLowerCase()}`)
  const content = retry
    ? h('span', [
        message + ' ',
        h(
          'button',
          {
            type: 'button',
            onClick: async () => {
              if (current !== generation) return
              if (await change(task, true)) toasts.get(task.attemptId)?.close()
            }
          },
          tr('task_retry')
        )
      ])
    : message + ' ' + tr('task_expiry')
  toasts.set(
    task.attemptId,
    ElMessage({
      message: content,
      type: task.status === 'COMPLETED' ? 'success' : 'info',
      duration: retry ? 0 : 6000,
      showClose: true,
      onClose: () => toasts.delete(task.attemptId)
    })
  )
}
const remember = (task: AiTask) => {
  if (!active(task)) {
    if (submission?.taskId === task.taskId) submission = undefined
    tasks.value = tasks.value.filter(previous => previous.attemptId !== task.attemptId)
    notify(task)
    return
  }
  // An in-flight poll from before cancellation cannot revive its terminal attempt.
  if (finished.has(task.attemptId)) return
  tasks.value = [task, ...tasks.value.filter(previous => previous.taskId !== task.taskId)]
}
const submit = async () => {
  if (pending.value || (!props.resourceId && !props.text.trim())) return
  const value = {
    capability: 'sql.assist' as const,
    scene: 'sql' as const,
    resourceId: props.resourceId,
    editorId: props.editorId,
    sql: props.sql,
    text: props.text.trim() || tr('single_request'),
    ...(props.origin ? { origin: { ...props.origin } } : {})
  }
  const fingerprint = JSON.stringify(value)
  if (submission?.fingerprint !== fingerprint)
    submission = { fingerprint, value: { ...value, submissionKey: crypto.randomUUID() } }
  pending.value = true
  failure.value = ''
  const current = generation
  const accepted = submission
  try {
    const task = await submitAiTask(accepted.value)
    if (current !== generation) return
    accepted.taskId = task.taskId
    if (accepted.value.origin && accepted.value.origin.sessionId === props.origin?.sessionId) {
      if (origins.size >= 50) origins.delete(origins.keys().next().value!)
      origins.set(task.taskId, { origin: accepted.value.origin, question: accepted.value.text })
    }
    // Retain the same operation key after admission; a second click must replay its task.
    // A poll may already have observed terminal state before this response arrived.
    if (finished.has(task.attemptId)) {
      if (submission === accepted) submission = undefined
      return
    }
    remember(task)
  } catch (error) {
    if (current === generation) failure.value = requestFailure(error)
  } finally {
    if (current === generation) pending.value = false
  }
}
const change = async (task: AiTask | AiTaskNotice, retry = false) => {
  if (pending.value) return
  pending.value = true
  failure.value = ''
  const current = generation
  try {
    let result: AiTask
    if (retry) {
      const key = `${task.taskId}:${task.attemptId}`
      if (!retryKeys.has(key)) {
        if (retryKeys.size >= 50) retryKeys.delete(retryKeys.keys().next().value!)
        retryKeys.set(key, crypto.randomUUID())
      }
      result = await retryAiTask(task.taskId, task.attemptId, retryKeys.get(key)!)
    } else result = await cancelAiTask(task.taskId)
    if (current === generation) {
      remember(result)
      return true
    }
  } catch (error) {
    if (current === generation) failure.value = requestFailure(error)
  } finally {
    if (current === generation) pending.value = false
  }
}
const poll = async () => {
  const current = generation
  try {
    const activeTasks = await activeAiTasks()
    const activeIds = new Set(activeTasks.map(task => task.taskId))
    const completed = await Promise.all(
      tasks.value
        .filter(task => active(task) && !activeIds.has(task.taskId))
        .map(task => getAiTask(task.taskId))
    )
    if (current !== generation) return
    activeTasks.forEach(remember)
    completed.forEach(remember)
    // A notice acknowledged by another tab cannot consume the original conversation's task association.
    for (const [id, linked] of origins) {
      if (linked.origin.sessionId !== props.origin?.sessionId) {
        origins.delete(id)
        continue
      }
      try {
        const task = await getAiTask(id)
        if (current !== generation) return
        if (active(task)) continue
        if (task.status === 'COMPLETED' && !conversationDelivered.has(task.attemptId)) {
          const delivered = await deliverAiTaskConversation(id, linked.origin)
          if (current !== generation || linked.origin.sessionId !== props.origin?.sessionId) return
          conversationDelivered.add(delivered.attemptId)
          if (conversationDelivered.size > 50)
            conversationDelivered.delete(conversationDelivered.values().next().value!)
          emit('delivered', delivered, linked.question, linked.origin)
        }
        origins.delete(id)
      } catch {
        /* Keep this bounded live association for transient reads or an active foreground turn. */
      }
    }
    const notices = await pendingAiTaskNotices()
    if (current !== generation) return
    for (const notice of notices) {
      if (notice.expiresAt <= Date.now()) continue
      notify(notice)
      await acknowledgeAiTaskNotice(notice.attemptId)
      if (current !== generation) return
    }
    loadingFailed.value = false
  } catch {
    if (current !== generation) return
    // A denied or unreachable read never leaves a stale protected result visible.
    tasks.value = []
    loadingFailed.value = true
  } finally {
    if (current === generation) timer = setTimeout(poll, 1000)
  }
}
watch(
  () => [user.uid, user.oid, user.proxyInfo?.proxyOid],
  () => {
    generation++
    clearTimeout(timer)
    tasks.value = []
    submission = undefined
    retryKeys.clear()
    origins.clear()
    conversationDelivered.clear()
    closeToasts()
    pending.value = false
    failure.value = ''
    void poll()
  },
  { flush: 'sync' }
)
onMounted(poll)
onBeforeUnmount(() => {
  generation++
  clearTimeout(timer)
  origins.clear()
  conversationDelivered.clear()
  closeToasts()
})
</script>

<template>
  <section :aria-label="tr('tasks')">
    <h2>{{ tr('tasks') }}</h2>
    <p>{{ tr('task_lifecycle') }}</p>
    <el-button :disabled="pending || (!resourceId && !text.trim())" @click="submit">{{
      tr('background')
    }}</el-button>
    <p v-if="loadingFailed" role="status">{{ tr('task_load_failed') }}</p>
    <p v-if="failure" role="alert">{{ failure }}</p>
    <article v-for="task in tasks" :key="task.taskId">
      <p>
        {{ task.resourceName }} · {{ tr(`task_status_${task.status.toLowerCase()}`) }} ·
        {{ tr('task_attempt') }} {{ task.attemptNumber }} · {{ task.modelIdentifier }}
      </p>
      <el-button v-if="active(task)" :disabled="pending" @click="change(task)">{{
        tr('task_cancel')
      }}</el-button>
    </article>
  </section>
</template>
