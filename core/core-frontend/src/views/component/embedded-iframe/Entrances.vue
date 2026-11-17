<template>
  <div class="de-embedded-entrances" />
</template>

<script lang="ts" setup>
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'
import { onBeforeMount, onBeforeUnmount } from 'vue'
import { initApi } from './embedded'
import { useCache } from '@/hooks/web/useCache'
import { rsaEncryp, ensureDekey } from '@/utils/encryption'
import { embeddedErrorMessage } from '@/config/axios/embeddedError'
import { toDataEaseError } from '@/config/axios/error'
import { wasErrorPresented } from '@/config/axios/errorPresenter'
import { ElMessage } from 'element-plus-secondary'
const { wsCache } = useCache()
const embeddedStore = () => useEmbedded()
const appStore = () => useAppStoreWithOut()
const emits = defineEmits(['initIframe', 'authentication-failed'])
const communicationCb = async event => {
  if (!event.data['de-embedded']) {
    return
  }
  try {
    await ensureDekey()
  } catch {
    showError()
    return
  }
  const origin = event.origin
  if (origin === window.location.origin) {
    embeddedStore().setIframeData(event.data)
    appStore().setIsIframe(true)
    window['dataease-embedded-host'] = event.source
    emits('initIframe', embeddedStore().getType)
    return
  }
  const originText = rsaEncryp(window.btoa(origin)).toString()
  initApi(event.data.embeddedToken, originText)
    .then(res => {
      if (!res.msg) {
        embeddedStore().setIframeData(event.data)
        appStore().setIsIframe(true)
        window['dataease-embedded-host'] = event.source
        emits('initIframe', embeddedStore().getType)
      } else {
        showError()
      }
    })
    .catch(error => {
      if (!wasErrorPresented(error)) showError(toDataEaseError(error).messageKey)
    })
}
const showError = (messageKey?: string) => {
  ElMessage({
    type: 'error',
    message: embeddedErrorMessage(messageKey),
    grouping: true,
    showClose: true
  })
  emits('authentication-failed')
}
const isInIframe = () => {
  try {
    return window.top !== window.self
  } catch (error) {
    console.error(error)
    return true
  }
}
onBeforeMount(async () => {
  const previewPage = ['#/preview?dvId=', '#/spreadsheet-preview?'].some(path =>
    window.location.href.includes(path)
  )
  if (previewPage && (wsCache.get('user.token') || !isInIframe())) {
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
  try {
    await ensureDekey()
  } catch {
    showError()
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
