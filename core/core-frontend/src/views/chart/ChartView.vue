<script lang="ts" setup>
import { shallowRef, defineAsyncComponent, ref, onBeforeUnmount, onBeforeMount } from 'vue'
import { debounce } from 'lodash-es'
import { XpackComponent } from '@/components/plugin'
import { useEmitt } from '@/hooks/web/useEmitt'

const currentComponent = shallowRef()

const VisualizationEditor = defineAsyncComponent(
  () => import('@/views/data-visualization/index.vue')
)
const DashboardEditor = defineAsyncComponent(() => import('@/views/dashboard/index.vue'))

const Dashboard = defineAsyncComponent(() => import('@/pages/panel/DashboardPreview.vue'))
const ViewWrapper = defineAsyncComponent(() => import('@/pages/panel/ViewWrapper.vue'))
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
const iframeStyle = ref(null)
const setStyle = debounce(() => {
  iframeStyle.value = {
    height: window.innerHeight + 'px',
    width: window.innerWidth + 'px'
  }
}, 300)
onBeforeMount(() => {
  window.addEventListener('resize', setStyle)
  setStyle()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', setStyle)
})
const initIframe = (name: string) => {
  currentComponent.value = componentMap[name || 'ViewWrapper']
}

useEmitt({
  name: 'changeCurrentComponent',
  callback: initIframe
})
</script>

<template>
  <XpackComponent
    jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvRW50cmFuY2Vz"
    @init-iframe="initIframe"
  />
  <div :style="iframeStyle">
    <component :is="currentComponent"></component>
  </div>
</template>
