<script lang="ts" setup>
import {useI18n} from '@/hooks/web/useI18n'
import {computed} from "vue";

const props = defineProps({
  id: {
    type: String,
    default: '-'
  },
  creator: {
    type: String,
    default: '-'
  },
  createTime: {
    type: String,
    default: '-'
  },
  updater: {
    type: String,
    default: '-'
  },
  updateTime: {
    type: String,
    default: '-'
  },
  dataSourceName: {
    type: String,
    default: '-'
  },
  tableName: {
    type: String,
    default: '-'
  },
})
const {t} = useI18n()

const _creator = computed(() => {
  if (props.creator) {
    return props.creator
  }
  return '-'
})
const _updater = computed(() => {
  if (props.updater) {
    return props.updater
  }
  return '-'
})

</script>

<template>
  <div class="info-card">
    <div class="info-title">
      ID
    </div>
    <div class="info-content">
      {{ id }}
    </div>
    <div class="info-title">
      {{ t('dataset.create_by') }}
    </div>
    <div class="info-content">
      {{ _creator }}
    </div>
    <div class="info-title">
      {{ t('dataset.create_time') }}
    </div>
    <div class="info-content">
      {{ createTime }}
    </div>
    <div class="info-title">
      {{ t('visualization.update_by') }}
    </div>
    <div class="info-content">
      {{ _updater }}
    </div>
    <div class="info-title">
      {{ t('visualization.update_time') }}
    </div>
    <div class="info-content">
      {{ updateTime }}
    </div>
    <div class="info-title">
      {{ t('data_fill.form.datasource') }}
    </div>
    <div class="info-content">
      {{ dataSourceName }}
    </div>
    <div class="info-title">
      {{ t('data_fill.form.table_name') }}
    </div>
    <div class="info-content">
      {{ tableName }}
    </div>
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
