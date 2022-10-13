import { color } from "echarts"

export const DEFAULT_COLOR_CASE = {
  value: 'default',
  value1: 'default',
  bgColor: '#031F3E',
  variety: false,
  colors: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
  colors1: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
  alpha: 100,
  tableHeaderBgColor: '#e1eaff',
  tableItemBgColor: '#ffffff',
  tableFontColor: '#000000',
  tableInfoFontColor: '#000000',
  tableHeightColor: '#fff',
  tableHeightFontColor: '#333',
  tableStripe: true,
  dimensionColor: '#000000',
  quotaColor: '#000000',
  tableBorderColor: '#cfdaf4',

  innerRing: '#eeeeee',
  outerRing: '#eeeeee'
}
export const DEFAULT_SIZE = {
  automatic: false,
  tableRollingRate: 30,
  heightLightFontSize: 12,
  highlightNumber: 2,
  tableRowsNumber: 5,
  tableLightFontSize: 12,
  heightLightLine: 3,
  automaticTime: 2000,
  tableHeightLight: 0,
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
  pieCircleLeft: 50,
  pieCircleTop: 50,
  pieKeyValue: 1,
  pieRoseOffset: 10,
  pieRoseType: 'radius',
  pieRoseRadius: 5,
  funnelWidth: 80,
  radarShape: 'polygon',
  radarSize: 80,
  tableTitleFontSize: 12,
  tableItemFontSize: 12,
  tableSpacing: 10,
  tableHeightWidth: 80,
  tableTitleHeight: 36,
  tableItemHeight: 36,
  tablePageSize: '20',
  tableColumnMode: 'custom',
  tableColumnWidth: 100,
  tableHeaderAlign: 'left',
  tableItemAlign: 'right',

  barBorderRadius: 0,
  barBorderValue: 0,
  spaceleft: 10,
  xPaddingOffst: 10,
  spaceRight: 10,
  spaceTop: 32,
  spaceBottom: 30,
  containLabel: true,

  wordMin: 10,
  wordMax: 32,
  wordShape: 'pentagon',

  alpha: 10,
  beta: 0,
  depth: 20,

  gaugeMin: 0,
  gaugeMax: 100,
  gaugeStartAngle: 225,
  gaugeEndAngle: -45,
  dimensionFontSize: 18,
  quotaFontSize: 18,
  spaceSplit: 10,
  dimensionShow: true,
  quotaShow: true,
  scatterSymbol: 'circle',
  scatterSymbolSize: 20,
  treemapWidth: 80,
  treemapHeight: 80,
  liquidMax: 100,
  liquidShow: false,
  liquidBan: true,
  liquidSize: 80,
  liquidOutlineBorder: 4,
  liquidOutlineDistance: 8,
  liquidWaveLength: 128,
  liquidWaveCount: 3,
  liquidShape: 'circle'

}
export const DEFAULT_LABEL = {
  show: false,
  position: 'top',
  color: '#909399',
  fontSize: '10',
  formatter: '{c}',
  gaugeFormatter: '{value}',
  labelLine: {
    show: true
  },
  progressFontSize: '14',
  progressFontColor: '#000000',
  progressPosition: 'top',
  progressInside: true,
  strokeWidth: 20,
  progressColor: '#409EFF',

  dragEnable: false,
  repulsion: 100,
  edgeLength: 10,
  gravity: 0.1,
  reductionRate: 50,
  shadowBlur: 10,
  variety_depth: 1,
  scaleLimitMax: 0.7,
  scaleLimitMin: 0.1,

  popShow: false,
  popOpen: 'right',
  popLeft: 0,
  popTop: 0,
  popTitleBackground: '#082456',
  popTitleColor: '#FFFFFF',
  popHeight: 30,
  popPosition: 'center',
  popContentBackground: '#1b2642',
  popContentColor: '#FFFFFF',
  popContentHeight: 25,
  // popContentBorderBottomStyle: 'dashed',
  popContentBorderBottomColor: '#ffffff'
  // popContentBorderBottomWidth: 1,
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
    }
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
    }
  }
}
export const DEFAULT_TITLE_STYLE = {
  show: true,
  fontSize: '18',
  color: '#303133',
  hPosition: 'center',
  vPosition: 'top',
  isItalic: false,
  isBolder: false,
  fontFamily: ''
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
  },
  itemGap: 10,
  marginTop: 5,
  marginButtom: 5
}
export const DEFAULT_XAXIS_STYLE = {
  show: true,
  position: 'bottom',
  name: '',
  nameLocation: 'end',
  nameGap: 20,
  paddingLeft: 0,
  paddingRight: 0,
  paddingTop: 0,
  paddingBottom: 0,
  nameTextStyle: {
    color: '#333333',
    fontSize: 12,
    lineHeight: 40
  },
  axisLabel: {
    show: true,
    color: '#333333',
    fontSize: '12',
    rotate: 0,
    formatter: '{value}'
  },
  splitLine: {
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
export const DEFAULT_XAXIS_STYLE_HC = {
  show: true,
  position: 'bottom',
  name: '',
  align: 'middle',
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
      type: 'solid'
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
export const DEFAULT_YAXIS_STYLE = {
  show: true,
  position: 'left',
  name: '',
  nameLocation: 'end',
  nameGap: 20,
  nameTop: 0,
  nameLeft: 0,
  paddingLeft: 0,
  paddingRight: 0,
  paddingTop: 0,
  paddingBottom: 0,
  nameTextStyle: {
    color: '#333333',
    fontSize: 12,
    lineHeight: 20,
    fontFamily: '',
  },
  axisLabel: {
    show: true,
    color: '#333333',
    fontSize: '12',
    rotate: 0,
    formatter: '{value}',
    fontFamily: ''
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
export const DEFAULT_YAXIS_STYLE_HC = {
  show: true,
  position: 'left',
  name: '',
  align: 'middle',
  nameTextStyle: {
    color: '#333333',
    fontSize: 12,
    fontFamily: ''
  },
  axisLabel: {
    show: true,
    color: '#333333',
    fontSize: '12',
    rotate: 0,
    formatter: '{value}',
    fontFamily: ''
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
export const DEFAULT_BACKGROUND_COLOR = {
  color: '#ffffff',
  alpha: 0,
  borderRadius: 0
}

export const DEFAULT_ZAXIS_STYLE = {
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

export const THERMODYNAMIC_DIAGRAM = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect',
    data: []
  },
  tooltip: {
    position: 'top'
  },
  grid: {
    height: '50%',
    top: '10%'
  },
  xAxis: {
    type: 'category',
    data: [],
    splitArea: {
      show: true
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    splitArea: {
      show: true
    }
  },
  visualMap: {
    min: 0,
    max: 50,
    calculable: true,
    orient: 'horizontal',
    left: 'center',
    bottom: '15%'
  },
  series: {
    name: '',
    type: 'heatmap',
    data: [],
    label: {
      show: true
    },
    emphasis: {
      itemStyle: {
        shadowBlur: 10,
        shadowColor: 'rgba(0, 0, 0, 0.5)'
      }
    }
  }
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
  sliderRange: [0, 10]
}
export const DEFAULT_THRESHOLD = {
  gaugeThreshold: ''
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
      xAxisIndex: [0],
      start: 0,
      end: 100
    },
    {
      type: 'inside',
      yAxisIndex: [0],
      start: 0,
      end: 100
    }
  ]
}

export const BASE_BAR_PART = {
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
      xAxisIndex: [0],
      start: 0,
      end: 100
    },
    {
      type: 'inside',
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
      xAxisIndex: [0],
      start: 0,
      end: 100
    },
    {
      type: 'inside',
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
      xAxisIndex: [0],
      start: 0,
      end: 100
    },
    {
      type: 'inside',
      yAxisIndex: [0],
      start: 0,
      end: 100
    }
  ]
}

export const BASE_PICTORIAL_BAR = {
  animation: false, // 去除加载时缓动动画
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
    data: [],
    textStyle: {
      color: '#ccc'
    }
  },
  xAxis: {
    data: [],
    type: 'category',
    axisLabel: {
      show: true,
      margin: 16,
      color: '#333333',
      fontSize: '12',
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'value'
  },
  series: []
  // dataZoom: [
  //   {
  //     type: 'slider',
  //     show: false,
  //     xAxisIndex: [0],
  //     start: 0,
  //     end: 100
  //   },
  //   {
  //     type: 'slider',
  //     show: false,
  //     yAxisIndex: [0],
  //     left: '93%',
  //     start: 0,
  //     end: 100
  //   },
  //   {
  //     type: 'inside',
  //     xAxisIndex: [0],
  //     start: 0,
  //     end: 100
  //   },
  //   {
  //     type: 'inside',
  //     yAxisIndex: [0],
  //     start: 0,
  //     end: 100
  //   }
  // ]
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
      // label: {
      //   show: true,
      //   position: 'inside'
      // },
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
  xaxis: '[]',
  yaxis: '[]',
  zaxis: '[]',
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
    yAxisExt: DEFAULT_YAXIS_EXT_STYLE,
    zaxis: DEFAULT_ZAXIS_STYLE,
    background: DEFAULT_BACKGROUND_COLOR
  }),
  customFilter: '[]'
}

export const BASE_CHART = {
  xaxis: [],
  yaxis: [],
  zaxis: [],
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
    yAxisExt: DEFAULT_YAXIS_EXT_STYLE,
    zaxis: DEFAULT_ZAXIS_STYLE,
    background: DEFAULT_BACKGROUND_COLOR
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
    right: 0
  },
  //   legend: {},
  series: [
    {
      name: '',
      type: 'map',
      map: 'MAP',
      roam: true,
      //   label: {
      //     show: true
      //   },
      data: []
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
      xAxisIndex: [0],
      start: 0,
      end: 100
    },
    {
      type: 'inside',
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
      itemStyle: {
        gapWidth: 2
      },
      breadcrumb: {
        show: false
      },
      // radius: ['0%', '60%'],
      // avoidLabelOverlap: false,
      // emphasis: {
      //   itemStyle: {
      //     shadowBlur: 10,
      //     shadowOffsetX: 0,
      //     shadowColor: 'rgba(0, 0, 0, 0.5)'
      //   }
      // },
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
      xAxisIndex: [0],
      start: 0,
      end: 100
    },
    {
      type: 'inside',
      yAxisIndex: [0],
      start: 0,
      end: 100
    }
  ]
}

