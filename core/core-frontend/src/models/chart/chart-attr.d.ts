/**
 * 图表设置
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
  /**
   * 象限设置
   */
  quadrant: QuadrantAttr
  /**
   * 指标值
   */
  indicator: ChartIndicatorStyle
  /**
   * 指标名称
   */
  indicatorName: ChartIndicatorNameStyle
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
  tableColumnMode: 'adapt' | 'custom' | 'field' | 'dialog'
  /**
   * 表格列宽
   */
  tableColumnWidth: number
  /**
   * 表格字段列宽
   */
  tableFieldWidth: {
    /**
     * 字段ID
     */
    fieldId: string
    /**
     * 字段名称
     */
    name?: string
    /**
     * 字段宽度占比
     */
    width: number
  }[]
  /**
   * 表格分页模式
   */
  tablePageMode: 'page' | 'pull'
  /**
   * 表格分页器风格
   */
  tablePageStyle: 'simple' | 'general'
  /**
   * 表格分页大小
   */
  tablePageSize: number
  /**
   * 表格展示形式,平铺和树形
   */
  tableLayoutMode: 'grid' | 'tree'
  /**
   * 表格默认展开层级
   */
  defaultExpandLevel: number | 'all'
  /**
   * 表格指标展示位置
   */
  quotaPosition: 'col' | 'row'
  /**
   * 行头指标列名称
   */
  quotaColLabel: string
  /**
   * 仪表盘样式
   */
  gaugeStyle: string
  /**
   * 仪表盘刻度显示
   */
  gaugeAxisLine: boolean
  /**
   * 仪表盘百分比刻度
   */
  gaugePercentLabel: boolean
  /**
   * 配色方案
   */
  colorScheme: string
  /**
   * 配色
   */
  colors: string[]
  /**
   * 多序列颜色
   */
  seriesColor: {
    /**
     * 序列识别id，多指标就是轴id，分组或者堆叠就是类别值
     */
    id: string
    /**
     * 显示名称
     */
    name: string
    /**
     * 序列颜色
     */
    color: string
  }[]
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
   * 柱子形状：直角｜圆角｜顶部圆角
   */
  radiusColumnBar?: 'rightAngle' | 'roundAngle' | 'topRoundAngle'
  /**
   * 圆角柱倒角
   */
  columnBarRightAngleRadius: number
  /**
   * 一般柱状图宽度占比，0 - 100 范围数值
   */
  columnWidthRatio: number
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
   * 自定义地图风格url
   */
  mapStyleUrl: string
  heatMapType?: string
  heatMapIntensity?: number
  heatMapRadius?: number
  /**
   * 地图边线颜色
   */
  areaBorderColor: string
  /**
   * @deprecated
   * 悬浮工具栏
   */
  suspension?: boolean
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
  /**
   * 是否显示地图缩放按钮
   */
  showZoom: boolean
  /**
   * 地图缩放按钮颜色
   */
  zoomButtonColor: string
  /**
   * 地图缩放按钮背景颜色
   */
  zoomBackground: string
  /**
   * 是否合并数据为其他
   */
  calcTopN: boolean
  /**
   * 只展示 TopN 项，其他合并为一项
   */
  topN: number
  /**
   * 其他项的标签
   */
  topNLabel: string
  /**
   * 对称柱状图方向
   */
  layout?: 'horizontal' | 'vertical'
  /**
   * 汇总表显示总计
   */
  showSummary: boolean
  /**
   * 汇总表总计标签
   */
  summaryLabel: string

  seriesSummary?: Array<{
    show: boolean
    field: string
    summary: string
  }>
  /**
   * 符号地图符号大小最小值
   */
  mapSymbolSizeMin: number
  /**
   * 符号地图符号大小最大值
   */
  mapSymbolSizeMax: number
  /**
   * 显示标签
   */
  showLabel: boolean
  /**
   * 自适应缩放
   */
  autoFit: boolean
  /**
   * 地图中心点经纬度
   */
  mapCenter: {
    /**
     * 经度
     */
    longitude: number
    /**
     * 纬度
     */
    latitude: number
  }
  /**
   * 缩放等级
   */
  zoomLevel: number
  /**
   * 符号地图自定义符号形状
   */
  customIcon: string
  /**
   * 表格鼠标悬浮样式
   */
  showHoverStyle: boolean
  /**
   * 明细表单元格自动换行
   */
  autoWrap: boolean
  /**
   * 最大行数
   */
  maxLines?: number
  /**
   * 雷达图辅助点
   */
  radarShowPoint: boolean
  /**
   * 雷达图辅助点大小
   */
  radarPointSize: number
  /**
   * 雷达图面积颜色开关
   */
  radarAreaColor: boolean
  /**
   * 圆形填充图边线颜色
   */
  circleBorderColor: string
  /**
   * 圆形填充图边线宽度
   */
  circleBorderWidth: number
  /**
   * 圆形填充图间距
   */
  circlePadding: number
}
/**
 * 表头属性
 */
