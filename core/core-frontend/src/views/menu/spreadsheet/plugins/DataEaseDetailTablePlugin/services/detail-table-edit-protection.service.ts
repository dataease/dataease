import {
  ClearSelectionContentCommand,
  DeleteRangeMoveLeftCommand,
  DeleteRangeMoveUpCommand,
  InsertColByRangeCommand,
  InsertRangeMoveDownCommand,
  InsertRangeMoveRightCommand,
  InsertRowByRangeCommand,
  MoveColsCommand,
  MoveRangeCommand,
  MoveRowsCommand,
  RemoveColByRangeCommand,
  RemoveRowByRangeCommand,
  SetRangeValuesCommand,
  SetRangeValuesMutation,
  SheetsSelectionsService,
  SplitTextToColumnsCommand,
  TextToNumberCommand,
  AutoFillCommand,
  AutoClearContentCommand,
  AddWorksheetMergeCommand,
  RemoveWorksheetMergeCommand
} from '@univerjs/sheets'
import {
  SetSheetFilterRangeCommand,
  SetSheetsFilterCriteriaCommand,
  SetSheetsFilterCriteriaMutation,
  SetSheetsFilterRangeMutation,
  SheetsFilterService,
  SmartToggleSheetsFilterCommand
} from '@univerjs/sheets-filter'
import { OpenFilterPanelOperation } from '@univerjs/sheets-filter-ui'
import {
  DeleteRangeMoveLeftConfirmCommand,
  DeleteRangeMoveUpConfirmCommand,
  IEditorBridgeService,
  InsertRangeMoveDownConfirmCommand,
  InsertRangeMoveRightConfirmCommand,
  SetCellEditVisibleOperation,
  SheetCutCommand
} from '@univerjs/sheets-ui'
import {
  SortRangeAscCommand,
  SortRangeAscExtCommand,
  SortRangeAscExtInCtxMenuCommand,
  SortRangeAscInCtxMenuCommand,
  SortRangeCustomCommand,
  SortRangeCustomInCtxMenuCommand,
  SortRangeDescCommand,
  SortRangeDescExtCommand,
  SortRangeDescExtInCtxMenuCommand,
  SortRangeDescInCtxMenuCommand
} from '@univerjs/sheets-sort-ui'
import {
  CustomCommandExecutionError,
  IUniverInstanceService,
  Inject,
  type ICommandInfo,
  type IRange,
  UniverInstanceType
} from '@univerjs/core'
import { ElMessage } from 'element-plus-secondary'
import { DetailTableDisplayStateService } from './detail-table-display-state.service'
import type { DetailTableDisplayState } from './detail-table-display-state.service'
import { PluginRenderStatusService } from '../../DataEaseRuntimePlugin/services/table'
import { SpreadsheetModeService } from '../../../services/spreadsheet-mode.service'
import { isPresentationOnlyCellValueMutation } from '../../../services/plugin-render-range-edit-policy'
import {
  ApplyThreeSlashCellOperation,
  ApplyTwoSlashCellOperation,
  ClearSlashCellOperation
} from '../../DataEaseSlashCellPlugin/commands/operations'

type ProtectedRange = IRange & {
  sheetId: string
}

const INSERT_ROW_COMMAND_ID = 'sheet.command.insert-row'
const INSERT_COL_COMMAND_ID = 'sheet.command.insert-col'
const REMOVE_ROW_COMMAND_ID = 'sheet.command.remove-row'
const REMOVE_COL_COMMAND_ID = 'sheet.command.remove-col'
const OPEN_TABLE_SELECTOR_OPERATION_ID = 'sheet.operation.open-table-selector'
const ADD_SHEET_TABLE_COMMAND_ID = 'sheet.command.add-table'
const INSERT_TABLE_COL_COMMAND_ID = 'sheet.command.table-insert-col'
const REMOVE_TABLE_COL_COMMAND_ID = 'sheet.command.table-remove-col'
const SORT_RANGE_COMMAND_ID = 'sheet.command.sort-range'

