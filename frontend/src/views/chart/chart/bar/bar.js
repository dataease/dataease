import { hexColorToRGBA } from '../util.js'
import { componentStyle, seniorCfg } from '../common/common'

export function baseBarOption(chart_option, chart, cstyle = {}) {
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
    chart_option.xAxis.data = chart.data.x
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        // color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
        color: {
          type: 'linear',
          x: 0,
          y: 1,
          x2: 0,
          y2: 0,
          colorStops: [{
            offset: 0,  // 0% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
          }, {
            offset: 1, // 100% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[(i+1) % customAttr.color.colors.length], customAttr.color.alpha)
          }],
          global: false // 缺省为 false
        }
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
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart,cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function stackBarOption(chart_option, chart,cstyle = {}) {
  baseBarOption(chart_option, chart,cstyle)

  // ext
  chart_option.series.forEach(function(s) {
    s.stack = 'stack'
    s.emphasis = {
      focus: 'series'
    }
  })
  return chart_option
}

export function horizontalBarOption(chart_option, chart,cstyle = {}) {
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
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart,cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function horizontalStackBarOption(chart_option, chart,cstyle = {}) {
  horizontalBarOption(chart_option, chart,cstyle)

  // ext
  chart_option.series.forEach(function(s) {
    s.stack = 'stack'
    s.emphasis = {
      focus: 'series'
    }
  })
  return chart_option
}

export function basePictorialBarOption(chart_option, chart, cstyle = {}) {
  console.log('测试1',chart_option,chart)
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
      y.label = {
        show: true,
        position: 'right',
        offset: [10, 0],
        fontSize: 16
      }
      y.type = 'pictorialBar'

      y.symbolRepeat = 'fixed'
      y.symbolMargin = '5%'
      y.symbolClip = true
      y.symbolSize = 20
      y.symbolBoundingData = 100
      
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }

  // console.log(chart_option);
  componentStyle(chart_option, chart,cstyle)
  seniorCfg(chart_option, chart)
  return chart_option

}
