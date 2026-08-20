import { HorizontalAlign } from '@univerjs/core'
import type { IStyleData } from '@univerjs/core'
import { applyTableBorderStyle } from '../../../components/table-border/border-style'
import type { DetailTableConfig } from '../types'
import type { FieldItemData } from '../../../types/plugin'
import { TableStyleService } from './table-style.service'
import { getFieldDisplayScale } from '../utils/field-format'

const DEFAULT_RENDER_STYLE: Partial<IStyleData> = {
  ht: HorizontalAlign.LEFT
}

export interface DetailTableRenderStyleRange {
  pluginId: string
  unitId: string
  sheetId: string
  startRow: number
  startColumn: number
  rowCount: number
  colCount: number
  fields?: Array<FieldItemData | undefined>
  config: DetailTableConfig
}

export class DetailTableRenderStyleService {
  private readonly _ranges = new Map<string, DetailTableRenderStyleRange>()
  private readonly _styleService = new TableStyleService()

  setRange(range: DetailTableRenderStyleRange): void {
    this._ranges.set(this.getRangeKey(range.unitId, range.sheetId, range.pluginId), range)
  }

  deleteRange(unitId: string, sheetId: string, pluginId: string): void {
    this._ranges.delete(this.getRangeKey(unitId, sheetId, pluginId))
  }

  deleteUnit(unitId: string): void {
    Array.from(this._ranges.keys()).forEach(key => {
      if (key.startsWith(`${unitId}|`)) {
        this._ranges.delete(key)
      }
    })
  }

  clear(): void {
    this._ranges.clear()
  }

  shiftRows(
    unitId: string,
    sheetId: string,
    position: number,
    count: number,
    excludedPluginId?: string
  ): void {
    this._ranges.forEach(range => {
      if (range.unitId === unitId && range.sheetId === sheetId && range.startRow >= position) {
        range.startRow += count
        if (range.pluginId !== excludedPluginId) {
          range.config.placement.startCell = this.toCellAddress(range.startRow, range.startColumn)
        }
      }
    })
  }

  shiftColumns(
    unitId: string,
    sheetId: string,
    position: number,
    count: number,
    excludedPluginId?: string
  ): void {
    this._ranges.forEach(range => {
      if (
        range.unitId === unitId &&
        range.sheetId === sheetId &&
        range.startColumn >= position
      ) {
        range.startColumn += count
        if (range.pluginId !== excludedPluginId) {
          range.config.placement.startCell = this.toCellAddress(range.startRow, range.startColumn)
        }
      }
    })
  }

  findRangeAt(
    unitId: string,
    sheetId: string,
    row: number,
    col: number
  ): DetailTableRenderStyleRange | undefined {
    return Array.from(this._ranges.values()).find(item =>
      item.unitId === unitId &&
      item.sheetId === sheetId &&
      row >= item.startRow &&
      row < item.startRow + item.rowCount &&
      col >= item.startColumn &&
      col < item.startColumn + item.colCount
    )
  }

  hasRange(pluginId: string): boolean {
    return Array.from(this._ranges.values()).some(item => item.pluginId === pluginId)
  }

  getStyle(unitId: string, sheetId: string, row: number, col: number): Partial<IStyleData> | undefined {
    const range = Array.from(this._ranges.values()).find(item =>
      item.unitId === unitId &&
      item.sheetId === sheetId &&
      row >= item.startRow &&
      row < item.startRow + item.rowCount &&
      col >= item.startColumn &&
      col < item.startColumn + item.colCount
    )

    if (!range) {
      return undefined
    }

    const hideHeader = !!range.config.style?.base?.hideHeader
    const relativeColumn = col - range.startColumn
    if (!hideHeader && row === range.startRow) {
      const headerStyle = range.config.style?.header
      return headerStyle?.enable
        ? applyTableBorderStyle(
            this._styleService.convertHeaderStyle(headerStyle),
            headerStyle.border,
            {
              rowIndex: 0,
              columnIndex: relativeColumn,
              rowCount: 1,
              columnCount: range.colCount
            }
          )
        : DEFAULT_RENDER_STYLE
    }

    const totalStyle = range.config.style?.total
    const isTotalRow = !!totalStyle?.enable && row === range.startRow + range.rowCount - 1
    if (isTotalRow) {
      return totalStyle.customStyle
        ? applyTableBorderStyle(
            this._styleService.convertTotalRowStyle(totalStyle),
            totalStyle.border,
            {
              rowIndex: 0,
              columnIndex: relativeColumn,
              rowCount: 1,
              columnCount: range.colCount
            }
          )
        : DEFAULT_RENDER_STYLE
    }

    const cellStyle = range.config.style?.cell
    if (!cellStyle || cellStyle.enable === false) {
      return DEFAULT_RENDER_STYLE
    }

    const headerRowCount = hideHeader ? 0 : 1
    const totalRowCount = totalStyle?.enable ? 1 : 0
    const dataRowCount = Math.max(range.rowCount - headerRowCount - totalRowCount, 0)
    const relativeDataRow = row - range.startRow - headerRowCount
    const style = applyTableBorderStyle(
      this._styleService.convertCellStyle(cellStyle),
      cellStyle.border,
      {
        rowIndex: relativeDataRow,
        columnIndex: relativeColumn,
        rowCount: dataRowCount,
        columnCount: range.colCount
      }
    )
    if (range.config.style?.base?.mergeCell || !cellStyle.enableZebra) {
      return style
    }

    return this._styleService.applyAlternatingRowStyle(style, relativeDataRow, cellStyle.zebraColor)
  }

  getDisplayValue(
    unitId: string,
    sheetId: string,
    row: number,
    col: number,
    value: unknown
  ): unknown {
    if (typeof value !== 'number' || !Number.isFinite(value)) {
      return value
    }

    const range = this.findRangeAt(unitId, sheetId, row, col)
    if (!range) {
      return value
    }

    const showIndex = !!range.config.style?.header?.showIndex
    const fieldIndex = col - range.startColumn - (showIndex ? 1 : 0)
    const field = range.fields?.[fieldIndex]
    const displayScale = getFieldDisplayScale(field)
    return displayScale === 1 ? value : value / displayScale
  }

  shouldOverrideUserStyle(
    unitId: string,
    sheetId: string,
    row: number,
    col: number
  ): boolean {
    const range = this.findRangeAt(unitId, sheetId, row, col)
    if (!range) {
      return false
    }
    const hideHeader = !!range.config.style?.base?.hideHeader
    if (!hideHeader && row === range.startRow) {
      return !!range.config.style?.header?.enable
    }
    const totalStyle = range.config.style?.total
    if (totalStyle?.enable && row === range.startRow + range.rowCount - 1) {
      return !!totalStyle.customStyle
    }
    return !!range.config.style?.cell && range.config.style.cell.enable !== false
  }

  private getRangeKey(unitId: string, sheetId: string, pluginId: string): string {
    return `${unitId}|${sheetId}|${pluginId}`
  }

  private toCellAddress(row: number, column: number): string {
    let columnName = ''
    let current = column
    do {
      columnName = String.fromCharCode(65 + (current % 26)) + columnName
      current = Math.floor(current / 26) - 1
    } while (current >= 0)
    return `${columnName}${row + 1}`
  }

}
