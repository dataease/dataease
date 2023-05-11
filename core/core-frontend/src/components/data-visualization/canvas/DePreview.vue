<script setup lang="ts">
import { getStyle, getCanvasStyle } from '@/utils/style'
import ComponentWrapper from './ComponentWrapper.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { ref, toRefs } from 'vue'

const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  },
  componentData: {
    type: Object,
    required: true
  }
})

const { canvasStyleData, componentData } = toRefs(props)
</script>

<template>
  <div
    class="canvas-container"
    :style="{
      ...getCanvasStyle(canvasStyleData),
      width: changeStyleWithScale(canvasStyleData?.width) + 'px',
      height: changeStyleWithScale(canvasStyleData?.height) + 'px'
    }"
  >
    <ComponentWrapper v-for="(item, index) in componentData" :key="index" :config="item" />
  </div>
</template>

<style lang="less" scoped>
.canvas-container {
  width: 100%;
  height: 100%;
  overflow-x: auto;
  overflow-y: auto;
}
</style>
