import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle, seniorCfg } from '../common/common'

export function baseBoxPlotOption (chart_option, chart,cstyle = {}) {
  console.log('盒须图',chart)
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

  //处理data
  if(chart.data) {
    chart_option.title.text = chart.title
    chart_option.xAxis.data = chart.data.x

    let arr = []
    for(let i = 0; i < chart.data.series.length; i++) {
      const y  = chart.data.series[i]
      let arr1 = []
      y.data.map(item => {
        arr1.push(item.value)
      })
      
      // arr.push({
      //   value: arr1,
      //   itemStyle: {
      //     color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
      //   },
      // })
      arr.push(arr1)
      chart_option.legend.data.push(y.name)
    }
    chart_option.series = {
      type: 'boxplot',
      // data: arr
      dimensions: chart.data.x
    }
    chart_option.dataset[0].source = arr
  }

  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  console.log('boxxxxxx',chart_option)
  return chart_option

}