<script lang="ts" setup>
import { onBeforeMount, ref, onBeforeUnmount } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { XpackComponent } from '@/components/plugin'
import eventBus from '@/utils/eventBus'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import DePreviewMobile from './MobileInPc.vue'
const panelInit = ref(false)
const dvMainStore = dvMainStoreWithOut()

const checkItemPosition = component => {
  component.x = 1
  component.sizeX = 72
  component.y = dvMainStore.componentData.reduce((pre, next) => {
    return Math.max(pre, next.y + next.sizeY)
  }, 1)
  component.sizeY = 20
}

const hanedleMessage = event => {
  if (event.data.type === 'panelInit') {
    const { componentData, canvasStyleData, dvInfo, canvasViewInfo } = event.data.value
    componentData.forEach(ele => {
      const { mx, my, mSizeX, mSizeY } = ele
      ele.x = mx
      ele.y = my
      ele.sizeX = mSizeX
      ele.sizeY = mSizeY

      if (ele.component === 'DeTabs') {
        ele.propValue.forEach(tabItem => {
          tabItem.componentData.forEach(tabComponent => {
            const { mx: tx, my: ty, mSizeX: tSizeX, mSizeY: tSizeY } = tabComponent
            if (tSizeX && tSizeY) {
              tabComponent.x = tx
              tabComponent.y = ty
              tabComponent.sizeX = tSizeX
              tabComponent.sizeY = tSizeY
            }
          })
        })
      }
    })
    dvMainStore.setComponentData(componentData)
    dvMainStore.setMobileInPc(true)
    dvMainStore.setCanvasStyle(canvasStyleData)
    dvMainStore.updateCurDvInfo(dvInfo)
    dvMainStore.setCanvasViewInfo(canvasViewInfo)
    eventBus.emit('doCanvasInit-canvas-main')
    panelInit.value = true
  }

  if (event.data.type === 'addToMobile') {
    const component = event.data.value
    checkItemPosition(component)
    dvMainStore.componentData.push(component)
    eventBus.emit('doCanvasInit-canvas-main')
  }

  if (event.data.type === 'setCanvasStyle') {
    dvMainStore.setCanvasStyle(event.data.value)
  }

  if (event.data.type === 'mobileSave') {
    window.top.postMessage(
      {
        type: 'mobileSaveFromMobile',
        value: dvMainStore.componentData.reduce((pre, next) => {
          const { x, y, sizeX, sizeY, id, component } = next
          pre[id] = { x, y, sizeX, sizeY, component }
          if (next.component === 'DeTabs') {
            pre[id].tab = {}
            next.propValue.forEach(tabItem => {
              tabItem.componentData.forEach(tabComponent => {
                const { x: tx, y: ty, sizeX: tSizeX, sizeY: tSizeY, id: tId } = tabComponent
                pre[id].tab[tId] = {
                  x: tx,
                  y: ty,
                  sizeX: tSizeX,
                  sizeY: tSizeY
                }
              })
            })
          }
          return pre
        }, {})
      },
      '*'
    )
  }
}

let p = null
const XpackLoaded = () => p(true)
onBeforeMount(async () => {
  await new Promise(r => (p = r))
  window.top.postMessage({ type: 'panelInit', value: true }, '*')
  window.addEventListener('message', hanedleMessage)
  useEmitt({
    name: 'onMobileStatusChange',
    callback: ({ type, value }) => {
      mobileStatusChange(type, value)
    }
  })
})

const mobileStatusChange = (type, value) => {
  window.top.postMessage({ type, value }, '*')
  if (type === 'delFromMobile') {
    eventBus.emit('removeMatrixItemById-canvas-main', value)
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('message', hanedleMessage)
})
</script>

<template>
  <div class="panel-mobile">
    <de-preview-mobile v-if="panelInit"></de-preview-mobile>
  </div>
  <XpackComponent
    jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvTmV3V2luZG93SGFuZGxlcg=="
    @loaded="XpackLoaded"
    @load-fail="XpackLoaded"
  />
</template>

<style lang="less" scoped>
.panel-mobile {
  width: 100vw;
  height: 100vh;
  overflow-y: auto;
}
</style>
