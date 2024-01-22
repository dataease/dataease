<script setup lang="ts">
import CanvasAttr from '@/components/data-visualization/CanvasAttr.vue'
import { computed, watch, onMounted, reactive, ref, nextTick, onUnmounted } from 'vue'
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
import CanvasCore from '@/components/data-visualization/canvas/CanvasCore.vue'
import { listenGlobalKeyDown, releaseAttachKey } from '@/utils/DeShortcutKey'
import { adaptCurThemeCommonStyle } from '@/utils/canvasStyle'
import { changeComponentSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { useEmitt } from '@/hooks/web/useEmitt'
import { check, compareStorage } from '@/utils/CrossPermission'
import { useCache } from '@/hooks/web/useCache'
import RealTimeListTree from '@/components/data-visualization/RealTimeListTree.vue'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { watermarkFind } from '@/api/watermark'
const interactiveStore = interactiveStoreWithOut()
const { wsCache } = useCache()
const eventCheck = e => {
  if (e.key === 'screen-weight' && !compareStorage(e.oldValue, e.newValue)) {
    const { dvId, opt } = window.DataEaseBi || router.currentRoute.value.query
    if (!(opt && opt === 'create')) {
      check(wsCache.get('screen-weight'), dvId, 4)
    }
  }
}
const mainCanvasCoreRef = ref(null)

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()
const {
  componentData,
  curComponent,
  isClickComponent,
  canvasStyleData,
  canvasViewInfo,
  editMode,
  dvInfo
} = storeToRefs(dvMainStore)
const { editorMap } = storeToRefs(composeStore)
const canvasOut = ref(null)
const canvasInner = ref(null)
const leftSidebarRef = ref(null)
const dvLayout = ref(null)
const canvasCenterRef = ref(null)
const state = reactive({
  datasetTree: [],
  scaleHistory: 100,
  canvasId: 'canvas-main',
  canvasInitStatus: false
})

const contentStyle = computed(() => {
  const { width, height } = canvasStyleData.value
  if (editMode.value === 'preview') {
    return {
      width: '100%',
      height: 'auto',
      overflow: 'hidden'
    }
  } else {
    return {
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      width: width * 1.5 + 'px',
      height: height * 1.5 + 'px'
    }
  }
})

// 通过实时监听的方式直接添加组件
const handleNew = newComponentInfo => {
  const { componentName, innerType } = newComponentInfo
  if (componentName) {
    const { width, height, scale } = canvasStyleData.value
    const component = findNewComponent(componentName, innerType)
    component.style.top = ((height - component.style.height) * scale) / 200
    component.style.left = ((width - component.style.width) * scale) / 200
    component.id = guid()
    changeComponentSizeWithScale(component)
    dvMainStore.addComponent({ component: component, index: undefined })
    adaptCurThemeCommonStyle(component)
    snapshotStore.recordSnapshotCache('renderChart', component.id)
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
    dvMainStore.addComponent({ component: component, index: undefined })
    adaptCurThemeCommonStyle(component)
    snapshotStore.recordSnapshotCache('renderChart', component.id)
  }
}

const handleDragOver = e => {
  e.preventDefault()
  e.dataTransfer.dropEffect = 'copy'
}

const handleMouseDown = e => {
  // e.stopPropagation()
  dvMainStore.setClickComponentStatus(false)
  // 点击画布的空区域 提前清空curComponent 防止右击菜单内容抖动
  dvMainStore.setCurComponent({ component: null, index: null })
  dvMainStore.setInEditorStatus(true)
  mainCanvasCoreRef.value.handleMouseDown(e)
}

const deselectCurComponent = e => {
  if (!isClickComponent.value) {
    curComponent.value && dvMainStore.setCurComponent({ component: null, index: null })
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
listenGlobalKeyDown()

const initScroll = () => {
  nextTick(() => {
    const { width, height } = canvasStyleData.value
    const mainWidth = canvasCenterRef.value.clientWidth
    const mainHeight = canvasCenterRef.value.clientHeight
    const scrollX = (1.5 * width - mainWidth) / 2
    const scrollY = (1.5 * height - mainHeight) / 2 + 20
    // 设置画布初始滚动条位置
    canvasOut.value.scrollTo(scrollX, scrollY)
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
const checkPer = async resourceId => {
  if (!window.DataEaseBi || !resourceId) {
    return true
  }
  const request = { busiFlag: 'dataV' }
  await interactiveStore.setInteractive(request)
  return check(wsCache.get('screen-weight'), resourceId, 4)
}
onMounted(async () => {
  window.addEventListener('blur', releaseAttachKey)
  if (editMode.value === 'edit') {
    window.addEventListener('storage', eventCheck)
  }
  const { dvId, opt, pid, createType } = window.DataEaseBi || router.currentRoute.value.query
  const checkResult = await checkPer(dvId)
  if (!checkResult) {
    return
  }
  initDataset()
  if (dvId) {
    state.canvasInitStatus = false
    const busiFlg = opt === 'copy' ? 'dataV-copy' : 'dataV'
    initCanvasData(dvId, busiFlg, function () {
      state.canvasInitStatus = true
      // afterInit
      nextTick(() => {
        dvMainStore.setDataPrepareState(true)
        snapshotStore.recordSnapshotCache('renderChart')
        if (dvInfo.value && opt === 'copy') {
          dvInfo.value.dataState = 'prepare'
          dvInfo.value.optType = 'copy'
          dvInfo.value.pid = pid
          setTimeout(() => {
            snapshotStore.recordSnapshotCache('renderChart')
          }, 1500)
        }
      })
    })
  } else if (opt && opt === 'create') {
    state.canvasInitStatus = false
    let watermarkBaseInfo
    try {
      await watermarkFind().then(rsp => {
        watermarkBaseInfo = rsp.data
        watermarkBaseInfo.settingContent = JSON.parse(watermarkBaseInfo.settingContent)
      })
    } catch (e) {
      console.error('can not find watermark info')
    }

    dvMainStore.createInit('dataV', null, pid, watermarkBaseInfo)
    nextTick(() => {
      state.canvasInitStatus = true
      dvMainStore.setDataPrepareState(true)
      snapshotStore.recordSnapshotCache('renderChart')
      // 从模板新建
      if (createType === 'template') {
        const deTemplateDataStr = wsCache.get(`de-template-data`)
        wsCache.delete('de-template-data')
        const deTemplateData = JSON.parse(deTemplateDataStr)
        dvMainStore.setComponentData(JSON.parse(deTemplateData['componentData']))
        dvMainStore.setCanvasStyle(JSON.parse(deTemplateData['canvasStyleData']))
        dvMainStore.setCanvasViewInfo(deTemplateData['canvasViewInfo'])
        setTimeout(() => {
          snapshotStore.recordSnapshotCache()
        }, 1500)
      }
    })
  } else {
    let url = '#/screen/index'
    window.open(url, '_self')
  }
  initScroll()
  useEmitt({
    name: 'initScroll',
    callback: function () {
      initScroll()
    }
  })
})

onUnmounted(() => {
  window.removeEventListener('storage', eventCheck)
  window.removeEventListener('blur', releaseAttachKey)
})

const previewStatus = computed(() => editMode.value === 'preview')

eventBus.on('handleNew', handleNew)
</script>

<template>
  <div ref="dvLayout" class="dv-common-layout">
    <DvToolbar />
    <div class="custom-dv-divider"></div>
    <el-container
      class="dv-layout-container"
      :class="{ 'preview-layout-container': previewStatus }"
    >
      <!-- 左侧组件列表 -->
      <dv-sidebar
        :title="'图层'"
        :width="180"
        :scroll-width="3"
        :aside-position="'left'"
        :side-name="'realTimeComponent'"
        class="left-sidebar"
        id="dv-main-left-sidebar"
        :class="{ 'preview-aside': previewStatus }"
      >
        <real-time-list-tree />
      </dv-sidebar>
      <!-- 中间画布 -->
      <main id="dv-main-center" class="center" ref="canvasCenterRef">
        <el-scrollbar ref="canvasOut" class="content" :class="{ 'preview-content': previewStatus }">
          <div
            id="canvas-dv-outer"
            ref="canvasInner"
            :style="contentStyle"
            @drop="handleDrop"
            @dragover="handleDragOver"
            @mousedown="handleMouseDown"
            @mouseup="deselectCurComponent"
          >
            <canvas-core
              class="canvas-area-shadow"
              v-if="state.canvasInitStatus"
              ref="mainCanvasCoreRef"
              :component-data="componentData"
              :canvas-style-data="canvasStyleData"
              :canvas-view-info="canvasViewInfo"
              :canvas-id="state.canvasId"
            ></canvas-core>
          </div>
        </el-scrollbar>
        <ComponentToolBar :class="{ 'preview-aside-x': previewStatus }"></ComponentToolBar>
      </main>
      <!-- 右侧侧组件列表 -->
      <div style="width: auto; height: 100%" ref="leftSidebarRef">
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
          v-show="!curComponent"
          :title="'大屏配置'"
          :width="240"
          :side-name="'canvas'"
          :aside-position="'right'"
          class="left-sidebar"
          :class="{ 'preview-aside': editMode === 'preview' }"
        >
          <canvas-attr></canvas-attr>
        </dv-sidebar>
        <editor
          v-show="curComponent && ['UserView'].includes(curComponent.component)"
          :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"
          themes="dark"
          :dataset-tree="state.datasetTree"
          :class="{ 'preview-aside': editMode === 'preview' }"
        ></editor>
      </div>
    </el-container>
  </div>
</template>

<style lang="less">
.preview-layout-container {
}

.preview-content {
  align-items: center;
}

.dv-common-layout {
  height: 100vh;
  width: 100vw;
  color: @dv-canvas-main-font-color;
  .dv-layout-container {
    height: calc(100vh - @top-bar-height - 1px);
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

.canvas-area-shadow {
  box-sizing: border-box;
  border: 1px solid rgba(85, 85, 85, 0.4);
}

.custom-dv-divider {
  width: 100%;
  height: 1px;
  background: #000;
}
</style>
