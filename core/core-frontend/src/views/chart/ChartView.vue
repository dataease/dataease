<script lang="ts" setup>
import { shallowRef, defineAsyncComponent } from 'vue'
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useRoute } from 'vue-router'
import { onBeforeMount } from 'vue'
const route = useRoute()
const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()

const currentComponent = shallowRef()

const VisualizationEditor = defineAsyncComponent(
  () => import('@/views/data-visualization/index.vue')
)
const DashboardEditor = defineAsyncComponent(() => import('@/views/dashboard/index.vue'))

const Dashboard = defineAsyncComponent(() => import('./DashboardPreview.vue'))
const ViewWrapper = defineAsyncComponent(() => import('./ViewWrapper.vue'))
const Dataset = defineAsyncComponent(() => import('@/views/visualized/data/dataset/index.vue'))
const Datasource = defineAsyncComponent(
  () => import('@/views/visualized/data/datasource/index.vue')
)
const ScreenPanel = defineAsyncComponent(() => import('@/views/data-visualization/PreviewShow.vue'))
const DashboardPanel = defineAsyncComponent(
  () => import('@/views/dashboard/DashboardPreviewShow.vue')
)

const componentMap = {
  DashboardEditor,
  VisualizationEditor,
  ViewWrapper,
  Dashboard,
  Dataset,
  Datasource,
  ScreenPanel,
  DashboardPanel
}
const init = () => {
  appStore.setIsIframe(true)
  const busiFlag = route.query.busiFlag as string
  const dvId = route.query.dvId as string
  const chartId = route.query.chartId as string
  const type = route.query.type as string
  const embeddedToken = route.query.embeddedToken as string
  embeddedStore.setBusiFlag(busiFlag)
  embeddedStore.setToken(embeddedToken)
  embeddedStore.setChartId(chartId)
  embeddedStore.setDvId(dvId)
  embeddedStore.setType(type)
  currentComponent.value = componentMap[type || 'ViewWrapper']
}
onBeforeMount(() => {
  init()
})
</script>

<template>
  <component :is="currentComponent"></component>
</template>
