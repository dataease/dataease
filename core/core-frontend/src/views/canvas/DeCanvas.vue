<script setup lang="ts">
import { findNewComponentFromList } from '@/custom-component/component-list' // 左侧列表数据
import { computed, nextTick, onMounted, reactive, ref, toRefs, onBeforeUnmount, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import eventBus from '../../utils/eventBus'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import elementResizeDetectorMaker from 'element-resize-detector'
import { getCanvasStyle, syncShapeItemStyle } from '@/utils/style'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'
import CanvasCore from '@/components/data-visualization/canvas/CanvasCore.vue'
import { isMainCanvas, isDashboard } from '@/utils/canvasUtils'

// change-begin
const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  },
  componentData: {
    type: Array,
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
  },
  canvasActive: {
    type: Boolean,
    default: true
  },
  outerScale: {
    type: Number,
    required: false,
    default: 1
  }
})
const { canvasStyleData, componentData, canvasViewInfo, canvasId, canvasActive, outerScale } =
  toRefs(props)
const domId = ref('de-canvas-' + canvasId.value)
// change-end

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { pcMatrixCount, curOriginThemes, mobileInPc } = storeToRefs(dvMainStore)
const canvasOut = ref(null)
const canvasInner = ref(null)
const canvasInitStatus = ref(false)
const scaleWidth = ref(100)
const scaleHeight = ref(100)
const scaleMin = ref(100)

const state = reactive({
  screenWidth: 1920,
  screenHeight: 1080
})

//仪表板矩阵信息适配，
const baseWidth = ref(0)
const baseHeight = ref(0)
const renderState = ref(false) // 仪表板默认
const baseMarginLeft = ref(0)
const baseMarginTop = ref(0)
const cyGridster = ref(null)
const editDomId = ref('edit-' + canvasId.value)

const editStyle = computed(() => {
  if (canvasStyleData.value && isMainCanvas(canvasId.value)) {
    return {
      ...getCanvasStyle(canvasStyleData.value)
    }
  } else {
    return {}
  }
})

// 通过实时监听的方式直接添加组件
const handleNewFromCanvasMain = newComponentInfo => {
  const { componentName, innerType, staticMap } = newComponentInfo
  if (componentName) {
    const component = findNewComponentFromList(componentName, innerType, curOriginThemes, staticMap)
    syncShapeItemStyle(component, baseWidth.value, baseHeight.value)
    component.id = guid()
    component.y = 200
    component.x = cyGridster.value.findPositionX(component)
    dvMainStore.addComponent({
      component: component,
      index: undefined,
      componentData: componentData.value
    })
    adaptCurThemeCommonStyle(component)
    nextTick(() => {
      cyGridster.value.addItemBox(component) //在适当的时候初始化布局组件
      scrollTo(component.y)
    })
    snapshotStore.recordSnapshotCache('renderChart', component.id)
  }
}

const handleDrop = e => {
  if (isMainCanvas(canvasId.value)) {
    e.preventDefault()
    e.stopPropagation()
    const addComponent = cyGridster.value.getMoveItem()
    // 当前isShow =  true 则确定已经移入画布中
    addComponent.isShow = true
    syncShapeItemStyle(addComponent, baseWidth.value, baseHeight.value)
    cyGridster.value.handleMouseUp(e, addComponent, componentData.value.length - 1)
    snapshotStore.recordSnapshotCache('renderChart', addComponent.id)
  }
}

const handleDragOver = e => {
  if (isMainCanvas(canvasId.value)) {
    e.preventDefault()
    e.dataTransfer.dropEffect = 'copy'
    cyGridster.value.handleDragOver(e)
  }
}

const handleMouseDown = e => {
  if (isMainCanvas(canvasId.value)) {
    e.stopPropagation()
    dvMainStore.setClickComponentStatus(false)
    dvMainStore.setInEditorStatus(true)
    dvMainStore.setCurComponent({ component: null, index: null })
  }
}

const canvasInit = (isFistLoad = true) => {
  if (canvasActive.value) {
    renderState.value = true
    setTimeout(function () {
      if (canvasOut.value) {
        dashboardCanvasSizeInit()
        nextTick(() => {
          cyGridster.value.canvasInit() //在适当的时候初始化布局组件
          cyGridster.value.afterInitOk(function () {
            renderState.value = false
          })
        })
      }
      // afterInit
      dvMainStore.setDataPrepareState(true)
      if (isMainCanvas(canvasId.value) && isFistLoad) {
        snapshotStore.recordSnapshotCache('renderChart')
      }
    }, 500)
  }
}

const canvasSizeInit = () => {
  nextTick(() => {
    if (canvasOut.value) {
      //div容器获取tableBox.value.clientWidth
      dashboardCanvasSizeInit()
      nextTick(() => {
        cyGridster.value.canvasSizeInit() //在适当的时候初始化布局组件
        // 缩放比例变化
        scaleInit()
      })
    }
  })
}

