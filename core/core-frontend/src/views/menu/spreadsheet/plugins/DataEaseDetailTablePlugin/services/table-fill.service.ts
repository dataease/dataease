import { HorizontalAlign, Inject, VerticalAlign } from '@univerjs/core'
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
import { validateDetailConfig } from '../utils/detail-config-validator'
import { ensureSheetSize } from '../utils/sheet-size'
import { isDetailTableIndexVisible } from '../utils/table-style-state'
import {
  PluginRenderLoadingService,
  PluginRenderStatusService
} from '../../DataEaseRuntimePlugin/services/table'
import {
  addWorksheetMergesSilently,
  removeWorksheetMergesSilently
} from '../../../utils/silent-worksheet-merge'
import {
  clearWorksheetFormatsSilently,
  setWorksheetStylesSilently,
  setWorksheetValuesSilently
} from '../../../utils/silent-worksheet-write'
import { TableRenderExpansionService } from '../../../services/table-render-expansion.service'

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
    private readonly pluginRenderLoadingService: PluginRenderLoadingService,
    @Inject(TableRenderExpansionService)
    private readonly tableRenderExpansionService: TableRenderExpansionService,
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService
  ) {}

  async fillTable(
    univerApi: any,
    config: DetailTableConfig,
    options: TableFillOptions = {}
  ): Promise<boolean> {
    const workbook = univerApi.getActiveWorkbook?.()
    if (!workbook) {
      throw new Error('未找到当前工作簿')
    }

    const sheetId = config.placement.sheetId
    const worksheet = workbook
      .getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === sheetId)
    if (!worksheet) {
      throw new Error(`未找到明细表目标工作表: ${sheetId || '未配置 Sheet ID'}`)
    }
    const startCell = config.placement.startCell
    const unitId = workbook.getId?.() || workbook.getUnitId?.()
    if (!unitId) {
      return this.fillTableSerial(univerApi, config, startCell, worksheet, options)
    }

    return this.tableRenderExpansionService.runExclusive(unitId, sheetId, () =>
      this.fillTableSerial(univerApi, config, startCell, worksheet, options)
    )
  }

  private async fillTableSerial(
    univerApi: any,
    config: DetailTableConfig,
    startCell: string,
    targetWorksheet: any,
    options: TableFillOptions = {}
  ): Promise<boolean> {

    const fWorkbook = univerApi.getActiveWorkbook()
    const fWorksheet = targetWorksheet

    const unitId = fWorkbook?.getId?.() || fWorkbook?.getUnitId?.()
    const sheetId = fWorksheet.getSheetId()
    if (unitId) {
      startCell = this.tableRenderExpansionService.resolveStartCell(
        unitId,
        'detail',
        config.id,
        startCell
      )
      config.placement.startCell = startCell
    }
    let startPos = this.parseCell(startCell)
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

    if (unitId) {
      this.pluginRenderStatusService.set({
        pluginId: config.id,
        type: 'detail',
        status: 'loading',
        unitId,
        sheetId,
        startCell,
        updatedAt: Date.now()
      })
    }

    const validateMessage = validateDetailConfig(config)
    if (validateMessage) {
      ElMessage.warning(validateMessage)
      try {
        await this.clearPreviousData(univerApi, config.id, startCell, fWorksheet)
      } catch (clearError) {
      }
      if (unitId) {
        this.pluginRenderStatusService.set({
          pluginId: config.id,
          type: 'detail',
          status: 'error',
          reason: validateMessage,
          unitId,
          sheetId,
          startCell,
          updatedAt: Date.now()
        })
      }
      loading?.dispose()
      return false
    }

    try {
      if (unitId) {
        await this.spreadsheetFilterRuntimeService.waitForValues(unitId)
      }
      const queryConfig = unitId
        ? this.spreadsheetFilterRuntimeService.applyQueryFilterToConfig(unitId, config)
        : config
      const dataResult = await this.dataService.queryData(queryConfig)
      if (unitId) {
        // 查询期间可能发生合法的行列删除，写入前必须重新读取实例的最新坐标。
        startCell = this.tableRenderExpansionService.resolveStartCell(
          unitId,
          'detail',
          config.id,
          startCell
        )
        config.placement.startCell = startCell
        startPos = this.parseCell(startCell)
      }

      const configuredFields = config.data?.zones?.fields || []
      // 查询仍保留完整字段，仅在写入工作表时投影出可见字段。
      const renderFields = dataResult.data.fields
        .map(field => ({
          field,
          configuredField: findConfiguredField(configuredFields, field)
        }))
        .filter(item => item.configuredField?.hidden !== true)
      const resultFields = renderFields.map(item => item.field)
      const resolvedFields = renderFields.map(item => item.configuredField)
      const fieldCount = resultFields.length
      const showIndex = isDetailTableIndexVisible(config)
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
      const expansionMessage = unitId
        ? await this.tableRenderExpansionService.ensureRenderSpace({
            unitId,
            sheetId,
            pluginId: config.id,
            tableType: 'detail',
            worksheet: fWorksheet,
            startCell,
            rowCount,
            columnCount,
            initialRestore: options.initialRestore === true
          })
        : undefined
      const conflictMessage = expansionMessage ||
        this.detailTableRangeService.validateRenderRangeBeforeFill(
          config,
          startCell,
          rowCount,
          columnCount,
          fWorksheet,
          options.initialRestore === true
        )
      if (conflictMessage) {
        ElMessage.warning(conflictMessage)
        if (unitId) {
          this.pluginRenderStatusService.set({
            pluginId: config.id,
            type: 'detail',
            status: 'error',
            reason: conflictMessage,
            unitId,
            sheetId,
            startCell,
            updatedAt: Date.now()
          })
        }
        return false
      }

      await this.clearPreviousData(univerApi, config.id, startCell, fWorksheet)

      const dataValues = dataResult.data.rowData.map((row, rowIndex) => {
        const rowValues = resultFields.map((column, index) => {
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
      const headerValues = resultFields.map(column => column.chartShowName || column.name)
      const totalValues = totalEnabled
        ? this.buildTotalRow(
            dataValues,
            resultFields,
            config,
            showIndex,
            columnCount,
            resolvedFields,
            dataResult.data.customTotalResult
          )
        : undefined
      const values = [
        ...(hideHeader ? [] : [showIndex ? [indexLabel, ...headerValues] : headerValues]),
        ...dataValues,
        ...(totalValues ? [totalValues] : [])
      ]

      if (values.length > 0 && columnCount > 0) {

        await this.detailTableEditProtectionService.runWithoutProtection(() =>
          setWorksheetValuesSilently(univerApi, {
            unitId,
            sheetId,
            range: {
              startRow: startPos.row,
              endRow: startPos.row + values.length - 1,
              startColumn: startPos.col,
              endColumn: startPos.col + columnCount - 1
            },
            values
          })
        )
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

      this.updateRenderStyleRange(univerApi, config)
      await this.applyStyleOnly(univerApi, config)

      if (unitId) {
        this.pluginRenderStatusService.set({
          pluginId: config.id,
          type: 'detail',
          status: rowCount > 0 ? 'rendered' : 'empty',
          unitId,
          sheetId,
          startCell,
          updatedAt: Date.now()
        })
      }

      return true
    } catch (error) {
      try {
        await this.clearPreviousData(univerApi, config.id, startCell, fWorksheet)
      } catch (clearError) {
      }
      if (unitId) {
        this.pluginRenderStatusService.set({
          pluginId: config.id,
          type: 'detail',
          status: 'error',
          reason: error instanceof Error ? error.message : 'Unknown error',
          unitId,
          sheetId,
          startCell,
          updatedAt: Date.now()
        })
      }
      return false
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

    const showIndex = isDetailTableIndexVisible(config)
    const indexLabel = config.style?.header?.indexLabel?.trim() || '序号'
    const hideHeader = !!config.style?.base?.hideHeader
    if (!!state.hideHeader !== hideHeader) {
      await this.fillTable(univerApi, config)
      return
    }
    if (state.totalDataSignature !== this.getTotalDataSignature(config)) {
      await this.fillTable(univerApi, config)
      return
    }
    if (!!state.showIndex !== showIndex) {
      await this.fillTable(univerApi, config)
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
        await this.detailTableEditProtectionService.runWithoutProtection(() =>
          setWorksheetValuesSilently(univerApi, {
            unitId,
            sheetId: state.sheetId,
            range: {
              startRow: startPos.row,
              endRow: startPos.row,
              startColumn: startPos.col,
              endColumn: startPos.col
            },
            values: [[indexLabel]]
          })
        )
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
      await this.mergeDimensionCells(univerApi, state, dataRange, unitId)
    }

    this.refreshTargetSheet(univerApi, state.sheetId)
  }

  private async mergeDimensionCells(
    univerApi: any,
    state: DetailTableDisplayState,
    dataRange: { startRow: number; endRow: number; startColumn: number; endColumn: number },
    unitId: string
  ): Promise<void> {
    // 合并层级以实际渲染字段为准，隐藏字段不占列也不形成合并边界。
    const fields = state.fields || []
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

  private getMergeColumnCount(
    fields: Array<{ groupType?: string } | undefined>,
    colCount: number
  ): number {
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
    resolvedFields: Array<ReturnType<typeof findConfiguredField>>,
    customTotalResult: Record<string, number | string | null> = {}
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
      let value: number | undefined
      if (aggregation === 'CUSTOM') {
        value = this.getNumericCellValue(customTotalResult[field.dataeaseName])
      } else {
        const numericValues = dataValues
          .map(row => this.getNumericCellValue(row[columnIndex]))
          .filter((item): item is number => item !== undefined)
        if (numericValues.length) {
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
        }
      }
      if (value === undefined) {
        return
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
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const worksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId() === state.sheetId)
    if (!unitId || !worksheet) {
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
    const dataStartRow = startPos.row + (state.hideHeader ? 0 : 1)
    await this.detailTableEditProtectionService.runWithoutProtection(() =>
      setWorksheetValuesSilently(univerApi, {
        unitId,
        sheetId: state.sheetId,
        range: {
          startRow: dataStartRow,
          endRow: dataStartRow + dataValues.length - 1,
          startColumn: startPos.col,
          endColumn: startPos.col + state.colCount - 1
        },
        values: dataValues
      })
    )
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
    // 未启用单元格样式时，合并后的维度字段仍与普通维度字段保持左对齐。
    await this.detailTableEditProtectionService.runWithoutProtection(() =>
      setWorksheetStylesSilently(univerApi, {
        unitId,
        sheetId,
        ranges,
        style: {
          ht: HorizontalAlign.LEFT,
          vt: VerticalAlign.MIDDLE
        }
      })
    )
  }

  private async clearPreviousData(
    univerApi: any,
    pluginId: string,
    targetStartCell: string,
    targetWorksheet: any
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
    targetWorksheet: any
  ): Promise<void> {
    const startPos = this.parseCell(state.startCell)
    const fWorkbook = univerApi.getActiveWorkbook()
    const fWorksheet = targetWorksheet
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

    if (unitId && subUnitId) {
      await this.detailTableEditProtectionService.runWithoutProtection(() =>
        setWorksheetValuesSilently(univerApi, {
          unitId,
          sheetId: subUnitId,
          range: {
            startRow: startPos.row,
            endRow: startPos.row + state.rowCount - 1,
            startColumn: startPos.col,
            endColumn: startPos.col + state.colCount - 1
          },
          values: emptyValues
        })
      )
    }
  }

  async clearTableData(
    univerApi: any,
    startCell: string,
    rowCount: number,
    colCount: number,
    targetWorksheet: any
  ): Promise<void> {
    const startPos = this.parseCell(startCell)
    const fWorkbook = univerApi.getActiveWorkbook()
    const fWorksheet = targetWorksheet
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

    if (unitId && subUnitId) {
      await this.detailTableEditProtectionService.runWithoutProtection(() =>
        setWorksheetValuesSilently(univerApi, {
          unitId,
          sheetId: subUnitId,
          range: {
            startRow: startPos.row,
            endRow: startPos.row + rowCount - 1,
            startColumn: startPos.col,
            endColumn: startPos.col + colCount - 1
          },
          values: emptyValues
        })
      )
    }

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
      clearWorksheetFormatsSilently(univerApi, {
        unitId,
        sheetId,
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
      col = col * 26 + (colStr.charCodeAt(i) - 64)
    }

    const row = parseInt(rowStr, 10) - 1

    return { row, col: col - 1 }
  }
}
