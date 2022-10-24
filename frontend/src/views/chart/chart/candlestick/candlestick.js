import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle, seniorCfg } from '../common/common'

export function candlestickOption(chart_option, chart, cstyle = {}) {
  console.log('k线图', chart)
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
    chart_option.grid.left = customAttr.size.spaceleft
    chart_option.grid.right = customAttr.size.spaceRight
    chart_option.grid.top = customAttr.size.spaceTop
    chart_option.grid.bottom = customAttr.size.spaceBottom
  }

  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    chart_option.xAxis.data = chart.data.x

    let arr = []
    for(let i = 0; i< chart.data.series.length; i++) {
      const y = chart.data.series[i]
      arr.push(y.data)
    }
    console.log('k数据',arr)

		let map = new Array(Math.max(... arr.map(item => item.length)));
		for(let index = 0;index < map.length;index ++) {
			console.log(map);
			for(let key in arr) {
				if(!map[index]) {
					map[index] = [arr[key][index]]
				}else {
					map[index][key] = arr[key][index]
				}
			} 
		}
    console.log(map)

  }

  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}