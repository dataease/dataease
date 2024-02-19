<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef, toRefs } from 'vue'
import { getData } from '@/api/chart'
import { ChartLibraryType } from '@/views/chart/components/js/panel/types'
import { G2PlotChartView } from '@/views/chart/components/js/panel/types/impl/g2plot'
import { L7PlotChartView } from '@/views/chart/components/js/panel/types/impl/l7plot'
import chartViewManager from '@/views/chart/components/js/panel'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ViewTrackBar from '@/components/visualization/ViewTrackBar.vue'
import { storeToRefs } from 'pinia'
import { parseJson } from '@/views/chart/components/js/util'
import { defaultsDeep, cloneDeep } from 'lodash-es'
import ChartError from '@/views/chart/components/views/components/ChartError.vue'
import { BASE_VIEW_CONFIG } from '../../editor/util/chart'
import { customAttrTrans, customStyleTrans, recursionTransObj } from '@/utils/canvasStyle'
import { deepCopy } from '@/utils/utils'

const dvMainStore = dvMainStoreWithOut()
const { nowPanelTrackInfo, nowPanelJumpInfo } = storeToRefs(dvMainStore)

const props = defineProps({
  view: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  showPosition: {
    type: String,
    required: false,
    default: 'canvas'
  },
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  terminal: {
    type: String,
    default: 'pc'
  }
})

const emit = defineEmits(['onChartClick', 'onDrillFilters', 'onJumpClick'])

const { view, showPosition, scale, terminal } = toRefs(props)

const isError = ref(false)
const errMsg = ref('')

const state = reactive({
  trackBarStyle: {
    position: 'absolute',
    left: '50px',
    top: '50px'
  },
  linkageActiveParam: null,
  pointParam: null,
  loading: false,
  data: { fields: [] } // 图表数据
})
let chartData = shallowRef<Partial<Chart['data']>>({
  fields: []
})

const containerId = 'container-' + showPosition.value + '-' + view.value.id
const viewTrack = ref(null)

const calcData = (view, callback) => {
  if (view.tableId || view['dataFrom'] === 'template') {
    state.loading = true
    isError.value = false
    const v = JSON.parse(JSON.stringify(view))
    getData(v)
      .then(res => {
        if (res.code && res.code !== 0) {
          isError.value = true
          errMsg.value = res.msg
        } else {
          chartData.value = res?.data as Partial<Chart['data']>
          emit('onDrillFilters', res?.drillFilters)
          if (!res?.drillFilters?.length) {
            dynamicAreaId.value = ''
          } else {
            dynamicAreaId.value =
              view.chartExtRequest?.drill?.[res?.drillFilters?.length - 1].extra?.adcode + ''
            // 地图
            if (!dynamicAreaId.value?.startsWith(country.value)) {
              dynamicAreaId.value = country.value + dynamicAreaId.value
            }
          }
          dvMainStore.setViewDataDetails(view.id, chartData.value)
          renderChart(res)
        }
        callback?.()
      })
      .catch(() => {
        callback?.()
      })
  } else {
    if (view.type === 'map') {
      renderChart(view)
    }
    callback?.()
  }
}
let curView
const renderChart = async view => {
  if (!view) {
    return
  }
  curView = view
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  // 与默认视图对象合并，方便增加配置项
  const chart = deepCopy({
    ...defaultsDeep(view, cloneDeep(BASE_VIEW_CONFIG)),
    data: chartData.value
  })
  const chartView = chartViewManager.getChartView(view.render, view.type)
  recursionTransObj(customAttrTrans, chart.customAttr, scale.value, terminal.value)
  recursionTransObj(customStyleTrans, chart.customStyle, scale.value, terminal.value)
  switch (chartView.library) {
    case ChartLibraryType.L7_PLOT:
      renderL7Plot(chart, chartView as L7PlotChartView<any, any>)
      break
    case ChartLibraryType.G2_PLOT:
      renderG2Plot(chart, chartView as G2PlotChartView<any, any>)
      break
    default:
      break
  }
}
let myChart = null
const renderG2Plot = (chart, chartView: G2PlotChartView<any, any>) => {
  myChart?.destroy()
  myChart = chartView.drawChart({
    chartObj: myChart,
    container: containerId,
    chart: chart,
    scale: 1,
    action
  })
  myChart?.render()
}

