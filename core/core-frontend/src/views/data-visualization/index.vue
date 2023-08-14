<script setup lang="ts">
import DvCanvas from '@/components/data-visualization/canvas/index.vue'
import RealTimeComponentList from '@/components/data-visualization/RealTimeComponentList.vue'
import CanvasAttr from '@/components/data-visualization/CanvasAttr.vue'
import { changeComponentSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { computed, watch, onMounted, reactive, ref, nextTick } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import DvToolbar from '../../components/data-visualization/DvToolbar.vue'
import ComponentToolBar from '../../components/data-visualization/ComponentToolBar.vue'
import eventBus from '../../utils/eventBus'
import findComponent from '../../utils/components'
import DvSidebar from '../../components/visualization/DvSidebar.vue'
import router from '@/router'
import Editor from '@/views/chart/components/editor/index.vue'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import { getDatasetTree } from '@/api/dataset'
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import { findDragComponent, findNewComponent, initCanvasData } from '@/utils/canvasUtils'
import { ElMessage } from 'element-plus-secondary'

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()
const activeName = ref('attr')
const { componentData, curComponent, isClickComponent, canvasStyleData, canvasViewInfo, editMode } =
  storeToRefs(dvMainStore)
const { editorMap } = storeToRefs(composeStore)
const canvasOut = ref(null)
const dvLayout = ref(null)
const state = reactive({
  datasetTree: [],
  scaleHistory: 100,
  canvasId: 'canvas-main'
})

const contentStyle = computed(() => {
  const { width, height, scale } = canvasStyleData.value
  if (editMode.value === 'preview') {
    return {
      width: '100%',
      height: 'auto',
      overflow: 'hidden'
    }
  } else {
    return {
      width: width * 1.5 + 'px',
      height: (height * 2 * scale) / 100 + 'px',
      paddingTop: (height * scale) / 200 + 'px'
    }
  }
})

// 通过实时监听的方式直接添加组件
const handleNew = newComponentInfo => {
  const { componentName, innerType } = newComponentInfo
  if (componentName) {
    const component = findNewComponent(componentName, innerType)
    component.style.top = 0
    component.style.left = 0
    component.id = guid()
    changeComponentSizeWithScale(component)
    dvMainStore.addComponent({ component: component, index: undefined })
    snapshotStore.recordSnapshot('dv-handleNew')
  }
}

const handleDrop = e => {
  e.preventDefault()
  e.stopPropagation()
  const componentInfo = e.dataTransfer.getData('id')
  const rectInfo = editorMap.value[state.canvasId].getBoundingClientRect()
  if (componentInfo) {
    const component = findDragComponent(componentInfo)
    component.style.top = e.clientY - rectInfo.y
    component.style.left = e.clientX - rectInfo.x
    component.id = guid()
    changeComponentSizeWithScale(component)
    dvMainStore.addComponent({ component: component, index: 0 })
    snapshotStore.recordSnapshot('dv-handleDrop')
  }
}

const handleDragOver = e => {
  e.preventDefault()
  e.dataTransfer.dropEffect = 'copy'
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

const initDataset = () => {
  getDatasetTree({}).then(res => {
    state.datasetTree = (res as unknown as Tree[]) || []
  })
}

// 全局监听按键事件
// listenGlobalKeyDown()

const initScroll = () => {
  nextTick(() => {
    const { width, height, scale } = canvasStyleData.value
    // 设置画布初始滚动条位置
    canvasOut.value.scrollTo(
      (width * 1.5 - (width * scale) / 100) / 2 - 20,
      (height * scale) / 200 - 20
    )
  })
}

const previewScaleChange = () => {
  state.scaleHistory = canvasStyleData.value.scale
  nextTick(() => {
    let canvasWidth = dvLayout.value.clientWidth
    const previewScale = (canvasWidth * 100) / canvasStyleData.value.width
    canvasStyleData.value.scale = previewScale
  })
}

watch(
  () => editMode.value,
  val => {
    if (val === 'edit') {
      canvasStyleData.value.scale = state.scaleHistory
      initScroll()
    } else {
      previewScaleChange()
    }
  }
)

onMounted(() => {
  initDataset()
  const { dvId, pid } = window.DataEaseBi || router.currentRoute.value.query
  if (dvId) {
    initCanvasData(dvId, function () {
      // afterInit
      nextTick(() => {
        dvMainStore.setDataPrepareState(true)
        snapshotStore.recordSnapshot('dv-init')
      })
    })
  } else {
    ElMessage.error('未获取资源ID')
  }
  initScroll()
})

const previewStatus = computed(() => editMode.value === 'preview')

eventBus.on('handleNew', handleNew)
</script>

<template>
  <div ref="dvLayout" class="dv-common-layout">
    <DvToolbar />
    <el-container
      class="dv-layout-container"
      :class="{ 'preview-layout-container': previewStatus }"
    >
      <!-- 左侧组件列表 -->
      <dv-sidebar
        :title="'图层'"
        :width="180"
        :aside-position="'left'"
        :side-name="'realTimeComponent'"
        class="left-sidebar"
        :class="{ 'preview-aside': previewStatus }"
      >
        <RealTimeComponentList />
      </dv-sidebar>
      <!-- 中间画布 -->
      <main class="center">
        <div ref="canvasOut" class="content" :class="{ 'preview-content': previewStatus }">
          <div
            :style="contentStyle"
            @drop="handleDrop"
            @dragover="handleDragOver"
            @mousedown="handleMouseDown"
            @mouseup="deselectCurComponent"
          >
            <DvCanvas />
          </div>
        </div>
        <ComponentToolBar :class="{ 'preview-aside-x': previewStatus }"></ComponentToolBar>
      </main>
      <!-- 右侧侧组件列表 -->
      <dv-sidebar
        v-if="curComponent && !['UserView'].includes(curComponent.component)"
        :title="'属性'"
        :width="240"
        :side-name="'componentProp'"
        :aside-position="'right'"
        class="left-sidebar"
        :class="{ 'preview-aside': editMode === 'preview' }"
      >
        <component :is="findComponent(curComponent['component'] + 'Attr')" />
      </dv-sidebar>
      <dv-sidebar
        v-if="!curComponent"
        :title="'大屏配置'"
        :width="240"
        :side-name="'canvas'"
        :aside-position="'right'"
        class="left-sidebar"
        :class="{ 'preview-aside': editMode === 'preview' }"
      >
        <CanvasAttr></CanvasAttr>
      </dv-sidebar>
      <editor
        v-show="curComponent && ['UserView'].includes(curComponent.component)"
        :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"
        :dataset-tree="state.datasetTree"
        :class="{ 'preview-aside': editMode === 'preview' }"
      ></editor>
    </el-container>
  </div>
</template>

<style lang="less">
::-webkit-scrollbar {
  width: 0px !important;
  height: 0px !important;
}

.preview-layout-container {
  height: 100vh !important;
}

.preview-content {
  display: flex;
  align-items: center;
  overflow: hidden !important;
}

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
      background-color: rgba(51, 51, 51, 1);
      overflow: auto;
      .content {
        flex: 1;
        width: 100%;
        overflow: auto;
        margin: auto;
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

.preview-aside-x {
  height: 0px !important;
  overflow: hidden;
  padding: 0px;
}
</style>
