import { DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'

export const CHART_MIX_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'dual-basic-style-selector',
  'x-axis-selector',
  'dual-y-axis-selector',
  'title-selector',
  'legend-selector',
  'label-selector',
  'tooltip-selector',
  'assist-line',
  'function-cfg',
  'jump-set',
  'linkage'
]
export const CHART_MIX_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'label-selector': ['fontSize', 'color'],
  'tooltip-selector': ['fontSize', 'color', 'backgroundColor', 'show'],
  'dual-basic-style-selector': [
    'colors',
    'alpha',
    'gradient',
    'lineWidth',
    'lineSymbol',
    'lineSymbolSize',
    'lineSmooth',
    'radiusColumnBar',
    'subSeriesColor'
  ],
  'x-axis-selector': [
    'name',
    'color',
    'fontSize',
    'position',
    'axisLabel',
    'axisLine',
    'splitLine'
  ],
  'y-axis-selector': [
    'name',
    'color',
    'fontSize',
    'axisLabel',
    'axisLine',
    'splitLine',
    'axisValue',
    'axisLabelFormatter'
  ],
  'title-selector': [
    'title',
    'fontSize',
    'color',
    'hPosition',
    'isItalic',
    'isBolder',
    'remarkShow',
    'fontFamily',
    'letterSpace',
    'fontShadow'
  ],
  'legend-selector': ['icon', 'orient', 'fontSize', 'color', 'hPosition', 'vPosition'],
  'function-cfg': ['emptyDataStrategy']
}

export const CHART_MIX_AXIS_TYPE: AxisType[] = [
  'xAxis',
  'yAxis',
  'drill',
  'filter',
  'extLabel',
  'extTooltip'
]

export const CHART_MIX_DEFAULT_BASIC_STYLE = {
  ...DEFAULT_BASIC_STYLE,
  subAlpha: 100,
  subColorScheme: 'fast',
  subSeriesColor: [],
  subColors: [
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
}

export interface MixChartBasicStyle extends ChartBasicStyle {
  subAlpha: number
  subColors: string[]
  subSeriesColor: {
    /**
     * 序列识别id，多指标就是轴id，分组或者堆叠就是类别值
     */
    id: string
    /**
     * 显示名称
     */
    name: string
    /**
     * 序列颜色
     */
    color: string
  }[]
}
