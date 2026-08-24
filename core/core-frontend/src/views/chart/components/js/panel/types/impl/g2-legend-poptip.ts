let legendTextMeasureContext: CanvasRenderingContext2D | null | undefined
const LEGEND_POPTIP_ID = 'component-poptip'
const LEGEND_POPTIP_CONTENT_ATTRIBUTE = 'data-de-g2-legend-poptip'
const LEGEND_POPTIP_MANAGED_ATTRIBUTE = 'data-de-g2-legend-poptip-position'
const LEGEND_POPTIP_LEFT_PROPERTY = '--de-g2-legend-poptip-left'
const LEGEND_POPTIP_TOP_PROPERTY = '--de-g2-legend-poptip-top'
const LEGEND_POPTIP_GAP = 12

let pointerListenerBound = false
let positionFrame: number | undefined
let latestPointer: { x: number; y: number } | undefined

export const measureLegendTextWidth = (
  value: unknown,
  fontSize: number,
  fontFamily = 'sans-serif',
  fontWeight: string | number = 'normal'
) => {
  const label = `${value ?? ''}`
  legendTextMeasureContext ??= document.createElement('canvas').getContext('2d')
  if (legendTextMeasureContext) {
    legendTextMeasureContext.font = `${fontWeight} ${fontSize}px ${fontFamily}`
    return legendTextMeasureContext.measureText(label).width
  }
  return Array.from(label).reduce(
    (width, char) => width + fontSize * (/[^\x00-\xff]/.test(char) ? 1 : 0.6),
    0
  )
}

// 锁住图例提示的最终位置并提升到图表提示层，避免缩放跳动、事件抢占和预览遮挡
export const LEGEND_POPTIP_FOLLOW_DOM_STYLE = {
  position: 'fixed !important',
  transform: 'none !important',
  left: `var(${LEGEND_POPTIP_LEFT_PROPERTY}, -10000px) !important`,
  top: `var(${LEGEND_POPTIP_TOP_PROPERTY}, -10000px) !important`,
  'z-index': '10002 !important',
  'pointer-events': 'none !important',
  transition: 'none !important'
}

const escapeLegendPoptipText = (value: unknown) =>
  `${value ?? ''}`
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')

const resetManagedPoptipPosition = (poptip: HTMLElement) => {
  if (poptip.dataset.deG2LegendPoptipPosition !== 'true') {
    return
  }
  poptip.style.removeProperty('position')
  poptip.style.removeProperty('transform')
  poptip.style.removeProperty(LEGEND_POPTIP_LEFT_PROPERTY)
  poptip.style.removeProperty(LEGEND_POPTIP_TOP_PROPERTY)
  poptip.removeAttribute(LEGEND_POPTIP_MANAGED_ATTRIBUTE)
}

const updateLegendPoptipPosition = () => {
  positionFrame = undefined
  const poptip = document.getElementById(LEGEND_POPTIP_ID)
  const visibleLegendPoptip =
    poptip?.style.visibility !== 'hidden' &&
    poptip?.querySelector(`[${LEGEND_POPTIP_CONTENT_ATTRIBUTE}]`)
  if (!visibleLegendPoptip || !latestPointer) {
    if (poptip) {
      resetManagedPoptipPosition(poptip)
    }
    return
  }

  const { width, height } = poptip.getBoundingClientRect()
  const viewportWidth = document.documentElement.clientWidth
  const viewportHeight = document.documentElement.clientHeight
  let left = latestPointer.x + LEGEND_POPTIP_GAP
  let top = latestPointer.y + LEGEND_POPTIP_GAP
  if (left + width > viewportWidth) left = latestPointer.x - width - LEGEND_POPTIP_GAP
  if (top + height > viewportHeight) top = latestPointer.y - height - LEGEND_POPTIP_GAP

  poptip.style.setProperty(
    LEGEND_POPTIP_LEFT_PROPERTY,
    `${Math.max(0, Math.min(left, viewportWidth - width))}px`
  )
  poptip.style.setProperty(
    LEGEND_POPTIP_TOP_PROPERTY,
    `${Math.max(0, Math.min(top, viewportHeight - height))}px`
  )
  poptip.setAttribute(LEGEND_POPTIP_MANAGED_ATTRIBUTE, 'true')
}

const scheduleLegendPoptipPosition = () => {
  if (positionFrame === undefined) {
    positionFrame = window.requestAnimationFrame(updateLegendPoptipPosition)
  }
}

const ensureLegendPoptipPointerListener = () => {
  if (pointerListenerBound) return
  pointerListenerBound = true
  // 捕获原生视口坐标，避开大屏编辑层的事件拦截和缩放坐标
  document.addEventListener(
    'mousemove',
    event => {
      latestPointer = { x: event.clientX, y: event.clientY }
      const poptip = document.getElementById(LEGEND_POPTIP_ID)
      const visibleLegendPoptip =
        poptip?.style.visibility !== 'hidden' &&
        poptip?.querySelector(`[${LEGEND_POPTIP_CONTENT_ATTRIBUTE}]`)
      if (!visibleLegendPoptip && poptip?.dataset.deG2LegendPoptipPosition !== 'true') {
        return
      }
      scheduleLegendPoptipPosition()
    },
    { capture: true, passive: true }
  )
}

// 大屏编辑画布会拦截冒泡事件，需要在首次悬浮前提前注册捕获监听
export const prepareLegendPoptip = () => ensureLegendPoptipPointerListener()

export const renderLegendPoptipText = (value: unknown) => {
  prepareLegendPoptip()
  scheduleLegendPoptipPosition()
  return `<span ${LEGEND_POPTIP_CONTENT_ATTRIBUTE}>${escapeLegendPoptipText(value)}</span>`
}
