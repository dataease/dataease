import { G2 } from '@antv/g2plot'
import type { IShape } from '@antv/g-base'

/** Ratios are resolved by G2 against the current view width on every render. */
export function getLegendTextOverflow(position: string) {
  const side = /^(left|right)/.test(position)
  return { maxItemWidth: side ? 0.25 : 0.8, ...(side ? { maxWidthRatio: 0.3 } : {}) }
}

G2.registerGeometryLabelLayout('de-pie-all-labels', (items, labels, shapes, region) => {
  const coordinate = labels[0]?.get('coordinate')
  if (!coordinate) return
  // Reserve a slot for every item. Full display explicitly permits text overlap.
  const height = Math.abs(coordinate.start.y - coordinate.end.y)
  const halfWidth = Math.abs(coordinate.end.x - coordinate.start.x) / 2
  const layoutItems = items.map(item => {
    const textSpace = Math.min(halfWidth, Math.max(36, (item.style?.fontSize || 12) * 3))
    return {
      ...item,
      labelHeight: Math.min(item.labelHeight || 14, height / (items.length + 1)),
      // Move the line endpoint inward when necessary, keeping text and line attached.
      offsetX: Math.min(
        item.offsetX ?? 4,
        halfWidth - coordinate.getRadius() - Math.max(item.offset || 0, 4) - textSpace
      )
    }
  })
  G2.getGeometryLabelLayout('pie-spider')(layoutItems, labels, shapes, region)
  layoutItems.forEach((item, index) => {
    Object.assign(items[index], {
      x: item.x,
      y: item.y,
      labelLine: item.labelLine,
      invisible: false
    })
  })
  labels.forEach(label => label.show())
})

