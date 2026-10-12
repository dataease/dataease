/**
 * 轮播指定类别优先，其次采用实际命中的柱，最后按横轴类别吸附并补齐同类别数据
 */
export function resolveMixTooltipCategory<T extends { field?: unknown; value?: unknown }>(
  event,
  view,
  title: string,
  items: T[] = [],
  sourceItems: T[] = items
) {
  const scale = view?.scale?.x
  const domain = scale?.getOptions?.().domain ?? []
  let field = event?._x
  if (!domain.includes(field)) {
    let target = event?.target
    while (target && target.className !== 'plot') {
      if (target.markType === 'interval') {
        field =
          target.__data__?.data?.field ?? target.__data__?.items?.find(item => item.left)?.field
        break
      }
      target = target.parentNode
    }
  }
  if (!domain.includes(field)) {
    const {
      x = 0,
      y = 0,
      paddingLeft = 0,
      paddingTop = 0,
      marginLeft = 0,
      marginTop = 0
    } = view?.layout ?? {}
    if (!Number.isFinite(event?.offsetX) || !Number.isFinite(event?.offsetY) || !domain.length) {
      return {
        title,
        items: domain.includes(title) ? sourceItems.filter(item => item.field === title) : items
      }
    }
    const [position] = view.coordinate.invert([
      event.offsetX - x - paddingLeft - marginLeft,
      event.offsetY - y - paddingTop - marginTop
    ])
    const halfBand = (scale.getBandWidth?.() ?? 0) / 2
    field = domain.reduce((nearest, candidate) =>
      Math.abs(scale.map(candidate) + halfBand - position) <
      Math.abs(scale.map(nearest) + halfBand - position)
        ? candidate
        : nearest
    )
  }
  return { title: String(field), items: sourceItems.filter(item => item.field === field) }
}

const mixCrosshairStates = new WeakMap<object, { layout: object; visible: boolean }>()

/**
 * G2 完成原生定位线绘制后，仅在当前类别有有效折线提示项时显示
 */
export function updateMixTooltipCrosshair(chart, view, items: { left?: boolean }[]) {
  const state = { layout: view?.layout, visible: items.some(item => !item.left) }
  if (mixCrosshairStates.has(chart)) {
    mixCrosshairStates.set(chart, state)
    return
  }
  mixCrosshairStates.set(chart, state)
  chart.on('tooltip:show', event => {
    if (!event.nativeEvent) return
    const current = mixCrosshairStates.get(chart)
    const plots = chart.getContext().canvas?.document?.getElementsByClassName('plot') ?? []
    for (const plot of plots) {
      if (plot.__data__ === current.layout && plot.ruleY) {
        plot.ruleY.style.visibility = current.visible ? 'visible' : 'hidden'
      }
    }
  })
}
