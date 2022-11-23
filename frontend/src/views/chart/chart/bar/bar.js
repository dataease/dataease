import { hexColorToRGBA } from '../util.js'
import { componentStyle, seniorCfg } from '../common/common'

import img1 from '../../../../assets/Funnel1.png'
import img2 from '../../../../assets/Funnel2.png'
import img3 from '../../../../assets/Funnel3.png'

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
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColors[i % customAttr.color.borderColors.length], customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr,
          borderType: customAttr.size.borderType,
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColors[i % customAttr.color.borderColors.length], customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr,
          borderType: customAttr.size.borderType,
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
  console.log('bar_echarts', chart_option)
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}
// contrastBarOption
export function contrastBarOption(chart_option, chart, cstyle = {}) {
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
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
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
      y.yAxisIndex = i
      y.type = 'bar'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  console.log('bar_echarts', chart_option)
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  console.log('bar_echarts++++++++++>>>>>', chart_option)
  const yAixsdata = JSON.parse(JSON.stringify(chart_option.yAxis))
  // yAixsdata.push(chart_option.yAxis)
  // yAixsdata.push(chart_option.yAxis)
  // console.log('yAixsdata===>', yAixsdata)
  // yAixsdata[0].type = 'value'
  // yAixsdata[1].type = 'value'
  // yAixsdata[1].inverse = true
  console.log('yAixsdata===>', yAixsdata)
  // chart_option.yAxis = yAixsdata
  chart_option.yAxis = [
    {
      type: 'value',
      // max: 350,
      axisLabel: yAixsdata.axisLabel,
      splitLine: yAixsdata.splitLine,
      nameTextStyle: yAixsdata.nameTextStyle,
      show: yAixsdata.show,
      nameLocation: yAixsdata.nameLocation,
      nameRotate: yAixsdata.nameRotate
    },
    {
      type: 'value',
      // max: 350
      inverse: true,
      axisLabel: yAixsdata.axisLabel,
      splitLine: yAixsdata.splitLine,
      nameTextStyle: yAixsdata.nameTextStyle,
      show: yAixsdata.show,
      nameLocation: yAixsdata.nameLocation,
      nameRotate: yAixsdata.nameRotate
    }
  ]
  chart_option.xAxis.position = 'center'

  return chart_option
}

