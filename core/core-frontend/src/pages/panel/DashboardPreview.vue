<script lang="ts" setup>
import { ref, reactive, onBeforeMount, nextTick } from 'vue'
import { initCanvasData } from '@/utils/canvasUtils'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useEmbedded } from '@/store/modules/embedded'
import { check } from '@/utils/CrossPermission'
import { useCache } from '@/hooks/web/useCache'
import { getOuterParamsInfo } from '@/api/visualization/outerParams'
import { ElMessage } from 'element-plus-secondary'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
const { wsCache } = useCache()
const interactiveStore = interactiveStoreWithOut()
const embeddedStore = useEmbedded()
const dashboardPreview = ref(null)
const { t } = useI18n()
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})
const dvMainStore = dvMainStoreWithOut()

const checkPer = async resourceId => {
  if (!window.DataEaseBi || !resourceId) {
    return true
  }
  const request = { busiFlag: embeddedStore.busiFlag }
  await interactiveStore.setInteractive(request)
  const key = embeddedStore.busiFlag === 'dataV' ? 'screen-weight' : 'panel-weight'
  return check(wsCache.get(key), resourceId, 1)
}
onBeforeMount(async () => {
  const checkResult = await checkPer(embeddedStore.dvId)
  if (!checkResult) {
    return
  }

  // 添加外部参数
  let attachParams
  await getOuterParamsInfo(embeddedStore.dvId).then(rsp => {
    dvMainStore.setNowPanelOuterParamsInfo(rsp.data)
  })

  // div嵌入
  if (embeddedStore.outerParams) {
    try {
      const outerPramsParse = JSON.parse(embeddedStore.outerParams)
      attachParams = outerPramsParse.attachParams
      dvMainStore.setEmbeddedCallBack(outerPramsParse.callBackFlag || 'no')
    } catch (e) {
      console.error(e)
      ElMessage.error(t('visualization.outer_param_decode_error'))
    }
  }

  initCanvasData(
    embeddedStore.dvId,
    embeddedStore.busiFlag,
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
      if (attachParams) {
        dvMainStore.addOuterParamsFilter(attachParams, canvasDataResult, 'outer')
      }
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
  width: 100%;
  height: 100%;
}
</style>
