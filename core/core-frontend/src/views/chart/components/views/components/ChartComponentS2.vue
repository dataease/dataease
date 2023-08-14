<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, toRaw, toRefs } from 'vue'
import { getData } from '@/api/chart'
import { ChartLibraryType } from '@/views/chart/components/js/panel/types'
import { G2PlotChartView } from '@/views/chart/components/js/panel/types/impl/g2plot'
import { L7PlotChartView } from '@/views/chart/components/js/panel/types/impl/l7plot'
import chartViewManager from '@/views/chart/components/js/panel'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ViewTrackBar from '@/components/visualization/ViewTrackBar.vue'
import { storeToRefs } from 'pinia'
import { getGeoJsonFile, parseJson } from '@/views/chart/components/js/util'
import { S2ChartView } from '@/views/chart/components/js/panel/types/impl/s2'
import debounce from 'lodash-es/debounce'
import en from '@/locales/en'
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
  dynamicAreaId: {
    type: String,
    required: false,
    default: ''
  }
})

const emit = defineEmits(['onChartClick', 'onDrillFilters', 'onJumpClick'])

const { view, showPosition, dynamicAreaId } = toRefs(props)

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

const containerId = 'container-' + showPosition.value + '-' + view.value.id
const viewTrack = ref(null)

const calcData = view => {
  if (!view.tableId) {
    return
  }
  state.loading = true
  const v = JSON.parse(JSON.stringify(view))
  getData(v)
    .then(res => {
      // console.log(res)
      state.data = res?.data
      emit('onDrillFilters', res?.drillFilters)
      renderChart(res)
    })
    .finally(() => {
      state.loading = false
    })
}

const renderChart = async view => {
  if (!view) {
    return
  }
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  const chart = { ...view, data: state.data }
  const chartView = chartViewManager.getChartView(view.render, view.type) as S2ChartView<any>
  state.myChart = chartView.drawChart({
    container: containerId,
    chart: toRaw(chart),
    chartObj: state.myChart,
    action
  })
  state.myChart?.render()
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
  if (!param || !param.data || !param.data.dimensionList) {
    // 地图提示没有关联字段 其他没有维度信息的 直接返回
    // if (this.chart.type === 'map') {
    //   this.$warning(this.$t('panel.no_drill_field'))
    // }
    return
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
  state.data &&
    state.data?.fields &&
    state.data?.fields.forEach(item => {
      const sourceInfo = view.value.id + '#' + item.id
      if (nowPanelTrackInfo.value[sourceInfo]) {
        linkageCount++
      }
      if (nowPanelJumpInfo.value[sourceInfo]) {
        jumpCount++
      }
    })
  jumpCount && trackMenuInfo.push('jump')
  linkageCount && trackMenuInfo.push('linkage')
  view.value.drillFields.length && trackMenuInfo.push('drill')
  return trackMenuInfo
})

defineExpose({
  calcData,
  renderChart
})
const resize = (width, height) => {
  debounce(() => {
    state.myChart?.changeSheetSize(width, height)
    state.myChart?.render()
  }, 500)()
}
onMounted(() => {
  const resizeObserver = new ResizeObserver(([entry] = []) => {
    console.info(entry)
    const [size] = entry.borderBoxSize || []
    resize(size.inlineSize, size.blockSize)
  })

  resizeObserver.observe(document.getElementById(containerId))
  // queryData(true)
  // renderChart({ render: ChartRenderType.ANT_V, type: 'bar' })
})
onBeforeUnmount(() => {
  state.myChart?.destroy()
})
</script>

<template>
  <div class="canvas-area" v-loading="state.loading">
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      class="track-bar"
      :style="state.trackBarStyle"
      @trackClick="trackClick"
    />
    <div class="canvas-content" :id="containerId"></div>
  </div>
</template>

<style lang="less" scoped>
.canvas-area {
  position: relative;
  width: 100%;
  height: 100%;
  .canvas-content {
    width: 100%;
    height: 100%;
  }
}
</style>
