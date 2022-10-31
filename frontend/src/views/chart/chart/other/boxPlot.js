import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle, seniorCfg } from '../common/common'

export function baseBoxPlotOption (chart_option, chart,cstyle = {}) {
  console.log('盒须图',chart_option,chart)
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    // if (customAttr.color) {
    //   chart_option.color = customAttr.color.colors
    // }
    // tooltip
    // if (customAttr.tooltip) {
    //   const tooltip = JSON.parse(JSON.stringify(customAttr.tooltip))
    //   const reg = new RegExp('\n', 'g')
    //   tooltip.formatter = tooltip.formatter.replace(reg, '<br/>')
    //   chart_option.tooltip = tooltip
    // }
    // chart_option.grid.left = customAttr.size.spaceleft
    // chart_option.grid.right = customAttr.size.spaceRight
    // chart_option.grid.top = customAttr.size.spaceTop
    // chart_option.grid.bottom = customAttr.size.spaceBottom
  }

  //处理data
  if(chart.data) {
    chart_option.title.text = chart.title
    // chart_option.xAxis.data = chart.data.x

    let arr = []
    for(let i = 0; i < chart.data.series.length; i++) {
      const y  = chart.data.series[i]
      let arr1 = []
      y.data.map(item => {
        arr1.push(item.value)
      })
      
      arr.push(arr1)
      // chart_option.legend.data.push(y.name)
    }
    console.log('boxarr,,',arr)

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
    console.log('box数据',map)
    chart_option.dataset[0].source = map
  }

  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  console.log('boxxxxxx',chart_option)
  return chart_option

}