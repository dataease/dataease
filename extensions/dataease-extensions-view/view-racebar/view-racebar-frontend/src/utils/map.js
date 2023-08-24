import {valueFormatter} from "./formatter";

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
export const DEFAULT_SIZE = {
  barDefault: true,
  barWidth: 40,
  barGap: 0.4,
  lineWidth: 2,
  lineType: 'solid',
  lineSymbol: 'marker',
  lineSymbolSize: 4,
  lineSmooth: true,
  lineArea: false,
  pieInnerRadius: 0,
  pieOuterRadius: 80,
  pieRoseType: 'radius',
  pieRoseRadius: 5,
  funnelWidth: 80,
  radarShape: 'polygon',
  radarSize: 80,
  tableTitleFontSize: 12,
  tableItemFontSize: 12,
  tableTitleHeight: 36,
  tableItemHeight: 36,
  tablePageSize: '20',
  tableColumnMode: 'custom',
  tableColumnWidth: 100,
  tableHeaderAlign: 'left',
  tableItemAlign: 'right',
  gaugeMin: 0,
  gaugeMax: 100,
  gaugeStartAngle: 225,
  gaugeEndAngle: -45,
  dimensionFontSize: 18,
  quotaFontSize: 18,
  spaceSplit: 10,
  dimensionShow: true,
  quotaShow: true,
  scatterSymbol: 'marker',
  scatterSymbolSize: 15,
  symbolOpacity: 5,
  symbolStrokeWidth: 1,
  treemapWidth: 80,
  treemapHeight: 80,
  liquidMax: 100,
  liquidSize: 80,
  liquidOutlineBorder: 4,
  liquidOutlineDistance: 8,
  liquidWaveLength: 128,
  liquidWaveCount: 3,
  liquidShape: 'circle',
  tablePageMode: 'page'
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

export const DEFAULT_SLIDER = {
  show: false,
  auto: true,
  repeat: true,
  timeout: 2,
  fontSize: '10',
  color: '#000000',
  max: 10,
}
export const DEFAULT_Graphic = {
  show: true,
  fontSize: '60',
  color: '#000000',
  alpha: 25,
  bottom: 90,
  right: 60,
}

export const DEFAULT_LABEL = {
  show: true,
  position: 'middle',
  color: '#000000',
  fontSize: '10',
  labelLine: {
    show: true
  },
  fields: null,
  initialized: true
}
export const DEFAULT_TOOLTIP = {
  show: true,
  trigger: 'item',
  confine: true,
  textStyle: {
    fontSize: '10',
    color: '#909399'
  },
  backgroundColor: '#ffffff',
  formatter: '',
}

export const BASE_ECHARTS_SELECT = {
  itemStyle: {
    shadowBlur: 2
  }
}

export const CHART_FONT_FAMILY = [
  {name: '微软雅黑', value: 'Microsoft YaHei'},
  {name: '宋体', value: 'SimSun'},
  {name: '黑体', value: 'SimHei'},
  {name: '楷体', value: 'KaiTi'}
]

export const CHART_FONT_LETTER_SPACE = [
  {name: '0px', value: '0'},
  {name: '1px', value: '1'},
  {name: '2px', value: '2'},
  {name: '3px', value: '3'},
  {name: '4px', value: '4'},
  {name: '5px', value: '5'},
  {name: '6px', value: '6'},
  {name: '7px', value: '7'},
  {name: '8px', value: '8'},
  {name: '9px', value: '9'},
  {name: '10px', value: '10'}
]

export const DEFAULT_TITLE_STYLE = {
  show: true,
  fontSize: '18',
  color: '#000000',
  hPosition: 'left',
  vPosition: 'top',
  isItalic: false,
  isBolder: true,
  remarkShow: false,
  remark: '',
  remarkBackgroundColor: '#ffffffff',
  fontFamily: 'Microsoft YaHei',
  letterSpace: '0',
  fontShadow: false
}
export const DEFAULT_LEGEND_STYLE = {
  show: true,
  hPosition: 'center',
  vPosition: 'bottom',
  orient: 'horizontal',
  icon: 'circle',
  textStyle: {
    color: '#333333',
    fontSize: '12'
  }
}
export const DEFAULT_BACKGROUND_COLOR = {
  color: '#ffffff',
  alpha: 100,
  borderRadius: 5
}
export const DEFAULT_BASE_MAP_STYLE = {
  baseMapTheme: 'light'
}
export const BASE_MAP = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  visualMap: {
    min: 50,
    max: 52,
    text: ['High', 'Low'],
    realtime: false,
    calculable: true,
    inRange: {
      color: ['lightskyblue', 'yellow', 'orangered']
    },
    right: 0
  },

  tooltip: {},
  geo: [
    {
      show: true,
      map: 'BUDDLE_MAP_BORDER',

      emphasis: {
        disabled: true
      },
      itemStyle: {
        borderWidth: 2,
        borderColor: '#d1d1d1',
        borderType: 'solid'
      },

      roam: false
    },
    {
      show: true,
      map: 'BUDDLE_MAP',
      label: {
        normal: {
          show: false
        },
        emphasis: {
          show: false
        }
      },
      itemStyle: {
        areaColor: '#f3f3f3',
        borderType: 'dashed',
        borderColor: '#fff'
      },

      roam: false
    }
  ],
  series: [
    {
      name: '',
      type: 'scatter',
      coordinateSystem: 'geo',
      data: []
    }
  ]
}

