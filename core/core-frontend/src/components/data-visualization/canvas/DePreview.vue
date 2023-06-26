<script setup lang="ts">
import { getStyle, getCanvasStyle, getShapeItemStyle } from '@/utils/style'
import ComponentWrapper from './ComponentWrapper.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { computed, nextTick, onMounted, ref, toRefs } from 'vue'
import { changeRefComponentsSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { pcMatrixCount } = storeToRefs(dvMainStore)

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
  dvInfo: {
    type: Object,
    required: true
  },
  curGap: {
    type: Number,
    required: false,
    default: 0
  },
  canvasId: {
    type: String,
    required: false,
    default: 'canvas-main'
  }
})

const { canvasStyleData, componentData, dvInfo, curGap, canvasId } = toRefs(props)
const domId = 'preview-' + canvasId.value
const scaleWidth = ref(100)
const previewCanvas = ref(null)
const domWidth = ref()
const domHeight = ref()
const cellWidth = ref(10)
const cellHeight = ref(10)

const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})

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
      let canvasHeight = previewCanvas.value.clientHeight
      scaleWidth.value = (canvasWidth * 100) / canvasStyleData.value.width
      if (dashboardActive.value) {
        cellWidth.value = canvasWidth / pcMatrixCount.value.x
        cellHeight.value = canvasHeight / pcMatrixCount.value.y
      } else {
        changeRefComponentsSizeWithScale(
          componentData.value,
          canvasStyleData.value,
          scaleWidth.value
        )
      }
    }
  })
}

const getShapeItemShowStyle = item => {
  return getShapeItemStyle(item, {
    dvModel: dvInfo.value.type,
    cellWidth: cellWidth.value,
    cellHeight: cellHeight.value,
    curGap: curGap.value
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
      v-for="(item, index) in componentData"
      :view-info="canvasViewInfo[item.id]"
      :key="index"
      :config="item"
      :style="getShapeItemShowStyle(item)"
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
