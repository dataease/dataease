<script setup lang="ts">
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import DeResourceTree from '@/views/common/DeResourceTree.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { reactive, nextTick, ref, toRefs, onBeforeMount, computed, onMounted } from 'vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import PreviewHead from '@/views/data-visualization/PreviewHead.vue'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import ArrowSide from '@/views/common/DeResourceArrow.vue'
import { initCanvasData, initCanvasDataPrepare, onInitReady } from '@/utils/canvasUtils'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { Icon } from '@/components/icon-custom'
import { download2AppTemplate, downloadCanvas2 } from '@/utils/imgUtils'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus-secondary'
import AppExportForm from '@/components/de-app/AppExportForm.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useUserStoreWithOut } from '@/store/modules/user'
import { useI18n } from '@/hooks/web/useI18n'
import CanvasOptBar from '@/components/visualization/CanvasOptBar.vue'
const userStore = useUserStoreWithOut()

const userName = computed(() => userStore.getName)
const appExportFormRef = ref(null)

const dvMainStore = dvMainStoreWithOut()
const previewCanvasContainer = ref(null)
const dashboardPreview = ref(null)
const slideShow = ref(true)
const appStore = useAppStoreWithOut()
const dataInitState = ref(true)
const downloadStatus = ref(false)
const state = reactive({
  canvasDataPreview: null,
  canvasStylePreview: null,
  canvasViewInfoPreview: null,
  dvInfo: null,
  curPreviewGap: 0,
  showOffset: {
    top: 110,
    left: 280
  }
})

const { fullscreenFlag, canvasViewDataInfo } = storeToRefs(dvMainStore)

const { width, node } = useMoveLine('DASHBOARD')
const { t } = useI18n()

const props = defineProps({
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  },
  noClose: {
    required: false,
    type: Boolean,
    default: false
  },
  resourceTable: {
    required: false,
    type: String,
    default: 'core'
  }
})

const { showPosition, resourceTable } = toRefs(props)

const resourceTreeRef = ref()

const hasTreeData = computed(() => {
  return resourceTreeRef.value?.hasData
})
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)

const rootManage = computed(() => {
  return resourceTreeRef.value?.rootManage
})
const mounted = computed(() => {
  return resourceTreeRef.value?.mounted
})

onMounted(() => {
  useEmitt({
    name: 'canvasDownload',
    callback: function () {
      downloadH2('img')
    }
  })
})

function createNew() {
  resourceTreeRef.value?.createNewObject()
}

const loadCanvasData = (dvId, weight?) => {
  // 复用不设置 dvMain 中的componentData 等画布信息
  const initMethod = showPosition.value === 'multiplexing' ? initCanvasDataPrepare : initCanvasData
  dataInitState.value = false
  initMethod(
    dvId,
    { busiFlag: 'dashboard', resourceTable: 'core' },
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
        onInitReady({ resourceId: dvId })
      })
    }
  )
}
// 地图类图表，需要预先准备图片
const mapChartTypes = ['bubble-map', 'flow-map', 'heat-map', 'map', 'symbolic-map']
const downloadH2 = type => {
  downloadStatus.value = true
  const mapElementIds =
    state.canvasDataPreview
      ?.filter(ele => mapChartTypes.includes(ele.innerType))
      .map(ele => ele.id) || []
  mapElementIds.forEach(id => useEmitt().emitter.emit('l7-prepare-picture', id))
  nextTick(() => {
    const vueDom = previewCanvasContainer.value.querySelector('.canvas-container')
    downloadCanvas2(type, vueDom, state.dvInfo.name, () => {
      downloadStatus.value = false
      mapElementIds.forEach(id => useEmitt().emitter.emit('l7-unprepare-picture', id))
    })
  })
}

const downloadAsAppTemplate = downloadType => {
  if (downloadType === 'template') {
    fileDownload(downloadType, null)
  } else if (downloadType === 'app') {
    downLoadToAppPre()
  }
}

const downLoadToAppPre = () => {
  const result = checkTemplate()
  if (result && result.length > 0) {
    ElMessage.warning(t('visualization.export_tips', [result]))
  } else {
    appExportFormRef.value.init({
      appName: state.dvInfo.name,
      icon: null,
      version: '2.0',
      creator: userName.value,
      required: '2.9.0',
      description: null
    })
  }
}

const checkTemplate = () => {
  let templateViewNames = ','
  Object.keys(canvasViewDataInfo.value).forEach(key => {
    const viewInfo = canvasViewDataInfo.value[key]
    if (viewInfo && viewInfo?.dataFrom === 'template') {
      templateViewNames = templateViewNames + viewInfo.title + ','
    }
  })
  return templateViewNames.slice(1)
}

const fileDownload = (downloadType, attachParams) => {
  downloadStatus.value = true
  nextTick(() => {
    const vueDom = previewCanvasContainer.value.querySelector('.canvas-container')
    download2AppTemplate(downloadType, vueDom, state.dvInfo.name, attachParams, () => {
      downloadStatus.value = false
    })
  })
}

const slideOpenChange = () => {
  slideShow.value = !slideShow.value
}

const getPreviewStateInfo = () => {
  return state
}

