import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpSingleDimensionSeriesColor
} from '@/views/chart/components/js/util'
import { TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep, isEmpty } from 'lodash-es'
import { ChartEvent, Chart as G2Chart, G2Spec } from '@antv/g2'
import { valueFormatter } from '../../../../formatter'

const { t } = useI18n()
/**
 * 象限图
 */
export class Quadrant extends G2ChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'label-selector',
    'tooltip-selector',
    'legend-selector',
    'jump-set',
    'linkage',
    'quadrant-selector'
  ]
  propertyInner: EditorPropertyInner = {
    'basic-style-selector': [
      'colors',
      'alpha',
      'scatterSymbol',
      'scatterSymbolSize',
      'seriesColor'
    ],
    'label-selector': ['fontSize', 'color'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'x-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisLine',
      'axisValue',
      'splitLine',
      'axisForm',
      'axisLabel',
      'axisLabelFormatter'
    ],
    'y-axis-selector': [
      'position',
      'name',
      'color',
      'fontSize',
      'axisValue',
      'axisLine',
      'splitLine',
      'axisForm',
      'axisLabel',
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
    'legend-selector': ['icon', 'orient', 'color', 'fontSize', 'hPosition', 'vPosition'],
    'quadrant-selector': ['regionStyle', 'label', 'lineStyle']
  }
  axis: AxisType[] = [
    'xAxis',
    'yAxis',
    'yAxisExt',
    'extBubble',
    'filter',
    'drill',
    'extLabel',
    'extTooltip'
  ]
  axisConfig: AxisConfig = {
    extBubble: {
      name: `${t('chart.bubble_size')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1,
      allowEmpty: true
    },
    xAxis: {
      name: `${t('chart.form_type')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    yAxis: {
      name: `${t('chart.x_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    },
    yAxisExt: {
      name: `${t('chart.y_axis')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action, quadrantDefaultBaseline } = drawOptions
    if (!chart.data?.data) {
      return
    }
    // data
    const data = chart.data.data
    // x轴基准线 默认值
    const xValues = data.map(item => item.value)
    const xBaseline = (Math.max(...xValues) + Math.min(...xValues)) / 2
    // y轴基准线 默认值
    const yValues = data.map(item => item.extValue)
    const yBaseline = (Math.max(...yValues) + Math.min(...yValues)) / 2
    const defaultBaselineQuadrant = {
      ...chart.customAttr['quadrant']
    }
    // 新建图表
    if (defaultBaselineQuadrant.xBaseline === undefined) {
      // 默认基准线值
      defaultBaselineQuadrant.xBaseline = xBaseline
      defaultBaselineQuadrant.yBaseline = yBaseline
    }
    const baseOptions: G2Spec = {
      type: 'view',
      autoFit: true,
      data: {
        value: data
      },
      children: [
        { type: 'lineX', data: [xBaseline] },
        { type: 'lineY', data: [yBaseline] },
        {
          type: 'point',
          encode: { x: 'value', y: 'extValue', color: 'field' },
          legend: { size: false }
        },
        { type: 'range', data: [], encode: { x: 'x', y: 'y' }, zIndex: -1 }
      ]
    }
    chart.container = container
    const options: G2Spec = this.setupOptions(chart, baseOptions, {})
    const newChart = new G2Chart({ container })
    newChart.options(options)
    newChart.on(`point:${ChartEvent.CLICK}`, action)
    newChart.on(`plot:${ChartEvent.CLICK}`, () => quadrantDefaultBaseline(defaultBaselineQuadrant))
    newChart.once(ChartEvent.AFTER_RENDER, () => quadrantDefaultBaseline(defaultBaselineQuadrant))
    newChart.once(ChartEvent.AFTER_RENDER, () => {
      const rangeMark = newChart.getNodeByType('range')
      const xScale = newChart.getScaleByChannel('x')
      const [xMin, xMax] = xScale.getOptions().domain
      const yScale = newChart.getScaleByChannel('y')
      const [yMin, yMax] = yScale.getOptions().domain
      rangeMark.data([
        { x: [xBaseline, xMax], y: [yBaseline, yMax], region: 0 },
        { x: [xMin, xBaseline], y: [yBaseline, yMax], region: 1 },
        { x: [xMin, xBaseline], y: [yMin, yBaseline], region: 2 },
        { x: [xBaseline, xMax], y: [yMin, yBaseline], region: 3 }
      ])
      newChart.render()
    })

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
    const basicStyle = parseJson(chart.customAttr).basicStyle
    const { seriesColor } = basicStyle
    if (seriesColor?.length) {
      const pointMark = options.children[2]
      const colorRelations = []
      seriesColor.forEach(item => {
        colorRelations.push([item.name, hexColorToRGBA(item.color, basicStyle.alpha)])
      })
      if (colorRelations.length) {
        defaultsDeep(pointMark, {
          scale: {
            color: {
              relations: colorRelations
            }
          }
        })
      }
    }
    return options
  }

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const basicStyle = customAttr.basicStyle
    const sizeOptions = {
      scale: {
        color: {
          range: options.theme.category10
        }
      },
      encode: {
        shape: {
          type: 'constant',
          value: basicStyle.scatterSymbol
        }
      }
    }
    const pointMark = options.children[2]
    if (chart.extBubble?.length) {
      const tmpOptions = {
        encode: {
          size: 'popSize'
        },
        scale: {
          size: {
            range: [5, 30]
          }
        }
      }
      defaultsDeep(pointMark, sizeOptions, tmpOptions)
      return options
    }
    const tmp = {
      encode: {
        size: {
          type: 'constant',
          value: basicStyle.scatterSymbolSize
        }
      }
    }
    defaultsDeep(pointMark, sizeOptions, tmp)
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
    const pointMatk = options.children[2]
    if (!xAxis.axisValue.auto) {
      const scaleOpt = {
        scale: {
          x: {
            domainMin: xAxis.axisValue.min,
            domainMax: xAxis.axisValue.max,
            tickCount: xAxis.axisValue.splitCount,
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
      defaultsDeep(pointMatk, scaleOpt)
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
      const pointMatk = options.children[2]
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
      defaultsDeep(pointMatk, scaleOpt)
    }
    return defaultsDeep(options, axisOption)
  }

  protected configQuadrant(chart: Chart, options: G2Spec): G2Spec {
    const { quadrant } = parseJson(chart.customAttr)
    const [lineX, lineY, , range] = options.children
    const linetStyle = {
      style: {
        stroke: quadrant.lineStyle.stroke,
        lineWidth: quadrant.lineStyle.lineWidth,
        opacity: quadrant.lineStyle.opacity
      }
    }
    defaultsDeep(lineX, linetStyle)
    defaultsDeep(lineY, linetStyle)
    const rangeOpt = {
      style: {
        fill: d => {
          return quadrant.regionStyle[d.region].fill
        },
        fillOpacity: d => {
          return quadrant.regionStyle[d.region].fillOpacity
        }
      },
      labels: [
        {
          text: d => {
            return quadrant.labels[d.region].content
          },
          style: {
            fill: d => {
              return quadrant.labels[d.region].style.fill
            },
            fontSize: d => {
              return quadrant.labels[d.region].style.fontSize
            },
            fillOpacity: d => {
              return quadrant.labels[d.region].style.fillOpacity
            },
            position: d => {
              return {
                0: 'bottom-right',
                1: 'bottom-left',
                2: 'top-left',
                3: 'top-right'
              }[d.region]
            }
          }
        }
      ]
    }
    defaultsDeep(range, rangeOpt)
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
    const pointMark = options.children[2]
    defaultsDeep(pointMark, tmpLegend)
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      return options
    }
    const pointMark = options.children[2]
    const labelStyle = {
      labels: [
        {
          text: 'field',
          position: 'inside',
          style: {
            fill: label.color,
            fontSize: label.fontSize,
            fillOpacity: 1
          },
          transform: label.fullDisplay ? [] : [{ type: 'overlapHide' }, { type: 'exceedAdjust' }]
        }
      ]
    }
    defaultsDeep(pointMark, labelStyle)
    return options
  }

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    if (!tooltipAttr.show) {
      return { ...options, tooltip: false }
    }
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.seriesId] = next
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
    const { yAxis, extBubble } = chart
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
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
          render: (_, { items }) => {
            const head = items[0]
            const title = head.field
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const validField = []
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              let seriesId = `${head.quotaList[0].id}-yAxis`
              let formatter = formatterMap[seriesId]
              if (formatter) {
                validField.push('value')
              }
              seriesId = `${head.quotaList[1].id}-yAxisExt`
              formatter = formatterMap[seriesId]
              if (formatter) {
                validField.push('extValue')
              }
              if (extBubble?.length) {
                const extBubbleId = `${head.quotaList[2].id}-extBubble`
                formatter = formatterMap[extBubbleId]
                if (formatter) {
                  validField.push('popSize')
                }
              }
            } else {
              validField.push('value', 'extValue')
              if (extBubble?.length) {
                validField.push('popSize')
              }
            }
            const result = []
            validField.forEach(field => {
              let seriesId = `${head.quotaList[0].id}-yAxis`
              if (field === 'extValue') {
                seriesId = `${head.quotaList[1].id}-yAxisExt`
              }
              if (field === 'popSize') {
                seriesId = `${head.quotaList[2].id}-extBubble`
              }
              const formatter = formatterMap[seriesId] ?? yAxis[0]
              const value = valueFormatter(head[field], formatter.formatterCfg)
              const name = isEmpty(formatter.chartShowName)
                ? formatter.name
                : formatter.chartShowName
              result.push({ color: head.color, name, value })
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
    const pointMark = options.children[2]
    defaultsDeep(pointMark, tooltipOptions)
    return options
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.yAxis.splitLine = {
      ...chart.customStyle.yAxis.splitLine,
      show: false
    }
    chart.customStyle.yAxisExt.splitLine = {
      ...chart.customStyle.yAxisExt.splitLine,
      show: false
    }
    chart.customStyle.yAxis.axisLine = {
      ...chart.customStyle.yAxis.axisLine,
      show: true
    }
    chart.customStyle.yAxisExt.axisLine = {
      ...chart.customStyle.yAxisExt.axisLine,
      show: true
    }
    return chart
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    const { xAxis, yAxis, yAxisExt } = chart
    if (!(xAxis?.length && yAxis?.length && yAxisExt?.length)) {
      return []
    }
    return setUpSingleDimensionSeriesColor(chart, data)
  }

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configColor,
      this.configBasicStyle,
      this.configQuadrant,
      this.configXAxis,
      this.configYAxis,
      this.configLegend,
      this.configLabel,
      this.configTooltip
    )(chart, options, context, this)
  }

  constructor() {
    super('quadrant', [])
  }
}
