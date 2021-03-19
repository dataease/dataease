import { hexColorToRGBA } from '@/views/chart/chart/util'

export function baseLineOption(chart_option, chart) {
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
    chart_option.xAxis.data = chart.data.x
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % 9], customAttr.color.alpha)
      }
      // size
      if (customAttr.size) {
        y.symbol = customAttr.size.lineSymbol
        y.symbolSize = customAttr.size.lineSymbolSize
        y.lineStyle = {
          width: customAttr.size.lineWidth,
          type: customAttr.size.lineType
        }
        y.smooth = customAttr.size.lineSmooth
      }
      y.type = 'line'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  // console.log(chart_option);
  return chart_option
}

