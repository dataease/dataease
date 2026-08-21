import type { G2Spec } from '@antv/g2'
import { parseJson } from '@/views/chart/components/js/util'
import { getCategoryLegendStyle } from '@/views/chart/components/js/panel/types/impl/g2'

type MixLegendRelation = [string, string]

interface MixLegendOptions {
  supportOrient?: boolean
  alignBottom?: boolean
}

export const filterValidMixTooltipItems = <T extends { value?: any }>(items: T[] = []): T[] => {
  // 组合图 shared tooltip 会补齐同一维度下的空系列，渲染前只保留真实有值的项
  return items.filter(item => {
    const value = item?.value
    if (value === null || value === undefined || Number.isNaN(value)) {
      return false
    }
    if (typeof value === 'string') {
      const text = value.trim().toLowerCase()
      return text !== '' && text !== 'nan' && text !== 'null' && text !== 'undefined'
    }
    return true
  })
}

export const getAssistLineAxisIndex = (yAxisType?: string): number => {
  // 历史动态辅助线可能缺少 yAxisType，默认跟随左侧主数值轴
  return yAxisType === 'right' ? 1 : 0
}

export const getMixLabelTransform = (fullDisplay: boolean) => {
  const transform = [{ type: 'exceedAdjust', bounds: 'main' }]
  if (!fullDisplay) {
    transform.push({ type: 'overlapHide' })
  }
  return transform
}

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
  rightRelations: MixLegendRelation[] = [],
  legendOptions: MixLegendOptions = {}
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
  const legendMarkerSize = getPositiveNumber(legend.size, 4) * 2
  const legendIcon = legend.icon || 'circle'
  const legendColor = legend.color || '#333333'
  const legendChartGap = 8
  const legendRowPadding = 8
  const legendNavigatorWidth = 55
  const legendItemHeight = Math.ceil(Math.max(legendFontSize * 1.3, legendMarkerSize))
  const legendNavigatorHeight = legendItemHeight + 12
  const getTextWidth = text => {
    return Array.from(`${text ?? ''}`).reduce((width, char) => {
      return width + (char.charCodeAt(0) > 255 ? legendFontSize : legendFontSize * 0.6)
    }, 0)
  }
  const legendItemWidths = unionRelations.map(
    ([name]) => getTextWidth(name) + legendMarkerSize + 40
  )
  const getLegendChartGap = (
    direction: 'col' | 'row',
    legendFirst = false,
    verticalLegend = direction === 'row'
  ) =>
    (legendOptions.supportOrient || legendOptions.alignBottom) &&
    direction === 'col' &&
    !legendFirst &&
    !verticalLegend
      ? 0
      : direction === 'col' && !legendFirst
      ? 4
      : legendChartGap
  const getLegendRatio = (
    direction: 'col' | 'row',
    legendFirst = false,
    verticalLegend = direction === 'row',
    flexGap = 0
  ) => {
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
    const crossGap = getLegendChartGap(direction, legendFirst, verticalLegend)
    const sideLegendColumns = (() => {
      if (direction !== 'row') {
        return 1
      }
      const crossSize = containerRect?.height
      if (!crossSize || crossSize <= 0) {
        return Math.min(2, unionRelations.length)
      }
      const rowsPerColumn = Math.max(
        1,
        Math.floor((crossSize + legendRowPadding) / (legendItemHeight + legendRowPadding))
      )
      return Math.min(2, Math.ceil(unionRelations.length / rowsPerColumn))
    })()
    // spaceFlex 按比例切分子层，这里把图例字号/图标尺寸换算成近似像素层高，避免图例放大后覆盖绘图区
    const legendLineSize = legendItemHeight + (verticalLegend ? legendRowPadding : crossGap)
    const legendMainSize =
      direction === 'col'
        ? Math.max(
            (legendOptions.supportOrient || legendOptions.alignBottom) &&
              !legendFirst &&
              !verticalLegend
              ? 16
              : 24,
            verticalLegend
              ? legendLineSize * unionRelations.length - legendRowPadding + crossGap
              : legendLineSize
          )
        : Math.max(
            80,
            // 左右侧图例按最终列数预留宽度，避免第二列从独立子层溢出到 plot
            Math.max(...legendItemWidths) * sideLegendColumns
          )
    if (!mainSize || mainSize <= 0) {
      const fallbackLegendRatio = Math.max(2, Math.ceil(legendMainSize / 16))
      return legendFirst ? [fallbackLegendRatio, 20] : [20, fallbackLegendRatio]
    }
    // 垂直图例优先按内容完整展示；超过图表高度一半后固定占一半并启用分页
    const maxLegendMainSize =
      legendOptions.supportOrient && direction === 'col' && verticalLegend
        ? Math.max(1, mainSize / 2 - flexGap)
        : mainSize - 1
    // ratio 使用像素等价值，让图例层随内容增长，同时至少给绘图区保留 1px，避免极小容器下异常
    const safeLegendSize = Math.max(1, Math.min(legendMainSize, maxLegendMainSize, mainSize - 1))
    const chartMainSize = Math.max(mainSize - flexGap - safeLegendSize, 1)
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
    crossPadding: 10,
    itemMarker: legendIcon,
    ...getCategoryLegendStyle(legendMarkerSize, legendFontSize, legendColor)
  }
  unionRelations.forEach(([key, value]) => {
    legendMark.scale.color.domain.push(key)
    legendMark.scale.color.range.push(value)
  })
  if (legendOptions.supportOrient) {
    // 按 V2 语义分离图例方向与停靠位置，仅由启用该能力的图表进入此分支
    const verticalLegend = legend.orient === 'vertical'
    const centerHorizontal = hPosition === 'center'
    const centerVertical = vPosition === 'center'
    const position = centerHorizontal
      ? centerVertical
        ? 'top'
        : vPosition
      : centerVertical || verticalLegend
      ? hPosition
      : vPosition
    const alignPosition = position === hPosition ? vPosition : hPosition
    const positionVertical = position === 'left' || position === 'right'
    const direction = positionVertical ? 'row' : 'col'
    const legendFirst = position === 'top' || position === 'left'
    // 独立图例子层不叠加 G2 默认外边距，图表间距统一交给 crossPadding 控制
    legendMark.margin = 0
    const chartView = options.children.find(child => child.key === 'chart')
    if (chartView) {
      // 外层组合布局已负责整体留白，清除内层 view 的默认外边距以扩大绘图区
      chartView.margin = 0
    }
    legendMark.position = position
    legendMark.dataeaseOrientation = verticalLegend ? 'vertical' : 'horizontal'
    legendMark.layout.justifyContent =
      alignPosition === 'left' || alignPosition === 'top'
        ? 'flex-start'
        : alignPosition === 'right' || alignPosition === 'bottom'
        ? 'flex-end'
        : 'center'
    // 左右侧图例沿纵向分页，单页最多排列两列
    if (positionVertical) {
      legendMark.navOrientation = 'vertical'
      legendMark.maxCols = 2
    } else if (verticalLegend) {
      legendMark.navOrientation = 'horizontal'
      legendMark.maxCols = 1
    } else {
      legendMark.navOrientation = 'horizontal'
      legendMark.maxRows = 1
    }
    options.direction = direction
    const legendFlexGap = direction === 'col' && verticalLegend ? 4 : 0
    options.padding = legendFlexGap
    // 底部横向图例按单行实际高度占位，避免独立子层留下不可见空白
    legendMark.crossPadding = getLegendChartGap(direction, legendFirst, verticalLegend)
    options.ratio = getLegendRatio(direction, legendFirst, verticalLegend, legendFlexGap)
    if (positionVertical !== verticalLegend) {
      const legendLayerSize = legendFirst ? options.ratio[0] : options.ratio[1]
      legendMark.size = Math.max(1, legendLayerSize - legendMark.crossPadding)
      if (verticalLegend) {
        // 方向与停靠边交叉时按真实可用高度计算单列行数，仅在确实放不下时启用分页
        const rowsWithoutNavigator = Math.max(
          1,
          Math.floor((legendMark.size + legendRowPadding) / (legendItemHeight + legendRowPadding))
        )
        const showNavigator = rowsWithoutNavigator < unionRelations.length
        const legendItemsSize = Math.max(
          1,
          legendMark.size - (showNavigator ? legendNavigatorHeight : 0)
        )
        const visibleRows = Math.max(
          1,
          Math.min(
            unionRelations.length,
            Math.floor((legendItemsSize + legendRowPadding) / (legendItemHeight + legendRowPadding))
          )
        )
        legendMark.cols = 1
        legendMark.gridRow = visibleRows
        legendMark.rowPadding = legendRowPadding
        legendMark.dataeaseNavBelow = showNavigator
        legendMark.length =
          Math.max(...legendItemWidths) + (showNavigator ? legendNavigatorWidth : 0)
      }
    }
    if (legendFirst) {
      options.children.unshift(legendMark)
    } else {
      options.children.push(legendMark)
    }
    return options
  }
  if (legendOptions.alignBottom && vPosition === 'bottom') {
    // 水平左、中、右只改变横向对齐，底部图例统一使用相同的纵向留白
    legendMark.margin = 0
    options.padding = 0
    const chartView = options.children.find(child => child.key === 'chart')
    if (chartView) {
      chartView.margin = 0
    }
  }
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
    // 左右侧图例沿纵向分页，单页最多排列两列
    legendMark.navOrientation = 'vertical'
    legendMark.maxCols = 2
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
