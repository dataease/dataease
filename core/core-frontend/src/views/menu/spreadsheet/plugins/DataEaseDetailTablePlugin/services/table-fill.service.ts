import { HorizontalAlign, Inject, VerticalAlign } from '@univerjs/core'
import {
  ClearSelectionFormatCommand,
  SetStyleCommand
} from '@univerjs/sheets'
import type { DetailTableConfig } from '../types'
import type { CellPosition } from '../types'
import { TableDataService } from './table-data.service'
import { DetailTableDisplayStateService } from './detail-table-display-state.service'
import type { DetailTableDisplayState } from './detail-table-display-state.service'
import { DetailTableRangeService } from './detail-table-range.service'
import { ElMessage } from 'element-plus-secondary'
import { DetailTableRenderStyleService } from './detail-table-render-style.service'
import { DetailTableEditProtectionService } from './detail-table-edit-protection.service'
import { SpreadsheetFilterRuntimeService } from '../../DataEaseFilterPlugin/services/filter-runtime.service'
import {
  findConfiguredField,
  getFieldNumberFormat,
  toNativeCellValue
} from '../utils/field-format'
import { ensureSheetSize } from '../utils/sheet-size'
import { PluginRenderLoadingService } from '../../../services/plugin-render-loading.service'
import {
  addWorksheetMergesSilently,
  removeWorksheetMergesSilently
} from '../../../utils/silent-worksheet-merge'

export interface TableFillState {
  startCell: string
  sheetId: string
  rowCount: number
  colCount: number
  lastFillTime: number
}

interface TableFillOptions {
  initialRestore?: boolean
}

export class TableFillService {
  private readonly dataService = new TableDataService()

  constructor(
    @Inject(DetailTableDisplayStateService)
    private readonly displayStateService: DetailTableDisplayStateService,
    @Inject(DetailTableRangeService)
    private readonly detailTableRangeService: DetailTableRangeService,
    @Inject(DetailTableRenderStyleService)
    private readonly detailTableRenderStyleService: DetailTableRenderStyleService,
    @Inject(DetailTableEditProtectionService)
    private readonly detailTableEditProtectionService: DetailTableEditProtectionService,
    @Inject(SpreadsheetFilterRuntimeService)
    private readonly spreadsheetFilterRuntimeService: SpreadsheetFilterRuntimeService,
    @Inject(PluginRenderLoadingService)
    private readonly pluginRenderLoadingService: PluginRenderLoadingService
  ) {}

  async fillTableByConfig(
    univerApi: any,
    config: DetailTableConfig,
    options: TableFillOptions = {}
  ): Promise<void> {
    const workbook = univerApi.getActiveWorkbook()
    if (!workbook) {
      throw new Error('No active workbook found')
    }

    const targetSheet = workbook
      .getSheets()
      .find(sheet => sheet.getSheetId() === config.placement.sheetId)

    if (!targetSheet) {
      return
    }

    await this.fillTable(univerApi, config, config.placement.startCell, targetSheet, options)
  }

