import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
const { t } = useI18n()

export const DEFAULT_COLOR_CASE = {
  value: 'default',
  colors: [
    '#5470c6',
    '#91cc75',
    '#fac858',
    '#ee6666',
    '#73c0de',
    '#3ba272',
    '#fc8452',
    '#9a60b4',
    '#ea7ccc'
  ],
  alpha: 100,
  tableHeaderBgColor: '#6D9A49',
  tableItemBgColor: '#FFFFFF',
  tableHeaderFontColor: '#000000',
  tableFontColor: '#000000',
  tableStripe: true,
  dimensionColor: '#000000',
  quotaColor: '#5470c6',
  tableBorderColor: '#E6E7E4',
  seriesColors: [], // 格式：{"name":"s1","color":"","isCustom":false}
  areaBorderColor: '#303133',
  gradient: false,
  areaBaseColor: '#FFFFFF',
  tableScrollBarColor: 'rgba(0, 0, 0, 0.15)',
  tableScrollBarHoverColor: 'rgba(0, 0, 0, 0.4)',
  mapStyle: 'normal',
  mapLineGradient: false,
  mapLineSourceColor: '#146C94',
  mapLineTargetColor: '#576CBC'
}

export const DEFAULT_COLOR_CASE_LIGHT = {
  value: 'default',
  colors: [
    '#5470c6',
    '#91cc75',
    '#fac858',
    '#ee6666',
    '#73c0de',
    '#3ba272',
    '#fc8452',
    '#9a60b4',
    '#ea7ccc'
  ],
  alpha: 100,
  tableHeaderBgColor: '#6D9A49',
  tableItemBgColor: '#FFFFFF',
  tableHeaderFontColor: '#000000',
  tableFontColor: '#000000',
  tableStripe: true,
  dimensionColor: '#000000',
  quotaColor: '#5470c6',
  tableBorderColor: '#E6E7E4',
  seriesColors: [], // 格式：{"name":"s1","color":"","isCustom":false}
  areaBorderColor: '#303133',
  gradient: false,
  areaBaseColor: '#FFFFFF',
  tableScrollBarColor: 'rgba(0, 0, 0, 0.15)',
  tableScrollBarHoverColor: 'rgba(0, 0, 0, 0.4)',
  mapStyle: 'normal',
  mapLineGradient: false,
  mapLineSourceColor: '#146C94',
  mapLineTargetColor: '#576CBC'
}

export const DEFAULT_COLOR_CASE_DARK = {
  value: 'default',
  colors: [
    '#5470c6',
    '#91cc75',
    '#fac858',
    '#ee6666',
    '#73c0de',
    '#3ba272',
    '#fc8452',
    '#9a60b4',
    '#ea7ccc'
  ],
  alpha: 100,
  tableHeaderBgColor: '#5470c6',
  tableItemBgColor: '#131E42',
  tableFontColor: '#ffffff',
  tableStripe: true,
  dimensionColor: '#ffffff',
  quotaColor: '#5470c6',
  tableBorderColor: '#CCCCCC',
  seriesColors: [], // 格式：{"name":"s1","color":"","isCustom":false}
  areaBorderColor: '#EBEEF5',
  areaBaseColor: '5470C6',
  tableScrollBarColor: 'rgba(255, 255, 255, 0.5)',
  tableScrollBarHoverColor: 'rgba(255, 255, 255, 0.8)',
  mapStyle: 'darkblue',
  mapLineGradient: false,
  mapLineSourceColor: '#2F58CD',
  mapLineTargetColor: '#3795BD'
}

export const TAB_COMMON_STYLE_BASE = {
  headPosition: 'left'
}
export const TAB_COMMON_STYLE_LIGHT = {
  ...TAB_COMMON_STYLE_BASE,
  headFontColor: '#000000',
  headFontActiveColor: '#000000',
  headBorderColor: '#ffffff',
  headBorderActiveColor: '#ffffff'
}
export const TAB_COMMON_STYLE_DARK = {
  ...TAB_COMMON_STYLE_BASE,
  headFontColor: '#ffffff',
  headFontActiveColor: '#ffffff',
  headBorderColor: '#000000',
  headBorderActiveColor: '#000000'
}

