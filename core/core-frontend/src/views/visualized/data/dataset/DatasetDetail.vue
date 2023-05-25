<script lang="ts" setup>
import { PropType } from 'vue'
export interface Info {
  name: string
  value: string
}
defineProps({
  titleType: {
    type: String,
    default: ''
  },
  infoList: {
    type: Array as PropType<Info[]>,
    default: () => []
  }
})
</script>

<template>
  <div class="info-card">
    <div class="title-type">{{ titleType }}</div>
    <template v-for="info in infoList" :key="info.string">
      <div class="info-title">
        {{ info.name }}
      </div>
      <div class="info-content">
        {{ info.value }}
      </div>
    </template>
  </div>
</template>

<style lang="less" scoped>
.info-card {
  font-family: 'PingFang SC';
  font-style: normal;
  padding-left: 4px;

  .title-type {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 12px;
    color: var(--deTextPrimary, #1f2329);
  }
  .info-title {
    font-weight: 500;
    font-size: 14px;
    line-height: 22px;
    color: #646a73;
    margin-bottom: 4px;
  }
  .info-content {
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    color: #1f2329;
    margin-bottom: 12px;
  }

  :last-child {
    margin-bottom: 0;
  }
}
</style>
