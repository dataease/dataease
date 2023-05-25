<script setup lang="ts">
import { findById } from '@/api/dataVisualization'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { onMounted, ref } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import router from '@/router'

const dvMainStore = dvMainStoreWithOut()
const canvasDataPreview = ref(null)
const canvasStylePreview = ref(null)

const loadCanvasData = dvId => {
  findById(dvId).then(res => {
    const canvasInfo = res.data
    const bashInfo = {
      id: canvasInfo.id,
      name: canvasInfo.name,
      pid: canvasInfo.pid,
      status: canvasInfo.status,
      selfWatermarkStatus: canvasInfo.selfWatermarkStatus
    }
    canvasDataPreview.value = JSON.parse(canvasInfo.componentData)
    canvasStylePreview.value = JSON.parse(canvasInfo.canvasStyleData)
    dvMainStore.updateCurDvInfo(bashInfo)
  })
}

onMounted(() => {
  const { dvId } = router.currentRoute.value.query
  if (dvId) {
    loadCanvasData(dvId)
  }
})
</script>

<template>
  <div class="content">
    <de-preview
      v-if="canvasStylePreview"
      :component-data="canvasDataPreview"
      :canvas-style-data="canvasStylePreview"
    ></de-preview>
  </div>
</template>

<style lang="less">
.content {
  background-color: #ffffff;
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  overflow-x: hidden;
  overflow-y: auto;
}
</style>
