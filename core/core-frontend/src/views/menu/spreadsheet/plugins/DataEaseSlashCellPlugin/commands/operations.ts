import {
  CommandType,
  IUniverInstanceService,
  UniverInstanceType,
  type IAccessor,
  type ICommand
} from '@univerjs/core'
import { IRenderManagerService } from '@univerjs/engine-render'
import { SheetsSelectionsService } from '@univerjs/sheets'
import type { SlashCellRange, SlashCellType } from '../types'
import { SlashCellStateService } from '../services/slash-cell-state.service'

export interface IApplySlashCellOperationParams {
  ranges?: SlashCellRange[]
}

export interface IClearSlashCellOperationParams {
  ranges?: SlashCellRange[]
}

interface WorksheetContext {
  workbook: any
  worksheet: any
  unitId: string
  sheetId: string
}

export const ApplyTwoSlashCellOperation: ICommand = {
  id: 'dataease.operation.apply-two-slash-cell',
  type: CommandType.OPERATION,
  handler: (accessor: IAccessor, params?: IApplySlashCellOperationParams) => {
    return applySlashCells(accessor, 'two', params)
  }
}

export const ApplyThreeSlashCellOperation: ICommand = {
  id: 'dataease.operation.apply-three-slash-cell',
  type: CommandType.OPERATION,
  handler: (accessor: IAccessor, params?: IApplySlashCellOperationParams) => {
    return applySlashCells(accessor, 'three', params)
  }
}

export const ClearSlashCellOperation: ICommand = {
  id: 'dataease.operation.clear-slash-cell',
  type: CommandType.OPERATION,
  handler: (accessor: IAccessor, params?: IClearSlashCellOperationParams) => {
    const normalized = getNormalizedRanges(accessor, params?.ranges)
    if (!normalized.primaryRanges.length && !normalized.cleanupRanges.length) {
      return false
    }
    accessor.get(SlashCellStateService).clearCells(normalized.cleanupRanges)
    refreshActiveSheet(accessor)
    return true
  }
}

function getWorksheetContext(accessor: IAccessor): WorksheetContext | undefined {
  const workbook = accessor
    .get(IUniverInstanceService)
    .getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET) as any
  const worksheet = workbook?.getActiveSheet?.()
  const unitId = workbook?.getUnitId?.() || workbook?.getId?.()
  const sheetId = worksheet?.getSheetId?.()
  if (!unitId || !sheetId) {
    return undefined
  }
  return { workbook, worksheet, unitId, sheetId }
}

function getCurrentRanges(accessor: IAccessor): SlashCellRange[] {
  const context = getWorksheetContext(accessor)
  if (!context) {
    return []
  }

  const selections = accessor.get(SheetsSelectionsService).getCurrentSelections?.() || []
  return selections
    .map((selection: any) => selection?.range || selection)
    .filter(Boolean)
    .map((range: any) => ({
      unitId: context.unitId,
      sheetId: context.sheetId,
      startRow: Math.min(range.startRow ?? range.row, range.endRow ?? range.startRow ?? range.row),
      startColumn: Math.min(
        range.startColumn ?? range.col ?? range.startCol,
        range.endColumn ?? range.startColumn ?? range.col ?? range.startCol
      ),
      endRow: Math.max(range.startRow ?? range.row, range.endRow ?? range.startRow ?? range.row),
      endColumn: Math.max(
        range.startColumn ?? range.col ?? range.startCol,
        range.endColumn ?? range.startColumn ?? range.col ?? range.startCol
      )
    }))
    .filter((range: SlashCellRange) =>
      Number.isInteger(range.startRow) &&
      Number.isInteger(range.startColumn) &&
      Number.isInteger(range.endRow) &&
      Number.isInteger(range.endColumn)
    )
}

