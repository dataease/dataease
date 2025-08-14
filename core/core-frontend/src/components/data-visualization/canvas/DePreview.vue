<script setup lang="ts">
import { getCanvasStyle, getShapeItemStyle } from '@/utils/style'
import ComponentWrapper from './ComponentWrapper.vue'
import { changeStyleWithScale } from '@/utils/translate'
import { computed, nextTick, ref, toRefs, watch, onBeforeUnmount, onMounted, reactive } from 'vue'
import { changeRefComponentsSizeWithScalePoint } from '@/utils/changeComponentsSizeWithScale'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import elementResizeDetectorMaker from 'element-resize-detector'
import UserViewEnlarge from '@/components/visualization/UserViewEnlarge.vue'
import CanvasOptBar from '@/components/visualization/CanvasOptBar.vue'
import { isDashboard, isMainCanvas, refreshOtherComponent } from '@/utils/canvasUtils'
import { activeWatermarkCheckUser } from '@/components/watermark/watermark'
import router from '@/router'
import { XpackComponent } from '@/components/plugin'
import PopArea from '@/custom-component/pop-area/Component.vue'
import CanvasFilterBtn from '@/custom-component/canvas-filter-btn/Component.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import DatasetParamsComponent from '@/components/visualization/DatasetParamsComponent.vue'
import DeFullscreen from '@/components/visualization/common/DeFullscreen.vue'
import EmptyBackground from '../../empty-background/src/EmptyBackground.vue'
import LinkOptBar from '@/components/data-visualization/canvas/LinkOptBar.vue'
import { isDesktop } from '@/utils/ModelUtil'
import { isMobile } from '@/utils/utils'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const { pcMatrixCount, curComponent, mobileInPc, canvasState, inMobile } = storeToRefs(dvMainStore)
const openHandler = ref(null)
const customDatasetParamsRef = ref(null)
const emits = defineEmits(['onResetLayout'])
const fullScreeRef = ref(null)
const isOverSize = ref(false)
const isDesktopFlag = isDesktop()
const { t } = useI18n()
const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  },
  componentData: {
    type: Object,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  dvInfo: {
    type: Object,
    required: true
  },
  canvasId: {
    type: String,
    required: false,
    default: 'canvas-main'
  },
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  },
  previewActive: {
    type: Boolean,
    default: true
  },
  downloadStatus: {
    type: Boolean,
    default: false
  },
  userId: {
    type: String,
    require: false
  },
  outerScale: {
    type: Number,
    required: false,
    default: 1
  },
  outerSearchCount: {
    type: Number,
    required: false,
    default: 0
  },
  isSelector: {
    type: Boolean,
    default: false
  },
  // 显示悬浮按钮
  showPopBar: {
    type: Boolean,
    default: false
  },
  // 字体
  fontFamily: {
    type: String,
    required: false,
    default: 'inherit'
  },
  // 联动按钮位置
  showLinkageButton: {
    type: Boolean,
    default: true
  },
  outerScreenAdaptor: {
    type: String,
    required: false,
    default: null
  }
})

const {
  canvasStyleData,
  componentData,
  dvInfo,
  canvasId,
  canvasViewInfo,
  showPosition,
  previewActive,
  downloadStatus,
  outerScale,
  outerSearchCount,
  showPopBar,
  fontFamily,
  outerScreenAdaptor
} = toRefs(props)
const domId = 'preview-' + canvasId.value
const scaleWidthPoint = ref(100)
const scaleHeightPoint = ref(100)
const scaleMin = ref(100)
const previewCanvas = ref(null)
const cellWidth = ref(10)
const cellHeight = ref(10)
const userViewEnlargeRef = ref(null)
const searchCount = ref(0)
const refreshTimer = ref(null)
const renderReady = ref(false)
const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})
const state = reactive({
  initState: true,
  scrollMain: 0
})

const screenAdaptor = computed(() => {
  return outerScreenAdaptor.value || canvasStyleData.value?.screenAdaptor
})

