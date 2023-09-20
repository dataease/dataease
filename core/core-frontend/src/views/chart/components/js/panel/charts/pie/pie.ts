import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { Pie as G2Pie, PieOptions } from '@antv/g2plot/esm/plots/pie'
import { flow, hexColorToRGBA, parseJson } from '@/views/chart/components/js/util'
import { getPadding } from '@/views/chart/components/js/panel/common/common_antv'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import {
  PIE_AXIS_CONFIG,
  PIE_AXIS_TYPE,
  PIE_EDITOR_PROPERTY,
  PIE_EDITOR_PROPERTY_INNER
} from '@/views/chart/components/js/panel/charts/pie/common'
import { Datum } from '@antv/g2plot/esm/types/common'
import { add } from 'mathjs'

const DEFAULT_DATA = []
export class Pie extends G2PlotChartView<PieOptions, G2Pie> {
  axis: AxisType[] = PIE_AXIS_TYPE
  properties = PIE_EDITOR_PROPERTY
  propertyInner = PIE_EDITOR_PROPERTY_INNER
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
      ]
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
    const { tooltip, label } = parseJson(chart.customAttr)
    if (!tooltip.show) {
      return {
        ...options,
        tooltip: false
      }
    }
    const reserveDecimalCount = label.reserveDecimalCount
    // trick, cal total, maybe use scale of chart in plot instance
    const total = options.data?.reduce((pre, next) => add(pre, next.value ?? 0), 0)
    return {
      ...options,
      tooltip: {
        formatter: function (param: Datum) {
          const obj = { name: param.field, value: param.value }
          const res = valueFormatter(param.value, tooltip.tooltipFormatter)
          // sync with label
          const percent = (Math.round((param.value / total) * 10000) / 100).toFixed(
            reserveDecimalCount
          )
          obj.value = `${res ?? ''} (${percent}%)`
          return obj
        }
      }
    }
  }

  protected configBasicStyle(chart: Chart, options: PieOptions): PieOptions {
    const customAttr = parseJson(chart.customAttr)
    return {
      ...options,
      radius: customAttr.basicStyle.radius / 100
    }
  }
  setupDefaultOptions(chart: ChartObj): ChartObj {
    const customAttr = chart.customAttr
    const { label } = customAttr
    if (!['inner', 'outer'].includes(label.position)) {
      label.position = 'outer'
    }
    return chart
  }

  protected setupOptions(chart: Chart, options: PieOptions): PieOptions {
    return flow(
      this.configTheme,
      this.configBasicStyle,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options)
  }

  constructor(name = 'pie') {
    super(name, DEFAULT_DATA)
  }
}

export class PieDonut extends Pie {
  propertyInner: EditorPropertyInner = {
    ...PIE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'radius', 'innerRadius']
  }
  protected configBasicStyle(chart: Chart, options: PieOptions): PieOptions {
    const customAttr = parseJson(chart.customAttr)
    return {
      ...options,
      radius: customAttr.basicStyle.radius / 100,
      innerRadius: customAttr.basicStyle.innerRadius / 100
    }
  }

  constructor() {
    super('pie-donut')
  }
}
