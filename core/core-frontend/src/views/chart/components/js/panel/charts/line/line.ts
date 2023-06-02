import { G2PlotChartView, G2PlotDrawOptions } from '@/views/chart/components/js/panel/types'
import { Line as G2Line, LineOptions } from '@antv/g2plot'
import { getAnalyse, getPadding, getSlider } from '../../common/common_antv'
import {
  getLabel,
  getLegend,
  getTheme,
  getTooltip,
  getXAxis,
  getYAxis
} from '@/views/chart/components/js/panel/common/common_antv'
import { antVCustomColor, handleEmptyDataStrategy } from '@/views/chart/components/js/util'
import _ from 'lodash'

/**
 * 折线图
 */
export class Line extends G2PlotChartView<LineOptions, G2Line> {
  drawChart(drawOptions: G2PlotDrawOptions<G2Line>) {
    const chart = drawOptions.chart
    const theme = getTheme(chart)
    // attr
    const label = getLabel(chart)
    const tooltip = getTooltip(chart)
    // style
    const legend = getLegend(chart)
    const xAxis = getXAxis(chart)
    const yAxis = getYAxis(chart)
    // data
    const data = _.cloneDeep(chart.data.data)
    // config
    const slider = getSlider(chart)
    const analyse = getAnalyse(chart)
    // size
    let customAttr: DeepPartial<ChartAttr> = {}
    let lineSmooth = undefined
    let point = undefined
    let lineStyle = undefined
    if (chart.customAttr) {
      customAttr = JSON.parse(JSON.stringify(chart.customAttr))
      if (customAttr.size) {
        const s = JSON.parse(JSON.stringify(customAttr.size)) as ChartSizeAttr
        lineSmooth = s.lineSmooth
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
    const options = {
      theme: theme,
      data: data,
      xField: 'field',
      yField: 'value',
      seriesField: 'category',
      appendPadding: getPadding(chart),
      label: label,
      tooltip: tooltip,
      legend: legend,
      xAxis: xAxis,
      yAxis: yAxis,
      slider: slider,
      color: color,
      annotations: analyse,
      point: point,
      lineStyle: lineStyle,
      lineSmooth: lineSmooth,
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
    // 处理空值
    if (chart.senior) {
      let emptyDataStrategy = JSON.parse(JSON.stringify(chart.senior))?.functionCfg
        ?.emptyDataStrategy
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
}
