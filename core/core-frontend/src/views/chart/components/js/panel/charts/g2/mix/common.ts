import type { G2Spec } from '@antv/g2'
import { parseJson } from '@/views/chart/components/js/util'

type MixLegendRelation = [string, string]

export const CHART_MIX_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'border-style',
  'dual-basic-style-selector',
  'x-axis-selector',
  'dual-y-axis-selector',
  'title-selector',
  'legend-selector',
  'label-selector',
  'tooltip-selector',
  'assist-line',
  'function-cfg',
  'jump-set',
  'linkage'
]
export const CHART_MIX_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'border-style': ['all'],
  'label-selector': ['fontSize', 'color'],
  'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'show'],
  'dual-basic-style-selector': [
    'colors',
    'alpha',
    'gradient',
    'lineWidth',
    'lineSymbol',
    'lineSymbolSize',
    'lineSmooth',
    'radiusColumnBar',
    'subSeriesColor',
    'seriesColor',
    'columnWidthRatio'
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
  'dual-y-axis-selector': [
    'name',
    'color',
    'fontSize',
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
  'function-cfg': ['emptyDataStrategy']
}

export const CHART_MIX_AXIS_TYPE: AxisType[] = [
  'xAxis',
  'yAxis',
  'drill',
  'filter',
  'extLabel',
  'extTooltip'
]

export const configMixCustomLegend = (
  chart: Chart,
  options: G2Spec,
  leftRelations: MixLegendRelation[] = [],
  rightRelations: MixLegendRelation[] = []
): G2Spec => {
  const { legend } = parseJson(chart.customStyle) || {}
  if (!legend?.show || !options.children?.length) {
    return options
  }
  const unionRelations = [...leftRelations, ...rightRelations].filter(
    ([key, value]) => key !== undefined && key !== null && Boolean(value)
  )
  if (!unionRelations.length) {
    return options
  }
  const hPosition = ['left', 'center', 'right'].includes(legend.hPosition)
    ? legend.hPosition
    : 'center'
  const rawVPosition = ['top', 'center', 'bottom'].includes(legend.vPosition)
    ? legend.vPosition
    : 'bottom'
  const vPosition = hPosition === 'center' && rawVPosition === 'center' ? 'top' : rawVPosition
  const getPositiveNumber = (value: unknown, defaultValue: number) => {
    const numberValue = Number(value)
    return Number.isFinite(numberValue) && numberValue > 0 ? numberValue : defaultValue
  }
  const legendFontSize = getPositiveNumber(legend.fontSize, 12)
  const legendMarkerSize = getPositiveNumber(legend.size, 4)
  const legendIcon = legend.icon || 'circle'
  const legendColor = legend.color || '#333333'
  const legendChartGap = 8
  const getLegendChartGap = (direction: 'col' | 'row', legendFirst = false) =>
    direction === 'col' && !legendFirst ? 4 : legendChartGap
  const getLegendRatio = (direction: 'col' | 'row', legendFirst = false) => {
    const chartContainer = chart.container as unknown
    const containerDom =
      typeof document === 'undefined' || !chartContainer
        ? undefined
        : typeof chartContainer === 'string'
        ? document.getElementById(chartContainer)
        : typeof (chartContainer as HTMLElement).getBoundingClientRect === 'function'
        ? (chartContainer as HTMLElement)
        : undefined
    const containerRect = containerDom?.getBoundingClientRect()
    const mainSize = direction === 'col' ? containerRect?.height : containerRect?.width
    const getTextWidth = text => {
      return Array.from(`${text ?? ''}`).reduce((width, char) => {
        return width + (char.charCodeAt(0) > 255 ? legendFontSize : legendFontSize * 0.6)
      }, 0)
    }
    const crossGap = getLegendChartGap(direction, legendFirst)
    // spaceFlex 按比例切分子层，这里把图例字号/图标尺寸换算成近似像素层高，避免图例放大后覆盖绘图区
    const legendLineSize = Math.ceil(Math.max(legendFontSize * 1.3, legendMarkerSize) + crossGap)
    const legendMainSize =
      direction === 'col'
        ? Math.max(24, legendLineSize)
        : Math.max(
            80,
            ...unionRelations.map(([name]) => getTextWidth(name) + legendMarkerSize + 40)
          )
    if (!mainSize || mainSize <= 0) {
      const fallbackLegendRatio = Math.max(2, Math.ceil(legendMainSize / 16))
      return legendFirst ? [fallbackLegendRatio, 20] : [20, fallbackLegendRatio]
    }
    // ratio 使用像素等价值，让图例层随内容增长，同时至少给绘图区保留 1px，避免极小容器下异常
    const safeLegendSize = Math.max(1, Math.min(legendMainSize, mainSize - 1))
    const chartMainSize = Math.max(mainSize - safeLegendSize, 1)
    return legendFirst ? [safeLegendSize, chartMainSize] : [chartMainSize, safeLegendSize]
  }
  // 双轴组合图左右 mark 使用独立 color scale，G2 内置 legend 无法直接合并，因此手工生成 legends 子层
  const legendMark: any = {
    position: 'top',
    type: 'legends',
    key: 'legend',
    scale: {
      color: {
        type: 'ordinal',
        domain: [],
        range: [],
        relations: unionRelations
      }
    },
    layout: {
      justifyContent: 'center',
      alignItems: 'center'
    },
    crossPadding: 0,
    itemMarker: legendIcon,
    itemMarkerSize: legendMarkerSize,
    itemLabelFontSize: legendFontSize,
    itemLabelFill: legendColor,
    itemLabelOpacity: 1,
    itemLabelFillOpacity: 1
  }
  unionRelations.forEach(([key, value]) => {
    legendMark.scale.color.domain.push(key)
    legendMark.scale.color.range.push(value)
  })
  if (hPosition === 'center') {
    options.direction = 'col'
    legendMark.maxRows = 1
    if (vPosition === 'top') {
      legendMark.position = 'top'
      legendMark.crossPadding = getLegendChartGap('col', true)
      options.ratio = getLegendRatio('col', true)
      options.children.unshift(legendMark)
    }
    if (vPosition === 'bottom') {
      legendMark.position = 'bottom'
      legendMark.crossPadding = getLegendChartGap('col')
      options.ratio = getLegendRatio('col')
      options.children.push(legendMark)
    }
    return options
  }
  if (vPosition === 'center') {
    options.direction = 'row'
    legendMark.maxCols = 1
    if (hPosition === 'left') {
      legendMark.position = 'left'
      legendMark.crossPadding = getLegendChartGap('row', true)
      options.ratio = getLegendRatio('row', true)
      options.children.unshift(legendMark)
    }
    if (hPosition === 'right') {
      legendMark.position = 'right'
      legendMark.crossPadding = getLegendChartGap('row')
      options.ratio = getLegendRatio('row')
      options.children.push(legendMark)
    }
    return options
  }
  legendMark.maxRows = 1
  if (vPosition === 'top') {
    legendMark.position = 'top'
    legendMark.crossPadding = getLegendChartGap('col', true)
    options.ratio = getLegendRatio('col', true)
    options.children.unshift(legendMark)
  }
  if (vPosition === 'bottom') {
    legendMark.position = 'bottom'
    legendMark.crossPadding = getLegendChartGap('col')
    options.ratio = getLegendRatio('col')
    options.children.push(legendMark)
  }
  if (hPosition === 'left') {
    legendMark.layout.justifyContent = 'flex-start'
  }
  if (hPosition === 'right') {
    legendMark.layout.justifyContent = 'flex-end'
  }
  return options
}
