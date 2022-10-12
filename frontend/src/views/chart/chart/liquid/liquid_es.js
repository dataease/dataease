import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle, seniorCfg } from '../common/common'

export function baseLiquidOption(chart_option, chart, cstyle = {}) {
  console.log('水波图1',chart_option,chart)
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // console.log('样式：：',customAttr)
    if (customAttr.color) {
      chart_option.color = customAttr.color.colors
    }
  }

  if (chart.data) {
    chart_option.title.text = chart.title
    
    // size
    if(customAttr.size) {
      chart_option.series[0].shape = customAttr.size.liquidShape
      chart_option.series[0].radius = customAttr.size.liquidSize + '%'
      chart_option.series[0].waveAnimation = customAttr.size.liquidBan
      chart_option.series[0].outline.show = customAttr.size.liquidShow
      chart_option.series[0].outline.itemStyle.borderWidth = customAttr.size.liquidOutlineBorder
      chart_option.series[0].outline.borderDistance = customAttr.size.liquidOutlineDistance
      chart_option.series[0].waveLength  = customAttr.size.liquidWaveLength
    }

    if(customAttr.label) {
      console.log(customAttr.label)
      chart_option.series[0].label.show = customAttr.label.show
      chart_option.series[0].label.textStyle.fontSize =  customAttr.label.fontSize
      chart_option.series[0].label.textStyle.color = customAttr.label.color
    }
    chart_option.series[0].type = 'liquidFill'
    chart_option.series[0].data = chart.data.series[0].data
    chart_option.series[0].itemStyle = {
      color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha),
    }
    
  }

  console.log('liquidFill,,,,', chart_option)
  componentStyle(chart_option, chart, cstyle)
  return chart_option
}