import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle, seniorCfg } from '../common/common'

let bubbleArray = []
let terminalType = 'pc'

export function baseScatterOption(chart_option, chart, terminal = 'pc') {
  terminalType = terminal
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
    bubbleArray = []
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
      }
      // size
      if (customAttr.size) {
        y.symbol = customAttr.size.scatterSymbol ? customAttr.size.scatterSymbol : 'circle'

        const extBubble = JSON.parse(chart.extBubble)
        if (extBubble && extBubble.length > 0) {
          y.data.forEach(ele => {
            bubbleArray.push(ele.value[2])
          })
          y.symbolSize = funcSize
        } else {
          y.symbolSize = customAttr.size.scatterSymbolSize ? customAttr.size.scatterSymbolSize : 20
        }
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      y.type = 'scatter'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart)
  seniorCfg(chart_option, chart)
  return chart_option
}

const funcSize = function(data) {
  const k = terminalType === 'pc' ? 80 : 30
  const max = Math.max(...bubbleArray)
  return (data[2] / max) * k
}
