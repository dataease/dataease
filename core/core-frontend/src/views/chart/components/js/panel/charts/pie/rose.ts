import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { RoseOptions, Rose as G2Rose } from '@antv/g2plot/esm/plots/rose'
import {
  PIE_AXIS_CONFIG,
  PIE_AXIS_TYPE,
  PIE_EDITOR_PROPERTY,
  PIE_EDITOR_PROPERTY_INNER
} from './common'
import { getPadding } from '@/views/chart/components/js/panel/common/common_antv'
import { parseJson, flow } from '@/views/chart/components/js/util'
import { Label } from '@antv/g2plot/lib/types/label'
import { valueFormatter } from '@/views/chart/components/js/formatter'
import { Datum } from '@antv/g2plot/esm/types/common'
import { add } from 'mathjs'

export class Rose extends G2PlotChartView<RoseOptions, G2Rose> {
  axis: AxisType[] = PIE_AXIS_TYPE
  properties: EditorProperty[] = PIE_EDITOR_PROPERTY
  propertyInner: EditorPropertyInner = PIE_EDITOR_PROPERTY_INNER
  axisConfig = PIE_AXIS_CONFIG

  drawChart(drawOptions: G2PlotDrawOptions<G2Rose>): G2Rose {
    const { chart, container, action } = drawOptions
    if (!chart?.data?.data?.length) {
      return
    }
    // data
    const data = chart.data.data
    // options
    const baseOptions: RoseOptions = {
      data: data,
      xField: 'field',
      yField: 'value',
      seriesField: 'field',
      appendPadding: getPadding(chart),
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
        }
      ]
    }
    const options = this.setupOptions(chart, baseOptions)
    // custom color
    // options.color = antVCustomColor(chart)

    // 开始渲染
    const plot = new G2Rose(container, options)

    plot.on('interval:click', action)

    return plot
  }

  protected configBasicStyle(chart: Chart, options: RoseOptions): RoseOptions {
    const { basicStyle } = parseJson(chart.customAttr)
    return {
      ...options,
      radius: basicStyle.radius / 100
    }
  }

  protected configLabel(chart: Chart, options: RoseOptions): RoseOptions {
    const { label: labelAttr } = parseJson(chart.customAttr)
    if (!labelAttr.show) {
      return {
        ...options,
        label: false
      }
    }
    const total = options.data?.reduce((pre, next) => add(pre, next.value ?? 0), 0)
    const labelOptions: Label = {
      autoRotate: true,
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
          const percentage = `${(Math.round((param.value / total) * 10000) / 100).toFixed(
            labelAttr.reserveDecimalCount
          )}%`
          if (labelAttr.showDimension && labelAttr.showQuota) {
            contentItems.push(`(${percentage})`)
          } else {
            contentItems.push(percentage)
          }
        }
        res = contentItems.join('\n')
        return res
      }
    }
    if (labelAttr.position === 'inner') {
      labelOptions.offset = -10
    }
    return {
      ...options,
      label: labelOptions
    }
  }

  protected configTooltip(chart: Chart, options: RoseOptions): RoseOptions {
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

  setupDefaultOptions(chart: ChartObj): ChartObj {
    const customAttr = chart.customAttr
    const { label } = customAttr
    if (!['inner', 'outer'].includes(label.position)) {
      label.position = 'outer'
    }
    return chart
  }

  protected setupOptions(chart: Chart, options: RoseOptions): RoseOptions {
    return flow(
      this.configBasicStyle,
      this.configTheme,
      this.configLabel,
      this.configLegend,
      this.configTooltip
    )(chart, options)
  }

  constructor(name = 'pie-rose') {
    super(name, [])
  }
}

export class RoseDonut extends Rose {
  propertyInner: EditorPropertyInner = {
    ...PIE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'radius', 'innerRadius']
  }
  protected configBasicStyle(chart: Chart, options: RoseOptions): RoseOptions {
    const customAttr = parseJson(chart.customAttr)
    return {
      ...options,
      radius: customAttr.basicStyle.radius / 100,
      innerRadius: customAttr.basicStyle.innerRadius / 100
    }
  }

  constructor() {
    super('pie-donut-rose')
  }
}