G2.registerGeometryLabelLayout(
  'de-pie-wrap-or-ellipsis',
  (items, labels, _shapes, _region, cfg) => {
    const fullDisplay = cfg?.fullDisplay === true
    const coordinate = labels[0]?.get('coordinate')
    if (!coordinate) return
    const minX = Math.min(coordinate.start.x, coordinate.end.x)
    const maxX = Math.max(coordinate.start.x, coordinate.end.x)
    const minY = Math.min(coordinate.start.y, coordinate.end.y)
    const maxY = Math.max(coordinate.start.y, coordinate.end.y)
    const candidates = labels
      .filter(label => label.get('visible') !== false)
      .flatMap(label => label.findAll(shape => shape.get('type') === 'text'))
      .filter((shape): shape is IShape => shape.get('type') === 'text')
      .map(shape => {
        const original = String(shape.attr('text'))
        const box = shape.getCanvasBBox()
        const right = (box.minX + box.maxX) / 2 >= coordinate.getCenter().x
        const anchor = right ? box.minX : box.maxX
        const middle = (box.minY + box.maxY) / 2
        const width = Math.max(0, right ? maxX - anchor : anchor - minX)
        const measure = (text: string) => {
          shape.attr('text', text)
          return shape.getBBox().width
        }
        const fit = (text: string) => {
          if (measure(text) <= width) return text
          if (measure('...') > width) return fullDisplay ? text : ''
          const chars = Array.from(text)
          let low = 0
          let high = chars.length
          while (low < high) {
            const middle = Math.ceil((low + high) / 2)
            if (measure(chars.slice(0, middle).join('') + '...') <= width) low = middle
            else high = middle - 1
          }
          return chars.slice(0, low).join('') + '...'
        }
        const single = fit(original.replace(/\n/g, ' '))
        let wrapped = original
        if (measure(original) > width || original.includes('\n')) {
          const chars = Array.from(original.replace(/\n/g, ' '))
          let first = ''
          while (chars.length && measure(first + chars[0]) <= width) first += chars.shift()
          wrapped = first && chars.length ? first + '\n' + fit(chars.join('')) : single
        }
        const place = (text: string) => {
          shape.attr('text', text)
          const current = shape.getCanvasBBox()
          G2.Util.translate(
            shape,
            anchor - (right ? current.minX : current.maxX),
            middle - (current.minY + current.maxY) / 2
          )
        }
        place(wrapped)
        return { shape, single, place, wrapped: wrapped.includes('\n'), box: shape.getCanvasBBox() }
      })
    // Decide against all tentative boxes before reverting any label, independent of iteration order.
    const overlaps = (a, b) =>
      a.minX < b.maxX + 2 && a.maxX + 2 > b.minX && a.minY < b.maxY + 2 && a.maxY + 2 > b.minY
    const fallback = candidates.filter(
      (candidate, index) =>
        candidate.wrapped &&
        (candidate.box.minY < minY ||
          candidate.box.maxY > maxY ||
          candidates.some(
            (other, otherIndex) => index !== otherIndex && overlaps(candidate.box, other.box)
          ))
    )
    fallback.forEach(candidate => candidate.place(candidate.single))
    if (fullDisplay) {
      const itemsById = new Map(items.map(item => [item.id, item]))
      labels.forEach(label => {
        const item = itemsById.get(label.get('id'))
        if (!item) return
        const box = label.getCanvasBBox()
        const dx = Math.max(minX, Math.min(box.minX, maxX - box.width)) - box.minX
        const dy = Math.max(minY, Math.min(box.minY, maxY - box.height)) - box.minY
        if (!dx && !dy) return
        label
          .findAll(shape => shape.get('type') === 'text')
          .filter((shape): shape is IShape => shape.get('type') === 'text')
          .forEach(shape => G2.Util.translate(shape, dx, dy))
        item.x += dx
        item.y += dy
        // Lines are rendered after layouts. Rebuild only the moved label's connector.
        if (item.labelLine && typeof item.labelLine === 'object') {
          const center = coordinate.getCenter()
          const radius = coordinate.getRadius()
          const right = item.x >= center.x
          const startX = item.x + (right ? -4 : 4)
          const bendX = center.x + (radius + 4) * Math.cos(item.angle)
          const bendY = center.y + (radius + 4) * Math.sin(item.angle)
          const endX = center.x + radius * Math.cos(item.angle)
          const endY = center.y + radius * Math.sin(item.angle)
          item.labelLine.path = `M ${startX},${item.y} L ${bendX},${item.y} L ${bendX},${bendY} L ${endX},${endY}`
        }
      })
    }
    if (!fullDisplay) {
      // Preserve labels hidden by spider; G2's hide-overlap can show them again.
      const accepted = []
      labels.forEach(label => {
        if (label.get('visible') === false) return
        const texts = label.findAll(shape => shape.get('type') === 'text')
        const boxes = texts.map(shape => shape.getCanvasBBox())
        if (
          !texts.some(shape => String(shape.attr('text')).length > 0) ||
          boxes.some(box => accepted.some(other => overlaps(box, other)))
        ) {
          label.hide()
        } else {
          accepted.push(...boxes)
        }
      })
    }
  }
)

export function getOuterPieLabelLayout(fullDisplay: boolean) {
  return {
    type: fullDisplay ? 'no' : 'spider',
    offset: 16,
    layout: [
      ...(fullDisplay ? [{ type: 'de-pie-all-labels' }] : []),
      { type: 'de-pie-wrap-or-ellipsis', cfg: { fullDisplay } }
    ]
  }
}

/** Keep the original legend name accessible without changing its filter identity. */
export function bindLegendFullName(plot: {
  on: (event: string, handler: (event?: any) => void) => void
  container: HTMLElement
}) {
  const container = plot.container
  const originalTitle = container.getAttribute('title')
  const clear = () => {
    if (originalTitle === null) container.removeAttribute('title')
    else container.setAttribute('title', originalTitle)
  }
  plot.on('legend-item:mouseenter', event => {
    const item = event?.target?.get('delegateObject')?.item
    if (item?.name != null) container.setAttribute('title', String(item.name))
  })
  plot.on('legend-item:mouseleave', clear)
  plot.on('beforedestroy', clear)
}
