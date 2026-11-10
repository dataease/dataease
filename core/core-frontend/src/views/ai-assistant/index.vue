<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  closeConversation,
  getConversationResources,
  openConversation,
  sendConversationTurn,
  type AiConversationResource,
  type AiConversationSession
} from '@/api/aiConversation'

const { t } = useI18n()
const tr = (key: string) => t(`ai_conversation.${key}`)
const user = useUserStoreWithOut()
const resources = ref<AiConversationResource[]>([])
const resourceId = ref('')
const sql = ref('')
const input = ref('')
const session = ref<AiConversationSession>()
const messages = ref<
  { role: 'user' | 'assistant'; text: string; status: string; executionId?: string }[]
>([])
const busy = ref(false)
const failure = ref('')
let generation = 0
let editorId = crypto.randomUUID()
const reset = () => {
  generation++
  const previous = session.value
  session.value = undefined
  messages.value = []
  busy.value = false
  failure.value = ''
  editorId = crypto.randomUUID()
  if (previous) void closeConversation(previous.sessionId).catch(() => undefined)
}
const load = async () => {
  const current = generation
  try {
    const result = await getConversationResources()
    if (current === generation) resources.value = result
  } catch (error) {
    if (current === generation) failure.value = typeof error === 'string' ? error : tr('failed')
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
  const current = generation
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
    const answer = await sendConversationTurn(active.sessionId, active.contextVersion, text)
    if (
      current !== generation ||
      answer.sessionId !== active.sessionId ||
      answer.contextVersion !== active.contextVersion
    )
      return
    messages.value[index].status = 'COMPLETED'
    messages.value.push({
      role: 'assistant',
      text: answer.text,
      status: answer.status,
      executionId: answer.executionId
    })
  } catch (error) {
    if (current !== generation) return
    messages.value[index].status = 'FAILED'
    failure.value = typeof error === 'string' ? error : tr('failed')
  } finally {
    if (current === generation) busy.value = false
  }
}
onMounted(() => {
  void load()
  window.addEventListener('pagehide', reset)
})
onBeforeUnmount(() => {
  window.removeEventListener('pagehide', reset)
  reset()
})
</script>

<template>
  <main class="ai-conversation">
    <h1>{{ tr('title') }}</h1>
    <p>{{ tr('description') }}</p>
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
    <p role="status">{{ busy ? tr('running') : tr('ready') }}</p>
    <p v-if="failure" role="alert">{{ failure }}</p>
    <section class="messages" :aria-label="tr('conversation')" aria-live="polite">
      <article v-for="(message, index) in messages" :key="index">
        <strong>{{ tr(message.role) }}</strong>
        <span v-if="message.status === 'FAILED'"> · {{ tr('incomplete') }}</span>
        <pre>{{ message.text }}</pre>
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
