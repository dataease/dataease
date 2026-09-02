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
  toRefs
} from 'vue'
import { getData } from '@/api/chart'
import chartViewManager from '@/views/chart/components/js/panel'
import { useAppStoreWithOut } from '@/store/modules/app'
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
import { isDashboard, trackBarStyleCheck } from '@/utils/canvasUtils'
import { type SpreadSheet } from '@antv/s2'
import { parseJson } from '../../js/util'

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
// 接口空结果可能返回 null，统一补齐 S2 表格依赖的数据结构
const normalizeChartData = (data?: Partial<Chart['data']> | null): Partial<Chart['data']> => ({
  ...data,
  data: data?.data ?? [],
  fields: data?.fields ?? [],
  tableRow: data?.tableRow ?? []
})
// 图表数据不用全响应式
let chartData = shallowRef<Partial<Chart['data']>>(normalizeChartData())

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
          chartData.value = normalizeChartData(res?.data as Partial<Chart['data']> | null)
          state.totalItems = res?.totalItems
          dvMainStore.setViewDataDetails(viewInfo.id, res)
          if (!res.drill) {
            dvMainStore.setPureCanvasViewDataInfo(viewInfo.id, res)
          }
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
let chartComponentUnmounted = false
// 实际渲染的图表信息，适应缩放
let actualChart: ChartObj
let renderChartResolvers: Array<() => void> = []
const resolveRenderChart = () => {
  const resolvers = renderChartResolvers
  renderChartResolvers = []
  resolvers.forEach(resolve => resolve())
}
const renderChartFromDialog = (viewInfo: Chart, chartDataInfo) => {
  chartData.value = normalizeChartData(chartDataInfo)
  return renderChart(viewInfo, false)
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
      const tableHeaderAlign =
        tableHeader.tableHeaderAlign === 'custom' ? 'left' : tableHeader.tableHeaderAlign
      tableHeader.tableHeaderColBgColor = tableHeader.tableHeaderBgColor
      tableHeader.tableHeaderColFontColor = tableHeader.tableHeaderFontColor
      tableHeader.tableTitleColFontSize = tableHeader.tableTitleFontSize
      tableHeader.tableHeaderColAlign = tableHeaderAlign
      tableHeader.isColBolder = tableHeader.isBolder
      tableHeader.isColItalic = tableHeader.isItalic

      tableHeader.tableHeaderCornerBgColor = tableHeader.tableHeaderBgColor
      tableHeader.tableHeaderCornerFontColor = tableHeader.tableHeaderFontColor
      tableHeader.tableTitleCornerFontSize = tableHeader.tableTitleFontSize
      tableHeader.tableHeaderCornerAlign = tableHeaderAlign
      tableHeader.isCornerBolder = tableHeader.isBolder
      tableHeader.isCornerItalic = tableHeader.isItalic
    }
  }
}
// 样式配置触发本地重绘时 view 不包含接口返回的下钻状态
const restoreDrillState = (chart: ChartObj) => {
  if (chart.drill === true && chart.drillFilters?.length) {
    return
  }
  const drillRequests = chart.chartExtRequest?.drill ?? []
  const drillFields = chart.drillFields ?? []
  if (!drillRequests.length || drillRequests.length >= drillFields.length) {
    return
  }

  // 请求层级、配置字段与返回数据必须同时匹配
  const entryField = drillFields[0]
  const currentDrillField = drillFields[drillRequests.length]
  const entryFieldExists = chart.xAxis?.some(field => String(field.id) === String(entryField?.id))
  const currentFieldExists = chart.data?.fields?.some(
    field => String(field.id) === String(currentDrillField?.id)
  )
  if (!entryFieldExists || !currentFieldExists) {
    return
  }

  const restoredFilters: Filter[] = []
  for (let index = 0; index < drillRequests.length; index++) {
    const drillField = drillFields[index]
    const dimension = drillRequests[index].dimensionList?.find(
      item => String(item.id) === String(drillField?.id)
    )
    if (!dimension || !drillField) {
      return
    }
    restoredFilters.push({
      fieldId: String(dimension.id),
      datasetTableField: drillField
    })
  }

  // 完整校验后整体恢复，避免产生半有效的下钻状态
  chart.drill = true
  chart.drillFilters = restoredFilters
}
const renderChart = (viewInfo: Chart, resetPageInfo?: boolean) => {
  if (!viewInfo) {
    return Promise.resolve()
  }
  handleDefaultVal(viewInfo)
  // view 为引用对象 需要存库 view.data 直接赋值会导致保存不必要的数据
  actualChart = deepCopy({
    ...defaultsDeep(viewInfo, cloneDeep(BASE_VIEW_CONFIG)),
    data: chartData.value,
    fontFamily: props.fontFamily
  } as ChartObj)

  // 在样式转换前恢复，确保表格按当前下钻层级构造列
  restoreDrillState(actualChart)
  recursionTransObj(customAttrTrans, actualChart.customAttr, scale.value, terminal.value)
  recursionTransObj(customStyleTrans, actualChart.customStyle, scale.value, terminal.value)

  setupPage(actualChart, resetPageInfo)
  return new Promise<void>(resolve => {
    renderChartResolvers.push(resolve)
    nextTick(() => debounceRender())
  })
}

