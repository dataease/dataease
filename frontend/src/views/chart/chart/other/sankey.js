import { hexColorToRGBA } from '@/views/chart/chart/util'

export function baseSankeyOption(chart_option, chart,cstyle = {}) {
  console.log('桑葚图',chart_option,chart)
  chart_option.title.text = chart.title

  if(chart.data) {

  }

  return chart_option
}