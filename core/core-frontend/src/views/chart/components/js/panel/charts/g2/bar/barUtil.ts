import {
  handleBreakLineMultiDimension,
  handleIgnoreData,
  handleSetZeroMultiDimension,
  handleSetZeroSingleDimension,
  parseJson
} from '@/views/chart/components/js/util'
import { Chart as G2Chart } from '@antv/g2'
import { defaultsDeep } from 'lodash-es'

/**
 * 运行时形态与 G2Spec 完全一致 ，G2 以普通对象消费
 */
export interface ChildSpec {
  axis?: Record<string, any>
  encode?: Record<string, any>
  scale?: Record<string, any>
  style?: Record<string, any>
  transform?: Array<Record<string, any>>
  labels?: any[]
  tooltip?: any
  interaction?: Record<string, any>
  data?: any
  [key: string]: any
}

export interface ViewSpec {
  type?: string
  children?: ChildSpec[]
  data?: any
  scale?: Record<string, any>
  theme?: Record<string, any>
  coordinate?: Record<string, any>
  title?: any
  legend?: Record<string, any>
  tooltip?: any
  interaction?: Record<string, any>
  annotations?: any[]
  [key: string]: any
}

export type Transform = {
  type: string
  [key: string]: any
}

type PlotBackgroundClickOptions = {
  axis?: 'x' | 'y'
  markTypes?: string[]
}

export function getThemeContrastColor(chart: Chart) {
  const customAttr = parseJson(chart.customAttr)
  return customAttr?.basicStyle?.themeContrastColor ?? customAttr?.label?.color ?? '#000000'
}

export function getThemeSelectedState(chart: Chart, state: Record<string, any> = {}) {
  // 只调整选中态描边，保留现有 active 等交互状态
  return defaultsDeep({}, state, {
    selected: { stroke: getThemeContrastColor(chart), lineWidth: 1 }
  })
}

export function getBackgroundInteractionState(chart?: Chart) {
  const state = {
    active: { backgroundPointerEvents: 'none' },
    selected: { backgroundPointerEvents: 'none' }
  }
  return chart ? getThemeSelectedState(chart, state) : state
}

export function bindPlotBackgroundClick(
  chart: G2Chart,
  { axis = 'x', markTypes = ['interval'] }: PlotBackgroundClickOptions = {}
) {
  chart.on('plot:click', event => {
    if (event.target?.className !== 'plot') {
      return
    }
    let view = event.target
    while (view && view.className !== 'view') {
      view = view.parentNode
    }
    const viewKey = view?.id ?? view?.attributes?.id
    const pointer = axis === 'x' ? event.x : event.y
    if (!Number.isFinite(pointer)) {
      return
    }
    const axisIndex = axis === 'x' ? 0 : 1
    const elements = Array.from(
      chart.getContext()?.canvas?.document?.getElementsByClassName?.('element') || []
    ) as any[]
    // 按当前子视图和分类轴距离选择真实图形，兼容对称图的多视图结构
    const nearest = elements.reduce((result, element) => {
      if (
        element.__removed__ ||
        !markTypes.includes(element.markType) ||
        (viewKey && element.__data__?.viewKey !== viewKey)
      ) {
        return result
      }
      const bounds = element.getRenderBounds?.()
      const start = bounds?.min?.[axisIndex]
      const end = bounds?.max?.[axisIndex]
      if (!Number.isFinite(start) || !Number.isFinite(end)) {
        return result
      }
      const distance = pointer < start ? start - pointer : pointer > end ? pointer - end : 0
      return !result || distance < result.distance ? { element, distance } : result
    }, undefined)
    const datum = nearest?.element?.__data__?.data
    if (!datum) {
      return
    }
    // 复用实际 mark 的点击事件，统一进入联动、下钻和跳转流程
    chart.emit(`${nearest.element.markType}:click`, {
      ...event,
      data: { data: { ...datum } }
    })
  })
}

