<script setup lang="ts">
import { findNewComponentFromList } from '@/custom-component/component-list' // 左侧列表数据
import { computed, nextTick, onMounted, reactive, ref, toRefs, onBeforeUnmount } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import eventBus from '../../utils/eventBus'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import elementResizeDetectorMaker from 'element-resize-detector'
import { getCanvasStyle, syncShapeItemStyle } from '@/utils/style'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'
import CanvasCore from '@/components/data-visualization/canvas/CanvasCore.vue'
import { canvasChangeAdaptor, isMainCanvas, isSameCanvas } from '@/utils/canvasUtils'

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
  }
})
const { canvasStyleData, componentData, canvasViewInfo, canvasId } = toRefs(props)
const domId = ref('de-canvas-' + canvasId.value)
// change-end

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { curComponent, pcMatrixCount, editMode, curOriginThemes } = storeToRefs(dvMainStore)
const canvasOut = ref(null)
const canvasInitStatus = ref(false)

const state = reactive({
  screenWidth: 1920,
  screenHeight: 1080
})

//仪表板矩阵信息适配，
const baseWidth = ref(0)
const baseHeight = ref(0)
const renderState = ref('loading...')
const baseMarginLeft = ref(0)
const baseMarginTop = ref(0)
const cyGridster = ref(null)

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
  const { componentName, innerType } = newComponentInfo
  if (componentName) {
    const component = findNewComponentFromList(componentName, innerType, curOriginThemes)
    syncShapeItemStyle(component, baseWidth.value, baseHeight.value)
    component.id = guid()
    dvMainStore.addComponent({
      component: component,
      index: undefined,
      componentData: componentData.value
    })
    adaptCurThemeCommonStyle(component)
    nextTick(() => {
      cyGridster.value.addItemBox(component) //在适当的时候初始化布局组件
    })
    snapshotStore.recordSnapshot('handleNewFromCanvasMain')
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
    snapshotStore.recordSnapshot('dashboard-handleDrop')
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
  }
}

const canvasInit = () => {
  nextTick(() => {
    if (canvasOut.value) {
      dashboardCanvasSizeInit()
      nextTick(() => {
        cyGridster.value.canvasInit() //在适当的时候初始化布局组件
        cyGridster.value.afterInitOk(function () {
          renderState.value = 'done...'
        })
      })
    }
    // afterInit
    dvMainStore.setDataPrepareState(true)
    snapshotStore.recordSnapshot('db-init')
  })
}

const canvasSizeInit = () => {
  nextTick(() => {
    if (canvasOut.value) {
      //div容器获取tableBox.value.clientWidth
      dashboardCanvasSizeInit()
      nextTick(() => {
        cyGridster.value.canvasSizeInit() //在适当的时候初始化布局组件
      })
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
  component.canvasId = canvasId.value
  dvMainStore.addComponent({
    component,
    index: undefined,
    isFromGroup: true,
    componentData: componentData.value
  })
  addItemBox(component)
}

// 全局监听按键事件
onMounted(() => {
  window.addEventListener('resize', canvasSizeInit)
  const erd = elementResizeDetectorMaker()
  erd.listenTo(document.getElementById(domId.value), element => {
    canvasSizeInit()
  })
  canvasInit()
  if (isMainCanvas(canvasId.value)) {
    eventBus.on('handleNew', handleNewFromCanvasMain)
  }
  eventBus.on('moveOutFromTab-' + canvasId.value, moveOutFromTab)
})

onBeforeUnmount(() => {
  if (isMainCanvas(canvasId.value)) {
    eventBus.off('handleNew', handleNewFromCanvasMain)
  }
  eventBus.off('moveOutFromTab-' + canvasId.value, moveOutFromTab)
})

const getBaseMatrixSize = () => {
  return {
    baseWidth: baseWidth.value,
    baseHeight: baseHeight.value
  }
}

defineExpose({
  addItemBox,
  getBaseMatrixSize
})
</script>

<template>
  <div ref="canvasOut" class="content">
    <div
      :id="domId"
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
}
</style>
