import { Inject } from '@univerjs/core'
import { ClearSelectionFormatCommand } from '@univerjs/sheets'
import { ElMessage } from 'element-plus-secondary'
import type { PivotTableConfig } from '../types'
import { validatePivotConfig } from '../utils/pivot-config-validator'
import { PivotTableDataService } from './pivot-table-data.service'
import { PivotTableDisplayStateService } from './pivot-table-display-state.service'
import { PivotTableEditProtectionService } from './pivot-table-edit-protection.service'
import { PivotTableLayoutService } from './pivot-table-layout.service'
import { PivotTableRangeService } from './pivot-table-range.service'
import { PivotTableRenderStyleService } from './pivot-table-render-style.service'
import { PluginRenderLoadingService } from '../../../services/plugin-render-loading.service'
import { pluginRenderStatusService } from '../../../services/plugin-render-status.service'
import { SpreadsheetFilterRuntimeService } from '../../DataEaseFilterPlugin/services/filter-runtime.service'
import {
  addWorksheetMergesSilently,
  removeWorksheetMergesSilently
} from '../../../utils/silent-worksheet-merge'
import { TableRenderExpansionService } from '../../../services/table-render-expansion.service'

interface PivotTableFillOptions {
  initialRestore?: boolean
}

export class PivotTableFillService {
  private readonly dataService = new PivotTableDataService()
  private readonly layoutService = new PivotTableLayoutService()

  constructor(
    @Inject(PivotTableDisplayStateService)
    private readonly displayStateService: PivotTableDisplayStateService,
    @Inject(PivotTableRangeService)
    private readonly rangeService: PivotTableRangeService,
    @Inject(PivotTableRenderStyleService)
    private readonly renderStyleService: PivotTableRenderStyleService,
    @Inject(PivotTableEditProtectionService)
    private readonly editProtectionService: PivotTableEditProtectionService,
    @Inject(PluginRenderLoadingService)
    private readonly pluginRenderLoadingService: PluginRenderLoadingService,
    @Inject(SpreadsheetFilterRuntimeService)
    private readonly spreadsheetFilterRuntimeService: SpreadsheetFilterRuntimeService,
    @Inject(TableRenderExpansionService)
    private readonly tableRenderExpansionService: TableRenderExpansionService
  ) {}

  async fillByConfig(
    univerApi: any,
    config: PivotTableConfig,
    options: PivotTableFillOptions = {}
  ): Promise<boolean> {
    const workbook = univerApi.getActiveWorkbook?.()
    const worksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === config.placement.sheetId) || workbook?.getActiveSheet?.()
    if (!workbook || !worksheet) {
      throw new Error('未找到透视表目标工作表')
    }
    const unitId = workbook.getId?.() || workbook.getUnitId?.()
    const sheetId = worksheet.getSheetId?.()
    if (!unitId || !sheetId) {
      return this.fillByConfigSerial(univerApi, config, options)
    }

