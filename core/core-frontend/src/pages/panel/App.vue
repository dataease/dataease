<script setup lang="ts">
import { shallowRef, defineAsyncComponent, ref, onMounted, nextTick } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { useEmitt } from '@/hooks/web/useEmitt'

const VisualizationEditor = defineAsyncComponent(
  () => import('@/views/data-visualization/index.vue')
)
const DashboardEditor = defineAsyncComponent(() => import('@/views/dashboard/index.vue'))

const Dashboard = defineAsyncComponent(() => import('./DashboardPreview.vue'))
const ViewWrapper = defineAsyncComponent(() => import('./ViewWrapper.vue'))
const Iframe = defineAsyncComponent(() => import('./Iframe.vue'))
const Dataset = defineAsyncComponent(() => import('@/views/visualized/data/dataset/index.vue'))
const DatasetEditor = defineAsyncComponent(
  () => import('@/views/visualized/data/dataset/form/index.vue')
)
const Datasource = defineAsyncComponent(
  () => import('@/views/visualized/data/datasource/index.vue')
)
const ScreenPanel = defineAsyncComponent(() => import('@/views/data-visualization/PreviewShow.vue'))
const DashboardPanel = defineAsyncComponent(
  () => import('@/views/dashboard/DashboardPreviewShow.vue')
)

const Copilot = defineAsyncComponent(() => import('@/views/copilot/index.vue'))
const TemplateManage = defineAsyncComponent(() => import('@/views/template/indexInject.vue'))

const Preview = defineAsyncComponent(() => import('@/views/data-visualization/PreviewCanvas.vue'))
const DashboardEmpty = defineAsyncComponent(() => import('@/views/mobile/panel/DashboardEmpty.vue'))

const XpackComponent = defineAsyncComponent(() => import('@/components/plugin/src/index.vue'))

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
  Copilot,
  TemplateManage
}

const isDataFilling = ref(false)
const dataFillingPath = ref('')
const showComponent = ref(false)

const changeCurrentComponent = val => {
  isDataFilling.value = false
  showComponent.value = true
  currentComponent.value = undefined
  if (val && val.includes('DataFilling')) {
    if (val === 'DataFilling') {
      dataFillingPath.value = 'L21lbnUvZGF0YS9kYXRhLWZpbGxpbmcvbWFuYWdlL2luZGV4'
    } else if (val === 'DataFillingEditor') {
      dataFillingPath.value = 'L21lbnUvZGF0YS9kYXRhLWZpbGxpbmcvbWFuYWdlL2Zvcm0vaW5kZXg='
    } else if (val === 'DataFillingHandler') {
      dataFillingPath.value = 'L21lbnUvZGF0YS9kYXRhLWZpbGxpbmcvZmlsbC9UYWJQYW5lVGFibGU='
    }
    nextTick(() => {
      currentComponent.value = XpackComponent
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
    <component :is="currentComponent" :jsname="dataFillingPath"></component>
  </template>
</template>
