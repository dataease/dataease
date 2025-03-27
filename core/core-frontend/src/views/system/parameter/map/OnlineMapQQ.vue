<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

const domId = ref('de-map-container')
const mapInstance = ref(null)
const mapReloading = ref(false)

const props = defineProps<{
  mapKey: string
  securityCode?: string
}>()

const loadMap = () => {
  if (!props.mapKey) {
    return
  }
  const mykey = props.mapKey
  const url = `https://map.qq.com/api/gljs?v=1.exp&key=${mykey}`

  loadScript(url)
    .then(() => {
      if (mapInstance.value) {
        mapInstance.value?.destroy()
        mapInstance.value = null
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
        mapInstance.value.destroy()
        mapInstance.value = null
      }
    })
}
const createMapInstance = () => {
  if (window.TMap) {
    const center = new window.TMap.LatLng(39.90923, 116.397428)
    mapInstance.value = new window.TMap.Map(document.getElementById(domId.value), {
      viewMode: '2D',
      zoom: 11,
      center: center
    })
    mapInstance.value?.removeControl(window.TMap.constants.DEFAULT_CONTROL_ID.ZOOM)
    mapInstance.value?.removeControl(window.TMap.constants.DEFAULT_CONTROL_ID.ROTATION)
    mapInstance.value?.removeControl(window.TMap.constants.DEFAULT_CONTROL_ID.SCALE)
  }
}
const loadScript = (url: string) => {
  return new Promise(function (resolve, reject) {
    const scriptId = 'de-qq-script-id'
    let dom = document.getElementById(scriptId)
    if (dom) {
      dom.parentElement?.removeChild(dom)
      dom = null
      window.TMap = null
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
  const scriptId = 'de-qq-script-id'
  let dom = document.getElementById(scriptId)
  if (dom) {
    dom.parentElement?.removeChild(dom)
    dom = null
    window.TMap = null
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