export const FILTER_COMMON_STYLE_BASE = {
  horizontal: 'left',
  vertical: 'top'
}

export const FILTER_COMMON_STYLE_LIGHT = {
  ...FILTER_COMMON_STYLE_BASE,
  color: '#000000',
  brColor: '',
  wordColor: '',
  innerBgColor: ''
}

export const FILTER_COMMON_STYLE_DARK = {
  ...FILTER_COMMON_STYLE_BASE,
  color: '#FFFFFF',
  brColor: '#4E4B4B',
  wordColor: '#FFFFFF',
  innerBgColor: '#131E42'
}

export const DEFAULT_TAB_COLOR_CASE_BASE = {
  headPosition: 'left'
}

export const DEFAULT_TAB_COLOR_CASE_DARK = {
  ...DEFAULT_TAB_COLOR_CASE_BASE,
  headFontColor: '#FFFFFF',
  headFontActiveColor: '#FFFFFF',
  headBorderColor: '#131E42',
  headBorderActiveColor: '#131E42'
}

export const DEFAULT_TAB_COLOR_CASE_LIGHT = {
  ...DEFAULT_TAB_COLOR_CASE_BASE,
  headFontColor: '#OOOOOO',
  headFontActiveColor: '#OOOOOO',
  headBorderColor: '#OOOOOO',
  headBorderActiveColor: '#OOOOOO'
}

export const DEFAULT_SIZE = {
  barDefault: true,
  barWidth: 40,
  barGap: 0.4,
  lineWidth: 2,
  lineType: 'solid',
  lineSymbol: 'circle',
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
  tableAutoBreakLine: false,
  gaugeMinType: 'fix', // fix or dynamic
  gaugeMinField: {
    id: '',
    summary: ''
  },
  gaugeMin: 0,
  gaugeMaxType: 'fix', // fix or dynamic
  gaugeMaxField: {
    id: '',
    summary: ''
  },
  gaugeMax: 100,
  gaugeStartAngle: 225,
  gaugeEndAngle: -45,
  gaugeTickCount: 5,
  dimensionFontSize: 18,
  quotaFontSize: 18,
  spaceSplit: 10,
  dimensionShow: true,
  quotaShow: true,
  quotaFontFamily: 'Microsoft YaHei',
  quotaFontIsBolder: false,
  quotaFontIsItalic: false,
  quotaLetterSpace: '0',
  quotaFontShadow: false,
  dimensionFontFamily: 'Microsoft YaHei',
  dimensionFontIsBolder: false,
  dimensionFontIsItalic: false,
  dimensionLetterSpace: '0',
  dimensionFontShadow: false,
  scatterSymbol: 'circle',
  scatterSymbolSize: 20,
  treemapWidth: 80,
  treemapHeight: 80,
  liquidMax: 100,
  liquidMaxType: 'fix', // fix or dynamic
  liquidMaxField: {
    id: '',
    summary: ''
  },
  liquidSize: 80,
  liquidOutlineBorder: 4,
  liquidOutlineDistance: 8,
  liquidWaveLength: 128,
  liquidWaveCount: 3,
  liquidShape: 'circle',
  tablePageMode: 'page',
  symbolOpacity: 0.7,
  symbolStrokeWidth: 2,
  showIndex: false,
  indexLabel: '序号',
  hPosition: 'center',
  vPosition: 'center',
  mapPitch: 0,
  mapLineType: 'arc',
  mapLineWidth: 1,
  mapLineAnimate: true,
  mapLineAnimateDuration: 3,
  mapLineAnimateInterval: 1,
  mapLineAnimateTrailLength: 1
}
export const DEFAULT_SUSPENSION = {
  show: true
}

