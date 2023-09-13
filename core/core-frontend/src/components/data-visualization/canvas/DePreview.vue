<script setup lang="ts">
import { getStyle, getCanvasStyle, getShapeItemStyle } from '@/utils/style'
import ComponentWrapper from './ComponentWrapper.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { computed, nextTick, onMounted, ref, toRefs, watch, onBeforeUnmount } from 'vue'
import { changeRefComponentsSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import elementResizeDetectorMaker from 'element-resize-detector'
import UserViewEnlarge from '@/components/visualization/UserViewEnlarge.vue'
import CanvasOptBar from '@/components/visualization/CanvasOptBar.vue'
import { isMainCanvas } from '@/utils/canvasUtils'
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
  canvasId: {
    type: String,
    required: false,
    default: 'canvas-main'
  },
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  },
  previewActive: {
    type: Boolean,
    default: true
  }
})

const {
  canvasStyleData,
  componentData,
  dvInfo,
  canvasId,
  canvasViewInfo,
  showPosition,
  previewActive
} = toRefs(props)
const domId = 'preview-' + canvasId.value
const scaleWidth = ref(100)
const previewCanvas = ref(null)
const domWidth = ref()
const domHeight = ref()
const cellWidth = ref(10)
const cellHeight = ref(10)
const userViewEnlargeRef = ref(null)
const searchCount = ref(0)
const refreshTimer = ref(null)

const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})

const canvasStyle = computed(() => {
  if (canvasStyleData.value && canvasStyleData.value.width && isMainCanvas(canvasId.value)) {
    return {
      ...getCanvasStyle(canvasStyleData.value),
      height: dashboardActive.value
        ? '100%'
        : changeStyleWithScale(canvasStyleData.value?.height, scaleWidth.value) + 'px'
    }
  } else {
    return {}
  }
})

const forceRender = () => {
  cellWidth.value = cellWidth.value + 0.01
  nextTick(() => {
    cellWidth.value = cellWidth.value - 0.01
  })
}

watch(
  () => previewActive.value,
  () => {
    if (previewActive.value) {
      restore()
    }
  }
)

const restore = () => {
  nextTick(() => {
    if (previewCanvas.value) {
      //div容器获取tableBox.value.clientWidth
      let canvasWidth = previewCanvas.value.clientWidth
      let canvasHeight = previewCanvas.value.clientHeight
      scaleWidth.value = Math.floor((canvasWidth * 100) / canvasStyleData.value.width)
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

const curGap = computed(() => {
  return dashboardActive.value && canvasStyleData.value.dashboard.gap === 'yes'
    ? canvasStyleData.value.dashboard.gapSize
    : 0
})

const initRefreshTimer = () => {
  // 数据刷新计时器 (仪表开启刷新并且是预览状态才启动刷新)
  if (canvasStyleData.value.refreshViewEnable && showPosition.value === 'preview') {
    searchCount.value = 0
    refreshTimer.value && clearInterval(refreshTimer.value)
    let refreshTime = 300000
    if (canvasStyleData.value.refreshTime && canvasStyleData.value.refreshTime > 0) {
      if (canvasStyleData.value.refreshUnit === 'second') {
        refreshTime = canvasStyleData.value.refreshTime * 1000
      } else {
        refreshTime = canvasStyleData.value.refreshTime * 60000
      }
    }
    refreshTimer.value = setInterval(() => {
      searchCount.value++
    }, refreshTime)
  }
}

onMounted(() => {
  initRefreshTimer()
  restore()
  window.addEventListener('resize', restore)
  const erd = elementResizeDetectorMaker()
  erd.listenTo(document.getElementById(domId), element => {
    restore()
  })
})

onBeforeUnmount(() => {
  clearInterval(refreshTimer.value)
})

const userViewEnlargeOpen = (opt, item) => {
  userViewEnlargeRef.value.dialogInit(
    canvasStyleData.value,
    canvasViewInfo.value[item.id],
    item,
    opt
  )
}

defineExpose({
  restore
})
</script>

<template>
  <div :id="domId" class="canvas-container" :style="canvasStyle" ref="previewCanvas">
    <canvas-opt-bar
      :canvas-id="canvasId"
      :canvas-style-data="canvasStyleData"
      :component-data="componentData"
    ></canvas-opt-bar>
    <ComponentWrapper
      v-for="(item, index) in componentData"
      :canvas-id="canvasId"
      :canvas-style-data="canvasStyleData"
      :dv-info="dvInfo"
      :canvas-view-info="canvasViewInfo"
      :view-info="canvasViewInfo[item.id]"
      :key="index"
      :config="item"
      :style="getShapeItemShowStyle(item)"
      :show-position="showPosition"
      :search-count="searchCount"
      @userViewEnlargeOpen="userViewEnlargeOpen($event, item)"
    />
    <user-view-enlarge ref="userViewEnlargeRef"></user-view-enlarge>
  </div>
</template>

<style lang="less" scoped>
::-webkit-scrollbar {
  width: 0px !important;
  height: 0px !important;
}

.canvas-container {
  background-size: 100% 100% !important;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  position: relative;
}
</style>
