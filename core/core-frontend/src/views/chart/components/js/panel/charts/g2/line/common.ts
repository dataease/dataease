import type { G2Spec } from '@antv/g2'
import {
  getColorFormAlphaColor,
  getLineLabelColorByCondition,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'

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
 * 统一处理条件样式颜色透明度，避免条件色绕过基础透明度配置
 */
export const getLineConditionColorWithAlpha = (color = '#000000', alpha) => {
  const targetColor = color?.trim() ? color : '#000000'
  return hexColorToRGBA(getColorFormAlphaColor(targetColor), alpha)
}

/**
 * 将命中条件样式的颜色写回数据项，供点图层和标签逻辑复用
 */
export const configLineConditionDataColor = (data, conditions, alpha) => {
  data.forEach(item => {
    const color = getLineLabelColorByCondition(conditions, item.value, item.quotaList?.[0]?.id)
    if (color) {
      item.conditionColor = getLineConditionColorWithAlpha(color, alpha)
    } else {
      delete item.conditionColor
    }
  })
}

/**
 * 兼容 G2 style 支持函数和值两种写法，方便在覆盖样式时保留原始配置
 */
export const getStyleValue = (style, d) => (typeof style === 'function' ? style(d) : style)

/**
 * 从 G2 内部渐变语法中提取实际色值，避免条件渐变嵌套已有渐变
 */
const getPlainColorFromG2Gradient = color => {
  if (typeof color !== 'string' || !color.startsWith('l(')) {
    return color
  }
  const match = color.match(/\s1:(rgba?\([^)]+\)|#[0-9a-fA-F]{6,8})/)
  return match?.[1] ?? color
}

/**
 * 获取系列默认颜色，作为未命中条件样式时的回退色
 */
export const getLineDefaultSeriesColor = (chart: Chart, options: G2Spec, seriesName) => {
  const relation = options.scale?.color?.relations?.find(item => item[0] === seriesName)
  if (relation?.[1]) {
    return getPlainColorFromG2Gradient(relation[1])
  }
  const { basicStyle } = parseJson(chart.customAttr)
  const colors =
    options.theme?.category10?.length > 0
      ? options.theme.category10
      : basicStyle.colors.map(color => hexColorToRGBA(color, basicStyle.alpha))
  if (!colors.length) {
    return '#000000'
  }
  const domain = [...(options.scale?.color?.domain ?? [])]
  options.data?.value?.forEach(item => {
    if (item.category && !domain.includes(item.category)) {
      domain.push(item.category)
    }
  })
  const index = Math.max(domain.indexOf(seriesName), 0)
  return getPlainColorFromG2Gradient(colors[index % colors.length])
}

/**
 * 复刻面积图原有上实下淡的透明度变化，条件色开启渐变时也保留层次
 */
const getGradientAlpha = (alpha, percent, enableGradient) => {
  if (!enableGradient) {
    return alpha
  }
  return alpha * (1 - (Math.min(Math.max(percent, 0), 100) / 100) * 0.7)
}

/**
 * 按条件边界生成连续渐变色，折线按数据值域切分，面积图可传入包含基线的值域
 */
export const getLineConditionGradientColor = (
  data,
  conditions,
  baseColor,
  alpha,
  valueRange?,
  enableGradient = false
) => {
  const plainBaseColor = getPlainColorFromG2Gradient(baseColor)
  if (!Array.isArray(data) || data.length < 2) {
    return plainBaseColor
  }
  const fieldId = data.find(item => item.quotaList?.[0]?.id)?.quotaList?.[0]?.id
  if (!fieldId) {
    return plainBaseColor
  }
  const values = data.map(item => Number(item.value)).filter(value => !Number.isNaN(value))
  if (!values.length) {
    return plainBaseColor
  }
  const minValue = valueRange?.min ?? Math.min(...values)
  const maxValue = valueRange?.max ?? Math.max(...values)
  const getColor = (value, percent) => {
    const color = getLineLabelColorByCondition(conditions, value, fieldId)
    return getLineConditionColorWithAlpha(
      color || plainBaseColor,
      getGradientAlpha(alpha, percent, enableGradient)
    )
  }
  if (minValue === maxValue) {
    return getColor(minValue, 0)
  }
  const bounds = getLineConditionValueBounds(minValue, maxValue, conditions, fieldId)
  const stops = []
  const addStop = (percent, color) => {
    stops.push(`${color} ${Number(percent.toFixed(4))}%`)
  }
  for (let i = 0; i < bounds.length - 1; i++) {
    const upperValue = bounds[i]
    const lowerValue = bounds[i + 1]
    if (upperValue === lowerValue) {
      continue
    }
    const startPercent = getLineConditionGradientPercent(upperValue, minValue, maxValue)
    const endPercent = getLineConditionGradientPercent(lowerValue, minValue, maxValue)
    const middleValue = (upperValue + lowerValue) / 2
    addStop(startPercent, getColor(middleValue, startPercent))
    addStop(endPercent, getColor(middleValue, endPercent))
  }
  return stops.length ? `linear-gradient(90deg, ${stops.join(', ')})` : plainBaseColor
}

/**
 * 面积图需要把 0 基线纳入纵向范围，确保颜色分割线和辅助线水平对齐
 */
const getAreaConditionGradientRange = data => {
  const values = data.map(item => Number(item.value)).filter(value => !Number.isNaN(value))
  if (!values.length) {
    return undefined
  }
  return {
    min: Math.min(0, ...values),
    max: Math.max(0, ...values)
  }
}

/**
 * 收集条件边界和数据边界，作为渐变 stop 的分段依据
 */
const getLineConditionValueBounds = (minValue, maxValue, conditions, fieldId) => {
  const bounds = [minValue, maxValue]
  conditions
    .filter(item => item.fieldId === fieldId)
    .forEach(item => {
      const values = item.term === 'between' ? [item.min, item.max] : [item.value]
      values.forEach(value => {
        const numValue = Number(value)
        if (Number.isNaN(numValue)) {
          return
        }
        if (numValue > minValue && numValue < maxValue) {
          bounds.push(numValue)
        }
      })
    })
  return Array.from(new Set(bounds.map(item => Number(item.toFixed(8))))).sort((a, b) => b - a)
}

/**
 * 将实际数值映射到纵向渐变百分比，值越大越靠近图形上方
 */
const getLineConditionGradientPercent = (value, minValue, maxValue) => {
  return ((maxValue - value) / (maxValue - minValue)) * 100
}

/**
 * 给折线 mark 覆盖 stroke，让线段颜色按条件区间连续过渡
 */
export const configLineMarkConditionStyle = (
  chart: Chart,
  options: G2Spec,
  lineMark,
  conditions,
  alpha
) => {
  const originLineStroke = lineMark.style?.stroke
  lineMark.style = {
    ...lineMark.style,
    stroke: d => {
      const seriesName = d?.[0]?.category
      const baseColor =
        getStyleValue(originLineStroke, d) || getLineDefaultSeriesColor(chart, options, seriesName)
      return getLineConditionGradientColor(d, conditions, baseColor, alpha)
    }
  }
}

/**
 * 给面积 mark 覆盖 fill，让面区域按条件水平切色并保留原有渐变开关效果
 */
export const configAreaMarkConditionStyle = (
  chart: Chart,
  options: G2Spec,
  areaMark,
  conditions,
  alpha
) => {
  const originAreaFill = areaMark.style?.fill
  const { basicStyle } = parseJson(chart.customAttr)
  areaMark.style = {
    ...areaMark.style,
    fill: d => {
      const seriesName = d?.[0]?.category
      const baseColor =
        getStyleValue(originAreaFill, d) || getLineDefaultSeriesColor(chart, options, seriesName)
      return getLineConditionGradientColor(
        d,
        conditions,
        baseColor,
        alpha,
        getAreaConditionGradientRange(d),
        !!basicStyle?.gradient
      )
    }
  }
}

/**
 * 给点 mark 覆盖 fill 和 stroke，让数据点颜色与条件样式结果保持一致
 */
export const configPointConditionStyle = pointMark => {
  const originFill = pointMark.style?.fill
  const originStroke = pointMark.style?.stroke
  pointMark.style = {
    ...pointMark.style,
    fill: d => d.conditionColor || getStyleValue(originFill, d) || d.color,
    stroke: d => d.conditionColor || getStyleValue(originStroke, d) || d.color
  }
}

/**
 * 根据条件样式生成辅助线 mark，作为面积和折线条件色的可视分界参考
 */
export const getLineConditionLineYMarks = (chart: Chart, threshold, alpha): G2Spec[] => {
  const yAxisIds = chart.yAxis.map(item => item.id)
  const marks = []
  threshold.lineThreshold?.forEach(field => {
    if (!yAxisIds.includes(field.fieldId)) {
      return
    }
    field.conditions?.forEach(condition => {
      const values =
        condition.term === 'between' ? [condition.min, condition.max] : [condition.value]
      values.forEach(value => {
        const lineValue = Number(value)
        if (Number.isNaN(lineValue)) {
          return
        }
        marks.push({
          type: 'lineY',
          data: [
            {
              value: lineValue,
              color: getLineConditionColorWithAlpha(condition.color, alpha)
            }
          ],
          encode: { y: 'value' },
          style: {
            stroke: d => d.color,
            lineDash: [2, 2],
            opacity: 1,
            pointerEvents: 'none'
          },
          tooltip: false,
          zIndex: -0.25
        })
      })
    })
  })
  return marks
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
