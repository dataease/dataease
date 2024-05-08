import {
  configPlotTooltipEvent,
  getLabel,
  getLegend,
  getPadding,
  getTheme,
  getTooltip
} from '@/views/chart/chart/common/common_antv'
import { Funnel } from '@antv/g2plot'
import { antVCustomColor } from '@/views/chart/chart/util'

export function baseFunnelOptionAntV(container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  const legend = getLegend(chart)
  // data
  const data = chart.data.data
  // conversion tag
  const labelAttr = JSON.parse(chart.customAttr).label
  let conversionTag = labelAttr.showConversion
  if (conversionTag) {
    conversionTag = {
      formatter: datum => {
        const rate = ((datum[Funnel.CONVERSATION_FIELD][1] / datum[Funnel.CONVERSATION_FIELD][0]) * 100).toFixed(2)
        return `${labelAttr.conversionLabel ?? ''}${rate}%`
      }
    }
  }
  // options
  const options = {
    theme: theme,
    data: data,
    xField: 'field',
    yField: 'value',
    appendPadding: getPadding(chart),
    label: label,
    tooltip: tooltip,
    legend: legend,
    conversionTag,
    maxSize: conversionTag ? 0.8 : 1,
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
          start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
        }
      }
    ]
  }
  // custom color
  options.color = antVCustomColor(chart)

  const plot = new Funnel(container, options)

  plot.on('interval:click', action)
  // 处理 tooltip 被其他视图遮挡
  configPlotTooltipEvent(chart, plot)
  return plot
}

