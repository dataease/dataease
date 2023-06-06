import { G2PlotChartView, G2PlotDrawOptions } from '@/views/chart/components/js/panel/types'
import { Line as G2Line, LineOptions } from '@antv/g2plot'
import { getPadding } from '../../common/common_antv'
import {
  antVCustomColor,
  flow,
  handleEmptyDataStrategy,
  parseJson
} from '@/views/chart/components/js/util'
import _ from 'lodash'

const DEFAULT_DATA = []
/**
 * 折线图
 */
export class Line extends G2PlotChartView<LineOptions, G2Line> {
  drawChart(drawOptions: G2PlotDrawOptions<G2Line>) {
    const chart = drawOptions.chart
    // data
    const data = _.cloneDeep(chart.data.data)
    // size
    let customAttr: DeepPartial<ChartAttr> = {}
    let smooth, point, lineStyle
    if (chart.customAttr) {
      customAttr = parseJson(chart.customAttr)
      if (customAttr.size) {
        const s = JSON.parse(JSON.stringify(customAttr.size)) as ChartSizeAttr
        smooth = s.lineSmooth
        point = {
          size: s.lineSymbolSize,
          shape: s.lineSymbol
        }
        lineStyle = {
          lineWidth: s.lineWidth
        }
      }
    }
    // custom color
    const color = antVCustomColor(chart)
    // options
    const initOptions: LineOptions = {
      data: data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      appendPadding: getPadding(chart),
      color,
      point,
      lineStyle,
      smooth,
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
            start: [{ trigger: 'point:mousemove', action: 'tooltip:show' }],
            end: [{ trigger: 'point:mouseleave', action: 'tooltip:hide' }]
          }
        },
        {
          type: 'active-region',
          cfg: {
            start: [{ trigger: 'element:mousemove', action: 'active-region:show' }],
            end: [{ trigger: 'element:mouseleave', action: 'active-region:hide' }]
          }
        }
      ]
    }
    const options = this.setupOptions(chart, initOptions)
    // 处理空值
    if (chart.senior) {
      let emptyDataStrategy = parseJson(chart.senior)?.functionCfg?.emptyDataStrategy
      if (!emptyDataStrategy) {
        emptyDataStrategy = 'breakLine'
      }
      handleEmptyDataStrategy(emptyDataStrategy, chart, data, options)
    }
    // 开始渲染
    if (drawOptions.chartObj) {
      drawOptions.chartObj.destroy()
    }
    drawOptions.chartObj = new G2Line(drawOptions.container, options)

    drawOptions.chartObj.off('point:click')
    drawOptions.chartObj.on('point:click', drawOptions.action)

    return drawOptions.chartObj
  }

  protected setupOptions(chart: Chart, options: LineOptions): LineOptions {
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
    super('line', DEFAULT_DATA)
  }
}
