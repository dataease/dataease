export const BAR_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'basic-style-selector',
  'label-selector',
  'tooltip-selector',
  'x-axis-selector',
  'y-axis-selector',
  'title-selector',
  'legend-selector'
]

export const BAR_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'basic-style-selector': ['colors', 'alpha', 'gradient'],
  'label-selector': ['fontSize', 'color', 'position'],
  'tooltip-selector': ['fontSize', 'color', 'backgroundColor'],
  'x-axis-selector': [
    'position',
    'name',
    'color',
    'fontSize',
    'axisLine',
    'splitLine',
    'axisForm',
    'axisLabel'
  ],
  'y-axis-selector': [
    'position',
    'name',
    'color',
    'fontSize',
    'axisValue',
    'splitLine',
    'axisForm',
    'axisLabel',
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

export const BAR_AXIS_TYPE: AxisType[] = [
  'xAxis',
  'yAxis',
  'filter',
  'drill',
  'extLabel',
  'extTooltip'
]
