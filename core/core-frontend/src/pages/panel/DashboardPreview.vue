<script lang="ts" setup>
import { ref, reactive, onBeforeMount, nextTick } from 'vue'
import { initCanvasData } from '@/utils/canvasUtils'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { check } from '@/utils/CrossPermission'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
const interactiveStore = interactiveStoreWithOut()
const dashboardPreview = ref(null)
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})
const checkPer = async resourceId => {
  if (!window.DataEaseBi || !resourceId) {
    return true
  }
  const request = { busiFlag: window.DataEaseBi.busiFlag }
  await interactiveStore.setInteractive(request)
  const key = window.DataEaseBi.busiFlag === 'dataV' ? 'screen-weight' : 'panel-weight'
  return check(wsCache.get(key), resourceId, 1)
}
onBeforeMount(async () => {
  const checkResult = await checkPer(window.DataEaseBi.dvId)
  if (!checkResult) {
    return
  }
  initCanvasData(
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
