<script setup lang="ts">
import { defineProps, toRefs } from 'vue'

const props = defineProps({
  value: Number,
  name: String,
  min: Number,
  max: Number
})

const { name, value, min, max } = toRefs(props)
</script>

<template>
  <el-input-number
    class="de-input-number"
    v-model="value"
    :min="min"
    :max="max"
    size="small"
    controls-position="right"
  />
</template>

<style lang="less" scoped>
.de-input-number {
  margin-left: 5px;
  width: 80px;
}
:deep(.ed-input__wrapper) {
  background-color: rgba(37, 45, 54, 1);
  padding-left: 5px !important;
  padding-right: 30px !important;
}

:deep(.ed-input__inner) {
  color: #ffffff;
}

:deep(.ed-input-number__decrease) {
  background-color: rgba(37, 45, 54, 1);
  color: #ffffff;
}
:deep(.ed-input-number__increase) {
  background-color: rgba(37, 45, 54, 1);
  color: #ffffff;
}
</style>