export class DetailTableEditProtectionService {
  // 格式刷和“清除全部”由共享白名单策略收敛为纯用户样式写入，不再作为内容编辑拦截。
  private readonly valueCommandIds = new Set([
    SetRangeValuesCommand.id,
    SetRangeValuesMutation.id,
    ClearSelectionContentCommand.id,
    TextToNumberCommand.id,
    SplitTextToColumnsCommand.id,
    AutoFillCommand.id,
    AutoClearContentCommand.id,
    SetCellEditVisibleOperation.id,
    SheetCutCommand.id,
    SheetCutCommand.name
  ])
  private readonly structureCommandIds = new Set([
    INSERT_ROW_COMMAND_ID,
    INSERT_COL_COMMAND_ID,
    REMOVE_ROW_COMMAND_ID,
    REMOVE_COL_COMMAND_ID,
    InsertRowByRangeCommand.id,
    InsertColByRangeCommand.id,
    RemoveRowByRangeCommand.id,
    RemoveColByRangeCommand.id,
    DeleteRangeMoveUpCommand.id,
    DeleteRangeMoveLeftCommand.id,
    InsertRangeMoveDownCommand.id,
    InsertRangeMoveRightCommand.id,
    DeleteRangeMoveUpConfirmCommand.id,
    DeleteRangeMoveLeftConfirmCommand.id,
    InsertRangeMoveDownConfirmCommand.id,
    InsertRangeMoveRightConfirmCommand.id,
    MoveRangeCommand.id,
    MoveRowsCommand.id,
    MoveColsCommand.id,
    AddWorksheetMergeCommand.id,
    RemoveWorksheetMergeCommand.id,
    OPEN_TABLE_SELECTOR_OPERATION_ID,
    ADD_SHEET_TABLE_COMMAND_ID,
    INSERT_TABLE_COL_COMMAND_ID,
    REMOVE_TABLE_COL_COMMAND_ID,
    SortRangeAscCommand.id,
    SortRangeAscExtCommand.id,
    SortRangeDescCommand.id,
    SortRangeDescExtCommand.id,
    SortRangeCustomCommand.id,
    SortRangeAscInCtxMenuCommand.id,
    SortRangeAscExtInCtxMenuCommand.id,
    SortRangeDescInCtxMenuCommand.id,
    SortRangeDescExtInCtxMenuCommand.id,
    SortRangeCustomInCtxMenuCommand.id,
    SORT_RANGE_COMMAND_ID
  ])
  private readonly slashCellCommandIds = new Set([
    ApplyTwoSlashCellOperation.id,
    ApplyThreeSlashCellOperation.id,
    ClearSlashCellOperation.id
  ])
  private readonly filterCommandIds = new Set([
    SmartToggleSheetsFilterCommand.id,
    SetSheetFilterRangeCommand.id,
    SetSheetsFilterRangeMutation.id,
    SetSheetsFilterCriteriaCommand.id,
    SetSheetsFilterCriteriaMutation.id,
    OpenFilterPanelOperation.id
  ])
  private readonly pluginCommandIds = new Set([
    'dataease.operation.apply-detail-table',
    'dataease.operation.apply-detail-table-style',
    'dataease.operation.clear-detail-table'
  ])
  private _suspendCount = 0

  constructor(
    @Inject(DetailTableDisplayStateService)
    private readonly displayStateService: DetailTableDisplayStateService,
    @Inject(SheetsSelectionsService)
    private readonly selectionService: SheetsSelectionsService,
    @IUniverInstanceService
    private readonly univerInstanceService: IUniverInstanceService,
    @IEditorBridgeService
    private readonly editorBridgeService: IEditorBridgeService,
    @Inject(SheetsFilterService)
    private readonly sheetsFilterService: SheetsFilterService,
    @Inject(SpreadsheetModeService)
    private readonly spreadsheetModeService: SpreadsheetModeService,
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService
  ) {}

