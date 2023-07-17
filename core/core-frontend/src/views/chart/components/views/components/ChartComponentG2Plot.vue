<script lang="ts" setup>
import { computed, onBeforeMount, onMounted, reactive, ref, toRefs } from 'vue'
import { getData } from '@/api/chart'
import { G2PlotChartView } from '@/views/chart/components/js/panel/types'
import chartViewManager from '@/views/chart/components/js/panel'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useFilter } from '@/hooks/web/useFilter'
import { cloneDeep } from 'lodash-es'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ViewTrackBar from '@/components/visualization/ViewTrackBar.vue'
import { storeToRefs } from 'pinia'
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
  state.loading = true
  const v = JSON.parse(JSON.stringify(view))
  getData(v)
    .then(res => {
      // console.log(res)
      state.data = res?.data
      emit('onDrillFilters', res.drillFilters)
      renderChart(res)
    })
    .finally(() => {
      state.loading = false
    })
}

const renderChart = view => {
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  const chart = { ...view, data: state.data }
  state.myChart = (
    chartViewManager.getChartView(view.render, view.type) as G2PlotChartView<any, any>
  )?.drawChart({
    chartObj: state.myChart,
    container: containerId,
    chart: chart,
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

onMounted(() => {
  // queryData(true)
  // renderChart({ render: ChartRenderType.ANT_V, type: 'bar' })
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
