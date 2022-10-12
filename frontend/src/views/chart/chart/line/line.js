import { hexColorToRGBA } from '@/views/chart/chart/util'
import { componentStyle, seniorCfg } from '../common/common'

export function baseLineOption(chart_option, chart, cstyle = {}) {
  console.log('echarts,line------------', chart_option, chart)
  // 处理shape attr
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    console.log('chart.customAttr!!!!!!!!!!!', customAttr)
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
  // chart_option.grid =
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    chart_option.xAxis.data = chart.data.x
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
      }
      // size
      if (customAttr.size) {
        y.symbol = customAttr.size.lineSymbol
        y.symbolSize = customAttr.size.lineSymbolSize
        y.lineStyle = {
          width: customAttr.size.lineWidth,
          type: customAttr.size.lineType
        }
        y.smooth = customAttr.size.lineSmooth
        if (customAttr.size.lineArea) {
          y.areaStyle = {
            opacity: 0.6
          }
        } else {
          delete y.areaStyle
        }
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      y.type = 'line'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  console.log('图表数据修改===+++++++++++++++', chart_option, chart)
  // console.log(chart_option);
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}
// polarLineOption
export function polarLineOption(chart_option, chart, cstyle = {}) {
  console.log('echarts,line------------', chart_option, chart)
  // 处理shape attr
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    console.log('chart.customAttr!!!!!!!!!!!', customAttr)
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
  // chart_option.grid =
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    chart_option.xAxis.data = chart.data.x
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
      }
      // size
      if (customAttr.size) {
        y.symbol = customAttr.size.lineSymbol
        y.symbolSize = customAttr.size.lineSymbolSize
        y.lineStyle = {
          width: customAttr.size.lineWidth,
          type: customAttr.size.lineType
        }
        y.smooth = customAttr.size.lineSmooth
        if (customAttr.size.lineArea) {
          y.areaStyle = {
            opacity: 0.6
          }
        } else {
          delete y.areaStyle
        }
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      y.coordinateSystem = 'polar'
      y.type = 'line'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  console.log('图表数据修改===+++++++++++++++', chart_option, chart)
  chart_option.polar = {
    radius: [0, '70%']
  }
  chart_option.tooltip = {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  }
  chart_option.xAxis = [
    {
      show: false
    }
  ]
  chart_option.yAxis = [
    {
      show: false
    }
  ]
  chart_option.angleAxis = {
    type: 'value',
    startAngle: 0
  }
  chart_option.radiusAxis = {}
  // console.log(chart_option);
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function stackLineOption(chart_option, chart, cstyle = {}) {
  baseLineOption(chart_option, chart, cstyle)

  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
  }
  // ext
  // chart_option.tooltip.trigger = 'axis'
  chart_option.series.forEach(function(s, i) {
    console.log('stack............', s, i)
    s.stack = 'stack'

    if (customAttr.color.variety) {
      s.itemStyle = {
        color: {
          type: 'linear',
          x: 0,
          y: 1,
          x2: 0,
          y2: 0,
          colorStops: [{
            offset: 0, // 0% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
          }, {
            offset: 1, // 100% 的颜色
            color: hexColorToRGBA(customAttr.color.colors1[i % customAttr.color.colors1.length], customAttr.color.alpha)
          }],
          global: false // 缺省为 false
        }
      }
    } else {
      s.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
      }
    }
  })
  return chart_option
}

export function heatMapOption(chart_option, chart, cstyle = {}) {
  console.log('数据改变的值++++++++++', chart_option, chart)
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
  const dataArr = []
  // baseLineOption(chart_option, chart)
  // 处理data
  if (chart.data) {
    if (chart.data.series) {
      const handleArr = chart.data.series[0].data
      console.log('需要处理数据的数组', handleArr)

      handleArr.forEach(res => {
        var dataFromArr = []
        dataFromArr.push(res.dimensionList[0].value)
        dataFromArr.push(res.dimensionList[1].value)
        dataFromArr.push(res.value)
        dataArr.push(dataFromArr)
        // res.dimensionLis[0].value
      })
      console.log('最新数据状态数组', dataArr)
    }
    const xdata = []
    const ydata = []
    chart.data.x.forEach(item => {
      // item.trim().split("/n")
      var axisData = item.trim().split('\n')
      xdata.push(axisData[1])
      ydata.push(axisData[0])
      // console.log('item', item.trim().split('\n'))
    })
    chart_option.title.text = chart.title
    chart_option.yAxis.data = ydata
    chart_option.xAxis.data = xdata
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
      }
      // size
      if (customAttr.size) {
        y.symbol = customAttr.size.lineSymbol
        y.symbolSize = customAttr.size.lineSymbolSize
        y.lineStyle = {
          width: customAttr.size.lineWidth,
          type: customAttr.size.lineType
        }
        y.smooth = customAttr.size.lineSmooth
        if (customAttr.size.lineArea) {
          y.areaStyle = {
            opacity: 0.6
          }
        } else {
          delete y.areaStyle
        }
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      y.type = 'heatmap'
      console.log('这是个什么鬼yyyyyyyyy', y)
      chart_option.legend.data.push(y.name)
      // chart_option.series.data = y.data
      // 参数第一位是Y轴坐标，第二位是X坐标，第三位是数值

      chart_option.series.data = dataArr.map(function(item) {
        return [item[1], item[0], item[2] || '-']
      })
    }
  }
  console.log('最种的渲染样式=====chart_option', chart_option)
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}
