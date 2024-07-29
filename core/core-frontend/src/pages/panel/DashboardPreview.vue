<script lang="ts" setup>
import { ref, reactive, onBeforeMount, nextTick } from 'vue'
import { initCanvasData, initCanvasDataMobile } from '@/utils/canvasUtils'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useEmbedded } from '@/store/modules/embedded'
import { isMobile } from '@/utils/utils'
import { check } from '@/utils/CrossPermission'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useCache } from '@/hooks/web/useCache'
import { getOuterParamsInfo } from '@/api/visualization/outerParams'
import { ElMessage } from 'element-plus-secondary'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import VanSticky from 'vant/es/sticky'
import VanNavBar from 'vant/es/nav-bar'
import 'vant/es/nav-bar/style'
import 'vant/es/sticky/style'
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
const isPc = ref(true)
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

  isPc.value = !isMobile()
  const req = isPc.value ? initCanvasData : initCanvasDataMobile

  req(
    embeddedStore.dvId,
    embeddedStore.busiFlag,
    function ({
      canvasDataResult,
      canvasStyleResult,
      dvInfo,
      canvasViewInfoPreview,
      curPreviewGap
    }) {
      if (!isPc.value) {
        if (!dvInfo.mobileLayout) {
          useEmitt().emitter.emit('changeCurrentComponent', 'DashboardEmpty')
          return
        } else {
          dvMainStore.setMobileInPc(true)
          dvMainStore.setInMobile(true)
        }
      }
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
  <div
    :class="isPc ? 'dashboard-preview' : 'dv-common-layout-mobile_embedded'"
    v-if="state.canvasStylePreview"
  >
    <van-sticky v-if="!isPc">
      <van-nav-bar :title="state.dvInfo.name" />
    </van-sticky>
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
<style lang="less">
.dv-common-layout-mobile_embedded {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  --van-nav-bar-height: 44px;
  --van-nav-bar-arrow-size: 20px;
  --van-nav-bar-icon-color: #1f2329;
  --van-nav-bar-title-text-color: #1f2329;
  --van-font-bold: 500;
  --van-nav-bar-title-font-size: 17px;
}
</style>
