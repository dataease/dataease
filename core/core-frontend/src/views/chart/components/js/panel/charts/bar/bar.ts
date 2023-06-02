import { Column, ColumnOptions } from '@antv/g2plot/lib/plots/column'
import _ from 'lodash'
import { G2PlotDrawOptions, G2PlotChartView } from '@/views/chart/components/js/panel/types'
import { flow } from '@/views/chart/components/js/util'

const DEFAULT_DATA: any[] = []

/**
 * 柱状图
 */
export class Bar extends G2PlotChartView<ColumnOptions, Column> {
  drawChart(drawOptions: G2PlotDrawOptions<Column>): Column {
    if (drawOptions.chart?.data) {
      const data = _.cloneDeep(drawOptions.chart.data?.data)
      if (!data) return drawOptions.chartObj
      const initOptions: ColumnOptions = {
        xField: 'field',
        yField: 'value',
        seriesField: 'category',
        data: data,
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
      const options: ColumnOptions = this.setupOptions(drawOptions.chart, initOptions)

      if (drawOptions.chartObj) {
        drawOptions.chartObj.destroy()
      }
      drawOptions.chartObj = new Column(drawOptions.container, options)
      return drawOptions.chartObj
    }
  }

  protected setupOptions(chart: Chart, options: ColumnOptions): ColumnOptions {
    return flow(
      this.configTheme,
      this.configLabel,
      this.configTooltip,
      this.configLegend,
      this.configXAxis,
      this.configYAxis,
      this.configSlider,
      this.configAnalyse
    )(chart, options)
  }
  constructor() {
    super('bar', DEFAULT_DATA)
  }
}
