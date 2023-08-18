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
  | 'misc-style-selector'
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
 * 轴配置
 */
declare type AxisConfig = {
  [key in AxisType]?: AxisSpec
}
/**
 * 轴类型详细配置
 */
declare type AxisSpec = {
  /**
   * 轴名称
   */
  name: string
  /**
   * 轴类型限制, 没有表示不限制
   */
  type?: 'q' | 'd'
  /**
   * 轴维度/指标数量限制, 0表示不限制
   */
  limit?: number
  /**
   * 轴是否允许重复
   */
  duplicate?: boolean
  /**
   * 轴提示
   */
  tooltip?: string
}
