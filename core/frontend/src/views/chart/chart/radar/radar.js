import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle } from '../common/common'
import { BASE_ECHARTS_SELECT, DEFAULT_TOOLTIP } from '@/views/chart/chart/chart'

export function baseRadarOption(chart_option, chart) {
  // 处理shape attr
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.color) {
      chart_option.color = customAttr.color.colors
    }
    // size
    if (customAttr.size) {
      chart_option.radar.shape = customAttr.size.radarShape
      chart_option.radar.radius = (customAttr.size.radarSize ? customAttr.size.radarSize : 80) + '%'
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
    const maxValues = []
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      if (y.data.length === 0) {
        continue
      }
      // color
      y.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      chart_option.legend.data.push(y.name)

      const d = {
        name: y.name,
        type: 'radar',
        data: [
          {
            value: y.data,
            name: y.name,
            label: y.label
          }
        ],
        selectedMode: true,
        select: BASE_ECHARTS_SELECT
      }
      y.value = JSON.parse(JSON.stringify(y.data))
      chart_option.series.push(d)

      maxValues.push(Math.max.apply(null, y.value))
    }
    const max = Math.max.apply(null, maxValues)
    chart.data.x.forEach(function(ele) {
      chart_option.radar.indicator.push({ name: ele, max: max })
    })
  }
  componentStyle(chart_option, chart)
  return chart_option
}

