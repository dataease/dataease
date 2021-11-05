import {
  getLabel,
  getLegend,
  getPadding,
  getTheme,
  getTooltip,
  getXAxis,
  getYAxis
} from '@/views/chart/chart/common/common_antv'
import { Waterfall } from '@antv/g2plot'

export function baseWaterfallOptionAntV(plot, container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const label = getLabel(chart)
  const tooltip = getTooltip(chart)
  // style
  // const legend = getLegend(chart)
  const xAxis = getXAxis(chart)
  const yAxis = getYAxis(chart)
  // fix yAxis
  if (yAxis) {
    yAxis.min = yAxis.minLimit
    yAxis.max = yAxis.maxLimit
    delete yAxis.minLimit
    delete yAxis.maxLimit
  }
  // data
  const data = chart.data.datas
  // total
  const total = {
    label: '合计',
    style: {
      fill: theme.styleSheet.paletteQualitative10[2]
    }
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
    legend: {
      items: [
        { name: '增加', marker: {
          style: {
            fill: theme.styleSheet.paletteQualitative10[0]
          }
        }},
        { name: '减少', marker: {
          style: {
            fill: theme.styleSheet.paletteQualitative10[1]
          }
        }},
        { name: '合计', marker: {
          style: {
            fill: theme.styleSheet.paletteQualitative10[2]
          }
        }}
      ]
    },
    xAxis: xAxis,
    yAxis: yAxis,
    risingFill: theme.styleSheet.paletteQualitative10[0],
    fallingFill: theme.styleSheet.paletteQualitative10[1],
    total: total,
    interactions: [
      {
        type: 'element-active', cfg: {
          start: [{ trigger: 'element:mouseenter', action: ['element-highlight:highlight', 'element-active:reset', 'cursor:pointer'] }],
          end: [{ trigger: 'element:mouseleave', action: ['element-highlight:reset', 'element-active:reset', 'cursor:default'] }]
        }
      },
      // {
      //   type: 'legend-active', cfg: {
      //     start: [{ trigger: 'legend-item:mouseenter', action: ['element-active:reset'] }],
      //     end: [{ trigger: 'legend-item:mouseleave', action: ['element-active:reset'] }]
      //   }
      // },
      // {
      //   type: 'legend-filter', cfg: {
      //     start: [{ trigger: 'legend-item:click', action: ['list-unchecked:toggle', 'data-filter:filter', 'element-active:reset', 'element-highlight:reset'] }]
      //   }
      // },
      {
        type: 'tooltip', cfg: {
          start: [{ trigger: 'interval:mousemove', action: 'tooltip:show' }],
          end: [{ trigger: 'interval:mouseleave', action: 'tooltip:hide' }]
        }
      }
      // {
      //   type: 'active-region', cfg: {
      //     start: [{ trigger: 'interval:mousemove', action: 'active-region:show' }],
      //     end: [{ trigger: 'interval:mouseleave', action: 'active-region:hide' }]
      //   }
      // }
    ]
  }
  // size
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.size) {
      const s = JSON.parse(JSON.stringify(customAttr.size))
      if (s.barDefault) {
        delete options.marginRatio
      } else {
        options.marginRatio = s.barGap
      }
    }
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new Waterfall(container, options)

  plot.off('interval:click')
  plot.on('interval:click', action)

  return plot
}