// 仅在横向柱形标签与可见分类轴同侧时向绘图区内收
export function getHorizontalBarAxisSafeLabelStyle(chart: Chart, position: string, offset = 4) {
  const categoryAxis = parseJson(chart.customStyle)?.yAxis
  if (!categoryAxis?.show || categoryAxis.axisLabel?.show === false) {
    return {}
  }
  const labelPosition = position === 'middle' ? 'inside' : position
  const axisPosition = categoryAxis.position === 'right' ? 'right' : 'left'
  if (labelPosition !== axisPosition) {
    return {}
  }
  return labelPosition === 'left'
    ? { textAlign: 'start', dx: offset }
    : { textAlign: 'end', dx: -offset }
}

// 保留堆叠系列原始类型，避免数值或布尔分组在 color domain 中被误转
type StackSeriesValue = string | number | boolean

// 生成堆叠系列的字段顺序，供颜色、堆叠层级和 tooltip 复用
export function getStackSeriesOrder(chart: Chart, data?: any[]): StackSeriesValue[] {
  const order: StackSeriesValue[] = []
  const orderKeys = new Set<string>()
  const dataSeriesMap = getStackDataSeriesMap(data)
  const append = (item: any) => {
    if (item === null || item === undefined) {
      return
    }
    const key = getStackSeriesKey(item)
    if (orderKeys.has(key)) {
      return
    }
    orderKeys.add(key)
    order.push(dataSeriesMap.get(key) ?? item)
  }

  if (!chart.extStack?.length) {
    chart.yAxis?.forEach(item => append(item.chartShowName ?? item.name))
    return order
  }

  const stackField = chart.extStack[0]
  // 字段自定义排序优先，升序和降序沿用后端返回的数据顺序
  if (stackField.sort === 'custom_sort' && stackField.customSort?.length) {
    stackField.customSort.forEach(append)
  }

  // 数据顺序承载字段排序结果，颜色配置不能反向主导字段顺序
  if (Array.isArray(data)) {
    data.forEach(item => append(item.category))
  }
  const { basicStyle } = parseJson(chart.customAttr)
  // 仅补齐数据中未出现但已有颜色配置的系列，避免颜色项丢失
  basicStyle.seriesColor?.forEach(item => append(item.id))
  return order
}

// 将统一顺序转成索引表，降低排序时的重复查找成本
export function getStackSeriesIndexMap(seriesOrder: StackSeriesValue[]): Record<string, number> {
  return seriesOrder.reduce((pre, next, index) => {
    pre[getStackSeriesKey(next)] = index
    return pre
  }, {} as Record<string, number>)
}

export function sortStackTooltipItems<T extends { name: any; category?: any }>(
  items: T[],
  seriesOrder: StackSeriesValue[],
  seriesIndexMap: Record<string, number>
) {
  // tooltip 展示顺序与字段顺序和堆叠层级保持一致
  items.sort((a, b) => {
    const aSeries =
      a.category === null || a.category === undefined || a.category === '' ? a.name : a.category
    const bSeries =
      b.category === null || b.category === undefined || b.category === '' ? b.name : b.category
    return (
      (seriesIndexMap[getStackSeriesKey(aSeries)] ?? seriesOrder.length) -
      (seriesIndexMap[getStackSeriesKey(bSeries)] ?? seriesOrder.length)
    )
  })
}

// 用字符串键做去重，兼容不同来源的系列值
function getStackSeriesKey(series: any) {
  return `${series}`
}

// 缓存数据中的 category 原始值，保证 domain 使用真实类型
function getStackDataSeriesMap(data?: any[]) {
  const dataSeriesMap = new Map<string, StackSeriesValue>()
  if (!Array.isArray(data)) {
    return dataSeriesMap
  }
  data.forEach(item => {
    const category = item?.category
    if (category === null || category === undefined) {
      return
    }
    const key = getStackSeriesKey(category)
    if (!dataSeriesMap.has(key)) {
      dataSeriesMap.set(key, category)
    }
  })
  return dataSeriesMap
}

// 未命中的系列排在末尾，避免新增数据打乱已配置顺序
function getStackSeriesOrderRank(
  series: any,
  seriesOrder: StackSeriesValue[],
  seriesIndexMap: Record<string, number>
) {
  const index = seriesIndexMap[getStackSeriesKey(series)]
  if (index === undefined) {
    return seriesOrder.length
  }
  return index
}

