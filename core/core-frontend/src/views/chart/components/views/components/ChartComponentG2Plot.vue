<script lang="ts" setup>
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  shallowRef,
  toRefs
} from 'vue'
import { getData } from '@/api/chart'
import { ChartLibraryType } from '@/views/chart/components/js/panel/types'
import { G2PlotChartView } from '@/views/chart/components/js/panel/types/impl/g2plot'
import { L7PlotChartView } from '@/views/chart/components/js/panel/types/impl/l7plot'
import chartViewManager from '@/views/chart/components/js/panel'
import { useAppStoreWithOut } from '@/store/modules/app'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ViewTrackBar from '@/components/visualization/ViewTrackBar.vue'
import { storeToRefs } from 'pinia'
import { parseJson } from '@/views/chart/components/js/util'
import { defaultsDeep, cloneDeep } from 'lodash-es'
import ChartError from '@/views/chart/components/views/components/ChartError.vue'
import { BASE_VIEW_CONFIG } from '../../editor/util/chart'
import { customAttrTrans, customStyleTrans, recursionTransObj } from '@/utils/canvasStyle'
import { deepCopy } from '@/utils/utils'
import { trackBarStyleCheck } from '@/utils/canvasUtils'
import { useEmitt } from '@/hooks/web/useEmitt'
import { L7ChartView } from '@/views/chart/components/js/panel/types/impl/l7'

const dvMainStore = dvMainStoreWithOut()
const { nowPanelTrackInfo, nowPanelJumpInfo, mobileInPc, embeddedCallBack, inMobile } =
  storeToRefs(dvMainStore)
