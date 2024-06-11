<template>
  <div class="mobile-link-container" v-loading="loading">
    <LinkError v-if="!loading && !linkExist" />
    <Exp v-else-if="!loading && linkExp" />
    <PwdTips v-else-if="!loading && !pwdValid" />
    <PreviewCanvas v-else :class="{ 'hidden-link': loading }" ref="pcanvas" public-link-status />
  </div>
</template>
<script lang="ts" setup>
import { onMounted, nextTick, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import PreviewCanvas from '@/views/data-visualization/PreviewCanvasMobile.vue'
import { ProxyInfo, shareProxy } from './ShareProxy'
import Exp from './exp.vue'
import router from '@/router/mobile'
import LinkError from './error.vue'
import PwdTips from './pwd.vue'
const linkExist = ref(false)
const loading = ref(true)
const linkExp = ref(false)
const pwdValid = ref(false)
const dvMainStore = dvMainStoreWithOut()
const pcanvas = ref(null)
const curType = ref('')
onMounted(async () => {
  const proxyInfo = (await shareProxy.loadProxy()) as ProxyInfo
  curType.value = proxyInfo.type || 'dashboard'
  dvMainStore.setInMobile(true)
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
      const method = pcanvas?.value?.loadCanvasDataAsync
      if (method) {
        method(proxyInfo.resourceId, 'dashboard', null)
      }
      loading.value = false
    } else {
      loading.value = false
      router.push('/dvCanvas')
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
