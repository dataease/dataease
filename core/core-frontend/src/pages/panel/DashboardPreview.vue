<script lang="ts" setup>
import { ref, reactive, onBeforeMount, nextTick } from 'vue'
import { initCanvasData } from '@/utils/canvasUtils'

const dashboardPreview = ref(null)
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})

onBeforeMount(() => {
  initCanvasData(
    window.DataEaseBi.dvId,
    'dashboard',
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
      nextTick(() => {
        dashboardPreview.value.restore()
      })
    }
  )
})
</script>

<template>
  <div class="dashboard-preview" v-if="state.canvasStylePreview">
    <de-preview
      ref="dashboardPreview"
      :dv-info="state.dvInfo"
      :cur-gap="state.curPreviewGap"
      :component-data="state.canvasDataPreview"
      :canvas-style-data="state.canvasStylePreview"
      :canvas-view-info="state.canvasViewInfoPreview"
      show-position="preview"
    ></de-preview>
  </div>
</template>

<style lang="less" scoped>
.dashboard-preview {
  width: 1000px;
  height: 800px;
}
</style>
