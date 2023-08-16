export const TABLE_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'basic-style-selector',
  'table-header-selector',
  'table-cell-selector',
  'title-selector'
]
export const TABLE_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'basic-style-selector': ['tableColumnMode', 'tableBorderColor', 'tableScrollBarColor', 'alpha'],
  'table-header-selector': [
    'tableHeaderBgColor',
    'tableTitleFontSize',
    'tableHeaderFontColor',
    'tableTitleHeight',
    'tableHeaderAlign',
    'showIndex',
    'indexLabel'
  ],
  'table-cell-selector': [
    'tableItemBgColor',
    'tableItemFontSize',
    'tableFontColor',
    'tableItemAlign',
    'tableItemHeight'
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
  ]
}
