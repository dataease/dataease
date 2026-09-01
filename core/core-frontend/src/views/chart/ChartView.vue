<script lang="ts" setup>
import {
  shallowRef,
  defineAsyncComponent,
  ref,
  onBeforeUnmount,
  onBeforeMount,
  onMounted,
  nextTick
} from 'vue'
import { debounce } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useLoading } from '@/hooks/web/useLoading'
import ExportCenterWindow from '@/pages/panel/ExportCenterWindow.vue'
import { useAppStoreWithOut } from '@/store/modules/app'
const Entrances = defineAsyncComponent(
  () => import('@/views/component/embedded-iframe/Entrances.vue')
)

const { close } = useLoading()
const currentComponent = shallowRef()
const appStore = useAppStoreWithOut()
const Preview = defineAsyncComponent(() => import('@/views/data-visualization/PreviewCanvas.vue'))
const VisualizationEditor = defineAsyncComponent(
  () => import('@/views/data-visualization/indexV3.vue')
)
const DashboardEditor = defineAsyncComponent(() => import('@/views/dashboard/index.vue'))

const Dashboard = defineAsyncComponent(() => import('@/pages/panel/DashboardPreview.vue'))
const ViewWrapper = defineAsyncComponent(() => import('@/pages/panel/ViewWrapper.vue'))
const Dataset = defineAsyncComponent(
  () => import('@/views/pages/visualized/data/dataset/index.vue')
)
const Datasource = defineAsyncComponent(
  () => import('@/views/pages/visualized/data/datasource/index.vue')
)

const ExportExcel = defineAsyncComponent(
  () => import('@/views/visualized/data/dataset/ExportExcel.vue')
)
const ScreenPanel = defineAsyncComponent(() => import('@/views/data-visualization/PreviewShow.vue'))
const DashboardPanel = defineAsyncComponent(
  () => import('@/views/dashboard/DashboardPreviewShow.vue')
)
const TemplateManage = defineAsyncComponent(() => import('@/views/template/indexInject.vue'))
const DataFillingManage = defineAsyncComponent(
  () => import('@/views/pages/menu/data/data-filling/manage/index.vue')
)
const DataFillingForm = defineAsyncComponent(
  () => import('@/views/pages/menu/data/data-filling/manage/form/index.vue')
)
const DataFillingTabPaneTable = defineAsyncComponent(
  () => import('@/views/menu/data/data-filling/fill/TabPaneTable.vue')
)

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
  TemplateManage,
  ExportExcel
}
const dataFillingComponentMap = {
  DataFilling: DataFillingManage,
  DataFillingEditor: DataFillingForm,
  DataFillingHandler: DataFillingTabPaneTable
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
onMounted(() => {
  close()
  document.documentElement.style.overflowX = 'hidden'
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', setStyle)
  document.documentElement.style.overflowX = ''
})

const showComponent = ref(false)

const initIframe = (name: string) => {
  showComponent.value = false
  if (name && name.includes('DataFilling')) {
    nextTick(() => {
      currentComponent.value = dataFillingComponentMap[name] || DataFillingManage
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
  <Entrances v-if="appStore.getXpackValid" @init-iframe="initIframe"></Entrances>
  <div :style="iframeStyle">
    <component :is="currentComponent" v-if="showComponent"></component>
  </div>
  <ExportCenterWindow></ExportCenterWindow>
</template>
