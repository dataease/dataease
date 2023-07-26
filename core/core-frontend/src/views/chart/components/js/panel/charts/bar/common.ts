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
  'background-overall-component': [],
  'basic-style-selector': ['colors', 'barDefault', 'barWidth', 'alpha'],
  'label-selector': ['fontSize', 'color', 'position'],
  'tooltip-selector': ['textStyle', 'backgroundColor'],
  'x-axis-selector': ['position', 'name', 'nameTextStyle', 'splitLine', 'axisForm', 'axisLabel'],
  'y-axis-selector': [
    'position',
    'name',
    'nameTextStyle',
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
