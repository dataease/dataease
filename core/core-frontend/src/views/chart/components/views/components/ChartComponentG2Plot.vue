<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef, toRaw, toRefs } from 'vue'
import { getData } from '@/api/chart'
import { ChartLibraryType } from '@/views/chart/components/js/panel/types'
import { G2PlotChartView } from '@/views/chart/components/js/panel/types/impl/g2plot'
import { L7PlotChartView } from '@/views/chart/components/js/panel/types/impl/l7plot'
import chartViewManager from '@/views/chart/components/js/panel'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ViewTrackBar from '@/components/visualization/ViewTrackBar.vue'
import { storeToRefs } from 'pinia'
import { parseJson } from '@/views/chart/components/js/util'
import { debounce } from 'lodash-es'
import ChartError from '@/views/chart/components/views/components/ChartError.vue'

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
  }
})

const emit = defineEmits(['onChartClick', 'onDrillFilters', 'onJumpClick'])

const { view, showPosition } = toRefs(props)

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
  myChart: null,
  loading: false,
  data: { fields: [] } // 图表数据
})
let chartData = shallowRef<Partial<Chart['data']>>({
  fields: []
})

const containerId = 'container-' + showPosition.value + '-' + view.value.id
const viewTrack = ref(null)

const calcData = (view, callback) => {
  if (view.tableId) {
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
          }
          dvMainStore.setViewDataDetails(view.id, chartData.value)
          renderChart(res)
        }
      })
      .finally(callback?.())
  } else {
    callback?.()
  }
}

const renderChart = async view => {
  if (!view) {
    return
  }
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  const chart = toRaw({ ...view, data: chartData.value })
  const chartView = chartViewManager.getChartView(view.render, view.type)
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

const renderG2Plot = (chart, chartView: G2PlotChartView<any, any>) => {
  state.myChart?.destroy()
  state.myChart = chartView.drawChart({
    chartObj: state.myChart,
    container: containerId,
    chart: chart,
    scale: 1,
    action
  })
  state.myChart?.render()
}

const dynamicAreaId = ref('')
const country = ref('')
const renderL7Plot = (chart, chartView: L7PlotChartView<any, any>) => {
  const map = parseJson(chart.customAttr).map
  let areaId = map.id
  country.value = areaId.slice(0, 3)
  if (dynamicAreaId.value) {
    areaId = dynamicAreaId.value
  }
  debounce(async () => {
    state.myChart?.destroy()
    state.myChart = await chartView.drawChart({
      chartObj: state.myChart,
      container: containerId,
      chart,
      areaId,
      action
    })
  }, 0)()
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
    state.trackBarStyle.left = param.x + 'px'
    state.trackBarStyle.top = param.y + 10 + 'px'
    viewTrack.value.trackButtonClick()
  }
}

const trackClick = trackAction => {
  const param = state.pointParam
  if (!param?.data?.dimensionList) {
    return
  }
  // 地图
  dynamicAreaId.value = param.data.adcode + ''
  if (!dynamicAreaId.value?.startsWith(country.value)) {
    dynamicAreaId.value = country.value + dynamicAreaId.value
  }
  const quotaList = state.pointParam.data.quotaList
  quotaList[0]['value'] = state.pointParam.data.value
  const linkageParam = {
    option: 'linkage',
    name: state.pointParam.data.name,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: quotaList
  }
  const jumpParam = {
    option: 'jump',
    name: state.pointParam.data.name,
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
      emit('onJumpClick', jumpParam)
      break
    default:
      break
  }
}

const trackMenu = computed(() => {
  const trackMenuInfo = []
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
  jumpCount && view.value?.jumpActive && trackMenuInfo.push('jump')
  linkageCount && view.value?.linkageActive && trackMenuInfo.push('linkage')
  view.value.drillFields.length && trackMenuInfo.push('drill')
  return trackMenuInfo
})

defineExpose({
  calcData,
  renderChart
})

onMounted(() => {
  // queryData(true)
  // renderChart({ render: ChartRenderType.ANT_V, type: 'bar' })
})
onBeforeUnmount(() => {
  state.myChart?.destroy()
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
    width: 100%;
    height: 100%;
    ::v-deep(.g2-tooltip) {
      position: fixed !important;
    }
  }
}
</style>
