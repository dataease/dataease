<script setup lang="ts">
import { toRefs } from 'vue'
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
  // 选中区域图层最高 内部组件暂不可进行实际触发
  z-index: 15;
}
</style>
