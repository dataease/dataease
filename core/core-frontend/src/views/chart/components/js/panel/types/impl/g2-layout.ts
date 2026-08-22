/**
 * DataEase 对 AntV G2 布局阶段的统一补充
 *
 * 这个文件不是某一种图表的绘制代码，而是在 G2 真正画图前统一计算图表四周要留多少空间
 * Vite 会把 G2 内部对 runtime/layout 的调用重定向到这里，因此当前 G2 图表都会经过 computeLayout
 *
 * 主要处理的问题
 * 1 坐标轴首尾标签、旋转标签或格式化后的长标签可能伸出画布
 * 2 分类太多时标签互相遮挡，同时刻度线和网格线没有跟着标签一起减少
 * 3 左右侧图例过宽、分页器没有预留空间、长图例文字无法查看完整内容
 * 4 左侧纵轴标题过长时被裁切，同时需要保留完整标题供悬浮提示使用
 * 5 极小画布中标签和图例可能占满全部空间，需要保证图形内容区仍然可见
 * 6 双 View 图表需要关闭或限制公共边界修正，避免左右绘图区失去对称
 *
 * 当前影响范围
 * - 普通 G2 直角坐标图会使用轴标签抽稀和边界修正，例如柱状图、折线图、面积图和散点图
 * - 开启 dataeaseSideLegendAutoLayout 的左右图例会使用宽度限制、单行省略和分页器占位
 * - 开启 dataeaseAxisTitleSafeMargin 的左轴标题会使用单行省略和额外安全边距
 * - 设置 dataeaseAxisLabelOverflow 为 false 的轴会退出公共边界修正，目前双向条形图使用这个开关
 *
 * 不影响的内容
 * - 不修改图表数据、比例尺数据范围、数据集查询、权限、联动条件和后端接口
 * - 不处理 L7 地图和 S2 表格
 * - 无普通坐标轴的 G2 图表只继续使用从 G2 原样导出的布局辅助方法
 *
 * 主流程可以简单理解为
 * 先让 G2 算一次基础布局 -> 根据真实文字大小决定省略和抽稀 -> 再补足越界空间 -> 返回最终布局
 */
import {
  computeLayout as computeG2Layout,
  computeRoughPlotSize,
  placeComponents,
  processAxisZ
} from '@antv/g2/esm/runtime/layout'
import {
  computeLabelsBBox,
  createScale,
  groupComponents,
  styleOf
} from '@antv/g2/esm/runtime/component'
import { createCoordinate } from '@antv/g2/esm/runtime/coordinate'
import type { Layout, G2Theme } from '@antv/g2/esm/runtime/types/common'
import type { G2GuideComponentOptions, G2Library, G2View } from '@antv/g2/esm/runtime/types/options'
import {
  getSideLegendMaxWidth,
  SIDE_LEGEND_DEFAULT_COL_PADDING,
  SIDE_LEGEND_MIN_LABEL_WIDTH,
  SIDE_LEGEND_NAVIGATOR_WIDTH
} from './g2-legend'

/**
 * 这个文件替换了 G2 整个 layout 模块，所以必须继续提供 G2 原本依赖的三个导出
 * 它们保持 G2 原始实现，本文件没有改变无轴复合图布局、组件放置和三维轴处理算法
 */
export { computeRoughPlotSize, placeComponents, processAxisZ }

// 这里只处理最常见的上、右、下、左四种直角坐标轴
const AXIS_POSITIONS = ['top', 'right', 'bottom', 'left'] as const
// 标签和画布边缘之间至少保留 4px，避免文字笔画贴边或被抗锯齿裁切
const SAFE_SPACING = 4
// 左轴标题除了 4px 安全距离，还需要给 G2 默认向外偏移的标题多留 8px
const LEFT_AXIS_TITLE_SAFE_MARGIN = SAFE_SPACING + 8
// 左轴标题最多占最终绘图区高度的 80%，超过后显示省略号
const AXIS_TITLE_MAX_LENGTH_RATIO = 0.8
// 相邻轴标签之间至少希望保留 6px，空间不足时按这个间隔计算抽样数量
const AXIS_LABEL_MIN_GAP = 6
// 左右纵轴顶部固定保留 12px，避免第一个刻度标签贴住画布顶部
const VERTICAL_AXIS_TOP_SAFE_PADDING = SAFE_SPACING + 8
// 边界修正最多重新计算两轮，吸收位置变化，同时避免布局反复计算
const MAX_OVERFLOW_CORRECTION_PASSES = 2
// 标签再长也要给柱、线、点等图形保留至少四分之一内容区
const MIN_CONTENT_RATIO = 1 / 4

type AxisPosition = (typeof AXIS_POSITIONS)[number]
// 记录标签分别超出画布四条边多少像素，最终转换成对应方向的 padding
type Overflow = {
  top: number
  right: number
  bottom: number
  left: number
}
type LabelBounds = {
  x: number
  y: number
  width: number
  height: number
}
// 保存一个标签的原始刻度索引、沿轴方向的位置和最终文字边界
type PositionedLabel = {
  index: number
  axisCoordinate: number
  bounds: LabelBounds
}
type AxisTickFilter = (value: unknown, index: number, values: unknown[]) => boolean
// sourceIndex 始终指向完整刻度集合，抽稀后仍能同步过滤刻度线和网格线
type AxisTick = {
  value: unknown
  sourceIndex: number
}
// 同一轮布局中一个轴只测量一次文字，后续修正复用这里的结果
type AxisMeasurement = {
  scale: any
  style: Record<string, any>
  ticks: AxisTick[]
  labelBounds: LabelBounds[]
  sampledIndexes?: Set<number>
  sampled?: boolean
}

