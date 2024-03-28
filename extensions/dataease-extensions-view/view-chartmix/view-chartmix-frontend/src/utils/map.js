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
  barWidthPercent: 50,
  lineWidth: 2,
  lineType: 'solid',
  lineSymbol: 'circle',
  lineSymbolSize: 4,
  lineSmooth: true,
  scatterSymbol: 'circle',
  scatterSymbolSize: 15,
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

export const CHART_FONT_FAMILY = [
  { name: '微软雅黑', value: 'Microsoft YaHei' },
  { name: '宋体', value: 'SimSun' },
  { name: '黑体', value: 'SimHei' },
  { name: '楷体', value: 'KaiTi' }
]

export const CHART_FONT_LETTER_SPACE = [
  { name: '0px', value: '0' },
  { name: '1px', value: '1' },
  { name: '2px', value: '2' },
  { name: '3px', value: '3' },
  { name: '4px', value: '4' },
  { name: '5px', value: '5' },
  { name: '6px', value: '6' },
  { name: '7px', value: '7' },
  { name: '8px', value: '8' },
  { name: '9px', value: '9' },
  { name: '10px', value: '10' }
]

export const DEFAULT_LABEL = {
  show: true,
  position: 'middle',
  color: '#000000',
  fontSize: '10',
  subShow: true,
  subPosition: 'middle',
  subColor: '#000000',
  subFontSize: '10',
  formatter: '{c}',
  gaugeFormatter: '{value}',
  labelLine: {
    show: true
  },
  fields: null,
  labelTemplate: '{busiValue}',
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
  tooltipTemplate: '{busiValue}'
}
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

export function baseMapOption(chart_option, chart, mapData, terminal = 'pc') {
  terminalType = terminal
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
      const text = tooltip.formatter.replace(reg, '<br/>')
      tooltip.formatter = function (params) {
        const a = params.seriesName
        const b = params.name
        const c = params.value ? params.value[2] : ''
        return text.replace(new RegExp('{a}', 'g'), a).replace(new RegExp('{b}', 'g'), b).replace(new RegExp('{c}', 'g'), c)
      }
      chart_option.tooltip = tooltip
    }
  }
  // 处理data
  if (chart.data) {
    chart_option.title.text = chart.title
    if (chart.data.series && chart.data.series.length > 0) {
      chart_option.series[0].name = chart.data.series[0].name
      // label
      if (customAttr.label) {
        const text = customAttr.label.formatter
        chart_option.series[0].label = customAttr.label
        chart_option.series[0].label.formatter = function (params) {
          const a = params.seriesName
          const b = params.name
          const c = params.value ? params.value[2] : ''
          return text.replace(new RegExp('{a}', 'g'), a).replace(new RegExp('{b}', 'g'), b).replace(new RegExp('{c}', 'g'), c)
        }
        chart_option.series[0].labelLine = customAttr.label.labelLine
      }

      // visualMap
      const valueArr = chart.data.series[0].data
      if (valueArr && valueArr.length > 0) {
        const values = []
        valueArr.forEach(function (ele) {
          values.push(ele.value)
        })
        chart_option.visualMap.min = Math.min(...values)
        chart_option.visualMap.max = Math.max(...values)
        if (chart_option.visualMap.min === chart_option.visualMap.max) {
          chart_option.visualMap.min = 0
        }
      } else {
        chart_option.visualMap.min = 0
        chart_option.visualMap.max = 0
      }
      if (chart_option.visualMap.min === 0 && chart_option.visualMap.max === 0) {
        chart_option.visualMap.max = 100
      }
      // color
      if (customAttr.color && customAttr.color.colors) {
        chart_option.visualMap.inRange.color = customAttr.color.colors
        chart_option.visualMap.inRange.colorAlpha = customAttr.color.alpha / 100
      }
      // chart_option.visualMap = null

      const convert = convertData(mapData, chart)
      chart_option.series[0].data = convert.value
      chart_option.series[0].symbolSize = val => val[2] * convert.rate

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
    },
    enableDash: false,
    dashStyle: {
      width: 4,
      offset: 5
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
    },
    enableDash: false,
    dashStyle: {
      width: 4,
      offset: 5
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
    },
    enableDash: false,
    dashStyle: {
      width: 4,
      offset: 5
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

export function getLineDash(type) {
  switch (type) {
    case 'solid':
      return [0, 0]
    case 'dashed':
      return [10, 8]
    case 'dotted':
      return [2, 2]
    default:
      return [0, 0]
  }
}