const curSearchCount = computed(() => {
  return outerSearchCount.value + searchCount.value
})
// 大屏是否保持宽高比例 非全屏 full 都需要保持宽高比例
const dataVKeepRadio = computed(() => {
  return screenAdaptor.value !== 'full'
})

// 仪表板是否跟随宽度缩放 非全屏 full 都需要保持宽高比例
const dashboardScaleWithWidth = computed(() => {
  return isDashboard() && screenAdaptor.value === 'withWidth'
})
const isReport = computed(() => {
  return !!router.currentRoute.value.query?.report
})

const popComponentData = computed(() =>
  componentData.value.filter(
    ele =>
      ele.category &&
      ele.category === 'hidden' &&
      (!ele?.dashboardHidden || (ele?.dashboardHidden && isMobile()))
  )
)

const baseComponentData = computed(() =>
  componentData.value.filter(
    ele =>
      ele?.category !== 'hidden' &&
      ele.component !== 'GroupArea' &&
      (!ele?.dashboardHidden || (ele?.dashboardHidden && isMobile()))
  )
)
const canvasStyle = computed(() => {
  let style = {}
  if (isMainCanvas(canvasId.value) && !isDashboard()) {
    style['overflowY'] = 'hidden !important'
  }
  if (canvasStyleData.value && canvasStyleData.value.width && isMainCanvas(canvasId.value)) {
    style = getCanvasStyle(canvasStyleData.value)
    if (screenAdaptor.value === 'keep') {
      style['height'] = canvasStyleData.value?.height + 'px'
      style['width'] = canvasStyleData.value?.width + 'px'
      style['margin'] = 'auto'
    } else if (screenAdaptor.value === 'keepProportion') {
      style['aspect-ratio'] = canvasStyleData.value?.width / canvasStyleData.value?.height
      style['height'] = 'auto'
      style['width'] = 'auto'
    } else {
      style['height'] = dashboardActive.value
        ? downloadStatus.value
          ? getDownloadStatusMainHeight()
          : '100%'
        : !screenAdaptor.value || screenAdaptor.value === 'widthFirst'
        ? changeStyleWithScale(canvasStyleData.value?.height, scaleMin.value) + 'px'
        : '100%'
      style['width'] =
        !dashboardActive.value && screenAdaptor.value === 'heightFirst'
          ? changeStyleWithScale(canvasStyleData.value?.width, scaleHeightPoint.value) + 'px'
          : '100%'
    }
  }
  return style
})

const getDownloadStatusMainHeightV2 = () => {
  if (!previewCanvas.value?.childNodes) {
    nextTick(() => {
      canvasStyle.value.height = getDownloadStatusMainHeight()
    })
    return '100%'
  }
  const children = previewCanvas.value.childNodes
  let maxBottomPosition = 0

  children.forEach(child => {
    // 获取style中的top值
    const styleTop = child.style?.top || 0
    // 获取style中的height
    const styleHeight = child.style?.height || 0

    // 转换为数字
    const top = parseFloat(styleTop) || 0
    const height = parseFloat(styleHeight) || 0

    // 计算底部位置
    const bottomPosition = top + height

    if (bottomPosition > maxBottomPosition) {
      maxBottomPosition = bottomPosition
    }
  })
  return `${maxBottomPosition}px`
}

const getDownloadStatusMainHeight = () => {
  if (!previewCanvas.value?.childNodes) {
    nextTick(() => {
      canvasStyle.value.height = getDownloadStatusMainHeight()
    })
    return '100%'
  }
  const children = previewCanvas.value.childNodes
  let maxHeight = 0

  children.forEach(child => {
    const height = (child.offsetHeight || 0) + (child.offsetTop || 0)
    if (height > maxHeight) {
      maxHeight = height
    }
  })
  return `${maxHeight}px!important`
}

watch(
  () => previewActive.value,
  () => {
    if (previewActive.value) {
      restore()
    }
  }
)

useEmitt({
  name: 'tabCanvasChange-' + canvasId.value,
  callback: function () {
    restore()
  }
})