// 保存图表原本的刻度过滤条件，公共抽稀不能覆盖业务自己设置的过滤规则
const originalAxisTickFilters = new WeakMap<G2GuideComponentOptions, AxisTickFilter | undefined>()
// 只记录由本文件接管自动隐藏的轴，用户显式配置的 transform 继续交给 G2
const managedAxisAutoHide = new WeakSet<G2GuideComponentOptions>()

/**
 * 找出需要本文件接管的左右侧分类图例
 *
 * 只有图表显式设置 dataeaseSideLegendAutoLayout 才会进入
 * 顶部和底部图例有单独的文本限制逻辑，不在这里重复处理
 * 当前设置方包括 G2ChartView 公共图例、横向条形图图例和组合图自定义图例
 */
const getSideLegendComponents = (components: G2GuideComponentOptions[]) =>
  components.filter(
    component =>
      component.type === 'legendCategory' &&
      component.dataeaseSideLegendAutoLayout === true &&
      ['left', 'right'].includes(component.position)
  )

/**
 * 得到侧边图例本轮真正使用的样式
 *
 * 先使用主题默认值，再用当前图表自己的配置覆盖，后续宽度计算才能和 G2 实际显示一致
 */
const getSideLegendStyle = (component: G2GuideComponentOptions, theme: G2Theme) => ({
  ...(theme.legendCategory ?? {}),
  ...component
})

/**
 * 把完整图例文字转成可以安全放进提示层的文本
 *
 * 主要防止图例名称中的尖括号或引号被浏览器当成 HTML
 */
const escapeLegendPoptipText = (value: unknown) =>
  `${value ?? ''}`
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')

/**
 * 清除本文件上一轮给侧边图例添加的省略配置
 *
 * resize 会复用 G2 图例组件，不清理就会继续使用旧画布算出的文字宽度
 * 只删除带 dataeaseLegendTextOverflowManaged 标记的属性，不碰图表自己提供的配置
 */
const resetManagedLegendTextOverflow = (component: G2GuideComponentOptions) => {
  if (component.dataeaseLegendTextOverflowManaged !== true) {
    return
  }
  delete component.itemLabelWordWrap
  delete component.itemLabelWordWrapWidth
  delete component.itemLabelMaxLines
  delete component.itemLabelTextOverflow
  delete component.dataeaseLegendTextOverflowManaged
}

/**
 * 把侧边图例文字限制为单行，超出可用宽度后显示省略号
 *
 * Canvas 文字没有浏览器原生 title，所以同时安装 G2 poptip，悬浮时显示完整原文
 * 只在图表没有自己配置 poptip 时补默认提示，避免覆盖业务配置
 */
const applyLegendTextOverflow = (component: G2GuideComponentOptions, labelWidth: number) => {
  component.itemLabelWordWrap = true
  component.itemLabelWordWrapWidth = labelWidth
  component.itemLabelMaxLines = 1
  component.itemLabelTextOverflow = '...'
  component.dataeaseLegendTextOverflowManaged = true
  component.poptip ??= {
    // Canvas 文字没有原生 title，悬浮时通过 G2 提示层展示完整原文
    render: ({ label }) => escapeLegendPoptipText(label),
    domStyles: {
      '.component-poptip': {
        maxWidth: '320px',
        whiteSpace: 'normal',
        wordBreak: 'break-all'
      }
    }
  }
}

/**
 * 计算侧边图例中文字真正可以使用的宽度
 *
 * 最大图例宽度需要减去图例与图表的间隔、颜色图标、文字间距和分页器区域
 * 普通侧边图例默认最多占画布宽度的 30% 且至少可用 80px，组合图可在独立图例层中覆盖比例
 * 分页时固定给导航按钮和页码留出 55px，未分页时只使用普通列间距
 * 最终至少给文字保留 24px，避免极小画布下文字区域变成 0
 */
const getSideLegendLabelWidth = (
  component: G2GuideComponentOptions,
  options: G2View,
  theme: G2Theme,
  paged: boolean
) => {
  const style = getSideLegendStyle(component, theme)
  const itemSpacing = Array.isArray(style.itemSpacing)
    ? Number(style.itemSpacing[0]) || 0
    : Number(style.itemSpacing) || 0
  const itemMarkerSize = Number(style.itemMarkerSize) || 0
  const crossPadding = Number(style.crossPadding) || 0
  const colPadding = paged
    ? SIDE_LEGEND_NAVIGATOR_WIDTH
    : Number(style.colPadding) || SIDE_LEGEND_DEFAULT_COL_PADDING
  const maxLegendWidth = getSideLegendMaxWidth(
    Number(options.width),
    Number(component.dataeaseSideLegendMaxWidthRatio)
  )
  return Math.max(
    SIDE_LEGEND_MIN_LABEL_WIDTH,
    Math.floor(maxLegendWidth - crossPadding - itemMarkerSize - itemSpacing - colPadding)
  )
}

/**
 * 为左右侧图例准备第一轮布局
 *
 * 每次 resize 都先删除旧的尺寸、行列数和分页间距，让 G2 按当前画布重新计算
 * 第一轮暂时按未分页计算文字宽度，因为此时还不知道当前高度是否真的需要分页
 * 返回的图例集合会在 G2 第一轮布局后继续判断是否需要分页器
 */
const prepareSideLegendLayout = (
  components: G2GuideComponentOptions[],
  options: G2View,
  theme: G2Theme
) => {
  const sideLegends = getSideLegendComponents(components)
  sideLegends.forEach(component => {
    resetManagedLegendTextOverflow(component)
    delete component.size
    delete component.length
    delete component.cols
    delete component.gridRow
    delete component.colPadding
    applyLegendTextOverflow(component, getSideLegendLabelWidth(component, options, theme, false))
  })
  return sideLegends
}

/**
 * 判断侧边图例是否真的出现分页
 *
 * G2 第一轮布局会算出列数和每列行数，可显示数量就是 cols 乘 gridRow
 * 图例数据量超过可显示数量时，页面上才会真正出现上一页和下一页按钮
 */
