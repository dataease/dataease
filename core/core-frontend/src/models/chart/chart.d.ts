declare interface ChartSeriesColor {
  name: string
  color: string
  isCustom: boolean
}

declare interface ChartColorAttr {
  value: string
  colors: string[]
  alpha: number
  tableHeaderBgColor: string
  tableItemBgColor: string
  tableHeaderFontColor: string
  tableFontColor: string
  tableStripe: boolean
  dimensionColor: string
  quotaColor: string
  tableBorderColor: string
  seriesColors: ChartSeriesColor[]
  areaBorderColor: string
  gradient: boolean
  areaBaseColor: string
  tableScrollBarColor: string
  tableScrollBarHoverColor: string
  mapStyle: string
  mapLineGradient: boolean
  mapLineSourceColor: string
  mapLineTargetColor: string
}

declare interface GaugeField {
  id: string
  summary: string
}

declare interface ChartSizeAttr {
  barDefault: boolean
  barWidth: number
  barGap: number
  lineWidth: number
  lineType: string
  lineSymbol: string
  lineSymbolSize: number
  lineSmooth: boolean
  lineArea: boolean
  pieInnerRadius: number
  pieOuterRadius: number
  pieRoseType: string
  pieRoseRadius: number
  funnelWidth: number
  radarShape: string
  radarSize: number
  tableTitleFontSize: number
  tableItemFontSize: number
  tableTitleHeight: number
  tableItemHeight: number
  tablePageSize: string
  tableColumnMode: string
  tableColumnWidth: number
  tableHeaderAlign: string
  tableItemAlign: string
  tableAutoBreakLine: boolean
  gaugeMinType: string
  gaugeMinField: GaugeField
  gaugeMin: number
  gaugeMaxType: string
  gaugeMaxField: GaugeField
  gaugeMax: number
  gaugeStartAngle: number
  gaugeEndAngle: number
  gaugeTickCount: number
  dimensionFontSize: number
  quotaFontSize: number
  spaceSplit: number
  dimensionShow: boolean
  quotaShow: boolean
  quotaFontFamily: string
  quotaFontIsBolder: boolean
  quotaFontIsItalic: boolean
  quotaLetterSpace: string
  quotaFontShadow: boolean
  dimensionFontFamily: string
  dimensionFontIsBolder: boolean
  dimensionFontIsItalic: boolean
  dimensionLetterSpace: string
  dimensionFontShadow: boolean
  scatterSymbol: string
  scatterSymbolSize: number
  treemapWidth: number
  treemapHeight: number
  liquidMax: number
  liquidMaxType: string // fix or dynamic
  liquidMaxField: GaugeField
  liquidSize: number
  liquidOutlineBorder: number
  liquidOutlineDistance: number
  liquidWaveLength: number
  liquidWaveCount: number
  liquidShape: string
  tablePageMode: string
  symbolOpacity: number
  symbolStrokeWidth: number
  showIndex: boolean
  indexLabel: string
  hPosition: string
  vPosition: string
  mapPitch: number
  mapLineType: string
  mapLineWidth: number
  mapLineAnimate: boolean
  mapLineAnimateDuration: number
  mapLineAnimateInterval: number
  mapLineAnimateTrailLength: number
}

declare interface LabelLine {
  show: boolean
}

