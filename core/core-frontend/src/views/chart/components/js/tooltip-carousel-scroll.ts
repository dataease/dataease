/**
 * 轮播提示超过容器高度时，完整滚动展示后再切换维度。
 * 等待边界修正完成后测量；取消时停止所有帧和计时器，不影响悬浮提示。
 */
export function scheduleTooltipCarousel(
  getTooltip: () => HTMLElement | null,
  stayTime: number,
  onComplete: () => void
): () => void {
  let frame: number | undefined
  let timer: ReturnType<typeof setTimeout> | undefined
  let cancelled = false
  let activeTooltip: HTMLElement | null = null
  const cancel = () => {
    cancelled = true
    if (frame !== undefined) cancelAnimationFrame(frame)
    if (timer !== undefined) clearTimeout(timer)
    // 暂停轮播进入悬浮模式时，从顶部阅读，不继承自动滚动的位置。
    if (activeTooltip) activeTooltip.scrollTop = 0
  }
  const finishAfter = (delay: number) => {
    timer = setTimeout(() => {
      if (!cancelled) onComplete()
    }, delay)
  }
  frame = requestAnimationFrame(() => {
    frame = requestAnimationFrame(() => {
      if (cancelled) return
      const tooltip = getTooltip()
      activeTooltip = tooltip
      if (!tooltip || !tooltip.isConnected) {
        finishAfter(stayTime)
        return
      }
      tooltip.scrollTop = 0
      const distance = tooltip.scrollHeight - tooltip.clientHeight
      if (distance <= 1) {
        finishAfter(stayTime)
        return
      }
      // 首尾均留出阅读时间，避免内容很多时为了赶上切换间隔而快速掠过。
      const pause = Math.max(800, stayTime / 2)
      const duration = (distance / 40) * 1000
      timer = setTimeout(() => {
        let start: number | undefined
        const scroll = (time: number) => {
          if (cancelled) return
          if (!tooltip.isConnected) {
            finishAfter(pause)
            return
          }
          start ??= time
          const progress = Math.min(1, (time - start) / duration)
          tooltip.scrollTop = distance * progress
          if (progress < 1) {
            frame = requestAnimationFrame(scroll)
          } else {
            finishAfter(pause)
          }
        }
        frame = requestAnimationFrame(scroll)
      }, pause)
    })
  })
  return cancel
}
