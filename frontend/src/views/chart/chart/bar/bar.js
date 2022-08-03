import { hexColorToRGBA } from '../util.js'
import { componentStyle, seniorCfg } from '../common/common'

export function baseBarOption(chart_option, chart, cstyle = {}) {
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
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        // color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
        color: {
          type: 'linear',
          x: 0,
          y: 1,
          x2: 0,
          y2: 0,
          colorStops: [{
            offset: 0,  // 0% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
          }, {
            offset: 1, // 100% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[(i+1) % customAttr.color.colors.length], customAttr.color.alpha)
          }],
          global: false // 缺省为 false
        }
      }
      // size
      if (customAttr.size) {
        if (customAttr.size.barDefault) {
          y.barWidth = null
          y.barGap = null
        } else {
          y.barWidth = customAttr.size.barWidth
          y.barGap = customAttr.size.barGap
        }
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      y.type = 'bar'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart,cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function stackBarOption(chart_option, chart,cstyle = {}) {
  baseBarOption(chart_option, chart,cstyle)

  // ext
  chart_option.series.forEach(function(s) {
    s.stack = 'stack'
    s.emphasis = {
      focus: 'series'
    }
  })
  return chart_option
}

export function horizontalBarOption(chart_option, chart,cstyle = {}) {
  // 处理shape attr
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.color) {
      chart_option.color = customAttr.color.colors
    }
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
    chart_option.yAxis.data = chart.data.x
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      y.itemStyle = {
        // color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 1,
          y2: 0,
          colorStops: [{
            offset: 0,  // 0% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
          }, {
            offset: 1, // 100% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[(i+1) % customAttr.color.colors.length], customAttr.color.alpha)
          }],
          global: false // 缺省为 false
        }
      }
      // size
      if (customAttr.size) {
        if (customAttr.size.barDefault) {
          y.barWidth = null
          y.barGap = null
        } else {
          y.barWidth = customAttr.size.barWidth
          y.barGap = customAttr.size.barGap
        }
      }
      // label
      if (customAttr.label) {
        y.label = customAttr.label
      }
      y.type = 'bar'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart,cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function horizontalStackBarOption(chart_option, chart,cstyle = {}) {
  horizontalBarOption(chart_option, chart,cstyle)

  // ext
  chart_option.series.forEach(function(s) {
    s.stack = 'stack'
    s.emphasis = {
      focus: 'series'
    }
  })
  return chart_option
}

export function basePictorialBarOption(chart_option, chart, cstyle = {}) {
  console.log('测试1',chart_option,chart)
  // 处理shape attr
  let customAttr = {}
  if (chart.customAttr) {
    customAttr = JSON.parse(chart.customAttr)
    if (customAttr.color) {
      chart_option.color = customAttr.color.colors
    }
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
    console.log('chart.data',chart.data)
    let series = chart.data.series[0]
    const y = series
    y.itemStyle = {
      color: {
        type: 'linear',
        x: 0,
        y: 1,
        x2: 0,
        y2: 0,
        colorStops: [{
        offset: 0,  // 0% 的颜色
        color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha)
        },{
        offset: 1, // 100% 的颜色
        color: hexColorToRGBA(customAttr.color.colors[1], customAttr.color.alpha)
        }],
        global: false // 缺省为 false
      }
    }
    // size
	  if (customAttr.size) {
      if (customAttr.size.barDefault) {
        y.barWidth = null
        y.barGap = null
      } else {
        y.barWidth = customAttr.size.barWidth
        y.barGap = customAttr.size.barGap
      }
    }
    // label
	  if (customAttr.label) {
      // y.label = customAttr.label
      y.label  = {
        show: false,
        position: [customAttr.size.barWidth / 2, -(customAttr.size.barWidth + 20)],
        color: '#ffffff',
        fontSize: 14,
        fontStyle: 'bold',
        align: 'center',
      }
    }
    y.type = 'bar'
    y.z = 1

    let arr = []
    y.data.map(item => {
      arr.push(item.value)
    })

    let t = {
      z: 2,
      name: y.name,
      type: 'pictorialBar',
      symbolPosition: 'end',
      data: arr,
      symbol: "diamond",
      symbolOffset: [0, "-50%"],
      symbolSize: [customAttr.size.barDefault? '36.5%' : customAttr.size.barWidth, '20'],
      itemStyle: {
        color: hexColorToRGBA(customAttr.color.colors[1], customAttr.color.alpha),
        borderWidth: 4,
        borderColor: '#ffffff'
      },
    }
    let b = {
      z: 3,
      name: y.name,
      type: 'pictorialBar',
      data: arr,
      symbol: 'diamond',
      symbolOffset: [0, '50%'],
      symbolSize: [customAttr.size.barDefault? '36.5%' : customAttr.size.barWidth, '20'],
      itemStyle: {
        color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha),
        borderWidth: 0,
      }
    }
    let l = {
      z: 4,
      type: 'pictorialBar',
      symbol: 'rect',
      symbolSize: [1, '100%'],
      symbolOffset: [0, 10],
      barCategoryGap: '-100%',
      itemStyle: {
        normal: {
          color: '#ededed',
          borderWidth: 0
        }
      },
      data: arr,
    }
    chart_option.legend.data.push(y.name)
	  chart_option.series.push(y)
	  chart_option.series.push(t) // 顶部
	  chart_option.series.push(b) // 底部
	  chart_option.series.push(l) // 线

  }

  componentStyle(chart_option, chart,cstyle)
  seniorCfg(chart_option, chart)
  console.log('echarts,3d.....',chart_option)
  return chart_option

}
