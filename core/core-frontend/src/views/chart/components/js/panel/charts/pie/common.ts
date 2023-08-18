import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

export const PIE_EDITOR_PROPERTY: EditorProperty[] = [
  'background-overall-component',
  'basic-style-selector',
  'title-selector',
  'legend-selector',
  'label-selector',
  'tooltip-selector'
]
export const PIE_EDITOR_PROPERTY_INNER: EditorPropertyInner = {
  'background-overall-component': ['all'],
  'label-selector': ['fontSize', 'color', 'rPosition'],
  'tooltip-selector': ['fontSize', 'color', 'backgroundColor'],
  'basic-style-selector': ['colors', 'alpha', 'radius'],
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

export const PIE_AXIS_TYPE: AxisType[] = [
  'xAxis',
  'yAxis',
  'drill',
  'filter',
  'extLabel',
  'extTooltip'
]

export const PIE_AXIS_CONFIG: AxisConfig = {
  xAxis: {
    name: `${t('chart.drag_block_pie_label')}/${t('chart.dimension')}`,
    type: 'd'
  }
}