const { emitter } = useEmitt()
const props = defineProps({
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
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

const emit = defineEmits([
  'onPointClick',
  'onChartClick',
  'onDrillFilters',
  'onJumpClick',
  'resetLoading'
])

const g2TypeSeries1 = ['bidirectional-bar']
const g2TypeSeries0 = ['bar-range']

const { view, showPosition, scale, terminal } = toRefs(props)

const isError = ref(false)
const errMsg = ref('')
const linkageActiveHistory = ref(false)
const antVRenderStatus = ref(false)

const state = reactive({
  trackBarStyle: {
    position: 'absolute',
    left: '50px',
    top: '50px'
  },
  linkageActiveParam: null,
  pointParam: null,
  data: { fields: [] } // 图表数据
})
let chartData = shallowRef<Partial<Chart['data']>>({
  fields: []
})

const containerId = 'container-' + showPosition.value + '-' + view.value.id
const viewTrack = ref(null)

const clearLinkage = () => {
  linkageActiveHistory.value = false
  try {
    myChart?.setState('active', () => true, false)
    myChart?.setState('inactive', () => true, false)
    myChart?.setState('selected', () => true, false)
  } catch (e) {
    console.warn('clearLinkage error')
  }
}
const reDrawView = () => {
  linkageActiveHistory.value = false
  myChart?.render()
}
const linkageActivePre = () => {
  if (linkageActiveHistory.value) {
    reDrawView()
  }
  nextTick(() => {
    linkageActive()
  })
}
const linkageActive = () => {
  linkageActiveHistory.value = true
  myChart?.setState('active', param => {
    if (Array.isArray(param)) {
      return false
    } else {
      if (checkSelected(param)) {
        return true
      }
    }
  })
  myChart?.setState('inactive', param => {
    if (Array.isArray(param)) {
      return false
    } else {
      if (!checkSelected(param)) {
        return true
      }
    }
  })
}
const checkSelected = param => {
  if (g2TypeSeries1.includes(view.value.type)) {
    return state.linkageActiveParam.name === param.field
  } else if (g2TypeSeries0.includes(view.value.type)) {
    return state.linkageActiveParam.category === param.category
  } else {
    return (
      (state.linkageActiveParam.name === param.name ||
        (state.linkageActiveParam.name === 'NO_DATA' && !param.name)) &&
      state.linkageActiveParam.category === param.category
    )
  }
}

const calcData = async (view, callback) => {
  if (view.tableId || view['dataFrom'] === 'template') {
    isError.value = false
    const v = JSON.parse(JSON.stringify(view))
    getData(v)
      .then(async res => {
        if (res.code && res.code !== 0) {
          isError.value = true
          errMsg.value = res.msg
          callback?.()
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
          if (
            !res.drill &&
            !res.chartExtRequest?.filter?.length &&
            !res.chartExtRequest?.linkageFilters?.length
          ) {
            dvMainStore.setViewOriginData(view.id, chartData.value)
            emitter.emit('chart-data-change')
          }
          await renderChart(res, callback)
        }
      })
      .catch(() => {
        callback?.()
      })
  } else {
    if (['bubble-map', 'map', 'flow-map', 'heat-map'].includes(view.type)) {
      await renderChart(view, callback)
    }
    callback?.()
  }
}
let curView
const renderChart = async (view, callback?) => {
  if (!view) {
    return
  }
  curView = view
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  // 与默认图表对象合并，方便增加配置项
  const chart = deepCopy({
    ...defaultsDeep(view, cloneDeep(BASE_VIEW_CONFIG)),
    data: chartData.value
  })
  const chartView = chartViewManager.getChartView(view.render, view.type)
  recursionTransObj(customAttrTrans, chart.customAttr, scale.value, terminal.value)
  recursionTransObj(customStyleTrans, chart.customStyle, scale.value, terminal.value)
  switch (chartView.library) {
    case ChartLibraryType.L7_PLOT:
      await renderL7Plot(chart, chartView as L7PlotChartView<any, any>, callback)
      break
    case ChartLibraryType.L7:
      await renderL7(chart, chartView as L7ChartView<any, any>, callback)
      break
    case ChartLibraryType.G2_PLOT:
      renderG2Plot(chart, chartView as G2PlotChartView<any, any>)
      callback?.()
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
    action,
    quadrantDefaultBaseline
  })
  myChart?.render()
  if (linkageActiveHistory.value) {
    linkageActive()
  }
}

const dynamicAreaId = ref('')
const country = ref('')
const appStore = useAppStoreWithOut()
const chartContainer = ref<HTMLElement>(null)
let mapTimer: number
const renderL7Plot = async (chart: ChartObj, chartView: L7PlotChartView<any, any>, callback) => {
  const map = parseJson(chart.customAttr).map
  let areaId = map.id
  country.value = areaId.slice(0, 3)
  if (dynamicAreaId.value) {
    // 世界下钻到国家，切换路径
    if (country.value === '000' && dynamicAreaId.value.startsWith('000')) {
      country.value = dynamicAreaId.value.slice(3)
      areaId = country.value
    } else {
      areaId = dynamicAreaId.value
    }
  }
  mapTimer && clearTimeout(mapTimer)
  mapTimer = setTimeout(async () => {
    myChart?.destroy()
    if (chartContainer.value) {
      chartContainer.value.textContent = ''
    }
    myChart = await chartView.drawChart({
      chartObj: myChart,
      container: containerId,
      chart,
      areaId,
      action
    })
    callback?.()
    emit('resetLoading')
  }, 500)
}

let mapL7Timer: number
const renderL7 = async (chart: ChartObj, chartView: L7ChartView<any, any>, callback) => {
  mapL7Timer && clearTimeout(mapL7Timer)
  mapL7Timer = setTimeout(async () => {
    myChart?.destroy()
    myChart = await chartView.drawChart({
      chartObj: myChart,
      container: containerId,
      chart: chart,
      action
    })
    myChart?.render()
    callback?.()
    emit('resetLoading')
  }, 500)
}

const pointClickTrans = () => {
  if (embeddedCallBack.value === 'yes') {
    trackClick('pointClick')
  }
}

const action = param => {
  if (param.from === 'map') {
    emitter.emit('map-default-range', param)
    return
  }
  state.pointParam = param.data
  // 点击
  pointClickTrans()
  // 下钻 联动 跳转
  state.linkageActiveParam = {
    category: state.pointParam.data.category ? state.pointParam.data.category : 'NO_DATA',
    name: state.pointParam.data.name ? state.pointParam.data.name : 'NO_DATA'
  }
  if (trackMenu.value.length < 2) {
    // 只有一个事件直接调用
    trackClick(trackMenu.value[0])
  } else {
    // 图表关联多个事件
    const barStyleTemp = {
      left: param.x - 50,
      top: param.y + 10
    }
    trackBarStyleCheck(props.element, barStyleTemp, props.scale, trackMenu.value.length)
    state.trackBarStyle.left = barStyleTemp.left + 'px'
    if (curView.type === 'symbolic-map') {
      state.trackBarStyle.top = param.y + 10 + 'px'
    } else {
      state.trackBarStyle.top = barStyleTemp.top + 'px'
    }
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
  // 跳转字段处理
  let jumpName = state.pointParam.data.name
  if (state.pointParam.data.dimensionList.length > 1) {
    const fieldIds = []
    // 优先下钻字段
    if (curView.drill) {
      const curFiled = curView.drillFields[curView.drillFilters.length]
      fieldIds.push(curFiled.id)
    }
    if (curView.type.includes('chart-mix')) {
      chartData.value?.left?.fields?.forEach(field => {
        if (!fieldIds.includes(field.id)) {
          fieldIds.push(field.id)
        }
      })
      chartData.value?.right?.fields?.forEach(field => {
        if (!fieldIds.includes(field.id)) {
          fieldIds.push(field.id)
        }
      })
    } else {
      chartData.value?.fields?.forEach(field => {
        if (!fieldIds.includes(field.id)) {
          fieldIds.push(field.id)
        }
      })
    }
    for (let i = 0; i < fieldIds.length; i++) {
      const id = fieldIds[i]
      const sourceInfo = view.value.id + '#' + id
      if (nowPanelJumpInfo.value[sourceInfo]) {
        jumpName = id
        break
      }
    }
  }
  let quotaList = state.pointParam.data.quotaList
  if (curView.type === 'bar-range') {
    quotaList = state.pointParam.data.dimensionList
  } else {
    quotaList[0]['value'] = state.pointParam.data.value
  }
  const linkageParam = {
    option: 'linkage',
    name: checkName,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: quotaList
  }
  const jumpParam = {
    option: 'jump',
    name: jumpName,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: quotaList
  }

  const clickParams = {
    option: 'pointClick',
    name: checkName,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: quotaList
  }

  switch (trackAction) {
    case 'pointClick':
      emit('onPointClick', clickParams)
      break
    case 'linkageAndDrill':
      dvMainStore.addViewTrackFilter(linkageParam)
      emit('onChartClick', param)
      break
    case 'drill':
      emit('onChartClick', param)
      break
    case 'linkage':
      linkageActivePre()
      dvMainStore.addViewTrackFilter(linkageParam)
      break
    case 'jump':
      if (mobileInPc.value && !inMobile.value) return
      emit('onJumpClick', jumpParam)
      break
    default:
      break
  }
}

const trackMenu = computed(() => {
  let trackMenuInfo = []
  // 复用、放大状态的仪表板不进行联动、跳转和下钻的动作
  if (!['multiplexing', 'viewDialog'].includes(showPosition.value)) {
    let linkageCount = 0
    let jumpCount = 0
    if (curView?.type?.includes('chart-mix')) {
      chartData.value?.left?.fields?.forEach(item => {
        const sourceInfo = view.value.id + '#' + item.id
        if (nowPanelTrackInfo.value[sourceInfo]) {
          linkageCount++
        }
        if (nowPanelJumpInfo.value[sourceInfo]) {
          jumpCount++
        }
      })
      chartData.value?.right?.fields?.forEach(item => {
        const sourceInfo = view.value.id + '#' + item.id
        if (nowPanelTrackInfo.value[sourceInfo]) {
          linkageCount++
        }
        if (nowPanelJumpInfo.value[sourceInfo]) {
          jumpCount++
        }
      })
    } else {
      chartData.value?.fields?.forEach(item => {
        const sourceInfo = view.value.id + '#' + item.id
        if (nowPanelTrackInfo.value[sourceInfo]) {
          linkageCount++
        }
        if (nowPanelJumpInfo.value[sourceInfo]) {
          jumpCount++
        }
      })
    }
    jumpCount &&
      view.value?.jumpActive &&
      (!mobileInPc.value || inMobile.value) &&
      trackMenuInfo.push('jump')
    linkageCount && view.value?.linkageActive && trackMenuInfo.push('linkage')
    view.value.drillFields.length && trackMenuInfo.push('drill')
    // 如果同时配置jump linkage drill 切配置联动时同时下钻 在实际只显示两个 '跳转' '联动和下钻'
    if (trackMenuInfo.length === 3 && props.element.actionSelection.linkageActive === 'auto') {
      trackMenuInfo = ['jump', 'linkageAndDrill']
    } else if (
      trackMenuInfo.length === 2 &&
      props.element.actionSelection.linkageActive === 'auto' &&
      !trackMenuInfo.includes('jump')
    ) {
      trackMenuInfo = ['linkageAndDrill']
    }
  }
  return trackMenuInfo
})
const quadrantDefaultBaseline = defaultQuadrant => {
  emitter.emit('quadrant-default-baseline', defaultQuadrant)
}
defineExpose({
  calcData,
  renderChart,
  trackMenu,
  clearLinkage
})
let resizeObserver
const TOLERANCE = 0.01
const RESIZE_MONITOR_CHARTS = ['map', 'bubble-map', 'flow-map', 'heat-map']
onMounted(() => {
  const containerDom = document.getElementById(containerId)
  const { offsetWidth, offsetHeight } = containerDom
  const preSize = [offsetWidth, offsetHeight]
  resizeObserver = new ResizeObserver(([entry] = []) => {
    if (!RESIZE_MONITOR_CHARTS.includes(view.value.type)) {
      return
    }
    const [size] = entry.borderBoxSize || []
    const widthOffsetPercent = (size.inlineSize - preSize[0]) / preSize[0]
    const heightOffsetPercent = (size.blockSize - preSize[1]) / preSize[1]
    if (Math.abs(widthOffsetPercent) < TOLERANCE && Math.abs(heightOffsetPercent) < TOLERANCE) {
      return
    }
    if (myChart && preSize[1] > 1) {
      renderChart(curView)
    }
    preSize[0] = size.inlineSize
    preSize[1] = size.blockSize
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
    <div v-if="!isError" ref="chartContainer" class="canvas-content" :id="containerId"></div>
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
