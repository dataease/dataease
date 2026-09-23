<script lang="ts" setup>
import {
  computed,
  CSSProperties,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  PropType,
  reactive,
  ref,
  shallowRef,
  ShallowRef,
  toRaw,
  toRefs,
  watch
} from 'vue'
import { getData } from '@/api/chart'
import chartViewManager from '@/views/chart/components/js/panel'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ViewTrackBar from '@/components/visualization/ViewTrackBar.vue'
import { storeToRefs } from 'pinia'
import { S2ChartView } from '@/views/chart/components/js/panel/types/impl/s2'
import { ElMessage, ElPagination } from 'element-plus-secondary'
import ChartError from '@/views/chart/components/views/components/ChartError.vue'
import { defaultsDeep, cloneDeep, debounce } from 'lodash-es'
import { BASE_VIEW_CONFIG } from '../../editor/util/chart'
import { customAttrTrans, customStyleTrans, recursionTransObj } from '@/utils/canvasStyle'
import { deepCopy, isISOMobile, isMobile } from '@/utils/utils'
import { useEmitt } from '@/hooks/web/useEmitt'
import {
  getDataVControlScale,
  isDashboard,
  isDataVTouchDevice,
  trackBarStyleCheck
} from '@/utils/canvasUtils'
import { S2Event, type SpreadSheet } from '@antv/s2'
import { parseJson } from '../../js/util'
import { useI18n } from '@/hooks/web/useI18n'
import { hasNextDrillLevel, isCurrentDrillField } from '@/views/chart/components/views/util/drill'

const dvMainStore = dvMainStoreWithOut()
const {
  nowPanelTrackInfo,
  nowPanelJumpInfo,
  mobileInPc,
  canvasStyleData,
  embeddedCallBack,
  inMobile
} = storeToRefs(dvMainStore)
const { emitter } = useEmitt()
const { t } = useI18n()

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
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  terminal: {
    type: String,
    default: 'pc'
  },
  drillLength: {
    type: Number,
    required: false,
    default: 0
  },
  //图表渲染id后缀
  suffixId: {
    type: String,
    required: false,
    default: 'common'
  },
  fontFamily: {
    type: String,
    required: false,
    default: 'inherit'
  }
})

const emit = defineEmits(['onPointClick', 'onChartClick', 'onDrillFilters', 'onJumpClick'])
const dataVMobile = !isDashboard() && isMobile()

const { view, showPosition, scale, terminal, drillLength, suffixId } = toRefs(props)
const controlScale = computed(() => getDataVControlScale(scale.value))
const touchScrollEnabled = computed(isDataVTouchDevice)

const isError = ref(false)
const errMsg = ref('')
const chartExtRequest = inject('chartExtRequest') as ShallowRef<object>

const state = reactive({
  curActionId: null,
  curTrackMenu: [],
  trackBarStyle: {
    position: 'absolute',
    left: '50px',
    top: '50px'
  },
  linkageActiveParam: null,
  pointParam: null,
  loading: false,
  data: { fields: [] }, // 图表数据
  pageInfo: {
    total: 0,
    pageSize: 20,
    currentPage: 1
  },
  totalItems: 0,
  showPage: false,
  pageStyle: 'simple',
  currentPageSize: 0,
  imgEnlarge: false,
  imgSrc: ''
})
const PAGE_CHARTS = ['table-info', 'table-normal']
// 图表数据不用全响应式
let chartData = shallowRef<Partial<Chart['data']>>({
  fields: []
})

const containerId = 'container-' + showPosition.value + '-' + view.value.id + '-' + suffixId.value
const viewTrack = ref(null)

