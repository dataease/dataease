<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { getConversationToolDetail } from '@/api/aiConversation'
import { useI18n } from '@/hooks/web/useI18n'

const props = defineProps<{ sessionId: string; executionId: string; sequence: number }>()
const emit = defineEmits<{ (event: 'detail-error', error: unknown): void }>()
const { t } = useI18n()
const result = ref('')
const failed = ref(false)
const read = new AbortController()
onMounted(async () => {
  try {
    const tool = await getConversationToolDetail(
      props.sessionId,
      props.executionId,
      props.sequence,
      read.signal
    )
    if (read.signal.aborted) return
    const value = tool.result ?? ''
    try {
      result.value = JSON.stringify(JSON.parse(value), null, 2)
    } catch {
      result.value = value
    }
  } catch (error) {
    if (read.signal.aborted) return
    failed.value = true
    emit('detail-error', error)
  }
})
onBeforeUnmount(() => read.abort())
</script>

<template>
  <pre v-if="result">{{ result }}</pre>
  <span v-else role="status">{{
    t(failed ? 'ai_conversation.failed' : 'ai_conversation.tool_running')
  }}</span>
</template>

<style scoped>
pre {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
</style>