    return this.tableRenderExpansionService.runExclusive(unitId, sheetId, () =>
      this.fillByConfigSerial(univerApi, config, options)
    )
  }

  private async fillByConfigSerial(
    univerApi: any,
    config: PivotTableConfig,
    options: PivotTableFillOptions = {}
  ): Promise<boolean> {
    const workbook = univerApi.getActiveWorkbook?.()
    const worksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === config.placement.sheetId) || workbook?.getActiveSheet?.()
    if (!workbook || !worksheet) {
      throw new Error('未找到透视表目标工作表')
    }

    const unitId = workbook.getId?.() || workbook.getUnitId?.()
    const sheetId = worksheet.getSheetId()
    if (unitId) {
      config.placement.startCell = this.tableRenderExpansionService.resolveStartCell(
        unitId,
        'pivot',
        config.id,
        config.placement.startCell
      )
    }

    const validateMessage = validatePivotConfig(config)
    if (validateMessage) {
      ElMessage.warning(validateMessage)
      try {
        await this.clearPrevious(univerApi, config.id, worksheet, config.placement.startCell, true)
      } catch (clearError) {
        console.warn('[PivotTableFillService] Failed to clear previous data on validation failure:', clearError)
      }
      if (unitId) {
        pluginRenderStatusService.set({
          pluginId: config.id,
          type: 'pivot',
          status: 'error',
          reason: validateMessage,
          unitId,
          sheetId,
          startCell: config.placement.startCell,
          updatedAt: Date.now()
        })
      }
      return false
    }

    let start = this.rangeService.parseCell(config.placement.startCell)
    const previousState = this.displayStateService.get(config.id)
    const previousRange = previousState?.sheetId === sheetId &&
      previousState.rowCount > 0 && previousState.columnCount > 0
      ? previousState
      : undefined
    const loadingStart = previousRange
      ? this.rangeService.parseCell(previousRange.startCell)
      : start
    const loading = unitId
      ? this.pluginRenderLoadingService.begin({
          unitId,
          sheetId,
          pluginId: config.id,
          startRow: loadingStart.row,
          startColumn: loadingStart.col,
          rowCount: previousRange?.rowCount ?? 1,
          columnCount: previousRange?.columnCount ?? 1
        })
      : undefined

    if (unitId) {
      pluginRenderStatusService.set({
        pluginId: config.id,
        type: 'pivot',
        status: 'loading',
        unitId,
        sheetId,
        startCell: config.placement.startCell,
        updatedAt: Date.now()
      })
    }

    try {
      if (unitId) {
        await this.spreadsheetFilterRuntimeService.waitForValues(unitId)
      }
      const queryConfig = unitId
        ? this.spreadsheetFilterRuntimeService.applyQueryFilterToConfig(unitId, config)
        : config
      const result = await this.dataService.queryData(queryConfig)
      if (unitId) {
        // 查询期间可能发生合法的行列删除，写入前必须重新读取实例的最新坐标。
        config.placement.startCell = this.tableRenderExpansionService.resolveStartCell(
          unitId,
          'pivot',
          config.id,
          config.placement.startCell
        )
        start = this.rangeService.parseCell(config.placement.startCell)
      }
      const layout = this.layoutService.build(config, result)
      await this.ensureSheetSize(
        univerApi,
        worksheet,
        start.row + layout.rowCount,
        start.col + layout.columnCount
      )
      const expansionMessage = unitId
        ? await this.tableRenderExpansionService.ensureRenderSpace({
            unitId,
            sheetId,
            pluginId: config.id,
            tableType: 'pivot',
            worksheet,
            startCell: config.placement.startCell,
            rowCount: layout.rowCount,
            columnCount: layout.columnCount,
            initialRestore: options.initialRestore === true
          })
        : undefined
      const rangeMessage = expansionMessage || this.rangeService.validateBeforeFill(
        worksheet,
        config,
        layout.rowCount,
        layout.columnCount,
        options.initialRestore === true
      )
      if (rangeMessage) {
        ElMessage.warning(rangeMessage)
        if (unitId) {
          pluginRenderStatusService.set({
            pluginId: config.id,
            type: 'pivot',
            status: 'error',
            reason: rangeMessage,
            unitId,
            sheetId,
            startCell: config.placement.startCell,
            updatedAt: Date.now()
          })
        }
        return false
      }

      await this.clearPrevious(
        univerApi,
        config.id,
        worksheet,
        config.placement.startCell
      )
      this.editProtectionService.runWithoutProtection(() => {
        worksheet
          .getRange(start.row, start.col, layout.rowCount, layout.columnCount)
          .setValues(layout.values)
      })
      if (config.style?.base?.mergeCell) {
        await this.applyMerges(univerApi, worksheet, start, layout.merges)
      }

      this.displayStateService.set({
        pluginId: config.id,
        sheetId: worksheet.getSheetId(),
        startCell: config.placement.startCell,
        rowCount: layout.rowCount,
        columnCount: layout.columnCount,
        headerRowCount: layout.headerRowCount,
        headerColumnCount: layout.headerColumnCount,
        displayScales: layout.displayScales,
        dataRange: layout.dataRange,
        merges: layout.merges,
        updatedAt: Date.now()
      })
      this.updateRenderStyleRange(univerApi, config)
      this.refreshTargetSheet(univerApi, worksheet.getSheetId())
      if (unitId) {
        pluginRenderStatusService.set({
          pluginId: config.id,
          type: 'pivot',
          status: 'rendered',
          unitId,
          sheetId,
          startCell: config.placement.startCell,
          updatedAt: Date.now()
        })
      }
      return true
    } catch (error) {
      console.error('[PivotTableFillService] Failed to fill pivot table:', error)
      try {
        await this.clearPrevious(univerApi, config.id, worksheet, config.placement.startCell, true)
      } catch (clearError) {
        console.warn('[PivotTableFillService] Failed to clear previous data:', clearError)
      }
      if (unitId) {
        pluginRenderStatusService.set({
          pluginId: config.id,
          type: 'pivot',
          status: 'error',
          reason: error instanceof Error ? error.message : 'Unknown error',
          unitId,
          sheetId,
          startCell: config.placement.startCell,
          updatedAt: Date.now()
        })
      }
      return false
    } finally {
      loading?.dispose()
    }
  }

  async applyStyleOnly(univerApi: any, config: PivotTableConfig): Promise<boolean> {
    const state = this.displayStateService.get(config.id)
    if (!state || state.rowCount <= 0 || state.columnCount <= 0) {
      return false
    }

    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const worksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === state.sheetId)
    if (!unitId || !worksheet) {
      return false
    }

    const start = this.rangeService.parseCell(state.startCell)
    await this.editProtectionService.runWithoutProtection(() =>
      removeWorksheetMergesSilently(univerApi, {
        unitId,
        sheetId: state.sheetId,
        ranges: [{
          startRow: start.row,
          endRow: start.row + state.rowCount - 1,
          startColumn: start.col,
          endColumn: start.col + state.columnCount - 1
        }]
      })
    )

    if (config.style?.base?.mergeCell && state.merges?.length) {
      await this.applyMerges(univerApi, worksheet, start, state.merges)
    }

    this.updateRenderStyleRange(univerApi, config)
    this.refreshTargetSheet(univerApi, state.sheetId)
    return true
  }

  async clearTableData(univerApi: any, pluginId: string): Promise<void> {
    const state = this.displayStateService.get(pluginId)
    if (!state) {
      return
    }

    const worksheet = univerApi
      .getActiveWorkbook?.()
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === state.sheetId)
    if (!worksheet) {
      return
    }

    await this.clearPrevious(univerApi, pluginId, worksheet, state.startCell, true)
    this.displayStateService.delete(pluginId)
    this.refreshTargetSheet(univerApi, state.sheetId)
  }

  private async clearPrevious(
    univerApi: any,
    pluginId: string,
    targetWorksheet: any,
    targetStartCell: string,
    clearFormat = false
  ): Promise<void> {
    const previous = this.displayStateService.get(pluginId)
    if (!previous) {
      return
    }
    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const worksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === previous.sheetId)
    if (!worksheet) {
      if (unitId) {
        this.renderStyleService.deleteRange(unitId, previous.sheetId, pluginId)
      }
      return
    }

    const moved =
      previous.sheetId !== targetWorksheet?.getSheetId?.() ||
      previous.startCell !== targetStartCell
    const start = this.rangeService.parseCell(previous.startCell)
    await this.ensureSheetSize(
      univerApi,
      worksheet,
      start.row + previous.rowCount,
      start.col + previous.columnCount
    )

    if (unitId) {
      await this.editProtectionService.runWithoutProtection(() =>
        removeWorksheetMergesSilently(univerApi, {
          unitId,
          sheetId: previous.sheetId,
          ranges: [{
            startRow: start.row,
            endRow: start.row + previous.rowCount - 1,
            startColumn: start.col,
            endColumn: start.col + previous.columnCount - 1
          }]
        })
      )
      this.renderStyleService.deleteRange(unitId, previous.sheetId, pluginId)

      if (clearFormat || moved) {
        await this.editProtectionService.runWithoutProtection(() =>
          univerApi.executeCommand?.(ClearSelectionFormatCommand.id, {
            unitId,
            subUnitId: previous.sheetId,
            ranges: [{
              startRow: start.row,
              endRow: start.row + previous.rowCount - 1,
              startColumn: start.col,
              endColumn: start.col + previous.columnCount - 1
            }]
          })
        )
      }
    }

    const emptyValues = Array.from({ length: previous.rowCount }, () =>
      Array.from({ length: previous.columnCount }, () => '')
    )
    this.editProtectionService.runWithoutProtection(() => {
      worksheet
        .getRange(start.row, start.col, previous.rowCount, previous.columnCount)
        .setValues(emptyValues)
    })

    if (moved) {
      this.refreshTargetSheet(univerApi, previous.sheetId)
    }
  }

  private async applyMerges(
    univerApi: any,
    worksheet: any,
    start: { row: number; col: number },
    merges: Array<{ startRow: number; endRow: number; startColumn: number; endColumn: number }>
  ): Promise<void> {
    if (!merges.length) {
      return
    }

    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const subUnitId = worksheet?.getSheetId?.()
    if (!unitId || !subUnitId) {
      return
    }

    const selections = merges.map(range => ({
      startRow: start.row + range.startRow,
      endRow: start.row + range.endRow,
      startColumn: start.col + range.startColumn,
      endColumn: start.col + range.endColumn
    }))

    await this.editProtectionService.runWithoutProtection(() =>
      addWorksheetMergesSilently(univerApi, {
        unitId,
        sheetId: subUnitId,
        ranges: selections
      })
    )
  }

  private updateRenderStyleRange(univerApi: any, config: PivotTableConfig): void {
    const state = this.displayStateService.get(config.id)
    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (!state || !unitId) {
      return
    }

    const start = this.rangeService.parseCell(state.startCell)
    this.renderStyleService.setRange({
      pluginId: config.id,
      unitId,
      sheetId: state.sheetId,
      startRow: start.row,
      startColumn: start.col,
      rowCount: state.rowCount,
      columnCount: state.columnCount,
      headerRowCount: state.headerRowCount,
      headerColumnCount: state.headerColumnCount,
      displayScales: state.displayScales,
      dataRange: state.dataRange,
      config
    })
  }

  private refreshTargetSheet(univerApi: any, sheetId: string): void {
    const worksheet = univerApi
      .getActiveWorkbook?.()
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === sheetId)

    worksheet?.refreshCanvas?.()
  }

  private async ensureSheetSize(
    univerApi: any,
    worksheet: any,
    requiredRows: number,
    requiredColumns: number
  ): Promise<void> {
    const currentRows = this.getSheetRowCount(worksheet)
    const currentColumns = this.getSheetColumnCount(worksheet)

    if (currentRows > 0 && requiredRows > currentRows) {
      await this.expandRows(univerApi, worksheet, currentRows, requiredRows)
    }
    if (currentColumns > 0 && requiredColumns > currentColumns) {
      await this.expandColumns(univerApi, worksheet, currentColumns, requiredColumns)
    }

    const expandedRows = this.getSheetRowCount(worksheet)
    const expandedColumns = this.getSheetColumnCount(worksheet)
    if (
      expandedRows > 0 &&
      expandedColumns > 0 &&
      (expandedRows < requiredRows || expandedColumns < requiredColumns)
    ) {
      throw new Error(
        `工作表空间不足，自动扩容失败。当前 ${expandedRows} 行 ${expandedColumns} 列，需要 ${requiredRows} 行 ${requiredColumns} 列`
      )
    }
  }

  private getSheetRowCount(worksheet: any): number {
    return this.firstFiniteNumber(
      worksheet?.getRowCount?.(),
      worksheet?.getMaxRows?.(),
      worksheet?.getSheet?.()?.getRowCount?.(),
      worksheet?.getSheet?.()?.getMaxRows?.(),
      worksheet?.getSnapshot?.()?.rowCount,
      worksheet?.getSheet?.()?.getSnapshot?.()?.rowCount
    )
  }

  private getSheetColumnCount(worksheet: any): number {
    return this.firstFiniteNumber(
      worksheet?.getColumnCount?.(),
      worksheet?.getMaxColumns?.(),
      worksheet?.getSheet?.()?.getColumnCount?.(),
      worksheet?.getSheet?.()?.getMaxColumns?.(),
      worksheet?.getSnapshot?.()?.columnCount,
      worksheet?.getSheet?.()?.getSnapshot?.()?.columnCount
    )
  }

  private firstFiniteNumber(...values: unknown[]): number {
    const value = values.find(item => Number.isFinite(Number(item)))
    return value === undefined ? 0 : Number(value)
  }

  private async expandRows(
    univerApi: any,
    worksheet: any,
    currentRows: number,
    requiredRows: number
  ): Promise<void> {
    const appendCount = requiredRows - currentRows
    if (appendCount <= 0) {
      return
    }

    if (await this.callFirstAvailable(worksheet, [
      ['setRowCount', requiredRows],
      ['setRowsCount', requiredRows],
      ['resizeRows', requiredRows],
      ['insertRowsAfter', currentRows - 1, appendCount],
      ['insertRows', currentRows, appendCount],
      ['appendRows', appendCount]
    ])) {
      return
    }

    const sheetModel = worksheet?.getSheet?.()
    if (await this.callFirstAvailable(sheetModel, [
      ['setRowCount', requiredRows],
      ['setRowsCount', requiredRows],
      ['resizeRows', requiredRows],
      ['insertRowsAfter', currentRows - 1, appendCount],
      ['insertRows', currentRows, appendCount],
      ['appendRows', appendCount]
    ])) {
      return
    }

    await this.executeInsertCommand(univerApi, worksheet, 'sheet.command.insert-row', {
      startRow: currentRows,
      endRow: requiredRows - 1,
      startColumn: 0,
      endColumn: Math.max(this.getSheetColumnCount(worksheet) - 1, 0)
    })
  }

  private async expandColumns(
    univerApi: any,
    worksheet: any,
    currentColumns: number,
    requiredColumns: number
  ): Promise<void> {
    const appendCount = requiredColumns - currentColumns
    if (appendCount <= 0) {
      return
    }

    if (await this.callFirstAvailable(worksheet, [
      ['setColumnCount', requiredColumns],
      ['setColumnsCount', requiredColumns],
      ['resizeColumns', requiredColumns],
      ['insertColumnsAfter', currentColumns - 1, appendCount],
      ['insertColumns', currentColumns, appendCount],
      ['insertCols', currentColumns, appendCount],
      ['appendColumns', appendCount],
      ['appendCols', appendCount]
    ])) {
      return
    }

    const sheetModel = worksheet?.getSheet?.()
    if (await this.callFirstAvailable(sheetModel, [
      ['setColumnCount', requiredColumns],
      ['setColumnsCount', requiredColumns],
      ['resizeColumns', requiredColumns],
      ['insertColumnsAfter', currentColumns - 1, appendCount],
      ['insertColumns', currentColumns, appendCount],
      ['insertCols', currentColumns, appendCount],
      ['appendColumns', appendCount],
      ['appendCols', appendCount]
    ])) {
      return
    }

    await this.executeInsertCommand(univerApi, worksheet, 'sheet.command.insert-col', {
      startRow: 0,
      endRow: Math.max(this.getSheetRowCount(worksheet) - 1, 0),
      startColumn: currentColumns,
      endColumn: requiredColumns - 1
    })
  }

  private async callFirstAvailable(target: any, calls: Array<[string, ...unknown[]]>): Promise<boolean> {
    if (!target) {
      return false
    }

    for (const [method, ...args] of calls) {
      if (typeof target[method] !== 'function') {
        continue
      }
      await Promise.resolve(target[method](...args))
      return true
    }
    return false
  }

  private async executeInsertCommand(
    univerApi: any,
    worksheet: any,
    commandId: string,
    range: { startRow: number; endRow: number; startColumn: number; endColumn: number }
  ): Promise<void> {
    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const subUnitId = worksheet?.getSheetId?.()
    if (!unitId || !subUnitId) {
      throw new Error('工作表空间不足，且无法自动扩容')
    }

    await this.editProtectionService.runWithoutProtection(() =>
      univerApi.executeCommand?.(commandId, {
        unitId,
        subUnitId,
        range
      })
    )
  }
}
