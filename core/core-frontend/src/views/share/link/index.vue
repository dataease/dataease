<template>
  <div class="link-container" v-loading="loading">
    <IframeError v-if="!loading && iframeError" />
    <LinkError v-else-if="!loading && !linkExist" />
    <Exp v-else-if="!loading && linkExp" />
    <PwdTips v-else-if="!loading && !pwdValid" />
    <PreviewCanvas
      v-else
      :class="{ 'hidden-link': loading }"
      ref="pcanvas"
      public-link-status="true"
    />
  </div>
</template>
<script lang="ts" setup>
import { onMounted, nextTick, ref } from 'vue'
import PreviewCanvas from '@/views/data-visualization/PreviewCanvas.vue'
import { ProxyInfo, shareProxy } from './ShareProxy'
import Exp from './exp.vue'
import LinkError from './error.vue'
import PwdTips from './pwd.vue'
import IframeError from './IframeError.vue'
const pcanvas = ref(null)
const iframeError = ref(true)
const linkExist = ref(false)
const loading = ref(true)
const linkExp = ref(false)
const pwdValid = ref(false)
onMounted(async () => {
  const proxyInfo = (await shareProxy.loadProxy()) as ProxyInfo
  if (proxyInfo?.inIframeError) {
    loading.value = false
    iframeError.value = true
    return
  }
  iframeError.value = false
  if (!proxyInfo?.resourceId) {
    loading.value = false
    return
  }
  linkExist.value = true
  linkExp.value = !!proxyInfo.exp
  pwdValid.value = !!proxyInfo.pwdValid
  nextTick(() => {
    const method = pcanvas?.value?.loadCanvasDataAsync
    if (method) {
      method(proxyInfo.resourceId, proxyInfo.type, null)
    }
    loading.value = false
  })
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