export function configStackSeriesOrder(chart: Chart, options: ViewSpec): ViewSpec {
  const { children } = options
  const seriesOrder = getStackSeriesOrder(chart, children[0]?.data || options.data)
  if (!seriesOrder.length) {
    return options
  }
  const seriesIndexMap = getStackSeriesIndexMap(seriesOrder)

  // 同一字段顺序同时约束颜色、图例和堆叠层级，保持柱状图自上而下配色
  defaultsDeep(options, {
    scale: {
      color: {
        domain: seriesOrder
      }
    }
  })
  return {
    ...options,
    children: [
      {
        ...children[0],
        transform: children[0].transform.map(transform => {
          if (transform.type !== 'stackY') {
            return transform
          }
          return {
            ...transform,
            orderBy: (item: any) =>
              getStackSeriesOrderRank(item.category, seriesOrder, seriesIndexMap)
          }
        })
      },
      ...children.slice(1)
    ]
  }
}

export function handleEmptyDataStrategy<O extends ViewSpec>(chart: Chart, options: O): O {
  const childData = options.children?.[0]?.data
  const rootData = (options as any).data
  const data = childData ?? rootData
  if (!data?.length) return options
  if (!data?.length) {
    return options
  }
  const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
  if (strategy === 'ignoreData') {
    handleIgnoreData(data)
    return options
  }
  const { yAxis, xAxisExt, extStack } = chart
  const multiDimension = yAxis?.length >= 2 || xAxisExt?.length > 0 || extStack?.length > 0
  switch (strategy) {
    case 'breakLine': {
      if (multiDimension) {
        handleBreakLineMultiDimension(data)
      }
      break
    }
    case 'setZero': {
      if (multiDimension) {
        // 多维度置0
        handleSetZeroMultiDimension(data)
      } else {
        // 单维度置0
        handleSetZeroSingleDimension(data)
      }
      break
    }
  }
  return options
}

const BAR_BREAK_LINE_RENDER_VALUE = '__DE_BAR_BREAK_LINE_RENDER_VALUE__'

export function handleBarBreakLineNullData<O extends ViewSpec>(chart: Chart, options: O): O {
  const strategy = parseJson(chart.senior).functionCfg.emptyDataStrategy
  if (strategy !== 'breakLine') return options

  const child = options.children?.[0]
  if (!child) return options
  const childData = child.data
  const rootData = (options as any).data
  const data = childData ?? rootData
  if (!Array.isArray(data) || !data.some(item => item?.value === null)) return options

  const transforms = child?.transform ?? []
  const stack = transforms.some(transform => transform.type === 'stackY')
  if (transforms.some(transform => transform.type === 'normalizeY')) {
    const filteredData = data.filter(item => item?.value !== null)
    if (childData) {
      child.data = filteredData
    } else {
      ;(options as any).data = filteredData
    }
    return options
  }
  if (stack) {
    const nonEmptyFields = new Set(
      data.filter(item => item?.value !== null).map(item => item?.field)
    )
    // 堆叠维持原 value 通道，仅保留全空维度的 null 占位供坐标轴和 tooltip 使用
    const filteredData = data.filter(
      item => item?.value !== null || !nonEmptyFields.has(item?.field)
    )
    if (childData) {
      child.data = filteredData
    } else {
      ;(options as any).data = filteredData
    }
    return options
  }

  if (child?.type !== 'interval' || child.encode?.y !== 'value') return options

  // 用独立的零高度编码保留空维度，原始 null 继续供 tooltip 和业务逻辑使用
  data.forEach(item => (item[BAR_BREAK_LINE_RENDER_VALUE] = item.value ?? 0))
  child.encode = { ...child.encode, y: BAR_BREAK_LINE_RENDER_VALUE }
  return options
}

export function tooltipWrapperId(container: string) {
  return 'G2-TOOLTIP-WRAPPER-' + container
}

export type TooltipDisplayMode = 'hover' | 'carousel'