const isSideLegendPaged = (component: G2GuideComponentOptions, library: G2Library) => {
  const cols = Number(component.cols)
  const rows = Number(component.gridRow)
  if (!Number.isFinite(cols) || !Number.isFinite(rows) || cols <= 0 || rows <= 0) {
    return false
  }
  const domain = createScale(component, library).getOptions().domain ?? []
  return domain.length > cols * rows
}

/**
 * 为真正分页的侧边图例补上分页器宽度并要求 G2 再布局一次
 *
 * 未分页图例不预留 55px，避免图例和绘图区之间出现不必要的大块空白
 * 已分页图例缩短文字宽度并清除旧尺寸，让文字、省略号和分页器共同落在最大图例宽度内
 * 返回 true 表示至少一个图例发生变化，主流程需要调用 G2 再算一次布局
 */
const applyPagedSideLegendLayout = (
  sideLegends: G2GuideComponentOptions[],
  options: G2View,
  theme: G2Theme,
  library: G2Library
) => {
  let changed = false
  sideLegends.forEach(component => {
    if (!isSideLegendPaged(component, library)) {
      return
    }
    component.colPadding = SIDE_LEGEND_NAVIGATOR_WIDTH
    component.itemLabelWordWrapWidth = getSideLegendLabelWidth(component, options, theme, true)
    delete component.size
    delete component.length
    delete component.cols
    delete component.gridRow
    changed = true
  })
  return changed
}

/**
 * 限制左侧纵轴标题长度，解决标题上下两端被裁切的问题
 *
 * 只处理显式设置 dataeaseAxisTitleSafeMargin 的左轴，普通轴标题不会改变
 * 标题最多使用最终绘图区高度的 80%，超出后保持单行并显示省略号
 * 省略工作交给 G2 按真实字体宽度完成，中英文混排不会因为简单数字符而错误截断
 * 同时保存完整原文，公共悬浮提示会在标题确实省略时显示完整内容
 *
 * 当前使用场景包括折线图、区间条形图、进度条、子弹图、散点图和象限图等目标左轴
 */
const applyLeftAxisTitleOverflow = (components: G2GuideComponentOptions[], layout: Layout) => {
  // 纵轴标题旋转后沿竖直方向排布，所以可用长度取最终 Plot 高度的 80%
  const maxLength = Math.max(1, Math.floor(layout.innerHeight * AXIS_TITLE_MAX_LENGTH_RATIO))
  components.forEach(component => {
    if (
      component.position !== 'left' ||
      component.dataeaseAxisTitleSafeMargin !== true ||
      component.title === false ||
      component.title === null ||
      component.title === undefined ||
      component.title === ''
    ) {
      return
    }
    component.titleWordWrap = true
    component.titleWordWrapWidth = maxLength
    component.titleMaxLines = 1
    component.titleTextOverflow = '...'
    component.titleDataeaseOriginalText = Array.isArray(component.title)
      ? component.title.join(',')
      : `${component.title}`
  })
}

/**
 * 给标准直角坐标轴统一安装 DataEase 的标签防重叠规则
 *
 * 图表没有明确要求自动旋转时保持文字角度不变，空间不足优先等距减少标签
 * 图表明确设置 labelAutoHide 为 false 时完全尊重配置，不进行公共抽稀
 * 图表已经提供 transform 时继续采用图表自己的隐藏策略，不重复接管
 * 本文件接管的轴默认要求相邻标签保留 6px，G2 从两个标签各扩一半，所以写入 3px
 * resize 前先恢复业务原始 tickFilter，保证每轮都从完整刻度重新判断显示数量
 *
 * 影响所有使用普通上、右、下、左轴的 G2 图表，例如柱状图、折线图、面积图和散点图
 */
const applyAxisLabelOverlapDefaults = (components: G2GuideComponentOptions[]) => {
  components.forEach(component => {
    if (
      typeof component.type !== 'string' ||
      !component.type.startsWith('axis') ||
      !AXIS_POSITIONS.includes(component.position as AxisPosition)
    ) {
      return
    }
    if (component.labelAutoRotate === undefined) {
      component.labelAutoRotate = false
    }
    const transforms = Array.isArray(component.transform) ? component.transform : []
    if (component.labelAutoHide === false) {
      managedAxisAutoHide.delete(component)
      return
    }
    // 只有没有显式 transform 的轴才由公共逻辑接管，避免覆盖图表自己的隐藏策略
    if (!transforms.length) {
      managedAxisAutoHide.add(component)
    }
    if (!managedAxisAutoHide.has(component)) {
      return
    }
    // 字号或画布变化时恢复原始筛选，确保本轮从完整候选刻度重新计算
    if (!originalAxisTickFilters.has(component)) {
      originalAxisTickFilters.set(
        component,
        typeof component.tickFilter === 'function' ? component.tickFilter : undefined
      )
    }
    const originalTickFilter = originalAxisTickFilters.get(component)
    if (originalTickFilter) {
      component.tickFilter = originalTickFilter
    } else {
      delete component.tickFilter
    }
    const autoHide =
      component.labelAutoHide && typeof component.labelAutoHide === 'object'
        ? component.labelAutoHide
        : {}
    component.labelAutoHide = {
      margin: AXIS_LABEL_MIN_GAP / 2,
      ...autoHide
    }
  })
}

/**
 * 取得当前轴的完整候选刻度，并保留每个刻度在原集合中的索引
 *
 * 连续比例尺通常提供 getTicks，分类比例尺通常直接使用 domain
 * 业务原有 tickFilter 会先执行，公共抽稀只在业务允许显示的刻度中继续处理
 */