export const BASE_GRAPH = {
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
  animationDurationUpdate: function(idx) {
    // 越往后的数据时长越大
    return idx * 100
  },
  animationEasingUpdate: 'bounceIn',
  legend: {
    show: true,
    type: 'scroll',
    itemWidth: 10,
    itemHeight: 10,
    icon: 'rect',
    data: []
  },
  xAxis: {
    show: false,
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      type: 'graph',
      layout: 'force',
      // center: [110],
      force: {
        repulsion: 100,
        edgeLength: 10,
        gravity: 0.1
      },
      scaleLimit: {
        max: null,
        min: null
      },
      roam: true,
      label: {
        normal: {
          show: true,
          fontSize: 12
        }
      },
      data: []
    }
  ],
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
      xAxisIndex: [0],
      start: 0,
      end: 100
    },
    {
      type: 'inside',
      yAxisIndex: [0],
      start: 0,
      end: 100
    }
  ]
}

export const BASE_LIQUID = {
  title: {
    text: '',
    textStyle: {
      fontWeight: 'normal'
    }
  },
  grid: {
    containLabel: true
  },
  series: [
    {
      type: 'liquidFill',
      shape:'circle',
      label: {
        show: false,
        textStyle: {
          fontSize: 14,
          color: '#909399',
        }
      },
      outline: {
        show: false,
        itemStyle: {},
        borderDistance:8
      },
      waveLength: 100,
      waveAnimation: true,
      radius: '50%',
      data: [],
    }
  ]
}

