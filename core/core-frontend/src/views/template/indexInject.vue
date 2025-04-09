<script setup lang="ts">
import DeTemplateManage from './index.vue'
</script>

<template>
  <div class="inject-template-main">
    <DeTemplateManage></DeTemplateManage>
  </div>
</template>

<style scoped lang="less">
.inject-template-main {
  width: 100%;
  height: 100vh;
  padding: 24px;
  background-color: var(--MainBG, #f5f6f7);
  ::v-deep(.sys-setting-p) {
    height: calc(100vh - 100px);
  }
}
</style>
