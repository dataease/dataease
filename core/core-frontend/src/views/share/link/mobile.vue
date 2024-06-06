<template>
  <div
    :class="curType === 'dashboard' ? 'mobile-link-container' : 'link-container'"
    v-loading="loading"
  >
    <LinkError v-if="!loading && !linkExist" />
    <Exp v-else-if="!loading && linkExp" />
    <PwdTips v-else-if="!loading && !pwdValid" />
    <de-preview
      ref="dashboardPreview"
      v-else-if="state.canvasStylePreview && dataInitState && curType === 'dashboard'"
      :component-data="state.canvasDataPreview"
      :canvas-style-data="state.canvasStylePreview"
      :canvas-view-info="state.canvasViewInfoPreview"
      :dv-info="state.dvInfo"
      :cur-gap="state.curPreviewGap"
    ></de-preview>
    <PreviewCanvas
      v-else-if="curType !== 'dashboard'"
      :class="{ 'hidden-link': loading }"
      ref="pcanvas"
      public-link-status="true"
    />
  </div>
</template>
<script lang="ts" setup>
import { onMounted, nextTick, ref, reactive } from 'vue'
import { initCanvasDataMobile } from '@/utils/canvasUtils'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import PreviewCanvas from '@/views/data-visualization/PreviewCanvas.vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import { ProxyInfo, shareProxy } from './ShareProxy'
import Exp from './exp.vue'
import LinkError from './error.vue'
import PwdTips from './pwd.vue'
const linkExist = ref(false)
const loading = ref(true)
const linkExp = ref(false)
const pwdValid = ref(false)
const dvMainStore = dvMainStoreWithOut()
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: {
    name: '',
    mobileLayout: false
  },
  curPreviewGap: 0
})
const dataInitState = ref(true)
const dashboardPreview = ref(null)
const loadCanvasData = (dvId, weight?) => {
  dataInitState.value = false
  initCanvasDataMobile(
    dvId,
    'dashboard',
    function ({
      canvasDataResult,
      canvasStyleResult,
      dvInfo,
      canvasViewInfoPreview,
      curPreviewGap
    }) {
      dvInfo['weight'] = weight
      state.canvasDataPreview = canvasDataResult
      state.canvasStylePreview = canvasStyleResult
      state.canvasViewInfoPreview = canvasViewInfoPreview
      state.dvInfo = dvInfo
      state.curPreviewGap = curPreviewGap
      dataInitState.value = true
      if (!state.dvInfo.mobileLayout) {
        const href = window.location.href.replace('/de-link', '/pc/de-link')
        window.location.href = href
        return
      }
      nextTick(() => {
        document.title = dvInfo.name
        dashboardPreview.value.restore()
      })
    }
  )
}

const curType = ref('')
const pcanvas = ref(null)

onMounted(async () => {
  const proxyInfo = (await shareProxy.loadProxy()) as ProxyInfo
  curType.value = proxyInfo.type || 'dashboard'
  dvMainStore.setMobileInPc(curType.value === 'dashboard')
  if (!proxyInfo?.resourceId) {
    loading.value = false
    return
  }
  linkExist.value = true
  linkExp.value = !!proxyInfo.exp
  pwdValid.value = !!proxyInfo.pwdValid
  nextTick(() => {
    if (curType.value === 'dashboard') {
      loadCanvasData(proxyInfo.resourceId, proxyInfo.type)
      dvMainStore.setPublicLinkStatus(true)
      loading.value = false
    } else {
      const method = pcanvas?.value?.loadCanvasDataAsync
      if (method) {
        method(proxyInfo.resourceId, proxyInfo.type, null)
      }
      loading.value = false
    }
  })
})
</script>
<style lang="less" scoped>
.mobile-link-container {
  width: 100vw;
  height: 100vh;
  overflow-y: auto;
  position: relative;
}
</style>
<style lang="less" scoped>
.link-container {
  position: absolute !important;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
}
.hidden-link {
  display: none !important;
}
</style>
