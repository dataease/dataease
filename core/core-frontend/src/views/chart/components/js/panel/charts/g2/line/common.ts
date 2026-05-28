import type { G2Spec } from '@antv/g2'

export const LINE_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'border-style',
  'basic-style-selector',
  'x-axis-selector',
  'y-axis-selector',
  'title-selector',
  'legend-selector',
  'label-selector',
  'tooltip-selector',
  'assist-line',
  'function-cfg',
  'jump-set',
  'linkage',
  'threshold'
]
export const LINE_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'border-style': ['all'],
  'label-selector': ['fontSize', 'color'],
  'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'show'],
  'basic-style-selector': [
    'colors',
    'alpha',
    'lineWidth',
    'lineSymbol',
    'lineSymbolSize',
    'lineSmooth'
  ],
  'x-axis-selector': [
    'name',
    'color',
    'fontSize',
    'position',
    'axisLabel',
    'axisLine',
    'splitLine'
  ],
  'y-axis-selector': [
    'name',
    'color',
    'fontSize',
    'position',
    'axisLabel',
    'axisLine',
    'splitLine',
    'axisValue',
    'axisLabelFormatter'
  ],
  'title-selector': [
    'title',
    'fontSize',
    'color',
    'hPosition',
    'isItalic',
    'isBolder',
    'remarkShow',
    'fontFamily',
    'letterSpace',
    'fontShadow'
  ],
  'legend-selector': ['icon', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition'],
  'function-cfg': ['slider', 'emptyDataStrategy'],
  threshold: ['lineThreshold']
}

export const LINE_AXIS_TYPE: AxisType[] = [
  'xAxis',
  'yAxis',
  'drill',
  'filter',
  'extLabel',
  'extTooltip'
]

const Y_AXIS_SERIES_ORDER_NOT_FOUND = Number.MAX_SAFE_INTEGER

/**
 * 只有系列来自值轴指标时才按指标顺序处理；有分组/堆叠维度时，系列来自维度值，保留原逻辑
 */
export const isYAxisSeriesChart = (chart: Chart) => {
  return !!chart.yAxis?.length && !chart.xAxisExt?.length && !chart.extStack?.length
}

/**
 * 指标系列在图表数据里的名称来自字段展示名，未设置展示名时使用字段名
 */
export const getYAxisSeriesName = axis => axis.chartShowName || axis.name

/**
 * 为 legend 生成值轴指标顺序的 color domain，同时保留数据里未匹配到的新系列
 */
export const getYAxisSeriesDomain = (chart: Chart, data: any[] = []) => {
  const domain = []
  chart.yAxis?.forEach(axis => {
    const name = getYAxisSeriesName(axis)
    if (name && !domain.includes(name)) {
      domain.push(name)
    }
  })
  data.forEach(item => {
    if (item.category && !domain.includes(item.category)) {
      domain.push(item.category)
    }
  })
  return domain
}

/**
 * 建立指标 id/显示名到字段下标的映射，供 tooltip 和堆叠层级排序复用
 */
export const getYAxisOrderMap = (chart: Chart) => {
  const orderMap = new Map<string, number>()
  chart.yAxis?.forEach((axis, index) => {
    orderMap.set(axis.id, index)
    const name = getYAxisSeriesName(axis)
    if (name) {
      orderMap.set(name, index)
    }
  })
  return orderMap
}

/**
 * 根据指标字段顺序获取当前系列排序值，未命中时放到最后并保持原相对顺序
 */
export const getYAxisSeriesOrder = (orderMap: Map<string, number>, item) => {
  return (
    orderMap.get(item.quotaList?.[0]?.id) ??
    orderMap.get(item.category) ??
    Y_AXIS_SERIES_ORDER_NOT_FOUND
  )
}

/**
 * 对 tooltip items 按右侧指标字段自上而下排序
 */
export const sortTooltipItemsByYAxis = (chart: Chart, items: any[]) => {
  if (!isYAxisSeriesChart(chart)) {
    return items
  }
  const orderMap = getYAxisOrderMap(chart)
  return items
    .map((item, index) => ({
      item,
      index,
      order: getYAxisSeriesOrder(orderMap, item)
    }))
    .sort((a, b) => a.order - b.order || a.index - b.index)
    .map(({ item }) => item)
}

/**
 * 对 legend 的 color domain 按右侧指标字段自上而下排序，不覆盖已有颜色映射等样式配置
 */
export const configYAxisSeriesLegendDomain = (chart: Chart, options: G2Spec) => {
  if (!isYAxisSeriesChart(chart)) {
    return options
  }
  const domain = getYAxisSeriesDomain(chart, options.data?.value)
  if (!domain.length) {
    return options
  }
  const scale = options.scale ?? {}
  options.scale = {
    ...scale,
    color: {
      ...(scale.color ?? {}),
      domain
    }
  }
  return options
}

/**
 * G2 正值堆叠是从下往上累加，堆叠折线需要反向使用指标下标来实现视觉自上而下对齐
 */
export const configStackOrderByYAxis = (chart: Chart, options: G2Spec) => {
  if (!isYAxisSeriesChart(chart)) {
    return options
  }
  const orderMap = getYAxisOrderMap(chart)
  options.transform = (options.transform ?? []).map(transform => {
    if (transform.type !== 'stackY') {
      return transform
    }
    return {
      ...transform,
      orderBy: item => {
        const order = getYAxisSeriesOrder(orderMap, item)
        return order === Y_AXIS_SERIES_ORDER_NOT_FOUND ? order : -order
      }
    }
  })
  return options
}
