import {
  AddWorksheetMergeMutation,
  ClearSelectionAllCommand,
  type ISetRangeValuesMutationParams,
  RemoveWorksheetMergeMutation,
  SetRangeValuesMutation
} from '@univerjs/sheets'
import {
  Disposable,
  ICommandService,
  IUniverInstanceService,
  Inject,
  type ICommandInfo,
  type IMutationInfo,
  type IRange,
  type IStyleData,
  type Workbook
} from '@univerjs/core'
import {
  pickDataEaseUserStyle,
  replaceDataEaseUserStyle
} from '../../../../services/plugin-render-range-edit-policy'
import { PluginRenderStatusService } from './plugin-render-status.service'
import { TableRangeConflictService } from './table-range-conflict.service'

type MutableCellMatrix = Record<number, Record<number, unknown>>

/**
 * 统一管理表格渲染区域内的用户样式写入边界。
 * “清除全部”和格式刷都只能修改共享白名单，值、公式、合并及插件样式保持不变。
 */
export class TableUserStyleService extends Disposable {
  constructor(
    @ICommandService private readonly commandService: ICommandService,
    @IUniverInstanceService
    private readonly univerInstanceService: IUniverInstanceService,
    @Inject(TableRangeConflictService)
    private readonly tableRangeConflictService: TableRangeConflictService,
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService
  ) {
    super()
    this.disposeWithMe(
      this.commandService.beforeCommandExecuted(command => this.normalizeClearAllMutation(command))
    )
  }

  sanitizeFormatPainter(
    redoMutations: IMutationInfo[],
    undoMutations: IMutationInfo[],
    unitId: string,
    sheetId: string
  ): void {
    if (!this.mutationsTouchTable(redoMutations, sheetId)) {
      return
    }

    this.sanitizeFormatPainterMutations(redoMutations, unitId, sheetId, true)
    this.sanitizeFormatPainterMutations(undoMutations, unitId, sheetId, false)
  }

  private normalizeClearAllMutation(command: Readonly<ICommandInfo>): void {
    if (command.id !== SetRangeValuesMutation.id) {
      return
    }

    const params = command.params as ISetRangeValuesMutationParams | undefined
    if (
      !params?.unitId ||
      !params.subUnitId ||
      !params.cellValue ||
      params.trigger !== ClearSelectionAllCommand.id
    ) {
      return
    }

    const workbook = this.univerInstanceService.getUniverSheetInstance(params.unitId)
    const worksheet = workbook?.getSheetBySheetId(params.subUnitId)
    if (!workbook || !worksheet) {
      return
    }

    const cellMatrix = params.cellValue as unknown as MutableCellMatrix
    Object.entries(cellMatrix).forEach(([rowKey, rowData]) => {
      const row = Number(rowKey)
      Object.entries(rowData || {}).forEach(([columnKey, cellPatch]) => {
        const column = Number(columnKey)
        if (
          cellPatch !== null ||
          !Number.isFinite(row) ||
          !Number.isFinite(column) ||
          !this.isTableCell(params.subUnitId, row, column)
        ) {
          return
        }

        const rawCell = worksheet.getCellRaw?.(row, column)
        const rawStyle = workbook.getStyles().getStyleByCell(rawCell) || {}
        const nextStyle = replaceDataEaseUserStyle(rawStyle as Partial<IStyleData>, {})

        // Univer 按字段合并样式，白名单字段必须显式置空，才能保留 n 等插件样式。
        rowData[Number(columnKey)] = { s: nextStyle }
      })
    })
  }

  private sanitizeFormatPainterMutations(
    mutations: IMutationInfo[],
    fallbackUnitId: string,
    fallbackSheetId: string,
    applyUserStyle: boolean
  ): void {
    for (let index = mutations.length - 1; index >= 0; index--) {
      const mutation = mutations[index]
      if (mutation.id === SetRangeValuesMutation.id) {
        const hasRemainingCells = this.sanitizeFormatPainterCellMutation(
          mutation.params as ISetRangeValuesMutationParams,
          fallbackUnitId,
          fallbackSheetId,
          applyUserStyle
        )
        if (!hasRemainingCells) {
          mutations.splice(index, 1)
        }
        continue
      }

      if (
        mutation.id === AddWorksheetMergeMutation.id ||
        mutation.id === RemoveWorksheetMergeMutation.id
      ) {
        const hasRemainingRanges = this.sanitizeFormatPainterMergeMutation(
          mutation.params as Record<string, unknown>,
          fallbackSheetId
        )
        if (!hasRemainingRanges) {
          mutations.splice(index, 1)
        }
      }
    }
  }

