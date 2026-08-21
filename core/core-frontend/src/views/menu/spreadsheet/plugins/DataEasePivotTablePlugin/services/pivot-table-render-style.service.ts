import { BooleanNumber, HorizontalAlign, VerticalAlign } from '@univerjs/core'
import type { IStyleData } from '@univerjs/core'
import type { TableBorderCellContext } from '../../../components/table-border/border-style'
import { applyTableBorderStyle } from '../../../components/table-border/border-style'
import type { PivotTableCellStyle, PivotTableConfig, PivotTableHeaderStyle } from '../types'

const DEFAULT_RENDER_STYLE: Partial<IStyleData> = {
  ht: HorizontalAlign.LEFT
}

const horizontalAlignMap = {
  left: HorizontalAlign.LEFT,
  center: HorizontalAlign.CENTER,
  right: HorizontalAlign.RIGHT
} as const

const verticalAlignMap = {
  top: VerticalAlign.TOP,
  middle: VerticalAlign.MIDDLE,
  bottom: VerticalAlign.BOTTOM
} as const

export interface PivotTableRenderRange {
  pluginId: string
  unitId: string
  sheetId: string
  startRow: number
  startColumn: number
  rowCount: number
  columnCount: number
  headerRowCount: number
  headerColumnCount: number
  displayScales?: number[][]
  dataRange?: {
    startRow: number
    endRow: number
    startColumn: number
    endColumn: number
  }
  config: PivotTableConfig
}

interface PivotTableHeaderSection {
  style: PivotTableHeaderStyle | undefined
  borderContext: TableBorderCellContext
}

export class PivotTableRenderStyleService {
  private readonly ranges = new Map<string, PivotTableRenderRange>()

  setRange(range: PivotTableRenderRange): void {
    this.ranges.set(this.getRangeKey(range.unitId, range.sheetId, range.pluginId), range)
  }

  deleteRange(unitId: string, sheetId: string, pluginId: string): void {
    this.ranges.delete(this.getRangeKey(unitId, sheetId, pluginId))
  }

  deleteUnit(unitId: string): void {
    Array.from(this.ranges.keys()).forEach(key => {
      if (key.startsWith(`${unitId}|`)) {
        this.ranges.delete(key)
      }
    })
  }

  clear(): void {
    this.ranges.clear()
  }

