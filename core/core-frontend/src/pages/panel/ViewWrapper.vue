<script lang="ts" setup>
import { ref, onBeforeMount, reactive } from 'vue'
import { initCanvasDataPrepare } from '@/utils/canvasUtils'
const config = ref()
const viewInfo = ref()
const userViewEnlargeRef = ref()

const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})
onBeforeMount(() => {
  initCanvasDataPrepare(
    window.DataEaseBi.dvId,
    window.DataEaseBi.busiFlag,
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

      viewInfo.value = canvasViewInfoPreview[window.DataEaseBi.chartId]
      config.value = ((canvasDataResult as unknown as Array<{ id: string }>) || []).find(
        ele => ele.id === window.DataEaseBi.chartId
      )
    }
  )
})
const userViewEnlargeOpen = () => {
  userViewEnlargeRef.value.dialogInit(state.canvasStylePreview, viewInfo.value, config.value)
}
</script>

<template>
  <div class="de-view-wrapper" v-if="!!config">
    <ComponentWrapper
      style="width: 100%; height: 100%"
      :view-info="viewInfo"
      :config="config"
      :canvas-style-data="state.canvasStylePreview"
      :dv-info="state.dvInfo"
      :canvas-view-info="state.canvasViewInfoPreview"
      @userViewEnlargeOpen="userViewEnlargeOpen"
    />
    <user-view-enlarge ref="userViewEnlargeRef"></user-view-enlarge>
  </div>
</template>

<style lang="less" scoped>
.de-view-wrapper {
  width: 400px;
  height: 400px;
  position: relative;
}
</style>
