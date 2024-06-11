<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { onMounted, reactive } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import router from '@/router/mobile'
import { initCanvasDataMobile } from '@/utils/canvasUtils'
import { queryTargetVisualizationJumpInfo } from '@/api/visualization/linkJump'
import { Base64 } from 'js-base64'
import { getOuterParamsInfo } from '@/api/visualization/outerParams'
import { ElMessage } from 'element-plus-secondary'
import { useEmbedded } from '@/store/modules/embedded'
import { useI18n } from '@/hooks/web/useI18n'
import { XpackComponent } from '@/components/plugin'

const dvMainStore = dvMainStoreWithOut()
const { t } = useI18n()
const embeddedStore = useEmbedded()
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})

const props = defineProps({
  publicLinkStatus: {
    type: Boolean,
    required: false,
    default: false
  },
  isSelector: {
    type: Boolean,
    default: false
  }
})

const loadCanvasDataAsync = async (dvId, dvType) => {
  const jumpInfoParam = embeddedStore.jumpInfoParam || router.currentRoute.value.query.jumpInfoParam
  let jumpParam
  // 获取外部跳转参数
  if (jumpInfoParam) {
    jumpParam = JSON.parse(Base64.decode(decodeURIComponent(jumpInfoParam as string)))
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

  // 添加外部参数
  let attachParam
  await getOuterParamsInfo(dvId).then(rsp => {
    dvMainStore.setNowPanelOuterParamsInfo(rsp.data)
  })

  // 外部参数（iframe 或者 iframe嵌入）
  const attachParamsEncode = router.currentRoute.value.query.attachParams
  if (attachParamsEncode) {
    try {
      attachParam = JSON.parse(Base64.decode(decodeURIComponent(attachParamsEncode as string)))
    } catch (e) {
      console.error(e)
      ElMessage.error(t('visualization.outer_param_decode_error'))
    }
  }
  initCanvasDataMobile(
    dvId,
    dvType,
    function ({
      canvasDataResult,
      canvasStyleResult,
      dvInfo,
      canvasViewInfoPreview,
      curPreviewGap
    }) {
      if (!dvInfo.mobileLayout) {
        router.push('/DashboardEmpty')
        return
      }
      state.canvasDataPreview = canvasDataResult
      state.canvasStylePreview = canvasStyleResult
      state.canvasViewInfoPreview = canvasViewInfoPreview
      state.dvInfo = dvInfo
      state.curPreviewGap = curPreviewGap
      if (jumpParam) {
        dvMainStore.addViewTrackFilter(jumpParam)
      }
      if (attachParam) {
        dvMainStore.addOuterParamsFilter(attachParam)
      }
      if (props.publicLinkStatus) {
        // 设置浏览器title为当前仪表板名称
        document.title = dvInfo.name
      }
    }
  )
}

let p = null
const XpackLoaded = () => p(true)
onMounted(async () => {
  await new Promise(r => (p = r))
  dvMainStore.setMobileInPc(true)
  dvMainStore.setInMobile(true)
  const dvId = embeddedStore.dvId || router.currentRoute.value.query.dvId
  const { dvType, callBackFlag } = router.currentRoute.value.query
  if (dvId) {
    loadCanvasDataAsync(dvId, dvType)
    return
  }
  dvMainStore.setEmbeddedCallBack(callBackFlag || 'no')
  dvMainStore.setPublicLinkStatus(props.publicLinkStatus)
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
      :is-selector="props.isSelector"
    ></de-preview>
  </div>
  <XpackComponent
    jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvTmV3V2luZG93SGFuZGxlcg=="
    @loaded="XpackLoaded"
    @load-fail="XpackLoaded"
  />
</template>

<style lang="less">
.content {
  background-color: #ffffff;
  width: 100vw;
  height: 100vh;
  align-items: center;
  overflow-x: hidden;
  overflow-y: auto;
  ::-webkit-scrollbar {
    width: 0px !important;
    height: 0px !important;
  }
}
</style>
