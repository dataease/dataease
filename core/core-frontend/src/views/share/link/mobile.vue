<template>
  <div class="mobile-link-container" v-loading="loading">
    <ErrorTemplate v-if="!loading && disableError" msg="已禁用分享功能，请联系管理员！" />
    <IframeError v-else-if="!loading && iframeError" />
    <ErrorTemplate
      v-else-if="!loading && peRequireError"
      msg="已设置有效期密码必填，当前链接无效！"
    />
    <LinkError v-else-if="!loading && !linkExist" />
    <Exp v-else-if="!loading && linkExp" />
    <PwdTips v-else-if="!loading && !pwdValid" />
    <TicketError
      v-else-if="!loading && (!state.ticketValidVO.ticketValid || state.ticketValidVO.ticketExp)"
    />
    <PreviewCanvas
      v-else
      :class="{ 'hidden-link': loading }"
      ref="pcanvas"
      public-link-status
      :ticket-args="state.ticketValidVO.args"
    />
  </div>
</template>
<script lang="ts" setup>
import { onMounted, nextTick, ref, reactive } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import PreviewCanvas from '@/views/data-visualization/PreviewCanvasMobile.vue'
import TicketError from './TicketError.vue'
import { ProxyInfo, shareProxy } from './ShareProxy'
import Exp from './exp.vue'
import LinkError from './error.vue'
import IframeError from './IframeError.vue'
import PwdTips from './pwd.vue'
import ErrorTemplate from './ErrorTemplate.vue'
const disableError = ref(true)
const peRequireError = ref(true)
const linkExist = ref(false)
const loading = ref(true)
const linkExp = ref(false)
const iframeError = ref(true)
const pwdValid = ref(false)
const dvMainStore = dvMainStoreWithOut()
const pcanvas = ref(null)
const curType = ref('')
const state = reactive({
  ticketValidVO: {
    ticketValid: false,
    ticketExp: false,
    args: ''
  }
})
onMounted(async () => {
  const proxyInfo = (await shareProxy.loadProxy()) as ProxyInfo
  curType.value = proxyInfo.type || 'dashboard'
  dvMainStore.setInMobile(true)
  dvMainStore.setMobileInPc(curType.value === 'dashboard')
  if (proxyInfo?.shareDisable) {
    loading.value = false
    disableError.value = true
    return
  }
  disableError.value = false
  if (proxyInfo?.inIframeError) {
    loading.value = false
    iframeError.value = true
    return
  }
  iframeError.value = false
  if (proxyInfo && !proxyInfo.peRequireValid) {
    loading.value = false
    peRequireError.value = true
    return
  }
  peRequireError.value = false
  if (!proxyInfo?.resourceId) {
    loading.value = false
    return
  }
  linkExist.value = true
  linkExp.value = !!proxyInfo.exp
  if (!!proxyInfo.exp) {
    loading.value = false
    return
  }
  pwdValid.value = !!proxyInfo.pwdValid
  if (!proxyInfo.pwdValid) {
    loading.value = false
    return
  }
  state.ticketValidVO = proxyInfo.ticketValidVO
  nextTick(() => {
    const method = pcanvas?.value?.loadCanvasDataAsync
    if (method) {
      method(proxyInfo.resourceId, curType.value, null)
    }
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