export const TOOLTIP_HOVER_LEAVE_EVENT = 'de-tooltip-hover-leave'

export function getTooltipWrapper(container: string): HTMLElement | null {
  return document.getElementById(tooltipWrapperId(container))
}

export function getTooltipDisplayMode(container: string): TooltipDisplayMode {
  return getTooltipWrapper(container)?.dataset.tooltipDisplayMode === 'carousel'
    ? 'carousel'
    : 'hover'
}

export function switchTooltipWrapperHost(chart: Chart, mode: TooltipDisplayMode) {
  const wrapper = getTooltipWrapper(chart.container)
  const chartContainer = document.getElementById(chart.container)
  const host = mode === 'hover' ? document.body : chartContainer
  if (!wrapper || !host) return

  const shouldResetTooltip =
    wrapper.dataset.tooltipDisplayMode !== mode || wrapper.parentElement !== host
  if (shouldResetTooltip) {
    // 挂载点切换时先隐藏旧内容，等待新的 tooltip:show 再恢复
    wrapper.querySelectorAll<HTMLElement>('.g2-tooltip').forEach(tooltip => {
      resetHoverTooltipPosition(tooltip)
      tooltip.style.visibility = 'hidden'
      tooltip.style.removeProperty('left')
      tooltip.style.removeProperty('top')
    })
  }

  // 复用同一个 G2 tooltip 节点，按触发来源切换挂载位置
  wrapper.dataset.tooltipDisplayMode = mode
  if (wrapper.parentElement !== host) {
    host.appendChild(wrapper)
  }

  Object.assign(wrapper.style, {
    position: mode === 'hover' ? 'fixed' : 'absolute',
    inset: mode === 'hover' ? '0 auto auto 0' : '0',
    width: mode === 'hover' ? '0px' : '100%',
    height: mode === 'hover' ? '0px' : '100%',
    overflow: 'visible',
    pointerEvents: 'none',
    zIndex: chart.container.includes('viewDialog') ? '9999' : '2000'
  })
  if (chart.fontFamily && chart.fontFamily !== 'inherit') {
    wrapper.style.fontFamily = chart.fontFamily
  } else {
    wrapper.style.removeProperty('font-family')
  }
}

export function createTooltipWrapper(chart: Chart) {
  const wrapperId = tooltipWrapperId(chart.container)
  let g2TooltipWrapper = document.getElementById(wrapperId)
  if (!g2TooltipWrapper) {
    g2TooltipWrapper = document.createElement('div')
    g2TooltipWrapper.id = wrapperId
    g2TooltipWrapper.style.pointerEvents = 'none'
    document.body.appendChild(g2TooltipWrapper)
  }
  switchTooltipWrapperHost(chart, 'hover')
  return g2TooltipWrapper
}

export function tooltipCss(tooltipAttr: DeepPartial<ChartTooltipAttr>) {
  return {
    '.g2-tooltip': {
      background: tooltipAttr.backgroundColor,
      'max-height': '50vh',
      'overflow-y': 'auto',
      position: 'fixed',
      'scrollbar-width': tooltipAttr.carousel.enable ? 'none !important' : 'auto'
    },
    '.g2-tooltip-title': {
      color: tooltipAttr.color,
      'font-size': `${tooltipAttr.fontSize}px`
    },
    '.g2-tooltip-list-item-name-label': {
      color: tooltipAttr.color,
      'font-size': `${tooltipAttr.fontSize}px`
    },
    '.g2-tooltip-list-item-value': {
      color: tooltipAttr.color,
      'font-size': `${tooltipAttr.fontSize}px`
    }
  }
}

/**
 * 计算 tooltip 最大高度
 * @param chart
 */
export function tooltipMaxHeight(chart: Chart) {
  const chartContainer = document.getElementById(chart.container)
  const defaultHeight = 80
  const chartRect = chartContainer?.getBoundingClientRect()
  let doubleHeight = chartRect.height * 2 - 20
  const customAttr = parseJson(chart.customAttr)
  if (customAttr?.tooltip?.carousel?.enable) {
    doubleHeight = chartRect.height / 1.2 - 20
  }
  const maxHeight = chartContainer ? Math.max(doubleHeight, defaultHeight) : defaultHeight
  return `max-height: ${maxHeight}px;max-width: ${chartRect.width / 2}px;`
}