const scaleInit = () => {
  nextTick(() => {
    if (canvasOut.value) {
      //div容器获取tableBox.value.clientWidth
      let canvasWidth = canvasOut.value.clientWidth
      let canvasHeight = canvasOut.value.clientHeight
      scaleWidth.value = Math.floor((canvasWidth * 100) / canvasStyleData.value.width)
      scaleHeight.value = Math.floor((canvasHeight * 100) / canvasStyleData.value.height)
      scaleMin.value = Math.min(scaleWidth.value, scaleHeight.value)
      if (isDashboard() && isMainCanvas(canvasId.value)) {
        const offset = mobileInPc.value ? 4 : 1
        dvMainStore.setCanvasStyleScale(scaleMin.value * offset)
      }
    }
  })
}

const dashboardCanvasSizeInit = () => {
  //div容器获取tableBox.value.clientWidth
  state.screenWidth = canvasOut.value.clientWidth - 4
  state.screenHeight = canvasOut.value.clientHeight
  baseWidth.value = state.screenWidth / pcMatrixCount.value.x
  baseHeight.value = state.screenHeight / pcMatrixCount.value.y
  baseMarginLeft.value = 0
  baseMarginTop.value = 0
  canvasInitStatus.value = true
  if (isMainCanvas(canvasId.value)) {
    dvMainStore.setBashMatrixInfo({
      baseWidth: baseWidth.value,
      baseHeight: baseHeight.value,
      baseMarginLeft: baseMarginLeft.value,
      baseMarginTop: baseMarginTop.value
    })
  }
}
const addItemBox = component => {
  cyGridster.value.addItemBox(component)
}

const moveOutFromTab = component => {
  setTimeout(() => {
    component.canvasId = canvasId.value
    dvMainStore.addComponent({
      component,
      index: undefined,
      isFromGroup: true,
      componentData: componentData.value
    })
    addItemBox(component)
  }, 500)
}

// 全局监听按键事件
onMounted(() => {
  window.addEventListener('resize', canvasSizeInit)
  const erd = elementResizeDetectorMaker()
  erd.listenTo(document.getElementById(domId.value), () => {
    canvasSizeInit()
  })
  canvasInit()
  if (isMainCanvas(canvasId.value)) {
    eventBus.on('handleNew', handleNewFromCanvasMain)
  }
  eventBus.on('moveOutFromTab-' + canvasId.value, moveOutFromTab)
  eventBus.on('matrix-canvasInit', canvasInit)
})

onBeforeUnmount(() => {
  if (isMainCanvas(canvasId.value)) {
    eventBus.off('handleNew', handleNewFromCanvasMain)
  }
  eventBus.off('moveOutFromTab-' + canvasId.value, moveOutFromTab)
  eventBus.off('matrix-canvasInit', canvasInit)
})

const getBaseMatrixSize = () => {
  return {
    baseWidth: baseWidth.value,
    baseHeight: baseHeight.value
  }
}

const scrollTo = y => {
  setTimeout(() => {
    canvasInner.value.scrollTo({
      top: (y - 1) * baseHeight.value,
      behavior: 'smooth'
    })
  })
}

watch(
  () => canvasActive.value,
  () => {
    if (canvasActive.value) {
      canvasSizeInit()
    }
  }
)

defineExpose({
  addItemBox,
  getBaseMatrixSize
})
</script>

<template>
  <div ref="canvasOut" :id="editDomId" class="content" :class="{ 'render-active': renderState }">
    <canvas-opt-bar
      :canvas-style-data="canvasStyleData"
      :component-data="componentData"
      :canvas-id="canvasId"
    ></canvas-opt-bar>
    <div
      :id="domId"
      ref="canvasInner"
      class="db-canvas"
      :style="editStyle"
      @drop="handleDrop"
      @dragover="handleDragOver"
      @mousedown="handleMouseDown"
    >
      <canvas-core
        ref="cyGridster"
        v-if="canvasInitStatus"
        :component-data="componentData"
        :canvas-style-data="canvasStyleData"
        :canvas-view-info="canvasViewInfo"
        :canvas-id="canvasId"
        :base-margin-left="baseMarginLeft"
        :base-margin-top="baseMarginTop"
        :base-width="baseWidth"
        :base-height="baseHeight"
        @scrollCanvasToTop="scrollTo(1)"
      ></canvas-core>
    </div>
  </div>
</template>

<style lang="less">
.content {
  width: 100%;
  height: 100%;
  .db-canvas {
    padding: 2px;
    background-size: 100% 100% !important;
    overflow-y: auto;
    width: 100%;
    height: 100%;
  }
  ::-webkit-scrollbar {
    display: none;
  }
}

.render-active {
  opacity: 1;
}
</style>
