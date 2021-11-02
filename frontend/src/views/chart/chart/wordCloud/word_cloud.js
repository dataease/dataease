import {
  getPadding,
  getTheme,
  getTooltip
} from '@/views/chart/chart/common/common_antv'
import { WordCloud } from '@antv/g2plot'

export function baseWordCloudOptionAntV(plot, container, chart, action) {
  // theme
  const theme = getTheme(chart)
  // attr
  const tooltip = getTooltip(chart)
  // data
  const data = chart.data.datas
  // options
  const options = {
    theme: theme,
    data: data,
    wordField: 'field',
    weightField: 'value',
    colorField: 'field',
    wordStyle: {
      fontFamily: 'Verdana',
      fontSize: [8, 32],
      rotation: [0, 0],
      padding: 6
    },
    random: () => 0.5,
    appendPadding: getPadding(chart),
    tooltip: tooltip,
    legend: false,
    interactions: [
      {
        type: 'element-active', cfg: {
          start: [{ trigger: 'element:mouseenter', action: ['element-highlight:highlight', 'element-active:reset', 'cursor:pointer'] }],
          end: [{ trigger: 'element:mouseleave', action: ['element-highlight:reset', 'element-active:reset', 'cursor:default'] }]
        }
      }
    ]
  }
  // size
  // let customAttr = {}
  // if (chart.customAttr) {
  //   customAttr = JSON.parse(chart.customAttr)
  //   if (customAttr.size) {
  //     const s = JSON.parse(JSON.stringify(customAttr.size))
  //     if (s.barDefault) {
  //       delete options.marginRatio
  //     } else {
  //       options.marginRatio = s.barGap
  //     }
  //   }
  // }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new WordCloud(container, options)

  return plot
}