  shiftRows(
    unitId: string,
    sheetId: string,
    position: number,
    count: number,
    excludedPluginId?: string
  ): void {
    this.ranges.forEach(range => {
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
    this.ranges.forEach(range => {
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
    column: number
  ): PivotTableRenderRange | undefined {
    return Array.from(this.ranges.values()).find(range =>
      range.unitId === unitId &&
      range.sheetId === sheetId &&
      row >= range.startRow &&
      row < range.startRow + range.rowCount &&
      column >= range.startColumn &&
      column < range.startColumn + range.columnCount
    )
  }

  hasRange(pluginId: string): boolean {
    return Array.from(this.ranges.values()).some(range => range.pluginId === pluginId)
  }

  getStyle(
    unitId: string,
    sheetId: string,
    row: number,
    column: number
  ): Partial<IStyleData> | undefined {
    const range = this.findRangeAt(unitId, sheetId, row, column)
    if (!range) {
      return undefined
    }

    const relativeRow = row - range.startRow
    const relativeColumn = column - range.startColumn
    if (this.isDataCell(range, relativeRow, relativeColumn)) {
      return this.getCellStyle(range, relativeRow, relativeColumn)
    }

    return this.getHeaderStyle(range, relativeRow, relativeColumn)
  }

  getDisplayValue(
    unitId: string,
    sheetId: string,
    row: number,
    column: number,
    value: unknown
  ): unknown {
    if (typeof value !== 'number' || !Number.isFinite(value)) {
      return value
    }

    const range = this.findRangeAt(unitId, sheetId, row, column)
    if (!range) {
      return value
    }

    const relativeRow = row - range.startRow
    const relativeColumn = column - range.startColumn
    const displayScale = range.displayScales?.[relativeRow]?.[relativeColumn] ?? 1
    return displayScale === 1 ? value : value / displayScale
  }

  shouldOverrideUserStyle(
    unitId: string,
    sheetId: string,
    row: number,
    column: number
  ): boolean {
    const range = this.findRangeAt(unitId, sheetId, row, column)
    if (!range) {
      return false
    }

    const relativeRow = row - range.startRow
    const relativeColumn = column - range.startColumn
    if (this.isDataCell(range, relativeRow, relativeColumn)) {
      const cellStyle = range.config.style?.cell
      return !!cellStyle && cellStyle.enable !== false
    }

    const headerStyle = this.getHeaderStyleConfig(range, relativeRow, relativeColumn)
    return !!headerStyle && headerStyle.enable !== false
  }

  private isDataCell(
    range: PivotTableRenderRange,
    relativeRow: number,
    relativeColumn: number
  ): boolean {
    const dataRange = range.dataRange
    return !!dataRange &&
      relativeRow >= dataRange.startRow &&
      relativeRow <= dataRange.endRow &&
      relativeColumn >= dataRange.startColumn &&
      relativeColumn <= dataRange.endColumn
  }

  private getHeaderStyle(
    range: PivotTableRenderRange,
    relativeRow: number,
    relativeColumn: number
  ): Partial<IStyleData> | undefined {
    const section = this.getHeaderSection(range, relativeRow, relativeColumn)
    const headerStyle = section.style

    if (!headerStyle || headerStyle.enable === false) {
      return DEFAULT_RENDER_STYLE
    }

    return applyTableBorderStyle({
      ...(headerStyle.backgroundColor ? { bg: { rgb: headerStyle.backgroundColor } } : {}),
      cl: { rgb: headerStyle.textColor || '#333333' },
      fs: headerStyle.fontSize || 12,
      bl: headerStyle.bold ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      it: headerStyle.italic ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      ul: { s: headerStyle.underline ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      st: { s: headerStyle.strikethrough ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      ht: horizontalAlignMap[headerStyle.textAlign || 'left'],
      vt: verticalAlignMap[headerStyle.verticalAlign || 'middle']
    }, headerStyle.border, section.borderContext)
  }

  private getHeaderStyleConfig(
    range: PivotTableRenderRange,
    relativeRow: number,
    relativeColumn: number
  ): PivotTableHeaderStyle | undefined {
    return this.getHeaderSection(range, relativeRow, relativeColumn).style
  }

  private getHeaderSection(
    range: PivotTableRenderRange,
    relativeRow: number,
    relativeColumn: number
  ): PivotTableHeaderSection {
    if (relativeRow >= range.headerRowCount && relativeColumn < range.headerColumnCount) {
      return {
        style: range.config.style?.rowHeader,
        borderContext: {
          rowIndex: relativeRow - range.headerRowCount,
          columnIndex: relativeColumn,
          rowCount: Math.max(range.rowCount - range.headerRowCount, 0),
          columnCount: range.headerColumnCount
        }
      }
    }
    if (relativeRow < range.headerRowCount && relativeColumn >= range.headerColumnCount) {
      return {
        style: range.config.style?.columnHeader,
        borderContext: {
          rowIndex: relativeRow,
          columnIndex: relativeColumn - range.headerColumnCount,
          rowCount: range.headerRowCount,
          columnCount: Math.max(range.columnCount - range.headerColumnCount, 0)
        }
      }
    }
    return {
      style: range.config.style?.cornerHeader,
      borderContext: {
        rowIndex: relativeRow,
        columnIndex: relativeColumn,
        rowCount: range.headerRowCount,
        columnCount: range.headerColumnCount
      }
    }
  }

  private getCellStyle(
    range: PivotTableRenderRange,
    relativeRow: number,
    relativeColumn: number
  ): Partial<IStyleData> | undefined {
    const cellStyle = range.config.style?.cell
    if (!cellStyle || cellStyle.enable === false) {
      return DEFAULT_RENDER_STYLE
    }

    const dataRange = range.dataRange!
    const style = applyTableBorderStyle(
      this.convertCellStyle(cellStyle),
      cellStyle.border,
      {
        rowIndex: relativeRow - dataRange.startRow,
        columnIndex: relativeColumn - dataRange.startColumn,
        rowCount: dataRange.endRow - dataRange.startRow + 1,
        columnCount: dataRange.endColumn - dataRange.startColumn + 1
      }
    )
    if (
      this.isMergeCellEnabled(range.config) ||
      !cellStyle.enableZebra ||
      !cellStyle.zebraColor ||
      !range.dataRange
    ) {
      return style
    }

    const dataRowIndex = relativeRow - range.dataRange.startRow
    return dataRowIndex % 2 === 1
      ? { ...style, bg: { rgb: cellStyle.zebraColor } }
      : style
  }

  private convertCellStyle(cellStyle: PivotTableCellStyle): Partial<IStyleData> {
    return {
      ...(cellStyle.backgroundColor ? { bg: { rgb: cellStyle.backgroundColor } } : {}),
      cl: { rgb: cellStyle.textColor || '#333333' },
      fs: cellStyle.fontSize || 12,
      bl: cellStyle.bold ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      it: cellStyle.italic ? BooleanNumber.TRUE : BooleanNumber.FALSE,
      ul: { s: cellStyle.underline ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      st: { s: cellStyle.strikethrough ? BooleanNumber.TRUE : BooleanNumber.FALSE },
      ht: horizontalAlignMap[cellStyle.textAlign || 'left'],
      vt: verticalAlignMap[cellStyle.verticalAlign || 'middle']
    }
  }

  private isMergeCellEnabled(config: PivotTableConfig): boolean {
    return Boolean(config.style?.base?.mergeCell)
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