export const compareItem = {
  type: 'none', // year-yoy/month-yoy等
  resultData: 'percent', // 对比差sub，百分比percent等
  field: '',
  custom: {
    field: '',
    calcType: '0', // 0-增长值，1-增长率
    timeType: '0', // 0-固定日期，1-日期区间
    currentTime: '',
    compareTime: '',
    currentTimeRange: [],
    compareTimeRange: []
  }
}

export const formatterItem = {
  type: 'auto', // auto,value,percent
  unit: 1, // 换算单位
  suffix: '', // 单位后缀
  decimalCount: 2, // 小数位数
  thousandSeparator: true// 千分符
}

export const DEFAULT_XAXIS_STYLE = {
  show: true,
  position: 'bottom',
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
  axisLine: {
    show: true,
    lineStyle: {
      color: '#cccccc',
      width: 1,
      style: 'solid'
    }
  },
  splitLine: {
    show: false,
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
  },
  axisLabelFormatter: {
    type: 'auto', // auto,value,percent
    unit: 1, // 换算单位
    suffix: '', // 单位后缀
    decimalCount: 2, // 小数位数
    thousandSeparator: true// 千分符
  }
}

export const DEFAULT_YAXIS_STYLE = {
  show: true,
  position: 'left',
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
  axisLine: {
    show: false,
    lineStyle: {
      color: '#cccccc',
      width: 1,
      style: 'solid'
    }
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
  },
  axisLabelFormatter: {
    type: 'auto', // auto,value,percent
    unit: 1, // 换算单位
    suffix: '', // 单位后缀
    decimalCount: 2, // 小数位数
    thousandSeparator: true// 千分符
  }
}

export const DEFAULT_MARGIN_STYLE = {
  marginModel: 'auto',
  marginTop: 40,
  marginBottom: 44,
  marginLeft: 15,
  marginRight: 10
}

export const HORIZONTAL_BAR = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    // top: 40,
    // bottom: 44,
    // left: 15,
    // right: 40,
    //containLabel: true
  },
  tooltip: {},
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect',
    data: []
  },
  xAxis: {
    max: 'dataMax',
    axisLabel: {
      formatter: function (n) {
        return Math.round(n) + '';
      }
    }
  },
  yAxis: {
    type: 'category',
    inverse: true,
    max: 10,
    animationDuration: 300,
    animationDurationUpdate: 300
  },
  dataset: {
    source: []
  },
  series: [
    {
      realtimeSort: true,
      seriesLayoutBy: 'column',
      type: 'bar',
    }
  ],
  // Disable init animation.
  animationDuration: 0,
  animationDurationUpdate: 2000,
  animationEasing: 'linear',
  animationEasingUpdate: 'linear',
  graphic: {
    elements: [
      {
        type: 'text',
        right: 60,
        bottom: 60,
        style: {
          font: 'bolder 50px monospace',
          fill: 'rgba(100, 100, 100, 0.25)'
        },
        z: 100
      }
    ]
  },
  dataZoom: [
    {
      type: 'slider',
      show: false,
      xAxisIndex: [0],
      start: 0,
      end: 100
    },
    {
      type: 'slider',
      show: false,
      yAxisIndex: [0],
      left: '93%',
      start: 0,
      end: 100
    },
    {
      type: 'inside',
      disabled: true,
      xAxisIndex: [0],
      start: 0,
      end: 100
    },
    {
      type: 'inside',
      disabled: true,
      yAxisIndex: [0],
      start: 0,
      end: 100
    }
  ]
}

