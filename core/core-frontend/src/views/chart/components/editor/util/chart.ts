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
    tableScrollBarColor: 'rgba(0, 0, 0, 0.15)'
  },
  misc: {
    mapLineGradient: false,
    mapLineSourceColor: '#146C94',
    mapLineTargetColor: '#576CBC',
    nameFontColor: '#000000',
    valueFontColor: '#5470c6'
  },
  tableHeader: {
    tableHeaderBgColor: '#6D9A49',
    tableHeaderFontColor: '#000000'
  },
  tableCell: {
    tableItemBgColor: '#FFFFFF',
    tableFontColor: '#000000'
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
    tableScrollBarColor: 'rgba(0, 0, 0, 0.15)'
  },
  misc: {
    mapLineGradient: false,
    mapLineSourceColor: '#146C94',
    mapLineTargetColor: '#576CBC',
    nameFontColor: '#000000',
    valueFontColor: '#5470c6'
  },
  tableHeader: {
    tableHeaderBgColor: '#1E90FF',
    tableHeaderFontColor: '#000000'
  },
  tableCell: {
    tableItemBgColor: '#FFFFFF',
    tableFontColor: '#000000'
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
    tableScrollBarColor: 'rgba(255, 255, 255, 0.5)'
  },
  misc: {
    mapLineGradient: false,
    mapLineSourceColor: '#2F58CD',
    mapLineTargetColor: '#3795BD',
    nameFontColor: '#ffffff',
    valueFontColor: '#5470c6'
  },
  tableHeader: {
    tableHeaderBgColor: '#1E90FF',
    tableHeaderFontColor: '#FFFFFF'
  },
  tableCell: {
    tableItemBgColor: '#131E42',
    tableFontColor: '#ffffff'
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
  gaugeMax: 100,
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
  liquidMax: 100,
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
  mapLineType: 'arc',
  mapLineWidth: 1,
  mapLineAnimateDuration: 3,
  mapLineGradient: false,
  mapLineSourceColor: '#146C94',
  mapLineTargetColor: '#576CBC'
}
export const DEFAULT_SUSPENSION = {
  show: true
}

export const DEFAULT_MARK = {
  fieldId: '',
  conditions: []
}
export const DEFAULT_LABEL: ChartLabelAttr = {
  show: false,
  position: 'top',
  color: '#909399',
  fontSize: 10,
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
  tableTitleHeight: 36
}
export const DEFAULT_TABLE_CELL: ChartTableCellAttr = {
  tableFontColor: '#000000',
  tableItemAlign: 'right',
  tableItemBgColor: '#FFFFFF',
  tableItemFontSize: 12,
  tableItemHeight: 36
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
  fontSize: '20',
  color: '#5470C6',
  hPosition: 'center',
  vPosition: 'center',
  isItalic: false,
  isBolder: true,
  fontFamily: 'Microsoft YaHei',
  letterSpace: '0',
  fontShadow: false,

  suffixEnable: true,
  suffix: '',
  suffixFontSize: '14',
  suffixColor: '#5470C6',
  suffixIsItalic: false,
  suffixIsBolder: true,
  suffixFontFamily: 'Microsoft YaHei',
  suffixLetterSpace: '0',
  suffixFontShadow: false
}
export const DEFAULT_INDICATOR_NAME_STYLE: ChartIndicatorNameStyle = {
  show: true,
  fontSize: '18',
  color: '#ffffff',
  isItalic: false,
  isBolder: true,
  fontFamily: 'Microsoft YaHei',
  letterSpace: '0',
  fontShadow: false
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

export const COLOR_PANEL = [
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

export const CHART_CONT_FAMILY_MAP = {
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
        value: 'funnel',
        title: t('chart.chart_funnel'),
        icon: 'funnel'
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
  scatterSymbolSize: 8,
  radarShape: 'polygon',
  mapStyle: 'normal',
  areaBorderColor: '#EBEEF5',
  suspension: true,
  areaBaseColor: '#ffffff',
  mapSymbolOpacity: 0.7,
  mapSymbolStrokeWidth: 2,
  mapSymbol: 'circle',
  mapSymbolSize: 20,
  radius: 100,
  innerRadius: 60
}

export const BASE_VIEW_CONFIG = {
  id: '', // 视图id
  title: '图表',
  sceneId: 0, // 仪表板id
  tableId: '', // 数据集id
  type: 'bar',
  render: 'antv',
  resultCount: 1000,
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
