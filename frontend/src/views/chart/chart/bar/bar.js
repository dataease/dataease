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
    chart_option.grid.left = customAttr.size.spaceleft
    chart_option.grid.right = customAttr.size.spaceRight
    chart_option.grid.top = customAttr.size.spaceTop
    chart_option.grid.bottom = customAttr.size.spaceBottom
  }
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    chart_option.xAxis.data = chart.data.x
    console.log('customAttr?????????', customAttr)
    const barBorderRadiusArr = [customAttr.size.barBorderRadius, customAttr.size.barBorderRadius, 0, 0]
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      if (customAttr.color.variety) {
        y.itemStyle = {
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
          },
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
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
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}
// triangleBarOption
export function triangleBarOption(chart_option, chart, cstyle = {}) {
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
    console.log('customAttr?????????', customAttr)
    const barBorderRadiusArr = [customAttr.size.barBorderRadius, customAttr.size.barBorderRadius, 0, 0]
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color

      if (customAttr.color.variety) {
        y.itemStyle = {
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
          },
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
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
      y.type = 'pictorialBar'
      y.symbol = 'triangle'
      y.barMinHeight = 10
      y.barCategoryGap = '0%'
      // y.type = 'bar'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function annularBarOption(chart_option, chart, cstyle = {}) {
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
    console.log('customAttr?????????', customAttr)
    const barBorderRadiusArr = [customAttr.size.barBorderRadius, customAttr.size.barBorderRadius, 0, 0]
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      if (customAttr.color.variety) {
        y.itemStyle = {
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
          },
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
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
      y.coordinateSystem = 'polar'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
      y.label = {
        show: true,
        position: 'middle',
        formatter: '{c}'
      }
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
  chart_option.polar = {
    radius: [10, '80%']
  }
  chart_option.angleAxis = {
    // max: 8,
    startAngle: 0
  }
  chart_option.radiusAxis = {
    type: 'category',
    data: chart.data.x
  }
  // console.log(chart_option);
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function getData(data) {
  var res = {
    series: [],
    yAxis: []
  }
  const arrValue = getArrayValue(data, 'value')
  const sumValue = eval(arrValue.join('+'))
  for (let i = 0; i < data.length; i++) {
    res.series.push({
      name: '',
      type: 'pie',
      clockWise: false, // 顺时加载
      hoverAnimation: false, // 鼠标移入变大
      radius: [73 - i * 15 + '%', 68 - i * 15 + '%'],
      center: ['50%', '50%'],
      label: {
        show: false
      },
      itemStyle: {
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        borderWidth: 5
      },
      data: [
        {
          value: data[i].value,
          name: data[i].name
        },
        {
          value: sumValue - data[i].value - 20,
          name: '',
          itemStyle: {
            color: 'rgba(0,0,0,0)',
            borderWidth: 0
          },
          tooltip: {
            show: false
          },
          hoverAnimation: false
        }
      ]
    })
    res.series.push({
      name: '',
      type: 'pie',
      silent: true,
      z: 1,
      clockWise: false, // 顺时加载
      hoverAnimation: false, // 鼠标移入变大
      radius: [73 - i * 15 + '%', 68 - i * 15 + '%'],
      center: ['50%', '50%'],
      label: {
        show: false
      },
      itemStyle: {
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        borderWidth: 5
      },
      data: [
        {
          value: 7.5,
          itemStyle: {
            color: 'rgb(3, 31, 62)',
            borderWidth: 0
          },
          tooltip: {
            show: false
          },
          hoverAnimation: false
        },
        {
          value: 2.5,
          name: '',
          itemStyle: {
            color: 'rgba(0,0,0,0)',
            borderWidth: 0
          },
          tooltip: {
            show: false
          },
          hoverAnimation: false
        }
      ]
    })
    // res.yAxis.push(((data[i].value / sumValue) * 100).toFixed(2) + '%');
  }
  return res
}

export function getArrayValue(array, key) {
  var key = key || 'value'
  var res = []
  if (array) {
    array.forEach(function(t) {
      res.push(t[key])
    })
  }
  return res
}

// annularBarOption
export function annularBarOptions(chart_option, chart, cstyle = {}) {
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
    console.log('____________________++++++++++', chart.data)
    chart_option.title.text = chart.title
    chart_option.xAxis.data = chart.data.x
    // console.log('customAttr?????????', customAttr)
    const barBorderRadiusArr = [customAttr.size.barBorderRadius, customAttr.size.barBorderRadius, 0, 0]
    const arrValue = getArrayValue(chart.data.series[0].data, 'value')
    console.log('???????chart.data.series[0].data', chart.data.series[0].data, arrValue, eval(arrValue.join('+')))
    const dataForm = chart.data.series[0].data
    var seriesArr = []
    for (let i = 0; i < dataForm.length; i++) {
      console.log(dataForm[i])
      seriesArr.push({
        name: '',
        type: 'pie',
        clockWise: false, // 顺时加载
        hoverAnimation: false, // 鼠标移入变大
        radius: [73 - i * 15 + '%', 68 - i * 15 + '%'],
        center: ['50%', '50%'],
        label: {
          show: false
        },
        itemStyle: {
          label: {
            show: false
          },
          labelLine: {
            show: false
          },
          borderWidth: 5
        },
        data: [
          {
            value: dataForm[i].value,
            name: chart.data.x[i]
          },
          {
            value: eval(arrValue.join('+')) - dataForm[i].value - 20,
            itemStyle: {
              color: 'rgba(0,0,0,0)',
              borderWidth: 0
            },
            tooltip: {
              show: false
            },
            hoverAnimation: false
          }
        ]
      })
      seriesArr.push({
        name: '',
        type: 'pie',
        silent: true,
        z: 1,
        clockWise: false, // 顺时加载
        hoverAnimation: false, // 鼠标移入变大
        radius: [73 - i * 15 + '%', 68 - i * 15 + '%'],
        center: ['50%', '50%'],
        label: {
          show: false
        },
        itemStyle: {
          label: {
            show: false
          },
          labelLine: {
            show: false
          },
          borderWidth: 5
        },
        data: [
          {
            value: 7.5,
            itemStyle: {
              color: customAttr.color.bgColor,
              borderWidth: 0
            },
            tooltip: {
              show: false
            },
            hoverAnimation: false
          },
          {
            value: 2.5,
            name: '',
            itemStyle: {
              color: 'rgba(0,0,0,0)',
              borderWidth: 0
            },
            tooltip: {
              show: false
            },
            hoverAnimation: false
          }
        ]
      })
    }
    console.log('seriesArr-----', seriesArr)
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      if (customAttr.color.variety) {
        y.itemStyle = {
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
          },
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
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
      // y.type = 'bar'
      chart_option.legend.data.push(y.name)
      // chart_option.series.push(y)
    }
  }
  // const legendData = {
  //   show: true,
  //   icon: 'circle',
  //   // top: "center",
  //   top: '13%',
  //   bottom: '53%',
  //   left: '50%',
  //   data: chart.data.x,
  //   width: 50,
  //   padding: [0, 18],
  //   itemGap: 20
  // }

  chart_option.series = seriesArr
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

  // chart_option.angleAxis = {
  //   // max: 3,
  //   startAngle: 0
  // }
  // console.log(chart_option);
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  chart_option.legend.top = '13%'
  chart_option.legend.bottom = '53%'
  chart_option.legend.left = '50%'
  // chart_option.legend.itemGap = 20
  chart_option.legend.padding = [0, 18]
  chart_option.legend.data = chart.data.x
  chart_option.legend.orient = 'vertical'
  console.log('chart_option______________', chart_option)
  return chart_option
}

export function stackBarOption(chart_option, chart, cstyle = {}) {
  baseBarOption(chart_option, chart, cstyle)

  // ext
  chart_option.series.forEach(function(s) {
    s.stack = 'stack'
    s.emphasis = {
      focus: 'series'
    }
  })
  return chart_option
}

export function horizontalBarOption(chart_option, chart, cstyle = {}) {
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
    console.log('customAttr____________', chart_option, chart)
    chart_option.grid.left = customAttr.size.spaceleft
    chart_option.grid.right = customAttr.size.spaceRight
    chart_option.grid.top = customAttr.size.spaceTop
    chart_option.grid.bottom = customAttr.size.spaceBottom
  }
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    chart_option.yAxis.data = chart.data.x
    for (let i = 0; i < chart.data.series.length; i++) {
      const y = chart.data.series[i]
      // color
      if (customAttr.color.variety) {
        y.itemStyle = {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
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
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
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
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}

export function horizontalStackBarOption(chart_option, chart, cstyle = {}) {
  horizontalBarOption(chart_option, chart, cstyle)

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
  console.log('测试1', chart_option, chart)
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
    chart_option.grid.left = customAttr.size.spaceleft
    chart_option.grid.right = customAttr.size.spaceRight
    chart_option.grid.top = customAttr.size.spaceTop
    chart_option.grid.bottom = customAttr.size.spaceBottom
  }

  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    chart_option.xAxis.data = chart.data.x
    chart_option.xAxis.offset = 5

    console.log('chart.data', chart.data)
    const series = chart.data.series[0]
    const y = series
    y.itemStyle = {
      color: {
        type: 'linear',
        x: 0,
        y: 1,
        x2: 0,
        y2: 0,
        colorStops: [{
          offset: 0, // 0% 的颜色
          color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha)
        }, {
          offset: 1, // 100% 的颜色
          color: customAttr.color.variety ? hexColorToRGBA(customAttr.color.colors1[0], customAttr.color.alpha)
            : hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha)
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
      y.label = {
        show: false,
        position: [customAttr.size.barWidth / 2, -(customAttr.size.barWidth + 20)],
        color: '#ffffff',
        fontSize: 14,
        fontStyle: 'bold',
        align: 'center'
      }
    }
    y.type = 'bar'
    y.z = 2

    const arr = []
    y.data.map(item => {
      arr.push(item.value)
    })
    const max = Math.max(...arr)
    const arr1 = []
    y.data.map(item => {
      arr1.push(max)
    })

    const t = {
      z: 3,
      name: y.name,
      tooltip: { show: false },
      type: 'pictorialBar',
      symbolPosition: 'end',
      data: arr,
      symbol: 'diamond',
      symbolOffset: [0, '-60%'],
      symbolSize: [customAttr.size.barDefault ? '84%' : customAttr.size.barWidth, '20'],
      itemStyle: {
        color: customAttr.color.variety ? hexColorToRGBA(customAttr.color.colors1[0], customAttr.color.alpha)
          : hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha),
        borderWidth: 6,
        borderColor: '#eeeeee'
      }
    }
    const b = {
      z: 3,
      name: y.name,
      tooltip: { show: false },
      type: 'pictorialBar',
      data: arr,
      symbol: 'diamond',
      symbolOffset: [0, '50%'],
      symbolSize: [customAttr.size.barDefault ? '84%' : customAttr.size.barWidth, '20'],
      itemStyle: {
        // color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha),
        color: {
          type: 'linear',
          x: 0,
          y: 1,
          x2: 0,
          y2: 0,
          colorStops: [{
            offset: 0, // 0% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha)
          }, {
            offset: 0.5, // 50% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha)
          }, {
            offset: 1, // 100% 的颜色
            color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha - 90)
          }],
          global: false // 缺省为 false
        },
        borderWidth: 0
      }

    }

    const x1 = { // 背景柱
      name: y.name,
      tooltip: { show: false },
      type: 'pictorialBar',
      symbol: 'rect',
      symbolSize: [customAttr.size.barDefault ? '80%' : customAttr.size.barWidth, '100%'],
      symbolOffset: ['0', '0'],
      data: arr1,
      z: 0,
      itemStyle: {
        color: hexColorToRGBA(customAttr.color.colors[0], 20),
        borderWidth: 0
        // borderColor: hexColorToRGBA(customAttr.color.colors[1], 30)
      }
    }
    const x2 = { // 背景顶
      name: y.name,
      // stack: 'amount',
      tooltip: { show: false },
      type: 'pictorialBar',
      symbol: 'diamond',
      symbolOffset: [0, '-52%'],
      symbolPosition: 'end',
      symbolSize: [customAttr.size.barDefault ? '82%' : customAttr.size.barWidth, '20'],
      data: arr1,
      z: 0,
      itemStyle: {
        color: hexColorToRGBA(customAttr.color.colors[0], 20),
        borderWidth: 1,
        borderColor: '#eeeeee'
      }
      // emphasis: {
      //   itemStyle: {
      //     color: hexColorToRGBA(customAttr.color.colors[1], 30),
      //   }
      // }
    }
    const x3 = { // 背景底
      name: y.name,
      // stack: 'amount',
      tooltip: { show: false },
      type: 'pictorialBar',
      symbol: 'diamond',
      symbolOffset: [0, '50%'],
      symbolSize: [customAttr.size.barDefault ? '84%' : customAttr.size.barWidth, '20'],
      data: arr1,
      z: 0,
      itemStyle: {
        color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha - 60)
      },
      emphasis: {
        itemStyle: {
          color: hexColorToRGBA(customAttr.color.colors[0], customAttr.color.alpha - 60)
        }
      }
    }

    chart_option.legend.data.push(y.name)
	  chart_option.series.push(y) // 柱体
	  chart_option.series.push(t) // 顶部
	  chart_option.series.push(b) // 底部

    chart_option.series.push(x1)
    chart_option.series.push(x2)
    // chart_option.series.push(x3)
  }

  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  console.log('echarts,3d.....', chart_option)
  return chart_option
}
