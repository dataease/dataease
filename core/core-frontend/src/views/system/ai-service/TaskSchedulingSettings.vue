<script setup lang="ts">
import type { AiTaskScheduling } from '@/api/aiService'
import { useI18n } from '@/hooks/web/useI18n'
const limits = defineModel<AiTaskScheduling>({ required: true })
const { t } = useI18n()
const fields = [
  { key: 'maxRunning', min: 1, max: 20 },
  { key: 'maxQueued', min: 1, max: 10000 },
  { key: 'maxQueuedPerUser', min: 1, max: 1000 },
  { key: 'maxWaitSeconds', min: 1, max: 86400 }
] as const
</script>
<template>
  <section class="ai-task-scheduling">
    <h3>{{ t('ai_service.task_scheduling') }}</h3>
    <p class="ai-secondary">{{ t('ai_service.task_scheduling_hint') }}</p>
    <el-form-item
      v-for="field in fields"
      :key="field.key"
      :label="t(`ai_service.task_${field.key}`)"
    >
      <el-input-number
        v-model="limits[field.key]"
        :min="field.min"
        :max="field.max"
        :precision="0"
        controls-position="right"
      />
    </el-form-item>
  </section>
</template>