const calcData = (viewInfo: Chart, callback, resetPageInfo = true) => {
  if (viewInfo.customAttr.basicStyle.tablePageStyle === 'general') {
    if (state.currentPageSize !== 0) {
      viewInfo.chartExtRequest.pageSize = state.currentPageSize
      state.pageInfo.pageSize = state.currentPageSize
    } else {
      viewInfo.chartExtRequest.pageSize = state.pageInfo.pageSize
    }
  } else {
    delete viewInfo.chartExtRequest?.pageSize
  }
  if (viewInfo.tableId || viewInfo['dataFrom'] === 'template') {
    isError.value = false
    const v = JSON.parse(JSON.stringify(viewInfo))
    getData(v)
      .then(res => {
        if (res.code && res.code !== 0) {
          isError.value = true
          errMsg.value = res.msg
        } else {
          chartData.value = res?.data as Partial<Chart['data']>
          state.totalItems = res?.totalItems
          dvMainStore.setViewDataDetails(viewInfo.id, res)
          emit('onDrillFilters', res?.drillFilters)
          renderChart(res as unknown as Chart, resetPageInfo)
        }
        callback?.()
      })
      .catch(() => {
        callback?.()
      })
  } else {
    callback?.()
  }
}
// 图表对象不用响应式
let myChart: SpreadSheet = null
// 实际渲染的图表信息，适应缩放
let actualChart: ChartObj
const renderChartFromDialog = (viewInfo: Chart, chartDataInfo) => {
  chartData.value = chartDataInfo
  renderChart(viewInfo, false)
}
// 处理存量图表的默认值
const handleDefaultVal = (chart: Chart) => {
  const customAttr = parseJson(chart.customAttr)
  // 明细表默认合并单元格，存量的不合并
  if (customAttr.tableCell.mergeCells === undefined) {
    customAttr.tableCell.mergeCells = false
  }
  if (chart.type === 'table-pivot') {
    if (!customAttr.tableTotal?.row?.subTotalsDimensionsNew) {
      customAttr.tableTotal.row.subTotalsDimensionsNew =
        !!customAttr.tableTotal.row.subTotalsDimensionsNew
    }
    const { tableHeader } = customAttr
    // 存量透视表处理
    if (!tableHeader.tableHeaderColBgColor) {
      tableHeader.tableHeaderColBgColor = tableHeader.tableHeaderBgColor
      tableHeader.tableHeaderColFontColor = tableHeader.tableHeaderFontColor
      tableHeader.tableTitleColFontSize = tableHeader.tableTitleFontSize
      tableHeader.tableHeaderColAlign = tableHeader.tableHeaderAlign
      tableHeader.isColBolder = tableHeader.isBolder
      tableHeader.isColItalic = tableHeader.isItalic

      tableHeader.tableHeaderCornerBgColor = tableHeader.tableHeaderBgColor
      tableHeader.tableHeaderCornerFontColor = tableHeader.tableHeaderFontColor
      tableHeader.tableTitleCornerFontSize = tableHeader.tableTitleFontSize
      tableHeader.tableHeaderCornerAlign = tableHeader.tableHeaderAlign
      tableHeader.isCornerBolder = tableHeader.isBolder
      tableHeader.isCornerItalic = tableHeader.isItalic
    }
  }
}

/**
 * 根据图表请求状态恢复 S2 下钻状态
 * 仪表板在 resize/scale 重绘时可能复用原始 view，但数据仍是下钻后的结果
 * 这里通过 chartExtRequest.drill 反推 drillFilters，避免表头字段回退
 *
 */
const restoreDrillState = (chart: ChartObj) => {
  const drillRequests = chart.chartExtRequest?.drill
  const drillFields = chart.drillFields ?? []
  if (!drillRequests?.length || chart.drillFilters?.length || drillFields.length < 2) {
    return
  }
  if (drillRequests.length >= drillFields.length) {
    return
  }
  const drillFilters = []
  for (let index = 0; index < drillRequests.length; index++) {
    const request = drillRequests[index]
    const drillField = drillFields[index]
    const dimension = request.dimensionList?.find(item => item.id === drillField?.id)
    if (!dimension) {
      return
    }
    drillFilters.push({
      fieldId: dimension.id,
      value: dimension.value !== undefined && dimension.value !== null ? [dimension.value] : []
    })
  }
  chart.drill = true
  chart.drillFilters = drillFilters
}

