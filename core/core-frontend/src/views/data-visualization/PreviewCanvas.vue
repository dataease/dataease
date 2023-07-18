<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { onMounted, reactive, ref } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import router from '@/router'
import { initCanvasData } from '@/utils/canvasUtils'

const dvMainStore = dvMainStoreWithOut()
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})

const loadCanvasData = dvId => {
  initCanvasData(
    dvId,
    function ({
      canvasDataResult,
      canvasStyleResult,
      dvInfo,
      canvasViewInfoPreview,
      curPreviewGap
    }) {
      state.canvasDataPreview = canvasDataResult
      state.canvasStylePreview = canvasStyleResult
      state.canvasViewInfoPreview = canvasViewInfoPreview
      state.dvInfo = dvInfo
      state.curPreviewGap = curPreviewGap
    }
  )
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
      ref="dvPreview"
      v-if="state.canvasStylePreview"
      :component-data="state.canvasDataPreview"
      :canvas-style-data="state.canvasStylePreview"
      :canvas-view-info="state.canvasViewInfoPreview"
      :dv-info="state.dvInfo"
      :cur-gap="state.curPreviewGap"
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