export const DEFAULT_MARK = {
  fieldId: '',
  conditions: []
}
export const DEFAULT_LABEL = {
  show: false,
  position: 'top',
  color: '#909399',
  fontSize: 10,
  formatter: '',
  gaugeFormatter: '{value}',
  labelLine: {
    show: true
  },
  gaugeLabelFormatter: {
    type: 'value', // auto,value,percent
    unit: 1, // 换算单位
    suffix: '', // 单位后缀
    decimalCount: 2, // 小数位数
    thousandSeparator: true // 千分符
  },
  reserveDecimalCount: 2,
  labelContent: ['dimension', 'proportion']
}
export const DEFAULT_TOOLTIP = {
  show: true,
  trigger: 'item',
  confine: true,
  textStyle: {
    fontSize: '10',
    color: '#909399'
  },
  formatter: '',
  backgroundColor: '#ffffff'
}
export const DEFAULT_TOTAL = {
  row: {
    showGrandTotals: true,
    showSubTotals: true,
    reverseLayout: false,
    reverseSubLayout: false,
    label: '总计',
    subLabel: '小计',
    subTotalsDimensions: [],
    calcTotals: {
      aggregation: 'SUM'
    },
    calcSubTotals: {
      aggregation: 'SUM'
    },
    totalSort: 'none', // asc,desc
    totalSortField: ''
  },
  col: {
    showGrandTotals: true,
    showSubTotals: true,
    reverseLayout: false,
    reverseSubLayout: false,
    label: '总计',
    subLabel: '小计',
    subTotalsDimensions: [],
    calcTotals: {
      aggregation: 'SUM'
    },
    calcSubTotals: {
      aggregation: 'SUM'
    },
    totalSort: 'none', // asc,desc
    totalSortField: ''
  }
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
  remarkBackgroundColor: '#ffffff',
  fontFamily: 'Microsoft YaHei',
  letterSpace: '0',
  fontShadow: false
}

export const DEFAULT_TITLE_STYLE_BASE = {
  show: true,
  fontSize: '18',
  hPosition: 'left',
  vPosition: 'top',
  isItalic: false,
  isBolder: true,
  remarkShow: false,
  remark: '',
  fontFamily: 'Microsoft YaHei',
  letterSpace: '0',
  fontShadow: false
}

export const DEFAULT_TITLE_STYLE_LIGHT = {
  ...DEFAULT_TITLE_STYLE_BASE,
  color: '#000000',
  remarkBackgroundColor: '#ffffff'
}

export const DEFAULT_TITLE_STYLE_DARK = {
  ...DEFAULT_TITLE_STYLE_BASE,
  color: '#FFFFFF',
  remarkBackgroundColor: '#5A5C62'
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

export const DEFAULT_LEGEND_STYLE_BASE = {
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

export const DEFAULT_LEGEND_STYLE_LIGHT = {
  ...DEFAULT_LEGEND_STYLE_BASE,
  textStyle: {
    color: '#333333',
    fontSize: '12'
  }
}

export const DEFAULT_LEGEND_STYLE_DARK = {
  ...DEFAULT_LEGEND_STYLE_BASE,
  textStyle: {
    color: '#ffffff',
    fontSize: '12'
  }
}

export const DEFAULT_MARGIN_STYLE = {
  marginModel: 'auto',
  marginTop: 40,
  marginBottom: 44,
  marginLeft: 15,
  marginRight: 10
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
    min: '',
    max: '',
    split: '',
    splitCount: ''
  },
  axisLabelFormatter: {
    type: 'auto', // auto,value,percent
    unit: 1, // 换算单位
    suffix: '', // 单位后缀
    decimalCount: 2, // 小数位数
    thousandSeparator: true // 千分符
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
    min: '',
    max: '',
    split: '',
    splitCount: ''
  },
  axisLabelFormatter: {
    type: 'auto', // auto,value,percent
    unit: 1, // 换算单位
    suffix: '', // 单位后缀
    decimalCount: 2, // 小数位数
    thousandSeparator: true // 千分符
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
    thousandSeparator: true // 千分符
  }
}
export const DEFAULT_BACKGROUND_COLOR = {
  color: '#ffffff',
  alpha: 0,
  borderRadius: 0
}
export const DEFAULT_SPLIT = {
  name: {
    show: true,
    color: '#999999',
    fontSize: '12'
  },
  splitNumber: 5,
  axisLine: {
    show: true,
    lineStyle: {
      color: '#999999',
      width: 1,
      type: 'solid'
    }
  },
  axisTick: {
    show: false,
    length: 5,
    lineStyle: {
      color: '#999999',
      width: 1,
      type: 'solid'
    }
  },
  axisLabel: {
    show: false,
    rotate: 0,
    margin: 8,
    color: '#999999',
    fontSize: '12',
    formatter: '{value}'
  },
  splitLine: {
    show: true,
    lineStyle: {
      color: '#999999',
      width: 1,
      type: 'solid'
    }
  },
  splitArea: {
    show: true
  }
}
export const DEFAULT_FUNCTION_CFG = {
  sliderShow: false,
  sliderRange: [0, 10],
  sliderBg: '#FFFFFF',
  sliderFillBg: '#BCD6F1',
  sliderTextColor: '#999999',
  emptyDataStrategy: 'breakLine'
}
export const DEFAULT_THRESHOLD = {
  gaugeThreshold: '',
  labelThreshold: [],
  tableThreshold: [],
  textLabelThreshold: []
}
export const DEFAULT_SCROLL = {
  open: false,
  row: 1,
  interval: 2000,
  step: 50
}
// chart config
export const BASE_BAR = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
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
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [],
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
export const HORIZONTAL_BAR = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
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
    type: 'value'
  },
  yAxis: {
    data: []
  },
  series: [],
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

