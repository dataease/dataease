import { hexColorToRGBA } from '../util.js'
import { componentStyle, seniorCfg } from '../common/common'
import { BASE_ECHARTS_SELECT, DEFAULT_TOOLTIP } from '@/views/chart/chart/chart'

export function baseBarOption(chart_option, chart, activeParam) {
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
        if (customAttr.size.barDefault) {
          y.barWidth = null
          y.barGap = null
        } else {
          y.barWidth = customAttr.size.barWidth
          y.barGap = customAttr.size.barGap
        }
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      y.type = 'bar'
      y.selectedMode = true
      y.select = BASE_ECHARTS_SELECT
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  componentStyle(chart_option, chart)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function stackBarOption(chart_option, chart) {
  baseBarOption(chart_option, chart)

  // ext
  chart_option.series.forEach(function(s) {
    s.stack = 'stack'
    s.emphasis = {
      focus: 'series'
    }
  })
  return chart_option
}

export function horizontalBarOption(chart_option, chart) {
  // 处理shape attr
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.color) {
      chart_option.color = customAttr.color.colors
    }
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
    chart_option.yAxis.data = chart.data.x
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
      }
      // size
      if (customAttr.size) {
        if (customAttr.size.barDefault) {
          y.barWidth = null
          y.barGap = null
        } else {
          y.barWidth = customAttr.size.barWidth
          y.barGap = customAttr.size.barGap
        }
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      y.type = 'bar'
      y.selectedMode = true
      y.select = BASE_ECHARTS_SELECT
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  componentStyle(chart_option, chart)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function horizontalStackBarOption(chart_option, chart) {
  horizontalBarOption(chart_option, chart)

  // ext
  chart_option.series.forEach(function(s) {
    s.stack = 'stack'
    s.emphasis = {
      focus: 'series'
    }
  })
  return chart_option
}