export function getSeriesTooltipFormatterMap(tooltipAttr?: DeepPartial<ChartTooltipAttr>) {
  return (tooltipAttr?.seriesTooltipFormatter || []).reduce((pre, next) => {
    if (!next?.id) {
      return pre
    }
    const formatter = next as SeriesFormatter
    // 同一字段可出现在不同指标槽位，优先用 seriesId 区分具体槽位。
    if (formatter.seriesId) {
      pre[formatter.seriesId] = formatter
    }
    if (!formatter.seriesId || formatter.seriesId === formatter.id) {
      pre[formatter.id] = formatter
    }
    return pre
  }, {} as Record<string, SeriesFormatter>)
}

export function getTooltipItemFieldId(item?: any) {
  return item?.quotaList?.[0]?.id ?? item?.data?.quotaList?.[0]?.id ?? item?.fieldId
}

// 旧图表样式可能带着过期 formatter；当前字段缺少配置时按默认展示处理。
export function isSeriesTooltipFormatterShown(
  formatterMap: Record<string, SeriesFormatter>,
  fieldId?: string,
  axisType?: AxisType
) {
  if (!fieldId) {
    return true
  }
  if (axisType) {
    // 带槽位的主指标不能被旧的 id 级 show:false 误隐藏。
    const seriesKey = `${fieldId}-${axisType}`
    return Object.prototype.hasOwnProperty.call(formatterMap, seriesKey)
      ? formatterMap[seriesKey]?.show !== false
      : true
  }
  if (Object.prototype.hasOwnProperty.call(formatterMap, fieldId)) {
    return formatterMap[fieldId]?.show !== false
  }
  return true
}

export function isTooltipItemShown(
  formatterMap: Record<string, SeriesFormatter>,
  item?: any,
  axisType?: AxisType
) {
  return isSeriesTooltipFormatterShown(formatterMap, getTooltipItemFieldId(item), axisType)
}

// 带槽位的 formatter 缺项时回退到当前轴字段，避免旧 id 级配置污染新指标。
export function getSeriesTooltipFormatter(
  formatterMap: Record<string, SeriesFormatter>,
  fieldId?: string,
  fields: Partial<Axis>[] = [],
  axisType?: AxisType
) {
  const field = fields.find(field => field?.id === fieldId)
  const seriesId = (field as Partial<SeriesFormatter>)?.seriesId
  if (axisType && fieldId) {
    const seriesKey = `${fieldId}-${axisType}`
    return formatterMap[seriesKey] || (seriesId && formatterMap[seriesId]) || field
  }
  return (fieldId && formatterMap[fieldId]) || (seriesId && formatterMap[seriesId]) || field
}

export function getTooltipItemFormatter(
  formatterMap: Record<string, SeriesFormatter>,
  item: any,
  fields: Partial<Axis>[] = [],
  axisType?: AxisType
) {
  return getSeriesTooltipFormatter(formatterMap, getTooltipItemFieldId(item), fields, axisType)
}

// 将字段显示名降级到原始字段名，避免 tooltip 分组标题为空
export function getFieldDisplayName(field?: Partial<ChartViewField>) {
  return field?.chartShowName || field?.name || ''
}

function getFieldDisplayNames(fields?: Partial<ChartViewField>[]) {
  return fields?.map(getFieldDisplayName).filter(Boolean).join(' / ') || ''
}

// 多维字段按实际命中的维度输出分组标题
function getDimensionGroupName(fields: Partial<ChartViewField>[], item?: any) {
  if (!fields?.length) {
    return ''
  }
  if (item?.dimensionList?.length) {
    const itemFieldIds = item.dimensionList.reduce((pre, next) => {
      pre[`${next.id}`] = true
      return pre
    }, {} as Record<string, boolean>)
    const itemFields = fields.filter(field => itemFieldIds[`${field.id}`])
    return getFieldDisplayNames(itemFields)
  }
  return getFieldDisplayNames(fields)
}

