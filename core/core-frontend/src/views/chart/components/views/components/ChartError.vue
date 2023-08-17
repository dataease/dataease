<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

const props = defineProps({
  errMsg: {
    type: String,
    required: true,
    default: ''
  }
})
</script>

<template>
  <div class="canvas-content error-info">
    <span>{{ props.errMsg }}</span>
    <span>[{{ t('chart.chart_error_tips') }}]</span>
  </div>
</template>

<style lang="less" scoped>
.error-info {
  background-color: #ece7e7;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  color: #9ea6b2;
  width: 100%;
  height: 100%;
  flex-direction: column;
}
</style>
