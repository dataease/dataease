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
  bounds: LabelBounds
}

const getTicks = (
  scale,
  tickFilter?: (value: unknown, index: number, values: unknown[]) => boolean
) => {
  const ticks = scale.getTicks?.() ?? scale.getOptions().domain ?? []
  return tickFilter ? ticks.filter(tickFilter) : ticks
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

const hasLabelOverlap = (labels: PositionedLabel[], margin: unknown) => {
  const [top, right, bottom, left] = parseSpacing(margin)
  let previous: PositionedLabel | undefined
  return labels.some(current => {
    if (!previous || previous.index === current.index) {
      previous = current
      return false
    }
    const first = previous.bounds
    const second = current.bounds
    previous = current
    const overlapX =
      first.x - left < second.x + second.width + right &&
      second.x - left < first.x + first.width + right
    const overlapY =
      first.y - top < second.y + second.height + bottom &&
      second.y - top < first.y + first.height + bottom
    return overlapX && overlapY
  })
}

/**
 * 在绘制前预判 autoHide 抽样后首尾标签是否仍然可见
 * 只按 keepTail 猜测会出现两类错误，隐藏尾标签仍占留白或可见尾标签被画布裁切
 * 这里只同步 G2 5.4.x 的 parity 核心抽样规则，G2 更换隐藏算法后必须同步调整
 */
const getBoundaryLabelIndexes = (component: G2GuideComponentOptions, labels: PositionedLabel[]) => {
  const tickCount = labels.length
  if (tickCount <= 1) {
    return labels.map(({ index }) => index)
  }
  const headIndex = labels[0].index
  const tailIndex = labels[tickCount - 1].index
  const hideTransform = getHideTransform(component)
  if (!hideTransform || hideTransform.keepTail) {
    return [headIndex, tailIndex]
  }

  const header = hideTransform.keepHeader ? labels[0] : undefined
  const source = header ? labels.slice(1) : [...labels]
  let visible = [...labels]
  let sequence = 2
  // 与 G2 autoHide 的 parity 抽样保持一致，避免为已隐藏尾标签留白或裁掉仍可见尾标签
  while (sequence < tickCount) {
    const candidates = header ? [header, ...visible] : visible
    if (!hasLabelOverlap(candidates, hideTransform.margin)) {
      break
    }
    visible = source.filter((_, index) => index % sequence === 0)
    sequence += 1
  }
  return visible.some(({ index }) => index === tailIndex) ? [headIndex, tailIndex] : [headIndex]
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

/**
 * 把布局阶段的单标签降级同步到 G2 实际绘制阶段
 * 只减少布局留白而不写入 labelFilter 会让尾标签继续绘制并被画布裁切
 * 需要保留原有 labelFilter 结果，避免覆盖图表自身的标签过滤约束
 */
const keepOnlyHeadLabel = (component: G2GuideComponentOptions, headIndex: number) => {
  const originalFilter =
    typeof component.labelFilter === 'function' ? component.labelFilter : undefined
  // 极端长文本无法同时完整容纳首尾标签时，固定保留首标签避免绘制结果与布局判断不一致
  component.labelFilter = (value, index, values) =>
    index === headIndex && (originalFilter?.(value, index, values) ?? true)
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
  overflow: Overflow
) => {
  const position = component.position as AxisPosition
  // 多 View 图表在自身轴配置中显式退出，公共布局层不感知具体图表类型
  if (
    !AXIS_POSITIONS.includes(position) ||
    !component.bbox ||
    component.dataeaseAxisLabelOverflow === false
  ) {
    return
  }
  const style = styleOf(component, position, theme)
  if (style.labelOpacity === 0 || style.labelFillOpacity === 0) {
    return
  }
  const scale = createScale(component, library)
  const ticks = getTicks(scale, style.tickFilter)
  // 使用 G2 自身 formatter/transform 保证测量一致；代价是密集刻度仍会计算整轴标签 BBox
  const labelBounds = computeLabelsBBox(style, scale)
  if (!ticks.length || !labelBounds?.length) {
    return
  }

  const { bbox } = component
  const showTick = style.tick !== false && style.showTick !== false
  const sameDirection = isSameDirection(style.labelDirection, style.tickDirection)
  const labelVector = getLabelVector(position, style.labelDirection)
  const positionedLabels = ticks.map((value, index): PositionedLabel | undefined => {
    const labelBBox = labelBounds[index]
    if (!labelBBox) {
      return undefined
    }
    const ratio = getTickRatio(scale, value, position, transpose)
    const labelSpacing = getAxisCallbackNumber(style.labelSpacing, value, index, ticks)
    const tickLength =
      showTick && sameDirection ? getAxisCallbackNumber(style.tickLength, value, index, ticks) : 0
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
      index,
      bounds: {
        x: x + labelBBox.x,
        y: y + labelBBox.y,
        width: labelBBox.width,
        height: labelBBox.height
      }
    }
  })
  const visibleLabels = positionedLabels.filter((label): label is PositionedLabel => !!label)
  let indexes = getBoundaryLabelIndexes(component, visibleLabels)
  if (shouldKeepOnlyHeadLabel(component, visibleLabels, position, layout)) {
    const headIndex = visibleLabels[0].index
    keepOnlyHeadLabel(component, headIndex)
    indexes = [headIndex]
  }
  indexes.forEach(index => {
    const label = visibleLabels.find(item => item.index === index)
    if (label) {
      updateOverflow(overflow, label.bounds, layout.width, layout.height)
    }
  })
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
  const layout = computeG2Layout(components, options, theme, library)
  if (!layout) {
    return layout
  }
  const axisComponents = components.filter(
    component =>
      typeof component.type === 'string' &&
      component.type.startsWith('axis') &&
      AXIS_POSITIONS.includes(component.position as AxisPosition)
  )
  if (!axisComponents.length) {
    return layout
  }

  const coordinate = createCoordinate(layout, options, library)
  placeComponents(groupComponents(components), coordinate, layout)
  const transpose =
    (options.coordinates ?? []).filter(item => item.type === 'transpose').length % 2 === 1
  const overflow: Overflow = { top: 0, right: 0, bottom: 0, left: 0 }
  axisComponents.forEach(component =>
    measureAxisOverflow(component, layout, theme, library, transpose, overflow)
  )

  const rawTop = Math.max(0, Math.ceil(overflow.top))
  const rawRight = Math.max(0, Math.ceil(overflow.right))
  const rawBottom = Math.max(0, Math.ceil(overflow.bottom))
  const rawLeft = Math.max(0, Math.ceil(overflow.left))
  const viewWidth = layout.width - layout.marginLeft - layout.marginRight
  const viewHeight = layout.height - layout.marginTop - layout.marginBottom
  const [left, right] = limitCorrection(rawLeft, rawRight, layout.innerWidth, viewWidth)
  const [top, bottom] = limitCorrection(rawTop, rawBottom, layout.innerHeight, viewHeight)
  if (top + right + bottom + left === 0) {
    return layout
  }
  return {
    ...layout,
    paddingTop: layout.paddingTop + top,
    paddingRight: layout.paddingRight + right,
    paddingBottom: layout.paddingBottom + bottom,
    paddingLeft: layout.paddingLeft + left,
    innerWidth: Math.max(1, layout.innerWidth - left - right),
    innerHeight: Math.max(1, layout.innerHeight - top - bottom)
  }
}
