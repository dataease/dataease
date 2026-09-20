import type { Chart as G2Chart, G2Spec } from '@antv/g2'
import { toLinearGradient } from '../../common/common_antv'

interface LegendItem {
  id: string | number
  label: string
  color: string
}

type LegendOptions = Record<string, any>

/** Keep all items in a separate layout box; text is inserted as text, never HTML. */
export const renderTiledLegend = (
  items: LegendItem[],
  style: LegendOptions,
  width: number,
  height: number
): HTMLElement => {
  style = { ...style.dataeaseLegendTileTextStyle, ...style }
  const side = ['left', 'right'].includes(style.dataeaseLegendTilePosition || style.position)
  const scroll = style.dataeaseLegendTileOverflow !== 'adaptive'
  const vertical = style.dataeaseLegendTileOrient === 'vertical'
  const fontSize = Number(style.itemLabelFontSize) || 12
  // Use the same fallback in DOM and canvas when the configured font is unavailable locally.
  const fontFamily = style.itemLabelFontFamily
    ? `${style.itemLabelFontFamily}, sans-serif`
    : 'sans-serif'
  const markerSize = Number(style.itemMarkerSize) || 8
  const dark = style.dataeaseLegendTileTheme === 'dark'
  const root = document.createElement('div')
  root.className = 'dataease-tiled-legend'
  root.style.setProperty('--legend-scroll-thumb', dark ? '#8b95a5' : '#909399')
  if (style.dataeaseLegendTileBox) root.style.transform = 'translate(-50%, -50%)'
  root.setAttribute('role', 'group')
  Object.assign(root.style, {
    boxSizing: 'border-box',
    display: 'grid',
    alignContent: 'flex-start',
    alignItems: 'flex-start',
    gap: '6px 16px',
    padding: '4px',
    fontFamily,
    fontWeight: style.itemLabelFontWeight ?? 'normal',
    fontStyle: style.itemLabelFontStyle || 'normal',
    fontSize: `${fontSize}px`,
    lineHeight: '1.3',
    color: style.itemLabelFill || '#333',
    width: `${Math.max(1, side ? width * (scroll ? 0.3 : 0.5) : width)}px`,
    maxHeight: `${Math.max(1, scroll && !side ? height * 0.35 : height)}px`,
    maxWidth: `${Math.max(1, side && !scroll ? width - 80 : width)}px`,
    overflow: 'auto',
    colorScheme: dark ? 'dark' : 'light',
    scrollbarColor: 'var(--legend-scroll-thumb) transparent',
    // A stable gutter keeps column wrapping independent of whether content overflows.
    scrollbarGutter: 'stable',
    scrollbarWidth: 'auto',
    // Centering overflowing tracks would hide their leading edge outside the scroll origin.
    justifyContent: 'safe ' + (style.layout?.justifyContent || 'start')
  })
  // Use shared column tracks so labels of different lengths remain aligned across rows.
  const context = document.createElement('canvas').getContext('2d')
  if (context) {
    context.font = `${style.itemLabelFontStyle || 'normal'} ${
      style.itemLabelFontWeight ?? 'normal'
    } ${fontSize}px ${fontFamily}`
  }
  const areaWidth = Math.max(1, side ? (scroll ? width * 0.3 : width - 80) : width)
  const naturalWidth = items.reduce(
    (max, item) =>
      Math.max(
        max,
        (context?.measureText(String(item.label)).width || fontSize * String(item.label).length) +
          markerSize +
          6
      ),
    markerSize + 6
  )
  // DOM text shaping can be slightly wider than canvas measurement for fallback fonts.
  const columnWidth = Math.min(Math.max(1, areaWidth - 8), Math.ceil(naturalWidth) + 2)
  // Let CSS use the actual content width, including the scrollbar gutter. Empty tracks collapse.
  root.style.gridTemplateColumns = vertical
    ? `minmax(0, ${columnWidth}px)`
    : `repeat(auto-fit, minmax(min(100%, ${columnWidth}px), ${columnWidth}px))`
  if (side && !scroll) {
    const rows = Math.max(
      1,
      Math.min(items.length, Math.floor((height - 8) / (Math.max(fontSize * 1.3, markerSize) + 6)))
    )
    Object.assign(root.style, {
      gridAutoFlow: vertical ? 'column' : 'row',
      gridAutoColumns: `${columnWidth}px`,
      ...(vertical
        ? { gridTemplateRows: `repeat(${rows}, auto)`, gridTemplateColumns: 'none' }
        : {
            gridTemplateColumns: `repeat(${Math.max(
              1,
              Math.ceil(items.length / rows)
            )}, ${columnWidth}px)`
          }),
      width: 'max-content'
    })
  }

  const syncSelection = () => {
    const selected = style.dataeaseLegendTileSelection?.()
    Array.from(root.querySelectorAll<HTMLButtonElement>('[data-legend-value]')).forEach(
      (button, index) => {
        const active = !selected || selected.includes(items[index].id)
        button.style.opacity = active ? '1' : '0.4'
        button.setAttribute('aria-pressed', String(active))
      }
    )
  }
  root.addEventListener('dataease-legend-sync', syncSelection)
  items.forEach(item => {
    const entry = document.createElement('button')
    entry.type = 'button'
    entry.setAttribute('data-legend-value', String(item.id))
    entry.title = String(item.label)
    entry.addEventListener('click', event => {
      event.stopPropagation()
      style.dataeaseLegendTileFilter?.(
        items.map(value => value.id),
        item.id
      )
      syncSelection()
    })
    Object.assign(entry.style, {
      display: 'inline-flex',
      alignItems: 'baseline',
      gap: '6px',
      border: '0',
      background: 'transparent',
      padding: '0',
      margin: '0',
      color: 'inherit',
      font: 'inherit',
      lineHeight: 'inherit',
      textAlign: 'left',
      cursor: 'pointer',
      minWidth: '0',
      maxWidth: `${Math.max(1, width - 8)}px`,
      ...(vertical && !(side && !scroll) ? { width: '100%' } : {})
    })
    const marker = document.createElement('span')
    Object.assign(marker.style, {
      display: 'inline-block',
      flex: `0 0 ${markerSize}px`,
      width: `${markerSize}px`,
      height: `${markerSize}px`,
      background: toLinearGradient(item.color),
      borderRadius: style.itemMarker === 'circle' ? '50%' : '0',
      ...(style.itemMarker === 'triangle' ? { clipPath: 'polygon(50% 0, 100% 100%, 0 100%)' } : {}),
      ...(style.itemMarker === 'diamond'
        ? { clipPath: 'polygon(50% 0, 100% 50%, 50% 100%, 0 50%)' }
        : {})
    })
    const text = document.createElement('span')
    text.textContent = String(item.label)
    Object.assign(text.style, { overflowWrap: 'anywhere', minWidth: '0' })
    entry.append(marker, text)
    const selected = style.dataeaseLegendTileSelection?.()
    const active = !selected || selected.includes(item.id)
    entry.style.opacity = active ? '1' : '0.4'
    entry.setAttribute('aria-pressed', String(active))
    root.append(entry)
  })
  return root
}