export const BASE_LINE = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
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
    boundaryGap: false,
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [],
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

export const BASE_PIE = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
  },
  tooltip: {},
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect'
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['0%', '60%'],
      avoidLabelOverlap: false,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      },
      data: []
    }
  ]
}

export const BASE_FUNNEL = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect'
  },
  series: [
    {
      name: '',
      type: 'funnel',
      left: 'center',
      top: 60,
      bottom: 60,
      width: '80%',
      min: 0,
      max: 100,
      minSize: '0%',
      maxSize: '100%',
      sort: 'descending',
      gap: 1,
      labelLine: {
        length: 10,
        lineStyle: {
          width: 1,
          type: 'solid'
        }
      },
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 1
      },
      emphasis: {
        label: {
          fontSize: 20
        }
      },
      data: []
    }
  ]
}

export const BASE_RADAR = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
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
  radar: {
    shape: 'polygon',
    name: {
      show: true,
      color: '#999999',
      fontSize: '12'
    },
    splitNumber: 5,
    axisLine: {
      show: true,
      lineStyle: {
        color: '#999999',
        width: 1,
        type: 'solid'
      }
    },
    axisTick: {
      show: false,
      length: 5,
      lineStyle: {
        color: '#999999',
        width: 1,
        type: 'solid'
      }
    },
    axisLabel: {
      show: false,
      rotate: 0,
      margin: 8,
      color: '#999999',
      fontSize: '12',
      formatter: '{value}'
    },
    splitLine: {
      show: true,
      lineStyle: {
        color: '#999999',
        width: 1,
        type: 'solid'
      }
    },
    splitArea: {
      show: true
    },
    indicator: []
  },
  series: []
}

export const BASE_GAUGE = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
  },
  tooltip: {},
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect'
  },
  series: [
    {
      name: '',
      type: 'gauge',
      startAngle: 225,
      endAngle: -45,
      min: 0,
      max: 100,
      progress: {
        show: true
      },
      detail: {
        show: true,
        valueAnimation: true,
        formatter: '{value}'
      },
      data: []
    }
  ]
}

export const BASE_CHART_STRING = {
  stylePriority: 'view',
  xAxis: '[]',
  yAxis: '[]',
  show: true,
  type: 'panel',
  title: '',
  customAttr: JSON.stringify({
    color: DEFAULT_COLOR_CASE,
    tableColor: DEFAULT_COLOR_CASE,
    size: DEFAULT_SIZE,
    label: DEFAULT_LABEL,
    tooltip: DEFAULT_TOOLTIP
  }),
  customStyle: JSON.stringify({
    text: DEFAULT_TITLE_STYLE,
    legend: DEFAULT_LEGEND_STYLE,
    xAxis: DEFAULT_XAXIS_STYLE,
    yAxis: DEFAULT_YAXIS_STYLE,
    yAxisExt: DEFAULT_YAXIS_EXT_STYLE
  }),
  customFilter: '[]'
}

