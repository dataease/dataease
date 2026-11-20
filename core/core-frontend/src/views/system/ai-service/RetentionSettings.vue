<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'
import type { AiRetentionSettings } from '@/api/aiService'
const props = defineProps<{ modelValue: AiRetentionSettings }>()
const emit = defineEmits<{ 'update:modelValue': [AiRetentionSettings] }>()
const { t } = useI18n()
const limits = [
  { key: 'auditDays', min: 1, max: 3650 },
  { key: 'deliveryDays', min: 1, max: 3650 }
] as const
const change = (key: keyof AiRetentionSettings, value: number | undefined) =>
  emit('update:modelValue', { ...props.modelValue, [key]: value })
</script>
<template>
  <fieldset>
    <legend>{{ t('ai_service.retention_title') }}</legend>
    <p>{{ t('ai_service.retention_hint') }}</p>
    <el-form-item
      v-for="limit in limits"
      :key="limit.key"
      :label="t(`ai_service.retention_${limit.key}`)"
    >
      <el-input-number
        :model-value="modelValue[limit.key]"
        :min="limit.min"
        :max="limit.max"
        :precision="0"
        controls-position="right"
        :aria-label="t(`ai_service.retention_${limit.key}`)"
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
