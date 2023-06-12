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
  type: string // auto,value,percent
  unit: number // 换算单位
  suffix: string // 单位后缀
  decimalCount: number // 小数位数
  thousandSeparator: boolean // 千分符
}

declare interface ChartLabelAttr {
  show: boolean
  position: string
  color: string
  fontSize: string
  formatter: string
  gaugeFormatter: string
  labelLine: LabelLine
  gaugeLabelFormatter: GaugeLabelFormatter
  reserveDecimalCount: number
  labelContent: string[]
}

declare interface TextStyle {
  fontSize: string
  color: string
}

declare interface ChartTooltipAttr {
  show: boolean
  trigger: string
  confine: boolean
  textStyle: TextStyle
  formatter: string
  backgroundColor: string
}

declare interface CalcTotals {
  aggregation: string
}

declare interface TotalConfig {
  showGrandTotals: boolean
  showSubTotals: boolean
  reverseLayout: boolean
  reverseSubLayout: boolean
  label: string
  subLabel: string
  subTotalsDimensions: []
  calcTotals: CalcTotals
  calcSubTotals: CalcTotals
  totalSort: string // asc,desc
  totalSortField: string
}

declare interface ChartTotalAttr {
  row: TotalConfig
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

declare interface LegendTextStyle {
  color: string
  fontSize: string
}

declare interface ChartLegendStyle {
  show: boolean
  hPosition: string
  vPosition: string
  orient: string
  icon: string
  textStyle: LegendTextStyle
}

declare interface NameTextStyle {
  color: string
  fontSize: number
}

declare interface AxisLabel {
  show: boolean
  color: string
  fontSize: string
  rotate: number
  formatter: string
}

declare interface LineStyle {
  color: string
  width: number
  style: string
}

declare interface AxisLine {
  show: boolean
  lineStyle: LineStyle
}

declare interface AxisValue {
  auto: boolean
  min: null
  max: null
  split: null
  splitCount: null
}

declare interface AxisLabelFormatter {
  type: string // auto,value,percent
  unit: number // 换算单位
  suffix: string // 单位后缀
  decimalCount: number // 小数位数
  thousandSeparator: boolean // 千分符
}

declare interface ChartAxisStyle {
  show: boolean
  position: string
  name: string
  nameTextStyle: NameTextStyle
  axisLabel: AxisLabel
  axisLine: AxisLine
  splitLine: AxisLine
  axisValue: AxisValue
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

declare interface ChartStyle {
  text: ChartTextStyle
  legend: ChartLegendStyle
  xAxis: ChartAxisStyle
  yAxis: ChartAxisStyle
  yAxisExt: ChartAxisStyle
  split: ChartSplitStyle
  background: {
    color: string
    alpha: string
  }
}

declare interface ChartAttr {
  color: ChartColorAttr
  tableColor: ChartColorAttr
  size: ChartSizeAttr
  label: ChartLabelAttr
  tooltip: ChartTooltipAttr
  totalCfg: ChartTotalAttr
}

declare interface AssistLine {
  name: string
  value: string
  field: string
  color: string
  lineType: string
  fontSize: number
}
declare interface ChartSenior {
  functionCfg: ChartFunctionCfg
  assistLine: AssistLine[]
  threshold: ChartThreshold
}
declare interface Axis {
  name: string
  formatterCfg: AxisLabelFormatter
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
}
