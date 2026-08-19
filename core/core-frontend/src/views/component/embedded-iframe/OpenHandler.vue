<template>
  <div class="de-embedded-open" />
</template>
  
<script lang="ts" setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'
import { EmbeddedIntractive, interactive } from './EmbeddedIntractive'
const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()


const newWindowArray = ref<any[]>([])

const initOpenListener = event => {
  const len = newWindowArray.value.length
  let newWindow = null
  for (let index = 0; index < len; index++) {
    const element = newWindowArray.value[index]
    if (element && event.data['uuid'] === element['uuid']) {
      newWindow = element
      break
    }
  }
  if (!newWindow) {
    return
  }
  if (event.data?.msgOrigin !== 'de-inner-fit2cloud' || (appStore.getIsIframe && event.origin !== window.origin)) {
    return
  }
  const data = embeddedStore.getIframeData
  data['de-inner-embedded'] = true
  data['uuid'] = newWindow['uuid']
  if (embeddedStore.getBaseUrl) {
    data['baseUrl'] = embeddedStore.getBaseUrl
  }
  if (event.data.ready) {
    event.source.postMessage(data, '*')
  }
}
const initOpenHandler = newWindow => {
  if (!embeddedStore.getToken || !(appStore.getIsIframe || appStore.getIsDataEaseBi)) {
    return
  }
  if (appStore.getIsIframe) {
    newWindow['uuid'] = newWindow['uuid'] || new Date().getTime()
    newWindow['name'] = 'de-new-resource-window'
    newWindow['dataease-embedded-host'] = window['dataease-embedded-host']
  }
  newWindowArray.value.push(newWindow)
}
const embeddedInteractive = (param: EmbeddedIntractive) => {
  interactive(param)
}
const isDataEaseUrl = (url: string) => {
  const targetUrl = new URL(url + '', window.location.href)
  const isIframe = embeddedStore.getIsIframe
  const baseUrl = embeddedStore.getBaseUrl
  if (baseUrl) {
    return baseUrl.includes(targetUrl.origin)
  }
  if (isIframe) {
    return location.origin === targetUrl.origin
  }
  return false
}
const proxyOpen  = (url?: string | URL, target?: string, features?: string) => {
  const timestamp = new Date().getTime()
  const updatedUrl = new URL(url + '', window.location.href)
  if (isDataEaseUrl(url)) {
    const param = {
      uuid: timestamp,
      name: 'de-new-resource-window'
    }
    updatedUrl.searchParams.set('de-embedded-window-args', window.btoa(encodeURIComponent(JSON.stringify(param))))
  }
  return {
    uuid: timestamp,
    proxyWindow: window['originOpen'](updatedUrl.href, target, features)
  }
}

onMounted(() => {
  if (embeddedStore.getToken && (appStore.getIsIframe || appStore.getIsDataEaseBi)) {
    if (appStore.getIsDataEaseBi && !window['originOpen']) {
      window['originOpen'] = window.open
      window['open'] = proxyOpen
    }
    window['uuid'] = window['uuid'] || new Date().getTime()
    window.addEventListener('message', initOpenListener)
  }
})
onUnmounted(() => {
  if (embeddedStore.getToken && (appStore.getIsIframe || appStore.getIsDataEaseBi)) {
    window.removeEventListener('message', initOpenListener)
  }
})
defineExpose({ initOpenHandler, embeddedInteractive })

</script>