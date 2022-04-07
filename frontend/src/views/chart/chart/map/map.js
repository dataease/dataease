// import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle } from '../common/common'

export function baseMapOption(chart_option, chart) {
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
      const text = tooltip.formatter.replace(reg, '<br/>')
      tooltip.formatter = function(params) {
        const a = params.seriesName
        const b = params.name
        const c = params.value ? params.value : ''
        return text.replace(new RegExp('{a}', 'g'), a).replace(new RegExp('{b}', 'g'), b).replace(new RegExp('{c}', 'g'), c)
      }
      chart_option.tooltip = tooltip
    }
  }
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    if (chart.data.series && chart.data.series.length > 0) {
      chart_option.series[0].name = chart.data.series[0].name
      // label
      if (customAttr.label) {
        const text = customAttr.label.formatter
        chart_option.series[0].label = customAttr.label
        chart_option.series[0].label.formatter = function(params) {
          const a = params.seriesName
          const b = params.name
          const c = params.value ? params.value : ''
          return text.replace(new RegExp('{a}', 'g'), a).replace(new RegExp('{b}', 'g'), b).replace(new RegExp('{c}', 'g'), c)
        }
        chart_option.series[0].labelLine = customAttr.label.labelLine
      }
      // visualMap
      const valueArr = chart.data.series[0].data
      if (valueArr && valueArr.length > 0) {
        const values = []
        valueArr.forEach(function(ele) {
          values.push(ele.value)
        })
        chart_option.visualMap.min = Math.min(...values)
        chart_option.visualMap.max = Math.max(...values)
        if (chart_option.visualMap.min === chart_option.visualMap.max) {
          chart_option.visualMap.min = 0
        }
      } else {
        chart_option.visualMap.min = 0
        chart_option.visualMap.max = 0
      }
      if (chart_option.visualMap.min === 0 && chart_option.visualMap.max === 0) {
        chart_option.visualMap.max = 100
      }
      // color
      if (customAttr.color && customAttr.color.colors) {
        chart_option.visualMap.inRange.color = customAttr.color.colors
        chart_option.visualMap.inRange.colorAlpha = customAttr.color.alpha / 100
      }
      for (let i = 0; i < valueArr.length; i++) {
        // const y = {
        //   name: chart.data.x[i],
        //   value: valueArr[i]
        // }
        const y = valueArr[i]
        y.name = chart.data.x[i]
        // color
        // y.itemStyle = {
        //   color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
        //   borderRadius: 0
        // }
        chart_option.series[0].data.push(y)
      }
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart)
  return chart_option
}

