<script lang="ts" setup>
import {
  shallowRef,
  defineAsyncComponent,
  ref,
  onBeforeUnmount,
  onBeforeMount,
  nextTick
} from 'vue'
import { debounce } from 'lodash-es'
import { XpackComponent } from '@/components/plugin'
import { useEmitt } from '@/hooks/web/useEmitt'

const currentComponent = shallowRef()
const Preview = defineAsyncComponent(() => import('@/views/data-visualization/PreviewCanvas.vue'))
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
const Copilot = defineAsyncComponent(() => import('@/views/copilot/index.vue'))
const TemplateManage = defineAsyncComponent(() => import('@/views/template/indexInject.vue'))

const AsyncXpackComponent = defineAsyncComponent(() => import('@/components/plugin/src/index.vue'))

const componentMap = {
  DashboardEditor,
  VisualizationEditor,
  ViewWrapper,
  Preview,
  Dashboard,
  Dataset,
  Datasource,
  ScreenPanel,
  DashboardPanel,
  Copilot,
  TemplateManage
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

const showComponent = ref(false)
const dataFillingPath = ref('')

const initIframe = (name: string) => {
  showComponent.value = false
  if (name && name.includes('DataFilling')) {
    if (name === 'DataFilling') {
      dataFillingPath.value = 'L21lbnUvZGF0YS9kYXRhLWZpbGxpbmcvbWFuYWdlL2luZGV4'
    } else if (name === 'DataFillingEditor') {
      dataFillingPath.value = 'L21lbnUvZGF0YS9kYXRhLWZpbGxpbmcvbWFuYWdlL2Zvcm0vaW5kZXg='
    } else if (name === 'DataFillingHandler') {
      dataFillingPath.value = 'L21lbnUvZGF0YS9kYXRhLWZpbGxpbmcvZmlsbC9UYWJQYW5lVGFibGU='
    }
    nextTick(() => {
      currentComponent.value = AsyncXpackComponent
      showComponent.value = true
    })
  } else {
    nextTick(() => {
      currentComponent.value = componentMap[name || 'ViewWrapper']
      showComponent.value = true
    })
  }
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
    <component :is="currentComponent" :jsname="dataFillingPath" v-if="showComponent"></component>
  </div>
</template>
