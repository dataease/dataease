import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Pie as G2Pie, PieOptions } from '@antv/g2plot/esm/plots/pie'
import {
  flow,
  hexColorToRGBA,
  parseJson,
  setUpSingleDimensionSeriesColor
} from '@/views/chart/components/js/util'
import {
  getPadding,
  getTooltipSeriesTotalMap
} from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  PIE_AXIS_CONFIG,
  PIE_AXIS_TYPE,
  PIE_EDITOR_PROPERTY,
  PIE_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/pie/common'
import { Datum } from '@antv/g2plot/esm/types/common'
import { add } from 'mathjs'
import isEmpty from 'lodash-es/isEmpty'
import { cloneDeep } from 'lodash-es'

const DEFAULT_DATA = []
export class Pie extends G2PlotChartView<PieOptions, G2Pie> {
  axis: AxisType[] = PIE_AXIS_TYPE
  properties = PIE_EDITOR_PROPERTY
  propertyInner: EditorPropertyInner = {
    ...PIE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'radius', 'topN', 'seriesColor']
  }
  axisConfig = PIE_AXIS_CONFIG

  drawChart(drawOptions: G2PlotDrawOptions<G2Pie>): G2Pie {
    const { chart, container, action } = drawOptions
    if (!chart.data?.data?.length) {
      return
    }
    // data
    const data = chart.data.data
    // custom color
    const customAttr = parseJson(chart.customAttr)
    const color = customAttr.basicStyle.colors.map(i =>
      hexColorToRGBA(i, customAttr.basicStyle.alpha)
    )
    // options
    const initOptions: PieOptions = {
      data: data,
      angleField: 'value',
      colorField: 'field',
      appendPadding: getPadding(chart),
      color,
      pieStyle: {
        lineWidth: 0
      },
      statistic: {
        title: false,
        content: {
          style: {
            whiteSpace: 'pre-wrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis'
          },
          content: ''
        }
      },
      interactions: [
        {
          type: 'legend-active',
          cfg: {
            start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
            end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
          }
        },
        {
          type: 'legend-filter',
          cfg: {
            start: [
              {
                trigger: 'legend-item:click',
                action: [
                  'list-unchecked:toggle',
                  'data-filter:filter',
                  'element-active:reset',
                  'element-highlight:reset'
                ]
              }
            ]
          }
        },
        {
          type: 'tooltip',
          cfg: {
            start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
            end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
          }
        },
        {
          type: 'active-region',
          cfg: {
            start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
            end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
          }
        }
      ],
      meta: {
        field: {
          type: 'cat'
        }
      }
    }
    const options = this.setupOptions(chart, initOptions)

    const newChart = new G2Pie(container, options)
    newChart.on('interval:click', action)
    return newChart
  }

  protected configLabel(chart: Chart, options: PieOptions): PieOptions {
    const { label: labelAttr } = parseJson(chart.customAttr)
    if (!labelAttr?.show) {
      return {
        ...options,
        label: false
      }
    }
    const label = {
      type: labelAttr.position === 'outer' ? 'spider' : labelAttr.position,
      layout: [{ type: 'limit-in-plot' }],
      autoRotate: false,
      style: {
        fill: labelAttr.color,
        fontSize: labelAttr.fontSize
      },
      formatter: (param: Datum) => {
        let res = param.value
        const contentItems = []
        if (labelAttr.showDimension) {
          contentItems.push(param.field)
        }
        if (labelAttr.showQuota) {
          contentItems.push(valueFormatter(param.value, labelAttr.quotaLabelFormatter))
        }
        if (labelAttr.showProportion) {
          const percentage = `${(Math.round(param.percent * 10000) / 100).toFixed(
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
    return { ...options, label }
  }

  protected configTooltip(chart: Chart, options: PieOptions): PieOptions {
    const { tooltip: tooltipAttr, label } = parseJson(chart.customAttr)
    const { yAxis } = chart
    if (!tooltipAttr.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const reserveDecimalCount = label.reserveDecimalCount
    const seriesTotalMap = getTooltipSeriesTotalMap(options.data)
    // trick, cal total, maybe use scale of chart in plot instance
    const total = options.data?.reduce((pre, next) => add(pre, next.value ?? 0), 0)
    const formatterMap = tooltipAttr.seriesTooltipFormatter
      ?.filter(i => i.show)
      .reduce((pre, next) => {
        pre[next.id] = next
        return pre
      }, {}) as Record<string, SeriesFormatter>
    const tooltip: PieOptions['tooltip'] = {
      showTitle: true,
      title: () => undefined,
      customItems(originalItems) {
        let tooltipItems = originalItems
        if (tooltipAttr.seriesTooltipFormatter?.length) {
          tooltipItems = originalItems.filter(item => formatterMap[item.data.quotaList[0].id])
        }
        const result = []
        const head = originalItems[0]
        tooltipItems.forEach(item => {
          const formatter = formatterMap[item.data.quotaList[0].id] ?? yAxis[0]
          const originValue = parseFloat(item.value as string)
          const value = valueFormatter(originValue, formatter.formatterCfg)
          // sync with label
          const percent = (Math.round((originValue / total) * 10000) / 100).toFixed(
            reserveDecimalCount
          )
          const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
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
            const name = isEmpty(formatter.chartShowName) ? formatter.name : formatter.chartShowName
            result.push({ color: 'grey', name, value: `${value ?? ''} (${percent}%)` })
          }
        })
        return result
      }
    }
    return {
      ...options,
      tooltip
    }
  }

  protected configBasicStyle(chart: Chart, options: PieOptions): PieOptions {
    const customAttr = parseJson(chart.customAttr)
    const { basicStyle } = customAttr
    const { data } = options
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
    return {
      ...options,
      radius: basicStyle.radius / 100
    }
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

  protected setupOptions(chart: Chart, options: PieOptions): PieOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configSingleDimensionColor,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options, {}, this)
  }

  constructor(name = 'pie') {
    super(name, DEFAULT_DATA)
  }
}

export class PieDonut extends Pie {
  propertyInner: EditorPropertyInner = {
    ...PIE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'radius', 'innerRadius', 'topN', 'seriesColor']
  }
  protected configBasicStyle(chart: Chart, options: PieOptions): PieOptions {
    const tmp = super.configBasicStyle(chart, options)
    const { basicStyle } = parseJson(chart.customAttr)
    return {
      ...tmp,
      radius: basicStyle.radius / 100,
      innerRadius: basicStyle.innerRadius / 100
    }
  }

  constructor() {
    super('pie-donut')
  }
}
