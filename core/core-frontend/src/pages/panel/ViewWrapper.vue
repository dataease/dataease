<script lang="ts" setup>
import { ref, onBeforeMount, reactive } from 'vue'
import { initCanvasDataPrepare } from '@/utils/canvasUtils'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useEmbedded } from '@/store/modules/embedded'
import { check } from '@/utils/CrossPermission'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
const interactiveStore = interactiveStoreWithOut()
const embeddedStore = useEmbedded()
const config = ref()
const viewInfo = ref()
const userViewEnlargeRef = ref()

const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0
})

const checkPer = async resourceId => {
  if (!window.DataEaseBi || !resourceId) {
    return true
  }
  const request = { busiFlag: embeddedStore.busiFlag }
  await interactiveStore.setInteractive(request)
  const key = embeddedStore.busiFlag === 'dataV' ? 'screen-weight' : 'panel-weight'
  return check(wsCache.get(key), resourceId, 1)
}
onBeforeMount(async () => {
  const checkResult = await checkPer(embeddedStore.dvId)
  if (!checkResult) {
    return
  }
  initCanvasDataPrepare(
    embeddedStore.dvId,
    embeddedStore.busiFlag,
    function ({
      canvasDataResult,
      canvasStyleResult,
      dvInfo,
      canvasViewInfoPreview,
      curPreviewGap
    }) {
      state.canvasDataPreview = canvasDataResult
      state.canvasStylePreview = canvasStyleResult
      state.canvasViewInfoPreview = canvasViewInfoPreview
      state.dvInfo = dvInfo
      state.curPreviewGap = curPreviewGap

      viewInfo.value = canvasViewInfoPreview[embeddedStore.chartId]
      ;(
        (canvasDataResult as unknown as Array<{
          id: string
          component: string
          propValue: Array<{ id: string }>
        }>) || []
      ).some(ele => {
        if (ele.id === embeddedStore.chartId) {
          config.value = ele
          return true
        }

        if (ele.component === 'Group') {
          return (ele.propValue || []).some(itx => {
            if (itx.id === embeddedStore.chartId) {
              config.value = itx
              return true
            }
            return false
          })
        }
        return false
      })
    }
  )
})
const userViewEnlargeOpen = () => {
  userViewEnlargeRef.value.dialogInit(state.canvasStylePreview, viewInfo.value, config.value)
}
</script>

<template>
  <div class="de-view-wrapper" v-if="!!config">
    <ComponentWrapper
      style="width: 100%; height: 100%"
      :view-info="viewInfo"
      :config="config"
      :canvas-style-data="state.canvasStylePreview"
      :dv-info="state.dvInfo"
      :canvas-view-info="state.canvasViewInfoPreview"
      @userViewEnlargeOpen="userViewEnlargeOpen"
    />
    <user-view-enlarge ref="userViewEnlargeRef"></user-view-enlarge>
  </div>
</template>

<style lang="less" scoped>
.de-view-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}
</style>