const renderChart = (viewInfo: Chart, resetPageInfo: boolean) => {
  if (!viewInfo) {
    return
  }
  handleDefaultVal(viewInfo)
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  actualChart = deepCopy({
    ...defaultsDeep(viewInfo, cloneDeep(BASE_VIEW_CONFIG)),
    data: chartData.value,
    fontFamily: props.fontFamily
  } as ChartObj)
  restoreDrillState(actualChart)

  recursionTransObj(customAttrTrans, actualChart.customAttr, scale.value, terminal.value)
  recursionTransObj(customStyleTrans, actualChart.customStyle, scale.value, terminal.value)

  setupPage(actualChart, resetPageInfo)
  nextTick(() => debounceRender(resetPageInfo))
}

// 滚动条随移动端大屏缩放，只合并尺寸以保留其他主题样式
const setScrollBarScale = (value: number) => {
  myChart?.setTheme({
    scrollBar: {
      size: 8 * value,
      hoverSize: 12 * value,
      // 最小滑块长度也需等比缩放，避免覆盖短轨道后被判定为已到滚动边界
      thumbHorizontalMinSize: 32 * value,
      thumbVerticalMinSize: 32 * value
    }
  })
}

const limitScrollBarThumbs = (instance: SpreadSheet) => {
  if (controlScale.value === 1 || !instance.facet) {
    return
  }
  const { facet } = instance
  const { scrollX, scrollY, rowHeaderScrollX } = facet.getScrollOffset()
  const scrollBars = [
    { bar: facet.hScrollBar, offset: scrollX },
    { bar: facet.vScrollBar, offset: scrollY },
    { bar: facet.hRowScrollBar, offset: rowHeaderScrollX }
  ]
  scrollBars.forEach(({ bar, offset }) => {
    if (
      !bar ||
      !Number.isFinite(bar.trackLen) ||
      bar.trackLen <= 0 ||
      !Number.isFinite(bar.scrollTargetMaxOffset) ||
      bar.scrollTargetMaxOffset <= 0 ||
      bar.thumbLen < bar.trackLen
    ) {
      return
    }
    // 使用布局后的实际轨道留出滑动距离，兼容非等比缩放和冻结区域
    const thumbLen = bar.trackLen - Math.min(bar.theme.size, bar.trackLen / 2)
    if (bar.theme.size >= thumbLen) {
      bar.theme = {
        ...bar.theme,
        size: thumbLen / 2,
        hoverSize: Math.min(bar.theme.hoverSize, thumbLen)
      }
      bar.trackShape.attr('lineWidth', bar.theme.size)
      bar.thumbShape.attr('lineWidth', bar.theme.size)
    }
    const ratio = Math.max(0, Math.min(1, (offset || 0) / bar.scrollTargetMaxOffset))
    // 只更新滑块图形，避免 updateThumbLen 发出滚动事件而改变内容位置
    bar.thumbLen = thumbLen
    bar.thumbOffset = 0
    bar.onlyUpdateThumbOffset(ratio * (bar.trackLen - thumbLen))
  })
}

const debounceRender = debounce(() => {
  stopAutoScroll()
  myChart?.facet?.cancelScrollFrame()
  myChart?.destroy()
  myChart?.getCanvasElement()?.remove()
  const chartView = chartViewManager.getChartView(
    actualChart.render,
    actualChart.type
  ) as S2ChartView<any>
  myChart = chartView.drawChart({
    container: containerId,
    chart: toRaw(actualChart),
    chartObj: myChart,
    pageInfo: state.pageInfo,
    action,
    resizeAction,
    touchAction
  })
  if (controlScale.value !== 1) {
    setScrollBarScale(controlScale.value)
  }
  const instance = myChart
  instance?.on(S2Event.LAYOUT_AFTER_RENDER, () => limitScrollBarThumbs(instance))
  instance?.on(S2Event.GLOBAL_SCROLL, () => onTableScroll(instance))
  myChart?.render()
  dvMainStore.setViewInstanceInfo(actualChart.id, myChart)
  initScroll()
}, 500)

watch(
  controlScale,
  value => {
    if (!myChart?.facet) {
      return
    }
    stopAutoScroll()
    myChart.facet.cancelScrollFrame()
    setScrollBarScale(value)
    myChart.render(false)
    initScroll()
  },
  { flush: 'post' }
)

