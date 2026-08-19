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
const MIN_CONTENT_RATIO = 1 / 4

type AxisPosition = (typeof AXIS_POSITIONS)[number]
type Overflow = {
  top: number
  right: number
  bottom: number
  left: number
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

const getBoundaryLabelIndexes = (component: G2GuideComponentOptions, tickCount: number) => {
  if (tickCount <= 1) {
    return [0]
  }
  const hideTransform = getHideTransform(component)
  if (!hideTransform) {
    return [0, tickCount - 1]
  }
  // G2 hide 算法始终从首标签开始抽样；尾标签只有显式 keepTail 时才能在绘制前确定可见
  return hideTransform.keepTail ? [0, tickCount - 1] : [0]
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

  // 仅处理绘制前能够确定可见的边界标签，避免为自动隐藏的尾标签错误扩充留白
  const indexes = getBoundaryLabelIndexes(component, ticks.length)
  const { bbox } = component
  const showTick = style.tick !== false && style.showTick !== false
  const sameDirection = isSameDirection(style.labelDirection, style.tickDirection)
  const labelVector = getLabelVector(position, style.labelDirection)
  indexes.forEach(index => {
    const value = ticks[index]
    const labelBBox = labelBounds[index]
    if (!labelBBox) {
      return
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
    updateOverflow(
      overflow,
      {
        x: x + labelBBox.x,
        y: y + labelBBox.y,
        width: labelBBox.width,
        height: labelBBox.height
      },
      layout.width,
      layout.height
    )
  })
}

const limitCorrection = (start: number, end: number, innerSize: number, viewSize: number) => {
  const available = Math.max(0, innerSize - Math.max(1, viewSize * MIN_CONTENT_RATIO))
  const total = start + end
  if (total <= available || total === 0) {
    return [start, end]
  }
  // 极小画布或超长标签不能吞掉全部绘图区，必要时允许标签保留少量裁切
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