export const BASE_CHART = {
  xAxis: [],
  yAxis: [],
  show: true,
  type: 'panel',
  title: '',
  customAttr: {
    color: DEFAULT_COLOR_CASE,
    tableColor: DEFAULT_COLOR_CASE,
    size: DEFAULT_SIZE,
    label: DEFAULT_LABEL,
    tooltip: DEFAULT_TOOLTIP
  },
  customStyle: {
    text: DEFAULT_TITLE_STYLE,
    legend: DEFAULT_LEGEND_STYLE,
    xAxis: DEFAULT_XAXIS_STYLE,
    yAxis: DEFAULT_YAXIS_STYLE,
    yAxisExt: DEFAULT_YAXIS_EXT_STYLE
  },
  customFilter: []
}

export const BASE_MAP = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },

  tooltip: {},
  visualMap: {
    min: 50,
    max: 52,
    text: ['High', 'Low'],
    realtime: false,
    calculable: true,
    inRange: {
      color: ['lightskyblue', 'yellow', 'orangered']
    },
    seriesIndex: 0,
    textStyle: {},
    right: 0
  },
  geo: {
    map: 'MAP',
    roam: false,
    nameMap: {},
    itemStyle: {
      normal: {},
      emphasis: {
        label: {
          show: false
        }
      }
    }
  },
  series: [
    {
      name: '',
      type: 'map',
      geoIndex: 0,
      roam: true,
      data: [],
      itemStyle: {
        normal: {},
        emphasis: {
          label: {
            show: false
          }
        }
      }
    }
  ]
}

export const BASE_SCATTER = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
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
    data: [],
    boundaryGap: false
  },
  yAxis: {
    type: 'value'
  },
  series: [],
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

export const BASE_TREEMAP = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
  },
  tooltip: {},
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect'
  },
  series: [
    {
      // name: '',
      type: 'treemap',
      roam: true,
      itemStyle: {
        gapWidth: 2
      },
      breadcrumb: {
        show: false
      },
      data: []
    }
  ]
}

export const BASE_MIX = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
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
    data: []
  },
  yAxis: [
    {
      type: 'value'
    },
    {
      type: 'value'
    }
  ],
  series: [],
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

