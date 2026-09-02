import { Inject } from '@univerjs/core'
import { IRenderManagerService } from '@univerjs/engine-render'
import { ElMessage } from 'element-plus-secondary'
import type { PivotTableConfig } from '../types'
import { validatePivotConfig } from '../utils/pivot-config-validator'
import { PivotTableDataService } from './pivot-table-data.service'
import { PivotTableDisplayStateService } from './pivot-table-display-state.service'
import { PivotTableEditProtectionService } from './pivot-table-edit-protection.service'
import {
  PivotTableLayoutService,
  type PivotLayoutRange,
  type PivotTableValueRegion
} from './pivot-table-layout.service'
import { PivotTableRangeService } from './pivot-table-range.service'
import { PivotTableRenderStyleService } from './pivot-table-render-style.service'
import {
  PluginRenderLoadingService,
  PluginRenderStatusService
} from '../../DataEaseRuntimePlugin/services/table'
import { SpreadsheetFilterRuntimeService } from '../../DataEaseFilterPlugin/services/filter-runtime.service'
import {
  addWorksheetMergesSilently,
  removeWorksheetMergesSilently
} from '../../../utils/silent-worksheet-merge'
import {
  clearWorksheetFormatsSilently,
  setWorksheetColumnCountSilently,
  setWorksheetRowCountSilently,
  setWorksheetValuesSilently
} from '../../../utils/silent-worksheet-write'
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
    private readonly tableRenderExpansionService: TableRenderExpansionService,
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService,
    @Inject(IRenderManagerService)
    private readonly renderManagerService: IRenderManagerService
  ) {}

  async fillByConfig(
    univerApi: any,
    config: PivotTableConfig,
    options: PivotTableFillOptions = {}
  ): Promise<boolean> {
    const workbook = univerApi.getActiveWorkbook?.()
    if (!workbook) {
      throw new Error('未找到当前工作簿')
    }

    const configuredSheetId = config.placement.sheetId
    const worksheet = workbook
      .getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === configuredSheetId)
    if (!worksheet) {
      throw new Error(`未找到透视表目标工作表: ${configuredSheetId || '未配置 Sheet ID'}`)
    }
    const unitId = workbook.getId?.() || workbook.getUnitId?.()
    if (!unitId) {
      return this.fillByConfigSerial(univerApi, config, worksheet, options)
    }

    // 首次恢复可能在等待过滤器或排队期间切换 Sheet，队列内必须继续使用配置绑定的工作表。
    return this.tableRenderExpansionService.runExclusive(unitId, configuredSheetId, () =>
      this.fillByConfigSerial(univerApi, config, worksheet, options)
    )
  }

  private async fillByConfigSerial(
    univerApi: any,
    config: PivotTableConfig,
    worksheet: any,
    options: PivotTableFillOptions = {}
  ): Promise<boolean> {
    const workbook = univerApi.getActiveWorkbook?.()
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
      }
      if (unitId) {
        this.pluginRenderStatusService.set({
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
      this.pluginRenderStatusService.set({
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
          this.pluginRenderStatusService.set({
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
      await this.editProtectionService.runWithoutProtection(() =>
        setWorksheetValuesSilently(univerApi, {
          unitId,
          sheetId,
          range: {
            startRow: start.row,
            endRow: start.row + layout.rowCount - 1,
            startColumn: start.col,
            endColumn: start.col + layout.columnCount - 1
          },
          values: layout.values
        })
      )
      const effectiveMerges = this.getEffectiveMerges(
        config,
        layout.merges,
        layout.corner?.range
      )
      if (effectiveMerges.length) {
        await this.applyMerges(univerApi, worksheet, start, effectiveMerges)
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
        axisHeaderValues: layout.axisHeaderValues,
        corner: layout.corner,
        updatedAt: Date.now()
      })
      this.updateRenderStyleRange(univerApi, config)
      this.refreshTargetSheet(univerApi, worksheet.getSheetId())
      if (unitId) {
        this.pluginRenderStatusService.set({
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
      try {
        await this.clearPrevious(univerApi, config.id, worksheet, config.placement.startCell, true)
      } catch (clearError) {
      }
      if (unitId) {
        this.pluginRenderStatusService.set({
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

    const valueRegionsToRestore: PivotTableValueRegion[] = []
    if (!config.style?.base?.mergeCell) {
      valueRegionsToRestore.push(...(state.axisHeaderValues || []))
    }
    if (!config.style?.base?.slashHeader && state.corner) {
      valueRegionsToRestore.push({
        range: state.corner.range,
        values: state.corner.values
      })
    }
    if (valueRegionsToRestore.length) {
      await this.restoreValueRegions(
        univerApi,
        unitId,
        state.sheetId,
        start,
        valueRegionsToRestore
      )
    }

    const effectiveMerges = this.getEffectiveMerges(
      config,
      state.merges || [],
      state.corner?.range
    )
    if (effectiveMerges.length) {
      await this.applyMerges(univerApi, worksheet, start, effectiveMerges)
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
          clearWorksheetFormatsSilently(univerApi, {
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
      }
    }

    const emptyValues = Array.from({ length: previous.rowCount }, () =>
      Array.from({ length: previous.columnCount }, () => '')
    )
    if (unitId) {
      await this.editProtectionService.runWithoutProtection(() =>
        setWorksheetValuesSilently(univerApi, {
          unitId,
          sheetId: previous.sheetId,
          range: {
            startRow: start.row,
            endRow: start.row + previous.rowCount - 1,
            startColumn: start.col,
            endColumn: start.col + previous.columnCount - 1
          },
          values: emptyValues
        })
      )
    }

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

  private getEffectiveMerges(
    config: PivotTableConfig,
    headerMerges: PivotLayoutRange[],
    cornerRange?: PivotLayoutRange
  ): PivotLayoutRange[] {
    const merges = config.style?.base?.mergeCell ? [...headerMerges] : []
    if (
      config.style?.base?.slashHeader &&
      cornerRange &&
      this.isMultiCellRange(cornerRange)
    ) {
      merges.push(cornerRange)
    }
    return merges
  }

  private async restoreValueRegions(
    univerApi: any,
    unitId: string,
    sheetId: string,
    start: { row: number; col: number },
    regions: PivotTableValueRegion[]
  ): Promise<void> {
    // 合并 Mutation 会清空非主单元格，关闭对应合并能力时必须恢复布局阶段的原始表头值。
    await this.editProtectionService.runWithoutProtection(async () => {
      for (const region of regions) {
        await setWorksheetValuesSilently(univerApi, {
          unitId,
          sheetId,
          range: {
            startRow: start.row + region.range.startRow,
            endRow: start.row + region.range.endRow,
            startColumn: start.col + region.range.startColumn,
            endColumn: start.col + region.range.endColumn
          },
          values: region.values
        })
      }
    })
  }

  private isMultiCellRange(range: PivotLayoutRange): boolean {
    return range.startRow !== range.endRow || range.startColumn !== range.endColumn
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
      corner: state.corner,
      config
    })
  }

  private refreshTargetSheet(univerApi: any, sheetId: string): void {
    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const currentRender = unitId
      ? this.renderManagerService.getRenderById(unitId) as any
      : undefined
    currentRender?.mainComponent?.makeDirty?.(true)
    currentRender?.scene?.makeDirty?.(true)

    const worksheet = workbook
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

    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const sheetId = worksheet?.getSheetId?.()
    if (!unitId || !sheetId) {
      throw new Error('工作表空间不足，且无法自动扩容')
    }

    await this.editProtectionService.runWithoutProtection(() =>
      setWorksheetRowCountSilently(univerApi, {
        unitId,
        sheetId,
        count: requiredRows
      })
    )
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

    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const sheetId = worksheet?.getSheetId?.()
    if (!unitId || !sheetId) {
      throw new Error('工作表空间不足，且无法自动扩容')
    }

    await this.editProtectionService.runWithoutProtection(() =>
      setWorksheetColumnCountSilently(univerApi, {
        unitId,
        sheetId,
        count: requiredColumns
      })
    )
  }
}
