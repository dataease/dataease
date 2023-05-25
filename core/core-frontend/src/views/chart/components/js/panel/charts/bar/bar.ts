import { Column, ColumnOptions } from '@antv/g2plot/lib/plots/column'
import _ from 'lodash'
import {
  getLabel,
  getTheme,
  getTooltip,
  getLegend,
  getXAxis,
  getYAxis,
  getSlider,
  getAnalyse
} from '@/views/chart/components/js/panel/common/common_antv'
import { G2PlotDrawOptions, G2PlotChartView } from '@/views/chart/components/js/panel/types'

const DEFAULT_DATA_BAR: any[] = []

export class Bar extends G2PlotChartView<ColumnOptions, Column> {
  drawChart(drawOptions: G2PlotDrawOptions<Column>): Column {
    if (drawOptions.chart?.data) {
      // theme
      const theme = getTheme(drawOptions.chart)
      // label
      const label = getLabel(drawOptions.chart)
      // tooltip
      const tooltip = getTooltip(drawOptions.chart)
      // style
      const legend = getLegend(drawOptions.chart)
      const xAxis = getXAxis(drawOptions.chart)
      const yAxis = getYAxis(drawOptions.chart)

      // config
      const slider = getSlider(drawOptions.chart)
      const analyse = getAnalyse(drawOptions.chart)

      // data
      const data = _.cloneDeep(drawOptions?.chart?.data?.data)
      if (!data) return drawOptions.chartObj
      const options: ColumnOptions = {
        theme: theme,
        label: label,
        tooltip: tooltip,
        legend: legend,
        xAxis: xAxis,
        yAxis: yAxis,
        slider: slider,
        annotations: analyse,
        data: data,
        xField: 'field',
        yField: 'value',
        seriesField: 'category',
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

      if (drawOptions.chartObj) {
        drawOptions.chartObj.destroy()
      }
      drawOptions.chartObj = new Column(drawOptions.container, options)
      return drawOptions.chartObj
    }
  }
  constructor() {
    super('bar', DEFAULT_DATA_BAR)
  }
}
