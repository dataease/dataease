import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle, seniorCfg } from '../common/common'
import { BASE_ECHARTS_SELECT, DEFAULT_TOOLTIP } from '@/views/chart/chart/chart'

export function baseMixOption(chart_option, chart) {
  // 处理shape attr
  let customAttr = {}
  const yAxis = JSON.parse(chart.yaxis)
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
  const yAxisSeriesMaxList = []; const yAxisExtSeriesMaxList = []
  if (chart.data) {
    chart_option.title.text = chart.title
    chart_option.xAxis.data = chart.data.x
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      y.type = y.type ? y.type : 'bar'
      // color
      y.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
      }
      // size
      if (customAttr.size) {
        // bar
        if (y.type === 'bar') {
          if (customAttr.size.barDefault) {
            y.barWidth = null
            y.barGap = null
          } else {
            y.barWidth = customAttr.size.barWidth
            y.barGap = customAttr.size.barGap
          }
        }
        // line
        if (y.type === 'line') {
          y.symbol = customAttr.size.lineSymbol
          y.symbolSize = customAttr.size.lineSymbolSize
          y.lineStyle = {
            width: customAttr.size.lineWidth,
            type: customAttr.size.lineType
          }
          y.smooth = customAttr.size.lineSmooth
        }
        // scatter
        if (y.type === 'scatter') {
          y.symbol = customAttr.size.scatterSymbol ? customAttr.size.scatterSymbol : 'circle'
          y.symbolSize = customAttr.size.scatterSymbolSize ? customAttr.size.scatterSymbolSize : 20
        }
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      chart_option.legend.data.push(y.name)
      i >= yAxis.length ? (y.yAxisIndex = 1) : (y.yAxisIndex = 0)

      // get max
      if (i >= yAxis.length) {
        const valueList = []
        y.data.forEach(ele => {
          valueList.push(ele.value)
        })
        yAxisExtSeriesMaxList.push(Math.max.apply(null, valueList))
      } else {
        const valueList = []
        y.data.forEach(ele => {
          valueList.push(ele.value)
        })
        yAxisSeriesMaxList.push(Math.max.apply(null, valueList))
      }

      y.selectedMode = true
      y.select = BASE_ECHARTS_SELECT
      chart_option.series.push(y)
    }
  }
  componentStyle(chart_option, chart)

  // 若轴值中最大值小于data的最大值，则轴值最大值设置失效
  if (yAxisSeriesMaxList.length > 0 && !isNaN(chart_option.yAxis[0].max)) {
    const max = Math.max.apply(null, yAxisSeriesMaxList)
    if (max > chart_option.yAxis[0].max) {
      delete chart_option.yAxis[0].max
    }
  }
  if (yAxisExtSeriesMaxList.length > 0 && !isNaN(chart_option.yAxis[1].max)) {
    const max = Math.max.apply(null, yAxisExtSeriesMaxList)
    if (max > chart_option.yAxis[1].max) {
      delete chart_option.yAxis[1].max
    }
  }

  seniorCfg(chart_option, chart)
  return chart_option
}
