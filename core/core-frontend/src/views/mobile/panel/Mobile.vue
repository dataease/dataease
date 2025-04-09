<script setup lang="ts">
import { reactive } from 'vue'
import icon_collection_outlined from '@/assets/svg/icon_collection_outlined.svg'
import visualStar from '@/assets/svg/visual-star.svg'
import icon_replace_outlined from '@/assets/svg/icon_replace_outlined.svg'
import { initCanvasDataMobile } from '@/utils/canvasUtils'
import { ref, nextTick, onBeforeMount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeApi, storeStatusApi } from '@/api/visualization/dataVisualization'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import VanSticky from 'vant/es/sticky'
import VanNavBar from 'vant/es/nav-bar'
import 'vant/es/nav-bar/style'
import 'vant/es/sticky/style'
import { downloadCanvas2 } from '@/utils/imgUtils'
import { useEmitt } from '@/hooks/web/useEmitt'
import CanvasOptBar from '@/components/visualization/CanvasOptBar.vue'
const dvMainStore = dvMainStoreWithOut()
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: {
    name: '',
    id: ''
  },
  curPreviewGap: 0
})
const dataInitState = ref(true)
const dashboardPreview = ref(null)
const previewCanvasContainer = ref(null)
const downloadStatus = ref(false)

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
        storeQuery()
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
  useEmitt({
    name: 'canvasDownload',
    callback: function () {
      downloadH2('img')
    }
  })
})

const downloadH2 = type => {
  downloadStatus.value = true
  nextTick(() => {
    const vueDom = previewCanvasContainer.value.querySelector('.canvas-container')
    downloadCanvas2(type, vueDom, state.dvInfo.name, () => {
      downloadStatus.value = false
    })
  })
}

const onClickLeft = () => {
  router.replace({
    path: '/index',
    query: {
      from: fromPage,
      cache: cache
    }
  })
}
const reload = () => {
  window.location.reload()
}
const favorited = ref(false)
const executeStore = () => {
  const param = {
    id: state.dvInfo.id,
    type: 'panel'
  }
  storeApi(param).then(() => {
    storeQuery()
  })
}
const storeQuery = () => {
  if (!state.dvInfo?.id) return
  storeStatusApi(state.dvInfo.id).then(res => {
    favorited.value = res.data
  })
}
</script>

<template>
  <div class="dv-common-layout-mobile" ref="previewCanvasContainer">
    <van-sticky>
      <van-nav-bar :title="state.dvInfo.name" left-arrow @click-left="onClickLeft" />
    </van-sticky>
    <div class="top-nav_refresh">
      <el-icon
        size="16"
        @click="executeStore"
        :style="{ color: favorited ? '#FFC60A' : '#646A73' }"
      >
        <icon
          ><component
            class="svg-icon"
            :is="favorited ? visualStar : icon_collection_outlined"
          ></component
        ></icon>
      </el-icon>
      <el-icon style="margin-left: 16px" @click="reload" color="#646A73" size="16"
        ><icon_replace_outlined
      /></el-icon>
    </div>
    <canvas-opt-bar
      style="top: 48px"
      canvas-id="canvas-main"
      :canvas-style-data="state.canvasStylePreview || {}"
      :component-data="state.canvasDataPreview || []"
    ></canvas-opt-bar>
    <de-preview
      ref="dashboardPreview"
      v-if="state.canvasStylePreview && dataInitState"
      :dv-info="state.dvInfo"
      :cur-gap="state.curPreviewGap"
      :component-data="state.canvasDataPreview"
      :canvas-style-data="state.canvasStylePreview"
      :canvas-view-info="state.canvasViewInfoPreview"
      :download-status="downloadStatus"
      :show-linkage-button="false"
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

  .top-nav_refresh {
    position: absolute;
    top: 14px;
    right: 24px;
    z-index: 10;
    display: flex;
    align-items: center;
  }
  #preview-canvas-main {
    height: calc(100% - 44px) !important;
  }
}
</style>
