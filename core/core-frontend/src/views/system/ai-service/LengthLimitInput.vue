<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

const props = defineProps<{ modelValue: number; label: string }>()
const emit = defineEmits<{ 'update:modelValue': [number] }>()
const { t } = useI18n()
const draft = ref('')
watch(
  () => props.modelValue,
  value => {
    if (Number.isInteger(value) && value > 0) draft.value = String(value)
  },
  { immediate: true }
)
const parse = (value: string) => {
  const match = /^(\d+(?:\.\d+)?)\s*([km]?)$/i.exec(value.trim())
  if (!match) return NaN
  const unit = match[2].toLowerCase()
  const count = Number(match[1]) * (unit === 'k' ? 1024 : unit === 'm' ? 1048576 : 1)
  return Number.isInteger(count) && count > 0 && count <= 2147483647 ? count : NaN
}
const input = (value: string) => {
  draft.value = value
  emit('update:modelValue', parse(value))
}
</script>
<template>
  <div class="ai-length-limit" role="group" :aria-label="label">
    <el-radio-group
      :model-value="modelValue === 0 ? 'model' : 'custom'"
      :aria-label="label"
      @update:model-value="emit('update:modelValue', $event === 'model' ? 0 : parse(draft))"
    >
      <el-radio value="model">{{ t('ai_service.length_follow_model') }}</el-radio>
      <el-radio value="custom">{{ t('ai_service.length_custom') }}</el-radio>
    </el-radio-group>
    <el-input
      v-if="modelValue !== 0"
      :model-value="draft"
      :aria-label="label"
      :placeholder="t('ai_service.length_placeholder')"
      @update:model-value="input"
    />
    <p v-if="modelValue !== 0 && !Number.isInteger(modelValue)" role="alert">
      {{ t('ai_service.length_invalid') }}
    </p>
  </div>
</template>
<style scoped>
.ai-length-limit {
  width: 100%;
}

p {
  margin: 4px 0 0;
  color: var(--ed-color-danger);
}
</style>
