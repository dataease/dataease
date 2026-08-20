import { Disposable, Inject } from '@univerjs/core'
import type { IRangeSelectResult } from '../../RangeSelectPlugin/type'
import { TableRangeConflictService } from '../../../services/table-range-conflict.service'
import type { DetailTableConfig } from '../types'
import { DetailTableDisplayStateService } from './detail-table-display-state.service'

interface SheetRange {
  pluginId?: string
  sheetId: string
  startRow: number
  endRow: number
  startColumn: number
  endColumn: number
}

export class DetailTableRangeService extends Disposable {
  constructor(
    @Inject(DetailTableDisplayStateService)
    private readonly displayStateService: DetailTableDisplayStateService,
    @Inject(TableRangeConflictService)
    private readonly conflictService: TableRangeConflictService
  ) {
    super()
    this.disposeWithMe(
      this.conflictService.registerProvider('detail-table', {
        getRanges: () => this.getRenderedRanges().map(range => ({
          tableType: 'detail' as const,
          pluginId: range.pluginId as string,
          sheetId: range.sheetId,
          startRow: range.startRow,
          endRow: range.endRow,
          startColumn: range.startColumn,
          endColumn: range.endColumn
        }))
      })
    )
  }

  validateSelectedRange(range: IRangeSelectResult, resultLimit = 1000): string | undefined {
    const rowCount = this.getRowCountByResultLimit(resultLimit)
    const selectedRange: SheetRange = {
      sheetId: range.sheetId,
      startRow: range.startRowNumber,
      endRow: range.startRowNumber + rowCount - 1,
      startColumn: range.startColumnNumber,
      endColumn: range.endColumnNumber
    }

    return this.validateNoOverlap(selectedRange)
  }

  validateConfigUpdate(
    _config: DetailTableConfig,
    _key: string,
    _value: any,
    _univerApi?: any
  ): string | undefined {
    // 扩容冲突需要基于查询后的真实行列数统一规划，这里不再提前阻断配置更新。
    return undefined
  }

  validateRenderRangeBeforeFill(
    univerApi: any,
    config: DetailTableConfig,
    startCell: string,
    rowCount: number,
    colCount: number,
    targetWorksheet?: any,
    skipExistingCellValidation = false
  ): string | undefined {
    if (rowCount <= 0 || colCount <= 0) {
      return undefined
    }

    const workbook = univerApi.getActiveWorkbook?.()
    const worksheet = targetWorksheet || workbook?.getActiveSheet?.()
    const sheetId = worksheet?.getSheetId?.()
    if (!worksheet || !sheetId) {
      return undefined
    }

    const startPos = this.parseCell(startCell)
    const targetRange: SheetRange = {
      pluginId: config.id,
      sheetId,
      startRow: startPos.row,
      endRow: startPos.row + rowCount - 1,
      startColumn: startPos.col,
      endColumn: startPos.col + colCount - 1
    }

    const overlapMessage = this.validateNoOverlap(targetRange)
    if (overlapMessage) {
      return overlapMessage
    }

    if (
      !skipExistingCellValidation &&
      this.hasExternalCellValue(worksheet, targetRange, this.getCurrentRenderedRange(config.id))
    ) {
      return '渲染区域已有数据，请更换位置或清理后重试'
    }

    return undefined
  }

  private validateNoOverlap(targetRange: SheetRange): string | undefined {
    if (!this.conflictService.findConflict(targetRange)) {
      return undefined
    }

    return '当前选择区域与已有表格区域重合'
  }

  private getRenderedRanges(): SheetRange[] {
    return this.displayStateService
      .list()
      .filter(state => state.rowCount > 0 && state.colCount > 0)
      .map(state => {
        const startPos = this.parseCell(state.startCell)
        return {
          pluginId: state.pluginId,
          sheetId: state.sheetId,
          startRow: startPos.row,
          endRow: startPos.row + state.rowCount - 1,
          startColumn: startPos.col,
          endColumn: startPos.col + state.colCount - 1
        }
      })
  }

  private getCurrentRenderedRange(pluginId: string): SheetRange | undefined {
    return this.getRenderedRanges().find(range => range.pluginId === pluginId)
  }

  private getRowCountByResultLimit(resultLimit?: number): number {
    const normalizedLimit = Number.isFinite(resultLimit) && Number(resultLimit) > 0
      ? Math.floor(Number(resultLimit))
      : 1000
    return 1 + normalizedLimit
  }

  private hasExternalCellValue(worksheet: any, targetRange: SheetRange, allowedRange?: SheetRange): boolean {
    const values = worksheet
      .getRange(
        targetRange.startRow,
        targetRange.startColumn,
        targetRange.endRow - targetRange.startRow + 1,
        targetRange.endColumn - targetRange.startColumn + 1
      )
      .getValues()

    for (let rowIndex = 0; rowIndex < values.length; rowIndex++) {
      for (let colIndex = 0; colIndex < values[rowIndex].length; colIndex++) {
        const row = targetRange.startRow + rowIndex
        const col = targetRange.startColumn + colIndex
        if (allowedRange && this.isCellInRange(row, col, allowedRange)) {
          continue
        }

        if (!this.isEmptyCellValue(values[rowIndex][colIndex])) {
          return true
        }
      }
    }

    return false
  }

  private isCellInRange(row: number, col: number, range: SheetRange): boolean {
    return row >= range.startRow &&
      row <= range.endRow &&
      col >= range.startColumn &&
      col <= range.endColumn
  }

  private isEmptyCellValue(value: any): boolean {
    return value === null || value === undefined || value === ''
  }

  private parseCell(cellAddress: string): { row: number; col: number } {
    const match = cellAddress.match(/^([A-Z]+)(\d+)$/i)
    if (!match) {
      throw new Error(`Invalid cell address: ${cellAddress}`)
    }

    const colStr = match[1].toUpperCase()
    let col = 0
    for (let i = 0; i < colStr.length; i++) {
      col = col * 26 + (colStr.charCodeAt(i) - 64)
    }

    return {
      row: parseInt(match[2], 10) - 1,
      col: col - 1
    }
  }
}
