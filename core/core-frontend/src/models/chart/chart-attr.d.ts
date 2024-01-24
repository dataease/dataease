/**
 * 视图设置
 */
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
  /**
   * 杂项设置
   */
  misc: ChartMiscAttr
  /**
   * 标签设置
   */
  label: ChartLabelAttr
  /**
   * 提示设置
   */
  tooltip: ChartTooltipAttr
  /**
   * 地图设置
   */
  map: MapCfg
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
  tableColumnMode: 'adapt' | 'custom'
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
   * 符号地图符号形状
   */
  mapSymbol: string
  /**
   * 符号地图符号大小
   */
  mapSymbolSize: number
  /**
   * 符号透明度
   */
  mapSymbolOpacity: number
  /**
   * 符号边框宽度
   */
  mapSymbolStrokeWidth: number
  /**
   * 环形图/玫瑰图内径占比
   */
  innerRadius: number
  /**
   * 环形图/玫瑰图外径占比
   */
  radius: number
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
 * 表格汇总设置
 */
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
  subTotalsDimensions: string[]
  /**
   * 总计汇总设置
   */
  calcTotals: CalcTotals
  /**
   * 小计汇总设置
   */
  calcSubTotals: CalcTotals
  /**
   * 总计排序: asc,desc
   */
  totalSort: string
  /**
   * 小计排序
   */
  totalSortField: string
}
/**
 * 汇总聚合方式
 */
declare interface CalcTotals {
  aggregation: 'MIN' | 'MAX' | 'AVG' | 'SUM'
  cfg: CalcTotalCfg[]
  calcFunc?: (...args) => any
}

/**
 * 汇总聚合配置
 */
declare interface CalcTotalCfg {
  dataeaseName: string
  aggregation: 'MIN' | 'MAX' | 'AVG' | 'SUM' | ''
}

/**
 * 视图杂项设置
 */
declare interface ChartMiscAttr {
  /**
   * 饼图/玫瑰图内圈占比
   */
  pieInnerRadius: number
  /**
   * 饼图/玫瑰图外圈占比
   */
  pieOuterRadius: number
  /**
   * 雷达图形状
   */
  radarShape: string
  /**
   * 雷达图大小
   */
  radarSize: number
  /**
   * 仪表盘最小值类型: fix,dynamic
   */
  gaugeMinType: 'fix' | 'dynamic'
  /**
   * 仪表盘动态最小值配置
   */
  gaugeMinField: ExtremeField
  /**
   * 仪表盘静态最小值
   */
  gaugeMin: number
  /**
   * 仪表盘最大值类型: fix,dynamic
   */
  gaugeMaxType: 'fix' | 'dynamic'
  /**
   * 仪表盘动态最大值配置
   */
  gaugeMaxField: ExtremeField
  /**
   * 仪表盘静态最大值
   */
  gaugeMax: number
  /**
   * 仪表盘起始角度
   */
  gaugeStartAngle: number
  /**
   * 仪表盘结束角度
   */
  gaugeEndAngle: number
  treemapWidth: number
  treemapHeight: number
  /**
   * 水波图静态目标值
   */
  liquidMax: number
  /**
   * 水波图目标值类型: fix, dynamic
   */
  liquidMaxType: string
  /**
   * 水波图目标值配置
   */
  liquidMaxField: ExtremeField
  /**
   * 水波图大小
   */
  liquidSize: number
  /**
   * 水波图形状
   */
  liquidShape: string
  /**
   * 地图倾角
   */
  mapPitch: number
  /**
   * 地图线条类型
   */
  mapLineType: string
  /**
   * 地图线条宽度
   */
  mapLineWidth: number
  /**
   * 流向地图动画间隔
   */
  mapLineAnimateDuration: number
  /**
   * 地图线条渐变
   */
  mapLineGradient: boolean
  /**
   * 地图线条渐变起始颜色
   */
  mapLineSourceColor: string
  /**
   * 地图线条渐变结束颜色
   */
  mapLineTargetColor: string
  /**
   * 指标/文本卡值字体
   */
  valueFontFamily: string
  /**
   * 指标/文本卡值字体大小
   */
  valueFontSize: number
  /**
   * 指标/文本卡值字体颜色
   */
  valueFontColor: string
  /**
   * 指标/文本卡值字体加粗
   */
  valueFontIsBolder: boolean
  /**
   * 指标/文本卡值字体倾斜
   */
  valueFontIsItalic: boolean
  /**
   * 指标/文本卡值字体间距
   */
  valueLetterSpace: number
  /**
   * 指标/文本卡值字体阴影
   */
  valueFontShadow: boolean
  /**
   * 指标/文本卡名称显隐
   */
  showName: boolean
  /**
   * 指标/文本卡名称字体
   */
  nameFontFamily: string
  /**
   * 指标/文本卡名称字体大小
   */
  nameFontSize: number
  /**
   * 指标/文本卡名称字体颜色
   */
  nameFontColor: string
  /**
   * 指标/文本卡名称字体加粗
   */
  nameFontIsBolder: boolean
  /**
   * 指标/文本卡名称字体倾斜
   */
  nameFontIsItalic: boolean
  /**
   * 指标/文本卡名称字体间距
   */
  nameLetterSpace: string
  /**
   * 指标/文本卡名称字体阴影
   */
  nameFontShadow: boolean
  /**
   * 指标/文本卡名/值间距
   */
  nameValueSpace: number
  /**
   * 指标/文本卡水平位置
   */
  hPosition: 'left' | 'center' | 'right'
  /**
   * 指标/文本卡垂直位置
   */
  vPosition: 'top' | 'center' | 'bottom'
}
/**
 * 动态极值配置
 */
declare interface ExtremeField {
  /**
   * 字段id
   */
  id: string
  /**
   * 聚合方式
   */
  summary: string
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
  labelLine: {
    show: boolean
  }
  /**
   * 标签格式化设置
   */
  labelFormatter: BaseFormatter
  /**
   * 标签保留小数位数
   */
  reserveDecimalCount: number
  /**
   * 显示维度
   */
  showDimension: boolean
  /**
   * 显示指标
   */
  showQuota: boolean
  /**
   * 显示占比
   */
  showProportion: boolean
  /**
   * 指标格式化设置
   */
  quotaLabelFormatter: BaseFormatter
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
  /**
   * 多系列标签设置
   */
  seriesLabelFormatter: SeriesFormatter[]
}
/**
 * 提示设置
 */
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
   * 格式化
   */
  tooltipFormatter: BaseFormatter
  /**
   * 背景颜色
   */
  backgroundColor: string
  /**
   * 多系列提示设置
   */
  seriesTooltipFormatter: SeriesFormatter[]
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