const dynamicAreaId = ref('')
const country = ref('')
const isDataEaseBi = ref(false)
let mapTimer
const renderL7Plot = (chart, chartView: L7PlotChartView<any, any>) => {
  const map = parseJson(chart.customAttr).map
  let areaId = map.id
  country.value = areaId.slice(0, 3)
  if (dynamicAreaId.value) {
    areaId = dynamicAreaId.value
  }
  mapTimer && clearTimeout(mapTimer)
  mapTimer = setTimeout(async () => {
    myChart?.destroy()
    myChart = await chartView.drawChart({
      chartObj: myChart,
      container: containerId,
      chart,
      areaId,
      action
    })
  }, 500)
}

const action = param => {
  // 下钻 联动 跳转
  state.pointParam = param.data
  state.linkageActiveParam = {
    category: state.pointParam.data.category ? state.pointParam.data.category : 'NO_DATA',
    name: state.pointParam.data.name ? state.pointParam.data.name : 'NO_DATA'
  }
  if (trackMenu.value.length < 2) {
    // 只有一个事件直接调用
    trackClick(trackMenu.value[0])
  } else {
    // 视图关联多个事件
    state.trackBarStyle.left = param.x - 50 + 'px'
    state.trackBarStyle.top = param.y + 10 + 'px'
    viewTrack.value.trackButtonClick()
  }
}

const trackClick = trackAction => {
  const param = state.pointParam
  if (!param?.data?.dimensionList) {
    return
  }
  let checkName = state.pointParam.data.name
  // 对多维度的处理 取第一个
  if (state.pointParam.data.dimensionList.length > 1) {
    checkName = state.pointParam.data.dimensionList[0].id
  }
  const quotaList = state.pointParam.data.quotaList
  quotaList[0]['value'] = state.pointParam.data.value
  const linkageParam = {
    option: 'linkage',
    name: checkName,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: quotaList
  }
  const jumpParam = {
    option: 'jump',
    name: checkName,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: quotaList
  }

  switch (trackAction) {
    case 'drill':
      emit('onChartClick', param)
      break
    case 'linkage':
      dvMainStore.addViewTrackFilter(linkageParam)
      break
    case 'jump':
      if (isDataEaseBi.value) return
      emit('onJumpClick', jumpParam)
      break
    default:
      break
  }
}

const trackMenu = computed(() => {
  const trackMenuInfo = []
  // 复用、放大状态的仪表板不进行联动、跳转和下钻的动作
  if (!['multiplexing', 'viewDialog'].includes(showPosition.value)) {
    let linkageCount = 0
    let jumpCount = 0
    chartData.value?.fields?.forEach(item => {
      const sourceInfo = view.value.id + '#' + item.id
      if (nowPanelTrackInfo.value[sourceInfo]) {
        linkageCount++
      }
      if (nowPanelJumpInfo.value[sourceInfo]) {
        jumpCount++
      }
    })
    jumpCount && view.value?.jumpActive && !isDataEaseBi.value && trackMenuInfo.push('jump')
    linkageCount && view.value?.linkageActive && trackMenuInfo.push('linkage')
    view.value.drillFields.length && trackMenuInfo.push('drill')
  }
  return trackMenuInfo
})

defineExpose({
  calcData,
  renderChart,
  trackMenu
})
let resizeObserver
const TOLERANCE = 0.01
onMounted(() => {
  isDataEaseBi.value = !!window.DataEaseBi
  const containerDom = document.getElementById(containerId)
  const { offsetWidth, offsetHeight } = containerDom
  const preSize = [offsetWidth, offsetHeight]
  resizeObserver = new ResizeObserver(([entry] = []) => {
    if (view.value.type !== 'map') {
      return
    }
    const [size] = entry.borderBoxSize || []
    const widthOffsetPercent = (size.inlineSize - preSize[0]) / preSize[0]
    const heightOffsetPercent = (size.blockSize - preSize[1]) / preSize[1]
    if (Math.abs(widthOffsetPercent) < TOLERANCE && Math.abs(heightOffsetPercent) < TOLERANCE) {
      return
    }
    preSize[0] = size.inlineSize
    preSize[1] = size.blockSize
    renderChart(curView)
  })
  resizeObserver.observe(containerDom)
})
onBeforeUnmount(() => {
  myChart?.destroy()
  resizeObserver?.disconnect()
})
</script>

<template>
  <div class="canvas-area">
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      class="track-bar"
      :style="state.trackBarStyle"
      @trackClick="trackClick"
    />
    <div v-if="!isError" class="canvas-content" :id="containerId"></div>
    <chart-error v-else :err-msg="errMsg" />
  </div>
</template>

<style lang="less" scoped>
.canvas-area {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 0;
  .canvas-content {
    width: 100% !important;
    height: 100% !important;
    :deep(.g2-tooltip) {
      position: fixed !important;
    }
  }
}
</style>
