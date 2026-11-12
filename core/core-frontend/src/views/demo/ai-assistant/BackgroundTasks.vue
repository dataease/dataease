<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
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
let submission: { fingerprint: string; value: AiTaskSubmit } | undefined
const retryKeys = new Map<string, string>()
const active = (task: AiTask) => ['QUEUED', 'RUNNING'].includes(task.status)
const remember = (task: AiTask) => {
  tasks.value = [task, ...tasks.value.filter(previous => previous.taskId !== task.taskId)].slice(
    0,
    10
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
  try {
    const task = await submitAiTask(submission.value)
    if (current !== generation) return
    remember(task)
    submission = undefined
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
      if (!retryKeys.has(key)) retryKeys.set(key, crypto.randomUUID())
      result = await retryAiTask(task.taskId, task.attemptId, retryKeys.get(key)!)
    } else result = await cancelAiTask(task.taskId)
    if (current === generation) remember(result)
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
      <el-button
        v-if="['FAILED', 'CANCELLED', 'INTERRUPTED'].includes(task.status)"
        :disabled="pending"
        @click="change(task, true)"
        >{{ tr('task_retry') }}</el-button
      >
      <p v-if="task.status === 'COMPLETED'" role="status">{{ tr('task_completed') }}</p>
      <pre v-if="task.result">{{ task.result.text }}</pre>
      <pre v-if="task.result?.type === 'SQL_SUGGESTION'">{{ task.result.sql }}</pre>
    </article>
  </section>
</template>
