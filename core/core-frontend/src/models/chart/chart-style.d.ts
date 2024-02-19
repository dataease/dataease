/**
 * 视图样式设置
 */
declare interface ChartStyle {
  /**
   * 标题样式设置
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
  /**
   * 其他样式设置
   */
  misc: ChartMiscStyle
  background: {
    color: string
    alpha: string
  }
}

declare interface ChartIndicatorStyle {
  show: boolean
  fontSize: string
  color: string
  hPosition: 'left' | 'center' | 'right'
  vPosition: 'top' | 'center' | 'bottom'
  isItalic: boolean
  isBolder: boolean
  fontFamily: string
  letterSpace: string
  fontShadow: boolean

  suffixEnable: boolean
  suffix: string
  suffixFontSize: string
  suffixColor: string
  suffixIsItalic: boolean
  suffixIsBolder: boolean
  suffixFontFamily: string
  suffixLetterSpace: string
  suffixFontShadow: boolean
}
declare interface ChartIndicatorNameStyle {
  show: boolean
  fontSize: string
  color: string
  isItalic: boolean
  isBolder: boolean
  fontFamily: string
  letterSpace: string
  fontShadow: boolean
}

/**
 * 标题样式设置
 */
declare interface ChartTextStyle {
  /**
   * 显隐
   */
  show: boolean
  /**
   * 字体大小
   */
  fontSize: number
  /**
   * 颜色
   */
  color: string
  /**
   * 水平位置
   */
  hPosition: string
  /**
   * 垂直位置
   */
  vPosition: string
  /**
   * 斜体
   */
  isItalic: boolean
  /**
   * 加粗
   */
  isBolder: boolean
  /**
   * 备注显隐
   */
  remarkShow: boolean
  /**
   * 备注内容
   */
  remark: string
  /**
   * 备注背景颜色
   */
  remarkBackgroundColor: string
  /**
   * 字体
   */
  fontFamily: string
  /**
   * 字间距
   */
  letterSpace: string
  /**
   * 阴影
   */
  fontShadow: boolean
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
   * 排列方向
   */
  orient: string
  /**
   * 图例形状
   */
  icon: string
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
   * 轴线标题字体颜色
   */
  color: string
  /**
   * 轴线标题字体大小
   */
  fontSize: number
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
  axisLabelFormatter: BaseFormatter
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
  fontSize: number
  /**
   * 旋转角度
   */
  rotate: number
  /**
   * 格式化设置
   */
  formatter: string
}
/**
 * 轴线条设置
 */
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
 * 轴线条样式
 */
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
 * 其他样式设置
 */
declare interface ChartMiscStyle {
  /**
   * 名称显示
   */
  showName: boolean
  /**
   * 颜色
   */
  color: string
  /**
   * 字体大小
   */
  fontSize: number
  /**
   * 轴颜色
   */
  axisColor: string
  splitNumber: number
  axisLine: SplitAxisLine
  axisTick: SplitAxisTick
  axisLabel: SplitAxisLabel
  splitLine: SplitAxisLine
  splitArea: SplitSplitArea
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
