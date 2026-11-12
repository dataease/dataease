<script setup lang="ts">
import { h, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  activeAiTasks,
  cancelAiTask,
  getAiTask,
  retryAiTask,
  submitAiTask,
  type AiTask,
  type AiTaskSubmit
} from '@/api/aiTask'

const props = defineProps<{ resourceId: string; editorId: string; sql: string; text: string }>()
const { t } = useI18n()
const tr = (key: string) => t(`ai_conversation.${key}`)
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
const notify = (task: AiTask) => {
  if (finished.has(task.attemptId)) return
  finished.add(task.attemptId)
  if (finished.size > 50) finished.delete(finished.values().next().value!)
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
    : message
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
  tasks.value = [task, ...tasks.value.filter(previous => previous.taskId !== task.taskId)].slice(
    0,
    4
  )
}
const submit = async () => {
  if (pending.value || (!props.resourceId && !props.text.trim())) return
  const value = {
    capability: 'sql.assist' as const,
    scene: 'sql' as const,
    resourceId: props.resourceId,
    editorId: props.editorId,
    sql: props.sql,
    text: props.text.trim() || tr('single_request')
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
    // Retain the same operation key after admission; a second click must replay its task.
    // A poll may already have observed terminal state before this response arrived.
    if (finished.has(task.attemptId)) {
      if (submission === accepted) submission = undefined
      return
    }
    remember(task)
  } catch {
    if (current === generation) failure.value = tr('task_request_failed')
  } finally {
    if (current === generation) pending.value = false
  }
}
const change = async (task: AiTask, retry = false) => {
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
  } catch {
    if (current === generation) failure.value = tr('task_request_failed')
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
