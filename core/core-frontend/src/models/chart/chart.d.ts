/**
 * 视图对象
 */
declare interface Chart {
  id: string
  render: string
  name: string
  type: string
  title: string
  drill?: boolean
  refreshViewEnable: boolean
  refreshTime: number
  refreshUnit: string
  data: {
    data: any[]
    series?: any[]
    dynamicAssistLines?: AssistLine[]
    fields: ChartViewField[]
    tableRow: []
  }
  xAxis?: Axis[]
  xAxisExt?: Axis[]
  yAxis?: Axis[]
  yAxisExt?: Axis[]
  extStack?: Axis[]
  extBubble?: Axis[]
  extLabel?: Axis[]
  extTooltip?: Axis[]
  customFilter: []
  senior: CustomSenior
  customAttr: CustomAttr
  customStyle: CustomStyle
  drillFields: ChartViewField[]
  drillFilters: Filter[]
  datasetMode: 0 | 1
  datasourceType: string
  totalItems: number
  tableId: number
  resultMode: string
  resultCount: number
  linkageActive: boolean
  jumpActive: boolean
}
declare type CustomAttr = DeepPartial<ChartAttr> | JSONString<DeepPartial<ChartAttr>>
declare type CustomStyle = DeepPartial<ChartStyle> | JSONString<DeepPartial<ChartStyle>>
declare type CustomSenior = DeepPartial<ChartSenior> | JSONString<DeepPartial<ChartSenior>>

declare type ChartObj = Omit<Chart, 'customAttr' | 'customStyle' | 'senior'> & {
  customAttr: ChartAttr
  customStyle: ChartStyle
  senior: ChartSenior
}

/**
 * 格式化属性
 */
declare interface BaseFormatter {
  /**
   * 格式化类型：auto,value,percent
   */
  type: string
  /**
   * 单位换算
   */
  unit: number
  /**
   * 单位后缀
   */
  suffix: string
  /**
   * 保留小数位数
   */
  decimalCount: number
  /**
   * 千分符
   */
  thousandSeparator: boolean
}

declare interface Axis {
  id: string
  name: string
  dataeaseName: string
  formatterCfg: BaseFormatter
  chartShowName: string
}
declare interface ChartViewField {
  name: string
  dataeaseName: string
  id: string
  /**
   * 视图自定义字段名称
   */
  chartShowName: string
}

declare interface Filter {
  datasetTableField: ChartViewField
  fieldId: string
}
