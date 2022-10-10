export const DEFAULT_COLOR_CASE = {
    value: 'default',
    colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
    alpha: 100,
    tableHeaderBgColor: '#e1eaff',
    tableItemBgColor: '#ffffff',
    tableFontColor: '#000000',
    tableInfoFontColor: '#000000',
    tableStripe: true,
    dimensionColor: '#000000',
    quotaColor: '#000000',
    tableBorderColor: '#cfdaf4'
  }
  
  export const COLOR_PANEL = [
    '#ff4500',
    '#ff8c00',
    '#ffd700',
    '#90ee90',
    '#00ced1',
    '#1e90ff',
    '#c71585',
    '#999999',
    '#000000',
    '#FFFFFF'
  ]
  
  export const DEFAULT_LABEL = {
    show: false,
    position: 'top',
    color: '#909399',
    fontSize: '10',
    formatter: '{c}',
    gaugeFormatter: '{value}',
    labelLine: {
      show: true
    }
  }
  export const DEFAULT_TOOLTIP = {
    show: true,
    trigger: 'item',
    confine: true,
    textStyle: {
      fontSize: '10',
      color: '#909399'
    },
    formatter: ''
  }
  export const DEFAULT_TITLE_STYLE = {
    show: true,
    fontSize: '18',
    color: '#303133',
    hPosition: 'center',
    vPosition: 'top',
    isItalic: false,
    isBolder: false
  }
  export const DEFAULT_BACKGROUND_COLOR = {
    color: '#ffffff',
    alpha: 100,
    borderRadius: 5
  }
  export const BASE_CYLINDER = {
    chart: {
      type: 'cylinder', // 图表类型
      options3d: {
        enabled: true,
        alpha: 10, // 内（X）旋转角度
        beta: 0, // 外（Y）旋转角度
        depth: 50, // 图表的总深度。
        viewDistance: 25
      },
      backgroundColor: 'rgba(0,0,0,0)', // 背景颜色或渐变
      inverted: false // 反转坐标轴
    },
    credits: {
      enabled: false
    },
    title: {
      text: '',
      style: {
        fontWeight: 'normal'
      }
    },
    legend: {
      icon: 'circle'
    },
  
    plotOptions: {
      column: {
        allowPointSelect: true,
        cursor: 'pointer',
        depth: 25,
        dataLabels: {
          enabled: true
        },
        showInLegend: false
      }
    },
  
    tooltip: {},
    xAxis: {
      categories: [],
      title: {
        text: '',
        style: {
          color: '#333333',
          fontSize: '12'
        }
      },
      labels: {
        enabled: true,
        style: {}
      },
      gridLineColor: '#eeeeee',
      gridLineWidth: 1,
      gridLineDashStyle: 'solid',
      visible: true
    },
    yAxis: {
      title: {
        text: '',
        style: {
          color: '#333333',
          fontSize: '12'
        }
      },
      labels: {
        enabled: true,
        style: {}
      },
      gridLineColor: '#eeeeee',
      gridLineWidth: 1,
      gridLineDashStyle: 'solid',
      visible: true
    },
    series: [
      {
        name: '',
        data: []
      }
    ]
  }
  
  let terminalType = 'pc'
  export function baseCylinderOption(chart_option, chart, terminal = 'pc', cstyle = {}) {
    terminalType = terminal
    let customAttr = {}
    console.log('cylinder,chart: ', chart)
    if (chart.customAttr) {
      customAttr = JSON.parse(chart.customAttr)
      if (customAttr.color) {
        chart_option.colors = customAttr.color.colors
      }
      // tooltip
      if (customAttr.tooltip) {
        const tooltip = JSON.parse(JSON.stringify(customAttr.tooltip))
        const reg = new RegExp('\n', 'g')
        tooltip.formatter = tooltip.formatter.replace(reg, '<br/>')
  
        chart_option.tooltip.enabled = tooltip.show
        chart_option.tooltip.style = { fontSize: tooltip.textStyle.fontSize, color: tooltip.textStyle.color }
        let formatter = tooltip.formatter
        formatter = formatter.replace('{a}', '{series.name}')
        formatter = formatter.replace('{b}', '{point.name}')
        formatter = formatter.replace('{c}', '{point.y}')
        formatter = formatter.replace('{d', '{point.percentage')
        chart_option.tooltip.formatter = formatter
      }
  
      // label
      if (customAttr.label) {
        const dataLabels = {}
        dataLabels.enabled = customAttr.label.show
        dataLabels.color = customAttr.label.color
        dataLabels.style = { color: customAttr.label.color, fontSize: customAttr.label.fontSize }
        dataLabels.verticalAlign = customAttr.label.position
        const reg = new RegExp('\n', 'g')
        let formatter = customAttr.label.formatter.replace(reg, '<br/>')
        formatter = formatter.replace('{a}', '{series.name}')
        formatter = formatter.replace('{b}', '{point.name}')
        formatter = formatter.replace('{c}', '{point.y}')
        formatter = formatter.replace('{d', '{point.percentage')
        dataLabels.format = formatter
  
        // 系列数据标签的选项，显示在每个数据点旁边
        chart_option.plotOptions.column.dataLabels = dataLabels
      }
  
      // size
      if (customAttr.size) {
        chart_option.chart.options3d.alpha = customAttr.size.alpha ? customAttr.size.alpha : 10
        chart_option.chart.options3d.beta = customAttr.size.beta ? customAttr.size.beta : 0
        chart_option.chart.options3d.depth = customAttr.size.depth ? customAttr.size.depth : 20
      }
    }
  
    // 处理data
    if (chart.data) {
      // console.log('column,chart.data',chart.data)
      // chart_option.title.text = chart.title
      // 基础柱状数据处理
      console.log(chart)
      if (chart.data.series.length > 0) {
        // chart_option.series[0].name = chart.data.series[0].name
        // if (customAttr.color) {
        //   chart_option.series[0].opacity = customAttr.color.alpha / 100
        // }
        // const valueArr = chart.data.series[0].data
        // let arr = []
        // for (let i = 0; i < valueArr.length; i++) {
        //   const y = valueArr[i]
        //   y.name = chart.data.x[i]
        //   y.y = y.value
        //   arr.push(chart.data.x[i])
        //   chart_option.series[0].data.push(y)
        // }
        // chart_option.xAxis.categories = arr;

        const series = chart.data.series
        const arr = []
        for (let i = 0; i < series.length; i++) {
          const obj = {
            name: series[i].name,
            data: series[i].data.map(ele => { return ele.value }),
            opacity: series[i].opacity
          }
          if (customAttr.color) {
            obj.opacity = customAttr.color.alpha / 100
            if(customAttr.color.variety) {
              obj.color = {
                linearGradient: {x1: 0,y1: 1,x2: 0,y2: 0},
                stops: [
                  [0, hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)], // 0% 
                  [1, hexColorToRGBA(customAttr.color.colors1[i % customAttr.color.colors1.length], customAttr.color.alpha)], // 100%
                ]
              }
            } else {
              obj.color = hexColorToRGBA(customAttr.color.colors[i % customAttr.color.colors.length], customAttr.color.alpha)
            }
            
          }
          arr.push(obj)
        }
        chart_option.series = arr

        chart_option.xAxis.categories = chart.data.x

        // size
        /* if (customAttr.size) {
              chart_option.series[0].radius = [customAttr.size.pieInnerRadius + '%', customAttr.size.pieOuterRadius + '%']
            }*/

        console.log('isBase,chart_option:::::', chart_option)
      }
    }
  
    componentStyle(chart_option, chart, cstyle)
    return chart_option
  }
  export function componentStyle(chart_option, chart, cstyle) {
    const padding = '8px'
    console.log('column_hc,样式：：：', chart)
    if (chart.customStyle) {
      const customStyle = JSON.parse(chart.customStyle)
      console.log('customStyle=========', customStyle)
      if (customStyle.text) {
        chart_option.title.text = customStyle.text.show ? chart.title : ''
        const style = chart_option.title.style ? chart_option.title.style : {}
        style.fontSize = customStyle.text.fontSize
        style.color = customStyle.text.color
        style.fontFamily = customStyle.text.fontFamily ? customStyle.text.fontFamily : cstyle && cstyle.fontFamily ? cstyle.fontFamily : ''
        customStyle.text.isItalic ? style.fontStyle = 'italic' : style.fontStyle = 'normal'
        customStyle.text.isBolder ? style.fontWeight = 'bold' : style.fontWeight = 'normal'
        chart_option.title.style = style
        chart_option.title.align = customStyle.text.hPosition
        chart_option.title.verticalAlign = customStyle.text.vPosition
      }
  
      if (customStyle.legend && chart_option.legend) {
        chart_option.plotOptions.column.showInLegend = customStyle.legend.show
        // chart_option.legend.padding = padding
        chart_option.legend.layout = customStyle.legend.orient
        chart_option.legend.verticalAlign = customStyle.legend.vPosition
        chart_option.legend.align = customStyle.legend.hPosition
        chart_option.legend.icon = customStyle.legend.icon
        chart_option.legend.itemDistance = customStyle.legend.itemGap // 图例的间距
        chart_option.legend.itemMarginTop = customStyle.legend.marginTop // 图例的marginButtom
        chart_option.legend.itemMarginBottom = customStyle.legend.marginButtom // 图例的marginButtom
  
        console.log('customStyle.legend----------', customStyle.legend)
  
        chart_option.legend.itemStyle = customStyle.legend.textStyle
        chart_option.legend.itemStyle.icon = customStyle.legend.icon
        chart_option.legend.itemStyle.fontFamily = cstyle && cstyle.fontFamily ? cstyle.fontFamily : ''
      }
  
      if (customStyle.background) {
        chart_option.chart.backgroundColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
      }
  
      // 坐标轴设置
      if (customStyle.xAxis) {
        chart_option.xAxis.visible = customStyle.xAxis.show // 展示
        chart_option.xAxis.title.text = customStyle.xAxis.name // 描述
        chart_option.xAxis.title.align = customStyle.xAxis.align? customStyle.xAxis.align : 'middle' // 对齐
        chart_option.xAxis.title.style = customStyle.xAxis.nameTextStyle // 描述字体颜色
        chart_option.xAxis.title.style.fontFamily = cstyle && cstyle.fontFamily ? cstyle.fontFamily : '' // 描述字体
        chart_option.xAxis.gridLineColor = customStyle.xAxis.splitLine.lineStyle.color // 轴线颜色
        chart_option.xAxis.gridLineWidth = customStyle.xAxis.splitLine.lineStyle.width // 轴线宽度
        chart_option.xAxis.gridLineDashStyle = customStyle.xAxis.splitLine.lineStyle.type // 轴线线条样式
        chart_option.xAxis.labels.enabled = customStyle.xAxis.axisLabel.show // 轴标签展示
        chart_option.xAxis.labels.style.color = customStyle.xAxis.axisLabel.color // 轴标签颜色
        chart_option.xAxis.labels.style.fontSize = customStyle.xAxis.axisLabel.fontSize // 轴标签字体大小
        chart_option.xAxis.labels.style.fontFamily = cstyle && cstyle.fontFamily ? cstyle.fontFamily : '' // 轴标签字体
        // chart_option.xAxis.labels.rotation = customStyle.xAxis.axisLabel.rotate // 轴标签旋转角度
      }
      if (customStyle.yAxis) {
        chart_option.yAxis.visible = customStyle.yAxis.show // 展示
        chart_option.yAxis.title.text = customStyle.yAxis.name // 描述
        chart_option.yAxis.title.align = customStyle.yAxis.align? customStyle.yAxis.align : 'middle' // 对齐
        chart_option.yAxis.title.style = customStyle.yAxis.nameTextStyle // 字体颜色
        chart_option.yAxis.title.style.fontFamily = cstyle && cstyle.fontFamily ? cstyle.fontFamily : '' // 描述字体
        chart_option.yAxis.gridLineColor = customStyle.yAxis.splitLine.lineStyle.color // 轴线颜色
        chart_option.yAxis.gridLineWidth = customStyle.yAxis.splitLine.lineStyle.width // 轴线宽度
        chart_option.yAxis.gridLineDashStyle = customStyle.yAxis.splitLine.lineStyle.type // 轴线线条样式
        chart_option.yAxis.labels.enabled = customStyle.yAxis.axisLabel.show // 轴标签展示
        chart_option.yAxis.labels.style.color = customStyle.yAxis.axisLabel.color // 轴标签颜色
        chart_option.yAxis.labels.style.fontSize = customStyle.yAxis.axisLabel.fontSize // 轴标签字体大小
        chart_option.yAxis.labels.style.fontFamily = cstyle && cstyle.fontFamily ? cstyle.fontFamily : '' // 轴标签字体
      }
    }
  }
  export function hexColorToRGBA(hex, alpha) {
    const rgb = [] // 定义rgb数组
    if (/^\#[0-9A-F]{3}$/i.test(hex)) { // 判断传入是否为#三位十六进制数
      let sixHex = '#'
      hex.replace(/[0-9A-F]/ig, function(kw) {
        sixHex += kw + kw // 把三位16进制数转化为六位
      })
      hex = sixHex // 保存回hex
    }
    if (/^#[0-9A-F]{6}$/i.test(hex)) { // 判断传入是否为#六位十六进制数
      hex.replace(/[0-9A-F]{2}/ig, function(kw) {
        // eslint-disable-next-line no-eval
        rgb.push(eval('0x' + kw)) // 十六进制转化为十进制并存如数组
      })
      return `rgba(${rgb.join(',')},${alpha / 100})` // 输出RGB格式颜色
    } else {
      return 'rgb(0,0,0)'
    }
  }
  
  export const DEFAULT_YAXIS_EXT_STYLE = {
    show: true,
    position: 'right',
    name: '',
    nameTextStyle: {
      color: '#333333',
      fontSize: 12
    },
    axisLabel: {
      show: true,
      color: '#333333',
      fontSize: '12',
      rotate: 0,
      formatter: '{value}'
    },
    splitLine: {
      show: true,
      lineStyle: {
        color: '#cccccc',
        width: 1,
        style: 'solid'
      }
    },
    axisValue: {
      auto: true,
      min: null,
      max: null,
      split: null,
      splitCount: null
    }
  }
  
  export function uuid() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1)
  }
  
  