export const LINE_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'basic-style-selector',
  'x-axis-selector',
  'y-axis-selector',
  'title-selector',
  'legend-selector',
  'label-selector',
  'tooltip-selector'
]
export const LINE_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'label-selector': ['fontSize', 'color', 'position'],
  'tooltip-selector': ['fontSize', 'color', 'backgroundColor'],
  'basic-style-selector': [
    'colors',
    'alpha',
    'lineWidth',
    'lineSymbol',
    'lineSymbolSize',
    'lineSmooth'
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
    'position',
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
  'legend-selector': ['icon', 'orient', 'textStyle', 'hPosition', 'vPosition']
}

export const LINE_AXIS_TYPE: AxisType[] = ['xAxis', 'yAxis', 'drill', 'filter']
