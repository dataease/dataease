export interface PluginActionToolbarRangeLike {
  startRow: number
  startColumn: number
  rowCount: number
  columnCount?: number
  colCount?: number
}

const TOOLBAR_WIDTH = 32

export interface Rect {
  left: number
  top: number
  right: number
  bottom: number
}

interface CellCoord {
  startX: number
  startY: number
  endX: number
  endY: number
}

export function getPluginActionToolbarPosition(
  worksheet: any,
  range: PluginActionToolbarRangeLike
): { left: number; top: number } | undefined {
  const renderRect = getPluginCellRect(
    worksheet,
    range.startRow,
    range.startColumn,
    range.rowCount,
    range.columnCount ?? range.colCount ?? 1
  )
  if (!renderRect) {
    return undefined
  }

  const skeleton = worksheet?.getSkeleton?.()
  const editorRect = document
    .querySelector<HTMLElement>('.editor-canvas')
    ?.getBoundingClientRect()
  const canvasRect = getMainCanvasRect()
  if (!skeleton || !editorRect || !canvasRect) {
    return undefined
  }

  const gridRect = getGridRect(skeleton, canvasRect, editorRect)
  if (!intersects(renderRect, gridRect)) {
    return undefined
  }

  return {
    left: Math.max(renderRect.left, gridRect.left) - TOOLBAR_WIDTH,
    top: Math.max(renderRect.top, gridRect.top)
  }
}

/**
 * 计算单元格区域在编辑器画布（.editor-canvas）坐标系下的像素矩形，
 * 供 HTML overlay 定位使用（动作工具条、异常占位符等）。
 */
export function getPluginCellRect(
  worksheet: any,
  startRow: number,
  startColumn: number,
  rowCount = 1,
  columnCount = 1
): Rect | undefined {
  const skeleton = worksheet?.getSkeleton?.()
  const editorRect = document
    .querySelector<HTMLElement>('.editor-canvas')
    ?.getBoundingClientRect()
  const canvasRect = getMainCanvasRect()
  if (!skeleton || !editorRect || !canvasRect) {
    return undefined
  }

  return getRenderRect(worksheet, skeleton, canvasRect, editorRect, {
    startRow,
    startColumn,
    rowCount,
    columnCount
  })
}

/**
 * 计算单元格区域在视口（viewport）坐标系下的像素矩形，
 * 供 `position: fixed` 的 tooltip 定位使用，编辑态与预览态通用。
 */
export function getPluginCellViewportRect(
  worksheet: any,
  startRow: number,
  startColumn: number,
  rowCount = 1,
  columnCount = 1
): Rect | undefined {
  const skeleton = worksheet?.getSkeleton?.()
  const canvasRect = getMainCanvasRect()
  if (!skeleton || !canvasRect) {
    return undefined
  }

  const normalizedRowCount = Math.max(1, Number(rowCount))
  const normalizedColumnCount = Math.max(1, Number(columnCount))
  const startCell = getCellCoord(skeleton, startRow, startColumn)
  const endCell = getCellCoord(
    skeleton,
    startRow + normalizedRowCount - 1,
    startColumn + normalizedColumnCount - 1
  )
  if (!startCell || !endCell) {
    return undefined
  }

  const scale = getCanvasScale(canvasRect)
  const zoomRatio = Number(worksheet?.getZoom?.()) || 1
  const scroll = getScroll(skeleton)

  return {
    left: toViewportX(startCell.startX, canvasRect, scroll.x, zoomRatio, scale),
    top: toViewportY(startCell.startY, canvasRect, scroll.y, zoomRatio, scale),
    right: toViewportX(endCell.endX, canvasRect, scroll.x, zoomRatio, scale),
    bottom: toViewportY(endCell.endY, canvasRect, scroll.y, zoomRatio, scale)
  }
}

