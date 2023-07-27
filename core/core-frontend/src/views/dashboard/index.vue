<script setup lang="ts">
import componentList, { findNewComponentFromList } from '@/custom-component/component-list' // 左侧列表数据
import { deepCopy } from '@/utils/utils'
import { listenGlobalKeyDown } from '@/utils/shortcutKey'
import { computed, nextTick, onMounted, reactive, ref, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import eventBus from '../../utils/eventBus'
import findComponent from '../../utils/components'
import DvSidebar from '../../components/visualization/DvSidebar.vue'
import router from '@/router'
import CommonCanvas from '@/components/data-visualization/canvas/index.vue'
import $ from 'jquery'
import DbToolbar from '@/components/dashboard/DbToolbar.vue'
import ViewEditor from '@/views/chart/components/editor/index.vue'
import { getDatasetTree } from '@/api/dataset'
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import elementResizeDetectorMaker from 'element-resize-detector'
import { getCanvasStyle, syncShapeItemStyle } from '@/utils/style'
import DbCanvasAttr from '@/components/dashboard/DbCanvasAttr.vue'
import { initCanvasData } from '@/utils/canvasUtils'
import { ElMessage } from 'element-plus-secondary'
import ChartStyleBatchSet from '@/views/chart/components/editor/editor-style/ChartStyleBatchSet.vue'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()
const activeName = ref('attr')
const {
  componentData,
  curComponent,
  isClickComponent,
  canvasStyleData,
  canvasViewInfo,
  pcMatrixCount,
  basePcScreenSize,
  editMode,
  batchOptStatus,
  curOriginThemes
} = storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)
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

const initDataset = () => {
  getDatasetTree({}).then(res => {
    state.datasetTree = (res as unknown as Tree[]) || []
  })
}

const contentStyle = computed(() => {
  const { width, height, scale } = canvasStyleData.value
  return {
    width: width * 1.5 + 'px',
    height: (height * 2 * scale) / 100 + 'px',
    paddingTop: (height * scale) / 200 + 'px'
  }
})

const restore = (canvasData, canvasStyle, canvasViewInfo) => {
  dvMainStore.setComponentData(JSON.parse(canvasData))
  dvMainStore.setCanvasStyle(JSON.parse(canvasStyle))
  dvMainStore.setCanvasViewInfo(canvasViewInfo)
}

const findNewComponent = (componentName, innerType) => {
  let newComponent
  componentList.forEach(comp => {
    if (comp.component === componentName) {
      newComponent = deepCopy(comp)
      newComponent.innerType = innerType
    }
  })
  return newComponent
}

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

const findDragComponent = componentInfo => {
  const componentInfoArray = componentInfo.split('&')
  const componentName = componentInfoArray[0]
  const innerType = componentInfoArray[1]
  return findNewComponentFromList(componentName, innerType, curOriginThemes)
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

const handleDragOver = e => {
  e.preventDefault()
  e.dataTransfer.dropEffect = 'copy'
  cyGridster.value.handleDragOver(e)
}

const handleMouseDown = e => {
  e.stopPropagation()
  dvMainStore.setClickComponentStatus(false)
  dvMainStore.setInEditorStatus(true)
}

const deselectCurComponent = e => {
  if (!isClickComponent.value) {
    dvMainStore.setCurComponent({ component: null, index: null })
  }
  // 0 左击 1 滚轮 2 右击
  if (e.button != 2) {
    contextmenuStore.hideContextMenu()
  }
}

const myList = ref([{ id: 491, x: 6, y: 1, sizeX: 20, sizeY: 20 }])
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
// listenGlobalKeyDown()
onMounted(() => {
  initDataset()
  const { resourceId, pid } = window.DataEaseBi || router.currentRoute.value.query
  if (resourceId) {
    initCanvasData(resourceId, function () {
      canvasInit()
    })
  } else {
    ElMessage.error('未获取资源ID')
  }
  window.addEventListener('resize', canvasSizeInit)
  const erd = elementResizeDetectorMaker()
  erd.listenTo(document.getElementById('dashboardMainCanvas'), element => {
    canvasSizeInit()
  })
})

eventBus.on('handleNew', handleNew)
</script>

<template>
  <div class="dv-common-layout dv-teleport-query">
    <DbToolbar />
    <el-container class="dv-layout-container">
      <!-- 中间画布 -->
      <main class="center">
        <div ref="canvasOut" class="content">
          <div
            id="dashboardMainCanvas"
            class="db-canvas"
            :style="editStyle"
            @drop="handleDrop"
            @dragover="handleDragOver"
            @mousedown="handleMouseDown"
            @mouseup="deselectCurComponent"
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
      </main>
      <!-- 右侧侧组件列表 -->
      <dv-sidebar
        v-if="
          curComponent &&
          !['UserView', 'VQuery'].includes(curComponent.component) &&
          !batchOptStatus
        "
        :theme-info="'light'"
        :title="'属性'"
        :width="420"
        :side-name="'componentProp'"
        :aside-position="'right'"
        class="left-sidebar"
        :class="{ 'preview-aside': editMode === 'preview' }"
      >
        <component :is="findComponent(curComponent['component'] + 'Attr')" :themes="'light'" />
      </dv-sidebar>
      <dv-sidebar
        v-show="!curComponent && !batchOptStatus"
        :theme-info="'light'"
        title="仪表板配置"
        :width="420"
        aside-position="right"
        class="left-sidebar"
        :class="{ 'preview-aside': editMode === 'preview' }"
      >
        <DbCanvasAttr></DbCanvasAttr>
      </dv-sidebar>
      <view-editor
        v-show="
          curComponent && ['UserView', 'VQuery'].includes(curComponent.component) && !batchOptStatus
        "
        :themes="'light'"
        :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"
        :dataset-tree="state.datasetTree"
        :class="{ 'preview-aside': editMode === 'preview' }"
      ></view-editor>
      <dv-sidebar
        v-if="batchOptStatus"
        :theme-info="'light'"
        title="批量操作"
        :width="280"
        aside-position="right"
        class="left-sidebar"
        :side-name="'batchOpt'"
        :class="{ 'preview-aside': editMode === 'preview' }"
      >
        <chart-style-batch-set></chart-style-batch-set>
      </dv-sidebar>
    </el-container>
  </div>
</template>

<style lang="less">
.dv-common-layout {
  height: 100vh;
  width: 100vw;
  .dv-layout-container {
    height: calc(100vh - @top-bar-height);
    .left-sidebar {
      height: 100%;
    }
    .center {
      display: flex;
      flex-direction: column;
      height: 100%;
      flex: 1;
      position: relative;
      overflow: auto;
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
    }
    .right-sidebar {
      height: 100%;
    }
  }
}

.preview-aside {
  width: 0px !important;
  overflow: hidden;
  padding: 0px;
}
</style>
