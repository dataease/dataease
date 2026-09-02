import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  handleBreakLineMultiDimension,
  handleSetZeroMultiDimension,
  handleSetZeroSingleDimension,
  hexColorToRGBA,
  parseJson
} from '@/views/chart/components/js/util'
import { cloneDeep, defaultsDeep, isEmpty, merge } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import {
  DEFAULT_BASIC_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import {
  getG2Renderer,
  getTooltipCrosshairsStyle,
  handleChartDashboardHidden,
  setGradientColor,
  toLinearGradient,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'
import {
  CHART_MIX_EDITOR_PROPERTY,
  CHART_MIX_EDITOR_PROPERTY_INNER,
  filterValidMixTooltipItems,
  getAssistLineAxisIndex,
  getMixLabelTransform,
  MixG2Chart
} from './common'
import G2TooltipCarousel from '@/views/chart/components/js/G2TooltipCarousel'
import {
  bindPlotBackgroundClick,
  createTooltipWrapper,
  getBackgroundInteractionState,
  getSeriesIndexMapByRelations,
  getMixTooltipGroupIndex,
  getMixTooltipGroupName,
  renderGroupedTooltipItems,
  sortMixTooltipItems,
  tooltipCss,
  tooltipMaxHeight
} from '@/views/chart/components/js/panel/charts/g2/bar/barUtil'

const { t } = useI18n()
/**
 * 柱线混合图
 */
export class ColumnLineMix extends G2ChartView {
  properties: EditorProperty[] = CHART_MIX_EDITOR_PROPERTY
  propertyInner: EditorPropertyInner = {
    ...CHART_MIX_EDITOR_PROPERTY_INNER,
    'label-selector': ['vPosition', 'seriesLabelFormatter'],
    'tooltip-selector': [
      'fontSize',
      'color',
      'backgroundColor',
      'show',
      'seriesTooltipFormatter',
      'carousel'
    ]
  }

  axis: AxisType[] = [
    'xAxis',
    'yAxis',
    'drill',
    'filter',
    'extLabel',
    'extTooltip',
    'xAxisExtRight',
    'yAxisExt'
  ]

  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis_left')} / ${t('chart.column_quota')}`,
      limit: 1,
      type: 'q'
    },
    extBubble: {
      //用这个字段存放右轴分类
      name: `${t('chart.drag_block_type_axis_right')} / ${t('chart.dimension')}`,
      limit: 1,
      type: 'd',
      allowEmpty: true
    },
    yAxisExt: {
      name: `${t('chart.drag_block_value_axis_right')} / ${t('chart.line_quota')}`,
      limit: 1,
      type: 'q',
      allowEmpty: true
    }
  }

  protected getLeftType(): string {
    return 'column'
  }
  protected getRightType(): string {
    return 'line'
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, action, container } = drawOptions
    chart.container = container
    if (!chart.data?.left?.data?.length && !chart.data?.right?.data?.length) {
      return
    }
    const [left] = cloneDeep(chart.data?.left?.data)
    const [right] = cloneDeep(chart.data?.right?.data)
    const leftData = left?.data || []
    const rightData = right?.data || []
    // options
    const initOptions: G2Spec = {
      type: 'view',
      autoFit: true,
      children: [
        {
          type: 'interval',
          state: getBackgroundInteractionState(chart),
          data: {
            type: 'inline',
            value: leftData,
            transform: [
              {
                type: 'map',
                callback: d => ({ ...d, left: true })
              }
            ]
          },
          encode: {
            x: 'field',
            y: 'value',
            color: {
              type: 'transform',
              value: () => chart.yAxis[0]?.chartShowName ?? chart.yAxis[0]?.name
            }
          },
          axis: {
            y: {
              position: 'left'
            }
          },
          scale: {
            y: {
              key: 'left',
              nice: true
            }
          }
        },
        {
          type: 'line',
          state: getBackgroundInteractionState(),
          data: rightData,
          encode: {
            x: 'field',
            y: 'value',
            series: 'category',
            color: 'category'
          },
          scale: {
            y: {
              key: 'right',
              nice: true,
              independent: true
            }
          },
          axis: {
            y: {
              position: 'right'
            }
          }
        },
        {
          type: 'point',
          state: getBackgroundInteractionState(),
          data: rightData,
          encode: {
            x: 'field',
            y: 'value',
            color: 'category'
          },
          scale: {
            y: {
              key: 'right',
              nice: true,
              independent: true
            }
          },
          axis: {
            y: false
          },
          style: {
            stroke: 'white'
          },
          tooltip: false
        }
      ],
      interaction: {
        elementHighlight: {
          background: true,
          region: true
        },
        elementSelect: {
          background: true,
          single: true
        }
      }
    }
    // 注入公共渲染器配置以响应 SVG 渲染开关
    const newChart = new MixG2Chart({ container, ...getG2Renderer() })
    const options = this.setupOptions(chart, initOptions, {
      chartObj: newChart,
      leftData,
      rightData
    })

    newChart.on('point:click', action)
    newChart.on('interval:click', action)
    bindPlotBackgroundClick(newChart, { markTypes: ['interval', 'point'] })
    handleChartDashboardHidden(chart, options)
    newChart.options(options)
    new G2TooltipCarousel(newChart, chart, [...leftData, ...rightData]).start()
    return newChart
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    let leftColor = hexColorToRGBA(basicStyle.colors?.[0], basicStyle.alpha)
    const leftSeriesMap = basicStyle.seriesColor?.find(c => c.id === chart.yAxis[0]?.id)
    if (leftSeriesMap) {
      leftColor = hexColorToRGBA(leftSeriesMap.color, basicStyle.alpha)
    }
    merge(options, {
      scale: {
        color: {
          type: 'ordinal',
          relations: [[chart.yAxis[0]?.chartShowName ?? chart.yAxis[0]?.name, leftColor]]
        }
      }
    })
    if (basicStyle.subSeriesColor?.length) {
      const { yAxisExt, extBubble } = chart
      const relations = [options.scale?.color?.relations?.[0]]
      if (extBubble?.length) {
        basicStyle.subSeriesColor.reduce((acc, cur) => {
          acc[cur.id] = cur.color
          return acc
        }, {})
        basicStyle.subSeriesColor.forEach(c =>
          relations.push([c.id, hexColorToRGBA(c.color, basicStyle.subAlpha)])
        )
      } else {
        const rightColor = basicStyle.subSeriesColor.find(c => c.id === yAxisExt[0]?.id)?.color
        if (rightColor) {
          relations.push([
            yAxisExt[0]?.chartShowName ?? yAxisExt[0]?.name,
            hexColorToRGBA(rightColor, basicStyle.subAlpha)
          ])
        }
      }
      merge(options, {
        scale: {
          color: {
            relations
          }
        }
      })
    }
    const colors = basicStyle.subColors.map(c => hexColorToRGBA(c, basicStyle.subAlpha))
    merge(options, {
      scale: {
        color: {
          range: colors
        }
      }
    })
    const [intervalMark, lineMark, pointMark] = options.children
    if (basicStyle.gradient) {
      leftColor = setGradientColor(leftColor, true, 270)
    }
    merge(intervalMark, {
      style: {
        fill: leftColor,
        columnWidthRatio: basicStyle.columnWidthRatio / 100
      }
    })
    if (basicStyle.radiusColumnBar === 'roundAngle') {
      merge(intervalMark, {
        style: {
          radius: 20
        }
      })
    }
    if (basicStyle.radiusColumnBar === 'topRoundAngle') {
      merge(intervalMark, {
        style: {
          radiusTopLeft: 20,
          radiusTopRight: 20
        }
      })
    }
    merge(lineMark, {
      style: {
        lineWidth: basicStyle.lineWidth
      },
      encode: {
        shape: basicStyle.lineSmooth ? 'smooth' : 'line'
      }
    })
    merge(pointMark, {
      encode: {
        shape: basicStyle.lineSymbol,
        size: basicStyle.lineSymbolSize ? basicStyle.lineSymbolSize : 0.01
      },
      style: {
        opacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        fillOpacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        strokeOpacity: basicStyle.lineSymbolSize === 0 ? 0 : 1,
        lineWidth: basicStyle.lineSymbolSize === 0 ? 0 : 1
      }
    })
    return options
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    const baseLegend = this.getLegend(chart, 2)
    const horizontalLegend = ['top', 'bottom'].includes(baseLegend['position'])
    const legendFontSize = Number(legend.fontSize) > 0 ? Number(legend.fontSize) : 12
    const legendMarkerSize = Number(legend.size) > 0 ? Number(legend.size) * 2 : 8
    const legendItemHeight = Math.ceil(Math.max(legendFontSize * 1.3, legendMarkerSize))
    const tmpLegend = {
      legend: {
        color: {
          ...baseLegend,
          // 横向图例在布局前同步声明行高，避免放大文字或图标后上下被裁剪
          ...(horizontalLegend ? { size: legendItemHeight } : {}),
          itemMarker: legend.icon
        }
      }
    }
    defaultsDeep(options, tmpLegend)
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      return options
    }
    const seriesMap = label.seriesLabelFormatter?.reduce((acc, cur) => {
      acc[cur.id] = cur
      return acc
    }, {})
    const labelOpt = {
      labels: [
        {
          text: d => {
            if (!label.seriesLabelFormatter?.length) {
              return d.value
            }
            const labelCfg = seriesMap?.[d.quotaList[0].id] as SeriesFormatter
            if (!labelCfg) {
              return d.value
            }
            if (!labelCfg.show) {
              return ''
            }
            return valueFormatter(d.value, labelCfg.formatterCfg)
          },
          style: {
            fillOpacity: 1,
            fontSize: d => {
              if (!label.seriesLabelFormatter?.length) {
                return 12
              }
              const labelCfg = seriesMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg) {
                return 12
              }
              if (!labelCfg.show) {
                return 0
              }
              return labelCfg.fontSize
            },
            fill: d => {
              if (!label.seriesLabelFormatter?.length) {
                return 'black'
              }
              const labelCfg = seriesMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg?.show) {
                return 'black'
              }
              return labelCfg.color
            },
            position: label.position === 'middle' ? 'inside' : label.position
          },
          textBaseline: {
            top: 'bottom',
            middle: 'middle',
            bottom: 'top'
          }[label.position],
          transform: getMixLabelTransform(label.fullDisplay),
          fontFamily: chart.fontFamily
        }
      ]
    }
    const [intervalMark, _, pointMark] = options.children
    if (!label.seriesLabelFormatter?.length) {
      defaultsDeep(intervalMark, labelOpt)
      defaultsDeep(pointMark, labelOpt)
    } else {
      const showLeft = label.seriesLabelFormatter.some(c => c.id === chart.yAxis[0]?.id && c.show)
      const showRight = label.seriesLabelFormatter.some(
        c => c.id === chart.yAxisExt[0]?.id && c.show
      )
      if (showLeft) {
        defaultsDeep(intervalMark, labelOpt)
      }
      if (showRight) {
        defaultsDeep(pointMark, labelOpt)
      }
    }
    return options
  }

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { tooltip } = parseJson(chart.customAttr)
    const [intervalMark, lineMark] = options.children
    if (!tooltip.show) {
      defaultsDeep(intervalMark, { tooltip: false })
      defaultsDeep(lineMark, { tooltip: false })
      return options
    }
    const chartObj = context.chartObj as G2Chart
    const formatterMap = (tooltip.seriesTooltipFormatter || []).reduce((pre, next) => {
      pre[next.id] = next
      return pre
    }, {}) as Record<string, SeriesFormatter>
    const sourceTooltipItems = [
      ...(context.leftData || []).map(item => ({ ...item, left: true })),
      ...(context.rightData || [])
    ]
    const quotaIdOf = item => item?.quotaList?.[0]?.id
    const completeTooltipItems = (title, items = []) => {
      if (filterValidMixTooltipItems(items).length) {
        return items
      }
      // 柱线 shared tooltip 未返回有效指标时，仅按当前维度读取左右轴源数据
      return sourceTooltipItems.filter(item => item.field === title)
    }
    const yAxis = chart.yAxis
    // 读取 color relations，保持 tooltip 系列顺序与图例一致
    const seriesIndexMap = getSeriesIndexMapByRelations(options.scale?.color?.relations)
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        ...options.interaction,
        tooltip: {
          crosshairsLineDash: [4, 4],
          ...getTooltipCrosshairsStyle(chart),
          mount: createTooltipWrapper(chart),
          css: tooltipCss(tooltip),
          enterable: true,
          marker: false,
          render: (_, { title, items }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            items = completeTooltipItems(title, items)
            if (tooltip.seriesTooltipFormatter?.length) {
              items = items.filter(item => formatterMap[quotaIdOf(item)]?.show !== false)
            }
            items = filterValidMixTooltipItems(items)
            const result = []
            const [view] = chartObj.getContext().views
            items.forEach(item => {
              const formatterCfg =
                formatterMap[quotaIdOf(item)]?.formatterCfg ?? yAxis[0].formatterCfg
              const value = valueFormatter(item.value, formatterCfg)
              const name = item.category
              const colorScale = item.left ? view?.scale?.color : view?.scale?.color1
              const color = colorScale?.map?.(name) ?? item.color
              // 记录 tooltip 项所属维度分组，供后续分组标题和排序使用
              result.push({
                value,
                color,
                name,
                groupName: getMixTooltipGroupName(chart, item),
                groupIndex: getMixTooltipGroupIndex(chart, item)
              })
            })
            // 混合图 tooltip 先按维度分组，再按图例系列顺序排列
            sortMixTooltipItems(result, seriesIndexMap)
            const itemsHtml = renderGroupedTooltipItems(
              result,
              item => item.groupName,
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
    defaultsDeep(intervalMark, { tooltip: d => d })
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
    let lineLineDash = [0, 0]
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
          title: xAxis.nameShow === false || isEmpty(xAxis.name) ? false : xAxis.name,
          titleFontSize: xAxis.fontSize,
          titleFill: xAxis.color,
          ...this.getAxisLineStyle(chart, xAxis),
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelOpacity: 1,
          labelFill: xAxis.axisLabel.color,
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
    const { xAxis, yAxis, yAxisExt } = parseJson(chart.customStyle)
    const [intervalMark, lineMark, pointMark] = options.children
    if (!yAxis.show) {
      intervalMark.axis.y = false
      lineMark.axis.y = false
      return options
    }
    const overlapGridFilter = this.getOverlapGridFilter(xAxis)
    const yAxisOption = { ...this.getAxis(chart, yAxis), ...overlapGridFilter }
    const yAxisExtOption = { ...this.getAxis(chart, yAxisExt), ...overlapGridFilter }
    merge(intervalMark, {
      axis: {
        y: {
          ...yAxisOption,
          position: 'left'
        }
      }
    })
    merge(lineMark, {
      axis: {
        y: {
          ...yAxisExtOption,
          position: 'right'
        }
      }
    })
    if (yAxis.axisValue.auto === false) {
      const n = Math.max(2, yAxis.axisValue.splitCount)
      intervalMark.scale.y = {
        key: 'left',
        nice: false,
        clamp: true,
        domain: [yAxis.axisValue.min, yAxis.axisValue.max]
      }
      merge(intervalMark, {
        axis: {
          y: {
            tickCount: n,
            tickMethod: (min, max, count) => {
              const step = (max - min) / (count - 1)
              const ticks = []
              for (let i = 0; i < count; i++) {
                ticks.push(min + i * step)
              }
              return ticks
            }
          }
        }
      })
    }
    if (yAxisExt.axisValue.auto === false) {
      const n = Math.max(2, yAxisExt.axisValue.splitCount)
      const scaleY = {
        key: 'right',
        nice: false,
        clamp: true,
        independent: true,
        domain: [yAxisExt.axisValue.min, yAxisExt.axisValue.max]
      }
      lineMark.scale.y = scaleY
      pointMark.scale.y = scaleY
      merge(lineMark, {
        axis: {
          y: {
            tickCount: n,
            tickMethod: (min, max, count) => {
              const step = (max - min) / (count - 1)
              const ticks = []
              for (let i = 0; i < count; i++) {
                ticks.push(min + i * step)
              }
              return ticks
            }
          }
        }
      })
    }
    return options
  }

  protected configAssistLine(chart: Chart, options: G2Spec): G2Spec {
    const { assistLineCfg } = parseJson(chart.senior)
    if (!assistLineCfg.enable || !assistLineCfg.assistLine?.length) {
      return options
    }
    const splitLineData = [[], []]
    const splitDynamicFields = [[], []]
    assistLineCfg.assistLine?.forEach(item => {
      const axisIndex = getAssistLineAxisIndex(item.yAxisType)
      const lineData = splitLineData[axisIndex]
      const dynamicFields = splitDynamicFields[axisIndex]
      // 固定值
      if (item.field === '0') {
        lineData.push({ ...item, value: parseFloat(item.value) })
      }
      // 动态值
      if (item.field === '1') {
        dynamicFields.push(item.fieldId)
      }
    })
    const assistLineData = [
      ...(chart.data.left.dynamicAssistLines ?? []),
      ...(chart.data.right.dynamicAssistLines ?? [])
    ]
    assistLineData.forEach(d => {
      const axisIndex = getAssistLineAxisIndex(d.yAxisType)
      const fields = splitDynamicFields[axisIndex]
      if (fields.includes(d.fieldId)) {
        splitLineData[axisIndex].push({ ...d, value: parseFloat(d.value) })
      }
    })
    const { yAxis, yAxisExt } = parseJson(chart.customStyle)
    const yAxisFormatterCfg = yAxis.axisLabelFormatter ?? DEFAULT_YAXIS_STYLE.axisLabelFormatter
    const yAxisExtFormatterCfg =
      yAxisExt.axisLabelFormatter ?? DEFAULT_YAXIS_STYLE.axisLabelFormatter
    splitLineData.forEach((lineData, index) => {
      if (lineData.length) {
        const assistLineMark: G2Spec = {
          type: 'lineY',
          encode: { y: 'value' },
          // 组合图辅助线不参与图例过滤和分页
          legend: false,
          scale: {
            y: {
              key: index === 0 ? 'left' : 'right'
            }
          },
          // 右轴辅助线使用独立比例尺，只关闭其自动生成的冗余轴
          ...(index === 1 ? { axis: { y: false } } : {}),
          data: lineData,
          style: {
            stroke: d => d.color,
            lineDash: d =>
              d.lineType === 'solid' ? [] : d.lineType === 'dashed' ? [10, 8] : [1, 2],
            opacity: 1
          },
          labels: [
            {
              text: d => {
                const value = valueFormatter(
                  parseFloat(d.value),
                  index === 0 ? yAxisFormatterCfg : yAxisExtFormatterCfg
                )
                return d.name ? `${d.name}: ${value}` : value
              },
              style: {
                fontSize: d => parseInt(d.fontSize),
                fill: d => d.color,
                fillOpacity: 1
              },
              textBaseline: 'bottom',
              position: index === 0 ? 'left' : 'right',
              transform: [{ type: 'overlapHide' }, { type: 'exceedAdjust' }],
              fontFamily: chart.fontFamily
            }
          ]
        }
        options.children.push(assistLineMark)
      }
    })
    return options
  }

  public setupDefaultOptions(chart: ChartObj): ChartObj {
    const { senior } = chart
    if (
      senior.functionCfg.emptyDataStrategy == undefined ||
      senior.functionCfg.emptyDataStrategy === 'ignoreData'
    ) {
      senior.functionCfg.emptyDataStrategy = 'breakLine'
    }
    return chart
  }

  public setupSubSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    const result: ChartBasicStyle['seriesColor'] = []
    const seriesSet = new Set<string>()
    const colors = chart.customAttr.basicStyle.subColors ?? CHART_MIX_DEFAULT_BASIC_STYLE.subColors
    const { yAxisExt, extBubble } = chart
    if (extBubble?.length) {
      data?.forEach(d => {
        if (d.value === null || d.category === null || seriesSet.has(d.category)) {
          return
        }
        seriesSet.add(d.category)
        result.push({
          id: d.category,
          name: d.category,
          color: colors[(seriesSet.size - 1) % colors.length]
        })
      })
    } else {
      yAxisExt?.forEach(axis => {
        if (seriesSet.has(axis.id)) {
          return
        }
        seriesSet.add(axis.id)
        result.push({
          id: axis.id,
          name: axis.chartShowName ?? axis.name,
          color: colors[(seriesSet.size - 1) % colors.length]
        })
      })
    }
    return result
  }

  protected configEmptyDataStrategy(chart: Chart, options: G2Spec): G2Spec {
    const { functionCfg } = parseJson(chart.senior)
    const { emptyDataStrategy } = functionCfg
    const [intervalMark, lineMark] = options.children
    const leftData = intervalMark.data?.value || []
    const rightData = lineMark.data || []
    const multiDimension = chart.extBubble?.length > 0
    switch (emptyDataStrategy) {
      case 'breakLine': {
        if (multiDimension) {
          handleBreakLineMultiDimension(rightData)
        }
        merge(lineMark, { style: { connect: false } })
        break
      }
      case 'setZero': {
        if (multiDimension) {
          // 多维度置0
          handleSetZeroMultiDimension(rightData)
        } else {
          // 单维度置0
          handleSetZeroSingleDimension(rightData)
        }
        handleSetZeroSingleDimension(leftData)
        break
      }
    }
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configEmptyDataStrategy,
      this.configBasicStyle,
      this.configLegend,
      this.configLabel,
      this.configTooltip,
      this.configXAxis,
      this.configYAxis,
      this.configAssistLine
    )(chart, options, context, this)
  }

  constructor(name = 'chart-mix') {
    super(name, [])
  }
}

export const CHART_MIX_DEFAULT_BASIC_STYLE = {
  ...DEFAULT_BASIC_STYLE,
  subAlpha: 100,
  subColorScheme: 'fast',
  subSeriesColor: [],
  subColors: [
    '#fae800',
    '#00c039',
    '#0482dc',
    '#bb9581',
    '#ff7701',
    '#9c5ec3',
    '#00ccdf',
    '#00c039',
    '#ff7701'
  ],
  leftLineWidth: 2,
  leftLineSymbol: 'circle',
  leftLineSymbolSize: 4,
  leftLineSmooth: true
}
