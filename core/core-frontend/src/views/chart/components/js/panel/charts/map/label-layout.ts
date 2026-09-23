import type { Scene } from '@antv/l7-scene'

type Point = [number, number]
type Polygon = Point[][]
export interface MapLabel {
  name: string
  x: number
  y: number
  polygons?: Polygon[]
  point?: boolean
}
interface Rect {
  x: number
  y: number
  width: number
  height: number
}
const NS = 'http://www.w3.org/2000/svg'
const overlap = (a: Rect, b: Rect) =>
  a.x < b.x + b.width + 4 &&
  a.x + a.width + 4 > b.x &&
  a.y < b.y + b.height + 4 &&
  a.y + a.height + 4 > b.y

function inRing([x, y]: Point, ring: Point[]) {
  let inside = false
  for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
    const [xi, yi] = ring[i]
    const [xj, yj] = ring[j]
    if (yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) inside = !inside
  }
  return inside
}
const inPolygons = (p: Point, polygons: Polygon[]) =>
  polygons.some(rings => inRing(p, rings[0]) && !rings.slice(1).some(r => inRing(p, r)))

export function geometryPolygons(geometry: any): Polygon[] {
  if (geometry?.type === 'Polygon') return [geometry.coordinates]
  if (geometry?.type === 'MultiPolygon') return geometry.coordinates
  return []
}

// 保留有效的地图标签点；质心落在区域外时，从区域内部选择靠近原点的位置。
export function interiorAnchor(preferred: Point, polygons: Polygon[]): Point {
  if (!polygons.length || inPolygons(preferred, polygons)) return preferred
  let result: Point | undefined
  let distance = Infinity
  polygons.forEach(rings => {
    const ring = rings[0]
    const ys = ring.map(p => p[1])
    const min = ys.reduce((a, b) => Math.min(a, b), Infinity)
    const max = ys.reduce((a, b) => Math.max(a, b), -Infinity)
    for (let step = 1; step < 16; step++) {
      const y = min + ((max - min) * step) / 16
      const xs: number[] = []
      rings.forEach(r => {
        for (let i = 0, j = r.length - 1; i < r.length; j = i++) {
          const [ax, ay] = r[j]
          const [bx, by] = r[i]
          if (ay > y !== by > y) xs.push(ax + ((y - ay) * (bx - ax)) / (by - ay))
        }
      })
      xs.sort((a, b) => a - b)
      for (let i = 0; i + 1 < xs.length; i++) {
        const point: Point = [(xs[i] + xs[i + 1]) / 2, y]
        const d = Math.hypot(point[0] - preferred[0], point[1] - preferred[1])
        if (inPolygons(point, [rings]) && d < distance) {
          result = point
          distance = d
        }
      }
    }
  })
  return result ?? preferred
}

export function placeMapLabel(
  anchor: Point,
  size: [number, number],
  viewport: [number, number],
  occupied: Rect[],
  polygons: Polygon[],
  fullDisplay: boolean
): Rect | undefined {
  const [width, height] = size
  const rectAt = ([x, y]: Point): Rect => ({ x: x - width / 2, y: y - height / 2, width, height })
  const within = (r: Rect) =>
    r.x >= 4 && r.y >= 4 && r.x + width <= viewport[0] - 4 && r.y + height <= viewport[1] - 4
  const fitsArea = (r: Rect) =>
    !polygons.length ||
    [0, 0.5, 1].every(x =>
      [0, 0.5, 1].every(y => inPolygons([r.x + x * width, r.y + y * height], polygons))
    )
  const base = rectAt(anchor)
  if (within(base) && fitsArea(base) && !occupied.some(r => overlap(r, base))) return base
  // 限制候选位置在锚点附近，避免为了全量显示把标签推到遥远区域。
  const candidates: Rect[] = []
  for (const gap of [8, 20, 36]) {
    const dx = width / 2 + gap
    const dy = height / 2 + gap
    for (const [x, y] of [
      [0, -dy],
      [dx, 0],
      [-dx, 0],
      [0, dy],
      [dx, -dy],
      [-dx, -dy],
      [0, -height - gap],
      [width + gap, 0],
      [-width - gap, 0],
      [0, height + gap]
    ]) {
      if (Math.hypot(x, y) <= 160) candidates.push(rectAt([anchor[0] + x, anchor[1] + y]))
    }
  }
  const available = candidates.filter(r => within(r) && !occupied.some(o => overlap(o, r)))
  const placed = available.find(fitsArea) ?? available[0]
  if (placed) return placed
  if (!fullDisplay) return undefined
  // 全量模式保留数据；空间不足时允许重叠，但不隐藏标签或无限延长引导线。
  return {
    ...base,
    x: Math.max(4, Math.min(base.x, viewport[0] - width - 4)),
    y: Math.max(4, Math.min(base.y, viewport[1] - height - 4))
  }
}

