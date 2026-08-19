import { Disposable, Inject } from '@univerjs/core'
import type { IRangeSelectResult } from '../../RangeSelectPlugin/type'
import {
  TableRangeConflictService,
  type TableOccupiedRange
} from '../../../services/table-range-conflict.service'
import type { PivotTableConfig } from '../types'
import { PivotTableDisplayStateService } from './pivot-table-display-state.service'

interface SheetRange {
  pluginId?: string
  sheetId: string
  startRow: number
  endRow: number
  startColumn: number
  endColumn: number
}

export class PivotTableRangeService extends Disposable {
  constructor(
    @Inject(PivotTableDisplayStateService)
    private readonly displayStateService: PivotTableDisplayStateService,
    @Inject(TableRangeConflictService)
    private readonly conflictService: TableRangeConflictService
  ) {
    super()
    this.disposeWithMe(
      this.conflictService.registerProvider('pivot-table', {
        getRanges: () => this.getRenderedRanges()
      })
    )
  }

  validateSelectedRange(range: IRangeSelectResult): string | undefined {
    const target: SheetRange = {
      sheetId: range.sheetId,
      startRow: range.startRowNumber,
      endRow: range.endRowNumber,
      startColumn: range.startColumnNumber,
      endColumn: range.endColumnNumber
    }

    if (this.conflictService.findConflict(target)) {
      return '当前选择区域与已有表格区域重合'
    }
    return undefined
  }

  validateBeforeFill(
    worksheet: any,
    config: PivotTableConfig,
    rowCount: number,
    columnCount: number,
    skipExistingCellValidation = false
  ): string | undefined {
    const start = this.parseCell(config.placement.startCell)
    const target: SheetRange = {
      pluginId: config.id,
      sheetId: worksheet.getSheetId(),
      startRow: start.row,
      endRow: start.row + rowCount - 1,
      startColumn: start.col,
      endColumn: start.col + columnCount - 1
    }
    if (this.conflictService.findConflict(target)) {
      return '当前渲染区域与已有表格区域重合'
    }

    if (skipExistingCellValidation) {
      return undefined
    }

    const current = this.displayStateService.get(config.id)
    const values = worksheet
      .getRange(target.startRow, target.startColumn, rowCount, columnCount)
      .getValues()
    for (let row = 0; row < values.length; row++) {
      for (let column = 0; column < values[row].length; column++) {
        const absoluteRow = target.startRow + row
        const absoluteColumn = target.startColumn + column
        if (current && this.isInCurrentRange(current, absoluteRow, absoluteColumn)) {
          continue
        }
        if (!this.isEmpty(values[row][column])) {
          return '渲染区域已有数据，请更换位置或清理后重试'
        }
      }
    }
    return undefined
  }

  parseCell(cellAddress: string): { row: number; col: number } {
    const match = cellAddress.match(/^([A-Z]+)(\d+)$/i)
    if (!match) {
      throw new Error(`Invalid cell address: ${cellAddress}`)
    }
    let column = 0
    for (const char of match[1].toUpperCase()) {
      column = column * 26 + char.charCodeAt(0) - 64
    }
    return {
      row: Number(match[2]) - 1,
      col: column - 1
    }
  }

  private isInCurrentRange(
    state: { startCell: string; rowCount: number; columnCount: number },
    row: number,
    column: number
  ): boolean {
    const start = this.parseCell(state.startCell)
    return row >= start.row &&
      row < start.row + state.rowCount &&
      column >= start.col &&
      column < start.col + state.columnCount
  }

  private getRenderedRanges(): TableOccupiedRange[] {
    return this.displayStateService
      .list()
      .filter(state => state.rowCount > 0 && state.columnCount > 0)
      .map(state => {
        const start = this.parseCell(state.startCell)
        return {
          tableType: 'pivot' as const,
          pluginId: state.pluginId,
          sheetId: state.sheetId,
          startRow: start.row,
          endRow: start.row + state.rowCount - 1,
          startColumn: start.col,
          endColumn: start.col + state.columnCount - 1
        }
      })
  }

  private isEmpty(value: unknown): boolean {
    return value === null || value === undefined || value === ''
  }
}
