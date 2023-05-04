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
  if (!isClickComponent) {
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
  // 设置画布初始滚动条位置
  // canvasOut.value.nav.scrollLeft += 100
})

eventBus.on('handleNew', handleNew)
</script>

<template>
  <div class="home">
    <DvToolbar />
    <main>
      <!-- 左侧组件列表 -->
      <section class="left">
        <!--        <ComponentList />-->
        <RealTimeComponentList />
      </section>
      <!-- 中间画布 -->
      <section class="center" ref="canvasOut">
        <ComponentToolBar></ComponentToolBar>
        <div
          class="content"
          :style="contentStyle"
          @drop="handleDrop"
          @dragover="handleDragOver"
          @mousedown="handleMouseDown"
          @mouseup="deselectCurComponent"
        >
          <DvCanvas />
        </div>
      </section>
      <!--右侧属性列表-->
      <section class="right">
        <el-tabs v-if="curComponent" v-model="activeName">
          <el-tab-pane label="属性" name="attr">
            <component :is="curComponent['component'] + '-attr'" />
          </el-tab-pane>
        </el-tabs>
        <CanvasAttr v-else></CanvasAttr>
      </section>
    </main>
  </div>
</template>

<style lang="less">
.home {
  height: 100vh;

  main {
    height: calc(100% - @top-bar-height);
    position: relative;

    .left {
      position: absolute;
      height: 100%;
      width: 200px;
      left: 0;
      top: 0;
    }

    .right {
      position: absolute;
      height: 100%;
      width: 288px;
      right: 0;
      top: 0;
      z-index: 20;
      .el-select {
        width: 100%;
      }
    }

    .center {
      margin-left: 200px;
      margin-right: 0px;
      height: 100%;
      overflow: auto;
      padding: 0px;
      background-color: rgba(51, 51, 51, 1);

      .content {
        overflow: auto;
        margin: auto;
      }
    }
  }

  .placeholder {
    text-align: center;
    color: #333;
  }

  .global-attr {
    padding: 10px;
  }
}
</style>