const getTicks = (
  scale,
  tickFilter?: (value: unknown, index: number, values: unknown[]) => boolean
) => {
  const ticks = scale.getTicks?.() ?? scale.getOptions().domain ?? []
  return ticks
    .map((value, sourceIndex) => ({ value, sourceIndex }))
    .filter(({ value, sourceIndex }) => tickFilter?.(value, sourceIndex, ticks) ?? true)
}

/**
 * 把一个刻度值换算成沿坐标轴的 0 到 1 位置
 *
 * 分类刻度加一半带宽后落在柱子或分类区间中心
 * 左右纵轴和转置坐标的显示方向与比例尺默认方向相反，所以需要用 1 减去原比例
 */
const getTickRatio = (scale, value: unknown, position: AxisPosition, transpose: boolean) => {
  const offset = scale.getBandWidth?.(value) / 2 || 0
  const ratio = scale.map(value) + offset
  const vertical = position === 'left' || position === 'right'
  const reverse = vertical || (transpose && !!scale.getTicks)
  return reverse ? 1 - ratio : ratio
}

/**
 * 把 0 到 1 的刻度比例换成画布像素位置
 *
 * start 是轴起点，length 是轴长度，首尾 inset 是图形内容与 Plot 边缘的内部空隙
 */
const getTickPosition = (
  ratio: number,
  start: number,
  length: number,
  insetStart: number,
  insetEnd: number
) => start + insetStart + ratio * Math.max(0, length - insetStart - insetEnd)

/**
 * 读取 G2 允许写成数字或回调函数的轴配置
 *
 * labelSpacing 和 tickLength 都可能根据刻度值动态返回，这里统一得到当前刻度的数字结果
 */
const getAxisCallbackNumber = (option, value: unknown, index: number, ticks: unknown[]) => {
  const result = typeof option === 'function' ? option(value, index, ticks) : option
  return Number(result) || 0
}

/**
 * 判断当前轴最终是否允许隐藏标签，并取得隐藏配置
 *
 * 本文件接管的自动隐藏优先使用 labelAutoHide
 * 图表显式提供 transform 时，以 transform 中的 hide 为准，不再叠加 labelAutoHide
 * 没有任何 hide 配置时返回 undefined，后续保留全部标签
 *
 * 这里遵循 G2 5.4.x 的配置优先级，升级 G2 时需要确认这条规则是否改变
 */
const getHideTransform = (component: G2GuideComponentOptions) => {
  const transforms = Array.isArray(component.transform) ? component.transform : []
  if (managedAxisAutoHide.has(component) && component.labelAutoHide) {
    return typeof component.labelAutoHide === 'object'
      ? { type: 'hide', ...component.labelAutoHide }
      : { type: 'hide' }
  }
  // 与 G2 保持一致，显式 transform 存在时不再额外合并 labelAutoHide
  if (transforms.length) {
    return transforms.find(transform => transform.type === 'hide')
  }
  if (!component.labelAutoHide) {
    return undefined
  }
  return typeof component.labelAutoHide === 'object'
    ? { type: 'hide', ...component.labelAutoHide }
    : { type: 'hide' }
}

/**
 * 把类似 CSS 的间距写法统一还原成上、右、下、左四个数字
 *
 * 支持单个数字，以及长度为 1 到 4 的数组
 * 无效输入按四边都是 0 处理
 */
const parseSpacing = (spacing: unknown): [number, number, number, number] => {
  if (typeof spacing === 'number') {
    return [spacing, spacing, spacing, spacing]
  }
  if (!Array.isArray(spacing)) {
    return [0, 0, 0, 0]
  }
  const values = spacing.map(value => Number(value) || 0)
  if (values.length === 1) {
    return [values[0], values[0], values[0], values[0]]
  }
  if (values.length === 2) {
    return [values[0], values[1], values[0], values[1]]
  }
  if (values.length === 3) {
    return [values[0], values[1], values[2], values[1]]
  }
  return [values[0], values[1], values[2], values[3]]
}

/**
 * 取得标签沿坐标轴方向真正需要的间隔
 *
 * 上下轴的标签左右排列，所以使用 left 加 right
 * 左右轴的标签上下排列，所以使用 top 加 bottom
 */
const getAxisLabelGap = (position: AxisPosition, margin: unknown) => {
  const [top, right, bottom, left] = parseSpacing(margin)
  return position === 'top' || position === 'bottom' ? left + right : top + bottom
}

/**
 * 根据标签大小和刻度间距，一次算出每隔几个标签显示一个
 *
 * 先找出沿轴方向最大的标签，再找出相邻刻度之间最小的像素距离
 * 抽样步长等于 向上取整的 标签大小加期望间隔 除以 刻度距离
 * 例如标签需要 66px、相邻刻度只有 20px，则每 4 个刻度显示一个标签
 * 如果首尾标签已经伸出画布，会先扣掉把它们移回画布需要的空间，再计算可用刻度距离
 * hide 配置要求 keepTail 时，最后一个标签即使不在等距序列中也会补回来
 *
 * 这样密集分类轴只需要顺序扫描标签，不需要反复尝试多种隐藏组合
 */
