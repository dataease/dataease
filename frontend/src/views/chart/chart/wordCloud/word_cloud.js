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
  const data = chart.data.data
  // size
  let wordSizeRange
  let wordSpacing
  if (chart.customAttr) {
    const customAttr = JSON.parse(chart.customAttr)
    wordSizeRange = customAttr?.size?.wordSizeRange ?? [8, 32]
    wordSpacing = customAttr?.size?.wordSpacing ?? 6
  }
  // options
  const options = {
    theme: theme,
    data: data,
    wordField: 'field',
    weightField: 'value',
    colorField: 'field',
    wordStyle: {
      fontFamily: 'Verdana',
      fontSize: wordSizeRange,
      rotation: [0, 0],
      padding: wordSpacing
    },
    random: () => 0.5,
    appendPadding: getPadding(chart),
    tooltip: tooltip,
    legend: false,
    interactions: [
    ]
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new WordCloud(container, options)
  plot.off('point:click')
  plot.on('point:click', action)
  return plot
}