  runWithoutProtection<T>(handler: () => T): T {
    return this.spreadsheetModeService.runAsSystemWrite(() => {
      this._suspendCount += 1
      try {
        const result = handler()
        if (result instanceof Promise) {
          return result.finally(() => {
            this._suspendCount -= 1
          }) as T
        }

        this._suspendCount -= 1
        return result
      } catch (error) {
        this._suspendCount -= 1
        throw error
      }
    })
  }

  assertCommandAllowed(commandInfo: Readonly<ICommandInfo>): void {
    if (!this.shouldBlock(commandInfo)) {
      return
    }

    if (commandInfo.id === SetCellEditVisibleOperation.id) {
      throw new CustomCommandExecutionError('Detail table render range is readonly')
    }

    ElMessage.warning('明细表渲染区域不允许编辑')
    throw new CustomCommandExecutionError('Detail table render range is readonly')
  }

  shouldBlock(commandInfo: Readonly<ICommandInfo>): boolean {
    if (
      this._suspendCount > 0 ||
      this.spreadsheetModeService.isSystemWrite() ||
      this.pluginCommandIds.has(commandInfo.id)
    ) {
      return false
    }

    if (
      commandInfo.id === SetRangeValuesMutation.id &&
      this.isPresentationOnlyMutation(commandInfo)
    ) {
      return false
    }

    if (
      !this.valueCommandIds.has(commandInfo.id) &&
      !this.structureCommandIds.has(commandInfo.id) &&
      !this.slashCellCommandIds.has(commandInfo.id) &&
      !this.filterCommandIds.has(commandInfo.id)
    ) {
      return false
    }

    const ranges = this.getCommandRanges(commandInfo)
    if (!ranges.length) {
      return false
    }

    return ranges.some(range =>
      this.getProtectedRanges(range.sheetId).some(protectedRange =>
        this.isRangeAffected(commandInfo.id, range, protectedRange)
      )
    )
  }

