import type { Component } from "vue"

export type FieldSortType = 'none' | 'asc' | 'desc' | 'custom_sort'

export type FieldDateStyle =
  | 'y'
  | 'y_Q'
  | 'y_M'
  | 'y_W'
  | 'y_M_d'
  | 'H_m_s'
  | 'y_M_d_H'
  | 'y_M_d_H_m'
  | 'y_M_d_H_m_s'

export type FieldDatePattern = 'date_sub' | 'date_split'

export interface FieldFormatterConfig {
  type: 'auto' | 'value' | 'percent'
  unitLanguage: 'ch' | 'en'
  unit: number
  suffix: string
  decimalCount: number
  thousandSeparator: boolean
}

export interface FieldItemData {
  id: string | number
  name: string
  dataeaseName?: string
  chartShowName?: string
  desc?: string
  groupType: 'd' | 'q'
  deType?: number
  sort?: FieldSortType
  customSort?: Array<string | number>
  summary?: string
  dateFormat?: string
  dateStyle?: FieldDateStyle
  datePattern?: FieldDatePattern
  formatterCfg?: FieldFormatterConfig
  compareCalc?: {
    type: 'none' | 'percent' | 'accumulate'
    resultData?: 'percent' | 'sub'
  }
  hidden?: boolean
}

export interface FilterItem {
  fieldId: string | number
  fieldName: string
  operator: string
  values: (string | number)[]
}

export interface FilterTreeItem {
  type: 'item' | 'tree'
  fieldId?: string | number
  filterType?: string
  term?: string
  value?: string | number
  enumValue?: Array<string | number>
  timeType?: string
  timeValue?: string
  filterTypeTime?: string
  dynamicTimeSetting?: Record<string, any>
  subTree?: FilterTree
}

export interface FilterTree {
  logic?: 'and' | 'or'
  items?: FilterTreeItem[]
}

export interface SpreadsheetQueryFilterItem {
  filterId?: string
  componentId?: string | number
  fieldId: string | number
  operator: string
  value: string[]
  parameters: unknown[]
  isTree: boolean
  customFilter?: FilterTree
}

export interface PluginConfig {
  id: string
  type: string
}

export type SpreadsheetFilterTextSearchConditionType = 'single' | 'and' | 'or'

export interface SpreadsheetFilterTextSearchClause {
  operator: 'eq' | 'like'
  value: string
}

export interface SpreadsheetFilterTextSearchValue {
  conditionType: SpreadsheetFilterTextSearchConditionType
  clauses: SpreadsheetFilterTextSearchClause[]
}

export interface SpreadsheetFilterTreeField {
  fieldId: string | number
  fieldName: string
  datasetId: string | number
  deType: number
  order: number
}

export interface SpreadsheetFilterTreeLevelMapping {
  treeFieldId: string | number
  linkedFields: SpreadsheetFilterLinkedField[]
}

export type SpreadsheetFilterTreePath = Array<{
  treeFieldId: string | number
  value: string | number
}>

export type SpreadsheetFilterTimeGranularity = 'year' | 'month' | 'date' | 'datetime'

export type SpreadsheetFilterTimeRangeGranularity =
  | 'yearrange'
  | 'monthrange'
  | 'daterange'
  | 'datetimerange'

export type SpreadsheetFilterTimeUnit = 'year' | 'month' | 'day' | 'hour' | 'minute' | 'second'

export interface SpreadsheetFilterRelativeTime {
  value: number
  unit: SpreadsheetFilterTimeUnit
  direction?: 'before' | 'after'
  relativeToCurrent?: string
  time?: string
}

export interface SpreadsheetFilterTimeDynamicDefault {
  offset: SpreadsheetFilterRelativeTime
  time?: string
}

export interface SpreadsheetFilterTimeRangeDynamicDefault {
  start: SpreadsheetFilterRelativeTime
  end: SpreadsheetFilterRelativeTime
}

export type SpreadsheetFilterTimeRangeIntervalType = 'none' | 'start' | 'end' | 'timeInterval'

export interface SpreadsheetFilterTimeBoundary {
  type: 'fixed' | 'dynamic'
  value?: string
  dynamic?: SpreadsheetFilterRelativeTime
}

export interface SpreadsheetFilterTimeFilterRange {
  intervalType: SpreadsheetFilterTimeRangeIntervalType
  relativeToCurrentRange?: string
  start?: SpreadsheetFilterTimeBoundary
  end?: SpreadsheetFilterTimeBoundary
  dynamicWindow?: SpreadsheetFilterRelativeTime
  maximumSingleQuery?: SpreadsheetFilterRelativeTime
}