const setupPage = (chart: ChartObj, resetPageInfo?: boolean) => {
  const customAttr = chart.customAttr
  if (!PAGE_CHARTS.includes(chart.type) || customAttr.basicStyle.tablePageMode !== 'page') {
    state.showPage = false
    return
  }
  const pageInfo = state.pageInfo
  state.pageStyle = customAttr.basicStyle.tablePageStyle
  if (state.pageStyle !== 'general') {
    pageInfo.pageSize = customAttr.basicStyle.tablePageSize ?? 20
  }
  if (state.totalItems > state.pageInfo.pageSize || state.pageStyle === 'general') {
    pageInfo.total = state.totalItems
    state.showPage = true
  } else {
    state.showPage = false
  }
  if (resetPageInfo) {
    state.pageInfo.currentPage = 1
  }
  dvMainStore.setViewPageInfo(chart.id, state.pageInfo)
}

let scrollFrame: number | undefined
const activeTouches = new Map<number, { x: number; y: number }>()
const touchTargets = new Set<HTMLElement>()
let manualScrollPause = false
let touchMoved = false
let suppressClickUntil = 0

const clearTouchListeners = () => {
  touchTargets.forEach(target => {
    target.removeEventListener('touchmove', onTableTouchMove, true)
    target.removeEventListener('touchend', onTableTouchEnd, true)
    target.removeEventListener('touchcancel', onTableTouchEnd, true)
  })
  touchTargets.clear()
}

const onTableTouchStart = (event: TouchEvent) => {
  if (!touchScrollEnabled.value) {
    return
  }
  if (!activeTouches.size) {
    touchMoved = false
    suppressClickUntil = 0
  }
  Array.from(event.changedTouches).forEach(touch => {
    activeTouches.set(touch.identifier, { x: touch.clientX, y: touch.clientY })
  })
  touchMoved ||= event.touches.length > 1
  manualScrollPause = true
  stopAutoScroll()
  // 保留原触摸目标的监听，重绘替换 canvas 后也能收到松手事件
  const target = event.target as HTMLElement
  if (!touchTargets.has(target)) {
    const options = { capture: true, passive: true }
    target.addEventListener('touchmove', onTableTouchMove, options)
    target.addEventListener('touchend', onTableTouchEnd, options)
    target.addEventListener('touchcancel', onTableTouchEnd, options)
    touchTargets.add(target)
  }
}

const onTableTouchMove = (event: TouchEvent) => {
  Array.from(event.changedTouches).forEach(touch => {
    const start = activeTouches.get(touch.identifier)
    // 按屏幕距离区分点击与滑动，阈值不随图表比例缩小
    if (start && Math.hypot(touch.clientX - start.x, touch.clientY - start.y) > 6) {
      touchMoved = true
    }
  })
}

const onTableTouchEnd = (event: TouchEvent) => {
  onTableTouchMove(event)
  let ended = false
  Array.from(event.changedTouches).forEach(touch => {
    if (activeTouches.delete(touch.identifier)) {
      ended = true
    }
  })
  if (!ended) {
    return
  }
  touchMoved ||= event.type === 'touchcancel'
  if (touchMoved) {
    suppressClickUntil = performance.now() + 500
  }
  if (!activeTouches.size) {
    clearTouchListeners()
    initScroll()
  }
}

const suppressTouchClick = () =>
  touchScrollEnabled.value &&
  ((activeTouches.size > 0 && touchMoved) || performance.now() < suppressClickUntil)

const onTableScroll = (instance: SpreadSheet) => {
  // 自动滚动也会发出 GLOBAL_SCROLL，仅触摸后的惯性滚动延后恢复
  if (instance === myChart && manualScrollPause && !activeTouches.size) {
    initScroll()
  }
}

const stopAutoScroll = () => {
  clearTimeout(scrollTimer)
  if (scrollFrame !== undefined) {
    cancelAnimationFrame(scrollFrame)
    scrollFrame = undefined
  }
  myChart?.facet?.timer?.stop()
}