  async fillTable(
    univerApi: any,
    config: DetailTableConfig,
    startCell: string,
    targetWorksheet?: any,
    options: TableFillOptions = {}
  ): Promise<void> {
    console.log('[TableFillService] Starting fillTable:', { startCell, config })

    const startPos = this.parseCell(startCell)
    console.log('[TableFillService] Start position:', startPos)

    const fWorkbook = univerApi.getActiveWorkbook()
    const fWorksheet = targetWorksheet || fWorkbook.getActiveSheet()

    if (!fWorksheet) {
      throw new Error('No active worksheet found')
    }

    const unitId = fWorkbook?.getId?.() || fWorkbook?.getUnitId?.()
    const sheetId = fWorksheet.getSheetId()
    const previousState = this.displayStateService.get(config.id)
    const previousRange = previousState?.sheetId === sheetId &&
      previousState.rowCount > 0 && previousState.colCount > 0
      ? previousState
      : undefined
    const loadingStart = previousRange
      ? this.parseCell(previousRange.startCell)
      : startPos
    const loading = unitId
      ? this.pluginRenderLoadingService.begin({
          unitId,
          sheetId,
          pluginId: config.id,
          startRow: loadingStart.row,
          startColumn: loadingStart.col,
          rowCount: previousRange?.rowCount ?? 1,
          columnCount: previousRange?.colCount ?? 1
        })
      : undefined

    try {
      if (unitId) {
        await this.spreadsheetFilterRuntimeService.waitForValues(unitId)
      }
      const queryConfig = unitId
        ? this.spreadsheetFilterRuntimeService.applyQueryFilterToConfig(unitId, config)
        : config
      const dataResult = await this.dataService.queryData(queryConfig)

      const fieldCount = dataResult.data.fields.length
      const showIndex = !!config.style?.header?.showIndex
      const indexLabel = config.style?.header?.indexLabel?.trim() || '序号'
      const hideHeader = !!config.style?.base?.hideHeader
      const headerRowCount = hideHeader ? 0 : 1
      const totalEnabled = !!config.style?.total?.enable
      const totalDataSignature = this.getTotalDataSignature(config)
      const columnCount = fieldCount + (showIndex ? 1 : 0)
      const dataRowCount = dataResult.data.rowData.length
      const rowCount = headerRowCount + dataRowCount + (totalEnabled ? 1 : 0)
      await this.detailTableEditProtectionService.runWithoutProtection(() =>
        ensureSheetSize(univerApi, fWorksheet, startPos.row + rowCount, startPos.col + columnCount)
      )
      const conflictMessage = this.detailTableRangeService.validateRenderRangeBeforeFill(
        univerApi,
        config,
        startCell,
        rowCount,
        columnCount,
        fWorksheet,
        options.initialRestore === true
      )
      if (conflictMessage) {
        ElMessage.warning(conflictMessage)
        return
      }

      await this.clearPreviousData(univerApi, config.id, startCell, fWorksheet)

      const configuredFields = config.data?.zones?.fields || []
      const resolvedFields = dataResult.data.fields.map(field =>
        findConfiguredField(configuredFields, field)
      )
      const dataValues = dataResult.data.rowData.map((row, rowIndex) => {
        const rowValues = dataResult.data.fields.map((column, index) => {
          const value = toNativeCellValue(row[column.dataeaseName], resolvedFields[index])
          const numberFormat = getFieldNumberFormat(resolvedFields[index], value)
          if (!numberFormat || value === '') {
            return value
          }
          return {
            v: value,
            s: {
              n: {
                pattern: numberFormat
              }
            }
          }
        })
        return showIndex
          ? [
              {
                v: rowIndex + 1,
                s: {
                  n: {
                    pattern: '0'
                  }
                }
              },
              ...rowValues
            ]
          : rowValues
      })
      const headerValues = dataResult.data.fields.map(column => column.chartShowName || column.name)
      const totalValues = totalEnabled
        ? this.buildTotalRow(
            dataValues,
            dataResult.data.fields,
            config,
            showIndex,
            columnCount,
            resolvedFields
          )
        : undefined
      const values = [
        ...(hideHeader ? [] : [showIndex ? [indexLabel, ...headerValues] : headerValues]),
        ...dataValues,
        ...(totalValues ? [totalValues] : [])
      ]

      if (values.length > 0 && columnCount > 0) {
        console.log('[TableFillService] Filling data in batch...', {
          rowCount: values.length,
          colCount: columnCount
        })

        this.detailTableEditProtectionService.runWithoutProtection(() => {
          fWorksheet
            .getRange(startPos.row, startPos.col, values.length, columnCount)
            .setValues(values)
        })
      }

      this.displayStateService.set({
        pluginId: config.id,
        startCell,
        sheetId,
        rowCount,
        colCount: columnCount,
        dataValues,
        fields: resolvedFields,
        showIndex,
        indexLabel,
        totalEnabled,
        totalDataSignature,
        hideHeader,
        updatedAt: Date.now()
      })
      console.log('[TableFillService] Fill state updated for plugin:', config.id)

      this.updateRenderStyleRange(univerApi, config)
      await this.applyStyleOnly(univerApi, config)

      console.log('[TableFillService] Complete')
    } finally {
      loading?.dispose()
    }
  }

