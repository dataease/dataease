import { getLabel, getLegend, getPadding, getTheme, getTooltip } from '@/views/chart/chart/common/common_antv'
import { Treemap } from '@antv/g2plot'

export function baseTreemapOptionAntV(plot, container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  // data
  const data = chart.data.datas
  // options
  const options = {
    theme: theme,
    data: {
      name: 'root',
      children: data
    },
    colorField: 'name',
    appendPadding: getPadding(chart),
    label: label,
    tooltip: tooltip,
    legend: legend,
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
          start: [{ trigger: 'element:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'element:mouseleave', action: 'tooltip:hide' }]
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
    }
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Treemap(container, options)

  plot.off('polygon:click')
  plot.on('polygon:click', action)

  return plot
}
