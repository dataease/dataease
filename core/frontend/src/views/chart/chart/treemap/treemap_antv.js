import {
  configPlotTooltipEvent,
  getLegend,
  getPadding,
  getTheme,
  getTooltip
} from '@/views/chart/chart/common/common_antv'
import { Treemap } from '@antv/g2plot'
import { formatterItem, valueFormatter } from '@/views/chart/chart/formatter'
import { parseJson } from '@/views/chart/chart/util'

export function baseTreemapOptionAntV(container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  // data
  const data = chart.data.data
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
  const plot = new Treemap(container, options)

  plot.on('polygon:click', action)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

function getLabel(chart) {
  const { label: labelAttr } = JSON.parse(chart.customAttr)
  if (!labelAttr?.show) {
    return false
  }
  const yAxis = parseJson(chart.yaxis)
  const labelFormatter = yAxis?.[0].formatterCfg ?? formatterItem
  return {
    style: {
      fill: labelAttr.color,
      fontSize: labelAttr.fontSize
    },
    formatter: function(param) {
      const labelContent = labelAttr.labelContent ?? ['quota']
      const contentItems = []
      if (labelContent.includes('dimension')) {
        contentItems.push(param.field)
      }
      if (labelContent.includes('quota')) {
        contentItems.push(valueFormatter(param.value, labelFormatter))
      }
      if (labelContent.includes('proportion')) {
        const percentage = `${(((param.value / param.parent.value) * 10000) / 100).toFixed(
          labelAttr.reserveDecimalCount
        )}%`
        contentItems.push(percentage)
      }
      return contentItems.join('\n')
    }
  }
}
