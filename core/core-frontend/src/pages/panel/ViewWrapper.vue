<script lang="ts" setup>
import { ref, onBeforeMount, reactive } from 'vue'
import { initCanvasData } from '@/utils/canvasUtils'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useEmbedded } from '@/store/modules/embedded'
import { check } from '@/utils/CrossPermission'
import { useCache } from '@/hooks/web/useCache'
import { getOuterParamsInfo } from '@/api/visualization/outerParams'
import { ElMessage } from 'element-plus-secondary'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import { XpackComponent } from '@/components/plugin'
const { wsCache } = useCache()
const interactiveStore = interactiveStoreWithOut()
const embeddedStore = useEmbedded()
const config = ref()
const viewInfo = ref()
const userViewEnlargeRef = ref()
const dvMainStore = dvMainStoreWithOut()
const { t } = useI18n()
const openHandler = ref(null)
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  chartId: null
})

// 目标校验： 需要校验targetSourceId 是否是当前可视化资源ID
const winMsgHandle = event => {
  const msgInfo = event.data
  // 校验targetSourceId
  if (msgInfo && msgInfo.type === 'attachParams' && msgInfo.targetSourceId === state.chartId + '') {
    const attachParams = msgInfo.params
    if (attachParams) {
      dvMainStore.addOuterParamsFilter(attachParams, state.canvasDataPreview, 'outer')
    }
  }
}

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
  state.chartId = embeddedStore.dvId
  window.addEventListener('message', winMsgHandle)

  // 添加外部参数
  let attachParams
  await getOuterParamsInfo(embeddedStore.dvId).then(rsp => {
    dvMainStore.setNowPanelOuterParamsInfo(rsp.data)
  })

  // div嵌入
  if (embeddedStore.outerParams) {
    try {
      const outerPramsParse = JSON.parse(embeddedStore.outerParams)
      attachParams = outerPramsParse.attachParams
      dvMainStore.setEmbeddedCallBack(outerPramsParse.callBackFlag || 'no')
    } catch (e) {
      console.error(e)
      ElMessage.error(t('visualization.outer_param_decode_error'))
    }
  }

  initCanvasData(
    embeddedStore.dvId,
    embeddedStore.busiFlag,
    function ({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview }) {
      state.canvasDataPreview = canvasDataResult
      state.canvasStylePreview = canvasStyleResult
      state.canvasViewInfoPreview = canvasViewInfoPreview
      state.dvInfo = dvInfo
      if (attachParams) {
        dvMainStore.addOuterParamsFilter(attachParams, canvasDataResult)
      }

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

const onPointClick = param => {
  try {
    console.info('de_inner_params send')
    if (window['dataease-embedded-host'] && openHandler?.value) {
      const pm = {
        methodName: 'embeddedInteractive',
        args: {
          eventName: 'de_inner_params',
          args: param
        }
      }
      openHandler.value.invokeMethod(pm)
    } else {
      console.info('de_inner_params send to host')
      const targetPm = {
        type: 'dataease-embedded-interactive',
        eventName: 'de_inner_params',
        args: param
      }
      window.parent.postMessage(targetPm, '*')
    }
  } catch (e) {
    console.warn('de_inner_params send error')
  }
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
      @onPointClick="onPointClick"
    />
    <user-view-enlarge ref="userViewEnlargeRef"></user-view-enlarge>
  </div>
  <XpackComponent ref="openHandler" jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI=" />
</template>

<style lang="less" scoped>
.de-view-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}
</style>
