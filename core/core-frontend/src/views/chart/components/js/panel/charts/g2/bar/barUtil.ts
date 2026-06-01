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

export function tooltipWrapperId(container: string) {
  return 'G2-TOOLTIP-WRAPPER-' + container
}

export function createTooltipWrapper(chart: Chart) {
  const wrapperId = tooltipWrapperId(chart.container)
  let g2TooltipWrapper = document.getElementById(wrapperId)
  if (!g2TooltipWrapper) {
    g2TooltipWrapper = document.createElement('div')
    g2TooltipWrapper.id = wrapperId
    g2TooltipWrapper.style.position = 'absolute'
    g2TooltipWrapper.style.pointerEvents = 'none'
    g2TooltipWrapper.style.zIndex = '2000'
    g2TooltipWrapper.style.top = '0px'
    document.body.appendChild(g2TooltipWrapper)
  }
  // 如果开启轮播则不使用自定义tooltip容器
  const customAttr = parseJson(chart.customAttr)
  return customAttr?.tooltip?.carousel?.enable ? undefined : g2TooltipWrapper
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

export function listenerTooltipShow(newChart: G2Chart, chart: Chart) {
  newChart.on('tooltip:show', event => {
    const customAttr = parseJson(chart.customAttr)
    const isCarousel = customAttr?.tooltip?.carousel?.enable
    const tooltipWrapper = isCarousel
      ? document.getElementById(chart.container)
      : document.getElementById(tooltipWrapperId(chart.container))
    if (!tooltipWrapper) return
    tooltipWrapper.style.zIndex = chart.container.indexOf('viewDialog') > -1 ? '9999' : '2000'
    const allTooltips = tooltipWrapper?.querySelectorAll('.g2-tooltip')
    if (!allTooltips) return
    allTooltips.forEach(item => {
      const tooltip = item as HTMLElement
      const tooltipMouseleave = () => {
        tooltip.style.visibility = 'hidden'
      }
      tooltip.removeEventListener('mouseleave', tooltipMouseleave)
      tooltip.addEventListener('mouseleave', tooltipMouseleave)
      if (isCarousel) {
        tooltip.style.top = '0px'
      } else {
        const clientY = event?.client?.y
        if (!clientY) return
        if (clientY < tooltip.getBoundingClientRect().height) {
          tooltip.style.top = '0px'
        } else {
          tooltip.style.top = `${clientY - tooltip.getBoundingClientRect().height - 20}px`
        }
        const clientX = event.client?.x
        const targetDiv = document.getElementById(chart.container)
        if (!targetDiv || clientX == null) return

        const tooltipWidth = tooltip.getBoundingClientRect().width
        const left = clientX

        tooltip.style.left =
          left + tooltipWidth > targetDiv.getBoundingClientRect().right
            ? `${clientX - tooltipWidth - 20}px`
            : `${left + 20}px`
      }
    })
  })
}