export interface SpreadsheetFilterCondition {
  id: string
  name: string
  visible: boolean
  required: boolean
  displayType: SpreadsheetFilterDisplayType
  optionSource: 'auto' | 'dataset' | 'manual'
  optionDatasetId?: string | number
  optionDatasetName?: string
  queryFieldId?: string | number
  queryFieldName?: string
  displayFieldId?: string | number
  displayFieldName?: string
  sortFieldId?: string | number
  sortFieldName?: string
  sortType?: 'asc' | 'desc' | 'customSort'
  sortList?: Array<string | number>
  manualOptions: Array<string | number>
  displayForm: 'dropdown' | 'tile'
  optionCountMode: 'default' | 'all'
  queryMode: 'click' | 'auto'
  multiple: boolean
  defaultValueEnabled: boolean
  defaultValueFirstItem: boolean
  showRule: 'always' | 'smart' | 'custom'
  defaultValue?: unknown
  selectValue?: unknown
  textSearchConditionType: SpreadsheetFilterTextSearchConditionType
  hideTextSearchConditionSwitch: boolean
  textSearchDefaultClauses: SpreadsheetFilterTextSearchClause[]
  treeDatasetId?: string | number
  treeDatasetName?: string
  treeFields: SpreadsheetFilterTreeField[]
  treeLevelMappings: SpreadsheetFilterTreeLevelMapping[]
  timeGranularity: SpreadsheetFilterTimeGranularity
  timeRangeGranularity: SpreadsheetFilterTimeRangeGranularity
  timeDefaultType: 'fixed' | 'dynamic'
  timeDynamicDefault: SpreadsheetFilterTimeDynamicDefault
  timeRangeDynamicDefault: SpreadsheetFilterTimeRangeDynamicDefault
  timeFilterRangeEnabled: boolean
  timeFilterRange: SpreadsheetFilterTimeFilterRange
  linkedFields: SpreadsheetFilterLinkedField[]
}

export type SpreadsheetFilterDisplayType =
  | 'textSelect'
  | 'textSearch'
  | 'treeSelect'
  | 'numberSelect'
  | 'numberRange'
  | 'time'
  | 'timeRange'

export interface SpreadsheetFilterLinkedField {
  pluginId: string
  pluginName: string
  fieldId: string | number
  fieldName: string
  datasetId?: string | number
  datasetName?: string
  groupType?: 'd' | 'q'
  deType?: number
}

export interface SpreadsheetFilterConfig extends PluginConfig {
  type: 'filter'
  visible: boolean
  conditions: SpreadsheetFilterCondition[]
  style: SpreadsheetFilterStyle
}

export interface SpreadsheetFilterBoxValue {
  top: number
  right: number
  bottom: number
  left: number
}

export interface SpreadsheetFilterBaseStyle {
  paddingMode: 'uniform' | 'custom'
  padding: SpreadsheetFilterBoxValue
  radius: number
  backgroundEnabled: boolean
  backgroundColor: string
  layout: 'horizontal' | 'vertical'
  align: 'left' | 'center' | 'right'
  gap: number
}

export interface SpreadsheetFilterConditionStyle {
  fillEnabled: boolean
  fillColor: string
  borderEnabled: boolean
  borderColor: string
  borderWidth: number
  color: string
  fontSize: number
  fontWeight: 'normal' | 'bold'
  fontStyle: 'normal' | 'italic'
}

export interface SpreadsheetFilterConditionNameStyle {
  show: boolean
  position: 'top' | 'left'
  color: string
  fontSize: number
  fontWeight: 'normal' | 'bold'
  fontStyle: 'normal' | 'italic'
  gap: number
}

export interface SpreadsheetFilterButtonStyle {
  btnList: Array<'sure' | 'clear' | 'reset'>
  primaryColor: string
  textColor: string
  fontSize: number
  fontWeight: 'normal' | 'bold'
  fontStyle: 'normal' | 'italic'
}

export interface SpreadsheetFilterStyle {
  base: SpreadsheetFilterBaseStyle
  condition: SpreadsheetFilterConditionStyle
  conditionName: SpreadsheetFilterConditionNameStyle
  button: SpreadsheetFilterButtonStyle
}

export interface PluginDataConfig {
  datasetId: string | number
  zones: Record<string, FieldItemData[]>
  customFilter: FilterTree
  queryFilter?: SpreadsheetQueryFilterItem[]
  resultLimit: number
}

export interface PluginFieldValuesRequest {
  type: string
  data: PluginDataConfig
  field: FieldItemData
}


export interface TablePluginConfig extends PluginConfig {
  data: PluginDataConfig
  placement: {
    sheetId: string
    sheetName: string
    startCell: string
  }
}


// ==========================================
// 3. 样式子类型（被各插件复用）
// ==========================================

export interface TableHeaderStyle {
  backgroundColor: string
  textColor: string
  fontSize: number
  bold: boolean
  italic?: boolean
  textAlign: 'left' | 'center' | 'right'
  verticalAlign?: 'top' | 'middle' | 'bottom'
  height: number
}

export interface TableCellStyle {
  /**
   * 单元格背景色
   */
  backgroundColor?: string
  /**
   * 斑马纹开关
   */
  enableZebra: boolean
  /**
   * 斑马纹颜色
   */
  zebraColor?: string
}

export interface TableColumnWidthStyle {
  autoWidth: boolean
  fixedWidth: number
}


export interface FieldZoneSchema {
  id: string
  name: string
  icon?: string
  acceptTypes: ('d' | 'q')[]
  minFields?: number
  maxFields?: number
  placeholder: string
  allowFieldConfig: boolean
  fieldItemComponent?: Component
}

export interface StyleSchema {
  component: Component
}

export const PLUGIN_ZONES: Record<string, FieldZoneSchema[]> = {
  pivot: [
    {
      id: 'rowDimensions',
      name: '行维度',
      acceptTypes: ['d'],
      minFields: 1,
      placeholder: '拖动字段至此处',
      allowFieldConfig: true
    },
    {
      id: 'columnDimensions',
      name: '列维度',
      acceptTypes: ['d'],
      minFields: 1,
      placeholder: '拖动字段至此处',
      allowFieldConfig: true
    },
    {
      id: 'values',
      name: '值',
      acceptTypes: ['q'],
      minFields: 1,
      placeholder: '拖动字段至此处',
      allowFieldConfig: true
    }
  ]
}
