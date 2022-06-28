import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle, seniorCfg } from '../common/common'
import { BASE_ECHARTS_SELECT, DEFAULT_TOOLTIP } from '@/views/chart/chart/chart'

export function baseLineOption(chart_option, chart) {
  // 处理shape attr
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.color) {
      chart_option.color = customAttr.color.colors
    }
    // tooltip
    if (customAttr.tooltip) {
      const tooltip = JSON.parse(JSON.stringify(customAttr.tooltip))
      const reg = new RegExp('\n', 'g')
      tooltip.formatter = tooltip.formatter.replace(reg, '<br/>')
      chart_option.tooltip = tooltip

      const bgColor = tooltip.backgroundColor ? tooltip.backgroundColor : DEFAULT_TOOLTIP.backgroundColor
      chart_option.tooltip.backgroundColor = bgColor
      chart_option.tooltip.borderColor = bgColor
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
        color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
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
        if (customAttr.size.lineArea) {
          y.areaStyle = {
            opacity: 0.6
          }
        } else {
          delete y.areaStyle
        }
      }
      y.selectedMode = true
      y.select = BASE_ECHARTS_SELECT
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      y.type = 'line'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  componentStyle(chart_option, chart)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function stackLineOption(chart_option, chart) {
  baseLineOption(chart_option, chart)

  // ext
  // chart_option.tooltip.trigger = 'axis'
  chart_option.series.forEach(function(s) {
    s.stack = 'stack'
  })
  return chart_option
}