export function attachMapLabels(
  scene: Scene,
  labels: MapLabel[],
  style: { fontSize: number; color: string; fullDisplay: boolean; fontFamily?: string }
) {
  const map = scene.map
  const host = map.getContainer() as HTMLElement
  const svg = document.createElementNS(NS, 'svg')
  svg.classList.add('de-map-labels')
  svg.setAttribute('aria-hidden', 'true')
  Object.assign(svg.style, {
    position: 'absolute',
    // L7 画布为 z-index: 2，标签需显示在画布上方、提示框（5）下方。
    zIndex: '3',
    inset: '0',
    width: '100%',
    height: '100%',
    pointerEvents: 'none',
    overflow: 'hidden'
  })
  host.appendChild(svg)
  const connectors = document.createElementNS(NS, 'g')
  svg.appendChild(connectors)
  const fontSize = Number(style.fontSize) || 12
  const gap = Math.min(8, Math.max(3, fontSize * 0.15))
  const items = labels
    .filter(l => l.name && Number.isFinite(l.x) && Number.isFinite(l.y))
    .map(label => {
      const line = document.createElementNS(NS, 'line')
      line.setAttribute('stroke', style.color)
      line.setAttribute('stroke-width', '1')
      line.setAttribute('stroke-opacity', '0.65')
      const group = document.createElementNS(NS, 'g')
      group.classList.add('de-map-label')
      group.setAttribute('fill', style.color)
      group.setAttribute('font-family', style.fontFamily || 'sans-serif')
      group.setAttribute('font-size', String(fontSize))
      group.setAttribute('font-weight', 'bold')
      connectors.appendChild(line)
      svg.appendChild(group)
      String(label.name)
        .split('\n')
        .forEach(value => {
          const text = document.createElementNS(NS, 'text')
          text.textContent = value
          text.setAttribute('text-anchor', 'middle')
          group.appendChild(text)
        })
      const box = group.getBBox()
      return {
        label,
        line,
        group,
        box,
        anchor: interiorAnchor([label.x, label.y], label.polygons || [])
      }
    })
  let needsMeasure = true
  const measure = () => {
    items.forEach(item => {
      item.group.style.display = ''
      let y = 0
      item.group.querySelectorAll('text').forEach(text => {
        text.setAttribute('y', '0')
        const box = text.getBBox()
        text.setAttribute('y', String(y - box.y))
        y += box.height + gap
      })
      item.box = item.group.getBBox()
    })
    needsMeasure = false
  }
  let frame = 0
  let destroyed = false
  const project = (p: Point): Point => {
    const point = scene.lngLatToContainer(p)
    return [point.x, point.y]
  }
  const draw = () => {
    frame = 0
    if (destroyed) return
    if (needsMeasure) measure()
    const viewport: Point = [host.clientWidth, host.clientHeight]
    const occupied: Rect[] = []
    items.forEach(({ label, line, group, box, anchor }) => {
      const point = project(anchor)
      const visible =
        point.every(Number.isFinite) &&
        point[0] >= 0 &&
        point[0] <= viewport[0] &&
        point[1] >= 0 &&
        point[1] <= viewport[1]
      // 屏幕外的标签无需逐点投影边界，避免缩放、平移时产生无用计算。
      const polygons = visible
        ? (label.polygons || []).map(rings => rings.map(ring => ring.map(project)))
        : []
      const rect = visible
        ? placeMapLabel(
            point,
            [box.width, box.height],
            viewport,
            label.point
              ? [...occupied, { x: point[0] - 5, y: point[1] - 5, width: 10, height: 10 }]
              : occupied,
            polygons,
            style.fullDisplay
          )
        : undefined
      group.style.display = rect ? '' : 'none'
      line.style.display = 'none'
      if (!rect) return
      occupied.push(rect)
      group.setAttribute('transform', `translate(${rect.x - box.x},${rect.y - box.y})`)
      const end: Point = [
        Math.max(rect.x, Math.min(point[0], rect.x + rect.width)),
        Math.max(rect.y, Math.min(point[1], rect.y + rect.height))
      ]
      // 只有锚点位于标签框外时绘制一条短直线，隐藏标签绝不留下孤立线条。
      const lineLength = Math.hypot(end[0] - point[0], end[1] - point[1])
      if (lineLength > 4 && lineLength <= 160) {
        line.setAttribute('x1', String(point[0]))
        line.setAttribute('y1', String(point[1]))
        line.setAttribute('x2', String(end[0]))
        line.setAttribute('y2', String(end[1]))
        line.style.display = ''
      }
    })
  }
  const schedule = () => {
    if (!frame && !destroyed) frame = requestAnimationFrame(draw)
  }
  const observer = new ResizeObserver(() => {
    needsMeasure = true
    schedule()
  })
  // 字体加载与隐藏 Tab 首次显示后重新测量，不保留不可见状态下的零尺寸。
  document.fonts?.ready.then(() => {
    if (!destroyed) {
      needsMeasure = true
      schedule()
    }
  })
  observer.observe(host)
  map.on('move', schedule)
  map.on('resize', schedule)
  const cleanup = () => {
    if (destroyed) return
    destroyed = true
    scene.off('destroy', cleanup)
    cancelAnimationFrame(frame)
    observer.disconnect()
    map.off('move', schedule)
    map.off('resize', schedule)
    svg.remove()
  }
  scene.on('destroy', cleanup)
  draw()
  return cleanup
}