const reload = id => {
  loadCanvasData(id, state.dvInfo.weight)
}

const resourceNodeClick = data => {
  loadCanvasData(data.id, data.weight)
  if (showPosition.value === 'multiplexing') {
    dvMainStore.initCurMultiplexingComponents()
  }
}

const previewShowFlag = computed(() => !!dvMainStore.dvInfo?.name)

onBeforeMount(() => {
  if (showPosition.value === 'preview') {
    dvMainStore.canvasDataInit()
  }
})
const sideTreeStatus = ref(true)
const changeSideTreeStatus = val => {
  sideTreeStatus.value = val
}

const mouseenter = () => {
  appStore.setArrowSide(true)
}

const mouseleave = () => {
  appStore.setArrowSide(false)
}

const downLoadApp = appAttachInfo => {
  fileDownload('app', appAttachInfo)
}

const freezeStyle = computed(() => [
  { '--top-show-offset': state.showOffset.top },
  { '--left-show-offset': state.showOffset.left }
])

defineExpose({
  getPreviewStateInfo
})
</script>

<template>
  <div class="dv-preview dv-teleport-query" :style="freezeStyle">
    <ArrowSide
      v-if="!noClose"
      :style="{ left: (sideTreeStatus ? width - 12 : 0) + 'px' }"
      @change-side-tree-status="changeSideTreeStatus"
      :isInside="!sideTreeStatus"
    ></ArrowSide>
    <el-aside
      @mouseenter="mouseenter"
      @mouseleave="mouseleave"
      class="resource-area"
      :class="{ 'close-side': !slideShow, retract: !sideTreeStatus }"
      ref="node"
      :style="{ width: width + 'px' }"
    >
      <ArrowSide
        v-if="!noClose"
        :isInside="!sideTreeStatus"
        :style="{ left: (sideTreeStatus ? width - 12 : 0) + 'px' }"
        @change-side-tree-status="changeSideTreeStatus"
      ></ArrowSide>
      <de-resource-tree
        ref="resourceTreeRef"
        v-show="slideShow"
        :cur-canvas-type="'dashboard'"
        :show-position="showPosition"
        :resource-table="resourceTable"
        @node-click="resourceNodeClick"
      />
    </el-aside>
    <el-container
      class="preview-area"
      :class="{ 'no-data': !state.dvInfo?.id }"
      v-loading="!dataInitState"
    >
      <div
        @click="slideOpenChange"
        v-if="showPosition === 'preview' && false"
        class="flexible-button-area"
      >
        <el-icon v-if="slideShow"><ArrowLeft /></el-icon>
        <el-icon v-else><ArrowRight /></el-icon>
      </div>
      <!--从store中判断当前是否有点击仪表板 复用时也符合-->
      <template v-if="previewShowFlag">
        <preview-head
          v-if="showPosition === 'preview'"
          @reload="reload"
          @download="downloadH2"
          @downloadAsAppTemplate="downloadAsAppTemplate"
        />
        <div
          ref="previewCanvasContainer"
          class="content"
          id="de-preview-content"
          :class="{ 'de-screen-full': fullscreenFlag }"
        >
          <canvas-opt-bar
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
            :show-position="showPosition"
            :download-status="downloadStatus"
            :show-linkage-button="false"
          ></de-preview>
        </div>
      </template>
      <template v-else-if="hasTreeData && mounted">
        <empty-background :description="t('visualization.preview_select_tips')" img-type="select" />
      </template>
      <template v-else-if="mounted">
        <empty-background :description="t('visualization.have_none_resource')" img-type="none">
          <el-button v-if="rootManage && !isDataEaseBi" @click="createNew" type="primary">
            <template #icon>
              <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
            </template>
            {{ t('commons.create') }}{{ t('chart.dashboard') }}
          </el-button>
        </empty-background>
      </template>
    </el-container>
  </div>
  <app-export-form
    ref="appExportFormRef"
    :dv-info="state.dvInfo"
    :component-data="state.canvasDataPreview"
    :canvas-view-info="state.canvasViewInfoPreview"
    @downLoadApp="downLoadApp"
  ></app-export-form>
</template>

<style lang="less">
.dv-preview {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  background: #ffffff;
  position: relative;
  .resource-area {
    position: relative;
    height: 100%;
    width: 279px;
    padding: 0;
    border-right: 1px solid #d7d7d7;
    overflow: visible;

    &.retract {
      display: none;
    }
  }
  .preview-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow-x: hidden;
    overflow-y: auto;
    position: relative;
    //transition: 0.5s;

    &.no-data {
      background-color: rgba(245, 246, 247, 1);
    }

    .content {
      position: relative;
      display: flex;
      width: 100%;
      height: 100%;
      overflow-x: hidden;
      overflow-y: auto;
      align-items: center;
    }
  }
}

.close-side {
  width: 0px !important;
  padding: 0px !important;
}

.flexible-button-area {
  position: absolute;
  height: 60px;
  width: 16px;
  left: 0;
  top: calc(50% - 30px);
  background-color: #ffffff;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  border-top: 1px solid #d7d7d7;
  border-right: 1px solid #d7d7d7;
  border-bottom: 1px solid #d7d7d7;
}
</style>
