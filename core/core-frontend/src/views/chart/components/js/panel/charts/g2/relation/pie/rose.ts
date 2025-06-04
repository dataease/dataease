import { G2ChartView, G2DrawOptions } from '../../../../types/impl/g2'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpSingleDimensionSeriesColor
} from '@/views/chart/components/js/util'
import {
  PIE_AXIS_CONFIG,
  PIE_AXIS_TYPE,
  PIE_EDITOR_PROPERTY,
  PIE_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/g2plot/pie/common'
import {
  getTooltipSeriesTotalMap,
  TOOLTIP_ITEM_TPL,
  TOOLTIP_TITLE_TPL
} from '../../../../common/common_antv'
import { useI18n } from '@/hooks/web/useI18n'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { cloneDeep, defaultsDeep, isEmpty } from 'lodash-es'
import { Chart as G2Chart, G2Spec } from '@antv/g2'

const { t } = useI18n()

export class Rose extends G2ChartView {
  axis: AxisType[] = PIE_AXIS_TYPE
  properties = PIE_EDITOR_PROPERTY
  propertyInner: EditorPropertyInner = {
    ...PIE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'radius', 'topN', 'seriesColor'],
    'tooltip-selector': [...PIE_EDITOR_PROPERTY_INNER['tooltip-selector'], 'carousel']
  }
  axisConfig = PIE_AXIS_CONFIG

  async drawChart(drawOptions: G2DrawOptions<G2Chart>): Promise<G2Chart> {
    const { chart, container, action } = drawOptions
    this.configEmptyDataStyle(chart.data?.data, container, null, t('chart.no_data_or_not_positive'))
    chart.container = container
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data = chart.data.data
    // custom color
    const customAttr = parseJson(chart.customAttr)
    // options
    const initOptions: G2Spec = {
      type: 'interval',
      autoFit: true,
      data: {
        value: data
      },
      transform: [{ type: 'stackY' }],
      encode: {
        color: 'field',
        x: 'field',
        y: 'value'
      },
      axis: false,
      coordinate: {
        type: 'polar'
      }
    }
    const total = data.reduce((pre, next) => pre + (next.value ?? 0), 0)
    const options = this.setupOptions(chart, initOptions, { total })
    const newChart = new G2Chart({ container })
    newChart.options(options)
    newChart.on('interval:click', d => {
      d.data?.data?.field !== customAttr.basicStyle.topNLabel && action(d)
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

  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const customAttr = parseJson(chart.customAttr)
    const { basicStyle } = customAttr
    const data = options.data.value || []
    if (data?.length && basicStyle.calcTopN && data.length > basicStyle.topN) {
      data.sort((a, b) => b.value - a.value)
      const otherItems = data.splice(basicStyle.topN)
      const initOtherItem = {
        ...data[0],
        dynamicTooltipValue: [],
        field: basicStyle.topNLabel,
        name: basicStyle.topNLabel,
        value: 0
      }
      const dynamicTotalMap: Record<string, number> = {}
      otherItems.reduce((p, n) => {
        p.value += n.value ?? 0
        n.dynamicTooltipValue?.forEach(val => {
          dynamicTotalMap[val.fieldId] = (dynamicTotalMap[val.fieldId] || 0) + val.value
        })
        return p
      }, initOtherItem)
      for (const key in dynamicTotalMap) {
        initOtherItem.dynamicTooltipValue.push({
          fieldId: key,
          value: dynamicTotalMap[key]
        })
      }
      data.push(initOtherItem)
    }
    options.coordinate.outerRadius = basicStyle.radius / 100
    return options
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

  protected configLabel(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    const { label: labelAttr } = parseJson(chart.customAttr)
    if (!labelAttr?.show) {
      return options
    }
    const { total } = context
    const label = {
      transform: [{ type: 'exceedAdjust' }],
      position: labelAttr.position === 'inner' ? 'inside' : 'outside',
      style: {
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize,
        fillOpacoty: 1
      },
      text: param => {
        let res = param.value
        const contentItems = []
        if (labelAttr.showDimension) {
          contentItems.push(param.field)
        }
        if (labelAttr.showQuota) {
          contentItems.push(valueFormatter(param.value, labelAttr.quotaLabelFormatter))
        }
        if (labelAttr.showProportion) {
          const percentage = `${(Math.round((param.value / total) * 10000) / 100).toFixed(
            labelAttr.reserveDecimalCount
          )}%`
          if (labelAttr.showDimension && labelAttr.showQuota) {
            contentItems.push(`(${percentage})`)
          } else {
            contentItems.push(percentage)
          }
        }
        res = contentItems.join(' ')
        return res
      }
    }
    if (!labelAttr.fullDisplay) {
      label.transform.push({ type: 'overlapHide' })
    }
    return { ...options, labels: [label] }
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

    const { total } = context
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
            let tooltipItems = items
            if (tooltipAttr.seriesTooltipFormatter?.length) {
              tooltipItems = items.filter(item => formatterMap[item.quotaList[0].id])
            }
            const result = []
            const [head] = items
            const titleHtml = TOOLTIP_TITLE_TPL.replace('{title}', head.field)
            tooltipItems.forEach(item => {
              const formatter = formatterMap[item.quotaList[0].id] ?? yAxis[0]
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
            head.dynamicTooltipValue?.forEach(item => {
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

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const { customAttr, customStyle } = chart
    const { label } = customAttr
    if (!['inner', 'outer'].includes(label.position)) {
      label.position = 'outer'
    }
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
    data = cloneDeep(data)
    const { calcTopN, topN, topNLabel } = chart.customAttr.basicStyle
    if (data?.length && calcTopN && data.length > topN) {
      data.sort((a, b) => b.value - a.value)
      data.splice(topN)
      data.push({
        field: topNLabel,
        value: 0
      })
    }
    return setUpSingleDimensionSeriesColor(chart, data)
  }

  protected setupOptions(chart: Chart, options: G2Spec, context: Record<string, any>): G2Spec {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configColor,
      this.configLegend,
      this.configLabel,
      this.configTooltip
    )(chart, options, context, this)
  }

  constructor(name = 'pie-rose') {
    super(name, [])
  }
}

export class RoseDonut extends Rose {
  propertyInner: EditorPropertyInner = {
    ...PIE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'radius', 'innerRadius', 'topN', 'seriesColor'],
    'tooltip-selector': [...PIE_EDITOR_PROPERTY_INNER['tooltip-selector'], 'carousel']
  }
  protected configBasicStyle(chart: Chart, options: G2Spec): G2Spec {
    const tmp = super.configBasicStyle(chart, options)
    const { basicStyle } = parseJson(chart.customAttr)
    tmp.coordinate.innerRadius = basicStyle.innerRadius / 100
    return tmp
  }

  constructor() {
    super('pie-donut-rose')
  }
}
