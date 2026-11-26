interface CanvasComponent {
  id?: string | number
  component?: string
  innerType?: string
  propValue?: unknown
  linkageFilters?: { sourceViewId?: string | number }[]
}

export function forEachCanvasComponent(
  components: CanvasComponent[],
  visit: (component: CanvasComponent) => void
) {
  if (!Array.isArray(components)) return
  components.forEach(component => {
    if (!component) return
    visit(component)
    if (!Array.isArray(component.propValue)) return
    if (component.component === 'Group') {
      forEachCanvasComponent(component.propValue, visit)
    } else if (['DeTabs', 'DeScreen'].includes(component.component)) {
      component.propValue.forEach(tab => forEachCanvasComponent(tab?.componentData, visit))
    }
  })
}

export function clearCanvasLinkageFilters(components: CanvasComponent[]) {
  forEachCanvasComponent(components, component => {
    if (component.linkageFilters?.length) {
      component.linkageFilters = []
    }
  })
}

export function clearCanvasViewLinkageFilters(
  views: Record<string, { chartExtRequest?: { linkageFilters?: unknown[] } }>
) {
  Object.values(views || {}).forEach(view => {
    if (view?.chartExtRequest?.linkageFilters?.length) {
      view.chartExtRequest.linkageFilters = []
    }
  })
}
