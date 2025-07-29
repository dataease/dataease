import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { cloneDeep, defaultsDeep, isEmpty, merge } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import {
  DEFAULT_BASIC_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import { setGradientColor, TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'
import { CHART_MIX_EDITOR_PROPERTY, CHART_MIX_EDITOR_PROPERTY_INNER } from './common'

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

    // options
    const initOptions: G2Spec = {
      type: 'view',
      autoFit: true,
      children: [
        {
          type: 'interval',
          data: {
            type: 'inline',
            value: left.data,
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
          data: right.data,
          encode: {
            x: 'field',
            y: 'value',
            series: 'category',
            color: 'category'
          },
          scale: {
            y: {
              key: 'right',
              nice: true
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
          data: right.data,
          encode: {
            x: 'field',
            y: 'value',
            color: 'category'
          },
          scale: {
            y: {
              key: 'right'
            }
          },
          tooltip: false
        }
      ]
    }
    const newChart = new G2Chart({ container })
    const options = this.setupOptions(chart, initOptions, { chartObj: newChart })

    newChart.on('point:click', action)
    newChart.on('interval:click', action)
    newChart.options(options)
    // extremumEvt(newChart, chart, options, container)
    // configPlotTooltipEvent(chart, newChart)
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
        lineWidth: 0
      }
    })
    return options
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    const baseLegend = this.getLegend(chart)
    const tmpLegend = {
      legend: {
        color: {
          ...baseLegend,
          itemMarkerSize: legend.size,
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
          transform: label.fullDisplay
            ? [{ type: 'exceedAdjust' }]
            : [{ type: 'overlapHide' }, { type: 'overlapDodgeY' }, { type: 'exceedAdjust' }],
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
    const formatterMap = tooltip.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    let g2TooltipWrapper = document.getElementById('G2-TOOLTIP-WRAPPER')
    if (!g2TooltipWrapper) {
      g2TooltipWrapper = document.createElement('div')
      g2TooltipWrapper.id = 'G2-TOOLTIP-WRAPPER'
      g2TooltipWrapper.style.position = 'absolute'
      g2TooltipWrapper.style.pointerEvents = 'none'
      g2TooltipWrapper.style.zIndex = '9999'
      document.body.appendChild(g2TooltipWrapper)
    }
    const yAxis = chart.yAxis
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
          crosshairsLineDash: [4, 4],
          mount: g2TooltipWrapper,
          css: {
            '.g2-tooltip': {
              background: tooltip.backgroundColor
            },
            '.g2-tooltip-title': {
              color: tooltip.color,
              'font-size': `${tooltip.fontSize}px`
            },
            '.g2-tooltip-list-item-name-label': {
              color: tooltip.color,
              'font-size': `${tooltip.fontSize}px`
            },
            '.g2-tooltip-list-item-value': {
              color: tooltip.color,
              'font-size': `${tooltip.fontSize}px`
            }
          },
          render: (_, { title, items }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            if (tooltip.seriesTooltipFormatter?.length) {
              items = items.filter(i => formatterMap[i.quotaList[0].id])
            }
            const result = []
            const [view] = chartObj.getContext().views
            items.forEach(item => {
              const formatterCfg =
                formatterMap[item.quotaList[0].id]?.formatterCfg ?? yAxis[0].formatterCfg
              const value = valueFormatter(item.value, formatterCfg)
              const name = item.category
              const colorScale = item.left ? view.scale.color : view.scale.color1
              const color = colorScale.map(name) ?? item.color
              result.push({ value, color, name })
            })
            const itemsHtml = result
              .map(item => {
                const marker = item.color
                const label = item.name
                const value = item.value
                return TOOLTIP_ITEM_TPL.replace('{marker}', marker)
                  .replace('{label}', label)
                  .replace('{value}', value)
              })
              .join('')
            const listHtml = `<ul class="g2-tooltip-list" style="margin: 0px; list-style-type: none; padding: 0px;">${itemsHtml}</ul>`
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
    let lineLineDash = undefined
    if (xAxis.axisLine.lineStyle.style === 'dashed') {
      lineLineDash = [10, 8]
    }
    if (xAxis.axisLine.lineStyle.style === 'dotted') {
      lineLineDash = [1, 2]
    }
    let gridLineDash = undefined
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
          line: xAxis.axisLine.show,
          lineStroke: xAxis.axisLine.lineStyle.color,
          lineStrokeOpacity: 1,
          lineLineWidth: xAxis.axisLine.lineStyle.width,
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelFill: xAxis.axisLabel.color,
          labelFillOpacity: 1,
          labelFontSize: xAxis.axisLabel.fontSize,
          grid: xAxis.splitLine.show,
          gridStroke: xAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: xAxis.splitLine.lineStyle.width,
          gridLineDash,
          transform: xAxis.axisLabel.rotate
            ? [
                {
                  type: 'rotate',
                  optionalAngles: [xAxis.axisLabel.rotate],
                  recoverWhenFailed: false
                }
              ]
            : []
        }
      }
    }
    return defaultsDeep(options, axisStyle)
  }

  protected configYAxis(chart: Chart, options: G2Spec): G2Spec {
    const { yAxis, yAxisExt } = parseJson(chart.customStyle)
    const [intervalMark, lineMark, pointMark] = options.children
    if (!yAxis.show) {
      intervalMark.axis.y = false
      lineMark.axis.y = false
      return options
    }
    const yAxisOption = this.getAxis(yAxis)
    const yAxisExtOption = this.getAxis(yAxisExt)
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
      merge(intervalMark, {
        scale: {
          y: {
            domain: [yAxis.axisValue.min, yAxis.axisValue.max]
          }
        },
        axis: {
          y: {
            tickCount: yAxis.axisValue.splitCount,
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
      const scaleOpt = {
        scale: {
          y: {
            independent: true,
            domain: [yAxisExt.axisValue.min, yAxisExt.axisValue.max]
          }
        }
      }
      merge(lineMark, scaleOpt, {
        axis: {
          y: {
            tickCount: yAxisExt.axisValue.splitCount,
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
      merge(pointMark, scaleOpt)
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
      const lineData = splitLineData[item.yAxisType === 'left' ? 0 : 1]
      const dynamicFields = splitDynamicFields[item.yAxisType === 'left' ? 0 : 1]
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
      const fields = d.yAxisType === 'left' ? splitDynamicFields[0] : splitDynamicFields[1]
      if (fields.includes(d.fieldId)) {
        splitLineData[d.yAxisType === 'left' ? 0 : 1].push({ ...d, value: parseFloat(d.value) })
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
          scale: {
            y: {
              key: index === 0 ? 'left' : 'right'
            }
          },
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
    const { customAttr, senior } = chart
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

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
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
