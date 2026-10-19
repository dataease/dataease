<template>
  <div class="mobile-link-container" v-loading="loading">
    <ErrorTemplate v-if="loadError" :msg="t('link_ticket.load_error')" />
    <ErrorTemplate v-else-if="!loading && disableError" :msg="t('link_ticket.disable_error')" />
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
      :class="{ 'hidden-link': loading && !previewReady }"
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
import { useI18n } from '@/hooks/web/useI18n'
import { useLoading } from '@/hooks/web/useLoading'
import { ProxyInfo, shareProxy } from './ShareProxy'
import PwdTips from './pwd.vue'
import ErrorTemplate from './ErrorTemplate.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
const appearanceStore = useAppearanceStoreWithOut()
const disableError = ref(true)
const peRequireError = ref(true)
const linkExist = ref(false)
const loading = ref(true)
const loadError = ref(false)
const previewReady = ref(false)
const linkExp = ref(false)
const iframeError = ref(true)
const pwdValid = ref(false)
const dvMainStore = dvMainStoreWithOut()
const pcanvas = ref(null)
const curType = ref('')
const { close } = useLoading()
const { t } = useI18n()
const state = reactive({
  ticketValidVO: {
    ticketValid: false,
    ticketExp: false,
    args: ''
  }
})
onMounted(async () => {
  try {
    await appearanceStore.setFontList()
    close()
    const proxyInfo = (await shareProxy.loadProxy()) as ProxyInfo
    curType.value = proxyInfo?.type || 'dashboard'
    dvMainStore.setInMobile(true)
    dvMainStore.setMobileInPc(curType.value === 'dashboard')
    if (proxyInfo?.shareDisable) {
      disableError.value = true
      return
    }
    disableError.value = false
    if (proxyInfo?.inIframeError) {
      iframeError.value = true
      return
    }
    iframeError.value = false
    if (proxyInfo && !proxyInfo.peRequireValid) {
      peRequireError.value = true
      return
    }
    peRequireError.value = false
    if (!proxyInfo) {
      return
    }
    linkExist.value = true
    linkExp.value = !!proxyInfo.exp
    if (!!proxyInfo.exp) {
      return
    }
    pwdValid.value = !!proxyInfo.pwdValid
    if (!proxyInfo.pwdValid) {
      return
    }
    state.ticketValidVO = proxyInfo.ticketValidVO ?? state.ticketValidVO
    if (!state.ticketValidVO?.ticketValid || state.ticketValidVO.ticketExp) {
      return
    }
    if (!proxyInfo.resourceId) {
      linkExist.value = false
      return
    }
    previewReady.value = true
    await nextTick()
    await pcanvas.value?.loadCanvasDataAsync(proxyInfo.resourceId, curType.value, null)
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
    close()
  }
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
