import { Chart as G2Chart, extend, Runtime, stdlib, type G2Spec } from '@antv/g2'
import { parseJson } from '@/views/chart/components/js/util'
import {
  getCategoryLegendStyle,
  getHorizontalLegendLabelMaxWidth,
  getHorizontalLegendTextStyle
} from '@/views/chart/components/js/panel/types/impl/g2'
import {
  getSideLegendMaxWidth,
  getSideLegendRowsPerPage,
  SIDE_LEGEND_DEFAULT_COL_PADDING,
  SIDE_LEGEND_MIN_LABEL_WIDTH,
  SIDE_HORIZONTAL_LEGEND_MAX_COLS,
  SIDE_LEGEND_NAVIGATOR_WIDTH
} from '@/views/chart/components/js/panel/types/impl/g2-legend'

type MixLegendRelation = [string, string]

interface MixLegendOptions {
  supportOrient?: boolean
  alignBottom?: boolean
}

interface MixSideLegendLayout {
  legendFirst: boolean
  itemCount: number
  columns: number
  itemHeight: number
  rowPadding: number
  contentWidth: number
  minItemWidth?: number
  maxWidthRatio?: number
  // 组合图外层分栏无法读取 Navigator，保存逐项宽度和页码供翻页后重新分配 Plot
  itemWidths?: number[]
  currentPage?: number
  crossPadding: number
}

const MIX_LEGEND_LAYOUT_SAFETY = 4

const getPositiveNumber = (value: unknown, defaultValue: number) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue > 0 ? numberValue : defaultValue
}

const getNonNegativeNumber = (value: unknown, defaultValue: number) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue >= 0 ? numberValue : defaultValue
}

const getLegendTextWidth = (text: unknown, fontSize: number) =>
  Array.from(`${text ?? ''}`).reduce(
    (width, char) => width + (char.charCodeAt(0) > 255 ? fontSize : fontSize * 0.6),
    0
  )

const getLegendDomain = context => {
  const scale = context?.scales?.find(item => item?.getOptions?.().domain?.length)
  return scale?.getOptions?.().domain ?? []
}

const getMixSideLegendWidth = (
  containerWidth: number,
  containerHeight: number,
  layout: MixSideLegendLayout
) => {
  const rowsPerPage = getSideLegendRowsPerPage(
    containerHeight,
    layout.itemHeight,
    layout.rowPadding
  )
  const navigatorExtra =
    layout.itemCount > rowsPerPage * layout.columns
      ? layout.columns > 1
        ? SIDE_LEGEND_NAVIGATOR_WIDTH
        : SIDE_LEGEND_NAVIGATOR_WIDTH - SIDE_LEGEND_DEFAULT_COL_PADDING
      : 0
  const pageSize = Math.max(1, rowsPerPage * layout.columns)
  const totalPages = Math.max(1, Math.ceil(layout.itemCount / pageSize))
  const currentPage = Math.max(0, Math.min(Number(layout.currentPage) || 0, totalPages - 1))
  const currentPageItemWidths =
    layout.columns === 1 && Array.isArray(layout.itemWidths)
      ? layout.itemWidths.slice(currentPage * pageSize, (currentPage + 1) * pageSize)
      : []
  // 单列侧边图例按当前页最长项收紧外层图例层，多列布局保持原网格宽度
  const pageContentWidth = currentPageItemWidths.length
    ? Math.max(...currentPageItemWidths)
    : layout.contentWidth
  // 水平侧栏的内层网格会为每列保留最少的文字宽度；外层也需按同一最小宽度
  // 分栏，避免短名称反而令图例子 View 窄到只能降级为单列。
  const itemWidth =
    layout.columns > 1 ? Math.max(pageContentWidth, layout.minItemWidth ?? 0) : pageContentWidth
  // 内层 maxWidthRatio=1 仍会为绘图区保留 1px，因此多列外层再预留 1px，
  // 使分配后的子 View 宽度恰好满足内层 requiredWidth。
  const contentWidth =
    itemWidth * layout.columns + layout.crossPadding + navigatorExtra + (layout.columns > 1 ? 1 : 0)
  return Math.max(
    1,
    Math.min(contentWidth, getSideLegendMaxWidth(containerWidth, layout.maxWidthRatio))
  )
}

