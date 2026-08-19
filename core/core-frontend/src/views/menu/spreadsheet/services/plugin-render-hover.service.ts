export interface PluginRenderHoverRange {
  pluginId: string
  unitId: string
  sheetId: string
  startRow: number
  startColumn: number
  rowCount: number
  colCount: number
}

export class PluginRenderHoverService {
  private _hoverRange?: PluginRenderHoverRange

  getHoverRange(): PluginRenderHoverRange | undefined {
    return this._hoverRange
  }

  setHoverRange(range?: PluginRenderHoverRange): boolean {
    if (this._isSameRange(this._hoverRange, range)) {
      return false
    }
    this._hoverRange = range ? { ...range } : undefined
    return true
  }

  clearHoverRange(): boolean {
    return this.setHoverRange(undefined)
  }

  isCellInHoverRange(unitId: string, sheetId: string, row: number, col: number): boolean {
    const range = this._hoverRange
    if (!range || range.unitId !== unitId || range.sheetId !== sheetId) {
      return false
    }

    return (
      row >= range.startRow &&
      row < range.startRow + range.rowCount &&
      col >= range.startColumn &&
      col < range.startColumn + range.colCount
    )
  }

  private _isSameRange(
    current?: PluginRenderHoverRange,
    next?: PluginRenderHoverRange
  ): boolean {
    if (!current && !next) {
      return true
    }
    if (!current || !next) {
      return false
    }

    return (
      current.pluginId === next.pluginId &&
      current.unitId === next.unitId &&
      current.sheetId === next.sheetId &&
      current.startRow === next.startRow &&
      current.startColumn === next.startColumn &&
      current.rowCount === next.rowCount &&
      current.colCount === next.colCount
    )
  }
}
