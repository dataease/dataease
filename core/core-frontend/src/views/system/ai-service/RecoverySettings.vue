<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'
import type { AiRecoverySettings } from '@/api/aiService'
const props = defineProps<{ modelValue: AiRecoverySettings }>()
const emit = defineEmits<{ 'update:modelValue': [AiRecoverySettings] }>()
const { t } = useI18n()
const limits = [
  { key: 'stepRetries', min: 0, max: 5 },
  { key: 'attemptRetries', min: 0, max: 5 },
  { key: 'initialDelayMillis', min: 100, max: 10000 }
] as const
const change = (key: keyof AiRecoverySettings, value: number | undefined) =>
  emit('update:modelValue', { ...props.modelValue, [key]: value })
</script>
<template>
  <fieldset>
    <legend>{{ t('ai_service.recovery_title') }}</legend>
    <p>{{ t('ai_service.recovery_hint') }}</p>
    <el-form-item
      v-for="limit in limits"
      :key="limit.key"
      :label="t(`ai_service.recovery_${limit.key}`)"
    >
      <el-input-number
        :model-value="modelValue[limit.key]"
        :min="limit.min"
        :max="limit.max"
        :precision="0"
        controls-position="right"
        :aria-label="t(`ai_service.recovery_${limit.key}`)"
        @update:model-value="change(limit.key, $event)"
      />
    </el-form-item>
  </fieldset>
</template>
<style scoped>
fieldset {
  min-width: 0;
  padding: 16px;
  margin: 16px 0 0;
  border: 1px solid var(--ed-border-color);
}

p {
  color: var(--ed-text-color-secondary);
}
</style>
