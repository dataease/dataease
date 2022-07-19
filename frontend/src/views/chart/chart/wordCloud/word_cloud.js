import {
  getPadding,
  getTheme,
  getTooltip
} from '@/views/chart/chart/common/common_antv'
import { WordCloud } from '@antv/g2plot'

export function baseWordCloudOptionAntV(plot, container, chart, action,cstyle = {},scalePointWidth) {
  console.log('词云',chart,scalePointWidth)
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
      fontFamily: cstyle && cstyle.fontFamily? cstyle.fontFamily : 'Verdana',
      fontSize: [8, 32],
      rotation: [-20, 20],
      padding: 2
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
  
  //绘制图形

  if(chart.customAttr) {
    const customAttr = JSON.parse(chart.customAttr)
    // console.log('/??????',customAttr)
    if(customAttr.size) {
      // console.log(customAttr.size)
      options.wordStyle.fontSize[0] = customAttr.size.wordMin * scalePointWidth
      options.wordStyle.fontSize[1] = customAttr.size.wordMax * scalePointWidth
    }
  }

  // 开始渲染
  if (plot) {
    plot.destroy()
  }
  plot = new WordCloud(container, options)

  return plot
}
