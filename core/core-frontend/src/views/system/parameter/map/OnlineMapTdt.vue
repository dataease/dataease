<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

const domId = ref('de-map-container')
const center: [number, number] = [116.397428, 39.90923]
const mapInstance = ref(null)
const mapReloading = ref(false)

const props = defineProps<{
  mapKey: string
  securityCode?: string
}>()

function destroyMap() {
  const container = mapInstance.value?.getContainer()
  while (container.hasChildNodes()) {
    container.removeChild(container.firstChild)
  }
  mapInstance.value = null
}

const loadMap = () => {
  if (!props.mapKey) {
    return
  }
  const mykey = props.mapKey
  const url = `https://api.tianditu.gov.cn/api?v=4.0&tk=${mykey}`

  loadScript(url)
    .then(() => {
      if (mapInstance.value) {
        destroyMap()
        mapReloading.value = true
        setTimeout(() => {
          domId.value = domId.value + '-de-'
          mapReloading.value = false
          nextTick(() => {
            createMapInstance()
          })
        }, 1500)

        return
      }
      createMapInstance()
    })
    .catch(e => {
      console.error(e)
      if (mapInstance.value) {
        destroyMap()
      }
    })
}
const createMapInstance = () => {
  if (window.T) {
    mapInstance.value = new window.T.Map(domId.value)
    mapInstance.value.centerAndZoom(new T.LngLat(center[0], center[1]), 11)
  }
}
const loadScript = (url: string) => {
  return new Promise(function (resolve, reject) {
    const scriptId = 'de-tdt-script-id'
    let dom = document.getElementById(scriptId)
    if (dom) {
      dom.parentElement?.removeChild(dom)
      dom = null
      window.AMap = null
    }
    const script = document.createElement('script')

    script.id = scriptId
    script.onload = function () {
      return resolve(null)
    }
    script.onerror = function () {
      return reject(new Error('Load script from '.concat(url, ' failed')))
    }
    script.src = url
    const head = document.head || document.getElementsByTagName('head')[0]
    ;(document.body || head).appendChild(script)
  })
}

onMounted(() => {
  loadMap()
})
onBeforeUnmount(() => {
  const scriptId = 'de-tdt-script-id'
  let dom = document.getElementById(scriptId)
  if (dom) {
    dom.parentElement?.removeChild(dom)
    dom = null
    window.T = null
  }
})
</script>

<template>
  <div class="de-map-container" v-if="!mapReloading" :id="domId" />
</template>

<style scoped lang="less">
.de-map-container {
  height: 100%;
  width: 100%;
  position: relative;
}
</style>
