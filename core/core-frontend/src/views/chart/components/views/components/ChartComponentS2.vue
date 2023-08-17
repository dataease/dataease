<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, PropType, reactive, ref, toRaw, toRefs } from 'vue'
import { getData } from '@/api/chart'
import chartViewManager from '@/views/chart/components/js/panel'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ViewTrackBar from '@/components/visualization/ViewTrackBar.vue'
import { storeToRefs } from 'pinia'
import { S2ChartView } from '@/views/chart/components/js/panel/types/impl/s2'
import { ElPagination } from 'element-plus-secondary'
import ChartError from '@/views/chart/components/views/components/ChartError.vue'

const dvMainStore = dvMainStoreWithOut()
const { nowPanelTrackInfo, nowPanelJumpInfo } = storeToRefs(dvMainStore)

const props = defineProps({
  view: {
    type: Object as PropType<ChartObj>,
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
  data: { fields: [] }, // 图表数据
  pageInfo: {
    total: 0,
    pageSize: 20,
    currentPage: 1
  },
  showPage: false
})

const containerId = 'container-' + showPosition.value + '-' + view.value.id
const viewTrack = ref(null)

const calcData = view => {
  if (!view.tableId) {
    return
  }
  state.loading = true
  isError.value = false
  const v = JSON.parse(JSON.stringify(view))
  getData(v)
    .then(res => {
      if (res.code && res.code !== 0) {
        isError.value = true
        errMsg.value = res.msg
      } else {
        state.data = res?.data
        emit('onDrillFilters', res?.drillFilters)
        renderChart(res)
      }
    })
    .finally(() => {
      state.loading = false
    })
}
// 图表对象不用响应式
let myChart = null
const renderChart = async view => {
  if (!view) {
    return
  }
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  const chart = { ...view, data: state.data }
  setupPage(chart)
  myChart?.destroy()
  const chartView = chartViewManager.getChartView(view.render, view.type) as S2ChartView<any>
  myChart = chartView.drawChart({
    container: containerId,
    chart: toRaw(chart),
    chartObj: myChart,
    pageInfo: state.pageInfo,
    action
  })
  myChart?.render()
}

const pageColor = computed(() => {
  const text = view.value?.customStyle?.text
  return text.color ?? 'white'
})
const setupPage = (chart: ChartObj) => {
  const customAttr = chart.customAttr
  if (chart.type !== 'table-info' || customAttr.basicStyle.tablePageMode !== 'page') {
    state.showPage = false
    return
  }
  const pageInfo = state.pageInfo
  pageInfo.pageSize = customAttr.basicStyle.tablePageSize ?? 20
  if (chart.totalItems > state.pageInfo.pageSize) {
    pageInfo.total = chart.totalItems
    state.showPage = true
  }
}

const showPage = computed(() => {
  if (view.value.type !== 'table-info') {
    return false
  }
  return state.showPage
})

const handleCurrentChange = pageNum => {
  const pageReq = { goPage: pageNum }
  const chart = { ...view.value, chartExtRequest: pageReq }
  calcData(chart)
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

let timer
const resize = (width, height) => {
  if (timer) {
    clearTimeout(timer)
  }
  timer = setTimeout(() => {
    renderChart(view.value)
    // myChart?.changeSheetSize(width, height)
    // myChart?.render()
  }, 300)
}
const preSize = [0, 0]
const TOLERANCE = 1
let resizeObserver
onMounted(() => {
  resizeObserver = new ResizeObserver(([entry] = []) => {
    const [size] = entry.borderBoxSize || []
    // 拖动的时候宽高重新计算，误差范围内不重绘，误差先设置为1
    if (!(preSize[0] || preSize[1])) {
      preSize[0] = size.inlineSize
      preSize[1] = size.blockSize
    }
    const widthOffset = Math.abs(size.inlineSize - preSize[0])
    const heightOffset = Math.abs(size.blockSize - preSize[1])
    if (widthOffset < TOLERANCE && heightOffset < TOLERANCE) {
      return
    }
    preSize[0] = size.inlineSize
    preSize[1] = size.blockSize
    resize(size.inlineSize, size.blockSize)
  })

  resizeObserver.observe(document.getElementById(containerId))
})
onBeforeUnmount(() => {
  myChart?.destroy()
  resizeObserver?.disconnect()
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
    <div v-if="!isError" class="canvas-content">
      <div style="height: 100%" :id="containerId"></div>
    </div>
    <div class="table-page-info" v-if="showPage && !isError">
      <div :style="{ color: pageColor }">共有{{ state.pageInfo.total }}条数据</div>
      <el-pagination
        class="table-page-content"
        layout="prev, pager, next"
        v-model:page-size="state.pageInfo.pageSize"
        v-model:current-page="state.pageInfo.currentPage"
        :style="{ color: pageColor }"
        :pager-count="5"
        :total="state.pageInfo.total"
        @update:current-page="handleCurrentChange"
      />
    </div>
    <chart-error v-if="isError" :err-msg="errMsg" />
  </div>
</template>

<style lang="less" scoped>
.canvas-area {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  width: 100%;
  height: 100%;
  .canvas-content {
    flex: 1;
    width: 100%;
    overflow: hidden;
  }
}
.table-page-info {
  margin: 4px;
  height: 20px;
  display: flex;
  width: 100%;
  justify-content: space-between;
  :deep(.table-page-content) {
    button {
      color: inherit;
      background: transparent !important;
    }
    ul li {
      &:not(.is-active) {
        color: inherit;
      }
      background: transparent !important;
    }
  }
}
</style>