declare interface ChartTableHeaderAttr {
  /**
   * 表头背景颜色
   */
  tableHeaderBgColor: string
  tableHeaderCornerBgColor: string
  tableHeaderColBgColor: string
  /**
   * 表头字体大小
   */
  tableTitleFontSize: number
  tableTitleCornerFontSize: number
  tableTitleColFontSize: number
  /**
   * 表头字体颜色
   */
  tableHeaderFontColor: string
  tableHeaderCornerFontColor: string
  tableHeaderColFontColor: string
  /**
   * 表头行高
   */
  tableTitleHeight: number
  /**
   * 表头对齐方式
   */
  tableHeaderAlign: 'left' | 'center' | 'right'
  tableHeaderCornerAlign: 'left' | 'center' | 'right'
  tableHeaderColAlign: 'left' | 'center' | 'right'
  /**
   * 显示序号
   */
  showIndex: boolean
  /**
   * 序号表头名称
   */
  indexLabel: string
  /**
   * 表头排序开关
   */
  tableHeaderSort: boolean
  /**
   * @deprecated since version 2.7.0 由提示统一控制
   * 行头鼠标悬浮提示开关
   */
  showRowTooltip: boolean
  /**
   * @deprecated since version 2.7.0 由提示统一控制
   * 列头鼠标悬浮提示开关
   */
  showColTooltip: boolean
  /**
   * 表头显示开关
   */
  showTableHeader: boolean
  /**
   * 表头横边框线
   */
  showHorizonBorder: boolean
  /**
   * 表头纵边框线
   */
  showVerticalBorder: boolean
  /**
   * 斜体
   */
  isItalic: boolean
  isCornerItalic: boolean
  isColItalic: boolean
  /**
   * 加粗
   */
  isBolder: boolean
  isCornerBolder: boolean
  isColBolder: boolean
  /**
   * 表头分组开关
   */
  headerGroup: boolean
  /**
   * 表头分组设置
   */
  headerGroupConfig: {
    /**
     * 分组结构
     */
    columns: Columns
    /**
     * 分组名称
     */
    meta: {
      /**
       * 字段id
       */
      field: string
      /**
       * 名称
       */
      name: string
    }[]
  }
  /**
   * 透视表行头冻结
   */
  rowHeaderFreeze: boolean
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
  /**
   * 是否开启斑马纹
   */
  enableTableCrossBG: boolean
  /**
   * 斑马纹单数行颜色
   */
  tableItemSubBgColor: string
  /**
   * @deprecated since version 2.7.0 由提示统一控制
   * 鼠标悬浮提示
   */
  showTooltip: boolean
  /**
   * 单元格横边框线
   */
  showHorizonBorder: boolean
  /**
   * 单元格纵边框线
   */
  showVerticalBorder: boolean
  /**
   * 斜体
   */
  isItalic: boolean
  /**
   * 加粗
   */
  isBolder: boolean
  /**
   * 冻结
   */
  tableFreeze: boolean
  /**
   * 冻结列
   */
  tableColumnFreezeHead: number
  /**
   * 冻结行
   */
  tableRowFreezeHead: number
  /**
   * 合并单元格
   */
  mergeCells: boolean
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
   * 兼容旧版的判断
   */
  subTotalsDimensionsNew: boolean
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
declare interface CalcTotalCfg extends Axis {
  dataeaseName: string
  /**
   * 聚合方式
   */
  aggregation: 'MIN' | 'MAX' | 'AVG' | 'SUM' | 'CUSTOM' | ''
  /**
   * 自定义汇总表达式
   */
  originName: string
  /**
   * 别名
   */
  label: string
}

/**
 * 图表杂项设置
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
  /**
   * 词云图字体大小区间
   */
  wordSizeRange: [number, number]
  /**
   * 词云图文字间距
   */
  wordSpacing: number
  /**
   * 自动图例
   */
  mapAutoLegend: boolean
  /**
   * 图例最大值
   */
  mapLegendMax: number
  /**
   * 图例最小值
   */
  mapLegendMin: number
  /**
   * 显示图例个数
   */
  mapLegendNumber: number
  /**
   * 自定义区间类型，等间距，自定义
   */
  mapLegendRangeType: 'quantize' | 'custom'
  /**
   * 自定义区间类型为自定义(custom)时生效
   * 自定义区间值
   */
  mapLegendCustomRange: number[]
  /**
   * 流向地图配置
   */
  flowMapConfig: {
    lineConfig: {
      /**
       * 地图线条类型
       */
      mapLineType: string
      /**
       * 地图线条宽度
       */
      mapLineWidth: number
      /**
       * 流向地图动画
       */
      mapLineAnimate?: boolean
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
      alpha: number
    }
    pointConfig: {
      text: {
        color: string
        fontSize: number
      }
      point: {
        color: string
        size: number
        animate: boolean
        speed: number
      }
    }
  }

