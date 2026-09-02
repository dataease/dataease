import { Chart as G2Column } from '@antv/g2'
import {
  getCategoryLegendStyle,
  getHorizontalLegendTextStyle,
  G2ChartView,
  G2DrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2'
import {
  BAR_AXIS_TYPE,
  BAR_EDITOR_PROPERTY,
  BAR_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2/bar/common'
import { useI18n } from '@/hooks/web/useI18n'
import { flow, hexColorToRGBA, hexToRgba, parseJson } from '@/views/chart/components/js/util'
import { cloneDeep, defaultsDeep, filter, find, isEmpty } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  configAxisLengthLimit,
  configDimensionSlider,
  formatAxisLabelWithLengthLimit,
  getG2Renderer,
  getLineDash,
  handleChartDashboardHidden,
  setGradientColor,
  toLinearGradient,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '@/views/chart/components/js/panel/common/common_antv'
import {
  DEFAULT_BASIC_STYLE,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_EXT_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import {
  bindPlotBackgroundClick,
  createTooltipWrapper,
  getSeriesTooltipFormatter,
  getSeriesTooltipFormatterMap,
  getStackTooltipGroupName,
  getThemeSelectedState,
  getTooltipItemFormatter,
  handleEmptyDataStrategy,
  handleBarBreakLineNullData,
  isSeriesTooltipFormatterShown,
  isTooltipItemShown,
  renderGroupedTooltipItems,
  ChildSpec,
  tooltipCss,
  tooltipMaxHeight,
  Transform,
  ViewSpec
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'
import { addExtremumText, extremumEvt } from '@/views/chart/components/js/extremumUitl'
import G2TooltipCarousel from '@/views/chart/components/js/G2TooltipCarousel'

const { t } = useI18n()
const DEFAULT_DATA: any[] = []
const FULL_COLUMN_WIDTH_PADDING = 0.01
const PERCENTAGE_FULL_COLUMN_WIDTH_PADDING = 0.002
const isAssistLineRightAxis = item => item?.yAxisType === 'right'

/**
 * 柱状图
 */
export class Bar extends G2ChartView<ViewSpec, G2Column> {
  properties = BAR_EDITOR_PROPERTY
  propertyInner = {
    ...BAR_EDITOR_PROPERTY_INNER,
    'x-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['x-axis-selector'], 'showLengthLimit'],
    'basic-style-selector': [...BAR_EDITOR_PROPERTY_INNER['basic-style-selector'], 'seriesColor'],
    'label-selector': ['vPosition', 'seriesLabelFormatter', 'showExtremum'],
    'tooltip-selector': [
      'fontSize',
      'color',
      'backgroundColor',
      'seriesTooltipFormatter',
      'show',
      'carousel'
    ],
    'y-axis-selector': [...BAR_EDITOR_PROPERTY_INNER['y-axis-selector'], 'axisLabelFormatter']
  }
  axis: AxisType[] = [...BAR_AXIS_TYPE]
  axisConfig = {
    ...this['axisConfig'],
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }
  protected intervalOptions = {
    type: 'interval',
    encode: {
      x: 'field',
      y: 'value',
      color: 'category'
    },
    axis: {
      x: {
        title: false
      },
      y: {
        title: false
      }
    },
    state: {
      active: {
        backgroundPointerEvents: 'none'
      },
      unselected: { opacity: 0.5 }
    },
    interaction: {
      elementHighlight: {
        background: true,
        region: true,
        single: true
      }
    },
    tooltip: false,
    transform: [{ type: 'dodgeX' } as Transform]
  } as ViewSpec

  async drawChart(drawOptions: G2DrawOptions<G2Column>): Promise<G2Column> {
    const { chart, container, action, scale } = drawOptions
    chart.container = container
    if (!chart?.data?.data?.length) {
      return
    }
    const data = cloneDeep(drawOptions.chart.data?.data)
    if (this.name === 'bar-group') {
      if (!chart.xAxisExt[0]) {
        data.forEach(item => {
          item[this.intervalOptions.encode.color] = ' '
        })
      }
    }
    const initOptions: ViewSpec = {
      type: 'view',
      data: data,
      children: [
        {
          ...this.intervalOptions,
          transform: [].concat(this.intervalOptions.transform)
        }
      ]
    }
    const options: ViewSpec = this.setupOptions(chart, initOptions)
    const newChart = new G2Column({ container, autoFit: true, ...getG2Renderer() })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    newChart.on('interval:click', action)
    bindPlotBackgroundClick(newChart, { axis: this.name.includes('horizontal') ? 'y' : 'x' })
    new G2TooltipCarousel(newChart, chart, data).start()
    extremumEvt(newChart, chart, options.children[0], container, scale, this.name === 'bar')
    this.configLengthLimitTooltip(chart, newChart)
    return newChart
  }

  protected configLengthLimitTooltip(chart: Chart, chartObj: G2Column): void {
    if (this.supportAxisLengthLimit('xAxis')) {
      configAxisLengthLimit(chart, chartObj, 'xAxis')
    }
  }

  protected supportAxisLengthLimit(axisType: string): boolean {
    return axisType === 'xAxis'
  }

  protected configLabel(chart: Chart, options: ViewSpec): ViewSpec {
    const customAttr = parseJson(chart.customAttr)
    const { label: l } = customAttr
    // 如果没有启用标签，直接返回
    if (!l || !l.show) {
      return options
    }
    const { children } = options
    const { label: labelAttr } = parseJson(chart.customAttr)
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    const showExtremumIds = Object.keys(formatterMap).filter(id => formatterMap[id].showExtremum)
    if (showExtremumIds?.length > 0) {
      const { x: xField, color: colorField } = children[0].encode
      addExtremumText(children, showExtremumIds, xField, 'value', colorField)
    }
    const position = {
      position: l.position === 'middle' ? 'inside' : l.position,
      textAlign: 'center',
      dy: l.position === 'top' ? -15 : 0,
      dx: 0
    }
    // 配置标签样式
    const newLabel = {
      text: 'value',
      fillOpacity: 1,
      pointerEvents: 'none',
      fill: data => {
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return l.color
        }
        return labelCfg.color
      },
      fontSize: data => {
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return l.fontSize
        }
        return labelCfg.fontSize
      },
      ...position,
      formatter: (value, data) => {
        if (data.value === null || data.value === undefined) {
          return ''
        }
        if (data.extremum && showExtremumIds.includes(data.quotaList?.[0]?.id)) {
          return ''
        }
        if (!labelAttr.seriesLabelFormatter?.length) {
          return data.value
        }
        const labelCfg = formatterMap?.[data.quotaList[0].id] as SeriesFormatter
        if (!labelCfg) {
          return data.value
        }
        if (!labelCfg.show) {
          return ''
        }
        return valueFormatter(value, labelCfg.formatterCfg)
      }
    } as any
    if (!l.fullDisplay) {
      newLabel.transform = [{ type: 'exceedAdjust' }, { type: 'overlapHide' }]
    } else {
      newLabel.transform = [{ type: 'exceedAdjust' }]
    }
    return {
      ...options,
      children: [
        {
          ...children[0],
          labels: [newLabel]
        },
        ...children.slice(1)
      ]
    }
  }

  protected configTooltip(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const yAxis = chart.yAxis
    if (!tooltipAttr.show) {
      return options
    }
    const formatterMap = getSeriesTooltipFormatterMap(tooltipAttr)
    const tooltipOptions: ViewSpec = {
      tooltip: d => d,
      interaction: {
        ...children[0].interaction,
        tooltip: {
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltipAttr),
          enterable: true,
          shared: true,
          position: 'top-right',
          render: (_, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            let tooltipItems = originalItems
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              // 只隐藏明确配置为不展示的字段，避免过期 formatter 漏掉新指标
              tooltipItems = originalItems.filter(item =>
                isTooltipItemShown(formatterMap, item, 'yAxis')
              )
            }
            const result = []
            const head = originalItems[0]
            tooltipItems.forEach(item => {
              const formatter = getTooltipItemFormatter(formatterMap, item, yAxis, 'yAxis')
              const value =
                item.value === null || item.value === undefined
                  ? ''
                  : valueFormatter(
                      item.value,
                      formatter?.formatterCfg ?? tooltipAttr.tooltipFormatter
                    )
              const name = isEmpty(formatter?.chartShowName)
                ? formatter?.name ?? item.name
                : formatter.chartShowName
              result.push({ ...item, name, value })
            })
            head.dynamicTooltipValue?.forEach(item => {
              const formatter = getSeriesTooltipFormatter(
                formatterMap,
                item.fieldId,
                chart.extTooltip
              )
              if (formatter && isSeriesTooltipFormatterShown(formatterMap, item.fieldId)) {
                const value =
                  item.value === null || item.value === undefined
                    ? ''
                    : valueFormatter(parseFloat(item.value), formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: 'grey', name, value })
              }
            })
            // tooltip 项按维度槽位分组，帮助区分多维度明细
            const itemsHtml = renderGroupedTooltipItems(
              result,
              item => getStackTooltipGroupName(chart, item),
              item => {
                const marker = toLinearGradient(item.color)
                const label = item.name
                const value = item.value
                return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              }
            )
            const listHtml = `<ul class="g2-tooltip-list" style="${tooltipMaxHeight(
              chart
            )}margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
            return `${titleHtml}${listHtml}`
          }
        }
      }
    }
    return {
      ...options,
      children: [{ ...children[0], ...tooltipOptions }, ...children.slice(1)]
    }
  }

  protected configBasicStyle(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        let color = hexColorToRGBA(ele, basicStyle.alpha)
        if (basicStyle.gradient) {
          color = setGradientColor(color, true, 270)
        }
        colors.push(color ? color : hexColorToRGBA(ele, basicStyle.alpha))
      })
    }
    const scale: Record<string, any> = {
      color: {
        range: colors
      },
      y: {
        nice: true
      },
      x: {
        type: 'band',
        paddingInner: 0.01
      }
    }
    if (
      this.name === 'bar' ||
      this.name === 'percentage-bar-stack' ||
      this.name === 'waterfall' ||
      this.name === 'bar-group-stack'
    ) {
      scale.x.paddingInner = -0.21
    }
    if (this.name === 'bar-group') {
      scale.x.paddingInner = -0.2
    }
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { radiusColumnBar, columnBarRightAngleRadius } = basicStyle
    let style
    if (radiusColumnBar === 'topRoundAngle') {
      style = {
        radiusTopLeft: columnBarRightAngleRadius,
        radiusTopRight: columnBarRightAngleRadius
      }
    } else if (radiusColumnBar === 'roundAngle') {
      style = {
        radius: columnBarRightAngleRadius
      }
    } else {
      style = {
        radius: 0
      }
    }
    // 堆叠图按整根柱子的外轮廓圆角处理，中间色块连接处保持直边
    style = {
      ...style,
      ...this.getStackOuterRadiusStyle(basicStyle)
    }
    const columnWidthRatio = this.getColumnWidthRatio(basicStyle)
    const columnPadding = this.getColumnPadding(columnWidthRatio)
    let transform = children[0].transform
    if (columnWidthRatio) {
      // 100% 时保留极小 band 间距，避免 transpose 条形图贴边
      scale.x.padding = columnPadding
      scale.x.paddingInner = columnPadding
      transform = this.configDodgePadding(transform, columnPadding)
      style = {
        ...style,
        columnWidthRatio: this.getStyleColumnWidthRatio(columnPadding)
      }
    }
    return {
      ...options,
      children: [
        {
          ...children[0],
          scale,
          transform,
          style
        },
        ...children.slice(1)
      ]
    }
  }

  protected getColumnWidthRatio(basicStyle: DeepPartial<ChartBasicStyle>): number {
    // 兼容历史异常配置，保持样式面板 1-100% 的有效范围
    const value = basicStyle.columnWidthRatio ?? DEFAULT_BASIC_STYLE.columnWidthRatio
    if (value >= 1 && value <= 100) {
      return value / 100.0
    }
    if (value < 1) {
      return 1 / 100.0
    }
    return 1
  }

  protected getColumnPadding(columnWidthRatio: number): number {
    return Math.max(1 - columnWidthRatio, this.getFullColumnWidthPadding())
  }

  protected getFullColumnWidthPadding(): number {
    if (this.name.startsWith('percentage-bar-stack')) {
      return PERCENTAGE_FULL_COLUMN_WIDTH_PADDING
    }
    return FULL_COLUMN_WIDTH_PADDING
  }

  protected getStyleColumnWidthRatio(columnPadding: number): number {
    return 1 - columnPadding
  }

  protected getStackOuterRadiusStyle(
    basicStyle: DeepPartial<ChartBasicStyle>
  ): Record<string, any> {
    // 只覆盖堆叠柱条，普通柱条仍沿用原有单个 interval 圆角逻辑
    if (
      ![
        'bar-stack',
        'bar-group-stack',
        'percentage-bar-stack',
        'bar-stack-horizontal',
        'percentage-bar-stack-horizontal'
      ].includes(this.name)
    ) {
      return {}
    }
    if (!['topRoundAngle', 'roundAngle'].includes(basicStyle.radiusColumnBar)) {
      return {}
    }
    const radius = basicStyle.columnBarRightAngleRadius
    // G2 stackY 使用 first 和 last 控制外端圆角，innerRadius 为 0 可避免中间连接处圆角
    return {
      radiusTopLeft: radius,
      radiusTopRight: radius,
      radiusBottomRight: radius,
      radiusBottomLeft: radius,
      innerRadius: 0
    }
  }

  protected configDodgePadding(
    transforms: ChildSpec['transform'],
    padding: number
  ): ChildSpec['transform'] {
    if (!transforms?.length) {
      return transforms
    }
    if (padding > this.getFullColumnWidthPadding()) {
      return transforms
    }
    // dodgeX 会生成 series band，单独控制多指标柱之间的组内间距
    return transforms.map(transform =>
      transform.type === 'dodgeX' ? { ...transform, padding } : transform
    )
  }

  protected configLegend(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    return {
      ...options,
      children: [
        {
          ...children[0],
          legend: this.getLegend(chart)
        },
        ...children.slice(1)
      ]
    }
  }

  protected getLegend = (chart: Chart) => {
    let legend = {}
    let customStyle: CustomStyle
    if (chart.customStyle) {
      customStyle = parseJson(chart.customStyle)
      // legend
      if (customStyle.legend) {
        const l = JSON.parse(JSON.stringify(customStyle.legend))
        if (l.show) {
          let position
          const legendSymbol = l.icon
          const legendSize = l.size * 2
          const legendFontSize = l.fontSize
          const legendColor = l.color
          // position 图例布局
          // layoutJustifyContent 图例实例布局
          // 根据图例方向和位置设置布局和位置
          let layoutJustifyContent = 'center'
          if (l.vPosition === 'top' || l.vPosition === 'bottom') {
            position = l.vPosition
            layoutJustifyContent =
              l.hPosition === 'left'
                ? 'flex-start'
                : l.hPosition === 'right'
                ? 'flex-end'
                : 'center'
          } else {
            position = l.hPosition
          }
          const verticalLegend = position === 'left' || position === 'right'
          legend = {
            color: {
              position,
              layout: {
                justifyContent: layoutJustifyContent
              },
              itemMarker: legendSymbol,
              ...getCategoryLegendStyle(legendSize, legendFontSize, legendColor),
              ...(verticalLegend
                ? {
                    // 横向条形图的侧边图例与其他 G2 图表共用自适应规则
                    dataeaseSideLegendAutoLayout: true,
                    navOrientation: 'vertical',
                    maxCols: 1
                  }
                : {
                    ...getHorizontalLegendTextStyle(legendFontSize),
                    maxRows: 1
                  })
            }
          }
        } else {
          legend = false
        }
      }
    }
    return legend
  }

  protected configXAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const xAxis = this.getAxisConfig(chart, 'xAxis')
    return {
      ...options,
      children: [
        {
          ...children[0],
          axis: { ...children[0].axis, x: xAxis }
        },
        ...children.slice(1)
      ]
    }
  }

  protected configYAxis(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    const yAxis = this.getAxisConfig(chart, 'yAxis')
    const tmpOptions = {
      ...options,
      children: [
        {
          ...children[0],
          axis: { ...children[0].axis, y: yAxis }
        },
        ...children.slice(1)
      ]
    }
    const customStyle = parseJson(chart.customStyle)
    const yAxisAtt = JSON.parse(JSON.stringify(customStyle['yAxis']))
    if (!yAxisAtt.axisValue?.auto) {
      const child0 = tmpOptions.children[0]
      return {
        ...tmpOptions,
        children: [
          {
            ...child0,
            scale: {
              ...child0.scale,
              y: {
                nice: false,
                clamp: true,
                domain: [yAxisAtt.axisValue.min, yAxisAtt.axisValue.max],
                tickCount: yAxisAtt.axisValue.splitCount,
                tickMethod: (min, max, count) => {
                  const n = Math.max(2, count)
                  const step = (max - min) / (n - 1)
                  const ticks = []
                  for (let i = 0; i < n; i++) {
                    ticks.push(min + step * i)
                  }
                  return ticks
                }
              }
            }
          },
          ...tmpOptions.children.slice(1)
        ]
      }
    }
    return tmpOptions
  }

  protected configAnalyse(chart: Chart, options: ViewSpec): ViewSpec {
    const { children } = options
    return {
      ...options,
      children: [...children, ...this.getAssistLineStyle(chart, options)]
    }
  }
  protected getAssistLineStyle = (chart: Chart, options?: ViewSpec) => {
    const assistLine = []
    const senior = parseJson(chart.senior)
    if (!senior.assistLineCfg?.enable) {
      return assistLine
    }
    const assistLineArr = senior.assistLineCfg.assistLine
    if (assistLineArr?.length > 0) {
      const customStyle = parseJson(chart.customStyle)
      let axisFormatterCfg, axisExtFormatterCfg
      const isHorizontalBar = this.name.includes('horizontal')
      const axis = options?.children?.[0]?.axis
      const valueAxisLabelFormatter = isHorizontalBar ? (axis as any)?.y?.labelFormatter : undefined
      if (isHorizontalBar) {
        if (customStyle.xAxis) {
          const a = JSON.parse(JSON.stringify(customStyle.xAxis))
          axisFormatterCfg = a.axisLabelFormatter
            ? a.axisLabelFormatter
            : DEFAULT_XAXIS_STYLE.axisLabelFormatter
        }
      } else {
        if (customStyle.yAxis) {
          const a = JSON.parse(JSON.stringify(customStyle.yAxis))
          axisFormatterCfg = a.axisLabelFormatter
            ? a.axisLabelFormatter
            : DEFAULT_YAXIS_STYLE.axisLabelFormatter
        }
        if (customStyle.yAxisExt) {
          const a = JSON.parse(JSON.stringify(customStyle.yAxisExt))
          axisExtFormatterCfg = a.axisLabelFormatter
            ? a.axisLabelFormatter
            : DEFAULT_YAXIS_EXT_STYLE.axisLabelFormatter
        }
      }
      const fixedLines = assistLineArr.filter(ele => ele.field === '0')
      const dynamicLineFields = assistLineArr
        .filter(ele => ele.field === '1')
        .map(item => item.fieldId)
      const quotaFields = filter(chart.yAxis, ele => ele.summary !== '' && ele.id !== '-1')
      const quotaExtFields = filter(chart.yAxisExt, ele => ele.summary !== '' && ele.id !== '-1')
      const dynamicLines = chart.data.dynamicAssistLines?.filter(item => {
        return (
          dynamicLineFields?.includes(item.fieldId) &&
          (!!find(quotaFields, d => d.id === item.fieldId) ||
            (!!find(quotaExtFields, d => d.id === item.fieldId) &&
              chart.type.includes('chart-mix')))
        )
      })
      const lines = fixedLines.concat(dynamicLines || [])
      lines.forEach(item => {
        const value = parseFloat(item.value)
        // 历史动态辅助线可能缺少 yAxisType，默认跟随主数值轴
        const useExtAxisFormatter =
          isAssistLineRightAxis(item) && axisExtFormatterCfg && quotaExtFields.length > 0
        const targetFormatter = useExtAxisFormatter ? axisExtFormatterCfg : axisFormatterCfg
        const axisFormattedValue =
          typeof valueAxisLabelFormatter === 'function'
            ? valueAxisLabelFormatter(value)
            : valueFormatter(value, targetFormatter)
        const content = item.name + ' : ' + axisFormattedValue
        const fontSize = item.fontSize ? parseInt(item.fontSize + '') : '100%'
        const labelTransform = isHorizontalBar
          ? { transform: [{ type: 'exceedAdjust', offsetY: 24 }] }
          : {}
        assistLine.push({
          type: 'lineY',
          // 辅助线统一退出分类图例，避免后续新增视觉编码时影响图例布局
          legend: false,
          data: [value],
          style: {
            stroke: item.color,
            strokeOpacity: 1,
            lineDash: getLineDash(item.lineType)
          },
          labels: [
            {
              text: content,
              position: isHorizontalBar ? 'top-left' : 'left',
              textBaseline: 'bottom',
              fill: item.color,
              fillOpacity: 1,
              background: false,
              fontSize,
              ...labelTransform,
              style: isHorizontalBar ? { transform: 'rotate(90deg)' } : undefined
            }
          ]
        })
      })
    }
    return assistLine
  }

  protected getAxisConfig(chart: Chart, axisType: string): any {
    const customStyle = parseJson(chart.customStyle)
    const axis = JSON.parse(JSON.stringify(customStyle[axisType]))
    if (customStyle[axisType] && axis.show) {
      // 轴线与刻度线统一使用公共规则
      const lineAndTick = {
        ...this.getAxisLineStyle(chart, axis),
        lineLineDash: getLineDash(axis.axisLine.lineStyle.style)
      }
      const xAxis = customStyle.xAxis
      const gridFilter = axisType === 'yAxis' ? this.getOverlapGridFilter(xAxis) : {}
      // 网格线
      const grid = {
        grid: axis.splitLine.show,
        gridLineWidth: axis.splitLine.lineStyle.width,
        gridStroke: axis.splitLine.lineStyle.color,
        gridStrokeOpacity: 1,
        gridLineDash: getLineDash(axis.splitLine.lineStyle.style),
        ...gridFilter
      }
      // 标签（刻度值）
      const label = {
        label: axis.axisLabel.show,
        labelFontSize: axis.axisLabel.fontSize,
        labelFill: axis.axisLabel.color,
        labelOpacity: 1,
        labelFormatter: value => {
          if (axisType === 'yAxis') {
            return valueFormatter(value, axis.axisLabelFormatter)
          }
          if (this.supportAxisLengthLimit(axisType)) {
            return formatAxisLabelWithLengthLimit(value, axis.axisLabel.lengthLimit)
          }
          return value
        }
      }
      const x = {
        position: axis.position,
        // 标题
        title: axis.nameShow && axis.name ? axis.name : false,
        titleFontSize: axis.fontSize,
        titleFill: axis.color,
        // 轴线与刻度线
        ...lineAndTick,
        // 网格线
        ...grid,
        // 刻度值
        ...label,
        labelAutoHide: true,
        labelAutoRotate: false,
        ...this.getAxisLabelStyle(axis)
      }
      return x
    }
    return false
  }

  protected configTheme(chart: Chart, options: ViewSpec): ViewSpec {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, basicStyle.alpha))
      })
    }
    const customStyle = parseJson(chart.customStyle)
    let bgColor
    if (customStyle.background) {
      bgColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
    const theme = {
      color: colors[0],
      category10: colors,
      category20: colors,
      view: {
        viewFill: bgColor
      }
    }
    const [intervalMark] = options.children
    intervalMark.state = getThemeSelectedState(chart, intervalMark.state)
    return { ...options, theme }
  }

  protected configBarConditions(chart: Chart, options: ViewSpec): ViewSpec {
    const { threshold } = parseJson(chart.senior)
    if (!threshold.enable) return options
    const overThreshold = data => {
      data.forEach(item => {
        item['conditionColor'] = []
        const quotaList = item.quotaList.map(q => q.id) ?? []
        quotaList.forEach(q => {
          let currentValue = item['value']
          if (chart.type === 'progress-bar') {
            currentValue = item['originalValue']
          }
          const cColor = this.getColorByConditions([].concat(q), currentValue, chart)
          if (cColor) {
            item.conditionColor.push(cColor)
          }
        })
        if (!item.conditionColor.length) {
          item.conditionColor = undefined
        }
      })
      return data
    }
    options.children[0].data = {
      value: options.children[0].data || options.data,
      transform: [
        {
          type: 'custom',
          callback: data => overThreshold(data)
        }
      ]
    }
    options.children[0].style = {
      ...options.children[0].style,
      fill: d => {
        if (d.conditionColor?.length) {
          return d.conditionColor[0]
        }
        return d.color
      }
    }
    return options
  }

  protected getColorByConditions = (quotaList: any[], values: number | number[], chart) => {
    const { threshold } = parseJson(chart.senior)
    const { basicStyle } = parseJson(chart.customAttr)
    const currentValue = Array.isArray(values) ? values[1] - values[0] : values
    if (!currentValue) return undefined
    // 同样的指标只取最后一个
    const conditionMap = new Map()
    for (const condition of threshold.lineThreshold ?? []) {
      conditionMap.set(condition.fieldId, condition)
    }
    for (const condition of conditionMap.values()) {
      if (chart.type === 'progress-bar' && chart.yAxisExt?.[0]?.id !== quotaList?.[0]) continue
      if (!quotaList.includes(condition.fieldId) && chart.type !== 'waterfall') continue
      for (const tc of condition.conditions) {
        if (
          (tc.term === 'between' && currentValue >= tc.min && currentValue <= tc.max) ||
          (tc.term === 'lt' && currentValue < tc.value) ||
          (tc.term === 'le' && currentValue <= tc.value) ||
          (tc.term === 'gt' && currentValue > tc.value) ||
          (tc.term === 'ge' && currentValue >= tc.value)
        ) {
          let tmpColor = hexToRgba(tc.color, basicStyle.alpha)
          if (basicStyle.gradient) {
            const vhAngle = ['bar-horizontal', 'progress-bar'].includes(chart.type) ? 0 : 270
            tmpColor = setGradientColor(tmpColor, true, vhAngle)
          }
          return tmpColor
        }
      }
    }
  }

  protected configSlider(chart: Chart, options: ViewSpec): ViewSpec {
    // 仅在图表样式面板开放 slider 时注入缩略轴，避免影响无缩略轴配置的柱状图
    const functionCfgItems = this.propertyInner?.['function-cfg']
    const hasSliderConfig = Array.isArray(functionCfgItems) && functionCfgItems.includes('slider')
    const { functionCfg } = parseJson(chart.senior)
    if (!hasSliderConfig || !functionCfg?.sliderShow) {
      return options
    }
    const lineMark = options.children[0]
    const yAxis = parseJson(chart.customStyle)?.yAxis
    const valueField = lineMark.encode?.y ?? options.encode?.y
    // 垂直柱状图按离散维度域过滤缩略轴，并固定元素 key 避免切换范围时动画方向错乱
    configDimensionSlider(lineMark, lineMark.data ?? options.data, functionCfg, {
      interactionName: 'barDimensionSliderFilter',
      stableKey: true,
      disableMorph: true,
      // 基础柱状图仅在数值轴自动模式下跟随可见维度重新计算范围
      ...(this.name === 'bar' &&
        yAxis?.axisValue?.auto !== false &&
        typeof valueField === 'string' && {
          valueScale: { field: valueField, includeZero: true }
        }),
      // 大数据标签使用独立 interval 承载，缩略轴拖动时必须同步它的离散维度域
      syncChildren: true
    })
    return options
  }

  protected configColor(chart: Chart, options: ViewSpec): ViewSpec {
    const { basicStyle } = parseJson(chart.customAttr)
    const { seriesColor } = basicStyle
    if (!seriesColor?.length) {
      return options
    }
    const { xAxis, xAxisExt, yAxis } = chart
    if (!xAxis?.length || !yAxis?.length) {
      return options
    }
    const relations = []
    if (xAxisExt?.length) {
      seriesColor.forEach(item => {
        let color = hexColorToRGBA(item.color, basicStyle.alpha)
        if (basicStyle.gradient) {
          color = setGradientColor(color, true, 270)
        }
        relations.push([item.id, color])
      })
    } else {
      const colorMap = seriesColor.reduce((pre, next) => {
        pre[next.id] = next.color
        return pre
      }, {})
      yAxis.forEach(item => {
        if (colorMap[item.id]) {
          let color = hexColorToRGBA(colorMap[item.id], basicStyle.alpha)
          if (basicStyle.gradient) {
            color = setGradientColor(color, true, 270)
          }
          relations.push([item.chartShowName ?? item.name, color])
        }
      })
    }
    if (relations.length) {
      const scaleOptions = {
        scale: {
          color: {
            relations
          }
        }
      }
      defaultsDeep(options, scaleOptions)
    }
    return options
  }

  protected configEmptyDataStrategy(chart: Chart, options: ViewSpec): ViewSpec {
    handleEmptyDataStrategy(chart, options)
    handleBarBreakLineNullData(chart, options)
    return options
  }

  protected setupOptions(chart: Chart, options: ViewSpec): ViewSpec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAnalyse,
      this.configBarConditions,
      this.configSlider
    )(chart, options, {}, this)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.senior.functionCfg.emptyDataStrategy = 'ignoreData'
    return chart
  }

  constructor(name = 'bar', defaultData = DEFAULT_DATA) {
    super(name, defaultData)
  }
}
