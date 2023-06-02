<script setup lang="ts">
import DvCanvas from '@/components/data-visualization/canvas/index.vue'
import componentList from '@/custom-component/component-list' // 左侧列表数据
import { deepCopy } from '@/utils/utils'
import generateID from '@/utils/generateID'
import { listenGlobalKeyDown } from '@/utils/shortcutKey'
import RealTimeComponentList from '@/components/data-visualization/RealTimeComponentList.vue'
import CanvasAttr from '@/components/data-visualization/CanvasAttr.vue'
import { changeComponentSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { computed, getCurrentInstance, onMounted, reactive, ref, toRefs } from 'vue'
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
import { getData } from '@/api/chart'
import { findById } from '@/api/dataVisualization'
import router from '@/router'
import Editor from '@/views/chart/components/editor/index.vue'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import { getDatasetTree } from '@/api/dataset'
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()
const activeName = ref('attr')
const { componentData, curComponent, isClickComponent, canvasStyleData, canvasViewInfo } =
  storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)
const canvasOut = ref(null)

const state = reactive({
  datasetTree: []
})

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
    const component = findNewComponent(componentName, innerType)
    component.style.top = 0
    component.style.left = 0
    component.id = guid()
    changeComponentSizeWithScale(component)
    dvMainStore.addComponent({ component: component, index: undefined })
    snapshotStore.recordSnapshot()
  }
}

const findDragComponent = componentInfo => {
  const componentInfoArray = componentInfo.split('&')
  const componentName = componentInfoArray[0]
  const innerType = componentInfoArray[1]
  return findNewComponent(componentName, innerType)
}

const handleDrop = e => {
  e.preventDefault()
  e.stopPropagation()
  const componentInfo = e.dataTransfer.getData('id')
  const rectInfo = editor.value.getBoundingClientRect()
  if (componentInfo) {
    const component = findDragComponent(componentInfo)
    component.style.top = e.clientY - rectInfo.y
    component.style.left = e.clientX - rectInfo.x
    component.id = guid()
    changeComponentSizeWithScale(component)
    dvMainStore.addComponent({ component: component, index: 0 })
    snapshotStore.recordSnapshot()
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

onMounted(() => {
  initDataset()
  const { dvId, pid } = router.currentRoute.value.query
  if (dvId) {
    // 从数据库中获取
    findById(dvId).then(res => {
      const canvasInfo = res.data
      const bashInfo = {
        id: canvasInfo.id,
        name: canvasInfo.name,
        pid: canvasInfo.pid,
        status: canvasInfo.status,
        selfWatermarkStatus: canvasInfo.selfWatermarkStatus
      }
      dvMainStore.updateCurDvInfo(bashInfo)
      //恢复画布数据
      restore(canvasInfo.componentData, canvasInfo.canvasStyleData, canvasInfo.canvasViewInfo)
    })
  } else {
    dvMainStore.updateCurDvInfo({
      id: null,
      name: '新建仪表板',
      pid: pid,
      status: null,
      selfWatermarkStatus: null
    })
  }
  const { width, height, scale } = canvasStyleData.value
  // 设置画布初始滚动条位置
  canvasOut.value.scrollTo(
    (width * 1.5 - (width * scale) / 100) / 2 - 20,
    (height * scale) / 200 - 20
  )
})

eventBus.on('handleNew', handleNew)
</script>

<template>
  <div class="dv-common-layout">
    <DvToolbar />
    <el-container class="dv-layout-container">
      <!-- 左侧组件列表 -->
      <dv-sidebar title="图层" :width="180" aside-position="left" class="left-sidebar">
        <RealTimeComponentList />
      </dv-sidebar>
      <!-- 中间画布 -->
      <main class="center">
        <div ref="canvasOut" class="content">
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
        <ComponentToolBar></ComponentToolBar>
      </main>
      <!-- 右侧侧组件列表 -->
      <dv-sidebar
        v-if="curComponent && curComponent.component !== 'UserView'"
        title="属性"
        :width="240"
        aside-position="right"
        class="left-sidebar"
      >
        <component :is="findComponent(curComponent['component'] + 'Attr')" />
      </dv-sidebar>
      <dv-sidebar
        v-if="!curComponent"
        title="大屏配置"
        :width="240"
        aside-position="right"
        class="left-sidebar"
      >
        <CanvasAttr></CanvasAttr>
      </dv-sidebar>
      <editor
        v-if="curComponent && curComponent.component === 'UserView'"
        :view="canvasViewInfo[curComponent.id]"
        :dataset-tree="state.datasetTree"
      ></editor>
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
</style>
