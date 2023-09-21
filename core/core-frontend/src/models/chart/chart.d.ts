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

/**
 * 多系列格式化属性
 */
declare interface SeriesFormatter extends Axis {
  /**
   * 是否显示
   */
  show: boolean
  /**
   * 字体颜色
   */
  color: string
  /**
   * 字体大小
   */
  fontSize: number
}

declare interface Axis extends ChartViewField {
  /**
   * 格式化设置
   */
  formatterCfg: BaseFormatter
  /**
   * 聚合方式
   */
  summary: string
}
declare interface ChartViewField {
  /**
   * 字段名称
   */
  name: string
  /**
   * de名称
   */
  dataeaseName: string
  /**
   * id
   */
  id: string
  /**
   * 视图自定义字段名称
   */
  chartShowName: string
  /**
   * 字段类型
   */
  deType: number
}

declare interface Filter {
  datasetTableField: ChartViewField
  fieldId: string
}
