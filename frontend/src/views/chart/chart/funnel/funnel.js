import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle } from '../common/common'

export function baseFunnelOption(chart_option, chart, cstyle = {}) {
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
    if (chart.data.series.length > 0) {
      chart_option.series[0].name = chart.data.series[0].name
      // size
      if (customAttr.size) {
        chart_option.series[0].width = customAttr.size.funnelWidth + '%'
      }
      // label
      if (customAttr.label) {
        chart_option.series[0].label = customAttr.label
      }
      const valueArr = chart.data.series[0].data
      for (let i = 0; i < valueArr.length; i++) {
        // const y = {
        //   name: chart.data.x[i],
        //   value: valueArr[i]
        // }
        const y = valueArr[i]
        y.name = chart.data.x[i]
        // color
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
        }
        // y.type = 'funnel'
        chart_option.series[0].data.push(y)
      }
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart,cstyle)
  return chart_option
}

export function baseContrastFunnelOption(chart_option, chart, cstyle = {}) {
  console.log('对比',chart)

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
  if(chart.data) {
    chart_option.title.text = chart.title
    chart_option.legend.data = chart.data.x
    
    if(chart.data.series.length > 0) {
      console.log('series,,,',chart.data.series)
      const valueArr = chart.data.series[0].data
      let arr = []
      for(let i=0;i<valueArr.length;i++) {
        const y = valueArr[i]
        y.name = chart.data.x[i]
        // color
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
        }
        arr.push(y)
      }
      chart_option.series[0] = {
        type: 'funnel',
        width: '38%',
        height: '38%',
        left: '5%',
        top: '48%',
        funnelAlign: 'right',
        label: customAttr.label,
        data: arr
      }
      chart_option.series[1] = {
        type: 'funnel',
        width: '38%',
        height: '38%',
        left: '5%',
        top: '10%',
        sort: 'ascending',
        funnelAlign: 'right',
        label: customAttr.label,
        data: arr
      }
      chart_option.series[2] = {
        type: 'funnel',
        width: '38%',
        height: '38%',
        left: '55%',
        top: '10%',
        funnelAlign: 'left',
        label: customAttr.label,
        data: arr
      }
      chart_option.series[3] = {
        type: 'funnel',
        width: '38%',
        height: '38%',
        left: '55%',
        top: '48%',
        sort: 'ascending',
        funnelAlign: 'left',
        label: customAttr.label,
        data: arr
      }
    }
  }

  componentStyle(chart_option, chart,cstyle)
  console.log('对比funner',chart_option)
  return chart_option
}

