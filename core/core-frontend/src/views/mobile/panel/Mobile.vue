<script setup lang="ts">
import { reactive } from 'vue'
import { initCanvasDataMobile } from '@/utils/canvasUtils'
import { ref, nextTick, onBeforeMount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import VanSticky from 'vant/es/sticky'
import VanNavBar from 'vant/es/nav-bar'
import 'vant/es/nav-bar/style'
import 'vant/es/sticky/style'
const dvMainStore = dvMainStoreWithOut()
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: {
    name: ''
  },
  curPreviewGap: 0
})
const dataInitState = ref(true)
const dashboardPreview = ref(null)

const loadCanvasData = (dvId, weight?) => {
  dataInitState.value = false
  initCanvasDataMobile(
    dvId,
    'dashboard',
    function ({
      canvasDataResult,
      canvasStyleResult,
      dvInfo,
      canvasViewInfoPreview,
      curPreviewGap
    }) {
      dvInfo['weight'] = weight
      state.canvasDataPreview = canvasDataResult
      state.canvasStylePreview = canvasStyleResult
      state.canvasViewInfoPreview = canvasViewInfoPreview
      state.dvInfo = dvInfo
      state.curPreviewGap = curPreviewGap
      dataInitState.value = true
      nextTick(() => {
        dashboardPreview.value.restore()
      })
    }
  )
}

const route = useRoute()
const router = useRouter()
let fromPage, cache
onBeforeMount(() => {
  dvMainStore.setMobileInPc(true)
  dvMainStore.setInMobile(true)
  const dvId = route.query.dvId as unknown as string
  fromPage = route.query.from as unknown as string
  cache = route.query.cache as unknown as string
  loadCanvasData(dvId)
})

const onClickLeft = () => {
  router.replace({
    path: '/index',
    query: {
      from: fromPage,
      cache: cache
    }
  })
}
</script>

<template>
  <div class="dv-common-layout-mobile">
    <van-sticky>
      <van-nav-bar :title="state.dvInfo.name" left-arrow @click-left="onClickLeft" />
    </van-sticky>
    <de-preview
      ref="dashboardPreview"
      v-if="state.canvasStylePreview && dataInitState"
      :dv-info="state.dvInfo"
      :cur-gap="state.curPreviewGap"
      :component-data="state.canvasDataPreview"
      :canvas-style-data="state.canvasStylePreview"
      :canvas-view-info="state.canvasViewInfoPreview"
      :download-status="false"
    ></de-preview>
  </div>
</template>

<style lang="less">
.dv-common-layout-mobile {
  height: 100vh;
  width: 100vw;
  overflow-y: auto;
  --van-nav-bar-height: 44px;
  --van-nav-bar-arrow-size: 20px;
  --van-nav-bar-icon-color: #1f2329;
  --van-nav-bar-title-text-color: #1f2329;
  --van-font-bold: 500;
  --van-nav-bar-title-font-size: 17px;
}
</style>