useEmitt({
  name: 'componentRefresh',
  callback: function () {
    if (isMainCanvas(canvasId.value)) {
      refreshDataV()
    }
  }
})

useEmitt({
  name: 'canvasFullscreen',
  callback: function () {
    if (isMainCanvas(canvasId.value)) {
      fullScreeRef.value.toggleFullscreen()
    }
  }
})

const resetLayout = () => {
  if (downloadStatus.value) {
    return
  }
  nextTick(() => {
    if (previewCanvas.value) {
      //div容器获取tableBox.value.clientWidth
      let canvasWidth = previewCanvas.value.clientWidth
      let canvasHeight = previewCanvas.value.clientHeight
      scaleWidthPoint.value = (canvasWidth * 100) / canvasStyleData.value.width
      if (dashboardScaleWithWidth.value) {
        scaleHeightPoint.value = scaleWidthPoint.value * 0.7
      } else {
        scaleHeightPoint.value = (canvasHeight * 100) / canvasStyleData.value.height
      }
      scaleMin.value =
        isDashboard() && !dashboardScaleWithWidth.value
          ? Math.floor(Math.min(scaleWidthPoint.value, scaleHeightPoint.value))
          : scaleWidthPoint.value
      if (dashboardActive.value) {
        cellWidth.value = canvasWidth / pcMatrixCount.value.x
        // 如果是保持比例 则宽高相同
        if (dashboardScaleWithWidth.value) {
          cellHeight.value = cellWidth.value * 1.6
        } else {
          cellHeight.value = canvasHeight / pcMatrixCount.value.y
        }
        scaleMin.value = isMainCanvas(canvasId.value)
          ? scaleMin.value * 1.2
          : outerScale.value * 100
      } else {
        // 需要保持宽高比例时 高度伸缩和宽度伸缩保持一致 否则 高度伸缩单独计算
        // tip 当当前画布是tab时 使用的事 outerScale.value 因为 canvasStyleData.value为 {} 此处取数逻辑需进一步优化
        const scaleMinHeight = dataVKeepRadio.value ? scaleMin.value : scaleHeightPoint.value
        changeRefComponentsSizeWithScalePoint(
          baseComponentData.value,
          canvasStyleData.value,
          scaleMin.value || outerScale.value * 100,
          scaleMinHeight || outerScale.value * 100,
          outerScale.value * 100
        )
        scaleMin.value = isMainCanvas(canvasId.value) ? scaleMin.value : outerScale.value * 100
      }
      renderReady.value = true
      emits('onResetLayout')
      isOverSize.value = false
      if (previewCanvas.value?.clientHeight - previewCanvas.value?.parentNode?.clientHeight > 0) {
        isOverSize.value = true
      }
    }
  })
}
const restore = () => {
  if (isReport.value) {
    return
  }
  resetLayout()
}

const getShapeItemShowStyle = item => {
  return getShapeItemStyle(item, {
    dvModel: dvInfo.value.type,
    cellWidth: cellWidth.value,
    cellHeight: cellHeight.value,
    curGap: curGap.value
  })
}

const curGap = computed(() => {
  return dashboardActive.value && dvMainStore.canvasStyleData.dashboard?.gap === 'yes'
    ? dvMainStore.canvasStyleData?.dashboard?.gapSize
    : 0
})

const initRefreshTimer = () => {
  // 数据刷新计时器 (仪表开启刷新并且是预览状态才启动刷新)
  if (canvasStyleData.value.refreshViewEnable && showPosition.value === 'preview') {
    searchCount.value = 0
    refreshTimer.value && clearInterval(refreshTimer.value)
    let refreshTime = 300000
    if (canvasStyleData.value.refreshTime && canvasStyleData.value.refreshTime > 0) {
      if (canvasStyleData.value.refreshUnit === 'second') {
        refreshTime = canvasStyleData.value.refreshTime * 1000
      } else {
        refreshTime = canvasStyleData.value.refreshTime * 60000
      }
    }
    refreshTimer.value = setInterval(() => {
      refreshDataV()
    }, refreshTime)
  }
}