// 堆叠图使用堆叠字段和扩展维度作为 tooltip 分组
export function getStackTooltipGroupName(chart: Chart, item?: any) {
  return getDimensionGroupName([...(chart.extStack || []), ...(chart.xAxisExt || [])], item)
}

// 混合图左右轴维度来源不同，先确定当前 tooltip 项所属分组字段
function getMixTooltipGroupFields(chart: Chart, item: any) {
  const leftFields = [...(chart.xAxisExt || []), ...(chart.extStack || [])]
  const rightFields = chart.extBubble || []
  if (item?.left) {
    return leftFields
  }
  if (rightFields.length) {
    return rightFields
  }
  return leftFields
}

export function getMixTooltipGroupName(chart: Chart, item: any) {
  return getDimensionGroupName(getMixTooltipGroupFields(chart, item), item)
}

// 混合图右轴分组索引接在左轴后面，确保分组排序稳定
export function getMixTooltipGroupIndex(chart: Chart, item: any) {
  const leftFields = [...(chart.xAxisExt || []), ...(chart.extStack || [])]
  const rightFields = chart.extBubble || []
  const fields = getMixTooltipGroupFields(chart, item)
  const groupIndexOffset = item?.left || !rightFields.length ? 0 : leftFields.length
  const groupFieldIndexMap = fields
    .filter(field => field?.id)
    .reduce((pre, next, index) => {
      pre[`${next.id}`] = groupIndexOffset + index
      return pre
    }, {} as Record<string, number>)
  const indexes = item?.dimensionList
    ?.map(dimension => groupFieldIndexMap[`${dimension.id}`])
    .filter(index => index !== undefined)
  if (indexes?.length) {
    return Math.min(...indexes)
  }
  return fields.length ? groupIndexOffset : -1
}

// 分组标题复用 tooltip 字体样式，避免额外主题适配
export function renderTooltipGroupTitle(title: string, first = false) {
  return `<li class="g2-tooltip-list-group-title g2-tooltip-list-item-name-label" style="list-style-type: none; line-height: 2em; font-weight: 600; opacity: 0.85; margin: ${
    first ? 0 : 4
  }px 0 0; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">${title}</li>`
}

// 相邻分组变化时才插入标题，避免同组明细重复显示标题
export function renderGroupedTooltipItems<T>(
  items: T[],
  getGroupName: (item: T) => string,
  renderItem: (item: T) => string
) {
  let currentGroup = ''
  return items
    .map((item, index) => {
      const groupName = getGroupName(item)
      const groupTitle =
        groupName && groupName !== currentGroup
          ? renderTooltipGroupTitle(groupName, index === 0)
          : ''
      if (groupName) {
        currentGroup = groupName
      }
      return `${groupTitle}${renderItem(item)}`
    })
    .join('')
}

// 从 G2 color relations 提取图例顺序，供混合图 tooltip 排序
export function getSeriesIndexMapByRelations(relations?: any[]) {
  return (relations || []).reduce((pre, next, index) => {
    const seriesName = Array.isArray(next) ? next[0] : next
    pre[`${seriesName}`] = index
    return pre
  }, {} as Record<string, number>)
}

// 混合图 tooltip 先按维度分组，再按图例系列顺序排列
export function sortMixTooltipItems<T extends { groupIndex: number; name: any }>(
  items: T[],
  seriesIndexMap: Record<string, number>
) {
  const fallbackIndex = Object.keys(seriesIndexMap).length
  items.sort((pre, next) => {
    const groupDiff = pre.groupIndex - next.groupIndex
    if (groupDiff !== 0) {
      return groupDiff
    }
    return (
      (seriesIndexMap[`${pre.name}`] ?? fallbackIndex) -
      (seriesIndexMap[`${next.name}`] ?? fallbackIndex)
    )
  })
}

const CAROUSEL_TOOLTIP_GAP = 8
const G2_TOOLTIP_DEFAULT_MIN_WIDTH = 120
const G2_TOOLTIP_DEFAULT_MAX_WIDTH = 320

