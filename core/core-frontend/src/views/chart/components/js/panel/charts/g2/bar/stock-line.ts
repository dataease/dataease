import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { defaultsDeep, isEmpty } from 'lodash-es'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { LINE_EDITOR_PROPERTY_INNER } from '../line/common'
import { useI18n } from '@/hooks/web/useI18n'
import { ChartEvent, Chart as G2Chart, G2Spec } from '@antv/g2'
import { registerSymbol, Symbols } from '@antv/g2/esm/utils/marker'
import { TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'

const { t } = useI18n()
const DEFAULT_DATA = []
/**
 * K线图
 */
export class StockLine extends G2ChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'legend-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'tooltip-selector',
    'function-cfg',
    'jump-set',
    'linkage'
  ]
  propertyInner = {
    ...LINE_EDITOR_PROPERTY_INNER,
    'function-cfg': ['emptyDataStrategy'],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'position',
      'axisLabel',
      'axisLine',
      'splitLine',
      'axisLabelFormatter'
    ],
    'legend-selector': ['fontSize', 'color', 'show']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('common.component.date')} / ${t('chart.dimension')}`,
      limit: 1,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.k_line_yaxis_tip')} / ${t('chart.quota')}`,
      limit: 4,
      type: 'q'
    }
  }

  stockMarker = function (x, y, r) {
    const width = r * 1
    const height = r
    return [
      // 矩形框
      ['M', x - width - 1 / 2, y - height / 2],
      ['L', x + width + 1 / 2, y - height / 2],
      ['L', x + width + 1 / 2, y + height / 2],
      ['L', x - width - 1 / 2, y + height / 2],
      ['Z'],
      // 中线
      ['M', x, y + 10 / 2],
      ['L', x, y - 10 / 2]
    ]
  }

  RED = '#EF5350'
  GREEN = '#26A29A'
  GREY = '#999999'

  /**
   * 计算收盘价平均值
   * @param data
   * @param dayCount
   * @param chart
   */
  calculateMovingAverage = (data, dayCount, chart) => {
    const xAxis = chart.xAxis
    const yAxis = chart.yAxis
    // 时间字段
    const xAxisDataeaseName = xAxis[0].dataeaseName
    // 收盘价字段
    const yAxisDataeaseName = yAxis[1].dataeaseName
    const result = []
    for (let i = 0; i < data.length; i++) {
      if (i < dayCount) {
        result.push({
          series: `MA${dayCount}`,
          [xAxisDataeaseName]: data[i][xAxisDataeaseName],
          value: null
        })
      } else {
        const sum = data
          .slice(i - dayCount + 1, i + 1)
          .reduce((sum, item) => sum + item[yAxisDataeaseName], 0)
        result.push({
          series: `MA${dayCount}`,
          [xAxisDataeaseName]: data[i][xAxisDataeaseName],
          value: parseFloat((sum / dayCount).toFixed(3))
        })
      }
    }
    return result
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, action, container } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    const xAxis = chart.xAxis
    const yAxis = chart.yAxis
    if (yAxis.length != 4) {
      return
    }
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const colors = []
    const alpha = basicStyle.alpha
    basicStyle.colors.forEach(ele => {
      colors.push(hexColorToRGBA(ele, alpha))
    })
    const data = chart.data?.tableRow

    const [_, __, minAxis, maxAxis] = yAxis
    // 时间字段
    const dateAxis = xAxis[0].dataeaseName
    // 时间排序
    data.sort((a, b) => new Date(a[dateAxis]).getTime() - new Date(b[dateAxis]).getTime())

    const initOptions: G2Spec = {
      type: 'view',
      data: {
        value: data
      },
      autoFit: true,
      encode: {
        x: dateAxis
      },
      scale: {
        y: {
          key: '2'
        }
      },
      children: [
        {
          type: 'link',
          encode: {
            y: [minAxis.dataeaseName, maxAxis.dataeaseName],
            color: () => '日K'
          },
          tooltip: false
        },
        {
          type: 'interval',
          encode: { y: [yAxis[0].dataeaseName, yAxis[1].dataeaseName], color: () => '日K' },
          slider: {
            x: {
              position: 'bottom'
            }
          },
          style: {
            stroke: 'black'
          }
        }
      ]
    }
    const newChart = new G2Chart({ container })
    const options = this.setupOptions(chart, initOptions)
    // 开始渲染
    newChart.options(options)
    newChart.on(`interval:${ChartEvent.CLICK}`, evt => {
      const selectDate = evt.data.data[dateAxis]
      const paramData = chart.data?.data
      const selectData = paramData.filter(item => item.field === selectDate)
      const quotaList = []
      selectData.forEach(item => {
        quotaList.push({ ...item.quotaList[0], value: item.value })
      })
      if (selectData.length) {
        const param = {
          x: evt.x,
          y: evt.y,
          data: {
            data: {
              ...evt.data.data,
              value: quotaList[0].value,
              name: selectDate,
              dimensionList: selectData[0].dimensionList,
              quotaList: quotaList
            }
          }
        }
        action(param)
      }
    })
    return newChart
  }

  protected configAvgLine(chart: Chart, options: G2Spec): G2Spec {
    const avgDataSeries = []
    const averages = [5, 10, 20, 60, 120, 180]
    averages.forEach(avgDay => {
      avgDataSeries.push(...this.calculateMovingAverage(options.data.value, avgDay, chart))
    })
    const lineMark = {
      type: 'line',
      data: {
        value: avgDataSeries
      },
      encode: {
        y: 'value',
        color: 'series',
        series: 'series'
      }
    }
    const pointMark = {
      type: 'point',
      data: {
        value: avgDataSeries
      },
      encode: {
        y: 'value',
        color: 'series'
      },
      style: {
        size: 3,
        shape: 'circle'
      },
      tooltip: false
    }
    options.children.push(lineMark, pointMark)
    return options
  }

  protected configTheme(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const colors: string[] = [`rgba(255, 0, 0, ${basicStyle.alpha / 100})`]
    basicStyle.colors.forEach(ele => {
      colors.push(hexColorToRGBA(ele, basicStyle.alpha))
    })
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

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const { basicStyle } = parseJson(chart.customAttr)
    const [_, __, lineMark, pointMark] = options.children
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
        size: basicStyle.lineSymbolSize
      }
    }
    if (basicStyle.lineSymbolSize === 0) {
      pointStyleOpt.encode.shape = 'none'
    }
    defaultsDeep(pointMark, pointStyleOpt)
    const [linkMark, intervalMark] = options.children
    const [startAxis, endAxis] = chart.yAxis
    const red = hexColorToRGBA(this.RED, basicStyle.alpha)
    const green = hexColorToRGBA(this.GREEN, basicStyle.alpha)
    const grey = hexColorToRGBA(this.GREY, basicStyle.alpha)
    const linkOpt = {
      style: {
        stroke: d => {
          const offset = d[startAxis.dataeaseName] - d[endAxis.dataeaseName]
          return offset === 0 ? grey : offset > 0 ? green : red
        }
      }
    }
    defaultsDeep(linkMark, linkOpt)
    const intervalOpt = {
      style: {
        fillOpacity: 1,
        fill: d => {
          const offset = d[startAxis.dataeaseName] - d[endAxis.dataeaseName]
          return offset === 0 ? grey : offset > 0 ? green : red
        }
      }
    }
    defaultsDeep(intervalMark, intervalOpt)
    return options
  }

  protected configLegend(chart: Chart, options: G2Spec): G2Spec {
    const { legend } = parseJson(chart.customStyle)
    if (!legend.show) {
      return { ...options, legend: false }
    }
    if (!Symbols.has('stock')) {
      registerSymbol('stock', this.stockMarker)
    }
    const baseLegend = this.getLegend(chart)
    const tmpLegend = {
      legend: {
        color: {
          ...baseLegend,
          orientation: 'horizontal',
          position: 'top',
          layout: {
            justifyContent: 'center',
            alignItems: 'center',
            flexDirection: 'row'
          },
          itemMarkerSize: 12,
          itemMarker: d => {
            if (d === '日K') {
              return 'stock'
            }
            return 'hyphen'
          }
        }
      }
    }
    defaultsDeep(options, tmpLegend)
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
          tick: xAxis.axisLabel.show,
          grid: xAxis.splitLine.show,
          gridStroke: xAxis.splitLine.lineStyle.color,
          gridStrokeOpacity: 1,
          gridLineWidth: xAxis.splitLine.lineStyle.width,
          gridLineDash,
          labelTransform: `rotate(${xAxis.axisLabel.rotate || 0})`,
          transform: [
            {
              type: 'hide',
              keepHeader: true,
              keepTail: true
            }
          ]
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
          tick: false,
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
    return defaultsDeep(options, axisOption)
  }

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const { tooltip: tooltipAttr, basicStyle } = parseJson(chart.customAttr)
    const [_, intervalMark, lineMark] = options.children
    if (!tooltipAttr.show) {
      defaultsDeep(lineMark, { tooltip: false })
      defaultsDeep(intervalMark, { tooltip: false })
      return options
    }
    let g2TooltipWrapper = document.getElementById('G2-TOOLTIP-WRAPPER')
    if (!g2TooltipWrapper) {
      g2TooltipWrapper = document.createElement('div')
      g2TooltipWrapper.id = 'G2-TOOLTIP-WRAPPER'
      g2TooltipWrapper.style.position = 'absolute'
      g2TooltipWrapper.style.pointerEvents = 'none'
      g2TooltipWrapper.style.zIndex = '9999'
      document.body.appendChild(g2TooltipWrapper)
    }
    const [openAxis, closeAxis, minAxis, maxAxis] = chart.yAxis
    const yAxisMap = chart.yAxis.reduce((acc, axis) => {
      acc[axis.dataeaseName] = axis
      return acc
    }, {})
    const yAxisKeys = [
      maxAxis.dataeaseName,
      minAxis.dataeaseName,
      closeAxis.dataeaseName,
      openAxis.dataeaseName
    ]
    const maKeys = ['MA5', 'MA10', 'MA20', 'MA60', 'MA120', 'MA180']
    const intervalMarkTooltipOptions: G2Spec = {
      tooltip: {
        items: [d => d]
      }
    }
    defaultsDeep(intervalMark, intervalMarkTooltipOptions)
    const lineMarkTooltipOptions: G2Spec = {
      interaction: {
        tooltip: {
          shared: true,
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
          render: (_, { title, items }) => {
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const result = []
            items.forEach(item => {
              if (item.value === null || item.value === undefined) {
                return
              }
              if (maKeys.includes(item.name)) {
                const value = valueFormatter(item.value, tooltipAttr.tooltipFormatter)
                result[maKeys.indexOf(item.name)] = { ...item, value }
              } else {
                const offset = item[openAxis.dataeaseName] - item[closeAxis.dataeaseName]
                const color =
                  offset === 0
                    ? hexColorToRGBA(this.GREY, basicStyle.alpha)
                    : offset > 0
                    ? hexColorToRGBA(this.GREEN, basicStyle.alpha)
                    : hexColorToRGBA(this.RED, basicStyle.alpha)
                yAxisKeys.forEach(key => {
                  const axis = yAxisMap[key]
                  const value = valueFormatter(item[key], tooltipAttr.tooltipFormatter)
                  result.unshift({ name: axis.chartShowName ?? axis.name, value, color })
                })
              }
            })
            const itemsHtml = result
              .map(item => {
                if (isEmpty(item)) {
                  return ''
                }
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
    defaultsDeep(lineMark, lineMarkTooltipOptions)
    return options
  }

  protected customConfigEmptyDataStrategy(chart: Chart, options: G2Spec): G2Spec {
    const data = options.data.value
    if (!data?.length) {
      return options
    }
    const { emptyDataStrategy: strategy } = parseJson(chart.senior).functionCfg
    if (strategy === 'ignoreData') {
      for (let i = data.length - 1; i >= 0; i--) {
        const item = data[i]
        Object.keys(item).forEach(key => {
          if (key.startsWith('f_') && item[key] === null) {
            data.splice(i, 1)
          }
        })
      }
    }
    const updateValues = (strategy: 'breakLine' | 'setZero', data: any[]) => {
      data.forEach(obj => {
        Object.keys(obj).forEach(key => {
          if (key.startsWith('f_') && obj[key] === null) {
            obj[key] = strategy === 'breakLine' ? null : 0
          }
        })
      })
    }
    if (strategy === 'breakLine' || strategy === 'setZero') {
      updateValues(strategy, data)
    }
    return options
  }

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(
      this.configTheme,
      this.customConfigEmptyDataStrategy,
      this.configAvgLine,
      this.configLegend,
      this.configBasicStyle,
      this.configXAxis,
      this.configYAxis,
      this.configTooltip
    )(chart, options, {}, this)
  }

  constructor(name = 'stock-line') {
    super(name, DEFAULT_DATA)
    this.stockMarker.style = ['stroke', 'fill', 'lineWidth', 'lineOpacity']
  }
}
