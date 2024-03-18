<script lang="ts" setup>
import { shallowRef, defineAsyncComponent, ref, onBeforeUnmount } from 'vue'
import { debounce } from 'lodash-es'
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'
import { onBeforeMount } from 'vue'
import { communicationInit, EmbeddedData } from '@/utils/communication'
const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()

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
  communicationInit((data: EmbeddedData) => {
    embeddedStore.setIframeData(data)
    appStore.setIsIframe(true)
    currentComponent.value = componentMap[data.type || 'ViewWrapper']
  })
  window.addEventListener('resize', setStyle)
  setStyle()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', setStyle)
})
</script>

<template>
  <div :style="iframeStyle">
    <component :is="currentComponent"></component>
  </div>
</template>