// doubleBarOption
export function doubleBarOption(chart_option, chart, cstyle = {}) {
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
  const options = []
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
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
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
      // chart_option.series.push(y)
    }
    chart_option.timeline = {
      show: false,
      top: 0,
      data: []
    }
    chart_option.grid = [{
      show: false,
      left: '5%',
      top: '13%',
      bottom: '10%',
      containLabel: true,
      width: '37%'
    }, {
      show: false,
      left: '51%',
      top: '17%',
      bottom: '9%',
      width: '0%'
    }, {
      show: false,
      right: '5%',
      top: '13%',
      bottom: '10%',
      containLabel: true,
      width: '37%'
    }]

    options.push(chart.data.series[0])
    options.push(chart.data.series[1])
    options[1].xAxisIndex = 2
    options[1].yAxisIndex = 2
  }

  chart_option.series = []
  console.log('_____________', chart_option)
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  chart_option.xAxis = []
  const customStyle = JSON.parse(chart.customStyle)
  chart_option.xAxis = [{
    show: customStyle.xAxis.show,
    type: 'value',
    name: customStyle.xAxis.name,
    nameLocation: customStyle.xAxis.nameLocation,
    nameTextStyle: {
      ...customStyle.xAxis.nameTextStyle,
      lineHeight: 20,
      fontFamily: cstyle && cstyle.fontFamily? cstyle.fontFamily : '',
      padding: [
        customStyle.xAxis.paddingTop !== undefined? customStyle.xAxis.paddingTop : 0,
        customStyle.xAxis.paddingRight !== undefined? customStyle.xAxis.paddingRight : 0,
        customStyle.xAxis.paddingBottom !== undefined? customStyle.xAxis.paddingBottom : 0,
        customStyle.xAxis.paddingLeft !== undefined? customStyle.xAxis.paddingLeft : 0,
      ]
    },
    inverse: true,
    splitLine: {
      show: customStyle.xAxis.splitLine.show,
      lineStyle: {
        ...customStyle.xAxis.splitLine.lineStyle
      }
    },
    axisTick: {
      show: false
    },
    position: customStyle.xAxis.position,
    axisLabel: {
      ...customStyle.xAxis.axisLabel,
      fontFamily: cstyle && cstyle.fontFamily? cstyle.fontFamily : '',
    },
    // max: !customStyle.xAxis.axisValue.auto? customStyle.axisValue.xAxis.max : null,
    // min: !customStyle.xAxis.axisValue.auto? customStyle.axisValue.xAxis.min : null,
    // interval: !customStyle.xAxis.axisValue.auto? customStyle.axisValue.xAxis.split : null,
  }, {
    gridIndex: 1,
    show: false,
  }, {
    show: customStyle.xAxis.show,
    name: customStyle.xAxis.name,
    nameLocation: customStyle.xAxis.nameLocation,
    nameTextStyle: {
      ...customStyle.xAxis.nameTextStyle,
      lineHeight: 20,
      fontFamily: cstyle && cstyle.fontFamily? cstyle.fontFamily : '',
      padding: [
        customStyle.xAxis.paddingTop !== undefined? customStyle.xAxis.paddingTop : 0,
        customStyle.xAxis.paddingRight !== undefined? customStyle.xAxis.paddingRight : 0,
        customStyle.xAxis.paddingBottom !== undefined? customStyle.xAxis.paddingBottom : 0,
        customStyle.xAxis.paddingLeft !== undefined? customStyle.xAxis.paddingLeft : 0,
      ]
    },
    gridIndex: 2,
    splitLine: {
      show: customStyle.xAxis.splitLine.show,
      lineStyle: {
        ...customStyle.xAxis.splitLine.lineStyle
      }
    },
    axisTick: {
      show: false
    },
    position: customStyle.xAxis.position,
    axisLabel: {
      ...customStyle.xAxis.axisLabel,
      fontFamily: cstyle && cstyle.fontFamily? cstyle.fontFamily : '',
    },
  }]
  chart_option.yAxis = []
  chart_option.yAxis = [{
    show: customStyle.yAxis.show,
    type: 'category',
    inverse: true,
    position: 'right',
    axisTick: {
      show: false
    },
    axisLabel: {
      show: false
    },
    splitLine: {
      ...customStyle.yAxis.splitLine
    },
    data: chart.data.x
  }, {
    show: customStyle.yAxis.show,
    name: customStyle.yAxis.name,
    nameLocation: customStyle.yAxis.nameLocation,
    nameRotate: 0,
    nameTextStyle: {
      ...customStyle.yAxis.nameTextStyle,
      lineHeight: 20,
      fontFamily: cstyle && cstyle.fontFamily? cstyle.fontFamily : '',
      padding: [
        customStyle.yAxis.paddingTop !== undefined? customStyle.yAxis.paddingTop : 0,
        customStyle.yAxis.paddingRight !== undefined? customStyle.yAxis.paddingRight : 0,
        customStyle.yAxis.paddingBottom !== undefined? customStyle.yAxis.paddingBottom : 0,
        customStyle.yAxis.paddingLeft !== undefined? customStyle.yAxis.paddingLeft : 0,
      ]
    },
    gridIndex: 1,
    type: 'category',
    inverse: true,
    position: 'left',
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    },
    axisLabel: {
      ...customStyle.yAxis.axisLabel,
      fontFamily: cstyle && cstyle.fontFamily? cstyle.fontFamily : '',
      align: 'center'

    },
    data: chart.data.x
  }, {
    show: customStyle.yAxis.show,
    gridIndex: 2,
    type: 'category',
    inverse: true,
    position: 'left',
    axisTick: {
      show: false
    },
    axisLabel: {
      show: false
    },
    splitLine: {
      ...customStyle.yAxis.splitLine
    },
    data: chart.data.x
  }]
  console.log('double,,,,',chart_option)
  // var lastYearData = [3, 20, 62, 34, 55, 65, 33];
  // var thisYearData = [11, 38, 23, 39, 66, 66, 79];
  const option = {
    baseOption: chart_option,
    options: [
      {
        series: options
      }
    ]
  }
  console.log('options===>', option)
  return option
}