export const COLOR_CASES = [
  {
    name: t('chart.color_default'),
    value: 'default',
    colors: [
      '#5470c6',
      '#91cc75',
      '#fac858',
      '#ee6666',
      '#73c0de',
      '#3ba272',
      '#fc8452',
      '#9a60b4',
      '#ea7ccc'
    ]
  },
  {
    name: t('chart.color_retro'),
    value: 'retro',
    colors: [
      '#0780cf',
      '#765005',
      '#fa6d1d',
      '#0e2c82',
      '#b6b51f',
      '#da1f18',
      '#701866',
      '#f47a75',
      '#009db2'
    ]
  },
  {
    name: t('chart.color_elegant'),
    value: 'elegant',
    colors: [
      '#95a2ff',
      '#fa8080',
      '#ffc076',
      '#fae768',
      '#87e885',
      '#3cb9fc',
      '#73abf5',
      '#cb9bff',
      '#434348'
    ]
  },
  {
    name: t('chart.color_future'),
    value: 'future',
    colors: [
      '#63b2ee',
      '#76da91',
      '#f8cb7f',
      '#f89588',
      '#7cd6cf',
      '#9192ab',
      '#7898e1',
      '#efa666',
      '#eddd86'
    ]
  },
  {
    name: t('chart.color_gradual'),
    value: 'gradual',
    colors: [
      '#71ae46',
      '#96b744',
      '#c4cc38',
      '#ebe12a',
      '#eab026',
      '#e3852b',
      '#d85d2a',
      '#ce2626',
      '#ac2026'
    ]
  },
  {
    name: t('chart.color_simple'),
    value: 'simple',
    colors: [
      '#929fff',
      '#9de0ff',
      '#ffa897',
      '#af87fe',
      '#7dc3fe',
      '#bb60b2',
      '#433e7c',
      '#f47a75',
      '#009db2'
    ]
  },
  {
    name: t('chart.color_business'),
    value: 'business',
    colors: [
      '#194f97',
      '#555555',
      '#bd6b08',
      '#00686b',
      '#c82d31',
      '#625ba1',
      '#898989',
      '#9c9800',
      '#007f54'
    ]
  },
  {
    name: t('chart.color_gentle'),
    value: 'gentle',
    colors: [
      '#5b9bd5',
      '#ed7d31',
      '#70ad47',
      '#ffc000',
      '#4472c4',
      '#91d024',
      '#b235e6',
      '#02ae75',
      '#5b9bd5'
    ]
  },
  {
    name: t('chart.color_technology'),
    value: 'technology',
    colors: [
      '#05f8d6',
      '#0082fc',
      '#fdd845',
      '#22ed7c',
      '#09b0d3',
      '#1d27c9',
      '#f9e264',
      '#f47a75',
      '#009db2'
    ]
  },
  {
    name: t('chart.color_light'),
    value: 'light',
    colors: [
      '#884898',
      '#808080',
      '#82ae46',
      '#00a3af',
      '#ef8b07',
      '#007bbb',
      '#9d775f',
      '#fae800',
      '#5f9b3c'
    ]
  },
  {
    name: t('chart.color_classical'),
    value: 'classical',
    colors: [
      '#007bbb',
      '#ffdb4f',
      '#dd4b4b',
      '#2ca9e1',
      '#ef8b07',
      '#4a488e',
      '#82ae46',
      '#dd4b4b',
      '#bb9581'
    ]
  },
  {
    name: t('chart.color_fresh'),
    value: 'fresh',
    colors: [
      '#5f9b3c',
      '#75c24b',
      '#83d65f',
      '#aacf53',
      '#c7dc68',
      '#d8e698',
      '#e0ebaf',
      '#bbc8e6',
      '#e5e5e5'
    ]
  },
  {
    name: t('chart.color_energy'),
    value: 'energy',
    colors: [
      '#ef8b07',
      '#2a83a2',
      '#f07474',
      '#c55784',
      '#274a78',
      '#7058a3',
      '#0095d9',
      '#75c24b',
      '#808080'
    ]
  },
  {
    name: t('chart.color_red'),
    value: 'red',
    colors: [
      '#ff0000',
      '#ef8b07',
      '#4c6cb3',
      '#f8e944',
      '#69821b',
      '#9c5ec3',
      '#00ccdf',
      '#f07474',
      '#bb9581'
    ]
  },
  {
    name: t('chart.color_fast'),
    value: 'fast',
    colors: [
      '#fae800',
      '#00c039',
      '#0482dc',
      '#bb9581',
      '#ff7701',
      '#9c5ec3',
      '#00ccdf',
      '#00c039',
      '#ff7701'
    ]
  },
  {
    name: t('chart.color_spiritual'),
    value: 'spiritual',
    colors: [
      '#00a3af',
      '#4da798',
      '#57baaa',
      '#62d0bd',
      '#6ee4d0',
      '#86e7d6',
      '#aeede1',
      '#bde1e6',
      '#e5e5e5'
    ]
  }
]

export const BASE_ECHARTS_SELECT = {
  itemStyle: {
    shadowBlur: 2
  }
}

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

export const NOT_SUPPORT_PAGE_DATASET = [
  'kylin',
  'sqlServer',
  'es',
  'presto',
  'ds_doris',
  'StarRocks',
  'impala'
]

export const SUPPORT_Y_M = ['y', 'y_M', 'y_M_d']

export const DEFAULT_MAP = {
  mapPitch: 0,
  lineType: 'line',
  lineWidth: 1,
  lineAnimate: true,
  lineAnimateDuration: 4,
  lineAnimateInterval: 0.5,
  lineAnimateTrailLength: 0.1
}

