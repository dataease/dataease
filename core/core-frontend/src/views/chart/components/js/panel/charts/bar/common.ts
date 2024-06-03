export const BAR_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'basic-style-selector',
  'label-selector',
  'tooltip-selector',
  'x-axis-selector',
  'y-axis-selector',
  'title-selector',
  'legend-selector',
  'function-cfg',
  'assist-line',
  'jump-set',
  'linkage'
]
export const BAR_RANGE_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'basic-style-selector',
  'label-selector',
  'tooltip-selector',
  'x-axis-selector',
  'y-axis-selector',
  'title-selector',
  'legend-selector',
  'function-cfg',
  'jump-set',
  'linkage'
]

export const BAR_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'basic-style-selector': ['colors', 'alpha', 'gradient', 'radiusColumnBar'],
  'label-selector': ['fontSize', 'color', 'labelFormatter'],
  'tooltip-selector': ['fontSize', 'color', 'tooltipFormatter', 'show'],
  'x-axis-selector': [
    'name',
    'color',
    'fontSize',
    'axisLine',
    'splitLine',
    'axisForm',
    'axisLabel',
    'position'
  ],
  'y-axis-selector': [
    'name',
    'color',
    'fontSize',
    'axisValue',
    'axisLine',
    'splitLine',
    'axisForm',
    'axisLabel',
    'position'
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
  'function-cfg': ['slider', 'emptyDataStrategy']
}

export const BAR_AXIS_TYPE: AxisType[] = [
  'xAxis',
  'yAxis',
  'filter',
  'drill',
  'extLabel',
  'extTooltip'
]
