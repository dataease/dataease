<script setup lang="ts">
import { shallowRef, defineAsyncComponent } from 'vue'
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

const Preview = defineAsyncComponent(() => import('@/views/data-visualization/PreviewCanvas.vue'))
const DashboardEmpty = defineAsyncComponent(() => import('@/views/mobile/panel/DashboardEmpty.vue'))

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
  DashboardEmpty
}

const changeCurrentComponent = val => {
  currentComponent.value = componentMap[val]
}

useEmitt({
  name: 'changeCurrentComponent',
  callback: changeCurrentComponent
})

currentComponent.value = componentMap[props.componentName]
</script>
<template>
  <component :is="currentComponent"></component>
</template>
