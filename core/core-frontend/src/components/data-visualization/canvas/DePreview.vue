<script setup lang="ts">
import { getStyle, getCanvasStyle } from '@/utils/style'
import ComponentWrapper from './ComponentWrapper.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { computed, ref, toRefs } from 'vue'

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

const canvasStyle = computed(() => {
  if (canvasStyleData.value && canvasStyleData.value.width) {
    return {
      ...getCanvasStyle(canvasStyleData.value),
      width: changeStyleWithScale(canvasStyleData.value?.width) + 'px',
      height: changeStyleWithScale(canvasStyleData.value?.height) + 'px'
    }
  } else {
    return {}
  }
})
</script>

<template>
  <div class="canvas-container" :style="canvasStyle">
    <ComponentWrapper v-for="(item, index) in componentData" :key="index" :config="item" />
  </div>
</template>

<style lang="less" scoped>
.canvas-container {
  width: 100%;
  height: 100%;
  overflow-x: auto;
  overflow-y: auto;
  position: relative;
}
</style>
