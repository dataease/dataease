export const CHART_MIX_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'border-style',
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
  'border-style': ['all'],
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
    'subSeriesColor',
    'seriesColor',
    'columnWidthRatio'
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
  'dual-y-axis-selector': [
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
