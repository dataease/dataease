<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { onMounted, reactive } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import router from '@/router'
import { initCanvasData } from '@/utils/canvasUtils'
import { queryTargetVisualizationJumpInfo } from '@/api/visualization/linkJump'
import { Base64 } from 'js-base64'
const dvMainStore = dvMainStoreWithOut()
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})

const loadCanvasDataAsync = async (dvId, dvType, jumpInfoParam) => {
  let jumpParam
  // 获取外部跳转参数
  if (jumpInfoParam) {
    jumpParam = JSON.parse(Base64.decode(decodeURIComponent(jumpInfoParam)))
    const jumpRequestParam = {
      sourceDvId: jumpParam.sourceDvId,
      sourceViewId: jumpParam.sourceViewId,
      sourceFieldId: null,
      targetDvId: dvId
    }
    try {
      // 刷新跳转目标仪表板联动信息
      await queryTargetVisualizationJumpInfo(jumpRequestParam).then(rsp => {
        dvMainStore.setNowTargetPanelJumpInfo(rsp.data)
      })
    } catch (e) {
      console.error(e)
    }
  }

  initCanvasData(
    dvId,
    dvType,
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
      if (jumpParam) {
        dvMainStore.addViewTrackFilter(jumpParam)
      }
    }
  )
}

onMounted(() => {
  const { dvId, dvType, jumpInfoParam } = router.currentRoute.value.query
  if (dvId) {
    loadCanvasDataAsync(dvId, dvType, jumpInfoParam)
    return
  }
})
defineExpose({
  loadCanvasDataAsync
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
::-webkit-scrollbar {
  width: 0px !important;
  height: 0px !important;
}

.content {
  background-color: #ffffff;
  width: 100%;
  height: 100vh;
  align-items: center;
  overflow-x: hidden;
  overflow-y: auto;
}
</style>