type CarouselTooltipFrameState = {
  fitFrame?: number
  hoverReadyFrame?: number
}

const carouselTooltipFrameState = new WeakMap<HTMLElement, CarouselTooltipFrameState>()

function resetHoverTooltipPosition(tooltip: HTMLElement) {
  const frameState = carouselTooltipFrameState.get(tooltip)
  if (frameState?.hoverReadyFrame !== undefined) {
    window.cancelAnimationFrame(frameState.hoverReadyFrame)
    frameState.hoverReadyFrame = undefined
  }
  tooltip.removeAttribute('data-de-tooltip-position-ready')
  tooltip.removeAttribute('data-de-tooltip-position-pending')
}

function markHoverTooltipPositionReady(
  container: string,
  tooltipWrapper: HTMLElement,
  tooltip: HTMLElement
) {
  if (
    tooltip.dataset.deTooltipPositionReady === 'true' ||
    tooltip.dataset.deTooltipPositionPending === 'true'
  ) {
    return
  }
  const frameState = carouselTooltipFrameState.get(tooltip) || {}
  carouselTooltipFrameState.set(tooltip, frameState)
  tooltip.dataset.deTooltipPositionPending = 'true'
  // 先绘制一次最终坐标，再恢复 AntV 后续 left/top 平滑过渡
  frameState.hoverReadyFrame = window.requestAnimationFrame(() => {
    frameState.hoverReadyFrame = window.requestAnimationFrame(() => {
      frameState.hoverReadyFrame = undefined
      tooltip.removeAttribute('data-de-tooltip-position-pending')
      if (
        tooltip.isConnected &&
        tooltipWrapper.contains(tooltip) &&
        getTooltipDisplayMode(container) === 'hover'
      ) {
        tooltip.dataset.deTooltipPositionReady = 'true'
      }
    })
  })
}

function fitCarouselTooltipInChart(
  container: string,
  tooltipWrapper: HTMLElement,
  tooltip: HTMLElement
) {
  const frameState = carouselTooltipFrameState.get(tooltip) || {}
  if (frameState.fitFrame !== undefined) {
    window.cancelAnimationFrame(frameState.fitFrame)
    frameState.fitFrame = undefined
  }
  carouselTooltipFrameState.set(tooltip, frameState)

  // 等待 G2 完成内容和原始位置计算后再进行边界修正
  frameState.fitFrame = window.requestAnimationFrame(() => {
    frameState.fitFrame = undefined
    if (
      !tooltip.isConnected ||
      !tooltipWrapper.contains(tooltip) ||
      getTooltipDisplayMode(container) !== 'carousel'
    ) {
      return
    }

    const wrapperWidth = tooltipWrapper.clientWidth
    const wrapperHeight = tooltipWrapper.clientHeight
    if (wrapperWidth <= CAROUSEL_TOOLTIP_GAP * 2 || wrapperHeight <= CAROUSEL_TOOLTIP_GAP * 2) {
      return
    }

    // 轮播沿用悬浮 tooltip 的宽度上限，同时不能超出图表容器
    const maxWidth = Math.min(G2_TOOLTIP_DEFAULT_MAX_WIDTH, wrapperWidth - CAROUSEL_TOOLTIP_GAP * 2)
    const maxHeight = wrapperHeight - CAROUSEL_TOOLTIP_GAP * 2
    const minWidth = Math.min(G2_TOOLTIP_DEFAULT_MIN_WIDTH, maxWidth)
    tooltipWrapper.style.setProperty('--de-carousel-tooltip-min-width', `${minWidth}px`)
    tooltipWrapper.style.setProperty('--de-carousel-tooltip-max-width', `${maxWidth}px`)
    tooltipWrapper.style.setProperty('--de-carousel-tooltip-max-height', `${maxHeight}px`)

    const tooltipWidth = tooltip.offsetWidth
    const tooltipHeight = tooltip.offsetHeight
    const originalLeft = Number.parseFloat(tooltip.style.left)
    const originalTop = Number.parseFloat(tooltip.style.top)
    const preferredLeft = Number.isFinite(originalLeft) ? originalLeft : tooltip.offsetLeft
    const preferredTop = Number.isFinite(originalTop) ? originalTop : tooltip.offsetTop
    const maxLeft = Math.max(
      CAROUSEL_TOOLTIP_GAP,
      wrapperWidth - tooltipWidth - CAROUSEL_TOOLTIP_GAP
    )
    const maxTop = Math.max(
      CAROUSEL_TOOLTIP_GAP,
      wrapperHeight - tooltipHeight - CAROUSEL_TOOLTIP_GAP
    )
    const left = Math.max(CAROUSEL_TOOLTIP_GAP, Math.min(preferredLeft, maxLeft))
    const top = Math.max(CAROUSEL_TOOLTIP_GAP, Math.min(preferredTop, maxTop))

    // 复用同一 tooltip 节点，让轮播坐标更新自然过渡
    tooltip.style.left = `${left}px`
    tooltip.style.top = `${top}px`
    tooltip.style.visibility = 'visible'
  })
}

