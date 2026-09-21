const TOOLTIP_CURSOR_GAP = 12
const TOOLTIP_EDGE_GAP = 8
const tooltipPositionBindings = new WeakMap<HTMLElement, () => void>()

export function bindMapTooltipPosition(container: HTMLElement, selector: string) {
  tooltipPositionBindings.get(container)?.()
  let frame: number | undefined
  let clientX = 0
  let clientY = 0

  const updatePosition = () => {
    frame = undefined
    if (!container.isConnected) return
    const bounds = container.getBoundingClientRect()
    if (!container.offsetWidth || !container.offsetHeight || !bounds.width || !bounds.height) return
    const scaleX = bounds.width / container.offsetWidth
    const scaleY = bounds.height / container.offsetHeight

    container.querySelectorAll<HTMLElement>(selector).forEach(element => {
      // 清除上次翻转和 L7 锚点偏移，再按当前内容的实际屏幕尺寸定位
      element.style.transform = 'none'
      const tip = element.querySelector<HTMLElement>('.l7-popup-tip')
      if (tip) tip.style.display = 'none'
      const rect = element.getBoundingClientRect()
      if (!rect.width || !rect.height) return

      let left = clientX + TOOLTIP_CURSOR_GAP
      let top = clientY + TOOLTIP_CURSOR_GAP
      if (left + rect.width > bounds.right - TOOLTIP_EDGE_GAP) {
        left = clientX - TOOLTIP_CURSOR_GAP - rect.width
      }
      if (top + rect.height > bounds.bottom - TOOLTIP_EDGE_GAP) {
        top = clientY - TOOLTIP_CURSOR_GAP - rect.height
      }
      left = Math.max(
        bounds.left + TOOLTIP_EDGE_GAP,
        Math.min(left, bounds.right - TOOLTIP_EDGE_GAP - rect.width)
      )
      top = Math.max(
        bounds.top + TOOLTIP_EDGE_GAP,
        Math.min(top, bounds.bottom - TOOLTIP_EDGE_GAP - rect.height)
      )
      // 屏幕坐标差换回图表逻辑坐标，避免大屏缩放使位移被重复缩放
      element.style.transform = `translate(${(left - rect.left) / scaleX}px, ${
        (top - rect.top) / scaleY
      }px)`
    })
  }

  const onMouseMove = (event: MouseEvent) => {
    // 鼠标进入提示内容后保持位置，便于查看和选择文本
    if (event.target instanceof Element && event.target.closest(selector)) return
    clientX = event.clientX
    clientY = event.clientY
    // 等 L7 更新本轮内容和地图坐标后再定位，每帧只处理最新鼠标位置
    if (frame === undefined) frame = window.requestAnimationFrame(updatePosition)
  }
  const cancelFrame = () => {
    if (frame !== undefined) window.cancelAnimationFrame(frame)
    frame = undefined
  }
  container.addEventListener('mousemove', onMouseMove)
  container.addEventListener('mouseleave', cancelFrame)
  // 图表重新配置时替换旧监听，避免同一容器反复叠加定位操作
  tooltipPositionBindings.set(container, () => {
    cancelFrame()
    container.removeEventListener('mousemove', onMouseMove)
    container.removeEventListener('mouseleave', cancelFrame)
  })
}
