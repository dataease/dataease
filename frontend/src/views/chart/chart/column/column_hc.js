export const DEFAULT_COLOR_CASE = {
    value: 'default',
    colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
    alpha: 100,
    tableHeaderBgColor: '#e1eaff',
    tableItemBgColor: '#ffffff',
    tableFontColor: '#000000',
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
  export const BASE_COLUMM = {
    chart: {
      type: 'column', // 图表类型
      options3d: {
        enabled: true,
        alpha: 10, // 内（X）旋转角度
        beta: 10, // 外（Y）旋转角度
        depth: 50, // 图表的总深度。
        viewDistance: 25
      },
      backgroundColor: 'rgba(0,0,0,0)', //背景颜色或渐变
      inverted: false, // 反转坐标轴
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
    legend: {},
  
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
      gridLineWidth: 0,
      minorGridLineWidth: 0
    },
    yAxis: {
      title: {
        text: null,
      },
      gridLineWidth: 0,
      minorGridLineWidth: 0
    },
    series: [
      {
        name: '',
        data: []
      }
    ]
  }

  export const BASE_COLUMN_STACK = {
    chart: {
      type: 'column',
      options3d: {
        enabled: true,
        alpha: 15,
        beta: 15,
        viewDistance: 25,
        depth: 40
      },
      marginTop: 80,
      marginRight: 40,
      backgroundColor: 'rgba(0,0,0,0)'
    },
    credits: {
      enabled: false
    },
    title: {
      text: '',
    },
    legend: {},
    plotOptions: {
      column: {
        stacking: 'normal',
        depth: 40
      }
    },
    tooltip: {},
    xAxis: {
      categories: [],
      gridLineWidth: 0,
      minorGridLineWidth: 0
    },
    yAxis: {
      allowDecimals: false,
      title: {
        text: null
      },
      gridLineWidth: 0,
      minorGridLineWidth: 0
    },
    series: [
      {
        name: '',
        data: []
      }
    ]
  }
  
  let terminalType = 'pc'
  export function baseColumnOption(chart_option, chart, terminal = 'pc', isBase, isStack) {
    terminalType = terminal
    let customAttr = {}
    // console.log('column,chart: ', chart)
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
    }
  
    // 处理data
    if (chart.data) {
      // console.log('column,chart.data',chart.data)
      // chart_option.title.text = chart.title
      // 基础柱状数据处理
      if (isBase) {
        if (chart.data.series.length > 0) {
          chart_option.series[0].name = chart.data.series[0].name
          if (customAttr.color) {
            chart_option.series[0].opacity = customAttr.color.alpha / 100
          }
          // size
          /* if (customAttr.size) {
              chart_option.series[0].radius = [customAttr.size.pieInnerRadius + '%', customAttr.size.pieOuterRadius + '%']
            }*/
          const valueArr = chart.data.series[0].data
          let arr = []
          for (let i = 0; i < valueArr.length; i++) {
            const y = valueArr[i]
            y.name = chart.data.x[i]
            y.y = y.value
            arr.push(chart.data.x[i])
            chart_option.series[0].data.push(y)
          }
          chart_option.xAxis.categories = arr;
          // console.log('isBase,chart_option:::::',chart_option)
        }
      }
      
      // 堆叠柱状数据处理
      if (isStack) {
        if (chart.data.series.length > 0) {
          const series = chart.data.series
          let arr = []
          for (let i = 0; i < series.length; i++) {
            let obj = {
              name: series[i].name,
              data: series[i].data.map(ele => {return ele.value}),
              opacity: series[i].opacity,
              stack: (i%2 === 0)? 'male' : 'female' 
            }
            if (customAttr.color) {
              obj.opacity = customAttr.color.alpha / 100
            }
            arr.push(obj)
          }
          chart_option.series = arr
    
          chart_option.xAxis.categories = chart.data.x

          // console.log('isStack,chart_option',chart_option)
        }
        
      }
    }
  
    componentStyle(chart_option, chart)
    return chart_option
  }
  export function componentStyle(chart_option, chart) {
    const padding = '8px'
    if (chart.customStyle) {
      const customStyle = JSON.parse(chart.customStyle)
  
      if (customStyle.text) {
        chart_option.title.text = customStyle.text.show ? chart.title : ''
        const style = chart_option.title.style ? chart_option.title.style : {}
        style.fontSize = customStyle.text.fontSize
        style.color = customStyle.text.color
        customStyle.text.isItalic ? style.fontStyle = 'italic' : style.fontStyle = 'normal'
        customStyle.text.isBolder ? style.fontWeight = 'bold' : style.fontWeight = 'normal'
        chart_option.title.textStyle = style
        chart_option.title.align = customStyle.text.hPosition
        chart_option.title.verticalAlign = customStyle.text.vPosition
      }
  
      if (customStyle.legend && chart_option.legend) {
        chart_option.plotOptions.column.showInLegend = customStyle.legend.show
        // chart_option.legend.padding = padding
        chart_option.legend.layout = customStyle.legend.orient
        chart_option.legend.verticalAlign = customStyle.legend.vPosition
        chart_option.legend.align = customStyle.legend.hPosition
  
        chart_option.legend.itemStyle = customStyle.legend.textStyle
      }
  
      if (customStyle.background) {
        chart_option.chart.backgroundColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
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
  
  