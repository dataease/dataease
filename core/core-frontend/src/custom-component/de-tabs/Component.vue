<template>
  <de-full-tabs
    v-model="editableTabsValue"
    addable
    addType="dropdown"
    :dropdownMenus="menus"
    @command="handleCommand"
    @tab-remove="removeTab"
  >
    <el-tab-pane
      class="el-tab-pane-custom"
      :key="tabItem.name"
      v-for="tabItem in element.propValue"
      :label="tabItem.title"
      :name="tabItem.name"
      closable
    >
      <de-canvas
        ref="tabCanvas"
        :component-data="tabItem.componentData"
        :canvas-style-data="canvasStyleData"
        :canvas-view-info="canvasViewInfo"
        :canvas-id="element.id + '--' + tabItem.name"
      ></de-canvas>
    </el-tab-pane>
  </de-full-tabs>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, toRefs } from 'vue'
import DeFullTabs from '@/custom-component/de-tabs/DeFullTabs.vue'
import DeCanvas from '@/views/canvas/DeCanvas.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { guid } from '@/views/visualized/data/dataset/form/util'
import eventBus from '@/utils/eventBus'
import { canvasChangeAdaptor, findComponentIndexById } from '@/utils/canvasUtils'
const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasStyleData, canvasViewInfo, bashMatrixInfo } = storeToRefs(dvMainStore)
const tabCanvas = ref(null)

const props = defineProps({
  element: {
    type: Object,
    default() {
      return {
        propValue: []
      }
    }
  }
})

const { element } = toRefs(props)
const menus = [
  {
    command: 'ADD',
    label: '添加Tab'
  },
  {
    command: 'CLOSE_ALL',
    label: '关闭所有标签'
  }
]
const editableTabsValue = ref(null)

const editableTabs = ref([
  {
    name: '1',
    title: 'Tab 1',
    componentData: []
  }
]) as any

function handleCommand(name: string, obj: any) {
  if (obj.command === 'CLOSE_ALL') {
    element.value.propValue = []
    editableTabsValue.value = ''
  } else {
    const newName = guid()
    const newTab = {
      name: newName,
      title: '新建Tab',
      componentData: [],
      closable: true
    }
    element.value.propValue.push(newTab)
    editableTabsValue.value = newTab.name
  }
}
function removeTab(targetName: string) {
  let tabs = element.value.propValue
  let activeName = editableTabsValue.value
  if (activeName === targetName) {
    tabs.forEach((tab: any, index: number) => {
      if (tab.name === targetName) {
        let nextTab = tabs[index + 1] || tabs[index - 1]
        if (nextTab) {
          activeName = nextTab.name
        }
      }
    })
  }

  editableTabsValue.value = activeName
  element.value.propValue = tabs.filter((tab: any) => tab.name !== targetName)
}

const componentMoveIn = component => {
  console.log('componentMoveIn-' + JSON.stringify(component))
  const targetDomComponent = document.querySelector('#component' + component.id)
  const componentWidth = targetDomComponent.offsetWidth
  const componentHeight = targetDomComponent.offsetHeight
  element.value.propValue.forEach((tabItem, index) => {
    if (editableTabsValue.value === tabItem.name) {
      //获取主画布当前组件的index
      const curIndex = findComponentIndexById(component.id)
      // 从主画布中移除
      eventBus.emit('removeMatrixItem-canvas-main', curIndex)
      dvMainStore.setCurComponent({ component: null, index: null })
      component.canvasId = element.value.id + '--' + tabItem.name
      const matrixBase = tabCanvas.value[index].getBaseMatrixSize() //矩阵基础大小
      canvasChangeAdaptor(component, matrixBase)
      tabItem.componentData.push(component)
      nextTick(() => {
        component.x = 1
        component.y = 1
        component.style.left = 0
        component.style.top = 0
        tabCanvas.value[index].addItemBox(component) //在适当的时候初始化布局组件
      })
    }
  })
}

const componentMoveOut = component => {
  console.log('componentMoveOut-' + JSON.stringify(component))
  //获tab画布当前组件的index
  let curIndex = 0
  element.value.propValue.forEach((tabItem, index) => {
    if (editableTabsValue.value === tabItem.name) {
      //获取当前画布组件的index
      curIndex = findComponentIndexById(component.id, tabItem.componentData)
    }
  })
  canvasChangeAdaptor(component, bashMatrixInfo.value, true)
  // 从Tab画布中移除
  eventBus.emit('removeMatrixItem-' + component.canvasId, curIndex)
  // 主画布中添加
  eventBus.emit('moveOutFromTab-canvas-main', component)
}

onMounted(() => {
  if (element.value.propValue.length > 0) {
    editableTabsValue.value = element.value.propValue[0].name
  }
  eventBus.on('onTabMoveIn-' + element.value.id, componentMoveIn)
  eventBus.on('onTabMoveOut-' + element.value.id, componentMoveOut)
})
</script>
<style lang="less" scoped>
:deep(.ed-tabs__content) {
  height: calc(100% - 46px) !important;
}
.el-tab-pane-custom {
  height: 100%;
}
</style>