const refreshDataV = () => {
  searchCount.value++
  if (isMainCanvas(canvasId.value)) {
    refreshOtherComponent(dvInfo.value.id, dvInfo.value.type)
  }
}

const initWatermark = (waterDomId = 'preview-canvas-main') => {
  if (dvInfo.value.watermarkInfo && isMainCanvas(canvasId.value) && !downloadStatus.value) {
    activeWatermarkCheckUser(waterDomId, canvasId.value, scaleMin.value / 100)
  }
}

// 目标校验： 需要校验targetSourceId 是否是当前可视化资源ID
const winMsgHandle = event => {
  const msgInfo = event.data
  console.info('Received Message: ' + JSON.stringify(msgInfo))
  if (msgInfo?.targetSourceId === dvInfo.value.id + '' && isMainCanvas(canvasId.value))
    if (msgInfo.type === 'attachParams') {
      winMsgOuterParamsHandle(msgInfo)
    } else if (msgInfo.type === 'webParams') {
      // 网络消息处理
      winMsgWebParamsHandle(msgInfo)
    }
}

const winMsgWebParamsHandle = msgInfo => {
  const params = msgInfo.params
  dvMainStore.addWebParamsFilter(params, baseComponentData.value)
}

const winMsgOuterParamsHandle = msgInfo => {
  const attachParams = msgInfo.params
  state.initState = false
  dvMainStore.addOuterParamsFilter(attachParams, baseComponentData.value, 'outer')
  state.initState = true
}

onMounted(() => {
  initRefreshTimer()
  resetLayout()
  window.addEventListener('resize', restore)
  const erd = elementResizeDetectorMaker()
  erd.listenTo(document.getElementById(domId), () => {
    restore()
    initWatermark()
  })
  window.addEventListener('message', winMsgHandle)
})

onBeforeUnmount(() => {
  //初始化隐藏弹框区
  dvMainStore.canvasStateChange({ key: 'curPointArea', value: 'base' })
  clearInterval(refreshTimer.value)
  window.removeEventListener('message', winMsgHandle)
})