  /**
   * 词云图轴值配置
   */
  wordCloudAxisValueRange: {
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
     * 轴值字段
     */
    fieldId: string
  }
  /**
   * 子弹图
   */
  bullet: BulletAtt
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
   * 收缩下的显示标签
   */
  childrenShow?: boolean
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
  labelFormatter: Partial<BaseFormatter>
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

  /**
   * 显示字段，通过字段名称显示对应的值
   * @example
   * ['name', 'value']
   */
  showFields?: string[]

  /**
   * 自定义显示内容
   */
  customContent?: string

  showGap?: boolean

  /**
   * 显示极值
   */
  showExtremum?: boolean

  /**
   * 转化率标签
   */
  conversionTag: ConversionTagAtt
  /**
   * 堆叠柱状图显示总计
   */
  showTotal: boolean
  /**
   * 总计标签字体大小
   */
  totalFontSize: number
  /**
   * 总计标签字体颜色
   */
  totalColor: string
  /**
   * 总计标签格式化设置
   */
  totalFormatter: BaseFormatter
  /**
   * 柱状图堆叠指标
   */
  showStackQuota: boolean
  /**
   * 全部显示
   */
  fullDisplay: boolean
  /**
   * 仪表盘占比显示格式
   */
  proportionSeriesFormatter: SeriesFormatter
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

  showGap?: boolean

  /**
   * 显示字段，通过字段名称显示对应的值
   * @example
   * ['name', 'value']
   */
  showFields?: string[]

  /**
   * 自定义显示内容
   */
  customContent?: string

  /**
   * 轮播设置
   */
  carousel: CarouselAttr
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
 * 象限属性
 */
declare interface QuadrantAttr {
  xBaseline?: number
  yBaseline?: number
  lineStyle: QuadrantLineStyle
  regionStyle: QuadrantCommonStyle[]
  labels: QuadrantLabelConf[]
}

/**
 * 象限图label配置
 */
declare interface QuadrantLabelConf {
  content: string
  style: QuadrantCommonStyle
}

/**
 * 象限公共样式
 */
declare interface QuadrantCommonStyle {
  /**
   * 颜色
   */
  fill: string
  /**
   * 透明度
   */
  fillOpacity: number