function getRenderRect(
  worksheet: any,
  skeleton: any,
  canvasRect: DOMRect,
  editorRect: DOMRect,
  range: PluginActionToolbarRangeLike
): Rect | undefined {
  const rowCount = Math.max(1, Number(range.rowCount ?? 1))
  const columnCount = Math.max(1, Number(range.columnCount ?? range.colCount ?? 1))
  const startCell = getCellCoord(skeleton, range.startRow, range.startColumn)
  const endCell = getCellCoord(
    skeleton,
    range.startRow + rowCount - 1,
    range.startColumn + columnCount - 1
  )
  if (!startCell || !endCell) {
    return undefined
  }

  const scale = getCanvasScale(canvasRect)
  const zoomRatio = Number(worksheet?.getZoom?.()) || 1
  const scroll = getScroll(skeleton)

  return {
    left: toEditorX(startCell.startX, canvasRect, editorRect, scroll.x, zoomRatio, scale),
    top: toEditorY(startCell.startY, canvasRect, editorRect, scroll.y, zoomRatio, scale),
    right: toEditorX(endCell.endX, canvasRect, editorRect, scroll.x, zoomRatio, scale),
    bottom: toEditorY(endCell.endY, canvasRect, editorRect, scroll.y, zoomRatio, scale)
  }
}

function getGridRect(skeleton: any, canvasRect: DOMRect, editorRect: DOMRect): Rect {
  const rowHeaderWidth = Number(skeleton.rowHeaderWidthAndMarginLeft ?? skeleton.rowHeaderWidth ?? 0)
  const columnHeaderHeight = Number(
    skeleton.columnHeaderHeightAndMarginTop ?? skeleton.columnHeaderHeight ?? 0
  )

  return {
    left: canvasRect.left - editorRect.left + rowHeaderWidth,
    top: canvasRect.top - editorRect.top + columnHeaderHeight,
    right: canvasRect.right - editorRect.left,
    bottom: canvasRect.bottom - editorRect.top
  }
}

function getCellCoord(skeleton: any, row: number, column: number): CellCoord | undefined {
  const cell = skeleton?.getCellWithCoordByIndex?.(row, column)
  if (!cell) {
    return undefined
  }

  const coord = cell.isMergedMainCell ? cell.mergeInfo : cell
  return {
    startX: Number(coord.startX),
    startY: Number(coord.startY),
    endX: Number(coord.endX),
    endY: Number(coord.endY)
  }
}

function getMainCanvasRect(): DOMRect | undefined {
  return Array.from(document.querySelectorAll<HTMLCanvasElement>('.univer-container canvas'))
    .map(canvas => canvas.getBoundingClientRect())
    .filter(rect => rect.width > 0 && rect.height > 0)
    .sort((a, b) => b.width * b.height - a.width * a.height)[0]
}

function getCanvasScale(canvasRect: DOMRect): number {
  const canvas = Array.from(document.querySelectorAll<HTMLCanvasElement>('.univer-container canvas'))
    .find(item => {
      const rect = item.getBoundingClientRect()
      return rect.width === canvasRect.width && rect.height === canvasRect.height
    })
  const styleWidth = Number.parseFloat(canvas?.style?.width || '')
  return styleWidth > 0 ? canvasRect.width / styleWidth : 1
}

function getScroll(skeleton: any): { x: number; y: number } {
  const scroll = skeleton?.getScrollXY?.()
  return {
    x: toFiniteNumber(scroll?.x) ?? toFiniteNumber(skeleton?.scrollX) ?? 0,
    y: toFiniteNumber(scroll?.y) ?? toFiniteNumber(skeleton?.scrollY) ?? 0
  }
}

function toEditorX(
  x: number,
  canvasRect: DOMRect,
  editorRect: DOMRect,
  scrollX: number,
  zoomRatio: number,
  scale: number
): number {
  return canvasRect.left - editorRect.left + (x - scrollX) * zoomRatio * scale
}

function toEditorY(
  y: number,
  canvasRect: DOMRect,
  editorRect: DOMRect,
  scrollY: number,
  zoomRatio: number,
  scale: number
): number {
  return canvasRect.top - editorRect.top + (y - scrollY) * zoomRatio * scale
}

function toViewportX(
  x: number,
  canvasRect: DOMRect,
  scrollX: number,
  zoomRatio: number,
  scale: number
): number {
  return canvasRect.left + (x - scrollX) * zoomRatio * scale
}

function toViewportY(
  y: number,
  canvasRect: DOMRect,
  scrollY: number,
  zoomRatio: number,
  scale: number
): number {
  return canvasRect.top + (y - scrollY) * zoomRatio * scale
}

function intersects(a: Rect, b: Rect): boolean {
  return a.right > b.left && a.left < b.right && a.bottom > b.top && a.top < b.bottom
}

function toFiniteNumber(value: unknown): number | undefined {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : undefined
}