const compare = function(prop) {
  return function(obj1, obj2) {
    var val1 = obj1[prop]
    var val2 = obj2[prop]; if (val1 < val2) {
      return -1
    } else if (val1 > val2) {
      return 1
    } else {
      return 0
    }
  }
}
// rankingBarOption
export function rankingBarOption(chart_option, chart, cstyle = {}) {
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
    // chart_option.xAxis.show = false
    chart_option.polar = {
      radius: [10, '70%']
    }
    chart_option.radiusAxis = {
      // max: 4
    }
    chart_option.angleAxis = {
      type: 'category',
      data: chart.data.x,
      startAngle: 75
    }
    console.log('customAttr?????????', customAttr)
    const barBorderRadiusArr = [customAttr.size.barBorderRadius, customAttr.size.barBorderRadius, 0, 0]
    let dataArr = []
    dataArr = chart.data.series[0].data
    // dataArr.forEach(res => {
    //   console.log('res===>', res)
    // })
    // console.log('++++++>>>>', dataArr)
    const arrss = [{ value: 2, name: '222' }, { value: 1, name: '322' }]
    console.log('------------>>>>>', arrss.sort(compare('value')))

    // dataArr.sort((a, b) => { return b.value - a.value })
    console.log('dataArr', dataArr)

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
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
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
      y.coordinateSystem = 'polar'
      y.type = 'bar'
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
    // chart.data.series[0].forEach(res => {
    //   console.log('res===>', res)
    // })
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
  console.log('_____________+++++++++++++', chart_option, chart.data)
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}
// polarStackBarOption
export function polarStackBarOption(chart_option, chart, cstyle = {}) {
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
    // chart_option.xAxis.show = false
    chart_option.polar = {
      radius: [0, '70%']
    }
    chart_option.radiusAxis = {
      // max: 4
    }
    chart_option.angleAxis = {
      type: 'category',
      data: chart.data.x
      // startAngle: 75
    }
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
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
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
      y.coordinateSystem = 'polar'
      y.type = 'bar'
      y.stack = 'stack'
      y.emphasis = {
        focus: 'series'
      }
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
    // chart.data.series[0].forEach(res => {
    //   console.log('res===>', res)
    // })
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
  console.log('_____________+++++++++++++', chart_option, chart.data)
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}
// clockcatterOption
export function clockcatterOption(chart_option, chart, cstyle = {}) {
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
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
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
      y.type = 'candlestick'
      console.log('yyyyyyyyyyyyyyyy', y)
      chart_option.legend.data.push(y.name)
      chart_option.series.push(y)
    }
  }
  console.log('_____________', chart_option)
  componentStyle(chart_option, chart, cstyle)
  seniorCfg(chart_option, chart)
  return chart_option
}

//
// pyramidBarOption
export function pyramidBarOption(chart_option, chart, cstyle = {}) {
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
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
          barBorderRadius: barBorderRadiusArr
        }
      } else {
        y.itemStyle = {
          color: hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha),
          barBorderWidth: customAttr.size.barBorderValue,
          barBorderColor: hexColorToRGBA(customAttr.color.borderColor, customAttr.color.alpha),
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
  const seriesArr = [{
    type: 'pictorialBar',
    data: [{
      name: '2',
      // z: 100,
      value: 20,
      number: '2',
      symbolSize: ['130%', '90%'],
      symbolPosition: 'center',
      symbolOffset: ['150%', '-180%'],
      symbol: 'image://' + img1
    }, {
      name: '3',
      // z: 90,
      value: 40,
      number: '3',
      symbolSize: ['200%', '40%'],
      symbolPosition: 'center',
      symbolOffset: ['26%', '-54%'],
      symbol: 'image://' + img2
    }, {
      name: '3',
      // z: 80,
      value: 60,
      number: '3',
      symbolSize: ['280%', '35%'],
      symbolPosition: 'center',
      symbolOffset: ['-33%', '70%'],
      symbol: 'image://' + img3
    }]
  }]
  const titleArr = [
    {
      text: '数据',
      top: '17%',
      left: '72%',
      textStyle: {
        color: '#12e7e8',
        fontSize: '16'
      }
    },
    {
      text: '123',
      top: '22%',
      left: '72%',
      textStyle: {
        color: '#333',
        fontSize: '16'
      }
    }
  ]
  // chart_option.title = titleArr
  chart_option.yAxis = {
    show: false
  }
  chart_option.xAxis = {
    show: false,
    data: [
      '',
      '',
      '',
      ''
    ]
  }
  chart_option.series = seriesArr

  console.log('_____________!!!!', chart_option)
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

export function stackBarPartOption(chart_option, chart, cstyle = {}) {
  baseBarOption(chart_option, chart, cstyle)

  // ext
  chart_option.series.forEach(function(s, index) {
    if (index !== 0) {
      s.stack = 'stack'
      s.emphasis = {
        focus: 'series'
      }
      s.yAxisIndex = 1
    } else {
      s.yAxisIndex = 0
    }
  })
  console.log('部分堆叠',chart_option)
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
    // console.log('customAttr____________', chart_option, chart)
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
  // console.log('横向柱：',chart_option);
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

export  function base3DColumnOption(chart_option, chart, cstyle = {}) {
  console.log('3d柱状图',chart)
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

  //data
  if(chart.data) {
    chart_option.title.text = chart.title
    
    if(chart.data.series.length) {
      let  arr = []
      for(let i = 0; i< chart.data.series.length; i++) {
        const y = chart.data.series[i]
        let arr1 = []
        y.data.map((item,index) => {
          arr1.push(item.value)
        })
        arr.push(arr1)
      }
      console.log('bbbarr',arr)

      let s = new Array(Math.max(... arr.map(item => item.length)));
      for(let index = 0;index < s.length;index ++) {
        for(let key in arr) {
          if(!s[index]) {
            s[index] = [arr[key][index]]
          }else {
            s[index][key] = arr[key][index]
          }
        } 
      }
      console.log('3dbar数据',s)

      chart_option.series[0].data = s

      chart_option.series[0].label = customAttr.label
    }
  }
  componentStyle(chart_option,chart,cstyle)
  return chart_option
}
