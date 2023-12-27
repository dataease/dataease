<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
export interface Info {
  name: string
  value: string
}
defineProps({
  creator: {
    type: String,
    default: ''
  },
  createTime: {
    type: String,
    default: ''
  }
})
const { t } = useI18n()
</script>

<template>
  <div class="info-card">
    <div class="info-title">
      {{ t('dataset.create_by') }}
    </div>
    <div class="info-content">
      {{ creator }}
    </div>
    <div class="info-title">
      {{ t('dataset.create_time') }}
    </div>
    <div class="info-content">
      {{ createTime }}
    </div>
  </div>
</template>

<style lang="less" scoped>
.info-card {
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-style: normal;
  padding-left: 4px;
  font-weight: 400;
  line-height: 22px;

  .info-title {
    color: #646a73;
    font-size: 14px;
    margin-bottom: 4px;
  }
  .info-content {
    color: #1f2329;
    font-size: 14px;
    margin-bottom: 12px;
  }

  :last-child {
    margin-bottom: 0;
  }
}
</style>
