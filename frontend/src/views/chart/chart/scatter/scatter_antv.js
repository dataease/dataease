import {
  getTheme,
  getLabel,
  getTooltip,
  getLegend,
  getXAxis,
  getYAxis,
  getPadding,
  getSlider,
  getAnalyse
} from '@/views/chart/chart/common/common_antv'

import { Scatter } from '@antv/g2plot'
import { antVCustomColor } from '@/views/chart/chart/util'

export function baseScatterOptionAntV(plot, container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  const xAxis = getXAxis(chart)
  const yAxis = getYAxis(chart)
  // data
  const data = chart.data.datas
  // config
  const slider = getSlider(chart)
  const analyse = getAnalyse(chart)
  // options
  const options = {
    theme: theme,
    data: data,
    xField: 'field',
    yField: 'value',
    colorField: 'category',
    appendPadding: getPadding(chart),
    label: label,
    tooltip: tooltip,
    legend: legend,
    xAxis: xAxis,
    yAxis: yAxis,
    slider: slider,
    annotations: analyse,
    pieStyle: {
      lineWidth: 0
    },
    interactions: [
      {
        type: 'legend-active', cfg: {
          start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
          end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
        }
      },
      {
        type: 'legend-filter', cfg: {
          start: [{ trigger: 'legend-item:click', action: ['list-unchecked:toggle', 'data-filter:filter', 'element-active:reset', 'element-highlight:reset'] }]
        }
      },
      {
        type: 'tooltip', cfg: {
          start: [{ trigger: 'point:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'point:mouseleave', action: 'tooltip:hide' }]
        }
      }
    ]
  }
  // size
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      if (JSON.parse(chart.extBubble).length > 0) {
        options.size = [5, 30]
        options.sizeField = 'popSize'
      } else {
        options.size = parseInt(s.scatterSymbolSize)
      }
      options.shape = s.scatterSymbol
    }
  }
  // custom color
  options.color = antVCustomColor(chart)

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Scatter(container, options)

  plot.off('point:click')
  plot.on('point:click', action)

  return plot
}
