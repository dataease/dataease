import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle } from '../common/common'

export function baseFunnelOption(chart_option, chart) {
  // 处理shape attr
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.color) {
      chart_option.color = customAttr.color.colors
    }
  }
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    if (chart.data.series.length > 0) {
      chart_option.series[0].name = chart.data.series[0].name
      // size
      if (customAttr.size) {
        chart_option.series[0].width = customAttr.size.funnelWidth + '%'
      }
      const valueArr = chart.data.series[0].data
      // max value
      chart_option.series[0].max = Math.max.apply(Math, valueArr)
      for (let i = 0; i < valueArr.length; i++) {
        const y = {
          name: chart.data.x[i],
          value: valueArr[i]
        }
        // color
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % 9], customAttr.color.alpha)
        }
        y.type = 'funnel'
        chart_option.series[0].data.push(y)
      }
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart)
  return chart_option
}