export function transAxisPosition(chart, axis) {
  if (chart.type.includes('horizontal')) {
    switch (axis.position) {
      case 'top':
        return 'left'
      case 'bottom':
        return 'right'
      case 'left':
        return 'bottom'
      case 'right':
        return 'top'
      default:
        return axis.position
    }
  } else {
    return axis.position
  }
}

const convertData = (mapData, chart) => {
  let maxVal = 0
  const k = terminalType === 'pc' ? 30 : 15
  const names = chart.data.x
  const results = []
  for (let index = 0; index < names.length; index++) {
    const name = names[index];
    const item = chart.data.series[0].data[index]
    results.push({
      name,
      value: (mapData[name] ? mapData[name].concat(item.value) : []),
      dimensionList: item.dimensionList,
      quotaList: item.quotaList
    })
    maxVal = Math.max(maxVal, item.value)
  }
  const rate = k / maxVal

  return {
    value: results,
    rate: rate
  }
}

let terminalType = 'pc'


export function componentStyle(chart_option, chart) {
  let xAxisLabelFormatter = null
  let yAxisLabelFormatter = null
  let yExtAxisLabelFormatter = null
  const xFormatter = function (value) {
    if (!xAxisLabelFormatter) {
      return valueFormatter(value, formatterItem)
    } else {
      return valueFormatter(value, xAxisLabelFormatter)
    }
  }

  const yFormatter = function (value) {
    if (!yAxisLabelFormatter) {
      return valueFormatter(value, formatterItem)
    } else {
      return valueFormatter(value, yAxisLabelFormatter)
    }
  }

  const yExtFormatter = function (value) {
    if (!yExtAxisLabelFormatter) {
      return valueFormatter(value, formatterItem)
    } else {
      return valueFormatter(value, yExtAxisLabelFormatter)
    }
  }

  const padding = '8px'
  if (chart.customStyle) {
    const customStyle = JSON.parse(chart.customStyle)
    if (customStyle.text) {
      chart_option.title.show = customStyle.text.show
      // 水平方向
      if (customStyle.text.hPosition === 'left') {
        chart_option.title.left = padding
      } else if (customStyle.text.hPosition === 'right') {
        chart_option.title.right = padding
      } else {
        chart_option.title.left = customStyle.text.hPosition
      }
      // 垂直方向
      if (customStyle.text.vPosition === 'top') {
        chart_option.title.top = padding
      } else if (customStyle.text.vPosition === 'bottom') {
        chart_option.title.bottom = padding
      } else {
        chart_option.title.top = customStyle.text.vPosition
      }
      const style = chart_option.title.textStyle ? chart_option.title.textStyle : {}
      style.fontSize = customStyle.text.fontSize
      style.color = customStyle.text.color
      customStyle.text.isItalic ? style.fontStyle = 'italic' : style.fontStyle = 'normal'
      customStyle.text.isBolder ? style.fontWeight = 'bold' : style.fontWeight = 'normal'
      chart_option.title.textStyle = style
    }
    if (customStyle.legend && chart_option.legend) {
      chart_option.legend.show = customStyle.legend.show
      // 水平方向
      if (customStyle.legend.hPosition === 'left') {
        chart_option.legend.left = padding
      } else if (customStyle.legend.hPosition === 'right') {
        chart_option.legend.right = padding
      } else {
        chart_option.legend.left = customStyle.legend.hPosition
      }
      // 垂直方向
      if (customStyle.legend.vPosition === 'top') {
        chart_option.legend.top = padding
      } else if (customStyle.legend.vPosition === 'bottom') {
        chart_option.legend.bottom = padding
      } else {
        chart_option.legend.top = customStyle.legend.vPosition
      }
      chart_option.legend.orient = customStyle.legend.orient
      chart_option.legend.icon = customStyle.legend.icon
      chart_option.legend.textStyle = customStyle.legend.textStyle
      if (chart.type === 'treemap' || chart.type === 'gauge') {
        chart_option.legend.show = false
      }
    }

    if (customStyle.margin && customStyle.margin.marginModel && customStyle.margin.marginModel !== 'auto') {
      const unit = getMarginUnit(customStyle.margin)
      const result = {containLabel: true}
      const realUnit = (unit === '%' ? unit : '')
      if (customStyle.margin.marginTop != null) {
        result.top = customStyle.margin.marginTop + realUnit
      }
      if (customStyle.margin.marginBottom != null) {
        result.bottom = customStyle.margin.marginBottom + realUnit
      }
      if (customStyle.margin.marginLeft != null) {
        result.left = customStyle.margin.marginLeft + realUnit
      }
      if (customStyle.margin.marginRight != null) {
        result.right = customStyle.margin.marginRight + realUnit
      }
      if (!chart_option.grid) {
        chart_option.grid = {}
      }
      Object.assign(chart_option.grid, JSON.parse(JSON.stringify(result)))
    }
    if (customStyle.background) {
      chart_option.backgroundColor = hexColorToRGBA(customStyle.background.color, customStyle.background.alpha)
    }
  }
}

