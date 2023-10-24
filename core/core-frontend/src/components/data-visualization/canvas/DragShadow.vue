<template>
  <div class="shadow-main" :style="shadowStyle">
    <div class="shadow-background"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue'
const props = defineProps({
  baseWidth: {
    required: true,
    type: Number
  },
  baseHeight: {
    required: true,
    type: Number
  },
  curGap: {
    required: true,
    type: Number
  },
  element: {
    required: true,
    type: Object,
    default() {
      return {
        component: null,
        propValue: null,
        request: null,
        linkage: null,
        type: null,
        events: null,
        style: null,
        id: null
      }
    }
  }
})

const { element, baseWidth, baseHeight, curGap } = toRefs(props)

const shadowStyle = computed(() => {
  const { x, y, sizeX, sizeY } = element.value
  return {
    padding: curGap.value + 'px',
    width: sizeX * baseWidth.value + 'px',
    height: sizeY * baseHeight.value + 'px',
    left: (x - 1) * baseWidth.value + 'px',
    top: (y - 1) * baseHeight.value - 3 + 'px'
  }
})
</script>

<style lang="less" scoped>
.shadow-main {
  position: absolute;
  transition: all 0.1s;
}
.shadow-background {
  width: 100%;
  height: 100%;
  background-color: #b8d3f9;
}
</style>
