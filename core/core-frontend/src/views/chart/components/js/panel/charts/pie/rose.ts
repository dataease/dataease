import {
  G2PlotChartView,
  G2PlotDrawOptions
} from '@/views/chart/components/js/panel/types/impl/g2plot'
import { RoseOptions, Rose as G2Rose, Datum } from '@antv/g2plot'
import { PIE_AXIS_TYPE, PIE_EDITOR_PROPERTY, PIE_EDITOR_PROPERTY_INNER } from './common'
import { getPadding } from '@/views/chart/components/js/panel/common/common_antv'
import { parseJson, flow } from '@/views/chart/components/js/util'
import { Label } from '@antv/g2plot/lib/types/label'
import { formatterItem, valueFormatter } from '@/views/chart/components/js/formatter'

export class Rose extends G2PlotChartView<RoseOptions, G2Rose> {
  axis: AxisType[] = PIE_AXIS_TYPE
  properties: EditorProperty[] = PIE_EDITOR_PROPERTY
  propertyInner: EditorPropertyInner = {
    ...PIE_EDITOR_PROPERTY_INNER,
    'basic-style-selector': ['colors', 'alpha', 'radius']
  }

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
      radius: basicStyle.radius
    }
  }

  protected configLabel(chart: Chart, options: RoseOptions): RoseOptions {
    const { label } = parseJson(chart.customAttr)
    if (!label.show) {
      return {
        ...options,
        label: false
      }
    }
    const yAxis = chart.yAxis
    const labelOptions: Label = {
      autoRotate: true,
      style: {
        fill: label.color,
        fontSize: label.fontSize
      },
      formatter: (param: Datum) => {
        let res = param.value
        for (let i = 0; i < yAxis.length; i++) {
          const field = yAxis[i]
          if (field.name === param.category) {
            let formatterCfg = formatterItem
            if (field.formatterCfg) {
              formatterCfg = field.formatterCfg
            }
            const quotaValue = valueFormatter(param.value, formatterCfg)
            res = `${param.value} ${quotaValue}`
          }
        }
        return res
      }
    }
    if (label.position === 'inner') {
      labelOptions.offsetY = -10
    }
    return {
      ...options,
      label: labelOptions
    }
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

  constructor() {
    super('pie-rose', [])
  }
}
