<script setup lang="ts">
import { findNewComponentFromList } from '@/custom-component/component-list' // 左侧列表数据
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { storeToRefs } from 'pinia'
import eventBus from '../../utils/eventBus'
import CommonCanvas from '@/components/data-visualization/canvas/index.vue'
import $ from 'jquery'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import elementResizeDetectorMaker from 'element-resize-detector'
import { getCanvasStyle, syncShapeItemStyle } from '@/utils/style'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const {
  componentData,
  curComponent,
  isClickComponent,
  canvasStyleData,
  canvasViewInfo,
  pcMatrixCount,
  editMode,
  curOriginThemes
} = storeToRefs(dvMainStore)
const canvasOut = ref(null)
const canvasInitStatus = ref(false)

const state = reactive({
  datasetTree: []
})

const editStyle = computed(() => {
  if (canvasStyleData.value) {
    return {
      ...getCanvasStyle(canvasStyleData.value)
    }
  } else {
    return {}
  }
})

// 通过实时监听的方式直接添加组件
const handleNew = newComponentInfo => {
  const { componentName, innerType } = newComponentInfo
  if (componentName) {
    const component = findNewComponentFromList(componentName, innerType, curOriginThemes)
    syncShapeItemStyle(component, baseWidth.value, baseHeight.value)
    component.id = guid()
    // changeComponentSizeWithScale(component)
    dvMainStore.addComponent({ component: component, index: undefined })
    adaptCurThemeCommonStyle(component)
    nextTick(() => {
      cyGridster.value.addItemBox(component) //在适当的时候初始化布局组件
    })
    snapshotStore.recordSnapshot('dashboard-handleNew')
  }
}

const handleDrop = e => {
  e.preventDefault()
  e.stopPropagation()
  const addComponent = cyGridster.value.getMoveItem()
  // 当前isShow =  true 则确定已经移入画布中
  addComponent.isShow = true
  syncShapeItemStyle(addComponent, baseWidth.value, baseHeight.value)
  cyGridster.value.handleMouseUp(e, addComponent, componentData.value.length - 1)
  snapshotStore.recordSnapshot('dashboard-handleDrop')
}

const handleMouseDown = e => {
  e.stopPropagation()
  dvMainStore.setClickComponentStatus(false)
  dvMainStore.setInEditorStatus(true)
}

//屏幕适配，
let screenWidth = 1920
let screenHeight = 1080
const baseWidth = ref(0)
const baseHeight = ref(0)
const renderState = ref('loading...')
const baseMarginLeft = ref(0)
const baseMarginTop = ref(0)
const cyGridster = ref(null)

const canvasInit = () => {
  nextTick(() => {
    if (canvasOut.value) {
      //div容器获取tableBox.value.clientWidth
      screenWidth = canvasOut.value.clientWidth - 4
      screenHeight = canvasOut.value.clientHeight
      baseWidth.value = screenWidth / pcMatrixCount.value.x
      baseHeight.value = screenHeight / pcMatrixCount.value.y
      baseMarginLeft.value = 0
      baseMarginTop.value = 0
      canvasInitStatus.value = true
      nextTick(() => {
        $('.dragAndResize').css('width', 'calc(100% - ' + baseMarginLeft.value + 'px)')
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
      screenWidth = canvasOut.value.clientWidth - 4
      screenHeight = canvasOut.value.clientHeight
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
      nextTick(() => {
        $('.dragAndResize').css('width', 'calc(100% - ' + baseMarginLeft.value + 'px)')
        cyGridster.value.canvasSizeInit() //在适当的时候初始化布局组件
      })
    }
  })
}

// 全局监听按键事件
onMounted(() => {
  window.addEventListener('resize', canvasSizeInit)
  const erd = elementResizeDetectorMaker()
  erd.listenTo(document.getElementById('dashboardMainCanvas'), element => {
    canvasSizeInit()
  })
})

eventBus.on('handleNew', handleNew)
</script>

<template>
  <div ref="canvasOut" class="content">
    <div
      id="dashboardMainCanvas"
      class="db-canvas"
      :style="editStyle"
      @drop="handleDrop"
      @mousedown="handleMouseDown"
    >
      <common-canvas
        ref="cyGridster"
        v-if="canvasInitStatus"
        :your-list="componentData"
        :base-margin-left="baseMarginLeft"
        :base-margin-top="baseMarginTop"
        :base-width="baseWidth"
        :base-height="baseHeight"
        :dv-model="'dashboard'"
      >
      </common-canvas>
    </div>
  </div>
</template>

<style lang="less">
.content {
  flex: 1;
  width: 100%;
  .db-canvas {
    padding: 2px;
    background-size: 100% 100% !important;
    overflow-y: auto;
    width: 100%;
    height: 100%;
  }
}
</style>