export const NEW_CHART = {
  id: null, // 视图id
  title: '图表',
  sceneId: 0, // 仪表板id
  tableId: '', // 数据集id
  type: 'bar',
  render: 'antv',
  resultCount: 100,
  resultMode: 'all',
  refreshViewEnable: false,
  refreshTime: 5,
  refreshUnit: 'minute',
  xAxis: [],
  xAxisExt: [],
  yAxis: [],
  yAxisExt: [],
  extStack: [],
  drillFields: [],
  viewFields: [],
  extBubble: [],
  customFilter: [],
  customAttr: {
    color: DEFAULT_COLOR_CASE,
    size: DEFAULT_SIZE,
    label: DEFAULT_LABEL,
    tooltip: DEFAULT_TOOLTIP,
    totalCfg: DEFAULT_TOTAL
  },
  customStyle: {
    text: DEFAULT_TITLE_STYLE,
    legend: DEFAULT_LEGEND_STYLE,
    xAxis: DEFAULT_XAXIS_STYLE,
    yAxis: DEFAULT_YAXIS_STYLE,
    yAxisExt: DEFAULT_YAXIS_EXT_STYLE,
    split: DEFAULT_SPLIT
  },
  senior: {
    functionCfg: DEFAULT_FUNCTION_CFG,
    assistLine: [],
    threshold: DEFAULT_THRESHOLD,
    scrollCfg: DEFAULT_SCROLL
  }
}

export const CHART_TYPE_CONFIGS = [
  {
    category: 'quota',
    title: t('chart.chart_type_quota'),
    details: [
      {
        render: 'antv',
        category: 'quota',
        value: 'gauge',
        title: t('chart.chart_gauge'),
        icon: 'gauge',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      },
      {
        render: 'antv',
        category: 'quota',
        value: 'liquid',
        title: t('chart.chart_liquid'),
        icon: 'liquid',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      }
    ]
  },
  {
    category: 'table',
    title: t('chart.chart_type_table'),
    details: [
      {
        render: 'antv',
        category: 'table',
        value: 'table-normal',
        title: t('chart.chart_table_normal'),
        icon: 'table-normal',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      },
      {
        render: 'antv',
        category: 'table',
        value: 'table-info',
        title: t('chart.chart_table_info'),
        icon: 'table-info',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      }
    ]
  },
  {
    category: 'trend',
    title: t('chart.chart_type_trend'),
    details: [
      {
        render: 'antv',
        category: 'trend',
        value: 'line',
        title: t('chart.chart_line'),
        icon: 'line',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      },
      {
        render: 'antv',
        category: 'trend',
        value: 'area',
        title: t('chart.chart_area'),
        icon: 'area',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      }
    ]
  },
  {
    category: 'compare',
    title: t('chart.chart_type_compare'),
    details: [
      {
        render: 'antv',
        category: 'compare',
        value: 'bar',
        title: t('chart.chart_bar'),
        icon: 'bar',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'bar-horizontal',
        title: t('chart.chart_bar_horizontal'),
        icon: 'bar-horizontal',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      }
    ]
  },
  {
    category: 'distribute',
    title: t('chart.chart_type_distribute'),
    details: [
      {
        render: 'antv',
        category: 'distribute',
        value: 'pie',
        title: t('chart.chart_pie'),
        icon: 'pie',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      },
      {
        render: 'antv',
        category: 'distribute',
        value: 'pie-rose',
        title: t('chart.chart_pie_rose'),
        icon: 'pie-rose',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      },
      {
        render: 'antv',
        category: 'distribute',
        value: 'word-cloud',
        title: t('chart.chart_word_cloud'),
        icon: 'word-cloud',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      }
    ]
  },
  {
    category: 'map',
    title: t('chart.chart_type_space'),
    details: [
      {
        render: 'antv',
        category: 'map',
        value: 'map',
        title: t('chart.chart_map'),
        icon: 'map_mini',
        properties: [
          'background-overall-component',
          'basic-style-selector',
          'color-selector',
          'size-selector',
          'label-selector',
          'tooltip-selector',
          'x-axis-selector',
          'y-axis-selector',
          'title-selector',
          'legend-selector'
        ],
        propertyInner: {
          'background-overall-component': ['all'],
          'basic-style-selector': ['all'],
          'color-selector': ['all'],
          'size-selector': ['all'],
          'label-selector': ['all'],
          'tooltip-selector': ['all'],
          'x-axis-selector': ['all'],
          'y-axis-selector': ['all'],
          'title-selector': ['all'],
          'legend-selector': ['all']
        }
      }
    ]
  }
]