export const BASE_WORD_CLOUD = {
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
  series: [{
    // maskImage: '', // 词云轮廓图，白色区域将被排除在绘图文本之外,形状选项将继续应用为云的形状
    type: 'wordCloud',
    sizeRange: [8, 32], // 词云的文字字号范围
    rotationRange: [0, 0], // 词云中文字的角度,词云中的文字会随机的在 rotationRange 范围内旋转角度
    rotationStep: 45, // 渲染的梯度就是 rotationStep ，这个值越小，词云里出现的角度种类就越多。以上面参数为例，可能旋转的角度就是 -90 -45 0 45 90 。
    gridSize: 8, // 词云中每个词的间距
    drawOutOfBound: true, // 是否允许词云在边界外渲染，直接使用默认参数 false 就可以，否则容易造成词重叠
    shape: 'pentagon', // 词云的形状，默认是 circle(圆形)，可选的参数有cardioid（心形） 、 diamond（菱形 正方形） 、 triangle-forward 、 triangle（三角形）、 star（星形）、pentagon （五边形）；
    width: '100%', // 词云的宽高，默认是 75% 80%
    height: '90%',
    textStyle: { // 词云中文字的样式， normal 是初始的样式， emphasis 是鼠标移到文字上的样式。
      normal: {
        color: function() {
          // Random color
          return 'rgb(' + [
            Math.round(Math.random() * 160),
            Math.round(Math.random() * 160),
            Math.round(Math.random() * 160)
          ].join(',') + ')'
        },
        fontFamily: 'sans-serif',
        fontWeight: 'normal'
      },
      emphasis: {
        shadowBlur: 10,
        shadowColor: '#333333'
      }
    },
    itemStyle: {
      shadowBlur: 10,
      shadowColor: '#333333'
    },
    data: []
  }],
  textStyle: {
    fontFamily: 'sans-serif'
  }
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
