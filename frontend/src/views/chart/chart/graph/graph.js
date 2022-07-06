import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle,seniorCfg } from '../common/common'

export function baseGraphOption(chart_option, chart,cstyle = {}) {
    // 处理shape attr
    let customAttr = {}
    if (chart.customAttr) {
      customAttr = JSON.parse(chart.customAttr)
      // console.log('样式：：',customAttr)
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
          chart_option.series[0].radius = [customAttr.size.pieInnerRadius + '%', customAttr.size.pieOuterRadius + '%']
        }
        // label
        if (customAttr.label) {
          chart_option.series[0].label = customAttr.label
          chart_option.series[0].labelLine = {show: false}
          chart_option.series[0].label.labelLine = {show: false}
        }
        const valueArr = chart.data.series[0].data
        for (let i = 0; i < valueArr.length; i++) {
          const y = valueArr[i]
          y.name = chart.data.x[i]
          // color
          y.itemStyle = {
            color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
            borderRadius: 0,
          }
          y.type = 'graph'
          // 气泡大小
          y.symbolSize = parseInt(valueArr[i].value) + 10
          y.draggable = true

          chart_option.series[0].data.push(y)
        }
        console.log('chart_option.series[0]: ', chart_option.series[0])
      }
    }
    // console.log('气泡图的：：',chart_option);
    componentStyle(chart_option, chart,cstyle) //图表样式
    seniorCfg(chart_option, chart) //值样式
    return chart_option
  }