<script setup lang="ts">
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { toRefs } from 'vue'

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
  }>(),
  {
    themes: 'dark'
  }
)

const { themes } = toRefs(props)
</script>

<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr
      :themes="themes"
      :background-color-picker-width="197"
      :background-border-select-width="197"
    />
  </div>
</template>

<style lang="less" scoped>
.content {
  width: 100%;
  font-size: 12px;
  padding: 10px;
}

.de-collapse-style {
  :deep(.ed-collapse-item__header) {
    height: 36px !important;
    line-height: 36px !important;
    font-size: 12px !important;
    padding: 0 !important;
    font-weight: 500 !important;

    .ed-collapse-item__arrow {
      margin: 0 6px 0 8px;
    }
  }
  :deep(.ed-collapse-item__content) {
    padding: 16px 8px 0;
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 16px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
}

:deep(.ed-textarea__inner) {
  color: #ffffff;
  background-color: #000000;
}
</style>
