import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  getLineConditions,
  getLineLabelColorByCondition,
  handleBreakLineMultiDimension,
  handleIgnoreData,
  handleSetZeroMultiDimension,
  handleSetZeroSingleDimension,
  hexColorToRGBA,
  parseJson,
  randomString,
  setUpGroupSeriesColor,
  setUpStackSeriesColor
} from '@/views/chart/components/js/util'
import { cloneDeep, defaultsDeep, isEmpty, merge } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  configAreaMarkConditionStyle,
  configLineConditionDataColor,
  configLineMarkConditionStyle,
  configPointConditionStyle,
  configStackOrderByYAxis,
  configYAxisSeriesLegendDomain,
  getLineConditionLineYMarks,
  getLineTooltipSameDimensionItems,
  bindLineLegendState,
  LINE_AXIS_TYPE,
  LINE_CONDITION_VISIBLE_DOMAIN_KEY,
  LINE_EDITOR_PROPERTY,
  LINE_EDITOR_PROPERTY_INNER,
  sortTooltipItemsByYAxis
} from './common'
import { useI18n } from '@/hooks/web/useI18n'
import { addExtremumText, extremumEvt } from '@/views/chart/components/js/extremumUitl'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { DEFAULT_YAXIS_STYLE } from '@/views/chart/components/editor/util/chart'
import {
  configDimensionSlider,
  getG2Renderer,
  getTooltipCrosshairsStyle,
  handleChartDashboardHidden,
  setGradientColor,
  toLinearGradient,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'
import G2TooltipCarousel from '@/views/chart/components/js/G2TooltipCarousel'
import {
  createTooltipWrapper,
  getSeriesTooltipFormatter,
  getSeriesTooltipFormatterMap,
  getStackTooltipGroupName,
  getTooltipItemFormatter,
  renderGroupedTooltipItems,
  isSeriesTooltipFormatterShown,
  isTooltipItemShown,
  tooltipCss,
  tooltipMaxHeight
} from '../bar/barUtil'

const { t } = useI18n()
const DEFAULT_DATA = []
const MAX_POINT_RANGE_PADDING = 0.12
const MIN_POINT_RANGE_PADDING = 0.02

const getPointRangePadding = (chart: Chart, lineSymbolSize: number) => {
  const symbolSize = Number(lineSymbolSize) || 0
  if (symbolSize <= 0) {
    return 0
  }
  const containerWidth = chart.container?.clientWidth || 0
  if (!containerWidth) {
    return MIN_POINT_RANGE_PADDING
  }
  return Math.min(
    MAX_POINT_RANGE_PADDING,
    Math.max(MIN_POINT_RANGE_PADDING, (symbolSize * 2) / containerWidth)
  )
}

export class Area extends G2ChartView {
  properties = LINE_EDITOR_PROPERTY
  propertyInner = {
    ...LINE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [
      ...LINE_EDITOR_PROPERTY_INNER['basic-style-selector'],
      'gradient',
      'seriesColor'
    ],
    'label-selector': ['seriesLabelVPosition', 'seriesLabelFormatter', 'showExtremum'],
    'tooltip-selector': [
      ...LINE_EDITOR_PROPERTY_INNER['tooltip-selector'],
      'seriesTooltipFormatter',
      'carousel'
    ]
  }
  axis: AxisType[] = [...LINE_AXIS_TYPE]
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
  baseOptions: G2Spec = {
    type: 'view',
    autoFit: true,
    clip: true,
    encode: {
      x: 'field',
      y: 'value',
      color: 'category'
    },
    scale: {
      x: {
        range: [0, 1]
      },
      y: {
        nice: true
      }
    },
    children: [
      {
        type: 'area',
        tooltip: false,
        style: { fillOpacity: 0.3 },
        zIndex: -1
      },
      { type: 'line', encode: { series: 'category' }, zIndex: -2 },
      { type: 'point', tooltip: false, zIndex: 0 }
    ]
  }
  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, action, container, scale } = drawOptions
    chart.container = container
    if (!chart.data?.data?.length) {
      return
    }
    const data = cloneDeep(chart.data.data)
    // options
    const initOptions: G2Spec = {
      ...cloneDeep(this.baseOptions),
      data: {
        value: data
      }
    }
    const newChart = new G2Chart({ container, ...getG2Renderer() })
    const legendState = bindLineLegendState(newChart)
    const options = this.setupOptions(chart, initOptions, { legendState })
    // 开始渲染
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    newChart.on('point:click', action)
    extremumEvt(newChart, chart, options, container, scale, this.name === 'area')
    new G2TooltipCarousel(newChart, chart, data).start()
    return newChart
  }

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const colors: string[] = []
    if (customAttr.basicStyle) {
      const basicStyle = customAttr.basicStyle
      basicStyle.colors.forEach(ele => {
        let color = hexColorToRGBA(ele, basicStyle.alpha)
        if (basicStyle.gradient) {
          color = setGradientColor(color, true, 270)
        }
        colors.push(color)
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
    return { ...options, theme }
  }

  protected configColor(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const { seriesColor } = basicStyle
    if (!seriesColor?.length) {
      return options
    }
    const { xAxis, yAxis } = chart
    if (!xAxis?.length || !yAxis?.length) {
      return options
    }
    const relations = []
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

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label: labelAttr } = parseJson(chart.customAttr)
    if (!labelAttr.show) {
      return options
    }
    const conditions = getLineConditions(chart)
    const formatterMap = labelAttr.seriesLabelFormatter?.reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {})
    const showExtremumIds = Object.keys(formatterMap).filter(id => formatterMap[id].showExtremum)
    if (showExtremumIds?.length > 0) {
      const { x: xField, y: yField, color: colorField } = options.encode
      addExtremumText(options.children, showExtremumIds, xField, yField, colorField)
    }
    const pointMark: G2Spec = options.children[2]
    const labelOpt = {
      labels: [
        {
          text: d => {
            if (d.value === null) {
              return ''
            }
            const isExtremumShown = d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)
            if (isExtremumShown) {
              return ''
            }
            if (!labelAttr.seriesLabelFormatter?.length) {
              return d.value
            }
            const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
            if (!labelCfg) {
              return d.value
            }
            if (!labelCfg.show) {
              return ''
            }
            return valueFormatter(d.value, labelCfg.formatterCfg)
          },
          style: {
            opacity: 1,
            // 标签需要保持自身填充透明度，避免条件色被面图层透明度影响
            fillOpacity: 1,
            fontSize: d => {
              if (!labelAttr.seriesLabelFormatter?.length) {
                return 12
              }
              const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg) {
                return 12
              }
              if (d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)) {
                return labelCfg.showExtremum ? labelCfg.fontSize : 0
              }
              if (!labelCfg.show) {
                return 0
              }
              return labelCfg.fontSize
            },
            fill: d => {
              if (!labelAttr.seriesLabelFormatter?.length) {
                return 'black'
              }
              const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
              if (d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)) {
                return labelCfg?.showExtremum ? labelCfg?.color ?? 'black' : 'black'
              }
              if (!labelCfg?.show) {
                return 'black'
              }
              const color =
                getLineLabelColorByCondition(conditions, d.value, d.quotaList[0].id) ||
                labelCfg.color
              return color
            },
            position: d => {
              if (
                (d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)) ||
                !labelAttr.seriesLabelFormatter?.length
              ) {
                return 'top'
              }
              const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg?.show) {
                return 'top'
              }
              return labelCfg.position
            }
          },
          textBaseline: d => {
            if (
              (d.extremum && showExtremumIds.includes(d.quotaList?.[0]?.id)) ||
              !labelAttr.seriesLabelFormatter?.length
            ) {
              return 'bottom'
            }
            const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
            if (!labelCfg?.show) {
              return 'bottom'
            }
            return labelCfg.position === 'top' ? 'bottom' : 'top'
          },
          transform: labelAttr.fullDisplay
            ? [{ type: 'exceedAdjust', bounds: 'main' }]
            : [{ type: 'exceedAdjust', bounds: 'main' }, { type: 'overlapHide' }],
          fontFamily: chart.fontFamily
        }
      ]
    }
    defaultsDeep(pointMark, labelOpt)
    return options
  }

  protected configBasicStyle(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const [areaMark, lineMark, pointMark] = options.children
    const lineStyleOpt = {
      encode: {
        shape: basicStyle.lineSmooth ? 'smooth' : 'line',
        size: basicStyle.lineWidth
      }
    }
    defaultsDeep(lineMark, lineStyleOpt)
    const areaStyleOpt = {
      encode: {
        shape: basicStyle.lineSmooth ? 'smooth' : 'area'
      }
    }
    defaultsDeep(areaMark, areaStyleOpt)
    const pointStyleOpt = {
      encode: {
        shape: basicStyle.lineSymbol,
        size: basicStyle.lineSymbolSize ? basicStyle.lineSymbolSize : 0.01
      },
      style: {
        opacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        fillOpacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        strokeOpacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        lineWidth: 0
      },
      state: {
        // 禁止 hover/选中态给数据点补默认黑色描边
        active: { lineWidth: 0, strokeOpacity: 0 },
        selected: { lineWidth: 0, strokeOpacity: 0 }
      }
    }
    defaultsDeep(pointMark, pointStyleOpt)
    const pointRangePadding = getPointRangePadding(chart, basicStyle.lineSymbolSize)
    options.scale.x.range = [pointRangePadding, 1 - pointRangePadding]
    return options
  }

  /**
   * 给基础面积图追加条件样式，面、线和点共用同一套条件颜色
   */
  protected configConditions(chart: Chart, options: G2Spec): G2Spec {
    // 堆叠面积图没有条件样式入口，保持原有堆叠色彩逻辑
    if (chart.type === 'area-stack') {
      return options
    }
    const { threshold } = parseJson(chart.senior)
    if (!threshold?.enable || !threshold?.lineThreshold?.length) {
      return options
    }
    const data = options.data?.value
    if (!data?.length) {
      return options
    }
    const conditions = getLineConditions(chart)
    if (!conditions.length) {
      return options
    }
    const { basicStyle } = parseJson(chart.customAttr)
    const [areaMark, lineMark, pointMark] = options.children
    const conditionVisibleDomain = { field: options.encode?.x }
    Object.defineProperty(lineMark, LINE_CONDITION_VISIBLE_DOMAIN_KEY, {
      value: conditionVisibleDomain,
      configurable: true
    })
    // 先把条件色写入数据项，再分别驱动面积、折线和点的样式
    configLineConditionDataColor(data, conditions, basicStyle.alpha)
    configAreaMarkConditionStyle(
      chart,
      options,
      areaMark,
      conditions,
      basicStyle.alpha,
      conditionVisibleDomain
    )
    configLineMarkConditionStyle(
      chart,
      options,
      lineMark,
      conditions,
      basicStyle.alpha,
      conditionVisibleDomain
    )
    configPointConditionStyle(pointMark)
    // 辅助线作为面积水平切色的视觉参考，和条件色使用同一透明度
    options.children.push(...getLineConditionLineYMarks(chart, threshold, basicStyle.alpha))
    return options
  }

  protected configXAxis(chart: Chart, options: G2Spec): G2Spec {
    const { xAxis } = parseJson(chart.customStyle)
    if (!xAxis.show) {
      const axisHide = {
        axis: {
          x: false
        }
      }
      return defaultsDeep(options, axisHide)
    }
    let lineLineDash = undefined
    if (xAxis.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (xAxis.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    let gridLineDash = [0, 0]
    if (xAxis.splitLine.lineStyle.style === 'dashed') {
      gridLineDash = [10, 8]
    }
    if (xAxis.splitLine.lineStyle.style === 'dotted') {
      gridLineDash = [1, 2]
    }
    const axisStyle = {
      axis: {
        x: {
          position: xAxis.position,
          title: xAxis.nameShow === false ? false : xAxis.name,
          titleFontSize: xAxis.fontSize,
          titleFill: xAxis.color,
          ...this.getAxisLineStyle(chart, xAxis),
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelFill: xAxis.axisLabel.color,
          labelOpacity: 1,
          labelFillOpacity: 1,
          labelFontSize: xAxis.axisLabel.fontSize,
          grid: xAxis.splitLine.show,
          gridStroke: xAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: xAxis.splitLine.lineStyle.width,
          gridLineDash,
          ...this.getAxisLabelStyle(xAxis)
        }
      }
    }
    return defaultsDeep(options, axisStyle)
  }

  protected configYAxis(chart: Chart, options: G2Spec): G2Spec {
    const { xAxis, yAxis } = parseJson(chart.customStyle)
    if (!yAxis.show) {
      const axisHide = {
        axis: {
          y: false
        }
      }
      return defaultsDeep(options, axisHide)
    }
    let lineLineDash = undefined
    if (yAxis.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (yAxis.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    let gridLineDash = [0, 0]
    if (yAxis.splitLine.lineStyle.style === 'dashed') {
      gridLineDash = [10, 8]
    }
    if (yAxis.splitLine.lineStyle.style === 'dotted') {
      gridLineDash = [1, 2]
    }
    const axisOption = {
      axis: {
        y: {
          position: yAxis.position,
          title: yAxis.nameShow === false ? false : yAxis.name,
          titleFontSize: yAxis.fontSize,
          titleFill: yAxis.color,
          ...this.getAxisLineStyle(chart, yAxis),
          lineLineDash,
          label: yAxis.axisLabel.show,
          labelFill: yAxis.axisLabel.color,
          labelOpacity: 1,
          labelFillOpacity: 1,
          labelFontSize: yAxis.axisLabel.fontSize,
          grid: yAxis.splitLine.show,
          gridStroke: yAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: yAxis.splitLine.lineStyle.width,
          gridLineDash,
          ...this.getOverlapGridFilter(xAxis),
          ...this.getAxisLabelStyle(yAxis),
          labelFormatter: d => {
            return valueFormatter(d, yAxis.axisLabelFormatter)
          }
        }
      }
    }
    if (!yAxis.axisValue.auto) {
      const scaleOpt = {
        scale: {
          y: {
            nice: false,
            domainMin: yAxis.axisValue.min,
            domainMax: yAxis.axisValue.max,
            tickCount: yAxis.axisValue.splitCount,
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
      }
      defaultsDeep(axisOption, scaleOpt)
      const result = defaultsDeep(options, axisOption)
      if (result.scale?.y) {
        result.scale.y.nice = false
      }
      return result
    }
    return defaultsDeep(options, axisOption)
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpGroupSeriesColor(chart, data)
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    const baseLegend = this.getLegend(chart, 2)
    const tmpLegend = {
      legend: {
        color: {
          // 面积与堆叠折线沿用基础柱状图的分页按钮尺寸
          ...baseLegend
        }
      }
    }
    defaultsDeep(options, tmpLegend)
    return configYAxisSeriesLegendDomain(chart, options)
  }

  protected configAssistLine(chart: Chart, options: G2Spec): G2Spec {
    const { assistLineCfg } = parseJson(chart.senior)
    if (!assistLineCfg.enable || !assistLineCfg.assistLine?.length) {
      return options
    }
    const lineData = []
    const { yAxis } = parseJson(chart.customStyle)
    const position = yAxis.position === 'left' ? 'left' : 'right'
    const axisFormatterCfg = yAxis.axisLabelFormatter ?? DEFAULT_YAXIS_STYLE.axisLabelFormatter
    const dynamicFields = []
    assistLineCfg.assistLine?.forEach(item => {
      // 固定值
      if (item.field === '0') {
        lineData.push(item)
      }
      // 动态值
      if (item.field === '1') {
        dynamicFields.push(item.fieldId)
      }
    })
    chart.data.dynamicAssistLines?.forEach(item => {
      if (dynamicFields.includes(item.fieldId)) {
        lineData.push({ ...item, value: parseFloat(item.value) })
      }
    })
    if (lineData.length) {
      const randomAssistColorScale = randomString(6)
      const assistLineMark: G2Spec = {
        type: 'lineY',
        encode: { y: 'value', color: () => randomAssistColorScale },
        scale: {
          color: {
            independent: true
          }
        },
        // 只关闭辅助线独立比例尺的图例，避免影响系列图例
        legend: false,
        data: lineData,
        zIndex: 0,
        style: {
          stroke: d => d.color,
          lineDash: d => (d.lineType === 'solid' ? [] : d.lineType === 'dashed' ? [10, 8] : [1, 2]),
          opacity: 1
        },
        labels: [
          {
            text: d => {
              const value = valueFormatter(parseFloat(d.value), axisFormatterCfg)
              return d.name ? `${d.name}: ${value}` : value
            },
            style: {
              fontSize: d => parseInt(d.fontSize),
              fill: d => d.color,
              fillOpacity: 1
            },
            position: position,
            textBaseline: 'bottom',
            transform: [{ type: 'overlapHide' }, { type: 'exceedAdjust' }],
            fontFamily: chart.fontFamily
          }
        ]
      }
      options.children.push(assistLineMark)
    }
    return options
  }

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const lineMark = options.children[1]
    if (!tooltipAttr.show) {
      defaultsDeep(lineMark, { tooltip: false })
      return options
    }
    const formatterMap = getSeriesTooltipFormatterMap(tooltipAttr)
    const yAxis = chart.yAxis
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          crosshairsLineDash: [4, 4],
          // 关闭 G2 tooltip 悬浮 marker，仅保留辅助线
          marker: false,
          ...getTooltipCrosshairsStyle(chart),
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltipAttr),
          enterable: true,
          render: (_e, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            // G2 折线默认只返回当前命中的线段，按维度补齐同一 x 下的系列项
            const fullItems = getLineTooltipSameDimensionItems(
              options,
              customAttr,
              title,
              originalItems,
              context.legendState?.visibleSeries,
              color =>
                customAttr.basicStyle?.gradient ? setGradientColor(color, true, 270) : color
            )
            let tooltipItems = fullItems
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              // 只隐藏明确配置为不展示的字段，避免过期 formatter 漏掉新指标
              tooltipItems = fullItems.filter(item =>
                isTooltipItemShown(formatterMap, item, 'yAxis')
              )
            }
            const result = []
            const head = originalItems[0]
            sortTooltipItemsByYAxis(chart, tooltipItems).forEach(item => {
              if (item.value === null || item.value === undefined) {
                return
              }
              const formatter = getTooltipItemFormatter(formatterMap, item, yAxis, 'yAxis')
              const value = valueFormatter(
                item.value,
                formatter?.formatterCfg ?? tooltipAttr.tooltipFormatter
              )
              result.push({ ...item, name: item.category, value })
            })
            head.dynamicTooltipValue?.forEach(item => {
              const formatter = getSeriesTooltipFormatter(
                formatterMap,
                item.fieldId,
                chart.extTooltip
              )
              if (formatter && isSeriesTooltipFormatterShown(formatterMap, item.fieldId)) {
                const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
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
    defaultsDeep(lineMark, tooltipOptions)
    return options
  }

  protected configSlider(chart: Chart, options: G2Spec): G2Spec {
    const { functionCfg } = parseJson(chart.senior)
    if (!functionCfg?.sliderShow) {
      return options
    }
    const lineMark = options.children[1]
    const conditionVisibleDomain = lineMark[LINE_CONDITION_VISIBLE_DOMAIN_KEY]
    const yAxis = parseJson(chart.customStyle)?.yAxis
    const valueField = options.encode?.y
    // 面积图由 area、line、point 多个 mark 组成，缩略轴过滤维度时必须同步 x 域
    configDimensionSlider(lineMark, options.data, functionCfg, {
      dimensionField: options.encode?.x,
      interactionName: 'areaDimensionSliderFilter',
      syncChildren: true,
      sliderMarkIndex: 1,
      syncMarks: [options.children[0], ...options.children.slice(2)],
      ...(this.name === 'area' &&
        yAxis?.axisValue?.auto !== false &&
        typeof valueField === 'string' && {
          valueScale: { field: valueField, includeZero: true }
        }),
      // 条件渐变需要和缩略轴可见维度域保持一致
      onSelectedDomainChange: domain => {
        if (conditionVisibleDomain) {
          conditionVisibleDomain.values = domain
        }
      }
    })
    return options
  }

  protected configEmptyDataStrategy(chart: Chart, options: G2Spec): G2Spec {
    const { functionCfg } = parseJson(chart.senior)
    const { emptyDataStrategy } = functionCfg
    const [areaMark, lineMark] = options.children
    const data = options.data.value
    const multiDimension = chart.yAxis?.length > 1
    switch (emptyDataStrategy) {
      case 'breakLine': {
        if (multiDimension) {
          handleBreakLineMultiDimension(data)
        }
        merge(areaMark, { style: { connect: false } })
        merge(lineMark, { style: { connect: false } })
        break
      }
      case 'ignoreData': {
        handleIgnoreData(data)
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

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configEmptyDataStrategy,
      this.configColor,
      this.configLabel,
      this.configBasicStyle,
      this.configConditions,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAssistLine,
      this.configTooltip,
      this.configSlider
    )(chart, options, context, this)
  }

  constructor(name = 'area') {
    super(name, DEFAULT_DATA)
  }
}

/**
 * 堆叠面积图
 */
export class StackArea extends Area {
  propertyInner = {
    ...this['propertyInner'],
    'label-selector': ['vPosition', 'fontSize', 'color', 'labelFormatter'],
    'tooltip-selector': ['fontSize', 'color', 'tooltipFormatter', 'show', 'carousel']
  }
  axisConfig = {
    ...this['axisConfig'],
    extStack: {
      name: `${t('chart.stack_item')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true
    }
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label: labelAttr } = parseJson(chart.customAttr)
    if (!labelAttr.show) {
      return options
    }
    if (labelAttr.showExtremum) {
      const { x: xField, y: yField, color: colorField } = options.encode
      addExtremumText(options.children, [], xField, yField, colorField, false)
    }
    const pointMark: G2Spec = options.children[2]
    const labelOpt = {
      labels: [
        {
          text: d => {
            if (d.extremum || d.value === null) {
              return ''
            }
            return valueFormatter(d.value, labelAttr.labelFormatter)
          },
          style: {
            opacity: 1,
            fillOpacity: 1,
            fontSize: d => {
              if (d.extremum) {
                return 0
              }
              return labelAttr.fontSize
            },
            fill: labelAttr.color,
            position: labelAttr.position
          },
          textBaseline: () => {
            return labelAttr.position === 'top' ? 'bottom' : 'top'
          },
          transform: labelAttr.fullDisplay
            ? [{ type: 'exceedAdjust', bounds: 'main' }]
            : [{ type: 'exceedAdjust', bounds: 'main' }, { type: 'overlapHide' }],
          fontFamily: chart.fontFamily
        }
      ]
    }
    defaultsDeep(pointMark, labelOpt)
    return options
  }

  public setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.senior.functionCfg.emptyDataStrategy = 'ignoreData'
    return chart
  }

  protected configColor(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const { seriesColor } = basicStyle
    if (!seriesColor?.length) {
      return options
    }
    const { xAxis, extStack, yAxis } = chart
    if (!xAxis?.length || !yAxis?.length) {
      return options
    }
    const relations = []
    const colorMap = seriesColor.reduce((pre, next) => {
      pre[next.id] = next.color
      return pre
    }, {})
    if (extStack?.length) {
      Object.entries(colorMap).forEach(([k, v]) => {
        let color = hexColorToRGBA(v, basicStyle.alpha)
        if (basicStyle.gradient) {
          color = setGradientColor(color, true, 270)
        }
        relations.push([k, color])
      })
    } else {
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

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    return super.configLegend(chart, configStackOrderByYAxis(chart, options))
  }

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const lineMark = options.children[1]
    if (!tooltipAttr.show) {
      defaultsDeep(lineMark, { tooltip: false })
      return options
    }
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          crosshairsLineDash: [4, 4],
          // 关闭 G2 tooltip 悬浮 marker，仅保留辅助线
          marker: false,
          ...getTooltipCrosshairsStyle(chart),
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltipAttr),
          enterable: true,
          render: (e, { title, items }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const result = []
            // G2 折线默认只返回当前命中的线段，按维度补齐同一 x 下的系列项
            sortTooltipItemsByYAxis(
              chart,
              getLineTooltipSameDimensionItems(
                options,
                customAttr,
                title,
                items,
                context.legendState?.visibleSeries,
                color =>
                  customAttr.basicStyle?.gradient ? setGradientColor(color, true, 270) : color
              )
            ).forEach(item => {
              if (item.value === null || item.value === undefined) {
                return
              }
              const value = valueFormatter(item.value, tooltipAttr.tooltipFormatter)
              result.push({ ...item, name: item.category, value })
            })
            // tooltip 项按维度槽位分组，帮助区分堆叠面积明细
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
    defaultsDeep(lineMark, tooltipOptions)
    return options
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpStackSeriesColor(chart, data)
  }

  protected configEmptyDataStrategy(chart: Chart, options: G2Spec): G2Spec {
    const { functionCfg } = parseJson(chart.senior)
    const { emptyDataStrategy } = functionCfg
    const [areaMark, lineMark] = options.children
    const data = options.data.value
    const multiDimension = chart.yAxis?.length > 1 || chart.extStack?.length > 0
    switch (emptyDataStrategy) {
      case 'breakLine': {
        if (multiDimension) {
          handleBreakLineMultiDimension(data)
        }
        merge(areaMark, { style: { connect: false } })
        merge(lineMark, { style: { connect: false } })
        break
      }
      case 'ignoreData': {
        handleIgnoreData(data)
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

  protected configAssistLine(chart: Chart, options: G2Spec): G2Spec {
    const { assistLineCfg } = parseJson(chart.senior)
    if (!assistLineCfg.enable || !assistLineCfg.assistLine?.length) {
      return options
    }
    const lineData = []
    const { yAxis } = parseJson(chart.customStyle)
    const position = yAxis.position === 'left' ? 'left' : 'right'
    const axisFormatterCfg = yAxis.axisLabelFormatter ?? DEFAULT_YAXIS_STYLE.axisLabelFormatter
    const dynamicFields = []
    assistLineCfg.assistLine?.forEach(item => {
      // 固定值
      if (item.field === '0') {
        lineData.push(item)
      }
      // 动态值
      if (item.field === '1') {
        dynamicFields.push(item.fieldId)
      }
    })
    chart.data.dynamicAssistLines?.forEach(item => {
      if (dynamicFields.includes(item.fieldId)) {
        lineData.push({ ...item, value: parseFloat(item.value) })
      }
    })
    if (lineData.length) {
      const randomAssistColorScale = randomString(6)
      lineData.forEach(line => {
        const value = valueFormatter(parseFloat(line.value), axisFormatterCfg)
        const assistLineMark: G2Spec = {
          type: 'lineY',
          encode: { y: 'value', color: () => randomAssistColorScale },
          scale: {
            color: {
              independent: true
            }
          },
          // 堆叠面积图也只关闭辅助线自身的图例
          legend: false,
          data: [line],
          zIndex: 0,
          style: {
            stroke: line.color,
            lineDash:
              line.lineType === 'solid' ? [] : line.lineType === 'dashed' ? [10, 8] : [1, 2],
            opacity: 1
          },
          labels: [
            {
              text: line.name ? `${line.name}: ${value}` : value,
              style: {
                fontSize: parseInt(line.fontSize),
                fill: line.color,
                fillOpacity: 1
              },
              position: position,
              textBaseline: 'bottom',
              transform: [{ type: 'overlapHide' }, { type: 'exceedAdjust' }],
              fontFamily: chart.fontFamily
            }
          ]
        }
        options.children.push(assistLineMark)
      })
    }
    return options
  }

  constructor() {
    super('area-stack')
    this.baseOptions = {
      ...this.baseOptions,
      transform: [{ type: 'stackY', orderBy: 'series' }]
    }
    delete this.propertyInner.threshold
    this.properties = this.properties.filter(item => item !== 'threshold')
    this.axis.push('extStack')
  }
}
