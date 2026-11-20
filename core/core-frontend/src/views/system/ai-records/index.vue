<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
</script>

<template>
  <section class="ai-records-page">
    <h1>{{ t('ai_service.records') }}</h1>
    <div class="ai-records-placeholder">
      <h2>{{ t('ai_service.records_pending') }}</h2>
      <p>{{ t('ai_service.records_description') }}</p>
    </div>
  </section>
</template>

<style scoped lang="less">
.ai-records-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 24px;
  gap: 20px;
  background: var(--ed-fill-color-light);

  h1 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    line-height: 28px;
  }
}
.ai-records-placeholder {
  flex: 1;
  padding: 24px;
  border-radius: 12px;
  background: var(--ed-bg-color);

  h2 {
    margin: 0 0 8px;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
  }
  p {
    margin: 0;
    color: var(--ed-text-color-secondary);
    font-size: 14px;
    line-height: 22px;
  }
}
</style>
