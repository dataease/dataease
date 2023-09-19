<script setup lang="ts">
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { toRefs } from 'vue'
const props = defineProps({
  themes: {
    type: String,
    default: 'dark'
  }
})

const { themes } = toRefs(props)
</script>

<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr :themes="themes"></CommonAttr>
  </div>
</template>

<style lang="less">
.de-collapse-style {
  :deep(.ed-collapse-item__header) {
    height: 34px !important;
    line-height: 34px !important;
    padding: 0 0 0 6px !important;
    font-size: 12px !important;
    font-weight: 400 !important;
  }
  :deep(.ed-collapse-item__content) {
    padding: 16px !important;
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 16px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
}
</style>