const getSampledLabels = (
  component: G2GuideComponentOptions,
  labels: PositionedLabel[],
  position: AxisPosition,
  layout: Layout
) => {
  if (labels.length <= 1) {
    return labels
  }
  const hideTransform = getHideTransform(component)
  if (!hideTransform) {
    return labels
  }
  const horizontal = position === 'top' || position === 'bottom'
  const maxLabelSize = labels.reduce(
    (size, label) => Math.max(size, horizontal ? label.bounds.width : label.bounds.height),
    0
  )
  let minTickDistance = Number.POSITIVE_INFINITY
  for (let index = 1; index < labels.length; index++) {
    const distance = Math.abs(labels[index].axisCoordinate - labels[index - 1].axisCoordinate)
    if (distance > 0) {
      minTickDistance = Math.min(minTickDistance, distance)
    }
  }
  if (!Number.isFinite(minTickDistance)) {
    return labels
  }
  const head = labels[0]
  const tail = labels[labels.length - 1]
  const axisSpan = Math.abs(tail.axisCoordinate - head.axisCoordinate)
  const canvasSize = horizontal ? layout.width : layout.height
  const headStart = horizontal ? head.bounds.x : head.bounds.y
  const tailEnd = horizontal
    ? tail.bounds.x + tail.bounds.width
    : tail.bounds.y + tail.bounds.height
  const boundaryCorrection =
    Math.max(0, SAFE_SPACING - headStart) + Math.max(0, tailEnd - canvasSize + SAFE_SPACING)
  // 首尾越界需要占用一部分轴长，先把这部分从可用刻度距离中扣掉
  const safeSpanRatio = axisSpan
    ? Math.min(1, Math.max(1, axisSpan - boundaryCorrection) / axisSpan)
    : 1
  const safeTickDistance = minTickDistance * safeSpanRatio
  const gap = getAxisLabelGap(position, hideTransform.margin)
  // 步长为 1 表示全部显示，步长为 2 表示每两个显示一个，依次类推
  const step = Math.max(1, Math.ceil((maxLabelSize + gap) / safeTickDistance))
  if (step === 1) {
    return labels
  }
  const sampled = labels.filter((_, index) => index % step === 0)
  if (hideTransform.keepTail) {
    const tail = labels[labels.length - 1]
    if (sampled[sampled.length - 1]?.index !== tail.index) {
      sampled.push(tail)
    }
  }
  return sampled
}

/**
 * 保存并返回图表原本的 tickFilter
 *
 * 公共抽稀会临时改写 tickFilter，resize 或重新布局时必须先恢复业务原始规则
 * WeakMap 不会阻止已经销毁的图表组件被浏览器回收
 */
const getOriginalAxisTickFilter = (
  component: G2GuideComponentOptions,
  currentFilter?: AxisTickFilter
) => {
  if (!originalAxisTickFilters.has(component)) {
    originalAxisTickFilters.set(
      component,
      typeof currentFilter === 'function' ? currentFilter : undefined
    )
  }
  return originalAxisTickFilters.get(component)
}

/**
 * 把最终保留的标签索引同步到轴数据
 *
 * G2 的 tickFilter 同时决定标签、刻度线和对应网格线是否出现
 * 因此抽稀后不会出现文字已经隐藏但空刻度和空网格仍然密集保留的问题
 * 没有发生抽稀时恢复图表原始过滤条件，不额外改变显示内容
 */
const applyAxisTickIndexes = (
  component: G2GuideComponentOptions,
  originalFilter: AxisTickFilter | undefined,
  indexes?: Set<number>
) => {
  if (!indexes) {
    if (originalFilter) {
      component.tickFilter = originalFilter
    } else {
      delete component.tickFilter
    }
    return
  }
  component.tickFilter = (value, index, values) =>
    indexes.has(index) && (originalFilter?.(value, index, values) ?? true)
}

/**
 * 判断极端长文本下是否只能保留第一个标签
 *
 * 如果首标签和尾标签加起来已经超过整个可用 View，它们不可能同时完整放进画布
 * 图表明确要求 keepTail 时仍保留尾标签，否则优先保留首标签和 Plot 内容区
 * 这个降级只会出现在画布很小或文本特别长的情况
 */
const shouldKeepOnlyHeadLabel = (
  component: G2GuideComponentOptions,
  labels: PositionedLabel[],
  position: AxisPosition,
  layout: Layout
) => {
  const hideTransform = getHideTransform(component)
  if (!hideTransform || hideTransform.keepTail || labels.length <= 1) {
    return false
  }
  const head = labels[0].bounds
  const tail = labels[labels.length - 1].bounds
  const horizontal = position === 'top' || position === 'bottom'
  const boundarySize = horizontal ? head.width + tail.width : head.height + tail.height
  const viewSize = horizontal
    ? layout.width - layout.marginLeft - layout.marginRight
    : layout.height - layout.marginTop - layout.marginBottom
  return boundarySize + SAFE_SPACING * 2 > viewSize
}

/**
 * 计算标签从轴线向外移动的方向
 *
 * 返回值是横向和纵向两个移动方向，后续会乘以刻度长度加文字间距
 * 上下轴主要纵向移动，左右轴主要横向移动
 */
const getLabelVector = (position: AxisPosition, direction: string) => {
  const [tx, ty] = position === 'top' || position === 'bottom' ? [1, 0] : [0, -1]
  return direction === 'positive' ? [-ty, tx] : [ty, -tx]
}

/**
 * 判断标签和刻度线是否画在轴线同一侧
 *
 * 同一侧时标签位置需要额外加上刻度线长度，不同侧时只计算文字间距
 */
const isSameDirection = (first: string, second: string) =>
  (first === 'positive' ? -1 : 1) * (second === 'positive' ? -1 : 1) === 1

/**
 * 根据一个标签的最终边界，累计画布四边分别还需要多少空间
 *
 * 例如标签最左边是 -6px，而安全距离要求 4px，则左侧需要补 10px
 * 当前所有进入公共边界修正的标准轴都会统一检查上、右、下、左四条边
 */
const updateOverflow = (
  overflow: Overflow,
  bounds: { x: number; y: number; width: number; height: number },
  width: number,
  height: number
) => {
  overflow.left = Math.max(overflow.left, SAFE_SPACING - bounds.x)
  overflow.right = Math.max(overflow.right, bounds.x + bounds.width - width + SAFE_SPACING)
  overflow.top = Math.max(overflow.top, SAFE_SPACING - bounds.y)
  overflow.bottom = Math.max(overflow.bottom, bounds.y + bounds.height - height + SAFE_SPACING)
}