  private getCommandRanges(commandInfo: Readonly<ICommandInfo>): ProtectedRange[] {
    const params = (commandInfo.params || {}) as any
    const unitId = params.unitId || params.toUnitId || params.fromUnitId || this.getCurrentUnitId()
    const sheetId = params.subUnitId || params.toSubUnitId || params.fromSubUnitId || this.getCurrentSheetId(unitId)
    const withSheet = (range?: IRange, targetSheetId = sheetId): ProtectedRange[] =>
      range && targetSheetId ? [{ ...range, sheetId: targetSheetId }] : []
    const withRanges = (ranges?: IRange[], targetSheetId = sheetId): ProtectedRange[] =>
      (ranges || []).flatMap(range => withSheet(range, targetSheetId))

    // 斜线单元格只写插件状态，不会进入 SetRangeValuesMutation，
    // 必须在外层 operation 执行前按真实影响范围完成保护判断。
    if (this.slashCellCommandIds.has(commandInfo.id)) {
      return this.getSlashCellCommandRanges(params, unitId, sheetId)
    }

    switch (commandInfo.id) {
      case SmartToggleSheetsFilterCommand.id:
        return withRanges(this.getSelectionRanges(), sheetId)
      case SetSheetFilterRangeCommand.id:
      case SetSheetsFilterRangeMutation.id:
        return withSheet(params.range, params.subUnitId || sheetId)
      case SetSheetsFilterCriteriaCommand.id:
      case SetSheetsFilterCriteriaMutation.id:
      case OpenFilterPanelOperation.id:
        return this.getFilterColumnRange(
          params.unitId || unitId,
          params.subUnitId || sheetId,
          params.col
        )
      case SetRangeValuesCommand.id:
        {
          const valueRanges = this.matrixToRanges(params.value)
          return withRanges(params.range ? [params.range] : valueRanges.length ? valueRanges : this.getSelectionRanges(), sheetId)
        }
      case SetRangeValuesMutation.id:
        return withRanges(this.matrixToRanges(params.cellValue), params.subUnitId || sheetId)
      case ClearSelectionContentCommand.id:
      case TextToNumberCommand.id:
      case SheetCutCommand.id:
      case SheetCutCommand.name:
      case DeleteRangeMoveUpConfirmCommand.id:
      case DeleteRangeMoveLeftConfirmCommand.id:
      case InsertRangeMoveDownConfirmCommand.id:
      case InsertRangeMoveRightConfirmCommand.id:
      case OPEN_TABLE_SELECTOR_OPERATION_ID:
      case INSERT_TABLE_COL_COMMAND_ID:
      case REMOVE_TABLE_COL_COMMAND_ID:
      case SortRangeAscCommand.id:
      case SortRangeAscExtCommand.id:
      case SortRangeDescCommand.id:
      case SortRangeDescExtCommand.id:
      case SortRangeCustomCommand.id:
      case SortRangeAscInCtxMenuCommand.id:
      case SortRangeAscExtInCtxMenuCommand.id:
      case SortRangeDescInCtxMenuCommand.id:
      case SortRangeDescExtInCtxMenuCommand.id:
      case SortRangeCustomInCtxMenuCommand.id:
        return withRanges(params.ranges || this.getSelectionRanges(), sheetId)
      case ADD_SHEET_TABLE_COMMAND_ID:
      case SORT_RANGE_COMMAND_ID:
        return withSheet(params.range, params.subUnitId || sheetId)
      case SplitTextToColumnsCommand.id:
        return withSheet(params.range, sheetId)
      case AutoFillCommand.id:
        return [
          ...withSheet(params.sourceRange, sheetId),
          ...withSheet(params.targetRange, sheetId)
        ]
      case AutoClearContentCommand.id:
        return withSheet(params.clearRange, sheetId)
      case SetCellEditVisibleOperation.id:
        {
          if (params.visible !== true) {
            return []
          }
          const editLocation = this.editorBridgeService.getEditLocation()
          return editLocation
            ? withSheet({
                startRow: editLocation.row,
                endRow: editLocation.row,
                startColumn: editLocation.column,
                endColumn: editLocation.column
              }, editLocation.sheetId)
            : withRanges(this.getSelectionRanges(), sheetId)
        }
      case AddWorksheetMergeCommand.id:
        return withRanges(params.selections || this.getSelectionRanges(), sheetId)
      case RemoveWorksheetMergeCommand.id:
        return withRanges(params.ranges || this.getSelectionRanges(), sheetId)
      case MoveRangeCommand.id:
        return [
          ...withSheet(params.fromRange, params.fromSubUnitId || sheetId),
          ...withSheet(params.toRange, params.toSubUnitId || sheetId)
        ]
      case MoveRowsCommand.id:
      case MoveColsCommand.id:
        return [
          ...withSheet(params.fromRange || params.range, sheetId),
          ...withSheet(params.toRange, sheetId)
        ]
      default:
        return withSheet(params.range, sheetId)
    }
  }

  private isRangeAffected(commandId: string, range: ProtectedRange, protectedRange: ProtectedRange): boolean {
    if (
      commandId === INSERT_ROW_COMMAND_ID ||
      commandId === InsertRowByRangeCommand.id
    ) {
      return this.isRowShiftAffected(range, protectedRange)
    }

    if (
      commandId === INSERT_COL_COMMAND_ID ||
      commandId === InsertColByRangeCommand.id
    ) {
      return this.isColumnShiftAffected(range, protectedRange)
    }

    if (
      commandId === REMOVE_ROW_COMMAND_ID ||
      commandId === RemoveRowByRangeCommand.id
    ) {
      // 删除整行只禁止切到实例内部；实例之前的删除由共享服务负责同步坐标。
      return this.rowsOverlap(range, protectedRange)
    }

    if (
      commandId === REMOVE_COL_COMMAND_ID ||
      commandId === RemoveColByRangeCommand.id
    ) {
      return this.columnsOverlap(range, protectedRange)
    }

    if (
      commandId === DeleteRangeMoveUpCommand.id ||
      commandId === InsertRangeMoveDownCommand.id ||
      commandId === DeleteRangeMoveUpConfirmCommand.id ||
      commandId === InsertRangeMoveDownConfirmCommand.id
    ) {
      return this.columnsOverlap(range, protectedRange) && range.startRow <= protectedRange.endRow
    }

    if (
      commandId === DeleteRangeMoveLeftCommand.id ||
      commandId === InsertRangeMoveRightCommand.id ||
      commandId === DeleteRangeMoveLeftConfirmCommand.id ||
      commandId === InsertRangeMoveRightConfirmCommand.id ||
      commandId === INSERT_TABLE_COL_COMMAND_ID ||
      commandId === REMOVE_TABLE_COL_COMMAND_ID
    ) {
      return this.rowsOverlap(range, protectedRange) && range.startColumn <= protectedRange.endColumn
    }

    if (commandId === MoveRowsCommand.id) {
      return this.isRowShiftAffected(range, protectedRange)
    }

    if (commandId === MoveColsCommand.id) {
      return this.isColumnShiftAffected(range, protectedRange)
    }

    return this.intersects(range, protectedRange)
  }