declare interface GaugeLabelFormatter {
  /**
   * 格式类型
   */
  type: string
  /**
   * 换算单位
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
 * 标签属性
 */
declare interface ChartLabelAttr {
  /**
   * 显隐
   */
  show: boolean
  /**
   * 位置
   */
  position: string
  /**
   * 字体颜色
   */
  color: string
  /**
   * 字体大小
   */
  fontSize: number
  /**
   * 格式类型
   */
  formatter: string
  /**
   * 标签引线
   */
  labelLine: LabelLine
  /**
   * 标签格式化设置
   */
  gaugeLabelFormatter: GaugeLabelFormatter
  /**
   * 标签保留小数位数
   */
  reserveDecimalCount: number
  /**
   * 标签内容
   */
  labelContent: string[]
  /**
   * 标签阴影
   */
  labelShadow: boolean
  /**
   * 标签背景颜色
   */
  labelBgColor: string
  /**
   * 标签阴影颜色
   */
  labelShadowColor: string
}

declare interface TextStyle {
  fontSize: string
  color: string
}

declare interface ChartTooltipAttr {
  /**
   * 显隐
   */
  show: boolean
  trigger: string
  confine: boolean
  /**
   * 字体颜色
   */
  color: string
  /**
   * 字体大小
   */
  fontSize: number
  /**
   * 字体格式化
   */
  formatter: string
  /**
   * 背景颜色
   */
  backgroundColor: string
}

declare interface CalcTotals {
  aggregation: string
}

/**
 * 汇总设置
 */
declare interface TotalConfig {
  /**
   * 总计显隐
   */
  showGrandTotals: boolean
  /**
   * 小计显隐
   */
  showSubTotals: boolean
  /**
   * 总计反转布局
   */
  reverseLayout: boolean
  /**
   * 小计反转布局
   */
  reverseSubLayout: boolean
  /**
   * 总计标签
   */
  label: string
  /**
   * 小计标签
   */
  subLabel: string
  /**
   * 小计维度
   */
  subTotalsDimensions: []
  /**
   * 总计汇总设置
   */
  calcTotals: CalcTotals
  /**
   * 小计汇总设置
   */
  calcSubTotals: CalcTotals
  /**
   * 总计排序
   */
  totalSort: string // asc,desc
  /**
   * 小计排序
   */
  totalSortField: string
}

declare interface ChartTableTotalAttr {
  /**
   * 行汇总
   */
  row: TotalConfig
  /**
   * 列汇总
   */
  col: TotalConfig
}

declare interface ChartTextStyle {
  show: boolean
  fontSize: string
  color: string
  hPosition: string
  vPosition: string
  isItalic: boolean
  isBolder: boolean
  remarkShow: boolean
  remark: string
  remarkBackgroundColor: string
  fontFamily: string
  letterSpace: string
  fontShadow: boolean
}

/**
 * 图例文字样式
 */
declare interface LegendTextStyle {
  /**
   * 颜色
   */
  color: string
  /**
   * 大小
   */
  fontSize: string
}

/**
 * 图例设置
 */
declare interface ChartLegendStyle {
  /**
   * 显隐
   */
  show: boolean
  /**
   * 水平位置
   */
  hPosition: string
  /**
   * 垂直位置
   */
  vPosition: string
  /**
   *
   */
  orient: string
  icon: string
  /**
   * 字体设置
   */
  textStyle: LegendTextStyle
}

declare interface NameTextStyle {
  /**
   * 字体颜色
   */
  color: string
  /**
   * 字体大小
   */
  fontSize: number
}

/**
 * 轴线标签
 */
declare interface AxisLabel {
  /**
   * 显隐
   */
  show: boolean
  /**
   * 标签颜色
   */
  color: string
  /**
   * 字体大小
   */
  fontSize: string
  /**
   * 旋转角度
   */
  rotate: number
  /**
   * 格式化设置
   */
  formatter: string
}

declare interface LineStyle {
  /**
   * 轴线颜色
   */
  color: string
  /**
   * 轴线线条宽度
   */
  width: number
  style: string
}

declare interface AxisLine {
  /**
   * 轴线显示
   */
  show: boolean
  /**
   * 轴线样式设置
   */
  lineStyle: LineStyle
}

/**
 * 轴值设置
 */
declare interface AxisValue {
  /**
   * 自动轴值
   */
  auto: boolean
  /**
   * 最小值
   */
  min: number
  /**
   * 最大值
   */
  max: number
  /**
   * 刻度数
   */
  split: number
  /**
   * 刻度数
   */
  splitCount: number
}

/**
 * 轴线标签格式化属性
 */
declare interface AxisLabelFormatter {
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
 * 视图轴设置
 */
declare interface ChartAxisStyle {
  /**
   * 是否显示轴线
   */
  show: boolean
  /**
   * 轴线位置
   */
  position: string
  /**
   * 轴线标题名称
   */
  name: string
  /**
   * 轴线标题名称设置
   */
  nameTextStyle: NameTextStyle
  /**
   * 轴线标签设置
   */
  axisLabel: AxisLabel
  /**
   * 轴线设置
   */
  axisLine: AxisLine
  /**
   * 网格线设置
   */
  splitLine: AxisLine
  /**
   *  轴值设置
   */
  axisValue: AxisValue
  /**
   * (值)轴线格式化设置
   */
  axisLabelFormatter: AxisLabelFormatter
}

declare interface SplitName {
  show: boolean
  color: string
  fontSize: string
}

declare interface SplitLineStyle {
  color: string
  width: number
  type: string
}

declare interface SplitAxisLine {
  show: boolean
  lineStyle: SplitLineStyle
}

declare interface SplitAxisTick {
  show: boolean
  length: number
  lineStyle: SplitLineStyle
}

declare interface SplitAxisLabel {
  show: boolean
  rotate: number
  margin: number
  color: string
  fontSize: string
  formatter: string
}

declare interface SplitSplitArea {
  show: boolean
}

declare interface ChartSplitStyle {
  name: SplitName
  splitNumber: number
  axisLine: SplitAxisLine
  axisTick: SplitAxisTick
  axisLabel: SplitAxisLabel
  splitLine: SplitAxisLine
  splitArea: SplitSplitArea
}

declare interface ChartFunctionCfg {
  sliderShow: boolean
  sliderRange: number[]
  sliderBg: string
  sliderFillBg: string
  sliderTextColor: string
  emptyDataStrategy: string
  emptyDataFieldCtrl: []
}

declare interface ChartThreshold {
  gaugeThreshold: string
  labelThreshold: []
  tableThreshold: []
  textLabelThreshold: []
}

/**
 * 样式设置
 */
declare interface ChartStyle {
  /**
   * 文本样式
   */
  text: ChartTextStyle
  /**
   * 图例设置
   */
  legend: ChartLegendStyle
  /**
   * 横轴设置
   */
  xAxis: ChartAxisStyle
  /**
   * 值轴设置
   */
  yAxis: ChartAxisStyle
  /**
   * 副值轴设置
   */
  yAxisExt: ChartAxisStyle
  split: ChartSplitStyle
  background: {
    color: string
    alpha: string
  }
}

/**
 * 基础样式设置
 */
declare interface ChartBasicStyle {
  /**
   * 透明度
   */
  alpha: number
  /**
   * 表格边框颜色
   */
  tableBorderColor: string
  /**
   * 表格滚动条颜色
   */
  tableScrollBarColor: string
  /**
   * 表格列宽模式: 自适应和自定义
   */
  tableColumnMode: 'adopt' | 'custom'
  /**
   * 表格列宽
   */
  tableColumnWidth: number
  /**
   * 表格分页模式
   */
  tablePageMode: 'page' | 'pull'
  /**
   * 表格分页大小
   */
  tablePageSize: number
  /**
   * 仪表盘样式
   */
  gaugeStyle: string
  /**
   * 配色方案
   */
  colorScheme: string
  /**
   * 配色
   */
  colors: string[]
  /**
   * 渐变
   */
  gradient: boolean
  /**
   * 线宽
   */
  lineWidth: number
  /**
   * 折点形状
   */
  lineSymbol: string
  /**
   * 折点大小
   */
  lineSymbolSize: number
  /**
   * 平滑折线
   */
  lineSmooth: boolean
  /**
   * 自适应
   */
  barDefault: boolean
  /**
   * 柱宽
   */
  barWidth: number
  /**
   * 柱间距
   */
  barGap: number
  /**
   * 线形状
   */
  lineType: 'solid' | 'dashed'
  /**
   * 散点形状
   */
  scatterSymbol: string
  /**
   * 散点气泡大小
   */
  scatterSymbolSize: number
  /**
   * 雷达图外形形状
   */
  radarShape: 'circle' | 'polygon'
  /**
   * 地图底图类型
   */
  mapVendor: string
  /**
   * 地图主题风格
   */
  mapStyle: string
  /**
   * 地图边线颜色
   */
  areaBorderColor: string
  /**
   * 悬浮工具栏
   */
  suspension: boolean
  /**
   * 地图底色
   */
  areaBaseColor: string
  /**
   * 符号透明度
   */
  symbolOpacity: number
  /**
   * 符号边框宽度
   */
  symbolStrokeWidth: number
}

/**
 * 地图属性
 */
declare interface MapCfg {
  /**
   * 区域级别
   */
  level: 'world' | 'country' | 'province' | 'city' | 'district'
  /**
   * 区域id
   */
  id: string
}

/**
 * 表头属性
 */
declare interface ChartTableHeaderAttr {
  /**
   * 表头背景颜色
   */
  tableHeaderBgColor: string
  /**
   * 表头字体大小
   */
  tableTitleFontSize: number
  /**
   * 表头字体颜色
   */
  tableHeaderFontColor: string
  /**
   * 表头行高
   */
  tableTitleHeight: number
  /**
   * 表头对齐方式
   */
  tableHeaderAlign: 'left' | 'center' | 'right'
  /**
   * 显示序号
   */
  showIndex: boolean
  /**
   * 序号表头名称
   */
  indexLabel: string
}
/**
 * 单元格属性
 */
declare interface ChartTableCellAttr {
  /**
   * 单元格背景颜色
   */
  tableItemBgColor: string
  /**
   * 单元格字体大小
   */
  tableItemFontSize: number
  /**
   * 单元格字体颜色
   */
  tableFontColor: string
  /**
   * 单元格对齐方式
   */
  tableItemAlign: 'left' | 'center' | 'right'
  /**
   * 单元格行高
   */
  tableItemHeight: number
}

/**
 * 文本/指标卡属性
 */
declare interface ChartCardAttr {
  /**
   * 值字体
   */
  valueFontFamily: string
  /**
   * 值字体大小
   */
  valueFontSize: number
  /**
   * 值字体颜色
   */
  valueFontColor: string
  /**
   * 值字体加粗
   */
  valueFontIsBolder: boolean
  /**
   * 值字体倾斜
   */
  valueFontIsItalic: boolean
  /**
   * 值字体间距
   */
  valueLetterSpace: number
  /**
   * 值字体阴影
   */
  valueFontShadow: boolean
  /**
   * 名称显隐
   */
  showName: boolean
  /**
   * 名称字体
   */
  nameFontFamily: string
  /**
   * 名称字体大小
   */
  nameFontSize: number
  /**
   * 名称字体颜色
   */
  nameFontColor: string
  /**
   * 名称字体加粗
   */
  nameFontIsBolder: boolean
  /**
   * 名称字体倾斜
   */
  nameFontIsItalic: boolean
  /**
   * 名称字体间距
   */
  nameLetterSpace: string
  /**
   * 名称字体阴影
   */
  nameFontShadow: boolean
  /**
   * 名/值间距
   */
  nameValueSpace: number
  /**
   * 水平位置
   */
  hPosition: 'left' | 'center' | 'right'
  /**
   * 垂直位置
   */
  vPosition: 'top' | 'center' | 'bottom'
}
declare interface ChartAttr {
  /**
   * 基础样式设置
   */
  basicStyle: ChartBasicStyle
  /**
   * 表格表头配置
   */
  tableHeader: ChartTableHeaderAttr
  /**
   * 表格单元格配置
   */
  tableCell: ChartTableCellAttr
  /**
   * 表格总计设置
   */
  tableTotal: ChartTableTotalAttr
  color: ChartColorAttr
  tableColor: ChartColorAttr
  /**
   * 大小设置
   */
  size: ChartSizeAttr
  /**
   * 标签设置
   */
  label: ChartLabelAttr
  /**
   * 提示设置
   */
  tooltip: ChartTooltipAttr
  /**
   * 文本/指标卡设置
   */
  cardCfg: ChartCardAttr
  /**
   * 地图设置
   */
  map: MapCfg
}

declare interface AssistLine {
  name: string
  value: string
  field: string
  color: string
  lineType: string
  fontSize: number
  fieldId: string
}
declare interface ChartSenior {
  functionCfg: ChartFunctionCfg
  assistLine: AssistLine[]
  threshold: ChartThreshold
  mapMapping: Record<string, Record<string, string>>
}
declare interface Axis {
  name: string
  formatterCfg: AxisLabelFormatter
}
declare interface ChartViewField {
  name: string
  dataeaseName: string
}
declare type CustomAttr = DeepPartial<ChartAttr> | JSONString<DeepPartial<ChartAttr>>
declare type CustomStyle = DeepPartial<ChartStyle> | JSONString<DeepPartial<ChartStyle>>
declare type CustomSenior = DeepPartial<ChartSenior> | JSONString<DeepPartial<ChartSenior>>
declare interface Chart {
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
  senior: CustomSenior
  customAttr: CustomAttr
  customStyle: CustomStyle
  drillFields: ChartViewField[]
}
