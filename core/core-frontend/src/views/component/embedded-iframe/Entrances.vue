<template>
  <div class="de-embedded-entrances" />
</template>

<script lang="ts" setup>
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'
import { onBeforeMount, onBeforeUnmount } from 'vue'
import { initApi } from './embedded'
import { useCache } from '@/hooks/web/useCache'
import { rsaEncryp } from '@/utils/encryption'
import {  queryDekey } from '@/api/login'
import { ElMessage } from 'element-plus-secondary'
const { wsCache } = useCache()
const embeddedStore = () => useEmbedded()
const appStore = () => useAppStoreWithOut()
const emits = defineEmits(['initIframe'])
const communicationCb = async event => {
  if (!event.data['de-embedded']) {
    return
  }
  if (!wsCache.get(appStore().getDekey)) {
    const res = await queryDekey()
    wsCache.set(appStore().getDekey, res.data)
  }
  const origin = event.origin
  if(origin === window.location.origin) {
    embeddedStore().setIframeData(event.data)
    appStore().setIsIframe(true)
    window['dataease-embedded-host'] = event.source
    emits('initIframe', embeddedStore().getType)
    return
  }
  const originText = rsaEncryp(window.btoa(origin)).toString()
  initApi(event.data.embeddedToken, originText).then(res => {
    if (!res.msg) {
      embeddedStore().setIframeData(event.data)
      appStore().setIsIframe(true)
      window['dataease-embedded-host'] = event.source
      emits('initIframe', embeddedStore().getType)
    } else {
      showError()
    }
  }).catch(() => {
    showError()
  })
  
}
const showError = () => {
  ElMessage.error('未知来源，域名匹配错误！')
}
const isInIframe = () => {
  try {
    return window.top !== window.self
  } catch (error) {
    console.error(error)
    return true
  }
}
onBeforeMount(() => {
  if (window.location.href.includes('#/preview?dvId=') && (wsCache.get('user.token') || !isInIframe())) {
    emits('initIframe', false)
    return
  }
  if (embeddedStore().getToken) {
    emits('initIframe', false)
    return
  }
  if (window.location.href.includes('#/de-link/')) {
    emits('initIframe', false)
    return
  }
  window.addEventListener('message', communicationCb)
  const readyData = {
    ready: true,
    msgOrigin: 'de-fit2cloud'
  }
  window.parent.postMessage(readyData, '*')
})

onBeforeUnmount(() => {
  window.removeEventListener('message', communicationCb)
})
</script>