const getMixSideLegendRatio = (options, layout: MixSideLegendLayout) => {
  const width = Number(options.width)
  const height = Number(options.height)
  if (!Number.isFinite(width) || width <= 0 || !Number.isFinite(height) || height <= 0) {
    return options.ratio
  }
  const padding = getNonNegativeNumber(options.padding, 0)
  const childCount = Array.isArray(options.children) ? options.children.length : 2
  const availableWidth = Math.max(1, width - padding * Math.max(0, childCount - 1))
  const legendWidth = Math.max(
    1,
    Math.min(getMixSideLegendWidth(width, height, layout), Math.max(1, availableWidth - 1))
  )
  const chartWidth = Math.max(1, availableWidth - legendWidth)
  return layout.legendFirst ? [legendWidth, chartWidth] : [chartWidth, legendWidth]
}

export const createResponsiveMixSpaceFlex = baseSpaceFlex => {
  const responsiveSpaceFlex = (...args) => {
    const layout = baseSpaceFlex(...args)
    return options => {
      const sideLegendLayout = options.dataeaseSideLegendLayout as MixSideLegendLayout
      if (!sideLegendLayout) {
        return layout(options)
      }
      return layout({
        ...options,
        ratio: getMixSideLegendRatio(options, sideLegendLayout)
      })
    }
  }
  responsiveSpaceFlex.props = baseSpaceFlex.props
  return responsiveSpaceFlex
}

export const createResponsiveMixLegendCategory = baseLegendCategory => {
  const responsiveLegendCategory = options => context => {
    const position = options.position
    const bboxWidth = Number(context?.value?.bbox?.width)
    if (!['top', 'bottom'].includes(position) || !Number.isFinite(bboxWidth) || bboxWidth <= 0) {
      return baseLegendCategory(options)(context)
    }
    const domain = getLegendDomain(context)
    const itemFontSize = getPositiveNumber(
      options.itemLabelFontSize ?? context?.theme?.legendCategory?.itemLabelFontSize,
      12
    )
    const markerSize = getPositiveNumber(
      options.itemMarkerSize ?? context?.theme?.legendCategory?.itemMarkerSize,
      8
    )
    const itemSpacing = Array.isArray(options.itemSpacing)
      ? getNonNegativeNumber(options.itemSpacing[0], 8)
      : getNonNegativeNumber(options.itemSpacing, 8)
    const itemGap = getNonNegativeNumber(options.colPadding, 8)
    const labelFormatter = options.labelFormatter
    const estimatedItemsWidth = domain.reduce((width, value, index) => {
      const label = typeof labelFormatter === 'function' ? labelFormatter(value, index) : value
      // G2 布局阶段已缓存标签尺寸，优先复用该结果；无缓存时才使用字符宽度估算
      const cachedLabelWidth = Number(options.indexBBox?.get?.(index)?.[1]?.width)
      const labelWidth =
        Number.isFinite(cachedLabelWidth) && cachedLabelWidth >= 0
          ? cachedLabelWidth
          : getLegendTextWidth(label, itemFontSize)
      return width + markerSize + itemSpacing + labelWidth + itemGap
    }, 0)
    const configuredLength = getPositiveNumber(options.length, bboxWidth)
    const availableLength = Math.min(configuredLength, bboxWidth)
    if (domain.length < 2 || estimatedItemsWidth <= availableLength) {
      return baseLegendCategory(options)(context)
    }
    const buttonSize = getPositiveNumber(options.navButtonSize, 12)
    const pageFontSize = getPositiveNumber(options.navPageNumFontSize, 12)
    const controllerPadding = getNonNegativeNumber(options.navControllerPadding, 5)
    const controllerSpacing = getNonNegativeNumber(options.navControllerSpacing, 5)
    const maxPageTextWidth = getLegendTextWidth(`${domain.length}/${domain.length}`, pageFontSize)
    const navigatorOverflow = Math.max(
      0,
      Math.ceil(
        controllerSpacing +
          buttonSize * 1.5 +
          maxPageTextWidth +
          controllerPadding * 2 -
          SIDE_LEGEND_NAVIGATOR_WIDTH +
          MIX_LEGEND_LAYOUT_SAFETY
      )
    )
    const length = Math.max(1, availableLength - navigatorOverflow)
    // 在当前帧创建图例前一次性收紧内容区，避免右对齐分页器溢出后反向裁剪首项
    return baseLegendCategory({ ...options, length })(context)
  }
  responsiveLegendCategory.props = baseLegendCategory.props
  return responsiveLegendCategory
}