function syncHoverTooltipEllipsisTitles(tooltip: HTMLElement) {
  // 只管理公共模板的错误 title 和本方法生成的 title
  tooltip
    .querySelectorAll<HTMLElement>('.g2-tooltip-list-item-name-label, .g2-tooltip-list-item-value')
    .forEach(element => {
      const managed = element.dataset.deEllipsisTitle === 'true'
      const canManage = !element.title || element.title === 'value' || managed
      if (!canManage) return

      const text = element.textContent?.trim() || ''
      const isOverflowing = element.scrollWidth > element.clientWidth + 1
      if (text && isOverflowing) {
        element.title = text
        element.dataset.deEllipsisTitle = 'true'
      } else {
        element.removeAttribute('title')
        element.removeAttribute('data-de-ellipsis-title')
      }
    })
}

export function listenerTooltipShow(newChart: G2Chart, chart: Chart) {
  newChart.on('tooltip:show', event => {
    const tooltipWrapper = getTooltipWrapper(chart.container)
    if (!tooltipWrapper) return

    const isCarousel = getTooltipDisplayMode(chart.container) === 'carousel'
    tooltipWrapper.style.zIndex = chart.container.indexOf('viewDialog') > -1 ? '9999' : '2000'
    const allTooltips = tooltipWrapper.querySelectorAll<HTMLElement>('.g2-tooltip')
    if (!allTooltips) return
    allTooltips.forEach(item => {
      const tooltip = item
      if (tooltip.dataset.deTooltipLeaveBound !== 'true') {
        tooltip.dataset.deTooltipLeaveBound = 'true'
        tooltip.addEventListener('mouseleave', () => {
          tooltip.style.visibility = 'hidden'
          tooltipWrapper.dispatchEvent(new CustomEvent(TOOLTIP_HOVER_LEAVE_EVENT))
        })
      }
      tooltip.style.setProperty('position', isCarousel ? 'absolute' : 'fixed', 'important')
      if (isCarousel) {
        fitCarouselTooltipInChart(chart.container, tooltipWrapper, tooltip)
        return
      }

      const clientX = event?.client?.x
      const clientY = event?.client?.y
      if (clientX == null || clientY == null) {
        tooltip.style.visibility = 'hidden'
        return
      }

      const gap = 20
      const { width, height } = tooltip.getBoundingClientRect()
      const viewportWidth = document.documentElement.clientWidth
      const viewportHeight = document.documentElement.clientHeight
      let left = clientX + gap
      let top = clientY - height - gap
      if (left + width > viewportWidth) {
        left = clientX - width - gap
      }
      if (top < 0) {
        top = clientY + gap
      }
      tooltip.style.left = `${Math.max(0, Math.min(left, viewportWidth - width))}px`
      tooltip.style.top = `${Math.max(0, Math.min(top, viewportHeight - height))}px`
      tooltip.style.visibility = 'visible'
      markHoverTooltipPositionReady(chart.container, tooltipWrapper, tooltip)
      syncHoverTooltipEllipsisTitles(tooltip)
    })
  })
}
