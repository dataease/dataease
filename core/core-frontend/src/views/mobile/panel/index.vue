<script lang="ts" setup>
import { onBeforeMount, ref, onBeforeUnmount } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
const dvMainStore = dvMainStoreWithOut()
import DePreviewMobile from './MobileInPc.vue'
const panelInit = ref(false)

const checkItemPosition = component => {
  component.x = 1
  component.sizeX = 36
  component.y = dvMainStore.componentData.reduce((pre, next) => {
    return Math.max(pre, next.y + next.sizeY)
  }, 1)
}

const hanedleMessage = event => {
  if (event.data.type === 'panelInit') {
    const { componentData, canvasStyleData, dvInfo, canvasViewInfo } = event.data.value
    dvMainStore.setComponentData(componentData.filter(ele => !!ele.inMobile))
    dvMainStore.setMobileInPc(true)
    dvMainStore.setCanvasStyle(canvasStyleData)
    dvMainStore.updateCurDvInfo(dvInfo)
    dvMainStore.setCanvasViewInfo(canvasViewInfo)
    panelInit.value = true
  }

  if (event.data.type === 'addToMobile') {
    const component = event.data.value
    checkItemPosition(component)
    dvMainStore.componentData.push(component)
  }
}

onBeforeMount(() => {
  window.top.postMessage({ type: 'panelInit', value: true }, '*')
  window.addEventListener('message', hanedleMessage)
})

onBeforeUnmount(() => {
  window.removeEventListener('message', hanedleMessage)
})
</script>

<template>
  <div class="panel-mobile">
    <de-preview-mobile v-if="panelInit"></de-preview-mobile>
  </div>
</template>

<style lang="less" scoped>
.panel-mobile {
  width: 100vw;
  height: 100vh;
  overflow-y: auto;
}
</style>
