<script lang="ts" setup>
import {
  computed,
  inject,
  onBeforeUnmount,
  onMounted,
  PropType,
  reactive,
  ref,
  ShallowRef,
  toRaw,
  toRefs
} from 'vue'
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
  }
})

const emit = defineEmits(['onChartClick', 'onDrillFilters', 'onJumpClick'])

const { view, showPosition } = toRefs(props)

const isError = ref(false)
const errMsg = ref('')
const chartExtRequest = inject('chartExtRequest') as ShallowRef<object>

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
  totalItems: 0,
  showPage: false
})

const containerId = 'container-' + showPosition.value + '-' + view.value.id
const viewTrack = ref(null)

const calcData = async (view: Chart, resetPageInfo = true, callback) => {
  if (!view.tableId) {
    return
  }
  isError.value = false
  const v = JSON.parse(JSON.stringify(view))
  await getData(v).then(res => {
    if (res.code && res.code !== 0) {
      isError.value = true
      errMsg.value = res.msg
    } else {
      state.data = res?.data
      state.totalItems = res?.totalItems
      emit('onDrillFilters', res?.drillFilters)
      renderChart(res as unknown as Chart, resetPageInfo)
    }
  })
  callback?.()
}
// 图表对象不用响应式
let myChart = null
const renderChart = async (view: Chart, resetPageInfo: boolean) => {
  if (!view) {
    return
  }
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  const chart = { ...view, data: toRaw(state.data) } as ChartObj
  setupPage(chart, resetPageInfo)
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
const setupPage = (chart: ChartObj, resetPageInfo?: boolean) => {
  const customAttr = chart.customAttr
  if (chart.type !== 'table-info' || customAttr.basicStyle.tablePageMode !== 'page') {
    state.showPage = false
    return
  }
  const pageInfo = state.pageInfo
  pageInfo.pageSize = customAttr.basicStyle.tablePageSize ?? 20
  if (state.totalItems > state.pageInfo.pageSize) {
    pageInfo.total = state.totalItems
    state.showPage = true
  } else {
    state.showPage = false
  }
  if (resetPageInfo) {
    state.pageInfo.currentPage = 1
  }
}

const showPage = computed(() => {
  if (view.value.type !== 'table-info') {
    return false
  }
  return state.showPage
})

const handleCurrentChange = pageNum => {
  let extReq = { goPage: pageNum }
  if (chartExtRequest) {
    extReq = { ...extReq, ...chartExtRequest.value }
  }
  const chart = { ...view.value, chartExtRequest: extReq }
  calcData(chart, false)
}

const action = param => {
  // 下钻 联动 跳转
  state.pointParam = param
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
  const linkageParam = {
    option: 'linkage',
    name: state.pointParam.data.name,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: state.pointParam.data.quotaList
  }
  const jumpParam = {
    option: 'jump',
    name: state.pointParam.data.name,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: state.pointParam.data.quotaList,
    sourceType: state.pointParam.data.sourceType
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
  state.data?.fields?.forEach(item => {
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
    myChart?.changeSheetSize(width, height)
    myChart?.render()
  }, 500)
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
  <div class="canvas-area">
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
      <div :style="{ color: pageColor }">共{{ state.pageInfo.total }}条</div>
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
  z-index: 0;
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
