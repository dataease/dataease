<script setup lang="ts">
import { computed } from 'vue'
import useClipboard from 'vue-clipboard3'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { AnswerFailure } from './assistantFailure'

const props = defineProps<{
  issue: AnswerFailure
  executionId?: string
  canRetry?: boolean
  disabled: boolean
  noFormula: boolean
}>()
defineEmits<{ (event: 'retry'): void }>()
const { t } = useI18n()
const tr = (key: string) => t('ai_conversation.' + key)
const title = computed(() => [tr(props.issue.step), props.issue.tool].filter(Boolean).join(' · '))
const action = computed(() => {
  if (props.issue.step === 'progress_step' && !props.canRetry) return tr('resume_progress')
  if (props.issue.step === 'validation_step') return tr('check_proposal')
  return tr('check_service')
})
const { toClipboard } = useClipboard()
const copy = async () => {
  try {
    await toClipboard(
      [
        title.value,
        props.issue.reason,
        props.noFormula && tr('no_formula'),
        props.executionId,
        props.issue.requestId,
        props.issue.retryMessage,
        props.issue.retryRequestId
      ]
        .filter(Boolean)
        .join('\n')
    )
    ElMessage.success(t('data_set.copied_successfully'))
  } catch {
    ElMessage.warning(tr('copy_failed'))
  }
}
</script>

<template>
  <div class="assistant-failure" role="alert">
    <strong>{{ title }}</strong>
    <p>{{ issue.reason }}</p>
    <p v-if="noFormula">{{ tr('no_formula') }}</p>
    <p>{{ action }}</p>
    <small v-if="executionId">{{ executionId }}</small>
    <small v-if="issue.requestId">{{ issue.requestId }}</small>
    <p v-if="issue.retryMessage">{{ issue.retryMessage }}</p>
    <small v-if="issue.retryRequestId">{{ issue.retryRequestId }}</small>
    <div>
      <el-button text @click="copy">{{ tr('copy_diagnostic') }}</el-button>
      <el-button v-if="canRetry" :disabled="disabled" @click="$emit('retry')">{{
        tr(issue.step === 'progress_step' ? 'answer_again' : 'retry_answer')
      }}</el-button>
    </div>
  </div>
</template>

<style scoped>
.assistant-failure {
  padding: 8px 12px;
  margin: 8px 0;
  border-inline-start: 3px solid var(--ed-color-danger);
  background: var(--ed-color-danger-light-9);
  overflow-wrap: anywhere;
}

.assistant-failure p {
  margin: 4px 0;
}
</style>
