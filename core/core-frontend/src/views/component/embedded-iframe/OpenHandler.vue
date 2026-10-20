<template>
  <div class="de-embedded-open" />
</template>

<script lang="ts" setup>
import { shallowRef, onMounted, onUnmounted } from 'vue'
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'
import { EmbeddedIntractive, interactive } from './EmbeddedIntractive'
const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()

interface OpenedWindow {
  uuid: number
  proxyWindow: Window | null
  origin: string
}

const newWindowArray = shallowRef<OpenedWindow[]>([])

const getDataEaseOrigin = () => {
  if (appStore.getIsIframe) return window.location.origin
  if (!embeddedStore.getBaseUrl) return null
  try {
    const url = new URL(embeddedStore.getBaseUrl, window.location.href)
    return ['http:', 'https:'].includes(url.protocol) ? url.origin : null
  } catch {
    return null
  }
}

const initOpenListener = (event: MessageEvent) => {
  if (
    !event.data ||
    typeof event.data !== 'object' ||
    event.data.msgOrigin !== 'de-inner-fit2cloud' ||
    event.data.ready !== true ||
    !event.origin ||
    event.origin === 'null' ||
    !event.source
  ) {
    return
  }
  const newWindow = newWindowArray.value.find(
    item =>
      item.uuid === event.data.uuid &&
      item.proxyWindow === event.source &&
      item.origin === event.origin
  )
  if (!newWindow?.proxyWindow || !embeddedStore.getToken) {
    return
  }
  const data = embeddedStore.getIframeData
  data['de-inner-embedded'] = true
  data['uuid'] = newWindow['uuid']
  if (embeddedStore.getBaseUrl) {
    data['baseUrl'] = embeddedStore.getBaseUrl
  }
  newWindow.proxyWindow.postMessage(data, newWindow.origin)
}
const initOpenHandler = (newWindow: Window | OpenedWindow | null) => {
  if (
    !newWindow ||
    !embeddedStore.getToken ||
    !(appStore.getIsIframe || appStore.getIsDataEaseBi)
  ) {
    return
  }
  const origin = getDataEaseOrigin()
  if (!origin) return
  let openedWindow: OpenedWindow
  if (appStore.getIsIframe) {
    const iframeWindow = newWindow as Window
    newWindow['uuid'] = newWindow['uuid'] || new Date().getTime()
    iframeWindow.name = 'de-new-resource-window'
    iframeWindow['dataease-embedded-host'] = window['dataease-embedded-host']
    openedWindow = { uuid: iframeWindow['uuid'], proxyWindow: iframeWindow, origin }
  } else {
    openedWindow = newWindow as OpenedWindow
    if (!openedWindow.proxyWindow || openedWindow.origin !== origin) return
  }
  newWindowArray.value.push(openedWindow)
}
const embeddedInteractive = (param: EmbeddedIntractive) => {
  interactive(param)
}
const isDataEaseUrl = (url?: string | URL) => {
  const targetUrl = new URL(url + '', window.location.href)
  return targetUrl.origin === getDataEaseOrigin()
}
const proxyOpen = (url?: string | URL, target?: string, features?: string) => {
  const timestamp = new Date().getTime()
  const updatedUrl = new URL(url + '', window.location.href)
  if (isDataEaseUrl(url)) {
    const param = {
      uuid: timestamp,
      name: 'de-new-resource-window'
    }
    updatedUrl.searchParams.set(
      'de-embedded-window-args',
      window.btoa(encodeURIComponent(JSON.stringify(param)))
    )
  }
  return {
    uuid: timestamp,
    origin: updatedUrl.origin,
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
  window.removeEventListener('message', initOpenListener)
  newWindowArray.value = []
})
defineExpose({ initOpenHandler, embeddedInteractive })
</script>