  /**
   * 字体大小
   */
  fontSize?: number
}

/**
 * 象限分割线样式
 */
declare interface QuadrantLineStyle {
  /**
   * 颜色
   */
  stroke: string
  /**
   * 宽度
   */
  lineWidth: number
  /**
   * 透明度
   */
  opacity: number
}

/**
 * 指标卡值样式
 */
declare interface ChartIndicatorStyle {
  /**
   * 是否显示
   */
  show: boolean
  /**
   * 字体大小
   */
  fontSize: number
  /**
   * 字体颜色
   */
  color: string
  /**
   * 背景颜色
   */
  backgroundColor: string
  /**
   * 水平位置
   */
  hPosition: 'left' | 'center' | 'right'
  /**
   * 垂直位置
   */
  vPosition: 'top' | 'center' | 'bottom'
  /**
   * 是否斜体
   */
  isItalic: boolean
  /**
   * 是否加粗
   */
  isBolder: boolean
  /**
   * 字体类型
   */
  fontFamily: string
  /**
   * 字间距
   */
  letterSpace: number
  /**
   * 是否显示字体阴影
   */
  fontShadow: boolean
  /**
   * 是否显示后缀
   */
  suffixEnable: boolean
  /**
   * 后缀内容
   */
  suffix: string
  /**
   * 后缀字体大小
   */
  suffixFontSize: number
  /**
   * 后缀字体颜色
   */
  suffixColor: string
  /**
   * 后缀是否斜体
   */
  suffixIsItalic: boolean
  /**
   * 后缀是否加粗
   */
  suffixIsBolder: boolean
  /**
   * 后缀字体类型
   */
  suffixFontFamily: string
  /**
   * 后缀字间距
   */
  suffixLetterSpace: number
  /**
   * 后置是否显示阴影
   */
  suffixFontShadow: boolean
}

/**
 * 指标卡名称样式
 */
declare interface ChartIndicatorNameStyle {
  /**
   * 是否显示
   */
  show: boolean
  /**
   * 字体大小
   */
  fontSize: number
  /**
   * 字体颜色
   */
  color: string
  /**
   * 是否斜体
   */
  isItalic: boolean
  /**
   * 是否加粗
   */
  isBolder: boolean
  /**
   * 字体类型
   */
  fontFamily: string
  /**
   * 字间距
   */
  letterSpace: number
  /**
   * 是否显示字体阴影
   */
  fontShadow: boolean
  /**
   * 指标/名称间距
   */
  nameValueSpacing: number
}

/**
 * 轮播属性
 */
declare interface CarouselAttr {
  /**
   * 是否启用
   */
  enable: boolean
  /**
   * 停留时间 秒
   */
  stayTime: number
  /**
   * 轮播间隔时间 秒
   */
  intervalTime: number
}

declare interface ConversionTagAtt {
  /**
   * 是否显示
   */
  show: boolean
  /**
   * 文本
   */
  text: string
  /**
   * 精度
   */
  precision: number
}

declare interface ColumnNode {
  key: string
  children?: Columns
}

declare type Columns = Array<ColumnNode>

declare interface BulletAtt {
  bar: BulletAttr<BulletBarAtt>
}
declare interface BulletBarAtt {
  /**
   * 背景区间类型
   */
  showType?: 'dynamic' | 'fixed'
  /**
   * 自定义子弹背景区间
   */
  fixedRange?: BulletBarAtt[]
  /**
   * 自定义区间个数
   */
  fixedRangeNumber?: number
  /**
   * 自定义子弹背景区间分界值
   */
  fixedRangeValue?: number
  /**
   * 显示名称
   */
  name?: string
  /**
   * 当目标值为固定值时，显示目标值
   */
  value?: number
  /**
   * 子弹图形形状
   */
  symbol?: 'circle' | 'square' | 'line' | 'diamond' | 'triangle'
  symbolSize?: number
  fill?: string | string[]
  fillOpacity?: number
  radius?: number | number[]
  size?: number
}
declare interface BulletAttr<T> {
  measures?: T
  target?: T
  ranges?: T
}
