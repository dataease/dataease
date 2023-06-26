<script setup lang="ts">
import { getStyle, getCanvasStyle, getShapeItemStyle } from '@/utils/style'
import ComponentWrapper from './ComponentWrapper.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { toPng } from 'html-to-image'
import { deepCopy } from '@/utils/utils'

import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElButton } from 'element-plus-secondary'
import { computed, nextTick, onMounted, ref } from 'vue'
import elementResizeDetectorMaker from 'element-resize-detector'

const props = defineProps({
  isScreenshot: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['close'])

const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData, componentData, dvInfo, pcMatrixCount } = storeToRefs(dvMainStore)
const copyData = ref(deepCopy(componentData.value))
const container = ref(null)
const canvasOut = ref(null)
const baseWidth = ref(0)
const baseHeight = ref(0)
const renderState = ref('loading...')
const baseMarginLeft = ref(0)
const baseMarginTop = ref(0)
const cyGridster = ref(null)
const canvasInitStatus = ref(false)

const curGap = computed(() => {
  return dashboardActive.value && canvasStyleData.value.dashboard.gap === 'yes'
    ? canvasStyleData.value.dashboard.gapSize
    : 0
})

const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})

const close = () => {
  emit('close')
}

const htmlToImage = () => {
  toPng(container.value.querySelector('.canvas'))
    .then(dataUrl => {
      const a = document.createElement('a')
      a.setAttribute('download', 'screenshot')
      a.href = dataUrl
      a.click()
      close()
    })
    .catch(error => {
      console.error('oops, something went wrong!', error)
      close()
    })
}

const canvasSizeInit = () => {
  nextTick(() => {
    if (canvasOut.value) {
      //div容器获取tableBox.value.clientWidth
      const screenWidth = canvasOut.value.clientWidth - 4
      const screenHeight = canvasOut.value.clientHeight
      baseWidth.value = screenWidth / pcMatrixCount.value.x
      baseHeight.value = screenHeight / pcMatrixCount.value.y
      baseMarginLeft.value = 0
      baseMarginTop.value = 0
      canvasInitStatus.value = true
      dvMainStore.setBashMatrixInfo({
        baseWidth: baseWidth.value,
        baseHeight: baseHeight.value,
        baseMarginLeft: baseMarginLeft.value,
        baseMarginTop: baseMarginTop.value
      })
    }
  })
}

const getShapeItemShowStyle = item => {
  return getShapeItemStyle(item, {
    dvModel: dvInfo.value.type,
    cellWidth: baseWidth.value,
    cellHeight: baseHeight.value,
    curGap: curGap.value
  })
}

const dashboardCanvasInit = () => {
  window.addEventListener('resize', canvasSizeInit)
  const erd = elementResizeDetectorMaker()
  erd.listenTo(document.getElementById('dashboardMainCanvas'), element => {
    canvasSizeInit()
  })
}

onMounted(() => {
  if (dashboardActive.value) {
    dashboardCanvasInit()
  }
})
</script>

<template>
  <div ref="container" class="bg">
    <el-button v-if="!isScreenshot" class="close" @click="close()">关闭</el-button>
    <el-button v-else class="close" @click="htmlToImage()">确定</el-button>
    <div ref="canvasOut" class="canvas-container">
      <div
        class="canvas"
        :style="{
          ...getCanvasStyle(canvasStyleData),
          width: changeStyleWithScale(canvasStyleData.width) + 'px',
          height: changeStyleWithScale(canvasStyleData.height) + 'px'
        }"
      >
        <ComponentWrapper
          v-show="item.isShow"
          v-for="(item, index) in copyData"
          :style="getShapeItemShowStyle(item)"
          :key="index"
          :config="item"
        />
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.bg {
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  position: fixed;
  background: rgb(0, 0, 0, 0.5);
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  padding: 20px;

  .canvas-container {
    width: calc(100% - 40px);
    height: calc(100% - 120px);
    overflow: auto;

    .canvas {
      background: #fff;
      position: relative;
      margin: auto;
    }
  }

  .close {
    position: absolute;
    right: 20px;
    top: 20px;
  }
}
</style>
