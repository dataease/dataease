<template>
  <div class="dataease-v2-embedded-assistant-sqlbot-head">
    <div class="head-inner">
      <span>{{ t('visualization.sqlbot_query_tips') }}</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
</script>
<style lang="less" scoped>
.dataease-v2-embedded-assistant-sqlbot-head {
  width: calc(100% - 140px);
  left: 0;
  top: 0;
  z-index: 300;
  line-height: 56px;
  background: #fff;
  height: 56px;
  display: flex;
  align-items: center;
  position: absolute;
  .head-inner {
    display: flex;
    width: 100%;
    font-size: 16px;
    align-items: center;
    background: #1cba901a;
    line-height: 56px;
    font-weight: bold;
    padding: 0 16px;
  }
}
</style>
