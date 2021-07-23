import { hexColorToRGBA } from '@/views/chart/chart/util'

export function componentStyle(chart_option, chart) {
  const padding = '8px'
  if (chart.customStyle) {
    const customStyle = JSON.parse(chart.customStyle)
    if (customStyle.text) {
      chart_option.title.show = customStyle.text.show
      // 水平方向
      if (customStyle.text.hPosition === 'left') {
        chart_option.title.left = padding
      } else if (customStyle.text.hPosition === 'right') {
        chart_option.title.right = padding
      } else {
        chart_option.title.left = customStyle.text.hPosition
      }
      // 垂直方向
      if (customStyle.text.vPosition === 'top') {
        chart_option.title.top = padding
      } else if (customStyle.text.vPosition === 'bottom') {
        chart_option.title.bottom = padding
      } else {
        chart_option.title.top = customStyle.text.vPosition
      }
      const style = chart_option.title.textStyle ? chart_option.title.textStyle : {}
      style.fontSize = customStyle.text.fontSize
      style.color = customStyle.text.color
      customStyle.text.isItalic ? style.fontStyle = 'italic' : style.fontStyle = 'normal'
      customStyle.text.isBolder ? style.fontWeight = 'bold' : style.fontWeight = 'normal'
      chart_option.title.textStyle = style
    }
    if (customStyle.legend && chart_option.legend) {
      chart_option.legend.show = customStyle.legend.show
      // 水平方向
      if (customStyle.legend.hPosition === 'left') {
        chart_option.legend.left = padding
      } else if (customStyle.legend.hPosition === 'right') {
        chart_option.legend.right = padding
      } else {
        chart_option.legend.left = customStyle.legend.hPosition
      }
      // 垂直方向
      if (customStyle.legend.vPosition === 'top') {
        chart_option.legend.top = padding
      } else if (customStyle.legend.vPosition === 'bottom') {
        chart_option.legend.bottom = padding
      } else {
        chart_option.legend.top = customStyle.legend.vPosition
      }
      chart_option.legend.orient = customStyle.legend.orient
      chart_option.legend.icon = customStyle.legend.icon
      chart_option.legend.textStyle = customStyle.legend.textStyle
    }
    if (customStyle.xAxis && (chart.type.includes('bar') || chart.type.includes('line'))) {
      chart_option.xAxis.show = customStyle.xAxis.show
      chart_option.xAxis.position = customStyle.xAxis.position
      chart_option.xAxis.name = customStyle.xAxis.name
      chart_option.xAxis.axisLabel = customStyle.xAxis.axisLabel
      chart_option.xAxis.splitLine = customStyle.xAxis.splitLine
      chart_option.xAxis.nameTextStyle = customStyle.xAxis.nameTextStyle
    }
    if (customStyle.yAxis && (chart.type.includes('bar') || chart.type.includes('line'))) {
      chart_option.yAxis.show = customStyle.yAxis.show
      chart_option.yAxis.position = customStyle.yAxis.position
      chart_option.yAxis.name = customStyle.yAxis.name
      chart_option.yAxis.axisLabel = customStyle.yAxis.axisLabel
      chart_option.yAxis.splitLine = customStyle.yAxis.splitLine
      chart_option.yAxis.nameTextStyle = customStyle.yAxis.nameTextStyle
    }
    if (customStyle.split && chart.type.includes('radar')) {
      chart_option.radar.name = customStyle.split.name
      chart_option.radar.splitNumber = customStyle.split.splitNumber
      chart_option.radar.axisLine = customStyle.split.axisLine
      chart_option.radar.axisTick = customStyle.split.axisTick
      chart_option.radar.axisLabel = customStyle.split.axisLabel
      chart_option.radar.splitLine = customStyle.split.splitLine
      chart_option.radar.splitArea = customStyle.split.splitArea
    }
    if (customStyle.background) {
      chart_option.backgroundColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
  }
}
