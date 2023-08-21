<template>
  <el-row>
    <el-col class="info-item">
      <p class="info-title">{{ t('visualization.create_by') }}</p>
      <p class="info-content">{{ dvInfo.creatorName }}</p>
    </el-col>
    <el-col class="info-item">
      <p class="info-title">{{ t('visualization.create_time') }}</p>
      <p class="info-content">{{ timestampFormatDate(dvInfo.createTime) }}</p>
    </el-col>
    <el-col class="info-item">
      <p class="info-title">{{ t('visualization.update_by') }}</p>
      <p class="info-content">{{ dvInfo.updateName || 'N/A' }}</p>
    </el-col>
    <el-col class="info-item">
      <p class="info-title">{{ t('visualization.update_time') }}</p>
      <p v-if="dvInfo.updateTime" class="info-content">
        {{ timestampFormatDate(dvInfo.updateTime) }}
      </p>
      <p v-if="!dvInfo.updateTime" class="info-content">N/A</p>
    </el-col>
  </el-row>
</template>

<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)

const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return new Date(value)['format']()
}
</script>

<style lang="less" scoped>
.info-tab :deep(.el-tabs__item) {
  font-weight: 400;
  font-size: 12px;
}
.info-item {
  margin: 6px 0;
}
.info-title {
  margin: 0 !important;
  font-weight: 600;
  font-size: 12px;
}
.info-content {
  margin: 0 !important;
  font-size: 12px;
}
</style>
