import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpSingleDimensionSeriesColor
} from '@/views/chart/components/js/util'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { useI18n } from '@/hooks/web/useI18n'
import { defaultsDeep, isEmpty } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'
import { G2ChartView, G2DrawOptions } from '../../../types/impl/g2'
import {
  getTooltipSeriesTotalMap,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../common/common_antv'

const { t } = useI18n()

/**
 * 矩形树图
 */
export class Treemap extends G2ChartView {
  properties: EditorProperty[] = [
    'background-overall-component',
    'border-style',
    'basic-style-selector',
    'title-selector',
    'legend-selector',
    'label-selector',
    'tooltip-selector',
    'jump-set',
    'linkage'
  ]
  propertyInner: EditorPropertyInner = {
    'background-overall-component': ['all'],
    'border-style': ['all'],
    'basic-style-selector': ['colors', 'alpha', 'seriesColor'],
    'label-selector': ['fontSize', 'color', 'showDimension', 'showQuota', 'showProportion'],
    'legend-selector': ['icon', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition'],
    'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
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
    ]
  }
  axis: AxisType[] = ['xAxis', 'yAxis', 'filter', 'drill', 'extLabel', 'extTooltip']
  axisConfig: AxisConfig = {
    xAxis: {
      name: `${t('chart.drag_block_treemap_label')} / ${t('chart.dimension')}`,
      type: 'd'
    },
    yAxis: {
      name: `${t('chart.drag_block_treemap_size')} / ${t('chart.quota')}`,
      limit: 1
    }
  }

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    const data = chart.data.data
    const baseOptions: G2Spec = {
      type: 'treemap',
      data: {
        value: {
          name: 'root',
          children: data
        }
      },
      autoFit: true,
      encode: {
        value: 'value',
        color: d => d.path[d.path.length - 1]
      }
    }
    const total = data.reduce((pre, next) => pre + (next.value ?? 0), 0)
    const options = this.setupOptions(chart, baseOptions, { total })
    const newChart = new G2Chart({ container })
    newChart.options(options)
    newChart.on('polygon:click', action)
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

  protected configTooltip(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { tooltip: tooltipAttr, label } = parseJson(chart.customAttr)
    const { yAxis } = chart
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const reserveDecimalCount = label.reserveDecimalCount
    const seriesTotalMap = getTooltipSeriesTotalMap(options.data.value.children)
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
    //https://github.com/antvis/G2/blob/de0c9c87646bc899db55c1ce37f342eaf7a20fb9/src/mark/treemap.ts#L132
    const tooltip = function (a) {
      return a
    }
    tooltip.title = undefined

    const { total } = context
    const tooltipOptions: G2Spec = {
      tooltip,
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
          render: (_, { title, items: originalItems }) => {
            let tooltipItems = originalItems
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              tooltipItems = originalItems.filter(item => formatterMap[item.data.quotaList[0].id])
            }
            const result = []
            const head = originalItems[0]
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', head.data.name)
            tooltipItems.forEach(item => {
              const formatter = formatterMap[item.data.quotaList[0].id] ?? yAxis[0]
              const value = valueFormatter(parseFloat(item.value as string), formatter.formatterCfg)
              // sync with label
              const percent = (Math.round(((item.value as number) / total) * 10000) / 100).toFixed(
                reserveDecimalCount
              )
              const name = isEmpty(formatter.chartShowName)
                ? formatter.name
                : formatter.chartShowName
              result.push({ ...item, name, value: `${value ?? ''} (${percent}%)` })
            })
            head.data.dynamicTooltipValue?.forEach(item => {
              const formatter = formatterMap[item.fieldId]
              if (formatter) {
                const total = seriesTotalMap[item.fieldId]
                // sync with label
                const percent = (Math.round((item.value / total) * 10000) / 100).toFixed(
                  reserveDecimalCount
                )
                const value = valueFormatter(parseFloat(item.value), formatter.formatterCfg)
                const name = isEmpty(formatter.chartShowName)
                  ? formatter.name
                  : formatter.chartShowName
                result.push({ color: 'grey', name, value: `${value ?? ''} (${percent}%)` })
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
    return defaultsDeep(options, tooltipOptions)
  }

  protected configLabel(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const customAttr: DeepPartial<ChartAttr> = parseJson(chart.customAttr)
    const labelAttr = customAttr.label
    if (!labelAttr.show) {
      const hideLanbelOpt = {
        style: {
          labelText: () => ''
        }
      }
      return defaultsDeep(options, hideLanbelOpt)
    }
    const { total } = context
    const label: G2Spec = {
      style: {
        labelFill: labelAttr.color,
        labelFontSize: labelAttr.fontSize,
        labelMaxLines:
          Number(!!labelAttr.showDimension) +
          Number(!!labelAttr.showQuota) +
          Number(!!labelAttr.showProportion),
        labelText: param => {
          let res = param.value
          const contentItems = []
          if (labelAttr.showDimension) {
            contentItems.push(param.data.field)
          }
          if (labelAttr.showQuota) {
            contentItems.push(valueFormatter(param.value, labelAttr.quotaLabelFormatter))
          }
          if (labelAttr.showProportion) {
            const percentage = `${(((param.value / total) * 10000) / 100).toFixed(
              labelAttr.reserveDecimalCount
            )}%`
            contentItems.push(percentage)
          }
          res = contentItems.join('\n')
          return res
        },
        labelTransform: labelAttr.fullDisplay === true ? [] : [{ type: 'overflowHide' }]
      }
    }
    return defaultsDeep(options, label)
  }

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, customStyle } = chart
    const { label } = customAttr
    customAttr.label = {
      ...label,
      show: true,
      showDimension: true,
      showProportion: true,
      reserveDecimalCount: 2
    }
    const { legend } = customStyle
    legend.show = false
    return chart
  }

  public setupSeriesColor(chart: ChartObj, data?: any[]): ChartBasicStyle['seriesColor'] {
    data?.sort((a, b) => b.value - a.value)
    return setUpSingleDimensionSeriesColor(chart, data)
  }

  protected configColor(chart: Chart, options: G2Spec): G2Spec {
    const data = options.data.value.children
    data.sort((a, b) => b.value - a.value)
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

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options, context, this)
  }

  constructor() {
    super('treemap', [])
  }
}