export function hexColorToRGBA(hex, alpha) {
  const rgb = [] // 定义rgb数组
  if (/^\#[0-9A-F]{3}$/i.test(hex)) { // 判断传入是否为#三位十六进制数
    let sixHex = '#'
    hex.replace(/[0-9A-F]/ig, function (kw) {
      sixHex += kw + kw // 把三位16进制数转化为六位
    })
    hex = sixHex // 保存回hex
  }
  if (/^#[0-9A-F]{6}$/i.test(hex)) { // 判断传入是否为#六位十六进制数
    hex.replace(/[0-9A-F]{2}/ig, function (kw) {
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

export const getDefaultTemplate = (chart, type, feed, showKey) => {
  if (!chart || !chart.viewFields || !type) return null;
  let viewFields = []
  if (chart.viewFields instanceof Array) {
    viewFields = JSON.parse(JSON.stringify(chart.viewFields))
  } else {
    viewFields = JSON.parse(chart.viewFields)
  }
  const separator = feed ? '\n' : ' '
  return viewFields.filter(field => field.busiType && field.busiType === type).map(field => {
    const fieldName = field.name
    let template = "${" + field.name + "}"
    if (showKey) {
      template = fieldName + "：${" + field.name + "}"
    }
    return template
  }).join(separator)
}

export function uuid() {
  return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
}

export const reverseColor = colorValue => {
  colorValue = '0x' + colorValue.replace(/#/g, '')
  const str = '000000' + (0xFFFFFF - colorValue).toString(16)
  return '#' + str.substring(str.length - 6, str.length)
}

export function seniorCfg(chart_option, chart) {
  if (chart.senior && chart.type && (chart.type.includes('bar') || chart.type.includes('line') || chart.type.includes('mix'))) {
    const senior = JSON.parse(chart.senior)
    if (senior.functionCfg) {
      if (senior.functionCfg.sliderShow) {
        chart_option.dataZoom = [
          {
            type: 'inside',
            start: parseInt(senior.functionCfg.sliderRange[0]),
            end: parseInt(senior.functionCfg.sliderRange[1])
          },
          {
            type: 'slider',
            start: parseInt(senior.functionCfg.sliderRange[0]),
            end: parseInt(senior.functionCfg.sliderRange[1])
          }
        ]
        if (senior.functionCfg.sliderBg) {
          chart_option.dataZoom[1].dataBackground = {
            lineStyle: {color: hexToRgba(senior.functionCfg.sliderBg, 0.5)},
            areaStyle: {color: hexToRgba(senior.functionCfg.sliderBg, 0.5)}
          }
          chart_option.dataZoom[1].borderColor = hexToRgba(senior.functionCfg.sliderBg, 0.3)
        }
        if (senior.functionCfg.sliderFillBg) {
          chart_option.dataZoom[1].selectedDataBackground = {
            lineStyle: {color: senior.functionCfg.sliderFillBg},
            areaStyle: {color: senior.functionCfg.sliderFillBg}
          }
          const rgba = hexToRgba(senior.functionCfg.sliderFillBg, 0.2)
          chart_option.dataZoom[1].fillerColor = rgba
        }
        if (senior.functionCfg.sliderTextClolor) {
          chart_option.dataZoom[1].textStyle = {color: senior.functionCfg.sliderTextClolor}
          const rgba = hexToRgba(senior.functionCfg.sliderTextClolor, 0.5)
          chart_option.dataZoom[1].handleStyle = {color: rgba}
        }

        if (chart.type.includes('horizontal')) {
          chart_option.dataZoom[0].yAxisIndex = [0]
          chart_option.dataZoom[1].yAxisIndex = [0]
          chart_option.dataZoom[1].left = '10px'
        }
      }
    }
    // begin mark line settings
    if (chart_option.series && chart_option.series.length > 0) {
      chart_option.series[0].markLine = {
        symbol: 'none',
        data: []
      }
    }
    if (senior.assistLine && senior.assistLine.length > 0) {
      if (chart_option.series && chart_option.series.length > 0) {
        const customStyle = JSON.parse(chart.customStyle)
        let xAxis, yAxis, axisFormatterCfg
        if (customStyle.xAxis) {
          xAxis = JSON.parse(JSON.stringify(customStyle.xAxis))
          if (chart.type.includes('horizontal')) {
            axisFormatterCfg = xAxis.axisLabelFormatter ? xAxis.axisLabelFormatter : DEFAULT_XAXIS_STYLE.axisLabelFormatter
          }
        }
        if (customStyle.yAxis) {
          yAxis = JSON.parse(JSON.stringify(customStyle.yAxis))
          if (!chart.type.includes('horizontal')) {
            axisFormatterCfg = yAxis.axisLabelFormatter ? yAxis.axisLabelFormatter : DEFAULT_YAXIS_STYLE.axisLabelFormatter
          }
        }

        const fixedLines = senior.assistLine.filter(ele => ele.field === '0')
        const dynamicLines = chart.data.dynamicAssistLines
        const lines = fixedLines.concat(dynamicLines)

        lines.forEach(ele => {
          if (chart.type.includes('horizontal')) {
            chart_option.series[0].markLine.data.push({
              symbol: 'none',
              xAxis: parseFloat(ele.value),
              name: ele.name,
              lineStyle: {
                color: ele.color,
                type: ele.lineType
              },
              label: {
                show: true,
                color: ele.color,
                fontSize: ele.fontSize ? parseInt(ele.fontSize) : 10,
                position: xAxis.position === 'bottom' ? 'insideStartTop' : 'insideEndTop',
                formatter: function (param) {
                  return ele.name + ' : ' + valueFormatter(param.value, axisFormatterCfg)
                }
              },
              tooltip: {
                show: false
              }
            })
          } else {
            chart_option.series[0].markLine.data.push({
              symbol: 'none',
              yAxis: parseFloat(ele.value),
              name: ele.name,
              lineStyle: {
                color: ele.color,
                type: ele.lineType
              },
              label: {
                show: true,
                color: ele.color,
                fontSize: ele.fontSize ? parseInt(ele.fontSize) : 10,
                position: yAxis.position === 'left' ? 'insideStartTop' : 'insideEndTop',
                formatter: function (param) {
                  return ele.name + ' : ' + valueFormatter(param.value, axisFormatterCfg)
                }
              },
              tooltip: {
                show: false
              }
            })
          }
        })
      }
    }
  }
}

const hexToRgba = (hex, opacity) => {
  let rgbaColor = ''
  const reg = /^#[\da-f]{6}$/i
  if (reg.test(hex)) {
    rgbaColor = `rgba(${parseInt('0x' + hex.slice(1, 3))},${parseInt(
      '0x' + hex.slice(3, 5)
    )},${parseInt('0x' + hex.slice(5, 7))},${opacity})`
  }
  return rgbaColor
}

export const getMarginUnit = marginForm => {
  if (!marginForm.marginModel || marginForm.marginModel === 'auto') return null
  if (marginForm.marginModel === 'absolute') return 'px'
  if (marginForm.marginModel === 'relative') return '%'
  return null
}
