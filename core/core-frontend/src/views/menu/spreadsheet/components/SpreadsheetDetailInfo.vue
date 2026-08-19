<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { SpreadsheetPublishStatus, type SpreadsheetVO } from '../api'

const { t } = useI18n()

const props = defineProps<{
  info: Partial<SpreadsheetVO>
}>()

const createByText = computed(() => props.info.creator || props.info.createBy || '-')
const updateByText = computed(() => props.info.updater || props.info.updateBy || '-')
const publishStatusText = computed(() => {
  if (props.info.status === SpreadsheetPublishStatus.Published) {
    return t('spreadsheet.published')
  }
  if (props.info.status === SpreadsheetPublishStatus.SavedUnpublished) {
    return t('spreadsheet.saved_unpublished')
  }
  return t('spreadsheet.unpublished')
})

const timestampFormatDate = (value?: number) => {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString()
}
</script>

<template>
  <div class="info-card">
    <div class="info-title">{{ `${t('spreadsheet.title')} ID` }}</div>
    <div class="info-content">{{ info.id || '-' }}</div>
    <div class="info-title">{{ t('spreadsheet.publish_status') }}</div>
    <div class="info-content">{{ publishStatusText }}</div>
    <div class="info-title">{{ t('visualization.create_by') }}</div>
    <div class="info-content">{{ createByText }}</div>
    <div class="info-title">{{ t('visualization.create_time') }}</div>
    <div class="info-content">{{ timestampFormatDate(info.createTime) }}</div>
    <div class="info-title">{{ t('visualization.update_by') }}</div>
    <div class="info-content">{{ updateByText }}</div>
    <div class="info-title">{{ t('visualization.update_time') }}</div>
    <div class="info-content">{{ timestampFormatDate(info.updateTime) }}</div>
  </div>
</template>

<style lang="less" scoped>
.info-card {
  font-family: var(--de-custom_font, 'PingFang');
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
