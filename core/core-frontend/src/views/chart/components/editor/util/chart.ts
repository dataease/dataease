import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import { formatterItem } from '@/views/chart/components/js/formatter'
const { t } = useI18n()

export const DEFAULT_COLOR_CASE: DeepPartial<ChartAttr> = {
  basicStyle: {
    colorScheme: 'default',
    colors: [
      '#1E90FF',
      '#90EE90',
      '#00CED1',
      '#E2BD84',
      '#7A90E0',
      '#3BA272',
      '#2BE7FF',
      '#0A8ADA',
      '#FFD700'
    ],
    alpha: 100,
    gradient: false,
    mapStyle: 'normal',
    areaBaseColor: '#FFFFFF',
    areaBorderColor: '#303133',
    gaugeStyle: 'default',
    tableBorderColor: '#E6E7E4',
    tableScrollBarColor: 'rgba(0, 0, 0, 0.15)',
    zoomButtonColor: '#aaa',
    zoomBackground: '#fff'
  },
  misc: {
    flowMapConfig: {
      lineConfig: {
        mapLineAnimate: true,
        mapLineGradient: false,
        mapLineSourceColor: '#146C94',
        mapLineTargetColor: '#576CBC'
      }
    },
    nameFontColor: '#000000',
    valueFontColor: '#5470c6'
  },
  tableHeader: {
    tableHeaderBgColor: '#6D9A49',
    tableHeaderFontColor: '#000000'
  },
  tableCell: {
    tableItemBgColor: '#FFFFFF',
    tableFontColor: '#000000',
    tableItemSubBgColor: '#EEEEEE'
  }
}

export const DEFAULT_COLOR_CASE_LIGHT: DeepPartial<ChartAttr> = {
  basicStyle: {
    colorScheme: 'default',
    colors: [
      '#1E90FF',
      '#90EE90',
      '#00CED1',
      '#E2BD84',
      '#7A90E0',
      '#3BA272',
      '#2BE7FF',
      '#0A8ADA',
      '#FFD700'
    ],
    alpha: 100,
    gradient: false,
    mapStyle: 'normal',
    areaBaseColor: '#FFFFFF',
    areaBorderColor: '#303133',
    gaugeStyle: 'default',
    tableBorderColor: '#E6E7E4',
    tableScrollBarColor: 'rgba(0, 0, 0, 0.15)',
    zoomButtonColor: '#aaa',
    zoomBackground: '#fff'
  },
  misc: {
    flowMapConfig: {
      lineConfig: {
        mapLineAnimate: true,
        mapLineGradient: false,
        mapLineSourceColor: '#146C94',
        mapLineTargetColor: '#576CBC'
      }
    },
    nameFontColor: '#000000',
    valueFontColor: '#5470c6'
  },
  tableHeader: {
    tableHeaderBgColor: '#1E90FF',
    tableHeaderFontColor: '#000000'
  },
  tableCell: {
    tableItemBgColor: '#FFFFFF',
    tableFontColor: '#000000',
    tableItemSubBgColor: '#EEEEEE'
  }
}

