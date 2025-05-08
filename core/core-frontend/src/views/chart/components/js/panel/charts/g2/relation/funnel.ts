import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpSingleDimensionSeriesColor
} from '@/views/chart/components/js/util'
import { TOOLTIP_ITEM_TPL, TOOLTIP_TITLE_TPL } from '../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { defaultsDeep, isEmpty } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'

const { t } = useI18n()

/**
 * 漏斗图
 */
export class Funnel extends G2ChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'label-selector',
    'tooltip-selector',
    'title-selector',
    'legend-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'seriesColor'],
    'label-selector': ['fontSize', 'color', 'hPosition', 'showQuota', 'conversionTag'],
    'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
    'title-selector': [
      'show',
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
    'legend-selector': ['icon', 'orient', 'color', 'fontSize', 'hPosition', 'vPosition']
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_funnel_split')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_funnel_width')} / ${t('chart.quota')}`,
      type: 'q',
      limit: 1
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data) {
      return
    }
    const data = chart.data.data
    const baseOptions: G2Spec = {
      type: 'interval',
      autoFit: true,
      data,
      encode: { x: 'field', y: 'value', color: 'field', shape: 'funnel' },
      transform: [{ type: 'symmetryY' }],
      scale: { x: { paddingOuter: 0, paddingInner: 0 } },
      coordinate: { transform: [{ type: 'transpose' }] },
      axis: false,
      labels: []
    }
    const options = this.setupOptions(chart, baseOptions)
    const newChart = new G2Chart({ container })
    newChart.options(options)
    newChart.on('interval:click', action)
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
    const { xAxis, yAxis } = chart
    if (xAxis?.length && yAxis?.length) {
      const relations = []
      seriesColor.forEach(item => {
        relations.push([item.id, hexColorToRGBA(item.color, basicStyle.alpha)])
      })
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
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      return options
    }
    if (label.showQuota) {
      options.labels.push({
        text: d => {
          return valueFormatter(d.value, label.quotaLabelFormatter)
        },
        position: label.position === 'middle' ? 'inside' : label.position,
        fontSize: label.fontSize,
        fill: label.color,
        transform: !label.fullDisplay ? [{ type: 'overflowHide' }] : []
      })
    }
    if (label.conversionTag?.show) {
      const conversionTagArr = [
        {
          text: '',
          render: (_, __, i) =>
            i !== 0
              ? `<div style="height:1px;width:30px;background:#aaa;margin-right:20px;"></div>`
              : '',
          position: 'top-right'
        },
        {
          text: (_, i, data) => {
            if (i === 0) {
              return ''
            }
            const pre = data[i - 1].value
            const next = data[i].value
            const rate = `${((next / pre) * 100).toFixed(label.conversionTag.precision)}%`
            return (label.conversionTag.text ?? '转换率 ') + rate
          },
          position: 'top-right',
          textAlign: 'left',
          textBaseline: 'middle',
          dx: 60,
          fontSize: label.fontSize,
          fill: label.color
        }
      ]
      options.labels.push(...conversionTagArr)
      options.paddingRight = 120
    }
    return options
  }

  protected configTooltip(chart: Chart, options: G2Spec): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const tooltipAttr = customAttr.tooltip
    const yAxis = chart.yAxis
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
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
              const formatter = formatterMap[item.quotaList[0].id] ?? yAxis[0]
              const value = valueFormatter(item.value, formatter.formatterCfg)
              const name = isEmpty(formatter.chartShowName)
                ? formatter.name
                : formatter.chartShowName
              result.push({ ...item, name, value })
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
    defaultsDeep(options, tooltipOptions)
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

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    return setUpSingleDimensionSeriesColor(chart, data)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, customStyle } = chart
    const { label } = customAttr
    if (!['left', 'middle', 'right'].includes(label.position)) {
      label.position = 'middle'
    }
    customAttr.label = {
      ...label,
      show: true,
      showQuota: true,
      conversionTag: {
        show: false,
        precision: 2,
        text: t('chart.conversion_rate')
      }
    }
    const { legend } = customStyle
    legend.show = false
    return chart
  }

  protected setupOptions(chart: Chart, options: G2Spec): G2Spec {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options)
  }

  constructor() {
    super('funnel', [])
  }
}