  async applyStyleOnly(univerApi: any, config: DetailTableConfig): Promise<void> {
    const state = this.displayStateService.get(config.id)
    const cellStyle = config.style?.cell
    if (!state || state.rowCount <= 0 || state.colCount <= 0) {
      return
    }

    const showIndex = !!config.style?.header?.showIndex
    const indexLabel = config.style?.header?.indexLabel?.trim() || '序号'
    const hideHeader = !!config.style?.base?.hideHeader
    if (!!state.hideHeader !== hideHeader) {
      await this.fillTableByConfig(univerApi, config)
      return
    }
    if (state.totalDataSignature !== this.getTotalDataSignature(config)) {
      await this.fillTableByConfig(univerApi, config)
      return
    }
    if (!!state.showIndex !== showIndex) {
      await this.fillTableByConfig(univerApi, config)
      return
    }

    const workbook = univerApi.getActiveWorkbook()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (!unitId) {
      return
    }

    const startPos = this.parseCell(state.startCell)
    if (showIndex && state.indexLabel !== indexLabel) {
      const worksheet = workbook
        .getSheets?.()
        ?.find(sheet => sheet.getSheetId() === state.sheetId)
      if (!hideHeader) {
        this.detailTableEditProtectionService.runWithoutProtection(() => {
          worksheet?.getRange(startPos.row, startPos.col, 1, 1).setValues([[indexLabel]])
        })
      }
      this.displayStateService.set({
        ...state,
        indexLabel,
        updatedAt: Date.now()
      })
      this.updateRenderStyleRange(univerApi, config)
      this.refreshTargetSheet(univerApi, state.sheetId)
      return
    }
    const headerRowCount = state.hideHeader ? 0 : 1
    const dataRowCount = state.dataValues?.length ?? Math.max(
      state.rowCount - headerRowCount - (state.totalEnabled ? 1 : 0),
      0
    )
    const dataRange = dataRowCount > 0
      ? {
          startRow: startPos.row + headerRowCount,
          endRow: startPos.row + headerRowCount + dataRowCount - 1,
          startColumn: startPos.col,
          endColumn: startPos.col + state.colCount - 1
        }
      : undefined

    if (dataRange) {
      await this.removeMerges(univerApi, unitId, state.sheetId, dataRange)
      await this.restoreDataValues(univerApi, state)
    }
    this.updateRenderStyleRange(univerApi, config)

    if (dataRange && config.style?.base?.mergeCell) {
      await this.mergeDimensionCells(univerApi, config, state, dataRange, unitId)
    }

    this.refreshTargetSheet(univerApi, state.sheetId)
  }

  private async mergeDimensionCells(
    univerApi: any,
    config: DetailTableConfig,
    state: DetailTableDisplayState,
    dataRange: { startRow: number; endRow: number; startColumn: number; endColumn: number },
    unitId: string
  ): Promise<void> {
    const fields = config.data?.zones?.fields || []
    const indexColumnOffset = state.showIndex ? 1 : 0
    const mergeColumnCount = this.getMergeColumnCount(fields, state.colCount - indexColumnOffset)
    if (mergeColumnCount <= 0) {
      return
    }

    const renderedDataValues = state.dataValues || this.readDataValues(univerApi, state, dataRange)
    const dataValues = indexColumnOffset
      ? renderedDataValues.map(row => row.slice(indexColumnOffset))
      : renderedDataValues
    if (!dataValues.length) {
      return
    }

    const mergeRanges = this.buildMergeRanges(
      dataValues,
      dataRange.startRow,
      dataRange.startColumn + indexColumnOffset,
      mergeColumnCount
    )
    if (!mergeRanges.length) {
      return
    }

    await this.detailTableEditProtectionService.runWithoutProtection(() => {
      return addWorksheetMergesSilently(univerApi, {
        unitId,
        sheetId: state.sheetId,
        ranges: mergeRanges
      })
    })

    await this.applyMergeAlignment(univerApi, unitId, state.sheetId, mergeRanges)
  }