const mouseMove = () => {
  // 触屏兼容鼠标事件不能取消松手后安排的自动滚动
  if (!manualScrollPause) {
    stopAutoScroll()
  }
}

const mouseLeave = () => {
  if (!manualScrollPause) {
    initScroll()
  }
}

let scrollTimer: ReturnType<typeof setTimeout>
const initScroll = () => {
  stopAutoScroll()
  if (activeTouches.size) {
    return
  }
  scrollTimer = setTimeout(() => {
    if (activeTouches.size) {
      return
    }
    manualScrollPause = false
    const customAttr = actualChart?.customAttr
    const senior = actualChart?.senior
    if (
      myChart &&
      senior?.scrollCfg?.open &&
      chartData.value.tableRow?.length &&
      PAGE_CHARTS.includes(props.view.type) &&
      !state.showPage
    ) {
      const facet = myChart.facet
      facet.timer?.stop()
      if (!document.getElementById(containerId)?.offsetHeight) {
        return
      }

      // 使用 S2 实际的滚动范围，避免估算高度超出底部后无法重新开始。
      const maxScrollY = facet.vScrollBar?.scrollTargetMaxOffset ?? 0
      if (!Number.isFinite(maxScrollY) || maxScrollY <= 0) {
        return
      }

      const { scrollY } = facet.getScrollOffset()
      let scrolledOffset = scrollY || 0
      if (scrolledOffset >= maxScrollY - 1) {
        facet.setScrollOffset({ scrollY: 0 })
        facet.startScroll()
        scrolledOffset = 0
      }

      const rowHeight = customAttr.basicStyle?.autoWrap
        ? facet.viewCellHeights.getTotalHeight() / chartData.value.tableRow.length
        : customAttr.tableCell.tableItemHeight
      const scrollViewCount = (maxScrollY - scrolledOffset) / rowHeight
      const duration = (scrollViewCount / senior.scrollCfg.row) * senior.scrollCfg.interval
      if (!Number.isFinite(duration) || duration <= 0) {
        return
      }
      // 只更新纵向坐标，避免 S2 对固定横向坐标插值后取整产生 1px 抖动。
      const startTime = performance.now()
      const scroll = (now: number) => {
        scrollFrame = undefined
        const progress = Math.min((now - startTime) / duration, 1)
        facet.setScrollOffset({
          scrollY: scrolledOffset + (maxScrollY - scrolledOffset) * progress
        })
        facet.startScroll()
        if (progress < 1) {
          scrollFrame = requestAnimationFrame(scroll)
        } else {
          initScroll()
        }
      }
      scrollFrame = requestAnimationFrame(scroll)
    }
  }, 1500)
}

const showPage = computed(() => {
  if (!PAGE_CHARTS.includes(view.value.type)) {
    return false
  }
  return state.showPage
})

const handleCurrentChange = pageNum => {
  let extReq = { goPage: pageNum }
  if (chartExtRequest.value) {
    extReq = { ...extReq, ...chartExtRequest.value }
  }
  const chart = { ...view.value, chartExtRequest: extReq }
  calcData(chart, null, false)
}

const handlePageSizeChange = pageSize => {
  if (state.pageStyle === 'general') {
    state.currentPageSize = pageSize
    emitter.emit('set-page-size', pageSize)
    state.pageInfo.currentPage = 1
  }
  let extReq = { pageSize: pageSize }
  if (chartExtRequest.value) {
    extReq = { ...extReq, ...chartExtRequest.value }
  }
  const chart = { ...view.value, chartExtRequest: extReq }
  calcData(chart, null, false)
}

const pointClickTrans = () => {
  if (embeddedCallBack.value === 'yes') {
    trackClick('pointClick')
  }
}

const touchAction = (callback, fieldId) => {
  if (suppressTouchClick()) {
    return
  }
  if (fieldId) {
    state.curActionId = fieldId
  }
  if (!trackMenu.value.length) {
    callback?.()
  }
}

