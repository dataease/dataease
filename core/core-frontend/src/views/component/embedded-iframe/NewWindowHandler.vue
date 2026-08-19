<template>
  <div class="de-embedded-new-window" />
</template>
    
<script lang="ts" setup>
import { onMounted, onUnmounted } from 'vue'
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'
const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()
const emits = defineEmits(['loaded'])
  
  
let promiseResolve: Function = () => {}
const windowMsg = event => {
  if (
    !event.data['de-inner-embedded'] ||
    window['uuid'] !== event.data['uuid']
  ) {
    promiseResolve(null)
    return
  }
  embeddedStore.setIframeData(event.data)
  const params = new URL(window.location.href).searchParams
  if (params?.size && params.get('de-embedded-window-args')) {
    appStore.setIsDataEaseBi(true)
    if (event.data['baseUrl']) {
      embeddedStore.setBaseUrl(event.data['baseUrl'])
    }
  } else {
    appStore.setIsIframe(true)
  }
  promiseResolve(true)
}
const newWindowReady = async () => {
  const params = new URL(window.location.href).searchParams
  if (params?.size && params.get('de-embedded-window-args')) {
    const json = params.get('de-embedded-window-args')
    if (json) {
      const param = JSON.parse(decodeURIComponent(window.atob(json)))
      window['uuid'] = param['uuid']
      window['name'] = param['name']
    }
  }

  return new Promise((resolve, reject) => {
    if (!window.opener || window['name'] !== 'de-new-resource-window') {
      return resolve(null)
    }
    window.addEventListener('message', windowMsg)
    const readyData = {
      ready: true,
      msgOrigin: 'de-inner-fit2cloud',
      uuid: window['uuid']
    }
    window.opener.postMessage(readyData, '*')
    promiseResolve = resolve
  })
}
onMounted(async () => {
  await newWindowReady()
  emits('loaded')
})
onUnmounted(() => {
  if (window.opener || window['name'] === 'de-new-resource-window') {
    window.removeEventListener('message', windowMsg)
  }
})

</script>