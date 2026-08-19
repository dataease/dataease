<template>
  <div class="de-embedded-iframe-self" />
</template>
  
<script lang="ts" setup>
import { onUnmounted } from 'vue'
import { useEmbedded } from '@/store/modules/embedded'
const embeddedStore = useEmbedded()

const windowMsg = event => {
  if (event.data?.msgOrigin !== 'de-fit2cloud') {
    return
  }
  const params = {
    'de-embedded': true,
    embeddedToken: embeddedStore.getToken
  }
  const iframe = document.getElementById('iframe-de-preview-pop')
  const contentWindow = iframe['contentWindow']
  contentWindow.postMessage(params, '*')
}
const iframeInit = () => {
  window.addEventListener('message', windowMsg)
}
onUnmounted(() => {
  window.removeEventListener('message', windowMsg)
})
defineExpose({ iframeInit })

</script>