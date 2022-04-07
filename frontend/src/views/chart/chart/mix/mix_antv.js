import {
  getLabel,
  getLegend,
  getPadding,
  getTheme,
  getTooltip,
  getXAxis,
  getYAxis,
  getYAxisExt
} from '@/views/chart/chart/common/common_antv'
import { Mix } from '@antv/g2plot'
import { hexColorToRGBA } from '@/views/chart/chart/util'

export function baseMixOptionAntV(plot, container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  const xAxis = getXAxis(chart)
  const yAxis = getYAxis(chart)
  const yAxisExt = getYAxisExt(chart)
  // data
  const data = chart.data.datas
  const plots = []
  // color
  let customAttr = {}
  const colors = []
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // color
    if (customAttr.color) {
      const c = JSON.parse(JSON.stringify(customAttr.color))
      c.colors.forEach(ele => {
        colors.push(hexColorToRGBA(ele, c.alpha))
      })
    }
  }
  for (let i = 0; i < data.length; i++) {
    const d = data[i]
    const o = {
      type: '',
      options: {
        color: colors[i % colors.length],
        data: d.data,
        xField: 'field',
        yField: 'value',
        // seriesField: 'category',
        label: label,
        tooltip: tooltip,
        legend: legend,
        xAxis: i > 0 ? false : xAxis,
        yAxis: (i >= JSON.parse(chart.yaxis).length) ? yAxisExt : yAxis,
        interactions: [
          {
            type: 'element-active', cfg: {
              start: [{
                trigger: 'element:mouseenter',
                action: ['element-highlight:highlight', 'element-active:reset', 'cursor:pointer']
              }],
              end: [{
                trigger: 'element:mouseleave',
                action: ['element-highlight:reset', 'element-active:reset', 'cursor:default']
              }]
            }
          },
          {
            type: 'legend-active', cfg: {
              start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
              end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
            }
          },
          {
            type: 'legend-filter', cfg: {
              start: [{
                trigger: 'legend-item:click',
                action: ['list-unchecked:toggle', 'data-filter:filter', 'element-active:reset', 'element-highlight:reset']
              }]
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
              start: [{ trigger: 'element:mousemove', action: 'active-region:show' }],
              end: [{ trigger: 'element:mouseleave', action: 'active-region:hide' }]
            }
          }
        ]
      }
    }
    transChart(chart, d, o)
    plots.push(o)
  }

  // options
  const options = {
    theme: theme,
    appendPadding: getPadding(chart),
    syncViewPadding: true,
    plots: plots,
    legend: legend
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Mix(container, options)

  plot.off('point:click')
  plot.on('point:click', action)
  plot.off('interval:click')
  plot.on('interval:click', action)

  return plot
}

function transChart(chart, d, o) {
  // size
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      if (d.type === 'bar') { // bar
        o.type = 'column'
        if (s.barDefault) {
          delete o.options.marginRatio
        } else {
          o.options.marginRatio = s.barGap
        }
      } else if (d.type === 'line') { // line
        o.type = 'line'
        o.options.smooth = s.lineSmooth
        o.options.point = {
          size: parseInt(s.lineSymbolSize),
          shape: s.lineSymbol
        }
        o.options.lineStyle = {
          lineWidth: parseInt(s.lineWidth)
        }
      } else if (d.type === 'scatter') { // scatter
        o.type = 'scatter'
        o.options.size = parseInt(s.scatterSymbolSize)
        o.options.shape = s.scatterSymbol
      }
    }
  }
}
