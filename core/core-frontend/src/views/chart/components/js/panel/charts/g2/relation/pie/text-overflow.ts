import type { LabelTransformComponent } from '@antv/g2/esm/runtime'

/** Post-layout text fitting; leave sector data and connector origins unchanged. */
export const CircularLabelOverflow: LabelTransformComponent<{ fullDisplay?: boolean }> =
  ({ fullDisplay = false } = {}) =>
  (labels, { layout }) => {
    const left = layout.x + (layout.marginLeft || 0) + (layout.paddingLeft || 0)
    const top = layout.y + (layout.marginTop || 0) + (layout.paddingTop || 0)
    const right = layout.x + layout.width - (layout.marginRight || 0) - (layout.paddingRight || 0)
    const bottom =
      layout.y + layout.height - (layout.marginBottom || 0) - (layout.paddingBottom || 0)
    const candidates = labels
      .map(label => {
        const text = label.querySelector('text')
        if (!text) return null
        const original = String(label.style.text ?? '')
        const isRight = label.style.textAlign !== 'end' && label.style.textAlign !== 'right'
        const available = isRight ? right - Number(label.style.x) : Number(label.style.x) - left
        const width = Math.max(1, Math.min(right - left, Math.max(36, available)))
        const measure = (value: string) => {
          label.style.text = value
          const bounds = text.getBounds()
          return bounds.max[0] - bounds.min[0]
        }
        const chars = Array.from(original.replace(/\n/g, ' '))
        const fit = (value: string) => {
          if (measure(value) <= width) return value
          const parts = Array.from(value)
          let low = 0
          let high = parts.length
          while (low < high) {
            const middle = Math.ceil((low + high) / 2)
            if (measure(parts.slice(0, middle).join('') + '...') <= width) low = middle
            else high = middle - 1
          }
          return parts.slice(0, low).join('') + '...'
        }
        const single = fit(chars.join(''))
        let first = ''
        while (chars.length && measure(first + chars[0]) <= width) first += chars.shift()
        const wrapped = chars.length ? first + '\n' + fit(chars.join('')) : first
        const place = (value: string) => {
          label.style.text = value
          const b = text.getBounds()
          const dx = Math.max(left, Math.min(b.min[0], right - (b.max[0] - b.min[0]))) - b.min[0]
          const dy = Math.max(top, Math.min(b.min[1], bottom - (b.max[1] - b.min[1]))) - b.min[1]
          // Keep the sector-side bend in world coordinates when the text moves.
          const points = label.style.connectorPoints
          if (Array.isArray(points) && points.length) {
            label.style.connectorPoints = points.map((point, index) =>
              index === 0 ? [point[0] - dx, point[1] - dy] : [...point]
            )
          }
          label.style.x = Number(label.style.x) + dx
          label.style.y = Number(label.style.y) + dy
          return text.getBounds()
        }
        const bounds = place(wrapped)
        return { label, text, single, wrapped: wrapped.includes('\n'), place, bounds }
      })
      .filter(Boolean)
    const overlaps = (a, b) =>
      a.min[0] < b.max[0] + 2 &&
      a.max[0] + 2 > b.min[0] &&
      a.min[1] < b.max[1] + 2 &&
      a.max[1] + 2 > b.min[1]
    const fallback = candidates.filter(
      (candidate, index) =>
        candidate.wrapped &&
        candidates.some(
          (other, otherIndex) => index !== otherIndex && overlaps(candidate.bounds, other.bounds)
        )
    )
    fallback.forEach(candidate => candidate.place(candidate.single))
    const accepted = []
    candidates.forEach(candidate => {
      const bounds = candidate.text.getBounds()
      const hidden = !fullDisplay && accepted.some(other => overlaps(bounds, other))
      candidate.label.style.visibility = hidden ? 'hidden' : 'visible'
      if (!hidden) accepted.push(bounds)
    })
    return labels
  }