  private isRowShiftAffected(range: ProtectedRange, protectedRange: ProtectedRange): boolean {
    return range.startRow <= protectedRange.endRow
  }

  private isColumnShiftAffected(range: ProtectedRange, protectedRange: ProtectedRange): boolean {
    return range.startColumn <= protectedRange.endColumn
  }

  private toProtectedRange(state: DetailTableDisplayState): ProtectedRange {
    const start = this.parseCell(state.startCell)
    return {
      sheetId: state.sheetId,
      startRow: start.row,
      endRow: start.row + state.rowCount - 1,
      startColumn: start.col,
      endColumn: start.col + state.colCount - 1
    }
  }

  private getProtectedRanges(sheetId?: string): ProtectedRange[] {
    const renderedRanges = this.displayStateService
      .list()
      .filter(state => !sheetId || state.sheetId === sheetId)
      .map(state => this.toProtectedRange(state))

    const placeholderRanges = this.pluginRenderStatusService
      .list()
      .filter(
        status =>
          status.type === 'detail' &&
          status.sheetId &&
          status.startCell &&
          (!sheetId || status.sheetId === sheetId) &&
          (status.status === 'loading' ||
            status.status === 'error' ||
            status.status === 'empty' ||
            status.status === 'draft')
      )
      .map(status => {
        const start = this.parseCell(status.startCell!)
        return {
          sheetId: status.sheetId!,
          startRow: start.row,
          endRow: start.row,
          startColumn: start.col,
          endColumn: start.col
        }
      })

    return [...renderedRanges, ...placeholderRanges]
  }

  private getSelectionRanges(): IRange[] {
    return this.selectionService.getCurrentSelections()?.map(selection => selection.range) || []
  }

  private getFilterColumnRange(
    unitId: string | undefined,
    sheetId: string | undefined,
    column: number | undefined
  ): ProtectedRange[] {
    if (!unitId || !sheetId || typeof column !== 'number' || !Number.isFinite(column)) {
      return []
    }

    const filterColumn = column
    const filterRange = this.sheetsFilterService.getFilterModel(unitId, sheetId)?.getRange()
    if (
      !filterRange ||
      filterColumn < filterRange.startColumn ||
      filterColumn > filterRange.endColumn
    ) {
      return []
    }

    // 过滤条件命令只携带列号，需要还原该列在完整过滤范围内的实际影响区域。
    return [{
      ...filterRange,
      sheetId,
      startColumn: filterColumn,
      endColumn: filterColumn
    }]
  }

  private getSlashCellCommandRanges(
    params: any,
    fallbackUnitId?: string,
    fallbackSheetId?: string
  ): ProtectedRange[] {
    const ranges = Array.isArray(params.ranges) && params.ranges.length
      ? params.ranges
      : this.getSelectionRanges().map(range => ({
          ...range,
          unitId: fallbackUnitId,
          sheetId: fallbackSheetId
        }))

    return ranges.flatMap((range: any) => {
      const unitId = range.unitId || fallbackUnitId
      const sheetId = range.sheetId || range.subUnitId || fallbackSheetId
      return sheetId ? [this.expandMergedRange(range, unitId, sheetId)] : []
    })
  }

