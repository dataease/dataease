<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { onMounted, reactive, computed, onUnmounted } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import router from '@/router'
import { initCanvasData } from '@/utils/canvasUtils'
import { queryTargetVisualizationJumpInfo } from '@/api/visualization/linkJump'
import { Base64 } from 'js-base64'
import { getOuterParamsInfo } from '@/api/visualization/outerParams'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { XpackComponent } from '@/components/plugin'
import { useAppStoreWithOut } from '@/store/modules/app'
const appStore = useAppStoreWithOut()

const dvMainStore = dvMainStoreWithOut()
const { t } = useI18n()
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
  },
  // JSON String 格式
  embedParams: {
    type: String,
    required: false
  }
})

// div嵌入
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
// iframe嵌入
const isIframe = computed(() => appStore.getIsIframe)

const loadCanvasDataAsync = async (dvId, dvType) => {
  const { jumpInfoParam } = router.currentRoute.value.query
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

  // 添加外部参数
  let attachParam
  await getOuterParamsInfo(dvId).then(rsp => {
    dvMainStore.setNowPanelOuterParamsInfo(rsp.data)
  })

  // 外部参数（iframe 或者 iframe嵌入）
  const attachParamsEncode = router.currentRoute.value.query.attachParams
  if (attachParamsEncode) {
    try {
      attachParam = JSON.parse(Base64.decode(decodeURIComponent(attachParamsEncode)))
    } catch (e) {
      console.error(e)
      ElMessage.error(t('visualization.outer_param_decode_error'))
    }
  }

  // div嵌入
  if (props.embedParams && isDataEaseBi.value) {
    try {
      attachParam = JSON.parse(props.embedParams)
    } catch (e) {
      console.error(e)
      ElMessage.error(t('visualization.outer_param_decode_error'))
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

// 目标校验： 需要校验targetDvId 是否是当前可视化资源ID
const winMsgHandle = event => {
  console.info('PostMessage Params Received')
  const msgInfo = event.data
  // 校验targetDvId
  if (msgInfo && msgInfo.type === 'attachParams' && msgInfo.targetDvId === state.dvInfo.id + '') {
    const attachParam = msgInfo.params
    if (attachParam) {
      dvMainStore.addOuterParamsFilter(attachParam, 'outer')
    }
  }
}

let p = null
const XpackLoaded = () => p(true)
onMounted(async () => {
  await new Promise(r => (p = r))
  const { dvId, dvType } = router.currentRoute.value.query
  if (dvId) {
    loadCanvasDataAsync(dvId, dvType)
    return
  }
  dvMainStore.setPublicLinkStatus(props.publicLinkStatus)
  window.addEventListener('message', winMsgHandle)
})

onUnmounted(() => {
  window.removeEventListener('message', winMsgHandle)
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