function getNormalizedRanges(
  accessor: IAccessor,
  ranges: SlashCellRange[] | undefined
): { primaryRanges: SlashCellRange[]; cleanupRanges: SlashCellRange[] } {
  const rawRanges = ranges || getCurrentRanges(accessor)
  if (!rawRanges.length) {
    return { primaryRanges: rawRanges, cleanupRanges: rawRanges }
  }

  const primaryRanges = new Map<string, SlashCellRange>()
  const cleanupRanges = new Map<string, SlashCellRange>()

  rawRanges.forEach(range => {
    const context = getWorksheetContextByRange(accessor, range) || getWorksheetContext(accessor)
    if (!context) {
      cleanupRanges.set(rangeKey(range), range)
      return
    }

    for (let row = range.startRow; row <= range.endRow; row++) {
      for (let col = range.startColumn; col <= range.endColumn; col++) {
        const mergedRange = getMergedRange(context.worksheet, row, col)
        const cleanupRange = mergedRange
          ? toSlashCellRange(context.unitId, context.sheetId, mergedRange)
          : toSingleCellRange(context.unitId, context.sheetId, row, col)
        cleanupRanges.set(rangeKey(cleanupRange), cleanupRange)

        const primaryRange = mergedRange
          ? toSingleCellRange(context.unitId, context.sheetId, mergedRange.startRow, mergedRange.startColumn)
          : toSingleCellRange(context.unitId, context.sheetId, row, col)
        primaryRanges.set(rangeKey(primaryRange), primaryRange)
      }
    }
  })

  return {
    primaryRanges: Array.from(primaryRanges.values()),
    cleanupRanges: Array.from(cleanupRanges.values())
  }
}

function applySlashCells(
  accessor: IAccessor,
  type: SlashCellType,
  params?: IApplySlashCellOperationParams
): boolean {
  const normalized = getNormalizedRanges(accessor, params?.ranges)
  if (!normalized.primaryRanges.length) {
    return false
  }
  const stateService = accessor.get(SlashCellStateService)
  stateService.clearCells(normalized.cleanupRanges)
  stateService.setCells(normalized.primaryRanges, type)
  refreshActiveSheet(accessor)
  return true
}

function getWorksheetContextByRange(accessor: IAccessor, range: SlashCellRange): WorksheetContext | undefined {
  const workbook = accessor
    .get(IUniverInstanceService)
    .getUnit(range.unitId, UniverInstanceType.UNIVER_SHEET) as any
  const worksheet = workbook?.getSheetBySheetId?.(range.sheetId)
  const unitId = workbook?.getUnitId?.() || workbook?.getId?.() || range.unitId
  const sheetId = worksheet?.getSheetId?.() || range.sheetId
  if (!workbook || !worksheet || !unitId || !sheetId) {
    return undefined
  }
  return { workbook, worksheet, unitId, sheetId }
}

function getMergedRange(worksheet: any, row: number, col: number): any | undefined {
  const range = worksheet?.getMergedCell?.(row, col)
  if (!range) {
    return undefined
  }
  const startRow = range.startRow ?? row
  const startColumn = range.startColumn ?? range.startCol ?? col
  const endRow = range.endRow ?? startRow
  const endColumn = range.endColumn ?? range.endCol ?? startColumn
  return { startRow, startColumn, endRow, endColumn }
}

function toSingleCellRange(unitId: string, sheetId: string, row: number, col: number): SlashCellRange {
  return {
    unitId,
    sheetId,
    startRow: row,
    startColumn: col,
    endRow: row,
    endColumn: col
  }
}

function toSlashCellRange(unitId: string, sheetId: string, range: any): SlashCellRange {
  return {
    unitId,
    sheetId,
    startRow: range.startRow,
    startColumn: range.startColumn,
    endRow: range.endRow,
    endColumn: range.endColumn
  }
}

function rangeKey(range: SlashCellRange): string {
  return `${range.unitId}:${range.sheetId}:${range.startRow}:${range.startColumn}:${range.endRow}:${range.endColumn}`
}

function refreshActiveSheet(accessor: IAccessor): void {
  const workbook = accessor
    .get(IUniverInstanceService)
    .getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET) as any
  const unitId = workbook?.getUnitId?.() || workbook?.getId?.()
  if (!unitId) {
    return
  }

  const currentRender = accessor.get(IRenderManagerService).getRenderById(unitId) as any
  currentRender?.mainComponent?.makeDirty?.(true)
  currentRender?.scene?.makeDirty?.(true)
  workbook?.getActiveSheet?.()?.refreshCanvas?.()
}