export const configureTiledLegend = (legend: LegendOptions, width: number, height: number) => {
  if (!legend.dataeaseLegendTile) return
  legend.dataeaseSideLegendAutoLayout = false
  legend.dataeaseLegendOverlayPlot = false
  legend.crossPadding = 8
  delete legend.size
  delete legend.length
  delete legend.cols
  delete legend.gridRow
  delete legend.itemLabelWordWrapWidth
  // G2 measures this same DOM before allocating legend padding and the remaining plot area.
  const box = legend.dataeaseLegendTileBox
  if (box) {
    legend.dataeaseLegendTilePosition ||= legend.position
    legend.position = 'center'
  }
  legend.render = (items: LegendItem[]) =>
    renderTiledLegend(
      items,
      legend,
      box?.width || width,
      Math.max(
        1,
        (box?.height || height) - (box || ['left', 'right'].includes(legend.position) ? 32 : 80)
      )
    )
}

/** Apply after chart-specific legend options, including independent legends in flex charts. */
export const applyG2TiledLegend = (
  spec: G2Spec,
  legend: ChartLegendStyle,
  width: number,
  height: number,
  emit?: (event: string, payload: unknown) => void,
  theme: 'light' | 'dark' = 'light'
): G2Spec => {
  if (legend?.displayMode !== 'tile' || !legend.show) return spec
  const mark = (value: LegendOptions, channel = 'color') => {
    let selected: Array<string | number> | undefined
    value.dataeaseLegendTileSelection ||= () => selected
    value.dataeaseLegendTileSetSelection ||= values => {
      selected = values === undefined ? undefined : [...values]
    }
    value.dataeaseLegendTileFilter ||= (domain: Array<string | number>, id: string | number) => {
      selected ||= [...domain]
      selected = selected.includes(id) ? selected.filter(value => value !== id) : [...selected, id]
      emit?.('legend:filter', { data: { channel, values: selected } })
    }
    value.dataeaseLegendTileTheme = theme
    value.dataeaseLegendTileChannel = channel
    value.dataeaseLegendTile = true
    value.dataeaseLegendTileLayout = configureTiledLegend
    value.dataeaseSideLegendAutoLayout = false
    value.dataeaseLegendOverlayPlot = false
    value.dataeaseLegendTileOverflow = legend.tileOverflow || 'scroll'
    value.dataeaseLegendTileOrient = legend.orient
  }
  const visit = (node: any, inheritedTextStyle: LegendOptions = {}) => {
    if (!node || typeof node !== 'object') return
    const textStyle = { ...inheritedTextStyle }
    const category = node.theme?.legendCategory
    for (const key of ['itemLabelFontFamily', 'itemLabelFontWeight', 'itemLabelFontStyle']) {
      if (category?.[key] !== undefined) textStyle[key] = category[key]
    }
    if (node.type === 'legends') {
      node.dataeaseLegendTileTextStyle = textStyle
      mark(node)
    }
    if (node.legend && typeof node.legend === 'object') {
      Object.entries(node.legend).forEach(([channel, value]) => {
        if (value && typeof value === 'object') {
          ;(value as LegendOptions).dataeaseLegendTileTextStyle = textStyle
          mark(value, channel)
        }
      })
    }
    node.children?.forEach(child => visit(child, textStyle))
    const children = node.children || []
    const legendIndex = children.findIndex(child => child.type === 'legends')
    if (legendIndex < 0 || children.length !== 2) return
    const legendNode = children[legendIndex]
    // The combination-chart pagination adapter must not overwrite the tiled DOM measurement.
    delete node.dataeaseSideLegendLayout
    const scale = legendNode.scale?.color || {}
    const domain = scale.domain?.length
      ? scale.domain
      : (scale.relations || []).map(item => item[0])
    const items = domain.map((id, index) => ({
      id,
      label:
        typeof legendNode.labelFormatter === 'function'
          ? legendNode.labelFormatter(id)
          : String(id),
      color: scale.range?.[index] || scale.relations?.[index]?.[1] || '#333'
    }))
    if (!items.length) return
    legendNode.dataeaseLegendTileBox = { width, height }
    const content = renderTiledLegend(items, legendNode, width, Math.max(1, height - 32))
    const measure = document.createElement('div')
    Object.assign(measure.style, { position: 'absolute', visibility: 'hidden', left: '-100000px' })
    measure.append(content)
    document.body.append(measure)
    const bounds = content.getBoundingClientRect()
    measure.remove()
    const horizontal = node.direction === 'row'
    const available = horizontal ? width : height
    const desired = (horizontal ? bounds.width : bounds.height) + 8
    const legendSize = Math.max(1, Math.min(desired, available - 1))
    node.padding = 0
    node.ratio =
      legendIndex === 0
        ? [legendSize, Math.max(1, available - legendSize)]
        : [Math.max(1, available - legendSize), legendSize]
    legendNode.margin = 0
    legendNode.padding = 0
  }
  visit(spec)
  return spec
}

