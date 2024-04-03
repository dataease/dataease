<template>
  <div class="mobile-link-container" v-loading="loading">
    <LinkError v-if="!loading && !linkExist" />
    <Exp v-else-if="!loading && linkExp" />
    <PwdTips v-else-if="!loading && !pwdValid" />
    <de-preview
      ref="dashboardPreview"
      v-else-if="state.canvasStylePreview && dataInitState"
      :component-data="state.canvasDataPreview"
      :canvas-style-data="state.canvasStylePreview"
      :canvas-view-info="state.canvasViewInfoPreview"
      :dv-info="state.dvInfo"
      :cur-gap="state.curPreviewGap"
    ></de-preview>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, nextTick, ref, reactive } from 'vue'
import { initCanvasDataMobile } from '@/utils/canvasUtils'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
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
onMounted(async () => {
  dvMainStore.setMobileInPc(true)
  const proxyInfo = (await shareProxy.loadProxy()) as ProxyInfo
  if (!proxyInfo?.resourceId) {
    loading.value = false
    return
  }
  linkExist.value = true
  linkExp.value = !!proxyInfo.exp
  pwdValid.value = !!proxyInfo.pwdValid
  nextTick(() => {
    loadCanvasData(proxyInfo.resourceId, proxyInfo.type)
    dvMainStore.setPublicLinkStatus(true)
    loading.value = false
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
