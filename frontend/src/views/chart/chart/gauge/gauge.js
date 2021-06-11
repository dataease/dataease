import { componentStyle } from '../common/common'
import { hexColorToRGBA } from '@/views/chart/chart/util'

export function baseGaugeOption(chart_option, chart) {
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
    }
  }
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    if (chart.data.series.length > 0) {
      chart_option.series[0].name = chart.data.series[0].name
      // size
      if (customAttr.size) {
        chart_option.series[0].min = customAttr.size.gaugeMin
        chart_option.series[0].max = customAttr.size.gaugeMax
        chart_option.series[0].startAngle = customAttr.size.gaugeStartAngle
        chart_option.series[0].endAngle = customAttr.size.gaugeEndAngle
      }
      // detail
      if (customAttr.label) {
        const label = JSON.parse(JSON.stringify(customAttr.label))
        chart_option.series[0].detail.show = label.show
        chart_option.series[0].detail.fontSize = label.fontSize
        chart_option.series[0].detail.color = label.color
        chart_option.series[0].detail.formatter = label.gaugeFormatter
      }
      chart_option.series[0].type = 'gauge'
      // color
      chart_option.series[0].itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha)
      }
      // data只取第一个
      const y = {
        // name: chart.data.x[0],
        name: chart.data.series[0].name,
        value: chart.data.series[0].data[0]
      }
      chart_option.series[0].data.push(y)
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart)
  return chart_option
}
