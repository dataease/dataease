import { imgUrlTrans } from '@/utils/imgUtils'
import type {
  CommonBackground,
  CornerValues,
  EdgeValues
} from '@/components/visualization/component-background/Types'
import { ShorthandMode } from '@/Types'

/**
 * 判断是否启用背景图模糊
 * @param commonBackground 组件背景配置
 * @returns 是否启用背景图模糊
 */
export function isBlurBgEnabled(commonBackground: CommonBackground | undefined): boolean {
  if (!commonBackground) {
    return false
  }
  const { backdropFilterEnable, backgroundImageEnable, backgroundType, outerImage } =
    commonBackground
  return (
    backdropFilterEnable === true &&
    backgroundImageEnable === true &&
    backgroundType === 'outerImage' &&
    typeof outerImage === 'string'
  )
}

/**
 * 获取边距样式值
 * @param edgeValues 边距配置
 * @param scale 缩放比例
 * @returns CSS 边距样式值
 */
export function getEdgeValuesStyle(edgeValues: EdgeValues | number | undefined, scale = 1): string {
  if (edgeValues === undefined) {
    return '0px'
  }
  if (typeof edgeValues === 'number') {
    return `${edgeValues * scale}px`
  }
  const mode = edgeValues.mode
  const top = (edgeValues.top ?? 0) * scale
  const right = (edgeValues.right ?? 0) * scale
  const bottom = (edgeValues.bottom ?? 0) * scale
  const left = (edgeValues.left ?? 0) * scale
  if (mode === ShorthandMode.Uniform) {
    return `${top}px`
  } else if (mode === ShorthandMode.Axis) {
    return `${top}px ${left}px`
  } else if (mode === ShorthandMode.PerEdge) {
    return `${top}px ${right}px ${bottom}px ${left}px`
  }
  return `${top}px`
}

/**
 * 获取圆角样式值
 * @param cornerValues 圆角配置
 * @param scale 缩放比例
 * @returns CSS 圆角样式值
 */
export function getCornerValuesStyle(
  cornerValues: CornerValues | number | undefined,
  scale = 1
): string {
  if (cornerValues === undefined) {
    return '0px'
  }
  if (typeof cornerValues === 'number') {
    return `${cornerValues * scale}px`
  }
  const mode = cornerValues.mode
  const topLeft = (cornerValues.topLeft ?? 0) * scale
  const topRight = (cornerValues.topRight ?? 0) * scale
  const bottomLeft = (cornerValues.bottomLeft ?? 0) * scale
  const bottomRight = (cornerValues.bottomRight ?? 0) * scale
  if (mode === ShorthandMode.Uniform) {
    return `${topLeft}px`
  } else if (mode === ShorthandMode.Axis) {
    return `${topLeft}px ${bottomLeft}px`
  } else if (mode === ShorthandMode.PerEdge) {
    return `${topLeft}px ${topRight}px ${bottomRight}px ${bottomLeft}px`
  }
  return `${topLeft}px`
}

/**
 * 生成背景模糊层样式
 * @param commonBackground 组件背景配置
 * @param scale 缩放比例
 * @returns CSS 样式对象
 */
export function getBlurBgStyle(
  commonBackground: CommonBackground | undefined,
  scale = 1
): Record<string, string> {
  if (!isBlurBgEnabled(commonBackground)) {
    return {}
  }
  const { outerImage, backdropFilter, borderRadius } = commonBackground
  return {
    position: 'absolute',
    inset: '0',
    background: `url(${imgUrlTrans(outerImage!)}) no-repeat center/cover`,
    filter: `blur(${backdropFilter ?? 0}px)`,
    borderRadius: getCornerValuesStyle(borderRadius, scale),
    pointerEvents: 'none'
  }
}

/**
 * 生成组件背景样式
 * @param commonBackground 组件背景配置
 * @param options 配置选项
 * @returns CSS 样式对象
 */
export function getComponentBackgroundStyle(
  commonBackground: CommonBackground | undefined,
  options: {
    scale?: number
    isUserView?: boolean
    forceNoPadding?: boolean
  } = {}
): Record<string, string> {
  if (!commonBackground) {
    return {}
  }
  const {
    backdropFilterEnable,
    backdropFilter,
    backgroundColorSelect,
    backgroundColor,
    backgroundImageEnable,
    backgroundType,
    outerImage,
    innerPadding,
    borderRadius
  } = commonBackground
  const { scale = 1, isUserView = false, forceNoPadding = false } = options
  const style: Record<string, string> = {
    padding: forceNoPadding ? '0px' : getEdgeValuesStyle(innerPadding, scale),
    borderRadius: getCornerValuesStyle(borderRadius, scale)
  }
  // 背景色
  let colorRGBA = ''
  if (backgroundColorSelect && backgroundColor) {
    colorRGBA = backgroundColor
  }
  // 背景图
  const blurEnabled = isBlurBgEnabled(commonBackground)
  if (backgroundImageEnable) {
    if (blurEnabled) {
      style['background-color'] = colorRGBA
    } else if (backgroundType === 'outerImage' && typeof outerImage === 'string') {
      style['background'] = `url(${imgUrlTrans(outerImage)}) no-repeat ${colorRGBA}`
    } else {
      style['background-color'] = colorRGBA
    }
  } else {
    style['background-color'] = colorRGBA
  }
  // 溢出处理
  if (!isUserView) {
    style['overflow'] = 'hidden'
  }
  if (backdropFilterEnable && !blurEnabled) {
    style['backdrop-filter'] = `blur(${backdropFilter ?? 0}px)`
  }
  return style
}
