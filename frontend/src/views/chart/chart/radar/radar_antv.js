import { getLabel, getLegend, getPadding, getTheme, getTooltip } from '@/views/chart/chart/common/common_antv'
import { Radar } from '@antv/g2plot'
import { antVCustomColor } from '@/views/chart/chart/util'

export function baseRadarOptionAntV(plot, container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  // data
  const data = chart.data.datas

  const xAxis = {
    tickLine: null,
    line: null
  }
  const yAxis = {
    tickLine: null,
    line: null,
    label: null
  }
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
    point: {
      size: 4,
      shape: 'circle',
      style: {
        fill: null
      }
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
      },
      {
        type: 'active-region', cfg: {
          start: [{ trigger: 'point:mousemove', action: 'active-region:show' }],
          end: [{ trigger: 'point:mouseleave', action: 'active-region:hide' }]
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
      options.radius = parseFloat(parseInt(s.radarSize) / 100)
      options.radius = options.radius > 1 ? 1 : options.radius
      if (s.radarShape === 'polygon') {
        yAxis.grid = {
          line: {
            type: 'line'
          }
        }
      } else {
        yAxis.grid && yAxis.grid.line && delete yAxis.grid.line.type
      }
    }
  }
  // axis
  let customStyle = {}
  if (chart.customStyle) {
    customStyle = JSON.parse(chart.customStyle)
    if (customStyle.split) {
      const s = JSON.parse(JSON.stringify(customStyle.split))
      if (s.name.show) {
        xAxis.label = {
          style: {
            fill: s.name.color,
            fontSize: parseInt(s.name.fontSize)
          }
        }
      } else {
        xAxis.label = null
      }

      xAxis.grid = {
        line: {
          style: {
            stroke: s.axisLine.lineStyle.color
          }
        }
      }

      if (yAxis.grid && yAxis.grid.line) {
        yAxis.grid.line.style = {
          stroke: s.axisLine.lineStyle.color
        }
      } else {
        yAxis.grid = {
          line: {
            style: {
              stroke: s.axisLine.lineStyle.color
            }
          }
        }
      }
    }
  }
  options.xAxis = xAxis
  options.yAxis = yAxis

  // custom color
  options.color = antVCustomColor(chart)

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Radar(container, options)

  plot.off('point:click')
  plot.on('point:click', action)

  return plot
}

