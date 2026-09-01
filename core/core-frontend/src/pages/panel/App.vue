<script setup lang="ts">
import { shallowRef, defineAsyncComponent, ref, onMounted, nextTick } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { useEmitt } from '@/hooks/web/useEmitt'
import ExportCenterWindow from '@/pages/panel/ExportCenterWindow.vue'

const VisualizationEditor = defineAsyncComponent(
  () => import('@/views/data-visualization/indexV3.vue')
)
const DashboardEditor = defineAsyncComponent(() => import('@/views/dashboard/index.vue'))

const Dashboard = defineAsyncComponent(() => import('./DashboardPreview.vue'))
const ViewWrapper = defineAsyncComponent(() => import('./ViewWrapper.vue'))
const Iframe = defineAsyncComponent(() => import('./Iframe.vue'))
const Dataset = defineAsyncComponent(
  () => import('@/views/pages/visualized/data/dataset/index.vue')
)
const ExportExcel = defineAsyncComponent(
  () => import('@/views/visualized/data/dataset/ExportExcel.vue')
)
const DatasetEditor = defineAsyncComponent(
  () => import('@/views/pages/visualized/data/dataset/form/index.vue')
)
const Datasource = defineAsyncComponent(
  () => import('@/views/pages/visualized/data/datasource/index.vue')
)
const ScreenPanel = defineAsyncComponent(() => import('@/views/data-visualization/PreviewShow.vue'))
const DashboardPanel = defineAsyncComponent(
  () => import('@/views/dashboard/DashboardPreviewShow.vue')
)

const TemplateManage = defineAsyncComponent(() => import('@/views/template/indexInject.vue'))

const Preview = defineAsyncComponent(() => import('@/views/data-visualization/PreviewCanvas.vue'))
const DashboardEmpty = defineAsyncComponent(() => import('@/views/mobile/panel/DashboardEmpty.vue'))
const DataFillingManage = defineAsyncComponent(
  () => import('@/views/pages/menu/data/data-filling/manage/index.vue')
)
const DataFillingForm = defineAsyncComponent(
  () => import('@/views/pages/menu/data/data-filling/manage/form/index.vue')
)
const DataFillingTabPaneTable = defineAsyncComponent(
  () => import('@/views/menu/data/data-filling/fill/TabPaneTable.vue')
)

const props = defineProps({
  componentName: propTypes.string.def('Iframe')
})
const currentComponent = shallowRef()

const componentMap = {
  DashboardEditor,
  VisualizationEditor,
  ViewWrapper,
  Preview,
  Dashboard,
  Dataset,
  Iframe,
  Datasource,
  ScreenPanel,
  DashboardPanel,
  DatasetEditor,
  DashboardEmpty,
  TemplateManage,
  ExportExcel
}

const isDataFilling = ref(false)
const showComponent = ref(false)
const dataFillingComponentMap = {
  DataFilling: DataFillingManage,
  DataFillingEditor: DataFillingForm,
  DataFillingHandler: DataFillingTabPaneTable
}

const changeCurrentComponent = val => {
  isDataFilling.value = false
  showComponent.value = true
  currentComponent.value = undefined
  if (val && val.includes('DataFilling')) {
    nextTick(() => {
      currentComponent.value = dataFillingComponentMap[val] || DataFillingManage
      isDataFilling.value = true
    })
  } else {
    nextTick(() => {
      currentComponent.value = componentMap[val]
      showComponent.value = false
    })
  }
}

useEmitt({
  name: 'changeCurrentComponent',
  callback: changeCurrentComponent
})

onMounted(() => {
  changeCurrentComponent(props.componentName)
})
</script>
<template>
  <component :is="currentComponent" v-if="!isDataFilling && !showComponent"></component>
  <template v-else>
    <component :is="currentComponent"></component>
  </template>
  <ExportCenterWindow></ExportCenterWindow>
</template>
