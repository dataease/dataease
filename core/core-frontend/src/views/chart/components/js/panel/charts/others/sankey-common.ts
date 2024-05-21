export const SANKEY_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'basic-style-selector',
  'label-selector',
  'tooltip-selector',
  'title-selector',
  'jump-set',
  'linkage'
]

export const SANKEY_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'basic-style-selector': ['colors', 'alpha', 'gradient'],
  'label-selector': ['fontSize', 'color', 'labelFormatter'],
  'tooltip-selector': ['fontSize', 'color', 'tooltipFormatter'],
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
  'function-cfg': ['slider', 'emptyDataStrategy']
}

export const SANKEY_AXIS_TYPE: AxisType[] = [
  'xAxis',
  'xAxisExt',
  'yAxis',
  'filter',
  'extLabel',
  'extTooltip'
]