const userViewEnlargeOpen = (opt, item) => {
  userViewEnlargeRef.value.dialogInit(
    canvasStyleData.value,
    canvasViewInfo.value[item.id],
    item,
    opt,
    { scale: scaleMin.value / 100 }
  )
}
const handleMouseDown = () => {
  if (showPosition.value !== 'viewDialog') {
    dvMainStore.setCurComponent({ component: null, index: null })
    if (!curComponent.value || (curComponent.value && curComponent.value.category !== 'hidden')) {
      dvMainStore.canvasStateChange({ key: 'curPointArea', value: 'base' })
    }
  }
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

// v-if 使用 内容不渲染 默认参数不起用
const popAreaAvailable = computed(
  () => canvasStyleData.value?.popupAvailable && isMainCanvas(canvasId.value)
)

const filterBtnShow = computed(
  () =>
    popAreaAvailable.value &&
    popComponentData.value &&
    popComponentData.value.length > 0 &&
    !inMobile.value &&
    canvasStyleData.value.popupButtonAvailable
)
const datasetParamsInit = item => {
  customDatasetParamsRef.value?.optInit(item)
}
const dataVPreview = computed(
  () => dvInfo.value.type === 'dataV' && canvasId.value === 'canvas-main'
)

const linkOptBarShow = computed(() => {
  return Boolean(
    canvasStyleData.value.suspensionButtonAvailable &&
      !inMobile.value &&
      !mobileInPc.value &&
      showPopBar.value &&
      !isDesktopFlag
  )
})

const downloadAsPDF = () => {
  // test
}

const scrollPreview = () => {
  state.scrollMain = previewCanvas.value.scrollTop
}

const showUnpublishFlag = computed(() => dvInfo.value?.status === 0 && isMainCanvas(canvasId.value))
const getPreviewCanvasSize = () => {
  return {
    innerWidth: previewCanvas.value.clientWidth,
    innerHeight: previewCanvas.value.clientHeight
  }
}
defineExpose({
  restore,
  getPreviewCanvasSize,
  getDownloadStatusMainHeightV2
})
</script>

<template>
  <div
    :id="domId"
    class="canvas-container"
    :style="canvasStyle"
    :class="{
      'de-download-custom': downloadStatus,
      'datav-preview': dataVPreview,
      'datav-preview-unpublish': showUnpublishFlag
    }"
    ref="previewCanvas"
    @mousedown="handleMouseDown"
    @scroll="scrollPreview"
    v-if="state.initState"
  >
    <!--弹框触发区域-->
    <canvas-filter-btn :is-fixed="isOverSize" v-if="filterBtnShow"></canvas-filter-btn>
    <!-- 弹框区域 -->
    <PopArea
      v-if="popAreaAvailable"
      :dv-info="dvInfo"
      :canvas-id="canvasId"
      :canvas-style-data="canvasStyleData"
      :canvasViewInfo="canvasViewInfo"
      :pop-component-data="popComponentData"
      :scale="scaleMin"
      :canvas-state="canvasState"
      :show-position="'preview'"
    ></PopArea>
    <canvas-opt-bar
      v-if="showLinkageButton"
      :canvas-id="canvasId"
      :canvas-style-data="canvasStyleData"
      :component-data="baseComponentData"
      :is-fixed="isOverSize"
    ></canvas-opt-bar>
    <template v-if="renderReady && !showUnpublishFlag">
      <component-wrapper
        v-for="(item, index) in baseComponentData"
        v-show="item.isShow"
        :active="item.id === (curComponent || {})['id']"
        :canvas-id="canvasId"
        :canvas-style-data="canvasStyleData"
        :dv-info="dvInfo"
        :cur-style="getShapeItemShowStyle(item)"
        :canvas-view-info="canvasViewInfo"
        :view-info="canvasViewInfo[item.id]"
        :key="index"
        :config="item"
        :style="getShapeItemShowStyle(item)"
        :show-position="showPosition"
        :search-count="curSearchCount"
        :scale="mobileInPc && isDashboard() ? 100 : scaleMin"
        :is-selector="props.isSelector"
        :font-family="canvasStyleData.fontFamily || fontFamily"
        :scroll-main="state.scrollMain"
        @userViewEnlargeOpen="userViewEnlargeOpen($event, item)"
        @datasetParamsInit="datasetParamsInit(item)"
        @onPointClick="onPointClick"
        :index="index"
      />
    </template>
    <empty-background
      v-if="showUnpublishFlag"
      :description="t('visualization.resource_not_published')"
      img-type="none"
    >
    </empty-background>
    <user-view-enlarge ref="userViewEnlargeRef"></user-view-enlarge>
  </div>
  <empty-background v-if="!state.initState" description="参数不能为空" img-type="noneWhite" />
  <de-fullscreen ref="fullScreeRef"></de-fullscreen>
  <dataset-params-component ref="customDatasetParamsRef"></dataset-params-component>
  <XpackComponent ref="openHandler" jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI=" />
  <link-opt-bar
    v-if="linkOptBarShow"
    ref="link-opt-bar"
    :terminal="'pc'"
    :canvas-style-data="canvasStyleData"
    @link-export-pdf="downloadAsPDF"
  />
</template>

<style lang="less" scoped>
.canvas-container {
  background-size: 100% 100% !important;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  position: relative;
  div::-webkit-scrollbar {
    width: 0px !important;
    height: 0px !important;
  }
  div {
    -ms-overflow-style: none; /* IE and Edge */
    scrollbar-width: none; /* Firefox */
  }
}

.fix-button {
  position: fixed !important;
}

.datav-preview {
  overflow-y: hidden !important;
}

.datav-preview-unpublish {
  background-color: inherit !important;
}
</style>