  private expandMergedRange(
    range: IRange,
    unitId: string | undefined,
    sheetId: string
  ): ProtectedRange {
    const workbook = unitId
      ? this.univerInstanceService.getUniverSheetInstance(unitId)
      : undefined
    const worksheet = workbook?.getSheetBySheetId?.(sheetId)
    const expanded: ProtectedRange = { ...range, sheetId }
    if (!worksheet) {
      return expanded
    }

    // 斜线状态会归一化到合并主单元格，并在切换或清除时处理完整合并区域。
    // 这里使用相同的影响范围，避免选择合并区域中的子单元格绕过明细表保护。
    for (let row = range.startRow; row <= range.endRow; row++) {
      for (let column = range.startColumn; column <= range.endColumn; column++) {
        const merged = worksheet.getMergedCell?.(row, column)
        if (!merged) {
          continue
        }

        expanded.startRow = Math.min(expanded.startRow, merged.startRow ?? row)
        expanded.endRow = Math.max(expanded.endRow, merged.endRow ?? row)
        expanded.startColumn = Math.min(
          expanded.startColumn,
          merged.startColumn ?? merged.startCol ?? column
        )
        expanded.endColumn = Math.max(
          expanded.endColumn,
          merged.endColumn ?? merged.endCol ?? column
        )
      }
    }

    return expanded
  }

  private matrixToRanges(cellValue?: Record<number, Record<number, unknown>>): IRange[] {
    if (!cellValue) {
      return []
    }

    return Object.entries(cellValue).flatMap(([rowKey, rowData]) => {
      if (!Number.isFinite(Number(rowKey))) {
        return []
      }

      return Object.keys(rowData || {}).filter(colKey => Number.isFinite(Number(colKey))).map(colKey => {
        const row = Number(rowKey)
        const col = Number(colKey)
        return {
          startRow: row,
          endRow: row,
          startColumn: col,
          endColumn: col
        }
      })
    })
  }

  private intersects(range: IRange, target: IRange): boolean {
    return this.rowsOverlap(range, target) && this.columnsOverlap(range, target)
  }

  private rowsOverlap(range: IRange, target: IRange): boolean {
    return range.startRow <= target.endRow && range.endRow >= target.startRow
  }

  private columnsOverlap(range: IRange, target: IRange): boolean {
    return range.startColumn <= target.endColumn && range.endColumn >= target.startColumn
  }

  private getCurrentUnitId(): string | undefined {
    return this.univerInstanceService
      .getCurrentUnitOfType<any>(UniverInstanceType.UNIVER_SHEET)
      ?.getUnitId?.()
  }

  private isPresentationOnlyMutation(commandInfo: Readonly<ICommandInfo>): boolean {
    const params = (commandInfo.params || {}) as any
    const unitId = params.unitId || this.getCurrentUnitId()
    const sheetId = params.subUnitId || this.getCurrentSheetId(unitId)
    const workbook = unitId
      ? this.univerInstanceService.getUniverSheetInstance(unitId)
      : undefined
    const worksheet = sheetId ? workbook?.getSheetBySheetId?.(sheetId) : undefined
    if (!worksheet) {
      return false
    }

    const protectedRanges = this.getProtectedRanges(sheetId)

    return isPresentationOnlyCellValueMutation(
      params.cellValue,
      (row, column) => worksheet.getCellRaw?.(row, column),
      (row, column) => protectedRanges.some(range =>
        row >= range.startRow &&
        row <= range.endRow &&
        column >= range.startColumn &&
        column <= range.endColumn
      )
    )
  }

  private getCurrentSheetId(unitId?: string): string | undefined {
    const workbook = unitId
      ? this.univerInstanceService.getUniverSheetInstance(unitId)
      : this.univerInstanceService.getCurrentUnitOfType<any>(UniverInstanceType.UNIVER_SHEET)
    return workbook?.getActiveSheet?.()?.getSheetId?.()
  }

  private parseCell(cellAddress: string) {
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