const action = param => {
  if (suppressTouchClick()) {
    return
  }
  state.pointParam = param
  state.curActionId = param.data.name
  state.curTrackMenu = trackMenuCalc(state.curActionId)
  // 点击
  pointClickTrans()
  // 下钻 联动 跳转
  if (trackMenu.value.length < 2) {
    if (view.value.drillFields.length > 0 && trackMenu.value.length === 0) {
      if (showPosition.value === 'viewDialog') {
        return
      }
      if (view.value.type === 'table-pivot') {
        return
      }
      // 存在下钻时，只有点击当前下钻层级的字段才提示已到最后一层，点击其他字段不提示
      const currentDrillField = view.value.drillFields[drillLength.value]
      if (currentDrillField?.id !== state.curActionId) {
        return
      }
      ElMessage.error(t('chart.last_layer'))
      return
    }
    // 只有一个事件直接调用
    trackClick(trackMenu.value[0])
  } else {
    // 图表关联多个事件
    const barStyleTemp = {
      left: param.x - 50,
      top: param.y + 10
    }
    trackBarStyleCheck(props.element, barStyleTemp, props.scale, trackMenu.value.length)
    if (dataVMobile) {
      state.trackBarStyle.left = barStyleTemp.left + 40 + 'px'
      state.trackBarStyle.top = barStyleTemp.top + 70 + 'px'
    } else {
      state.trackBarStyle.left = barStyleTemp.left + 'px'
      state.trackBarStyle.top = barStyleTemp.top + 'px'
    }

    viewTrack.value.trackButtonClick(view.value.id)
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
  // 明细表 汇总表特殊处理 1.点击维度传递触发字段的值 2.点击指标传递的值非触发的维度字段值
  if (['table-info', 'table-normal'].includes(view.value.type)) {
    linkageParam.quotaList = []
    const dimensionIds = []
    const quotaIds = []
    view.value.xAxis.forEach(xd => {
      if (xd.groupType === 'd') {
        dimensionIds.push(xd.id)
      } else {
        quotaIds.push(xd.id)
      }
    })
    view.value.yAxis.forEach(xd => {
      if (xd.groupType === 'd') {
        dimensionIds.push(xd.id)
      } else {
        quotaIds.push(xd.id)
      }
    })
    if (dimensionIds.includes(param.data.name)) {
      linkageParam.dimensionList = linkageParam.dimensionList.filter(
        dimension => dimension.id === param.data.name
      )
    } else if (quotaIds.includes(param.data.name)) {
      linkageParam.dimensionList = linkageParam.dimensionList.filter(dimension =>
        dimensionIds.includes(dimension.id)
      )
    }
    view.value
  }
  const jumpParam = {
    option: 'jump',
    name: state.pointParam.data.name,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: state.pointParam.data.quotaList,
    sourceType: state.pointParam.data.sourceType
  }

  const clickParams = {
    option: 'pointClick',
    name: state.pointParam.data.name,
    viewId: view.value.id,
    dimensionList: state.pointParam.data.dimensionList,
    quotaList: state.pointParam.data.quotaList
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
      dvMainStore.addViewTrackFilter(linkageParam)
      break
    case 'jump':
      if (mobileInPc.value && !inMobile.value) return
      emit('onJumpClick', jumpParam)
      break
    case 'enlarge':
      if (view.value.type === 'table-info') {
        param.data.dimensionList?.forEach(d => {
          if (d.id === state.curActionId) {
            state.imgSrc = d.value
            state.imgEnlarge = true
          }
        })
      }
      break
    default:
      break
  }
}

const trackMenu = computed(() => {
  if (['table-info', 'table-normal'].includes(view.value.type) && state.curActionId) {
    return trackMenuCalc(state.curActionId)
  } else {
    return trackMenuCmp.value
  }
})

const trackMenuCmp = computed(() => {
  let trackMenuInfo = []
  if (showPosition.value === 'viewDialog') {
    return trackMenuInfo
  }
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
  if (view.value?.drillFields && view.value?.drillFilters && view.value.drillFilters.length > 0) {
    const lastItem = view.value?.drillFields[view.value.drillFilters.length]
    const sourceInfo = view.value.id + '#' + lastItem.id
    if (nowPanelTrackInfo.value[sourceInfo]) {
      linkageCount++
    }
    if (nowPanelJumpInfo.value[sourceInfo]) {
      jumpCount++
    }
  }
  jumpCount &&
    view.value?.jumpActive &&
    (!mobileInPc.value || inMobile.value) &&
    trackMenuInfo.push('jump')
  linkageCount && view.value?.linkageActive && trackMenuInfo.push('linkage')
  view.value.type !== 'table-pivot' &&
    hasNextDrillLevel(view.value.drillFields, drillLength.value) &&
    trackMenuInfo.push('drill')
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
  return trackMenuInfo
})

const trackMenuCalc = itemId => {
  let trackMenuInfo = []
  if (showPosition.value === 'viewDialog') {
    return trackMenuInfo
  }
  let linkageCount = 0
  let jumpCount = 0
  let drillCount = 0
  const sourceInfo = view.value.id + '#' + itemId
  if (nowPanelTrackInfo.value[sourceInfo]) {
    linkageCount++
  }
  if (nowPanelJumpInfo.value[sourceInfo]) {
    jumpCount++
  }
  jumpCount &&
    view.value?.jumpActive &&
    (!mobileInPc.value || inMobile.value) &&
    trackMenuInfo.push('jump')
  linkageCount && view.value?.linkageActive && trackMenuInfo.push('linkage')
  // 判断是否有下钻 同时判断下钻到第几层
  if (isCurrentDrillField(view.value.drillFields, drillLength.value, itemId)) {
    drillCount++
  }
  view.value.type !== 'table-pivot' && drillCount && trackMenuInfo.push('drill')
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
  // 明细表 URL 字段图片放大
  if (view.value.type === 'table-info') {
    view.value.xAxis?.forEach(axis => {
      if (axis.id === itemId && axis.deType === 7) {
        trackMenuInfo.push('enlarge')
      }
    })
  }
  return trackMenuInfo
}

const resizeAction = resizeColumn => {
  // 从头开始滚动
  if (myChart?.facet) {
    stopAutoScroll()
    nextTick(initScroll)
  }
  if (showPosition.value !== 'canvas') {
    return
  }
  const fieldId: string = resizeColumn.info.meta.field
  const { basicStyle } = view.value.customAttr
  const containerWidth = document.getElementById(containerId).offsetWidth
  const column = basicStyle.tableFieldWidth?.find(i => i.fieldId === fieldId)
  let tableWidth: ChartBasicStyle['tableFieldWidth']
  const width = parseFloat(((resizeColumn.info.resizedWidth / containerWidth) * 100).toFixed(2))
  if (column) {
    column.width = width
    tableWidth = [...basicStyle.tableFieldWidth]
  } else {
    const tmp = { fieldId, width }
    tableWidth = basicStyle.tableFieldWidth?.length ? [...basicStyle.tableFieldWidth, tmp] : [tmp]
  }
  emitter.emit('set-table-column-width', tableWidth)
}
defineExpose({
  calcData,
  renderChart,
  renderChartFromDialog,
  trackMenu
})

let timer
const resize = (width, height) => {
  if (timer) {
    clearTimeout(timer)
  }
  timer = setTimeout(() => {
    if (!myChart?.facet) {
      debounceRender(false)
    } else {
      stopAutoScroll()
      myChart?.changeSheetSize(width, height)
      myChart?.render()
    }
    initScroll()
  }, 500)
}
const preSize = [0, 0]
const TOLERANCE = 1
let resizeObserver: ResizeObserver
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
    resize(size.inlineSize, Math.round(size.blockSize))
  })

  resizeObserver.observe(document.getElementById(containerId))
})
onBeforeUnmount(() => {
  stopAutoScroll()
  clearTouchListeners()
  activeTouches.clear()
  clearTimeout(timer)
  debounceRender.cancel()
  try {
    myChart?.facet.timer?.stop()
    myChart?.destroy()
    myChart = null
    resizeObserver?.disconnect()
  } catch (e) {
    console.warn(e)
  }
})