/**
 * 测量一个轴最终会显示哪些标签，以及这些标签是否超出画布
 *
 * 这是轴标签处理的核心方法，执行顺序如下
 * 1 取得 G2 合并主题和图表配置后的真实轴样式
 * 2 用 G2 的比例尺取得完整刻度，用真实 formatter、字体和旋转角度测量文字边界
 * 3 根据轴的 bbox、刻度线长度和文字间距算出每个标签在画布上的实际位置
 * 4 空间不足时按等距规则抽稀，极端长文本下可能只保留第一个标签
 * 5 把抽样索引写回 tickFilter，让标签、刻度线和网格线一起减少
 * 6 只用最终可见的边界标签计算画布还需要补多少 padding
 *
 * dataeaseAxisLabelOverflow 为 false 时整个轴退出公共边界修正
 * 当前双向条形图使用这个开关，避免左右子 View 被分别挤压后失去对称
 * 透明标签不会参与测量，密集刻度会测量整轴文字，因此极端数据量下会增加布局时间
 */
const measureAxisOverflow = (
  component: G2GuideComponentOptions,
  layout: Layout,
  theme: G2Theme,
  library: G2Library,
  transpose: boolean,
  overflow: Overflow,
  measurementCache: Map<G2GuideComponentOptions, AxisMeasurement>
): boolean => {
  const position = component.position as AxisPosition
  // 多 View 图表可以在自身轴配置中退出，公共布局层不需要识别具体图表名称
  if (
    !AXIS_POSITIONS.includes(position) ||
    !component.bbox ||
    component.dataeaseAxisLabelOverflow === false
  ) {
    return false
  }
  let measurement = measurementCache.get(component)
  if (!measurement) {
    const style = styleOf(component, position, theme)
    if (style.labelOpacity === 0 || style.labelFillOpacity === 0) {
      return false
    }
    const scale = createScale(component, library)
    const originalTickFilter = getOriginalAxisTickFilter(component, style.tickFilter)
    const ticks = getTicks(scale, originalTickFilter)
    // 文字自身大小不会因为本轮 padding 修正而变化，所以同一轮只测量一次
    const measurementStyle = { ...style, tickFilter: originalTickFilter }
    const labelBounds = computeLabelsBBox(measurementStyle, scale)
    if (!ticks.length || !labelBounds?.length) {
      return false
    }
    measurement = { style: measurementStyle, scale, ticks, labelBounds }
    measurementCache.set(component, measurement)
  }
  const { style, scale, ticks, labelBounds } = measurement

  const { bbox } = component
  const showTick = style.tick !== false && style.showTick !== false
  const sameDirection = isSameDirection(style.labelDirection, style.tickDirection)
  const labelVector = getLabelVector(position, style.labelDirection)
  const tickEntries = ticks
    .map((tick, index) => ({ tick, labelBBox: labelBounds[index] }))
    .filter(
      ({ tick, labelBBox }) =>
        !!labelBBox &&
        (!measurement.sampledIndexes || measurement.sampledIndexes.has(tick.sourceIndex))
    )
  const tickValues = tickEntries.map(({ tick }) => tick.value)
  const positionedLabels = tickEntries.map(({ tick, labelBBox }, index): PositionedLabel => {
    const ratio = getTickRatio(scale, tick.value, position, transpose)
    const labelSpacing = getAxisCallbackNumber(style.labelSpacing, tick.value, index, tickValues)
    const tickLength =
      showTick && sameDirection
        ? getAxisCallbackNumber(style.tickLength, tick.value, index, tickValues)
        : 0
    const offset = tickLength + labelSpacing
    let x = 0
    let y = 0
    if (position === 'top' || position === 'bottom') {
      // 上下轴沿水平方向排列，y 落在轴组件靠近 Plot 的边缘
      x = getTickPosition(ratio, bbox.x, bbox.width, layout.insetLeft, layout.insetRight)
      y = position === 'top' ? bbox.y + bbox.height : bbox.y
    } else {
      // 左右轴沿垂直方向排列，x 落在轴组件靠近 Plot 的边缘
      x = position === 'left' ? bbox.x + bbox.width : bbox.x
      y = getTickPosition(ratio, bbox.y, bbox.height, layout.insetTop, layout.insetBottom)
    }
    x += labelVector[0] * offset
    y += labelVector[1] * offset
    return {
      index: tick.sourceIndex,
      axisCoordinate: position === 'top' || position === 'bottom' ? x : y,
      bounds: {
        x: x + labelBBox.x,
        y: y + labelBBox.y,
        width: labelBBox.width,
        height: labelBBox.height
      }
    }
  })
  const visibleLabels = positionedLabels.filter((label): label is PositionedLabel => !!label)
  let sampledIndexes = measurement.sampledIndexes
  let sampledLabels: PositionedLabel[]
  if (sampledIndexes) {
    sampledLabels = visibleLabels
  } else {
    sampledLabels = getSampledLabels(component, visibleLabels, position, layout)
    if (shouldKeepOnlyHeadLabel(component, sampledLabels, position, layout)) {
      sampledLabels = [sampledLabels[0]]
    }
    // 同一轮固定第一次抽样结果，避免补 padding 后又换一批标签造成布局反复变化
    sampledIndexes = new Set(sampledLabels.map(({ index }) => index))
    measurement.sampledIndexes = sampledIndexes
    measurement.sampled = sampledLabels.length < visibleLabels.length
  }
  const managedAutoHide = managedAxisAutoHide.has(component)
  if (managedAutoHide || sampledLabels.length === 1) {
    applyAxisTickIndexes(
      component,
      getOriginalAxisTickFilter(component),
      measurement.sampled ? sampledIndexes : undefined
    )
  }
  if (managedAutoHide) {
    // tickFilter 已经是最终结果，关闭 G2 第二次隐藏，避免测量过的标签随后又消失
    component.transform = []
    component.labelAutoHide = false
  }
  // 抽稀后中间标签不会沿轴方向超过首尾标签，只需检查最终可见的第一个和最后一个
  const boundaryLabels =
    sampledLabels.length <= 1
      ? sampledLabels
      : [sampledLabels[0], sampledLabels[sampledLabels.length - 1]]
  boundaryLabels.forEach(label =>
    updateOverflow(overflow, label.bounds, layout.width, layout.height)
  )
  return !!measurement.sampled
}