  private getMergeColumnCount(fields: Array<{ groupType?: string }>, colCount: number): number {
    if (!fields.length || fields[0]?.groupType === 'q') {
      return 0
    }

    const firstMetricIndex = fields.findIndex(field => field.groupType === 'q')
    const count = firstMetricIndex >= 0 ? firstMetricIndex : fields.length
    return Math.min(count, colCount)
  }

  private buildTotalRow(
    dataValues: any[][],
    fields: Array<{
      id: string | number
      name: string
      dataeaseName: string
      groupType: 'd' | 'q'
    }>,
    config: DetailTableConfig,
    showIndex: boolean,
    columnCount: number,
    resolvedFields: Array<ReturnType<typeof findConfiguredField>>
  ): any[] {
    const totalRow = Array.from({ length: columnCount }, () => '')
    const columnOffset = showIndex ? 1 : 0
    const firstDimensionIndex = fields.findIndex(field => field.groupType === 'd')
    const labelColumnIndex = showIndex
      ? 0
      : Math.max(firstDimensionIndex, 0)
    totalRow[labelColumnIndex] = config.style?.total?.label?.trim() || '总计'

    fields.forEach((field, fieldIndex) => {
      if (field.groupType !== 'q') {
        return
      }
      const columnIndex = fieldIndex + columnOffset
      if (columnIndex === labelColumnIndex) {
        return
      }

      const fieldConfig = config.style?.total?.fieldConfig?.find(item =>
        item.dataeaseName === field.dataeaseName || String(item.fieldId) === String(field.id)
      )
      const aggregation = fieldConfig?.aggregation || 'SUM'
      if (aggregation === 'CUSTOM') {
        return
      }

      const numericValues = dataValues
        .map(row => this.getNumericCellValue(row[columnIndex]))
        .filter((value): value is number => value !== undefined)
      if (!numericValues.length) {
        return
      }

      let value: number
      switch (aggregation) {
        case 'MAX':
          value = numericValues.reduce((result, item) => Math.max(result, item))
          break
        case 'MIN':
          value = numericValues.reduce((result, item) => Math.min(result, item))
          break
        case 'AVG':
          value = numericValues.reduce((sum, item) => sum + item, 0) / numericValues.length
          break
        default:
          value = numericValues.reduce((sum, item) => sum + item, 0)
      }

      const numberFormat = getFieldNumberFormat(resolvedFields[fieldIndex], value)
      totalRow[columnIndex] = numberFormat
        ? { v: value, s: { n: { pattern: numberFormat } } }
        : value
    })

    return totalRow
  }

  private getNumericCellValue(cell: any): number | undefined {
    const value = cell && typeof cell === 'object' && 'v' in cell ? cell.v : cell
    if (value === '' || value === null || value === undefined) {
      return undefined
    }
    const numberValue = typeof value === 'number' ? value : Number(value)
    return Number.isFinite(numberValue) ? numberValue : undefined
  }

  private getTotalDataSignature(config: DetailTableConfig): string {
    const total = config.style?.total
    return JSON.stringify({
      enable: !!total?.enable,
      label: total?.label?.trim() || '总计',
      fieldConfig: total?.fieldConfig || []
    })
  }

