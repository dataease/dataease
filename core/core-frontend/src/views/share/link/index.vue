<template>
  <div
    class="link-container"
    v-loading="loading || requestStore.loadingMap[permissionStore.currentPath]"
  >
    <ErrorTemplate v-if="!loading && disableError" :msg="t('link_ticket.disable_error')" />
    <ErrorTemplate v-else-if="!loading && iframeError" :msg="t('link_ticket.iframe_error')" />
    <ErrorTemplate
      v-else-if="!loading && peRequireError"
      :msg="t('link_ticket.pe_require_error')"
    />
    <ErrorTemplate v-else-if="!loading && !linkExist" :msg="t('link_ticket.link_error')" />
    <ErrorTemplate v-else-if="!loading && linkExp" :msg="t('link_ticket.link_exp_error')" />
    <PwdTips v-else-if="!loading && !pwdValid" />
    <ErrorTemplate
      v-else-if="!loading && !state.ticketValidVO.ticketValid"
      :msg="t('link_ticket.param_error')"
    />
    <ErrorTemplate
      v-else-if="!loading && state.ticketValidVO.ticketExp"
      :msg="t('link_ticket.exp_error')"
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
import { onMounted, nextTick, ref, reactive, onBeforeUnmount } from 'vue'
import { useRequestStoreWithOut } from '@/store/modules/request'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import PreviewCanvas from '@/views/data-visualization/PreviewCanvas.vue'
import { ProxyInfo, shareProxy } from './ShareProxy'
import PwdTips from './pwd.vue'
import ErrorTemplate from './ErrorTemplate.vue'
import { useLinkStoreWithOut } from '@/store/modules/link'
import { useI18n } from '@/hooks/web/useI18n'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
const linkStore = useLinkStoreWithOut()
const requestStore = useRequestStoreWithOut()
const permissionStore = usePermissionStoreWithOut()
const dvMainStore = dvMainStoreWithOut()
const pcanvas = ref(null)
const iframeError = ref(true)
const disableError = ref(true)
const peRequireError = ref(true)
const linkExist = ref(false)
const loading = ref(true)
const linkExp = ref(false)
const pwdValid = ref(false)
const { t } = useI18n()
const state = reactive({
  ticketValidVO: {
    ticketValid: false,
    ticketExp: false,
    args: ''
  }
})
onMounted(async () => {
  const proxyInfo = (await shareProxy.loadProxy()) as ProxyInfo
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
  if (!pwdValid.value) {
    loading.value = false
    return
  }
  state.ticketValidVO = proxyInfo.ticketValidVO
  nextTick(() => {
    const method = pcanvas?.value?.loadCanvasDataAsync
    if (method) {
      method(proxyInfo.resourceId, proxyInfo.type, null)
    }
    loading.value = false
  })
  dvMainStore.setPublicLinkStatus(true)
})
onBeforeUnmount(() => {
  linkStore.$reset()
})
</script>
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
