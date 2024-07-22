export const MAP_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'basic-style-selector',
  'title-selector',
  'label-selector',
  'tooltip-selector',
  'function-cfg',
  'map-mapping',
  'jump-set',
  'linkage'
]

export const MAP_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'basic-style-selector': ['colors', 'alpha', 'areaBorderColor', 'zoom'],
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
  'label-selector': [
    'color',
    'fontSize',
    'labelBgColor',
    'labelShadow',
    'labelShadowColor',
    'showDimension',
    'showQuota'
  ],
  'tooltip-selector': ['color', 'fontSize', 'backgroundColor', 'seriesTooltipFormatter', 'show'],
  'function-cfg': ['emptyDataStrategy'],
  'map-mapping': ['']
}

export const MAP_AXIS_TYPE: AxisType[] = [
  'xAxis',
  'yAxis',
  'area',
  'drill',
  'filter',
  'extLabel',
  'extTooltip'
]

export declare type MapMouseEvent = MouseEvent & {
  feature: GeoJSON.Feature
}