/** G2 rebuilds the unfiltered marks on resize; replay the selection retained by the HTML legend. */
export const replayG2TiledLegendSelection = (
  spec: G2Spec,
  emit: (event: string, payload: unknown) => void
) => {
  const channels = new Set<string>()
  const replay = (value: LegendOptions) => {
    if (!value?.dataeaseLegendTile) return
    const values = value.dataeaseLegendTileSelection?.()
    const channel = value.dataeaseLegendTileChannel || 'color'
    if (values === undefined || channels.has(channel)) return
    channels.add(channel)
    emit('legend:filter', { data: { channel, values: [...values] } })
  }
  const visit = (node: any) => {
    if (!node || typeof node !== 'object') return
    if (node.type === 'legends') replay(node)
    if (node.legend && typeof node.legend === 'object') Object.values(node.legend).forEach(replay)
    node.children?.forEach(visit)
  }
  visit(spec)
}

/** Keep programmatic filtering, focus and reset consistent with the HTML legend and resize replay. */
export const installG2TiledLegendStateAdapter = (chart: G2Chart) => {
  const sync = (channel: string | undefined, values: Array<string | number> | undefined) => {
    const update = (value: LegendOptions) => {
      if (
        value?.dataeaseLegendTile &&
        (channel === undefined || value.dataeaseLegendTileChannel === channel)
      ) {
        value.dataeaseLegendTileSetSelection?.(values)
      }
    }
    const visit = (node: any) => {
      if (!node || typeof node !== 'object') return
      if (node.type === 'legends') update(node)
      if (node.legend && typeof node.legend === 'object') Object.values(node.legend).forEach(update)
      node.children?.forEach(visit)
    }
    visit(chart.options())
    chart
      .getContainer()
      .querySelectorAll('.dataease-tiled-legend')
      .forEach(root => {
        root.dispatchEvent(new Event('dataease-legend-sync'))
      })
  }
  const filter = event => {
    if (typeof event?.data?.channel === 'string' && Array.isArray(event.data.values))
      sync(event.data.channel, event.data.values)
  }
  const focus = event => {
    if (typeof event?.data?.channel === 'string' && event.data.value !== undefined)
      sync(event.data.channel, [event.data.value])
  }
  const reset = () => sync(undefined, undefined)
  chart.on('legend:filter', filter)
  chart.on('legend:focus', focus)
  chart.on('legend:reset', reset)
  return () => {
    chart.off('legend:filter', filter)
    chart.off('legend:focus', focus)
    chart.off('legend:reset', reset)
  }
}
