<script setup lang="ts">
import { getStyle, getCanvasStyle } from '@/utils/style'
import ComponentWrapper from './ComponentWrapper.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { computed, nextTick, onMounted, ref, toRefs } from 'vue'
import { changeRefComponentsSizeWithScale } from '@/utils/changeComponentsSizeWithScale'

const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  },
  componentData: {
    type: Object,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  canvasId: {
    type: String,
    required: false,
    default: 'canvas-main'
  }
})

const { canvasStyleData, componentData, canvasId } = toRefs(props)
const domId = 'preview-' + canvasId.value
const scaleWidth = ref(100)
const previewCanvas = ref(null)
const domWidth = ref()
const domHeight = ref()

const canvasStyle = computed(() => {
  if (canvasStyleData.value && canvasStyleData.value.width) {
    return {
      ...getCanvasStyle(canvasStyleData.value),
      height: changeStyleWithScale(canvasStyleData.value?.height, scaleWidth.value) + 'px'
    }
  } else {
    return {}
  }
})

const restore = () => {
  nextTick(() => {
    if (previewCanvas.value) {
      //div容器获取tableBox.value.clientWidth
      let canvasWidth = previewCanvas.value.clientWidth
      scaleWidth.value = (canvasWidth * 100) / canvasStyleData.value.width
      changeRefComponentsSizeWithScale(componentData.value, canvasStyleData.value, scaleWidth.value)
    }
  })
}

onMounted(() => {
  restore()
  window.addEventListener('resize', restore)
})

defineExpose({
  restore
})
</script>

<template>
  <div class="canvas-container" :style="canvasStyle" ref="previewCanvas">
    <ComponentWrapper
      :view-info="canvasViewInfo[item.id]"
      v-for="(item, index) in componentData"
      :key="index"
      :config="item"
    />
  </div>
</template>

<style lang="less" scoped>
.canvas-container {
  background-size: 100% 100% !important;
  width: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  position: relative;
}
</style>