  private sanitizeFormatPainterCellMutation(
    params: ISetRangeValuesMutationParams,
    fallbackUnitId: string,
    fallbackSheetId: string,
    applyUserStyle: boolean
  ): boolean {
    if (!params.cellValue) {
      return false
    }

    const unitId = params.unitId || fallbackUnitId
    const sheetId = params.subUnitId || fallbackSheetId
    const workbook = this.univerInstanceService.getUniverSheetInstance(unitId)
    const worksheet = workbook?.getSheetBySheetId(sheetId)
    if (!workbook || !worksheet) {
      return true
    }

    const cellMatrix = params.cellValue as unknown as MutableCellMatrix
    Object.entries(cellMatrix).forEach(([rowKey, rowData]) => {
      const row = Number(rowKey)
      Object.entries(rowData || {}).forEach(([columnKey, cellPatch]) => {
        const column = Number(columnKey)
        if (
          !Number.isFinite(row) ||
          !Number.isFinite(column) ||
          !this.isTableCell(sheetId, row, column)
        ) {
          return
        }

        if (!cellPatch || typeof cellPatch !== 'object') {
          delete rowData[Number(columnKey)]
          return
        }

        const patch = cellPatch as Record<string, unknown>
        if (!applyUserStyle) {
          // 撤销只保留格式刷自身的样式回滚，合并产生的内容恢复不应触碰插件单元格。
          if (Object.keys(patch).some(key => key !== 's')) {
            delete rowData[Number(columnKey)]
          }
          return
        }

        if (!Object.prototype.hasOwnProperty.call(patch, 's')) {
          delete rowData[Number(columnKey)]
          return
        }

        const rawCell = worksheet.getCellRaw?.(row, column)
        const currentStyle = workbook.getStyles().getStyleByCell(rawCell) || {}
        const sourceStyle = this.resolveStyle(workbook, patch.s)
        const userStyle = pickDataEaseUserStyle(sourceStyle)
        const nextStyle = replaceDataEaseUserStyle(
          currentStyle as Partial<IStyleData>,
          userStyle
        )

        rowData[Number(columnKey)] = { s: nextStyle }
      })

      if (Object.keys(rowData || {}).length === 0) {
        delete cellMatrix[Number(rowKey)]
      }
    })

    return Object.keys(cellMatrix).length > 0
  }

  private sanitizeFormatPainterMergeMutation(
    params: Record<string, unknown>,
    fallbackSheetId: string
  ): boolean {
    const sheetId = typeof params.subUnitId === 'string' ? params.subUnitId : fallbackSheetId
    if (Array.isArray(params.ranges)) {
      const retainedRanges = params.ranges.filter(range =>
        !this.rangeIntersectsTable(sheetId, range as IRange)
      )
      params.ranges = retainedRanges
      return retainedRanges.length > 0
    }

    if (params.range && this.rangeIntersectsTable(sheetId, params.range as IRange)) {
      return false
    }
    return true
  }

  private mutationsTouchTable(mutations: IMutationInfo[], fallbackSheetId: string): boolean {
    return mutations.some(mutation => {
      if (mutation.id !== SetRangeValuesMutation.id) {
        return false
      }

      const params = mutation.params as ISetRangeValuesMutationParams
      const sheetId = params.subUnitId || fallbackSheetId
      const cellMatrix = params.cellValue as unknown as MutableCellMatrix | undefined
      return Object.entries(cellMatrix || {}).some(([rowKey, rowData]) =>
        Object.keys(rowData || {}).some(columnKey =>
          this.isTableCell(sheetId, Number(rowKey), Number(columnKey))
        )
      )
    })
  }

  private resolveStyle(workbook: Workbook, style: unknown): Partial<IStyleData> {
    if (typeof style === 'string') {
      return workbook.getStyles().get(style) || {}
    }
    return style && typeof style === 'object' ? style as Partial<IStyleData> : {}
  }

  private rangeIntersectsTable(sheetId: string, range: IRange): boolean {
    if (this.tableRangeConflictService.findConflict({ sheetId, ...range })) {
      return true
    }

    return this.pluginRenderStatusService.list().some(status => {
      if (status.sheetId !== sheetId || !status.startCell || status.status === 'rendered') {
        return false
      }
      const start = this.parseCell(status.startCell)
      return start.row >= range.startRow &&
        start.row <= range.endRow &&
        start.column >= range.startColumn &&
        start.column <= range.endColumn
    })
  }

  private isTableCell(sheetId: string, row: number, column: number): boolean {
    if (!Number.isFinite(row) || !Number.isFinite(column)) {
      return false
    }
    const renderedRange = this.tableRangeConflictService.findConflict({
      sheetId,
      startRow: row,
      endRow: row,
      startColumn: column,
      endColumn: column
    })
    if (renderedRange) {
      return true
    }

    return this.pluginRenderStatusService.list().some(status => {
      if (status.sheetId !== sheetId || !status.startCell || status.status === 'rendered') {
        return false
      }
      const start = this.parseCell(status.startCell)
      return start.row === row && start.column === column
    })
  }

  private parseCell(cellAddress: string): { row: number; column: number } {
    const match = cellAddress.match(/^([A-Z]+)(\d+)$/i)
    if (!match) {
      return { row: -1, column: -1 }
    }

    let column = 0
    for (const char of match[1].toUpperCase()) {
      column = column * 26 + char.charCodeAt(0) - 64
    }
    return {
      row: Number(match[2]) - 1,
      column: column - 1
    }
  }
}