  private buildMergeRanges(
    dataValues: any[][],
    startRow: number,
    startColumn: number,
    mergeColumnCount: number
  ) {
    let groups = [{ start: 0, end: dataValues.length - 1 }]
    const mergeRanges: Array<{
      startRow: number
      endRow: number
      startColumn: number
      endColumn: number
    }> = []

    for (let col = 0; col < mergeColumnCount; col++) {
      const nextGroups: typeof groups = []

      for (const group of groups) {
        let runStart = group.start
        for (let row = group.start + 1; row <= group.end + 1; row++) {
          const currentValue = row <= group.end ? this.normalizeMergeValue(dataValues[row]?.[col]) : undefined
          const previousValue = this.normalizeMergeValue(dataValues[row - 1]?.[col])

          if (row <= group.end && currentValue === previousValue) {
            continue
          }

          nextGroups.push({ start: runStart, end: row - 1 })
          if (row - runStart > 1) {
            mergeRanges.push({
              startRow: startRow + runStart,
              endRow: startRow + row - 1,
              startColumn: startColumn + col,
              endColumn: startColumn + col
            })
          }
          runStart = row
        }
      }

      groups = nextGroups
    }

    return mergeRanges
  }

  private normalizeMergeValue(value: any): string {
    const cellValue =
      value && typeof value === 'object' && 'v' in value
        ? value.v
        : value
    return cellValue == null ? '' : String(cellValue)
  }

  private readDataValues(
    univerApi: any,
    state: DetailTableDisplayState,
    dataRange: { startRow: number; startColumn: number }
  ): any[][] {
    const workbook = univerApi.getActiveWorkbook()
    const worksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId() === state.sheetId)
    const headerRowCount = state.hideHeader ? 0 : 1
    const dataRowCount = state.rowCount - headerRowCount - (state.totalEnabled ? 1 : 0)
    if (!worksheet || dataRowCount <= 0) {
      return []
    }