const mixLegendLibrary = stdlib() as Record<string, any>
mixLegendLibrary['component.legendCategory'] = createResponsiveMixLegendCategory(
  mixLegendLibrary['component.legendCategory']
)
mixLegendLibrary['composition.spaceFlex'] = createResponsiveMixSpaceFlex(
  mixLegendLibrary['composition.spaceFlex']
)
export const MixG2Chart = extend(Runtime, mixLegendLibrary) as typeof G2Chart

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
  // 隐藏组件只展示图形缩略内容，不创建独立图例子层及其布局占位
  if (chart.dashboardHidden || !legend?.show || !options.children?.length) {
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
  const legendFontSize = getPositiveNumber(legend.fontSize, 12)
  const legendMarkerSize = getPositiveNumber(legend.size, 4) * 2
  const legendIcon = legend.icon || 'circle'
  const legendColor = legend.color || '#333333'
  const legendChartGap = 8
  const legendRowPadding = 8
  const legendItemSpacing = 8
  const horizontalSideLegendMaxWidthRatio = 0.4
  const legendNavigatorWidth = SIDE_LEGEND_NAVIGATOR_WIDTH
  const legendItemHeight = Math.ceil(Math.max(legendFontSize * 1.3, legendMarkerSize))
  const legendNavigatorHeight = legendItemHeight + 12
  const getTextWidth = text => {
    return getLegendTextWidth(text, legendFontSize)
  }
  const legendItemWidths = unionRelations.map(
    ([name]) => getTextWidth(name) + legendMarkerSize + 40
  )
  const sideLegendItemWidths = unionRelations.map(
    ([name]) =>
      getTextWidth(name) + legendMarkerSize + legendItemSpacing + SIDE_LEGEND_DEFAULT_COL_PADDING
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
    const crossSize = direction === 'col' ? containerRect?.width : containerRect?.height
    const crossGap = getLegendChartGap(direction, legendFirst, verticalLegend)
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
            1,
            mainSize && crossSize
              ? getMixSideLegendWidth(mainSize, crossSize, {
                  legendFirst,
                  itemCount: unionRelations.length,
                  columns:
                    legendOptions.supportOrient && !verticalLegend
                      ? Math.min(SIDE_HORIZONTAL_LEGEND_MAX_COLS, unionRelations.length)
                      : 1,
                  itemHeight: legendItemHeight,
                  rowPadding: legendRowPadding,
                  contentWidth: Math.max(...sideLegendItemWidths),
                  minItemWidth:
                    legendMarkerSize +
                    legendItemSpacing +
                    SIDE_LEGEND_MIN_LABEL_WIDTH +
                    SIDE_LEGEND_DEFAULT_COL_PADDING,
                  maxWidthRatio:
                    legendOptions.supportOrient && !verticalLegend
                      ? horizontalSideLegendMaxWidthRatio
                      : undefined,
                  itemWidths: sideLegendItemWidths,
                  currentPage: 0,
                  crossPadding: crossGap
                })
              : Math.max(...sideLegendItemWidths) + crossGap
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
  const enableSideLegendLayout = (legendFirst: boolean, horizontal = false) => {
    const columns = horizontal
      ? Math.min(SIDE_HORIZONTAL_LEGEND_MAX_COLS, unionRelations.length)
      : 1
    // 独立图例子层已在外层限制为画布宽度的 30%，内层只需使用完整可用宽度
    legendMark.dataeaseSideLegendAutoLayout = true
    legendMark.dataeaseSideLegendMaxWidthRatio = 1
    if (horizontal) {
      legendMark.dataeaseLegendOrientLayout = 'horizontal'
    }
    legendMark.dataeaseSideLegendMinColumns = horizontal ? columns : undefined
    ;(options as any).dataeaseSideLegendLayout = {
      legendFirst,
      itemCount: unionRelations.length,
      columns,
      itemHeight: legendItemHeight,
      rowPadding: legendRowPadding,
      contentWidth: Math.max(...sideLegendItemWidths),
      minItemWidth: horizontal
        ? legendMarkerSize +
          legendItemSpacing +
          SIDE_LEGEND_MIN_LABEL_WIDTH +
          SIDE_LEGEND_DEFAULT_COL_PADDING
        : undefined,
      maxWidthRatio: horizontal ? horizontalSideLegendMaxWidthRatio : undefined,
      itemWidths: sideLegendItemWidths,
      currentPage: 0,
      crossPadding: Number(legendMark.crossPadding) || 0
    } satisfies MixSideLegendLayout
  }
  const enableHorizontalLegendText = () =>
    Object.assign(legendMark, getHorizontalLegendTextStyle(legendFontSize))
  unionRelations.forEach(([key, value]) => {
    legendMark.scale.color.domain.push(key)
    legendMark.scale.color.range.push(value)
  })
  const clearLegendMainAxisMargin = (direction: 'col' | 'row') => {
    // 只清除图例与绘图区排列方向的外边距，保留分页器首尾方向的默认安全边距
    if (direction === 'col') {
      legendMark.marginTop = 0
      legendMark.marginBottom = 0
      legendMark.size = legendItemHeight
    } else {
      legendMark.marginLeft = 0
      legendMark.marginRight = 0
    }
  }
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
    // 独立图例子层与绘图区的间距统一交给 crossPadding 控制
    clearLegendMainAxisMargin(direction)
    const chartView = options.children.find(child => child.key === 'chart')
    if (chartView) {
      // 外层组合布局已负责整体留白，清除内层 view 的默认外边距以扩大绘图区
      chartView.margin = 0
    }
    legendMark.position = position
    legendMark.dataeaseOrientation = verticalLegend ? 'vertical' : 'horizontal'
    if (positionVertical) {
      // 显式写入两个方向，避免同一图表切换配置后复用旧的水平网格标记
      legendMark.dataeaseLegendOrientLayout = verticalLegend ? 'vertical' : 'horizontal'
    }
    legendMark.layout.justifyContent =
      alignPosition === 'left' || alignPosition === 'top'
        ? 'flex-start'
        : alignPosition === 'right' || alignPosition === 'bottom'
        ? 'flex-end'
        : 'center'
    if (positionVertical) {
      legendMark.navOrientation = 'vertical'
      legendMark.maxCols = verticalLegend ? 1 : SIDE_HORIZONTAL_LEGEND_MAX_COLS
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
    if (positionVertical) {
      enableSideLegendLayout(legendFirst, !verticalLegend)
    } else {
      enableHorizontalLegendText()
    }
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
        const maxItemWidth =
          getHorizontalLegendLabelMaxWidth(legendFontSize) + legendMarkerSize + 40
        legendMark.length =
          Math.min(Math.max(...legendItemWidths), maxItemWidth) +
          (showNavigator ? legendNavigatorWidth : 0)
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
    clearLegendMainAxisMargin('col')
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
      enableHorizontalLegendText()
      options.ratio = getLegendRatio('col', true)
      options.children.unshift(legendMark)
    }
    if (vPosition === 'bottom') {
      legendMark.position = 'bottom'
      legendMark.crossPadding = getLegendChartGap('col')
      enableHorizontalLegendText()
      options.ratio = getLegendRatio('col')
      options.children.push(legendMark)
    }
    return options
  }
  if (vPosition === 'center') {
    options.direction = 'row'
    legendMark.navOrientation = 'vertical'
    legendMark.maxCols = 1
    if (hPosition === 'left') {
      legendMark.position = 'left'
      legendMark.crossPadding = getLegendChartGap('row', true)
      enableSideLegendLayout(true)
      options.ratio = getLegendRatio('row', true)
      options.children.unshift(legendMark)
    }
    if (hPosition === 'right') {
      legendMark.position = 'right'
      legendMark.crossPadding = getLegendChartGap('row')
      enableSideLegendLayout(false)
      options.ratio = getLegendRatio('row')
      options.children.push(legendMark)
    }
    return options
  }
  legendMark.maxRows = 1
  if (vPosition === 'top') {
    legendMark.position = 'top'
    legendMark.crossPadding = getLegendChartGap('col', true)
    enableHorizontalLegendText()
    options.ratio = getLegendRatio('col', true)
    options.children.unshift(legendMark)
  }
  if (vPosition === 'bottom') {
    legendMark.position = 'bottom'
    legendMark.crossPadding = getLegendChartGap('col')
    enableHorizontalLegendText()
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