const debounceRender = debounce(() => {
  try {
    if (chartComponentUnmounted) {
      return
    }
    myChart?.facet?.timer?.stop()
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
    myChart?.render()
    dvMainStore.setViewInstanceInfo(actualChart.id, myChart)
    initScroll()
  } finally {
    resolveRenderChart()
  }
}, 500)

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

const mouseMove = () => {
  if (scrollTimer) {
    clearTimeout(scrollTimer)
  }
  myChart?.facet?.timer?.stop()
}

const mouseLeave = () => {
  initScroll()
}

let scrollTimer: ReturnType<typeof setTimeout> | null = null
const initScroll = () => {
  if (scrollTimer) {
    clearTimeout(scrollTimer)
  }
  scrollTimer = setTimeout(() => {
    const senior = actualChart?.senior
    if (
      myChart &&
      senior?.scrollCfg?.open &&
      chartData.value.tableRow?.length &&
      PAGE_CHARTS.includes(props.view.type) &&
      !state.showPage
    ) {
      // 停止可能正在执行的滚动动画
      myChart.facet?.timer?.stop()

      const containerDom = document.getElementById(containerId)
      if (!containerDom || !containerDom.offsetHeight) {
        return
      }

      // 获取 S2 真实的最大可滚动垂直偏移量
      const maxScrollY =
        (myChart.facet as any)?.getAdjustedScrollOffset?.({ scrollY: 99999999 })?.scrollY ?? 0

      // 内容未超出视口，无需滚动
      if (maxScrollY <= 0) {
        return
      }

      // 当前已滚动的距离
      let scrolledOffset =
        (myChart.facet as any)?.getScrollOffset?.()?.scrollY ?? myChart.store.get('scrollY') ?? 0

      // 触底判断（允许 2px 容差）：如果已到底部，重置回到顶部并等待下一次循环
      if (scrolledOffset >= maxScrollY - 2) {
        myChart.facet?.scrollImmediately?.({ offsetY: { value: 0 } })
        myChart.store.set('scrollY', 0)
        // 回到顶部后，延时开启下一次从头滚动
        initScroll()
        return
      }

      // 计算剩余需要滚动的距离
      const remainingOffset = maxScrollY - scrolledOffset
      // 计算每行平均高度以估算剩余行数和滚动时长
      const totalRendererHeight = (myChart.facet as any)?.getRendererHeight?.() || maxScrollY
      const totalRowCount = chartData.value.tableRow.length
      const avgRowHeight = totalRowCount > 0 ? totalRendererHeight / totalRowCount : 36
      const remainRows = remainingOffset / avgRowHeight

      const scrollRow = senior.scrollCfg.row || 1
      const scrollInterval = senior.scrollCfg.interval || 2000
      const duration = Math.max(200, (remainRows / scrollRow) * scrollInterval)

      // 平滑滚动到底部，并在滚动结束时回调触发 initScroll 进行触底重置
      myChart.facet.scrollWithAnimation(
        { offsetY: { value: maxScrollY, animate: false } },
        duration,
        initScroll
      )
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

const touchAction = callback => {
  if (!trackMenu.value.length) {
    callback?.()
  }
}

const action = param => {
  state.pointParam = param
  state.curActionId = param.data.name
  state.curTrackMenu = trackMenuCalc(state.curActionId)
  // 点击
  pointClickTrans()
  // 下钻 联动 跳转
  if (trackMenu.value.length < 2) {
    if (view.value.drillFields.length > 0 && trackMenu.value.length === 0) {
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
    let pointX = param.x
    let pointY = param.y
    if (!isDashboard() && showPosition.value === 'canvas') {
      pointX = pointX / dvMainStore.canvasStyleData.tScale
      pointY = pointY / dvMainStore.canvasStyleData.tScale
    }
    // 图表关联多个事件
    const barStyleTemp = {
      left: pointX - 50,
      top: pointY + 10
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
const appStore = useAppStoreWithOut()

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
  if (view.value.drillFields.length && view.value.drillFields[drillLength.value].id === itemId) {
    drillCount++
  }
  drillCount && trackMenuInfo.push('drill')
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
  if (myChart?.facet.timer) {
    myChart?.facet.timer.stop()
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
    timer = null
  }
  // 全屏切换会将原编辑画布短暂隐藏，零尺寸不能写入 S2 实例
  if (chartComponentUnmounted || width <= 1 || height <= 1) {
    return
  }
  timer = setTimeout(() => {
    timer = null
    if (chartComponentUnmounted) {
      return
    }
    if (!myChart?.facet) {
      debounceRender()
    } else {
      myChart?.facet?.timer?.stop()
      myChart?.changeSheetSize(width, height)
      myChart?.render()
      dvMainStore.setViewInstanceInfo(actualChart.id, myChart)
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
  try {
    chartComponentUnmounted = true
    timer && clearTimeout(timer)
    scrollTimer && clearTimeout(scrollTimer)
    debounceRender.cancel()
    resolveRenderChart()
    myChart?.facet?.timer?.stop()
    myChart?.facet?.cancelScrollFrame()
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

const autoHeightStyle = computed(() => {
  return {
    height: 20 * scale.value + 8 + 'px'
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
        <div style="white-space: nowrap">共{{ state.pageInfo.total }}条</div>
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
  .show-close {
    padding-bottom: 24px;
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
