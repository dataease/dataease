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

export { computeRoughPlotSize, placeComponents, processAxisZ }

const AXIS_POSITIONS = ['top', 'right', 'bottom', 'left'] as const
const SAFE_SPACING = 4
const LEFT_AXIS_TITLE_SAFE_MARGIN = SAFE_SPACING + 8
const AXIS_TITLE_MAX_LENGTH_RATIO = 0.8
const AXIS_LABEL_MIN_GAP = 6
const VERTICAL_AXIS_TOP_SAFE_PADDING = SAFE_SPACING + 8
const MAX_OVERFLOW_CORRECTION_PASSES = 2
// 保证边界修正后仍保留至少四分之一的 Plot 内容区，避免超长标签吞掉全部图形区域
const MIN_CONTENT_RATIO = 1 / 4

type AxisPosition = (typeof AXIS_POSITIONS)[number]
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
type PositionedLabel = {
  index: number
  axisCoordinate: number
  bounds: LabelBounds
}
type AxisTickFilter = (value: unknown, index: number, values: unknown[]) => boolean
type AxisTick = {
  value: unknown
  sourceIndex: number
}
type AxisMeasurement = {
  scale: any
  style: Record<string, any>
  ticks: AxisTick[]
  labelBounds: LabelBounds[]
  sampledIndexes?: Set<number>
  sampled?: boolean
}

const originalAxisTickFilters = new WeakMap<G2GuideComponentOptions, AxisTickFilter | undefined>()
const managedAxisAutoHide = new WeakSet<G2GuideComponentOptions>()

/**
 * 左轴标题保持单行，并按最终 Plot 高度限制长度。
 * G2 Text 原生负责真实字形测量和省略，避免中英文混排时按字符数误截断。
 */
