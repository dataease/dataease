/**
 * 图表对象
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
    //chart-mix
    left: {
      data: any[]
      series?: any[]
      dynamicAssistLines?: AssistLine[]
      fields: ChartViewField[]
      tableRow: []
    }
    right: {
      data: any[]
      series?: any[]
      dynamicAssistLines?: AssistLine[]
      fields: ChartViewField[]
      tableRow: []
    }
  }
  xAxis?: Axis[]
  xAxisExt?: Axis[]
  yAxis?: Axis[]
  yAxisExt?: Axis[]
  extStack?: Axis[]
  extBubble?: Axis[]
  extLabel?: Axis[]
  extTooltip?: Axis[]
  customFilter: {}
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
  aggregate?: boolean
  plugin?: CustomPlugin
  isPlugin: boolean
  extremumValues?: Map<string, any>
  filteredData?: any[]
  container?: string
  /**
   * 针对不是序列字段的图表，通过获取分类字段的值作为序列字段
   */
  seriesFieldObjs?: any[]
  flowMapStartName?: Axis[]
  flowMapEndName?: Axis[]
}
declare type CustomAttr = DeepPartial<ChartAttr> | JSONString<DeepPartial<ChartAttr>>
declare type CustomStyle = DeepPartial<ChartStyle> | JSONString<DeepPartial<ChartStyle>>
declare type CustomSenior = DeepPartial<ChartSenior> | JSONString<DeepPartial<ChartSenior>>
declare type CustomPlugin = DeepPartial<ChartPlugin> | JSONString<DeepPartial<ChartPlugin>>

declare type ChartObj = Omit<Chart, 'customAttr' | 'customStyle' | 'senior' | 'plugin'> & {
  customAttr: ChartAttr
  customStyle: ChartStyle
  senior: ChartSenior
  plugin?: ChartPlugin
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
  color?: string
  /**
   * 字体大小
   */
  fontSize?: number
  /**
   * 序列id
   */
  seriesId: string
  /**
   * 轴类型
   */
  axisType: string
  /**
   * 显示极值
   */
  showExtremum?: boolean

  optionLabel?: string
  optionShowName?: string
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
  /**
   * 维度/指标分组类型
   */
  groupType: 'q' | 'd'
  /**
   * 排序规则
   */
  sort: 'asc' | 'desc' | 'none' | 'custom_sort'
  /**
   * 自定义排序项
   */
  customSort: string[]
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
   * 图表自定义字段名称
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
