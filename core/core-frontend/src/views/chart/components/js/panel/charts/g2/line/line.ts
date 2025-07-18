import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  getLineConditions,
  getLineLabelColorByCondition,
  hexColorToRGBA,
  parseJson,
  setUpGroupSeriesColor
} from '@/views/chart/components/js/util'
import { cloneDeep, defaultsDeep, isEmpty } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { LINE_AXIS_TYPE, LINE_EDITOR_PROPERTY, LINE_EDITOR_PROPERTY_INNER } from './common'
import { useI18n } from '@/hooks/web/useI18n'
import { clearExtremum } from '@/views/chart/components/js/extremumUitl'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { DEFAULT_YAXIS_STYLE } from '@/views/chart/components/editor/util/chart'
import { TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'

const { t } = useI18n()
const DEFAULT_DATA = []
/**
 * 折线图
 */
export class Line extends G2ChartView {
  properties = LINE_EDITOR_PROPERTY
  propertyInner = {
    ...LINE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': [...LINE_EDITOR_PROPERTY_INNER['basic-style-selector'], 'seriesColor'],
    'label-selector': ['seriesLabelVPosition', 'seriesLabelFormatter', 'showExtremum'],
    'tooltip-selector': [
      ...LINE_EDITOR_PROPERTY_INNER['tooltip-selector'],
      'seriesTooltipFormatter',
      'carousel'
    ],
    'legend-selector': [...LINE_EDITOR_PROPERTY_INNER['legend-selector'], 'legendSort']
  }
  axis: AxisType[] = [...LINE_AXIS_TYPE, 'xAxisExt']
  axisConfig = {
    ...this['axisConfig'],
    xAxis: {
      name: `${t('chart.drag_block_type_axis')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    xAxisExt: {
      name: `${t('chart.chart_group')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1,
      allowEmpty: true
    },
    yAxis: {
      name: `${t('chart.drag_block_value_axis')} / ${t('chart.quota')}`,
      type: 'q'
    }
  }
  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, action, container } = drawOptions
    chart.container = container
    if (!chart.data?.data?.length) {
      clearExtremum(chart)
      return
    }
    const data = cloneDeep(chart.data.data)
    // options
    const initOptions: G2Spec = {
      type: 'view',
      data: {
        value: data
      },
      autoFit: true,
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
        { type: 'line', encode: { series: 'category' } },
        { type: 'point', tooltip: false }
      ]
    }
    const options = this.setupOptions(chart, initOptions)
    // 开始渲染
    const newChart = new G2Chart({ container })
    newChart.options(options)
    newChart.on('point:click', action)
    // extremumEvt(newChart, chart, options, container)
    // configPlotTooltipEvent(chart, newChart)
    return newChart
  }

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
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
    return { ...options, theme }
  }

  protected configColor(chart: Chart, options: G2Spec): G2Spec {
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
        relations.push([item.id, hexColorToRGBA(item.color, basicStyle.alpha)])
      })
    } else {
      const colorMap = seriesColor.reduce((pre, next) => {
        pre[next.id] = next.color
        return pre
      }, {})
      yAxis.forEach(item => {
        if (colorMap[item.id]) {
          relations.push([
            item.chartShowName ?? item.name,
            hexColorToRGBA(colorMap[item.id], basicStyle.alpha)
          ])
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
    const pointMark: G2Spec = options.children[1]
    const labelOpt = {
      labels: [
        {
          text: d => {
            if (d.EXTREME || d.value === null) {
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
            fontSize: d => {
              if (d.EXTREME) {
                return 0
              }
              if (!labelAttr.seriesLabelFormatter?.length) {
                return 12
              }
              const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg) {
                return 12
              }
              if (!labelCfg.show) {
                return 0
              }
              return labelCfg.fontSize
            },
            fill: d => {
              if (d.EXTREME || !labelAttr.seriesLabelFormatter?.length) {
                return 'black'
              }
              const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
              if (!labelCfg?.show) {
                return 'black'
              }
              const color =
                getLineLabelColorByCondition(conditions, d.value, d.quotaList[0].id) ||
                labelCfg.color
              return color
            },
            position: d => {
              if (d.EXTREME || !labelAttr.seriesLabelFormatter?.length) {
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
            if (d.EXTREME || !labelAttr.seriesLabelFormatter?.length) {
              return 'bottom'
            }
            const labelCfg = formatterMap?.[d.quotaList[0].id] as SeriesFormatter
            if (!labelCfg?.show) {
              return 'bottom'
            }
            return labelCfg.position === 'top' ? 'bottom' : 'top'
          },
          transform: labelAttr.fullDisplay
            ? []
            : [{ type: 'overlapHide' }, { type: 'exceedAdjust' }],
          fontFamily: chart.fontFamily
        }
      ]
    }
    defaultsDeep(pointMark, labelOpt)
    return options
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    // size
    const { basicStyle } = parseJson(chart.customAttr)
    const [lineMark, pointMark] = options.children
    const lineStyleOpt = {
      encode: {
        shape: basicStyle.lineSmooth ? 'smooth' : 'line',
        size: basicStyle.lineWidth
      }
    }
    defaultsDeep(lineMark, lineStyleOpt)
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
      }
    }
    defaultsDeep(pointMark, pointStyleOpt)
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
          title: xAxis.nameShow === false ? false : xAxis.name,
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
    const { yAxis } = parseJson(chart.customStyle)
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
          line: yAxis.axisLine.show,
          lineStroke: yAxis.axisLine.lineStyle.color,
          lineStrokeOpacity: 1,
          lineLineWidth: yAxis.axisLine.lineStyle.width,
          lineLineDash,
          label: yAxis.axisLabel.show,
          labelFill: yAxis.axisLabel.color,
          labelFillOpacity: 1,
          labelFontSize: yAxis.axisLabel.fontSize,
          grid: yAxis.splitLine.show,
          gridStroke: yAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: yAxis.splitLine.lineStyle.width,
          gridLineDash,
          transform: yAxis.axisLabel.rotate
            ? [
                {
                  type: 'rotate',
                  optionalAngles: [yAxis.axisLabel.rotate],
                  recoverWhenFailed: false
                }
              ]
            : [],
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
            domainMin: yAxis.axisValue.min,
            domainMax: yAxis.axisValue.max,
            tickCount: yAxis.axisValue.splitCount,
            tickMethod: (min, max, count) => {
              const step = (max - min) / count
              const ticks = []
              for (let i = 0; i <= count; i++) {
                ticks.push(min + step * i)
              }
              return ticks
            }
          }
        }
      }
      defaultsDeep(axisOption, scaleOpt)
    }
    return defaultsDeep(options, axisOption)
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpGroupSeriesColor(chart, data)
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const xAxisExt = chart.xAxisExt[0]
    if (xAxisExt?.customSort?.length > 0) {
      // 图例自定义排序
      const sort = xAxisExt.customSort ?? []
      if (sort?.length) {
        // 用值域限定排序，有可能出现新数据但是未出现在图表上，所以这边要遍历一下子维度，加到后面，让新数据显示出来
        const data = options.data.value
        const cats =
          data?.reduce((p, n) => {
            const cat = n['category']
            if (cat && !p.includes(cat)) {
              p.push(cat)
            }
            return p
          }, []) || []
        const values = sort.reduce((p, n) => {
          if (cats.includes(n)) {
            const index = cats.indexOf(n)
            if (index !== -1) {
              cats.splice(index, 1)
            }
            p.push(n)
          }
          return p
        }, [])
        cats.length > 0 && values.push(...cats)
        const scaleOpt = {
          scale: {
            color: {
              domain: values
            }
          }
        }
        defaultsDeep(options, scaleOpt)
      }
    }
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

    const customStyle = parseJson(chart.customStyle)
    const { sort, customSort } = customStyle.legend
    if (sort && sort !== 'none' && chart.xAxisExt.length) {
      const domain = options.scale?.color?.domain || []
      if (!domain?.length) {
        options.data.value.forEach(item => {
          if (item.category && !domain.includes(item.category)) {
            domain.push(item.category)
          }
        })
      }
      if (sort !== 'custom') {
        domain.sort((a, b) => {
          return sort !== 'desc' ? a.localeCompare(b) : b.localeCompare(a)
        })
        const scaleOpt = {
          scale: {
            color: {
              domain
            }
          }
        }
        defaultsDeep(options, scaleOpt)
      } else {
        if (!customSort?.length) {
          return options
        }
        const tmp = []
        customSort.forEach(item => {
          if (domain.includes(item)) {
            const index = domain.indexOf(item)
            const val = domain.splice(index, 1)
            tmp.push(val[0])
          }
        })
        const scaleOpt = {
          scale: {
            color: {
              domain: [...tmp, ...domain]
            }
          }
        }
        defaultsDeep(options, scaleOpt)
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
        lineData.push(item)
      }
    })
    let max, min
    options.data.value.forEach(item => {
      const value = item.value
      if (value === null || value === undefined) {
        return
      }
      if (max === undefined || value > max) {
        max = value
      }
      if (min === undefined || value < min) {
        min = value
      }
    })
    if (lineData.length) {
      const assistLineMark: G2Spec = {
        type: 'lineY',
        encode: { y: 'value' },
        scale: {
          y: {
            domain: [min, max]
          }
        },
        data: lineData,
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

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const lineMark = options.children[0]
    if (!tooltipAttr.show) {
      defaultsDeep(lineMark, { tooltip: false })
      return options
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
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
              background: tooltipAttr.backgroundColor
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
          },
          render: (e, { title, items: originalItems }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            let tooltipItems = originalItems
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              tooltipItems = originalItems.filter(item => formatterMap[item.quotaList[0].id])
            }
            const result = []
            const head = originalItems[0]
            tooltipItems.forEach(item => {
              if (item.value === null || item.value === undefined) {
                return
              }
              const formatter = formatterMap[item.quotaList[0].id] ?? yAxis[0]
              const value = valueFormatter(item.value, formatter.formatterCfg)
              result.push({ ...item, name: item.category, value })
            })
            head.dynamicTooltipValue?.forEach(item => {
              const formatter = formatterMap[item.fieldId]
              if (formatter) {
                const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: 'grey', name, value })
              }
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
    return options
  }

  protected configSlider(chart: Chart, options: G2Spec): G2Spec {
    const { functionCfg } = parseJson(chart.senior)
    if (!functionCfg?.sliderShow) {
      return options
    }
    const lineMark = options.children[0]
    const sliderOpt = {
      slider: {
        x: {
          values: [functionCfg.sliderRange[0] / 100, functionCfg.sliderRange[1] / 100],
          style: {
            trackFill: functionCfg.sliderBg,
            selectionFill: functionCfg.sliderFillBg,
            handleLabelFill: functionCfg.sliderTextColor,
            sparklineLineStrokeOpacity: 0
          }
        }
      }
    }
    defaultsDeep(lineMark, sliderOpt)
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configBasicStyle,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configAssistLine,
      this.configTooltip,
      this.configSlider
    )(chart, options, {}, this)
  }

  constructor(name = 'line') {
    super(name, DEFAULT_DATA)
  }
}
