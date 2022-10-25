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
    chart_option.dataset.dimensions = chart.data.x

    let arr = []
    let arr2 = []
    for(let i = 0; i< chart.data.series.length; i++) {
      const y = chart.data.series[i]
      let arr1 = []
      arr2.push(y.name)
      y.data.map((item,index) => {
        
        arr1.push(item.value)
      })
      arr.push(arr1)
    }
    arr.unshift(arr2)
    console.log('karr',arr)

		let map = new Array(Math.max(... arr.map(item => item.length)));
		for(let index = 0;index < map.length;index ++) {
			for(let key in arr) {
				if(!map[index]) {
					map[index] = [arr[key][index]]
				}else {
					map[index][key] = arr[key][index]
				}
			} 
		}
    console.log('k数据',map)
    chart_option.dataset.source = map
    chart_option.series[0] = {
      type: 'candlestick',
      encode: {
        tooltip: [1,2,3,4],
      }
      // data: map
      // data: [
      //   [22, 24, 21, 32],
      //   [40, 35, 30, 50],
      //   [31, 38, 33, 44],
      //   [38, 15, 5, 42]
      // ]
    } 
  }

  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  console.log('kkkkkkkk',chart_option)
  return chart_option
}