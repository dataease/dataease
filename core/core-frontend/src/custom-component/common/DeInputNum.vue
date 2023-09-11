<script setup lang="ts">
import { toRefs, PropType } from 'vue'

const props = defineProps({
  value: Number,
  name: String,
  min: Number,
  max: Number,
  step: Number,
  disabled: Boolean,
  themes: {
    type: String as PropType<'light' | 'dark'>,
    default: 'light'
  }
})

const { name, value, min, max, step, themes, disabled } = toRefs(props)
</script>

<template>
  <el-input-number
    class="de-input-number"
    :class="themes + '-custom-input-number'"
    v-model="value"
    :effect="themes"
    :disabled="disabled"
    :min="min"
    :max="max"
    :step="step"
    size="small"
    controls-position="right"
  />
</template>

<style lang="less" scoped>
.de-input-number {
  margin-left: 5px;
  width: 80px;
}
.dark-custom-input-number :deep(.ed-input__wrapper) {
  background-color: rgba(0, 0, 0, 0) !important;
  padding-left: 5px !important;
  padding-right: 30px !important;
}

.dark-custom-input-number :deep(.ed-input__inner) {
  color: #ffffff;
}

.dark-custom-input-number :deep(.ed-input-number__decrease) {
  background-color: rgba(0, 0, 0, 0) !important;
  color: #ffffff;
}
.dark-custom-input-number :deep(.ed-input-number__increase) {
  background-color: rgba(0, 0, 0, 0) !important;
  color: #ffffff;
}
</style>