/**
 * 限制一对相反方向最多可以补多少 padding
 *
 * start 和 end 分别表示左加右或上加下的修正量
 * 如果全部补上会让 Plot 小于 View 的四分之一，就按比例缩小两边修正量
 * 这种极端情况下宁可允许特别长的标签保留少量裁切，也不能让柱、线、点完全没有空间
 */
const limitCorrection = (start: number, end: number, innerSize: number, viewSize: number) => {
  const available = Math.max(0, innerSize - Math.max(1, viewSize * MIN_CONTENT_RATIO))
  const total = start + end
  if (total <= available || total === 0) {
    return [start, end]
  }
  // 按左右或上下原本需要的比例分配剩余空间，避免只牺牲某一侧
  const ratio = available / total
  return [Math.floor(start * ratio), Math.floor(end * ratio)]
}

/**
 * 把四边越界量正式写入 Layout
 *
 * 某一边增加多少 padding，Plot 的 innerWidth 或 innerHeight 就同步减少多少
 * 最终宽高至少保留 1px，避免 G2 收到 0 或负数尺寸
 */
const applyLayoutCorrection = (
  layout: Layout,
  top: number,
  right: number,
  bottom: number,
  left: number
): Layout => ({
  ...layout,
  paddingTop: layout.paddingTop + top,
  paddingRight: layout.paddingRight + right,
  paddingBottom: layout.paddingBottom + bottom,
  paddingLeft: layout.paddingLeft + left,
  innerWidth: Math.max(1, layout.innerWidth - left - right),
  innerHeight: Math.max(1, layout.innerHeight - top - bottom)
})

/**
 * 统一生成 G2 最终使用的布局结果
 *
 * 这不是业务图表主动调用的方法，G2 在 render 和 forceFit 的布局阶段会进入这里
 * 方法先复用 G2 原生 computeLayout，再补充 DataEase 需要的图例、轴标签和轴标题处理
 * 普通图表不需要自己写固定 padding，也不需要首次 render 后测量 DOM 再 render 第二次
 *
 * 处理顺序
 * 1 安装轴标签防重叠默认规则，并找出标准直角坐标轴
 * 2 去掉重复的默认外边距，为目标左轴标题补安全空间
 * 3 重置侧边图例旧尺寸，让 resize 按当前画布重新测量
 * 4 调用 G2 原生布局得到图例行列数、轴组件大小和基础 Plot 空间
 * 5 如果侧边图例真的分页，为分页器补宽度并重新布局
 * 6 按真实文字大小决定轴标签抽稀，抽稀后再让 G2 计算一次准确轴宽
 * 7 最多两轮补足首尾标签越界空间，同时保证 Plot 至少保留四分之一
 * 8 应用纵轴顶部安全距离和左轴标题省略，返回最终结果
 *
 * 直接影响
 * - 带普通坐标轴的 G2 图表四周留白、轴标签数量、刻度线和网格线数量
 * - 开启侧边图例自动布局的 G2 图表图例宽度、省略文字、悬浮完整提示和分页器位置
 * - 开启左轴标题安全边距的图表标题长度和左侧留白
 * - 设置 dataeaseAxisLabelOverflow 为 false 后退出公共边界修正的多 View 图表
 *
 * 不直接影响
 * - mark 的数据值、柱线点形状、颜色、比例尺数据范围和后端返回结果
 * - L7 地图和 S2 表格
 *
 * 兼容性边界
 * - 依赖 G2 5.4.x 的内部布局、组件测量和坐标放置规则
 * - 升级 G2 时需要确认内部导入、component 字段、文字 BBox 和 transform 优先级
 */