export const DEFAULT_COLOR_CASE_DARK: DeepPartial<ChartAttr> = {
  basicStyle: {
    colorScheme: 'default',
    colors: [
      '#1E90FF',
      '#90EE90',
      '#00CED1',
      '#E2BD84',
      '#7A90E0',
      '#3BA272',
      '#2BE7FF',
      '#0A8ADA',
      '#FFD700'
    ],
    alpha: 100,
    gradient: false,
    mapStyle: 'darkblue',
    areaBaseColor: '5470C6',
    areaBorderColor: '#EBEEF5',
    gaugeStyle: 'default',
    tableBorderColor: '#CCCCCC',
    tableScrollBarColor: 'rgba(255, 255, 255, 0.5)',
    zoomButtonColor: '#fff',
    zoomBackground: '#000'
  },
  misc: {
    flowMapConfig: {
      lineConfig: {
        mapLineGradient: false,
        mapLineSourceColor: '#146C94',
        mapLineTargetColor: '#576CBC'
      }
    },
    nameFontColor: '#ffffff',
    valueFontColor: '#5470c6'
  },
  tableHeader: {
    tableHeaderBgColor: '#1E90FF',
    tableHeaderFontColor: '#FFFFFF'
  },
  tableCell: {
    tableItemBgColor: '#131E42',
    tableFontColor: '#ffffff',
    tableItemSubBgColor: '#EEEEEE'
  }
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

export const SENIOR_STYLE_SETTING_LIGHT = {
  linkageIconColor: '#a6a6a6',
  drillLayerColor: '#a6a6a6',
  pagerColor: '#a6a6a6'
}

export const SENIOR_STYLE_SETTING_DARK = {
  linkageIconColor: '#ffffff',
  drillLayerColor: '#ffffff',
  pagerColor: '#ffffff'
}

export const FILTER_COMMON_STYLE_BASE = {
  layout: 'horizontal',
  titleLayout: 'left'
}

export const FILTER_COMMON_STYLE_LIGHT = {
  ...FILTER_COMMON_STYLE_BASE,
  labelColor: '#1f2329',
  titleColor: '#1f2329',
  color: '#1f2329',
  borderColor: '#bbbfc4',
  text: '#1f2329',
  bgColor: '#FFFFFF'
}

export const FILTER_COMMON_STYLE_DARK = {
  ...FILTER_COMMON_STYLE_BASE,
  labelColor: '#ffffff',
  titleColor: '#ffffff',
  color: '#FFFFFF',
  borderColor: '#484747',
  text: '#AFAFAF',
  bgColor: '#131C42'
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

export const DEFAULT_MISC: ChartMiscAttr = {
  pieInnerRadius: 0,
  pieOuterRadius: 80,
  radarShape: 'polygon',
  radarSize: 80,
  gaugeMinType: 'fix',
  gaugeMinField: {
    id: '',
    summary: ''
  },
  gaugeMin: 0,
  gaugeMaxType: 'fix',
  gaugeMaxField: {
    id: '',
    summary: ''
  },
  gaugeMax: 1,
  gaugeStartAngle: 225,
  gaugeEndAngle: -45,
  nameFontSize: 18,
  valueFontSize: 18,
  nameValueSpace: 10,
  valueFontColor: '#5470c6',
  valueFontFamily: 'Microsoft YaHei',
  valueFontIsBolder: false,
  valueFontIsItalic: false,
  valueLetterSpace: 0,
  valueFontShadow: false,
  showName: true,
  nameFontColor: '#000000',
  nameFontFamily: 'Microsoft YaHei',
  nameFontIsBolder: false,
  nameFontIsItalic: false,
  nameLetterSpace: '0',
  nameFontShadow: false,
  treemapWidth: 80,
  treemapHeight: 80,
  liquidMax: 1,
  liquidMaxType: 'fix',
  liquidMaxField: {
    id: '',
    summary: ''
  },
  liquidSize: 80,
  liquidShape: 'circle',
  hPosition: 'center',
  vPosition: 'center',
  mapPitch: 0,
  wordSizeRange: [8, 32],
  wordSpacing: 6,
  mapAutoLegend: true,
  mapLegendMax: 0,
  mapLegendMin: 0,
  mapLegendNumber: 9,
  flowMapConfig: {
    lineConfig: {
      mapLineAnimate: true,
      mapLineType: 'arc',
      mapLineWidth: 1,
      mapLineAnimateDuration: 3,
      mapLineGradient: false,
      mapLineSourceColor: '#146C94',
      mapLineTargetColor: '#576CBC',
      alpha: 100
    },
    pointConfig: {
      text: {
        color: '#146C94',
        fontSize: 10
      },
      point: {
        color: '#146C94',
        size: 4,
        animate: false,
        speed: 0.01
      }
    }
  }
}

export const DEFAULT_MARK = {
  fieldId: '',
  conditions: []
}
export const DEFAULT_LABEL: ChartLabelAttr = {
  show: false,
  childrenShow: true,
  position: 'top',
  color: '#909399',
  fontSize: 12,
  formatter: '',
  labelLine: {
    show: true
  },
  labelFormatter: formatterItem,
  reserveDecimalCount: 2,
  labelShadow: false,
  labelBgColor: '',
  labelShadowColor: '',
  quotaLabelFormatter: formatterItem,
  showDimension: true,
  showQuota: false,
  showProportion: true,
  seriesLabelFormatter: []
}
export const DEFAULT_TOOLTIP: ChartTooltipAttr = {
  show: true,
  trigger: 'item',
  confine: true,
  fontSize: 12,
  color: '#909399',
  tooltipFormatter: formatterItem,
  backgroundColor: '#ffffff',
  seriesTooltipFormatter: []
}
export const DEFAULT_TABLE_TOTAL: ChartTableTotalAttr = {
  row: {
    showGrandTotals: true,
    showSubTotals: true,
    reverseLayout: false,
    reverseSubLayout: false,
    label: '总计',
    subLabel: '小计',
    subTotalsDimensions: [],
    calcTotals: {
      aggregation: 'SUM',
      cfg: []
    },
    calcSubTotals: {
      aggregation: 'SUM',
      cfg: []
    },
    totalSort: 'none',
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
      aggregation: 'SUM',
      cfg: []
    },
    calcSubTotals: {
      aggregation: 'SUM',
      cfg: []
    },
    totalSort: 'none', // asc,desc
    totalSortField: ''
  }
}
export const DEFAULT_TABLE_HEADER: ChartTableHeaderAttr = {
  indexLabel: '序号',
  showIndex: false,
  tableHeaderAlign: 'left',
  tableHeaderBgColor: '#6D9A49',
  tableHeaderFontColor: '#000000',
  tableTitleFontSize: 12,
  tableTitleHeight: 36,
  tableHeaderSort: false,
  showColTooltip: false,
  showRowTooltip: false,
  showTableHeader: true,
  showHorizonBorder: true,
  showVerticalBorder: true,
  isItalic: false,
  isBolder: true
}
export const DEFAULT_TABLE_CELL: ChartTableCellAttr = {
  tableFontColor: '#000000',
  tableItemAlign: 'right',
  tableItemBgColor: '#FFFFFF',
  tableItemFontSize: 12,
  tableItemHeight: 36,
  enableTableCrossBG: false,
  tableItemSubBgColor: '#EEEEEE',
  showTooltip: false,
  showHorizonBorder: true,
  showVerticalBorder: true,
  isItalic: false,
  isBolder: false
}
export const DEFAULT_TITLE_STYLE: ChartTextStyle = {
  show: true,
  fontSize: 16,
  color: '#ffffff',
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

export const DEFAULT_INDICATOR_STYLE: ChartIndicatorStyle = {
  show: true,
  fontSize: 20,
  color: '#5470C6ff',
  hPosition: 'center',
  vPosition: 'center',
  isItalic: false,
  isBolder: true,
  fontFamily: 'Microsoft YaHei',
  letterSpace: 0,
  fontShadow: false,
  backgroundColor: '',

  suffixEnable: true,
  suffix: '',
  suffixFontSize: 14,
  suffixColor: '#5470C6ff',
  suffixIsItalic: false,
  suffixIsBolder: true,
  suffixFontFamily: 'Microsoft YaHei',
  suffixLetterSpace: 0,
  suffixFontShadow: false
}
export const DEFAULT_INDICATOR_NAME_STYLE: ChartIndicatorNameStyle = {
  show: true,
  fontSize: 18,
  color: '#ffffffff',
  isItalic: false,
  isBolder: true,
  fontFamily: 'Microsoft YaHei',
  letterSpace: 0,
  fontShadow: false,
  nameValueSpacing: 0
}

export const DEFAULT_TITLE_STYLE_BASE: ChartTextStyle = {
  show: true,
  fontSize: 16,
  hPosition: 'left',
  vPosition: 'top',
  isItalic: false,
  isBolder: true,
  remarkShow: false,
  remark: '',
  fontFamily: 'Microsoft YaHei',
  letterSpace: '0',
  fontShadow: false,
  color: '',
  remarkBackgroundColor: ''
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

export const DEFAULT_LEGEND_STYLE: ChartLegendStyle = {
  show: true,
  hPosition: 'center',
  vPosition: 'bottom',
  orient: 'horizontal',
  icon: 'circle',
  color: '#333333',
  fontSize: 12
}

export const DEFAULT_LEGEND_STYLE_BASE: ChartLegendStyle = {
  show: true,
  hPosition: 'center',
  vPosition: 'bottom',
  orient: 'horizontal',
  icon: 'circle',
  color: '#333333',
  fontSize: 12
}

export const DEFAULT_LEGEND_STYLE_LIGHT: ChartLegendStyle = {
  ...DEFAULT_LEGEND_STYLE_BASE,
  color: '#333333',
  fontSize: 12
}

export const DEFAULT_LEGEND_STYLE_DARK: ChartLegendStyle = {
  ...DEFAULT_LEGEND_STYLE_BASE,
  color: '#ffffff',
  fontSize: 12
}

export const DEFAULT_MARGIN_STYLE = {
  marginModel: 'auto',
  marginTop: 40,
  marginBottom: 44,
  marginLeft: 15,
  marginRight: 10
}

export const DEFAULT_XAXIS_STYLE: ChartAxisStyle = {
  show: true,
  position: 'bottom',
  name: '',
  color: '#333333',
  fontSize: 12,
  axisLabel: {
    show: true,
    color: '#333333',
    fontSize: 12,
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
    min: 10,
    max: 100,
    split: 10,
    splitCount: 10
  },
  axisLabelFormatter: {
    type: 'auto',
    unit: 1,
    suffix: '',
    decimalCount: 2,
    thousandSeparator: true
  }
}
export const DEFAULT_YAXIS_STYLE: ChartAxisStyle = {
  show: true,
  position: 'left',
  name: '',
  color: '#333333',
  fontSize: 12,
  axisLabel: {
    show: true,
    color: '#333333',
    fontSize: 12,
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
    min: 10,
    max: 100,
    split: 10,
    splitCount: 10
  },
  axisLabelFormatter: {
    type: 'auto',
    unit: 1,
    suffix: '',
    decimalCount: 2,
    thousandSeparator: true
  }
}
export const DEFAULT_YAXIS_EXT_STYLE: ChartAxisStyle = {
  show: true,
  position: 'right',
  name: '',
  color: '#333333',
  fontSize: 12,
  axisLabel: {
    show: true,
    color: '#333333',
    fontSize: 12,
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
    show: false,
    lineStyle: {
      color: '#cccccc',
      width: 1,
      style: 'solid'
    }
  },
  axisValue: {
    auto: true,
    min: 10,
    max: 100,
    split: 10,
    splitCount: 10
  },
  axisLabelFormatter: {
    type: 'auto',
    unit: 1,
    suffix: '',
    decimalCount: 2,
    thousandSeparator: true
  }
}
export const DEFAULT_BACKGROUND_COLOR = {
  color: '#ffffff',
  alpha: 0,
  borderRadius: 0
}
export const DEFAULT_MISC_STYLE: ChartMiscStyle = {
  showName: false,
  color: '#999',
  fontSize: 12,
  axisColor: '#999',
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
export const DEFAULT_FUNCTION_CFG: ChartFunctionCfg = {
  sliderShow: false,
  sliderRange: [0, 10],
  sliderBg: '#FFFFFF',
  sliderFillBg: '#BCD6F1',
  sliderTextColor: '#999999',
  emptyDataStrategy: 'breakLine',
  emptyDataFieldCtrl: []
}
export const DEFAULT_ASSIST_LINE_CFG: ChartAssistLineCfg = {
  enable: false,
  assistLine: []
}
export const DEFAULT_THRESHOLD: ChartThreshold = {
  enable: false,
  gaugeThreshold: '',
  liquidThreshold: '',
  labelThreshold: [],
  tableThreshold: [],
  textLabelThreshold: []
}
export const DEFAULT_SCROLL: ScrollCfg = {
  open: false,
  row: 1,
  interval: 2000,
  step: 50
}

export const DEFAULT_BUBBLE_ANIMATE: BubbleCfg = {
  enable: false,
  speed: 1,
  rings: 1,
  type: 'wave'
}

export const DEFAULT_QUADRANT_STYLE: QuadrantAttr = {
  lineStyle: {
    stroke: '#aaa',
    lineWidth: 1,
    opacity: 0.5
  },
  regionStyle: [
    {
      fill: '#fdfcfc',
      fillOpacity: 0
    },
    {
      fill: '#fafdfa',
      fillOpacity: 0
    },
    {
      fill: '#fdfcfc',
      fillOpacity: 0
    },
    {
      fill: '#fafdfa',
      fillOpacity: 0
    }
  ],
  labels: [
    {
      content: '',
      style: {
        fill: '#000000',
        fillOpacity: 0.5,
        fontSize: 14
      }
    },
    {
      content: '',
      style: {
        fill: '#000000',
        fillOpacity: 0.5,
        fontSize: 14
      }
    },
    {
      content: '',
      style: {
        fill: '#000000',
        fillOpacity: 0.5,
        fontSize: 14
      }
    },
    {
      content: '',
      style: {
        fill: '#000000',
        fillOpacity: 0.5,
        fontSize: 14
      }
    }
  ]
}

export const COLOR_PANEL = [
  '#FF4500',
  '#FF8C00',
  '#FFD700',
  '#71AE46',
  '#00CED1',
  '#1E90FF',
  '#C71585',
  '#999999',
  '#000000',
  '#FFFFFF'
]

export const COLOR_CASES = [
  {
    name: t('chart.color_default'),
    value: 'default',
    colors: [
      '#1E90FF',
      '#90EE90',
      '#00CED1',
      '#E2BD84',
      '#7A90E0',
      '#3BA272',
      '#2BE7FF',
      '#0A8ADA',
      '#FFD700'
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

export const CHART_FONT_FAMILY_MAP = {
  'Microsoft YaHei': 'Microsoft YaHei',
  SimSun: 'SimSun, "Songti SC", STSong',
  SimHei: 'SimHei, Helvetica',
  KaiTi: 'KaiTi, "Kaiti SC", STKaiti'
}

export const CHART_FONT_LETTER_SPACE = [
  { name: '0px', value: 0 },
  { name: '1px', value: 1 },
  { name: '2px', value: 2 },
  { name: '3px', value: 3 },
  { name: '4px', value: 4 },
  { name: '5px', value: 5 },
  { name: '6px', value: 6 },
  { name: '7px', value: 7 },
  { name: '8px', value: 8 },
  { name: '9px', value: 9 },
  { name: '10px', value: 10 }
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

export const CHART_TYPE_CONFIGS = [
  {
    category: 'quota',
    title: t('chart.chart_type_quota'),
    display: 'show',
    details: [
      {
        render: 'antv',
        category: 'quota',
        value: 'gauge',
        title: t('chart.chart_gauge'),
        icon: 'gauge'
      },
      {
        render: 'antv',
        category: 'quota',
        value: 'liquid',
        title: t('chart.chart_liquid'),
        icon: 'liquid'
      },
      {
        render: 'custom',
        category: 'quota',
        value: 'indicator',
        title: t('chart.chart_indicator'),
        icon: 'indicator'
      }
    ]
  },
  {
    category: 'table',
    title: t('chart.chart_type_table'),
    display: 'show',
    details: [
      {
        render: 'antv',
        category: 'table',
        value: 'table-info',
        title: t('chart.chart_table_info'),
        icon: 'table-info'
      },
      {
        render: 'antv',
        category: 'table',
        value: 'table-normal',
        title: t('chart.chart_table_normal'),
        icon: 'table-normal'
      },
      {
        render: 'antv',
        category: 'table',
        value: 'table-pivot',
        title: t('chart.chart_table_pivot'),
        icon: 'table-pivot'
      }
    ]
  },
  {
    category: 'trend',
    title: t('chart.chart_type_trend'),
    display: 'show',
    details: [
      {
        render: 'antv',
        category: 'trend',
        value: 'line',
        title: t('chart.chart_line'),
        icon: 'line'
      },
      {
        render: 'antv',
        category: 'trend',
        value: 'area',
        title: t('chart.chart_area'),
        icon: 'area'
      },
      {
        render: 'antv',
        category: 'trend',
        value: 'area-stack',
        title: t('chart.chart_area_stack'),
        icon: 'area-stack'
      }
    ]
  },
  {
    category: 'compare',
    title: t('chart.chart_type_compare'),
    display: 'show',
    details: [
      {
        render: 'antv',
        category: 'compare',
        value: 'bar',
        title: t('chart.chart_bar'),
        icon: 'bar'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'bar-stack',
        title: t('chart.chart_bar_stack'),
        icon: 'bar-stack'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'percentage-bar-stack',
        title: t('chart.chart_percentage_bar_stack'),
        icon: 'percentage-bar-stack'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'bar-group',
        title: t('chart.chart_bar_group'),
        icon: 'bar-group'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'bar-group-stack',
        title: t('chart.chart_bar_group_stack'),
        icon: 'bar-group-stack'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'waterfall',
        title: t('chart.chart_waterfall'),
        icon: 'waterfall'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'bar-horizontal',
        title: t('chart.chart_bar_horizontal'),
        icon: 'bar-horizontal'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'bar-stack-horizontal',
        title: t('chart.chart_bar_stack_horizontal'),
        icon: 'bar-stack-horizontal'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'percentage-bar-stack-horizontal',
        title: t('chart.chart_percentage_bar_stack_horizontal'),
        icon: 'percentage-bar-stack-horizontal'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'bar-range',
        title: t('chart.chart_bar_range'),
        icon: 'bar-range'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'bidirectional-bar',
        title: t('chart.chart_bidirectional_bar'),
        icon: 'bidirectional-bar'
      },
      {
        render: 'antv',
        category: 'compare',
        value: 'progress-bar',
        title: t('chart.chart_progress_bar'),
        icon: 'progress-bar'
      },
      {
        render: 'antv',
        category: 'trend',
        value: 'stock-line',
        title: 'K 线图',
        icon: 'stock-line'
      }
    ]
  },
  {
    category: 'distribute',
    title: t('chart.chart_type_distribute'),
    display: 'show',
    details: [
      {
        render: 'antv',
        category: 'distribute',
        value: 'pie',
        title: t('chart.chart_pie'),
        icon: 'pie'
      },
      {
        render: 'antv',
        category: 'distribute',
        value: 'pie-donut',
        title: t('chart.chart_pie_donut'),
        icon: 'pie-donut'
      },
      {
        render: 'antv',
        category: 'distribute',
        value: 'pie-rose',
        title: t('chart.chart_pie_rose'),
        icon: 'pie-rose'
      },
      {
        render: 'antv',
        category: 'distribute',
        value: 'pie-donut-rose',
        title: t('chart.chart_pie_donut_rose'),
        icon: 'pie-donut-rose'
      },
      {
        render: 'antv',
        category: 'chart.chart_type_distribute',
        value: 'radar',
        title: t('chart.chart_radar'),
        icon: 'radar'
      },
      {
        render: 'antv',
        category: 'distribute',
        value: 'treemap',
        title: t('chart.chart_treemap'),
        icon: 'treemap'
      },
      {
        render: 'antv',
        category: 'distribute',
        value: 'word-cloud',
        title: t('chart.chart_word_cloud'),
        icon: 'word-cloud'
      }
    ]
  },
  {
    category: 'map',
    title: t('chart.chart_type_space'),
    display: 'show',
    details: [
      {
        render: 'antv',
        category: 'map',
        value: 'map',
        title: t('chart.chart_map'),
        icon: 'map'
      },
      {
        render: 'antv',
        category: 'map',
        value: 'bubble-map',
        title: t('chart.chart_bubble_map'),
        icon: 'bubble-map'
      },
      {
        render: 'antv',
        category: 'map',
        value: 'flow-map',
        title: t('chart.chart_flow_map'),
        icon: 'flow-map'
      },
      {
        render: 'antv',
        category: 'map',
        value: 'heat-map',
        title: t('chart.chart_heat_map'),
        icon: 'heat-map'
      },
      {
        render: 'antv',
        category: 'map',
        value: 'symbolic-map',
        title: '符号地图',
        icon: 'symbolic-map'
      }
    ]
  },
  {
    category: 'relation',
    title: t('chart.chart_type_relation'),
    display: 'show',
    details: [
      {
        render: 'antv',
        category: 'distribute',
        value: 'scatter',
        title: t('chart.chart_scatter'),
        icon: 'scatter'
      },
      {
        render: 'antv',
        category: 'distribute',
        value: 'quadrant',
        title: t('chart.chart_quadrant'),
        icon: 'quadrant'
      },
      {
        render: 'antv',
        category: 'distribute',
        value: 'funnel',
        title: t('chart.chart_funnel'),
        icon: 'funnel'
      },
      {
        render: 'antv',
        category: 'relation',
        value: 'sankey',
        title: t('chart.chart_sankey'),
        icon: 'sankey'
      }
    ]
  },
  {
    category: 'dual_axes',
    title: t('chart.chart_type_dual_axes'),
    display: 'show',
    details: [
      {
        render: 'antv',
        category: 'dual_axes',
        value: 'chart-mix',
        title: t('chart.chart_mix'),
        icon: 'chart-mix'
      },
      {
        render: 'antv',
        category: 'dual_axes',
        value: 'chart-mix-group',
        title: t('chart.chart_mix_group_column'),
        icon: 'chart-mix-group'
      },
      {
        render: 'antv',
        category: 'dual_axes',
        value: 'chart-mix-stack',
        title: t('chart.chart_mix_stack_column'),
        icon: 'chart-mix-stack'
      }
    ]
  },
  {
    category: 'other',
    title: '富文本',
    display: 'hidden',
    details: [
      {
        render: 'custom',
        category: 'quota',
        value: 'rich-text',
        title: '富文本',
        icon: 'rich-text'
      }
    ]
  }
]

export const DEFAULT_BASIC_STYLE: ChartBasicStyle = {
  alpha: 100,
  tableBorderColor: '#CCCCCC',
  tableScrollBarColor: 'rgba(0, 0, 0, 0.15)',
  tableColumnMode: 'adapt',
  tableColumnWidth: 100,
  tableFieldWidth: [],
  tablePageMode: 'page',
  tablePageStyle: 'simple',
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
  radiusColumnBar: 'rightAngle',
  columnBarRightAngleRadius: 20,
  barWidth: 40,
  barGap: 0.4,
  lineType: 'solid',
  scatterSymbol: 'circle',
  scatterSymbolSize: 8,
  radarShape: 'polygon',
  mapStyle: 'normal',
  heatMapType: 'heatmap',
  heatMapIntensity: 2,
  heatMapRadius: 20,
  areaBorderColor: '#EBEEF5',
  areaBaseColor: '#ffffff',
  mapSymbolOpacity: 0.7,
  mapSymbolStrokeWidth: 2,
  mapSymbol: 'circle',
  mapSymbolSize: 6,
  radius: 80,
  innerRadius: 60,
  showZoom: true,
  zoomButtonColor: '#aaa',
  zoomBackground: '#fff',
  tableLayoutMode: 'grid',
  calcTopN: false,
  topN: 5,
  topNLabel: '其他',
  gaugeAxisLine: true,
  gaugePercentLabel: true,
  showSummary: false,
  summaryLabel: '总计',
  seriesColor: [],
  layout: 'horizontal'
}

export const BASE_VIEW_CONFIG = {
  id: '', // 图表id
  title: '图表',
  sceneId: 0, // 仪表板id
  tableId: '', // 数据集id
  type: 'bar',
  render: 'antv',
  resultCount: 1000,
  resultMode: 'custom',
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
  customFilter: {},
  customAttr: {
    basicStyle: DEFAULT_BASIC_STYLE,
    misc: DEFAULT_MISC,
    label: DEFAULT_LABEL,
    tooltip: DEFAULT_TOOLTIP,
    tableTotal: DEFAULT_TABLE_TOTAL,
    tableHeader: DEFAULT_TABLE_HEADER,
    tableCell: DEFAULT_TABLE_CELL,
    indicator: DEFAULT_INDICATOR_STYLE,
    indicatorName: DEFAULT_INDICATOR_NAME_STYLE,
    map: {
      id: '',
      level: 'world'
    }
  },
  customStyle: {
    text: DEFAULT_TITLE_STYLE,
    legend: DEFAULT_LEGEND_STYLE,
    xAxis: DEFAULT_XAXIS_STYLE,
    yAxis: DEFAULT_YAXIS_STYLE,
    yAxisExt: DEFAULT_YAXIS_EXT_STYLE,
    misc: DEFAULT_MISC_STYLE
  },
  senior: {
    functionCfg: DEFAULT_FUNCTION_CFG,
    assistLineCfg: DEFAULT_ASSIST_LINE_CFG,
    threshold: DEFAULT_THRESHOLD,
    scrollCfg: DEFAULT_SCROLL,
    areaMapping: {},
    bubbleCfg: DEFAULT_BUBBLE_ANIMATE
  },
  flowMapStartName: [],
  flowMapEndName: []
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
