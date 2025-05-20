import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep, toString } from 'lodash-es'
import { ChartEvent, Chart as G2Chart, G2Spec } from '@antv/g2'
import { Text } from '@antv/g'

const { t } = useI18n()

const DEFAULT_DATA = []
/**
 * 热力图
 */
export class TableG2Chart extends G2ChartView {
  properties: EditorProperty[] = [
    'basic-style-selector',
    'background-overall-component',
    'label-selector',
    'legend-selector',
    'x-axis-selector',
    'y-axis-selector',
    'title-selector',
    'tooltip-selector',
    'jump-set',
    'linkage',
    'border-style'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'basic-style-selector': ['colors'],
    'label-selector': ['fontSize', 'color'],
    'x-axis-selector': ['name', 'color', 'fontSize', 'position', 'axisLabel', 'axisLine'],
    'y-axis-selector': [
      'name',
      'color',
      'fontSize',
      'position',
      'axisLabel',
      'axisLine',
      'showLengthLimit'
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
    'legend-selector': ['orient', 'color', 'fontSize', 'hPosition', 'vPosition'],
    'tooltip-selector': ['show', 'color', 'fontSize', 'backgroundColor'],
    'border-style': ['all']
  }
  axis: AxisType[] = ['xAxis', 'xAxisExt', 'extColor', 'filter']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.x_axis')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    xAxisExt: {
      name: `${t('chart.y_axis')} / ${t('chart.dimension')}`,
      type: 'd',
      limit: 1
    },
    extColor: {
      name: `${t('chart.color')} / ${t('chart.dimension_or_quota')}`,
      limit: 1
    }
  }
  protected getDefaultLength = (chart, l) => {
    const containerDom = document.getElementById(chart.container)
    const containerHeight = containerDom?.clientHeight || 100
    const containerWidth = containerDom?.clientWidth || 100
    let defaultLength = containerHeight - containerHeight * 0.5
    if (l.orient !== 'vertical') {
      defaultLength = containerWidth - containerWidth * 0.5
    }
    return defaultLength
  }
  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    const { xAxis, xAxisExt, extColor } = chart
    if (!xAxis?.length || !xAxisExt?.length || !extColor?.length) {
      return
    }
    const xField = xAxis[0].dataeaseName
    const xFieldExt = xAxisExt[0].dataeaseName
    const extColorField = extColor[0].dataeaseName
    // data
    const data = chart.data.tableRow
    // options
    const initOptions: G2Spec = {
      type: 'cell',
      autoFit: true,
      data: {
        value: data
      },
      encode: {
        x: xField,
        y: xFieldExt,
        color: extColorField
      }
    }
    const axisMap = {
      [chart.xAxis[0].dataeaseName]: chart.xAxis[0].chartShowName ?? chart.xAxis[0].name,
      [chart.xAxisExt[0].dataeaseName]: chart.xAxisExt[0].chartShowName ?? chart.xAxisExt[0].name,
      [chart.extColor[0].dataeaseName]: chart.extColor[0].chartShowName ?? chart.extColor[0].name
    }
    chart.container = container
    const options = this.setupOptions(chart, initOptions, { axisMap, container })
    const newChart = new G2Chart({ container })
    newChart.options(options)
    newChart.on('plot:click', param => {
      if (!param.data?.data) {
        return
      }
      const pointData = param.data.data
      const dimensionList = []
      chart.data.fields.forEach(item => {
        Object.keys(pointData).forEach(key => {
          if (key.startsWith('f_') && item.dataeaseName === key) {
            dimensionList.push({
              id: item.id,
              dataeaseName: item.dataeaseName,
              value: pointData[key]
            })
          }
        })
      })
      action({
        x: param.data.x,
        y: param.data.y,
        data: {
          data: {
            ...param.data.data,
            value: dimensionList[1].value,
            name: dimensionList[1].id,
            dimensionList: dimensionList,
            quotaList: [dimensionList[1]]
          }
        }
      })
    })
    this.configYAxisLabelLimit(newChart, chart)
    return newChart
  }

  protected configYAxisLabelLimit(chartObj: G2Chart, chart: Chart) {
    const { yAxis } = parseJson(chart.customStyle)
    if (!yAxis.show || !yAxis.axisLabel.show || !yAxis.axisLabel.lengthLimit) {
      return
    }
    const { tooltip } = parseJson(chart.customAttr)
    const labelTipId = `AXIS_LABEL_TIP-${chart.id}`
    chartObj.on(`axis-label-item:${ChartEvent.POINTER_OVER}`, e => {
      const target = e.target
      const { text, originValue } = target.attributes
      if (!originValue) {
        return
      }
      if (text !== originValue) {
        let parentDom = document.getElementById('G2-TOOLTIP-WRAPPER')
        if (!parentDom) {
          parentDom = document.createElement('div')
          parentDom.id = 'G2-TOOLTIP-WRAPPER'
          parentDom.style.position = 'absolute'
          parentDom.style.pointerEvents = 'none'
          parentDom.style.zIndex = '9999'
          document.body.appendChild(parentDom)
        }
        let labelTipDom = document.getElementById(labelTipId)
        if (!labelTipDom) {
          labelTipDom = document.createElement('div')
          labelTipDom.id = labelTipId
          labelTipDom.style.position = 'fixed'
          labelTipDom.style.color = tooltip.color
          labelTipDom.style.backgroundColor = tooltip.backgroundColor
          labelTipDom.style.fontSize = `${tooltip.fontSize}px`
          labelTipDom.style.padding = '5px 20px'
          labelTipDom.style.boxShadow = 'rgba(0, 0, 0, 0.12) 0px 6px 12px 0px'
          labelTipDom.style.borderRadius = '4px'
          labelTipDom.style.transition =
            'visibility 0.2s cubic-bezier(0.23, 1, 0.32, 1), left 0.4s cubic-bezier(0.23, 1, 0.32, 1), top 0.4s cubic-bezier(0.23, 1, 0.32, 1)'
          parentDom.appendChild(labelTipDom)
        }
        labelTipDom.innerText = originValue
        labelTipDom.style.visibility = 'visible'
        labelTipDom.style.left = `${e.client.x + 30}px`
        labelTipDom.style.top = `${e.client.y + 20}px`
      }
    })
    chartObj.on(`axis-label-item:${ChartEvent.POINTER_OUT}`, e => {
      const target = e.target
      const { originValue } = target.attributes
      if (!originValue) {
        return
      }
      const labelTipDom = document.getElementById(labelTipId)
      if (labelTipDom) {
        labelTipDom.style.visibility = 'hidden'
      }
    })
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

  protected configLegend(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
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
          itemMarker: legend.icon,
          title: false
        }
      }
    }
    defaultsDeep(options, tmpLegend)
    const colorField = chart.extColor[0]
    if (colorField.groupType === 'q') {
      const { container } = context
      const containerDom = document.getElementById(container)
      const colors = options.theme.category10
      const quotaLegendOption = {
        scale: {
          color: {
            type: 'linear',
            interpolate: () => {
              return c => colors[Math.floor(c * (colors.length - 1))]
            }
          }
        },
        legend: {
          color: {
            color: colors,
            label: false
          }
        }
      }
      if (legend.orient === 'vertical') {
        quotaLegendOption.legend.color.height = containerDom?.offsetHeight / 2
      } else {
        quotaLegendOption.legend.color.width = containerDom?.offsetWidth / 2
      }
      defaultsDeep(options, quotaLegendOption)
    }
    return options
  }

  protected configLabel(chart: Chart, options: G2Spec): G2Spec {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      return options
    }
    const labelStyle = {
      labels: [
        {
          text: chart.extColor[0].dataeaseName,
          position: 'inside',
          style: {
            fill: label.color,
            fontSize: label.fontSize
          },
          transform: label.fullDisplay ? [] : [{ type: 'overflowHide' }]
        }
      ]
    }
    return defaultsDeep(options, labelStyle)
  }

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { tooltip } = parseJson(chart.customAttr)
    if (!tooltip.show) {
      return {
        ...options,
        tooltip: false
      }
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
    const { axisMap } = context
    const tooltipOptions: G2Spec = {
      tooltip: d => d,
      interaction: {
        tooltip: {
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
          render: (_, { items }) => {
            const xField = chart.xAxis[0].dataeaseName
            const yField = chart.xAxisExt[0].dataeaseName
            const colorField = chart.extColor[0].dataeaseName
            const head = items[0]
            const title = head[xField]
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', title)
            const result = [
              { marker: head.color, label: axisMap[yField], value: head[yField] },
              { marker: head.color, label: axisMap[colorField], value: head[colorField] }
            ]
            const itemsHtml = result
              .map(({ marker, label, value }) => {
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
    return defaultsDeep(options, tooltipOptions)
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
    const axisStyle = {
      axis: {
        x: {
          position: xAxis.position,
          title: xAxis.nameShow === false ? false : xAxis.name,
          titleFontSize: xAxis.fontSize,
          titleFill: xAxis.color,
          line: xAxis.axisLine.show,
          lineStroke: xAxis.axisLine.lineStyle.color,
          lineLineWidth: xAxis.axisLine.lineStyle.width,
          lineLineDash,
          label: xAxis.axisLabel.show,
          labelFill: xAxis.axisLabel.color,
          labelFontSize: xAxis.axisLabel.fontSize,
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
    const axisStyle = {
      axis: {
        y: {
          position: yAxis.position,
          title: yAxis.nameShow === false ? false : yAxis.name,
          titleFontSize: yAxis.fontSize,
          titleFill: yAxis.color,
          line: yAxis.axisLine.show,
          lineStroke: yAxis.axisLine.lineStyle.color,
          lineLineWidth: yAxis.axisLine.lineStyle.width,
          lineLineDash,
          label: yAxis.axisLabel.show,
          labelFill: yAxis.axisLabel.color,
          labelFontSize: yAxis.axisLabel.fontSize,
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
            const str = toString(d)
            if (!str) {
              return ''
            }
            const lengthLimit = yAxis.axisLabel.lengthLimit
            if (lengthLimit) {
              const strLength = str.length
              if (strLength > lengthLimit) {
                const ellipsisText = str.substring(0, lengthLimit) + '...'
                return new Text({
                  style: {
                    text: ellipsisText,
                    originValue: `${d}`
                  }
                })
              }
            }
            return d
          }
        }
      }
    }
    return defaultsDeep(options, axisStyle)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    chart.customStyle.legend.orient = 'vertical'
    chart.customStyle.legend.vPosition = 'center'
    chart.customStyle.legend.hPosition = 'right'
    chart.customStyle.legend['rail'] = { defaultLength: 100 }
    return chart
  }

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configLegend,
      this.configLabel,
      this.configTooltip,
      this.configXAxis,
      this.configYAxis
    )(chart, options, context, this)
  }

  constructor() {
    super('t-heatmap', DEFAULT_DATA)
  }
}
