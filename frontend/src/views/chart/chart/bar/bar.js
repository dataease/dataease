import { hexColorToRGBA } from '../util.js'
import { componentStyle } from '../common/common'

export function baseBarOption(chart_option, chart) {
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
        if (customAttr.size.barDefault) {
          y.barWidth = null
          y.barGap = null
        } else {
          y.barWidth = customAttr.size.barWidth
          y.barGap = customAttr.size.barGap
        }
      }
      y.type = 'bar'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart)
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
  }
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    chart_option.yAxis.data = chart.data.x
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % 9], customAttr.color.alpha)
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
      y.type = 'bar'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart)
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