export function computeLayout(
  components: G2GuideComponentOptions[],
  options: G2View,
  theme: G2Theme,
  library: G2Library
): Layout {
  // 第一步统一普通轴的防重叠策略，后续测量必须与最终隐藏规则一致
  applyAxisLabelOverlapDefaults(components)
  // 极坐标轴和其他特殊组件不进入直角坐标轴的边界修正
  const axisComponents = components.filter(
    component =>
      typeof component.type === 'string' &&
      component.type.startsWith('axis') &&
      AXIS_POSITIONS.includes(component.position as AxisPosition)
  )
  // 图表自己配置过任一 margin 时完整尊重业务值，不使用下面的 DataEase 默认值
  const hasExplicitMargin = [
    'margin',
    'marginTop',
    'marginRight',
    'marginBottom',
    'marginLeft'
  ].some(key => options[key] !== undefined)
  // 只有带非空标题并显式开启安全边距的左轴才需要额外左侧空间
  const needsLeftAxisTitleSafeMargin = axisComponents.some(
    component =>
      component.position === 'left' &&
      component.dataeaseAxisTitleSafeMargin === true &&
      component.title !== false &&
      component.title !== null &&
      component.title !== undefined &&
      component.title !== ''
  )
  // G2 自动 padding 已覆盖轴和图例尺寸，标准直角坐标图不再重复叠加默认 16px 外 margin
  const layoutOptions =
    axisComponents.length && !hasExplicitMargin
      ? {
          ...options,
          margin: 0,
          // 顶部仍保留 2px，避免靠近画布顶边的字形抗锯齿被切掉
          marginTop: 2,
          // 左轴标题默认向外偏移，目标图表额外保留 12px 安全空间
          ...(needsLeftAxisTitleSafeMargin ? { marginLeft: LEFT_AXIS_TITLE_SAFE_MARGIN } : {})
        }
      : options
  // 侧边图例第一轮先按未分页宽度处理，G2 布局后才能知道当前高度是否真的分页
  const sideLegends = prepareSideLegendLayout(components, layoutOptions, theme)
  // 保存图表原始轴尺寸，抽稀后需要恢复它们，让 G2 按更少的标签重新测量
  const originalAxisSizes = new Map(axisComponents.map(component => [component, component.size]))
  // 第一次调用 G2 原生布局，得到基础 padding、组件大小和侧边图例行列数
  let layout = computeG2Layout(components, layoutOptions, theme, library)
  if (!layout) {
    return layout
  }
  // 只有真正分页的侧边图例才补 55px 导航区，未分页图例保持紧凑
  if (applyPagedSideLegendLayout(sideLegends, layoutOptions, theme, library)) {
    const pagedLegendLayout = computeG2Layout(components, layoutOptions, theme, library)
    if (pagedLegendLayout) {
      layout = pagedLegendLayout
    }
  }
  // 没有标准轴时无需执行轴标签处理，但上面的侧边图例处理仍然有效
  if (!axisComponents.length) {
    return layout
  }

  // 多 View 图表可以对指定轴设置 false，避免公共边界修正破坏共享边界或左右对称
  const measurableAxisComponents = axisComponents.filter(
    component => component.dataeaseAxisLabelOverflow !== false
  )
  // 全部轴都退出边界测量时仍需完成左轴标题处理
  if (!measurableAxisComponents.length) {
    applyLeftAxisTitleOverflow(axisComponents, layout)
    return layout
  }

  // transpose 出现奇数次表示最终方向发生交换，标签像素位置需要按转置后的方向计算
  const transpose =
    (layoutOptions.coordinates ?? []).filter(item => item.type === 'transpose').length % 2 === 1
  // 同一轮修正复用字体测量，避免每补一次 padding 都重新测量整轴文字
  const measurementCache = new Map<G2GuideComponentOptions, AxisMeasurement>()

  // G2 基础布局只有组件大小，先实际放置组件，轴才会拥有可用于像素定位的 bbox
  const initialCoordinate = createCoordinate(layout, layoutOptions, library)
  placeComponents(groupComponents(components), initialCoordinate, layout)
  const initialOverflow: Overflow = { top: 0, right: 0, bottom: 0, left: 0 }
  const hasSampledAxis = measurableAxisComponents
    .map(component =>
      measureAxisOverflow(
        component,
        layout,
        theme,
        library,
        transpose,
        initialOverflow,
        measurementCache
      )
    )
    .some(Boolean)
  if (hasSampledAxis) {
    // 标签减少后轴宽也可能变小，恢复原始 size 后让 G2 按最终可见标签重新计算留白
    axisComponents.forEach(component => {
      const originalSize = originalAxisSizes.get(component)
      if (originalSize === undefined) {
        delete component.size
      } else {
        component.size = originalSize
      }
    })
    const sampledLayout = computeG2Layout(components, layoutOptions, theme, library)
    if (sampledLayout) {
      layout = sampledLayout
    }
  }
  const viewWidth = layout.width - layout.marginLeft - layout.marginRight
  const viewHeight = layout.height - layout.marginTop - layout.marginBottom
  let correctedLayout = layout

  // padding 增加后轴位置会跟着移动，因此最多再测两轮，让边界逐步稳定
  for (let pass = 0; pass < MAX_OVERFLOW_CORRECTION_PASSES; pass++) {
    const coordinate = createCoordinate(correctedLayout, layoutOptions, library)
    placeComponents(groupComponents(components), coordinate, correctedLayout)
    const overflow: Overflow = { top: 0, right: 0, bottom: 0, left: 0 }
    measurableAxisComponents.forEach(component =>
      measureAxisOverflow(
        component,
        correctedLayout,
        theme,
        library,
        transpose,
        overflow,
        measurementCache
      )
    )

    // 只增加正数像素，向上取整避免小数像素仍留下半个字形被裁切
    const rawTop = Math.max(0, Math.ceil(overflow.top))
    const rawRight = Math.max(0, Math.ceil(overflow.right))
    const rawBottom = Math.max(0, Math.ceil(overflow.bottom))
    const rawLeft = Math.max(0, Math.ceil(overflow.left))
    const [left, right] = limitCorrection(rawLeft, rawRight, correctedLayout.innerWidth, viewWidth)
    const [top, bottom] = limitCorrection(
      rawTop,
      rawBottom,
      correctedLayout.innerHeight,
      viewHeight
    )
    // 四边都不再越界时提前结束，不执行无意义的第二轮
    if (top + right + bottom + left === 0) {
      break
    }
    correctedLayout = applyLayoutCorrection(correctedLayout, top, right, bottom, left)
  }
  // 左右纵轴的第一个标签即使当前刚好没越界，也固定保留顶部安全距离
  const hasVisibleVerticalAxisLabel = measurableAxisComponents.some(
    component =>
      (component.position === 'left' || component.position === 'right') && component.label !== false
  )
  const currentTopCorrection = correctedLayout.paddingTop - layout.paddingTop
  const missingTopPadding = hasVisibleVerticalAxisLabel
    ? Math.max(0, VERTICAL_AXIS_TOP_SAFE_PADDING - currentTopCorrection)
    : 0
  if (missingTopPadding) {
    // 同样受四分之一 Plot 保护限制，极小画布不会为了 12px 安全区吞掉内容
    const [safeTopPadding] = limitCorrection(
      missingTopPadding,
      0,
      correctedLayout.innerHeight,
      viewHeight
    )
    correctedLayout = applyLayoutCorrection(correctedLayout, safeTopPadding, 0, 0, 0)
  }
  // 标题最大长度依赖最终 innerHeight，因此必须放在所有 padding 修正之后
  applyLeftAxisTitleOverflow(axisComponents, correctedLayout)
  return correctedLayout
}
