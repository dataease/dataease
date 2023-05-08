<script setup lang="ts">
import DvCanvas from '@/components/data-visualization/canvas/index.vue'
import ComponentList from '@/components/data-visualization/ComponentList.vue' // 左侧列表组件
import componentList from '@/custom-component/component-list' // 左侧列表数据
import Toolbar from '@/components/data-visualization/Toolbar.vue'
import { deepCopy } from '@/utils/utils'
import generateID from '@/utils/generateID'
import { listenGlobalKeyDown } from '@/utils/shortcutKey'
import RealTimeComponentList from '@/components/data-visualization/RealTimeComponentList.vue'
import CanvasAttr from '@/components/data-visualization/CanvasAttr.vue'
import { changeComponentSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { setDefaultComponentData } from '@/store/modules/data-visualization/snapshot'
import { computed, getCurrentInstance, onMounted, ref, toRefs } from 'vue'
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

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const composeStore = composeStoreWithOut()
const activeName = ref('attr')
const reSelectAnimateIndex = ref(undefined)
const { componentData, curComponent, isClickComponent, canvasStyleData } = storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)
const canvasOut = ref(null)

const contentStyle = computed(() => {
  const { width, height, scale } = canvasStyleData.value
  return {
    width: width * 1.5 + 'px',
    height: (height * 2 * scale) / 100 + 'px',
    paddingTop: (height * scale) / 200 + 'px'
  }
})

const restore = () => {
  // 用保存的数据恢复画布
  if (localStorage.getItem('canvasData')) {
    setDefaultComponentData(JSON.parse(localStorage.getItem('canvasData')))
    dvMainStore.setComponentData(JSON.parse(localStorage.getItem('canvasData')))
  }

  if (localStorage.getItem('canvasStyle')) {
    dvMainStore.setCanvasStyle(JSON.parse(localStorage.getItem('canvasStyle')))
  }
}

const findNewComponent = componentName => {
  let newComponent
  componentList.forEach(component => {
    if (componentName === component.component) {
      newComponent = deepCopy(component)
    }
  })
  return newComponent
}

// 通过实时监听的方式直接添加组件
const handleNew = newComponentInfo => {
  const { componentName, innerType } = newComponentInfo
  if (componentName) {
    const component = findNewComponent(componentName)
    if (componentName === 'UserView' && innerType) {
      // do something
    }
    component.style.top = 0
    component.style.left = 0
    component.id = generateID()
    changeComponentSizeWithScale(component)
    dvMainStore.addComponent({ component: component, index: undefined })
    snapshotStore.recordSnapshot()
  }
}

const handleDrop = e => {
  e.preventDefault()
  e.stopPropagation()

  const index = e.dataTransfer.getData('index')
  const rectInfo = editor.value.getBoundingClientRect()
  if (index) {
    const component = deepCopy(componentList[index])
    component.style.top = e.clientY - rectInfo.y
    component.style.left = e.clientX - rectInfo.x
    component.id = generateID()

    changeComponentSizeWithScale(component)
    dvMainStore.addComponent({ component: component, index: undefined })
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

restore()
// 全局监听按键事件
listenGlobalKeyDown()

const props = defineProps({
  dvId: {
    required: false,
    type: String
  }
})

const { dvId } = toRefs(props)

onMounted(() => {
  if (dvId.value) {
    // 从数据库中获取
  } else {
    dvMainStore.updateCurDvInfo({
      id: null,
      name: '新建仪表板',
      pid: null,
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
      <dv-sidebar title="图层" aside-position="left" class="left-sidebar">
        <RealTimeComponentList />
      </dv-sidebar>
      <!-- 中间画布 -->
      <main class="center">
        <ComponentToolBar></ComponentToolBar>
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
      </main>
      <!-- 右侧侧组件列表 -->
      <dv-sidebar v-if="curComponent" title="属性" aside-position="right" class="left-sidebar">
        <component :is="findComponent(curComponent['component'] + 'Attr')" />
      </dv-sidebar>
      <dv-sidebar
        v-if="!curComponent"
        title="全局配置"
        width="350"
        aside-position="right"
        class="left-sidebar"
      >
        <CanvasAttr></CanvasAttr>
      </dv-sidebar>
      <dv-sidebar
        v-if="curComponent && curComponent.component === 'UserView'"
        title="数据集"
        width="150"
        aside-position="right"
        class="left-sidebar"
      >
        <div>数据集</div>
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
