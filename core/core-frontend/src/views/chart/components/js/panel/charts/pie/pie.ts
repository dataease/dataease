import { G2PlotChartView, G2PlotDrawOptions } from '@/views/chart/components/js/panel/types'
import { Pie as G2Pie, PieOptions } from '@antv/g2plot'
import { antVCustomColor, flow, parseJson } from '@/views/chart/components/js/util'
import { getPadding } from '@/views/chart/components/js/panel/common/common_antv'

const DEFAULT_DATA = []
export class Pie extends G2PlotChartView<PieOptions, G2Pie> {
  drawChart(drawOptions: G2PlotDrawOptions<G2Pie>): G2Pie {
    const chart = drawOptions.chart
    // data
    const data = chart.data.data
    // size
    let customAttr, radius, innerRadius
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      if (customAttr.size) {
        const s = customAttr.size
        radius = s.pieOuterRadius / 100
        innerRadius = s.pieInnerRadius / 100
      }
    }
    // custom color
    const color = antVCustomColor(chart)
    // options
    const initOptions: PieOptions = {
      data: data,
      angleField: 'value',
      colorField: 'field',
      appendPadding: getPadding(chart),
      color,
      radius,
      innerRadius,
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

    // 开始渲染
    if (drawOptions.chartObj) {
      drawOptions.chartObj.destroy()
    }
    drawOptions.chartObj = new G2Pie(drawOptions.container, options)

    drawOptions.chartObj.off('interval:click')
    drawOptions.chartObj.on('interval:click', drawOptions.action)

    return drawOptions.chartObj
  }

  protected setupOptions(chart: Chart, options: PieOptions): PieOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configLegend
    )(chart, options)
  }

  constructor() {
    super('pie', DEFAULT_DATA)
  }
}

export class PieDonut extends Pie {
  constructor() {
    super()
    this.name = 'pie-donut'
  }
}