const autoStyle = computed(() => {
  const adaptorScale =
    (scale.value * (canvasStyleData.value.component.seniorStyleSetting?.pagerSize || 14)) / 14
  if (isISOMobile()) {
    return {
      height: 20 * adaptorScale + 8 + 'px',
      width: 100 / adaptorScale + '%!important',
      left: 50 * (1 - 1 / adaptorScale) + '%', // 放大余量 除以 2
      transform: 'scale(' + adaptorScale + ') translateZ(0)'
    } as CSSProperties
  } else {
    return { zoom: adaptorScale }
  }
})

const tabStyle = computed(() => [
  { '--de-pager-color': canvasStyleData.value.component.seniorStyleSetting?.pagerColor }
])

const tablePageClass = computed(() => {
  return (
    ['#ffffff', '#ffffffff', '#a6a6a6ff'].includes(
      canvasStyleData.value.component.seniorStyleSetting?.pagerColor.toLowerCase()
    ) && 'table-page-info_dark'
  )
})
</script>

<template>
  <div class="canvas-area">
    <view-track-bar
      ref="viewTrack"
      :track-menu="trackMenu"
      :font-family="fontFamily"
      class="track-bar"
      :style="state.trackBarStyle"
      @trackClick="trackClick"
      :is-data-v-mobile="dataVMobile"
      @mousemove="mouseMove"
    />
    <div v-if="!isError" class="canvas-content">
      <div
        :id="containerId"
        style="position: relative; height: 100%"
        @mousemove="mouseMove"
        @mouseleave="mouseLeave"
        @touchstart.capture.passive="onTableTouchStart"
      ></div>
    </div>
    <el-row :style="autoStyle" v-if="showPage && !isError">
      <div
        class="table-page-info"
        :class="tablePageClass"
        :style="tabStyle"
        @keydown.stop
        @keyup.stop
      >
        <div>{{ t('chart.total') }} {{ state.pageInfo.total }} {{ t('chart.items') }}</div>
        <el-pagination
          v-if="state.pageStyle !== 'general'"
          class="table-page-content"
          layout="prev, pager, next"
          v-model:page-size="state.pageInfo.pageSize"
          v-model:current-page="state.pageInfo.currentPage"
          :pager-count="5"
          :total="state.pageInfo.total"
          @update:current-page="handleCurrentChange"
        />
        <el-pagination
          v-else
          class="table-page-content"
          layout="prev, pager, next, sizes, jumper"
          v-model:page-size="state.pageInfo.pageSize"
          v-model:current-page="state.pageInfo.currentPage"
          :pager-count="5"
          :total="state.pageInfo.total"
          @update:current-page="handleCurrentChange"
          @update:page-size="handlePageSizeChange"
        />
      </div>
    </el-row>
    <chart-error v-if="isError" :err-msg="errMsg" />
  </div>
  <el-dialog v-model="state.imgEnlarge" append-to-body class="image-dialog">
    <div class="enlarge-image">
      <img :src="state.imgSrc" style="width: 100%; height: 100%; object-fit: contain" />
    </div>
  </el-dialog>
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

.table-page-info_dark {
  --ed-fill-color-blank: #00000000;
}

.table-page-info {
  --ed-text-color-regular: var(--de-pager-color);
  position: relative;
  padding-left: 4px;
  margin: 4px;
  height: 20px;
  display: flex;
  width: 100%;
  font-size: 14px;
  color: var(--de-pager-color);
  :deep(.table-page-content) {
    button,
    button[disabled] {
      color: var(--de-pager-color);
      background: transparent !important;
    }
    ul li {
      &:not(.is-active) {
        color: var(--de-pager-color);
      }
      background: transparent !important;
    }
  }
}
</style>
<style lang="less">
.image-dialog {
  height: 100%;
  .ed-dialog__body {
    height: calc(100% - 24px);
    width: 100%;
  }
}
.enlarge-image {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
  flex-direction: row;
  justify-content: center;
}
.antv-s2-tooltip-container {
  max-width: 400px;
  min-width: 80px;
}
</style>