    return worksheet
      .getRange(dataRange.startRow, dataRange.startColumn, dataRowCount, state.colCount)
      .getValues()
  }

  private async restoreDataValues(
    univerApi: any,
    state: DetailTableDisplayState
  ): Promise<void> {
    if (!state.dataValues?.length) {
      return
    }
    const dataValues = state.dataValues

    const workbook = univerApi.getActiveWorkbook()
    const worksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId() === state.sheetId)
    if (!worksheet) {
      return
    }

    const startPos = this.parseCell(state.startCell)
    await this.detailTableEditProtectionService.runWithoutProtection(() =>
      ensureSheetSize(
        univerApi,
        worksheet,
        startPos.row + (state.hideHeader ? 0 : 1) + dataValues.length,
        startPos.col + state.colCount
      )
    )
    this.detailTableEditProtectionService.runWithoutProtection(() => {
      worksheet
        .getRange(
          startPos.row + (state.hideHeader ? 0 : 1),
          startPos.col,
          dataValues.length,
          state.colCount
        )
        .setValues(dataValues)
    })
  }

  private async removeMerges(
    univerApi: any,
    unitId: string,
    sheetId: string,
    range: { startRow: number; endRow: number; startColumn: number; endColumn: number }
  ): Promise<void> {
    await this.detailTableEditProtectionService.runWithoutProtection(() => {
      return removeWorksheetMergesSilently(univerApi, {
        unitId,
        sheetId,
        ranges: [range]
      })
    })
  }

  private async applyMergeAlignment(
    univerApi: any,
    unitId: string,
    sheetId: string,
    ranges: Array<{ startRow: number; endRow: number; startColumn: number; endColumn: number }>
  ): Promise<void> {
    await this.detailTableEditProtectionService.runWithoutProtection(async () => {
      for (const range of ranges) {
        await univerApi.executeCommand?.(SetStyleCommand.id, {
          unitId,
          subUnitId: sheetId,
          range,
          style: { type: 'ht', value: HorizontalAlign.CENTER }
        })
        await univerApi.executeCommand?.(SetStyleCommand.id, {
          unitId,
          subUnitId: sheetId,
          range,
          style: { type: 'vt', value: VerticalAlign.MIDDLE }
        })
      }
    })
  }

  private async clearPreviousData(
    univerApi: any,
    pluginId: string,
    targetStartCell: string,
    targetWorksheet?: any
  ): Promise<void> {
    const state = this.displayStateService.get(pluginId)
    if (!state) return

    const workbook = univerApi.getActiveWorkbook?.()
    const targetSheetId = targetWorksheet?.getSheetId?.()
    const previousWorksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === state.sheetId)
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (!previousWorksheet) {
      if (unitId) {
        this.detailTableRenderStyleService.deleteRange(unitId, state.sheetId, pluginId)
      }
      return
    }

    console.log('[TableFillService] Clearing previous data for plugin:', pluginId, state)
    await this.clearTableValues(univerApi, state, previousWorksheet)

    const moved = targetSheetId !== state.sheetId || targetStartCell !== state.startCell
    if (moved && unitId) {
      await this.clearRangeFormats(
        univerApi,
        unitId,
        state.sheetId,
        state.startCell,
        state.rowCount,
        state.colCount
      )
      this.refreshTargetSheet(univerApi, state.sheetId)
    }
  }

  private updateRenderStyleRange(univerApi: any, config: DetailTableConfig): void {
    const state = this.displayStateService.get(config.id)
    const workbook = univerApi.getActiveWorkbook()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (!state || !unitId) {
      return
    }

    const startPos = this.parseCell(state.startCell)
    this.detailTableRenderStyleService.setRange({
      pluginId: config.id,
      unitId,
      sheetId: state.sheetId,
      startRow: startPos.row,
      startColumn: startPos.col,
      rowCount: state.rowCount,
      colCount: state.colCount,
      fields: state.fields,
      config
    })
  }

  private refreshTargetSheet(univerApi: any, sheetId: string): void {
    const worksheet = univerApi
      .getActiveWorkbook()
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId() === sheetId)

    worksheet?.refreshCanvas?.()
  }

  private async clearTableValues(
    univerApi: any,
    state: DetailTableDisplayState,
    targetWorksheet?: any
  ): Promise<void> {
    const startPos = this.parseCell(state.startCell)
    const fWorkbook = univerApi.getActiveWorkbook()
    const fWorksheet = targetWorksheet || fWorkbook.getActiveSheet()
    const unitId = fWorkbook?.getId?.() || fWorkbook?.getUnitId?.()
    const subUnitId = fWorksheet?.getSheetId?.()

    if (state.rowCount <= 0 || state.colCount <= 0) {
      return
    }

    await this.detailTableEditProtectionService.runWithoutProtection(() =>
      ensureSheetSize(
        univerApi,
        fWorksheet,
        startPos.row + state.rowCount,
        startPos.col + state.colCount
      )
    )

    if (unitId && subUnitId) {
      await this.removeMerges(univerApi, unitId, subUnitId, {
        startRow: startPos.row,
        endRow: startPos.row + state.rowCount - 1,
        startColumn: startPos.col,
        endColumn: startPos.col + state.colCount - 1
      })

      this.detailTableRenderStyleService.deleteRange(unitId, subUnitId, state.pluginId)
    }

    const emptyValues = Array.from({ length: state.rowCount }, () =>
      Array.from({ length: state.colCount }, () => '')
    )

    this.detailTableEditProtectionService.runWithoutProtection(() => {
      const range = fWorksheet.getRange(
        startPos.row,
        startPos.col,
        state.rowCount,
        state.colCount
      )
      range.setValues(emptyValues)
    })
  }

  async clearTableData(
    univerApi: any,
    startCell: string,
    rowCount: number,
    colCount: number,
    targetWorksheet?: any
  ): Promise<void> {
    const startPos = this.parseCell(startCell)
    const fWorkbook = univerApi.getActiveWorkbook()
    const fWorksheet = targetWorksheet || fWorkbook.getActiveSheet()
    const unitId = fWorkbook?.getId?.() || fWorkbook?.getUnitId?.()
    const subUnitId = fWorksheet?.getSheetId?.()
    const state = subUnitId ? this.displayStateService.findByLocation(subUnitId, startCell) : undefined

    if (rowCount <= 0 || colCount <= 0) {
      return
    }

    await this.detailTableEditProtectionService.runWithoutProtection(() =>
      ensureSheetSize(univerApi, fWorksheet, startPos.row + rowCount, startPos.col + colCount)
    )

    if (unitId && subUnitId) {
      await this.removeMerges(univerApi, unitId, subUnitId, {
        startRow: startPos.row,
        endRow: startPos.row + rowCount - 1,
        startColumn: startPos.col,
        endColumn: startPos.col + colCount - 1
      })

      await this.clearRangeFormats(
        univerApi,
        unitId,
        subUnitId,
        startCell,
        rowCount,
        colCount
      )

      if (state) {
        this.detailTableRenderStyleService.deleteRange(unitId, subUnitId, state.pluginId)
      }
    }

    const emptyValues = Array.from({ length: rowCount }, () =>
      Array.from({ length: colCount }, () => '')
    )

    this.detailTableEditProtectionService.runWithoutProtection(() => {
      fWorksheet
        .getRange(startPos.row, startPos.col, rowCount, colCount)
        .setValues(emptyValues)
    })

    if (state) {
      this.displayStateService.delete(state.pluginId)
    }
  }

  private async clearRangeFormats(
    univerApi: any,
    unitId: string,
    sheetId: string,
    startCell: string,
    rowCount: number,
    colCount: number
  ): Promise<void> {
    if (rowCount <= 0 || colCount <= 0) {
      return
    }

    const start = this.parseCell(startCell)
    await this.detailTableEditProtectionService.runWithoutProtection(() =>
      univerApi.executeCommand?.(ClearSelectionFormatCommand.id, {
        unitId,
        subUnitId: sheetId,
        ranges: [{
          startRow: start.row,
          endRow: start.row + rowCount - 1,
          startColumn: start.col,
          endColumn: start.col + colCount - 1
        }]
      })
    )
  }

  getFillState(univerApi: any, startCell: string): TableFillState | undefined {
    const fWorkbook = univerApi.getActiveWorkbook()
    const fWorksheet = fWorkbook.getActiveSheet()
    const sheetId = fWorksheet.getSheetId()
    const state = this.displayStateService.findByLocation(sheetId, startCell)

    if (!state) {
      return undefined
    }

    return {
      startCell: state.startCell,
      sheetId: state.sheetId,
      rowCount: state.rowCount,
      colCount: state.colCount,
      lastFillTime: state.updatedAt
    }
  }

  clearFillState(univerApi: any, startCell: string): void {
    const fWorkbook = univerApi.getActiveWorkbook()
    const fWorksheet = fWorkbook.getActiveSheet()
    const sheetId = fWorksheet.getSheetId()
    const state = this.displayStateService.findByLocation(sheetId, startCell)
    if (state) {
      const unitId = fWorkbook?.getId?.() || fWorkbook?.getUnitId?.()
      if (unitId) {
        this.detailTableRenderStyleService.deleteRange(unitId, sheetId, state.pluginId)
      }
      this.displayStateService.delete(state.pluginId)
    }
  }

  clearAllStates(): void {
    this.displayStateService.clear()
    this.detailTableRenderStyleService.clear()
  }

  private parseCell(cellAddress: string): CellPosition {
    const match = cellAddress.match(/^([A-Z]+)(\d+)$/i)
    if (!match) {
      throw new Error(`Invalid cell address: ${cellAddress}`)
    }

    const colStr = match[1].toUpperCase()
    const rowStr = match[2]

    let col = 0
    for (let i = 0; i < colStr.length; i++) {
      col = col * 26 + (colStr.charCodeAt(i) - 65)
    }

    const row = parseInt(rowStr, 10) - 1

    return { row, col }
  }
}
