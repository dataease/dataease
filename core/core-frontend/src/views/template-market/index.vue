<template>
  <el-row class="custom-position template-main">
    {{ t('visualization.market_network_tips') }}
  </el-row>
</template>
<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
</script>

<style lang="less" scoped>
.custom-position {
  height: 80vh;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #646a73;
  font-weight: 400;
}

.template-main {
  text-align: center;
  border-radius: 4px;
  padding: 0 12px 24px 12px;
  height: calc(100vh - 56px) !important;
  overflow-x: hidden;
  overflow-y: auto;
  background-color: var(--ContentBG, #ffffff);
  justify-content: center;
}
</style>