export const DEFAULT_BASIC_STYLE: ChartBasicStyle = {
  alpha: 100,
  tableBorderColor: '#CCCCCC',
  tableScrollBarColor: 'rgba(255, 255, 255, 0.5)',
  tableColumnMode: 'adopt',
  tableColumnWidth: 100,
  tablePageMode: 'page',
  tablePageSize: 20,
  gaugeStyle: 'default',
  colorScheme: 'default',
  colors: [
    '#5470c6',
    '#91cc75',
    '#fac858',
    '#ee6666',
    '#73c0de',
    '#3ba272',
    '#fc8452',
    '#9a60b4',
    '#ea7ccc'
  ],
  mapVendor: 'amap',
  gradient: false,
  lineWidth: 2,
  lineSymbol: 'circle',
  lineSymbolSize: 4,
  lineSmooth: true,
  barDefault: true,
  barWidth: 40,
  barGap: 0.4,
  lineType: 'solid',
  scatterSymbol: 'circle',
  scatterSymbolSize: 20,
  radarShape: 'polygon',
  mapStyle: 'normal',
  areaBorderColor: '#EBEEF5',
  suspension: true,
  areaBaseColor: '#ffffff',
  symbolOpacity: 0.7,
  symbolStrokeWidth: 2
}

export const BASE_VIEW_CONFIG = {
  id: '', // 视图id
  title: '图表',
  sceneId: 0, // 仪表板id
  tableId: '', // 数据集id
  type: 'bar',
  render: 'antv',
  resultCount: 100,
  resultMode: 'all',
  refreshViewEnable: false,
  refreshTime: 5,
  refreshUnit: 'minute',
  xAxis: [],
  xAxisExt: [],
  yAxis: [],
  yAxisExt: [],
  extStack: [],
  drillFields: [],
  viewFields: [],
  extBubble: [],
  extLabel: [],
  extTooltip: [],
  customFilter: [],
  customAttr: {
    basicStyle: DEFAULT_BASIC_STYLE,
    color: DEFAULT_COLOR_CASE,
    size: DEFAULT_SIZE,
    label: DEFAULT_LABEL,
    tooltip: DEFAULT_TOOLTIP,
    totalCfg: DEFAULT_TOTAL,
    map: {
      id: '000',
      level: 'world'
    }
  },
  customStyle: {
    text: DEFAULT_TITLE_STYLE,
    legend: DEFAULT_LEGEND_STYLE,
    xAxis: DEFAULT_XAXIS_STYLE,
    yAxis: DEFAULT_YAXIS_STYLE,
    yAxisExt: DEFAULT_YAXIS_EXT_STYLE,
    split: DEFAULT_SPLIT
  },
  senior: {
    functionCfg: DEFAULT_FUNCTION_CFG,
    assistLine: [],
    threshold: DEFAULT_THRESHOLD,
    scrollCfg: DEFAULT_SCROLL
  }
}

export function getScaleValue(propValue, scale) {
  const propValueTemp = Math.round(propValue * scale)
  return propValueTemp > 1 ? propValueTemp : 1
}

export function getViewConfig(name) {
  let viewConfigResult = null
  CHART_TYPE_CONFIGS.forEach(category => {
    category.details.forEach(viewConfig => {
      if (viewConfig.value === name) {
        viewConfigResult = deepCopy(viewConfig)
      }
    })
  })
  return viewConfigResult
}
