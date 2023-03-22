<script setup lang="ts">
import { defineProps, toRefs } from 'vue'
const props = defineProps({
  start: {
    type: Object,
    default() {
      return {
        x: null,
        y: null
      }
    }
  },
  width: {
    type: Number,
    default: 0
  },
  height: {
    type: Number,
    default: 0
  }
})

const { start, width, height } = toRefs(props)
</script>

<template>
  <div
    :style="{
      left: start?.x + 'px',
      top: start?.y + 'px',
      width: width + 'px',
      height: height + 'px'
    }"
    class="area"
  ></div>
</template>
<style lang="less" scoped>
.area {
  border: 1px solid #70c0ff;
  position: absolute;
}
</style>
