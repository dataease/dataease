<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { nextTick, onMounted, reactive, ref } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import router from '@/router'
import { useEmitt } from '@/hooks/web/useEmitt'
import { initCanvasData } from '@/utils/canvasUtils'
import { queryTargetVisualizationJumpInfo } from '@/api/visualization/linkJump'
import { Base64 } from 'js-base64'
import { getOuterParamsInfo } from '@/api/visualization/outerParams'
import { ElMessage } from 'element-plus-secondary'
import { useEmbedded } from '@/store/modules/embedded'
import { useI18n } from '@/hooks/web/useI18n'
import { XpackComponent } from '@/components/plugin'
import { propTypes } from '@/utils/propTypes'
import { downloadCanvas2 } from '@/utils/imgUtils'
import { setTitle } from '@/utils/utils'
import EmptyBackground from '../../components/empty-background/src/EmptyBackground.vue'

const dvMainStore = dvMainStoreWithOut()
const { t } = useI18n()
const embeddedStore = useEmbedded()
const previewCanvasContainer = ref(null)
const downloadStatus = ref(false)
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0,
  initState: false
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
  },
  ticketArgs: propTypes.string.def(null)
})

const loadCanvasDataAsync = async (dvId, dvType) => {
  const jumpInfoParam = embeddedStore.jumpInfoParam || router.currentRoute.value.query.jumpInfoParam
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

  let argsObject = null
  try {
    argsObject = JSON.parse(props.ticketArgs)
  } catch (error) {
    console.error(error)
  }
  const hasTicketArgs = argsObject && Object.keys(argsObject)

  // 添加外部参数
  let attachParam
  await getOuterParamsInfo(dvId).then(rsp => {
    dvMainStore.setNowPanelOuterParamsInfo(rsp.data)
  })

  // 外部参数（iframe 或者 iframe嵌入）
  const attachParamsEncode = router.currentRoute.value.query.attachParams
  if (attachParamsEncode || hasTicketArgs) {
    try {
      if (!!attachParamsEncode) {
        attachParam = JSON.parse(Base64.decode(decodeURIComponent(attachParamsEncode)))
      }
      if (hasTicketArgs) {
        attachParam = Object.assign({}, attachParam, argsObject)
      }
    } catch (e) {
      console.error(e)
      ElMessage.error(t('visualization.outer_param_decode_error'))
    }
  }

  const initBrowserTimer = () => {
    if (state.canvasStylePreview.refreshBrowserEnable) {
      const gap = state.canvasStylePreview.refreshBrowserUnit === 'minute' ? 60 : 1
      const browserRefreshTime = state.canvasStylePreview.refreshBrowserTime * gap * 1000
      setTimeout(() => {
        window.location.reload()
      }, browserRefreshTime)
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
      state.initState = false
      state.canvasDataPreview = canvasDataResult
      state.canvasStylePreview = canvasStyleResult
      state.canvasViewInfoPreview = canvasViewInfoPreview
      state.dvInfo = dvInfo
      state.curPreviewGap = curPreviewGap
      if (jumpParam) {
        dvMainStore.addViewTrackFilter(jumpParam)
      }
      dvMainStore.addOuterParamsFilter(attachParam)
      state.initState = true
      if (props.publicLinkStatus) {
        // 设置浏览器title为当前仪表板名称
        document.title = dvInfo.name
        setTitle(dvInfo.name)
      }
      initBrowserTimer()
    }
  )
}
const downloadH2 = type => {
  downloadStatus.value = true
  nextTick(() => {
    const vueDom = previewCanvasContainer.value.querySelector('.canvas-container')
    downloadCanvas2(type, vueDom, state.dvInfo.name, () => {
      downloadStatus.value = false
    })
  })
}

let p = null
const XpackLoaded = () => p(true)
onMounted(async () => {
  useEmitt({
    name: 'canvasDownload',
    callback: function () {
      downloadH2('img')
    }
  })
  await new Promise(r => (p = r))
  const dvId = embeddedStore.dvId || router.currentRoute.value.query.dvId
  const { dvType, callBackFlag, taskId, showWatermark } = router.currentRoute.value.query
  if (!!taskId) {
    dvMainStore.setCanvasAttachInfo({ taskId, showWatermark })
  }
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
  <div class="content" ref="previewCanvasContainer">
    <de-preview
      ref="dvPreview"
      v-if="state.canvasStylePreview && state.initState"
      :component-data="state.canvasDataPreview"
      :canvas-style-data="state.canvasStylePreview"
      :canvas-view-info="state.canvasViewInfoPreview"
      :dv-info="state.dvInfo"
      :cur-gap="state.curPreviewGap"
      :is-selector="props.isSelector"
      :download-status="downloadStatus"
    ></de-preview>
    <empty-background v-if="!state.initState" description="参数不能为空" img-type="noneWhite" />
  </div>
  <XpackComponent
    jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvTmV3V2luZG93SGFuZGxlcg=="
    @loaded="XpackLoaded"
    @load-fail="XpackLoaded"
  />
</template>

<style lang="less" scoped>
.content {
  background-color: #ffffff;
  width: 100%;
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