const applyLeftAxisTitleOverflow = (components: G2GuideComponentOptions[], layout: Layout) => {
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
 * 对齐 G2Plot 的轴标签策略：默认关闭自动旋转，并以 6px 间距等距抽稀
 * G2 5 会同时扩张相邻标签的检测边界，因此单侧使用一半间距
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
    // 记录 G2 自动布局前的来源，避免内部补写 hide transform 后被误判为用户显式配置
    if (!transforms.length) {
      managedAxisAutoHide.add(component)
    }
    if (!managedAxisAutoHide.has(component)) {
      return
    }
    // 字号或画布变化时先移除上一轮抽样，确保本轮始终从完整候选刻度重新计算
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

const getTicks = (
  scale,
  tickFilter?: (value: unknown, index: number, values: unknown[]) => boolean
) => {
  const ticks = scale.getTicks?.() ?? scale.getOptions().domain ?? []
  return ticks
    .map((value, sourceIndex) => ({ value, sourceIndex }))
    .filter(({ value, sourceIndex }) => tickFilter?.(value, sourceIndex, ticks) ?? true)
}

const getTickRatio = (scale, value: unknown, position: AxisPosition, transpose: boolean) => {
  const offset = scale.getBandWidth?.(value) / 2 || 0
  const ratio = scale.map(value) + offset
  const vertical = position === 'left' || position === 'right'
  const reverse = vertical || (transpose && !!scale.getTicks)
  return reverse ? 1 - ratio : ratio
}

const getTickPosition = (
  ratio: number,
  start: number,
  length: number,
  insetStart: number,
  insetEnd: number
) => start + insetStart + ratio * Math.max(0, length - insetStart - insetEnd)

const getAxisCallbackNumber = (option, value: unknown, index: number, ticks: unknown[]) => {
  const result = typeof option === 'function' ? option(value, index, ticks) : option
  return Number(result) || 0
}

/**
 * 还原当前轴最终会交给 G2 的 hide transform 配置
 * 显式 transform 会覆盖 labelAutoHide，布局预判必须遵守与 G2 inferLabelOverlap 相同的优先级
 * 依赖 G2 5.4.x 的 transform 合并规则，升级 G2 后需要复核优先级
 */
const getHideTransform = (component: G2GuideComponentOptions) => {
  const transforms = Array.isArray(component.transform) ? component.transform : []
  if (managedAxisAutoHide.has(component) && component.labelAutoHide) {
    return typeof component.labelAutoHide === 'object'
      ? { type: 'hide', ...component.labelAutoHide }
      : { type: 'hide' }
  }
  // 与 G2 inferLabelOverlap 保持一致：显式 transform 存在时不再合并 labelAutoHide
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

const getAxisLabelGap = (position: AxisPosition, margin: unknown) => {
  const [top, right, bottom, left] = parseSpacing(margin)
  return position === 'top' || position === 'bottom' ? left + right : top + bottom
}

/**
 * 对齐 G2Plot：根据最大标签投影、最小刻度间距和安全间隔一次算出抽样步长
 * 相比逐级尝试 parity 序列，密集分类轴从最坏平方复杂度降为线性复杂度
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
  // 预留首尾标签进入 Canvas 所需空间，抽样直接按最终可用轴长计算
  const safeSpanRatio = axisSpan
    ? Math.min(1, Math.max(1, axisSpan - boundaryCorrection) / axisSpan)
    : 1
  const safeTickDistance = minTickDistance * safeSpanRatio
  const gap = getAxisLabelGap(position, hideTransform.margin)
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
 * 将抽样索引同步到真实轴数据，同时减少标签、刻度线和对应网格线
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
 * 判断极端长文本是否只能保留首标签
 * 首尾标签总尺寸超过可用视图时，即使当前互不重叠也无法同时完整显示
 * 此降级优先保证 Plot 可读性，未显式 keepTail 的尾标签可能被主动隐藏
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

const getLabelVector = (position: AxisPosition, direction: string) => {
  const [tx, ty] = position === 'top' || position === 'bottom' ? [1, 0] : [0, -1]
  return direction === 'positive' ? [-ty, tx] : [ty, -tx]
}

const isSameDirection = (first: string, second: string) =>
  (first === 'positive' ? -1 : 1) * (second === 'positive' ? -1 : 1) === 1

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
 * 按 G2 formatter 与 transform 的真实结果计算轴标签边界溢出
 * 字体大小、旋转角度和格式化文本都会改变 BBox，不能只依据配置值估算留白
 * 密集刻度会测量整轴标签 BBox，极端数据量下会增加单次布局计算开销
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
  // 多 View 图表在自身轴配置中显式退出，公共布局层不感知具体图表类型
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
    // 字体度量与布局尺寸无关，同一轮布局收敛中只计算一次
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
      x = getTickPosition(ratio, bbox.x, bbox.width, layout.insetLeft, layout.insetRight)
      y = position === 'top' ? bbox.y + bbox.height : bbox.y
    } else {
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
    // 同一轮收敛固定首次抽样结果，避免边界修正后更换末端标签并遗留陈旧 padding
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
    // tickFilter 已是最终抽样结果，关闭 G2 二次 hide，避免预留空间对应的标签再次消失
    component.transform = []
    component.labelAutoHide = false
  }
  const boundaryLabels =
    sampledLabels.length <= 1
      ? sampledLabels
      : [sampledLabels[0], sampledLabels[sampledLabels.length - 1]]
  boundaryLabels.forEach(label =>
    updateOverflow(overflow, label.bounds, layout.width, layout.height)
  )
  return !!measurement.sampled
}

const limitCorrection = (start: number, end: number, innerSize: number, viewSize: number) => {
  const available = Math.max(0, innerSize - Math.max(1, viewSize * MIN_CONTENT_RATIO))
  const total = start + end
  if (total <= available || total === 0) {
    return [start, end]
  }
  // 风险取舍：极小画布优先保留 Plot 内容区，必要时允许未自动隐藏的超长标签保留少量裁切
  const ratio = available / total
  return [Math.floor(start * ratio), Math.floor(end * ratio)]
}

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
 * 在 G2 最终绘制前修正旋转轴标签边界，避免业务层 render 后再次测量和重绘
 * 仅处理标准直角坐标轴；多 View 共享边界可在轴配置中设置 dataeaseAxisLabelOverflow: false
 * 风险：依赖 G2 5.4.x 内部布局结构，升级 G2 时必须复核导入路径和 BBox 语义
 */
export function computeLayout(
  components: G2GuideComponentOptions[],
  options: G2View,
  theme: G2Theme,
  library: G2Library
): Layout {
  applyAxisLabelOverlapDefaults(components)
  const axisComponents = components.filter(
    component =>
      typeof component.type === 'string' &&
      component.type.startsWith('axis') &&
      AXIS_POSITIONS.includes(component.position as AxisPosition)
  )
  const hasExplicitMargin = [
    'margin',
    'marginTop',
    'marginRight',
    'marginBottom',
    'marginLeft'
  ].some(key => options[key] !== undefined)
  const needsLeftAxisTitleSafeMargin = axisComponents.some(
    component =>
      component.position === 'left' &&
      component.dataeaseAxisTitleSafeMargin === true &&
      component.title !== false &&
      component.title !== null &&
      component.title !== undefined &&
      component.title !== ''
  )
  // G2 自动 padding 已覆盖轴和图例尺寸，标准直角坐标图无需再叠加默认 16px 外 margin
  const layoutOptions =
    axisComponents.length && !hasExplicitMargin
      ? {
          ...options,
          margin: 0,
          marginTop: 2,
          // 左轴标题默认向画布外平移 8px，额外保留安全空间避免字形上半部被裁剪
          ...(needsLeftAxisTitleSafeMargin ? { marginLeft: LEFT_AXIS_TITLE_SAFE_MARGIN } : {})
        }
      : options
  const originalAxisSizes = new Map(axisComponents.map(component => [component, component.size]))
  let layout = computeG2Layout(components, layoutOptions, theme, library)
  if (!layout) {
    return layout
  }
  if (!axisComponents.length) {
    return layout
  }

  const measurableAxisComponents = axisComponents.filter(
    component => component.dataeaseAxisLabelOverflow !== false
  )
  if (!measurableAxisComponents.length) {
    applyLeftAxisTitleOverflow(axisComponents, layout)
    return layout
  }

  const transpose =
    (layoutOptions.coordinates ?? []).filter(item => item.type === 'transpose').length % 2 === 1
  const measurementCache = new Map<G2GuideComponentOptions, AxisMeasurement>()

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
    // 首次布局只提供刻度位置，抽样后恢复自动尺寸并按真实可见标签重新计算轴留白
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

  // 可见刻度与自动轴尺寸确定后，最多两次吸收边界位置修正
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
    if (top + right + bottom + left === 0) {
      break
    }
    correctedLayout = applyLayoutCorrection(correctedLayout, top, right, bottom, left)
  }
  const hasVisibleVerticalAxisLabel = measurableAxisComponents.some(
    component =>
      (component.position === 'left' || component.position === 'right') && component.label !== false
  )
  const currentTopCorrection = correctedLayout.paddingTop - layout.paddingTop
  const missingTopPadding = hasVisibleVerticalAxisLabel
    ? Math.max(0, VERTICAL_AXIS_TOP_SAFE_PADDING - currentTopCorrection)
    : 0
  if (missingTopPadding) {
    // 纵向轴首个刻度需要固定顶部安全区，避免交互重布局后贴住 Plot 裁剪边界
    const [safeTopPadding] = limitCorrection(
      missingTopPadding,
      0,
      correctedLayout.innerHeight,
      viewHeight
    )
    correctedLayout = applyLayoutCorrection(correctedLayout, safeTopPadding, 0, 0, 0)
  }
  applyLeftAxisTitleOverflow(axisComponents, correctedLayout)
  return correctedLayout
}
