declare type EditorProperty =
  | 'background-overall-component'
  | 'basic-style-selector'
  | 'label-selector'
  | 'tooltip-selector'
  | 'x-axis-selector'
  | 'y-axis-selector'
  | 'title-selector'
  | 'legend-selector'
  | 'table-header-selector'
  | 'table-cell-selector'
  | 'table-total-selector'
  | 'text-selector'
  | 'misc-selector'
declare type EditorPropertyInner = {
  [key in EditorProperty]?: string[]
}

/**
 * 轴类型
 */
declare type AxisType =
  | 'xAxis'
  | 'yAxis'
  | 'xAxisExt'
  | 'yAxisExt'
  | 'drill'
  | 'filter'
  | 'extStack'
  | 'extLabel'
  | 'extTooltip'
  | 'area'
/**
 * 轴类型对应描述，不同视图对于轴的描述不同
 */
declare type AxisDesc = {
  [key in AxisType]?: string
}
