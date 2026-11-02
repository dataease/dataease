import {
  AddWorksheetMergeMutation,
  ClearSelectionAllCommand,
  ClearSelectionFormatCommand,
  type IClearSelectionFormatCommandParams,
  type IRemoveWorksheetMergeMutationParams,
  type ISetRangeValuesMutationParams,
  RemoveWorksheetMergeMutation,
  SetRangeValuesMutation,
  SetStyleCommand
} from '@univerjs/sheets'
import {
  CommandType,
  Disposable,
  ICommandService,
  IUndoRedoService,
  IUniverInstanceService,
  Inject,
  ObjectMatrix,
  Rectangle,
  Tools,
  UniverInstanceType,
  sequenceExecute,
  type ICellData,
  type IExecutionOptions,
  type IMutationInfo,
  type IRange,
  type IStyleData,
  type IUndoRedoItem,
  type Nullable,
  type Workbook
} from '@univerjs/core'
import {
  pickDataEaseUserStyle,
  replaceDataEaseUserStyle
} from '../../../../services/plugin-render-range-edit-policy'
import { PluginRenderStatusService } from './plugin-render-status.service'
import { TableRangeConflictService } from './table-range-conflict.service'

type MutableCellMatrix = Record<number, Record<number, unknown>>

const REPLAY_TABLE_CLEAR_MUTATION_ID = 'dataease.mutation.replay-table-clear'

interface ReplayTableClearParams {
  mutation: IMutationInfo
}

/**
 * 统一管理表格渲染区域内的用户样式写入边界。
 * 清除格式、清除全部和格式刷在表格内只修改用户样式，保留数据及插件控制的格式和合并。
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
    this.initClearCommands()
  }

  private initClearCommands(): void {
    this.disposeWithMe(
      this.commandService.registerCommand({
        id: REPLAY_TABLE_CLEAR_MUTATION_ID,
        type: CommandType.MUTATION,
        handler: (_accessor, params: ReplayTableClearParams, options) => {
          if (!params?.mutation || !this.isClearMutation(params.mutation.id)) {
            return false
          }
          // 查询可能改变表格范围；重放时重新保护当前范围，不修改原历史记录。
          return this.executeClearMutation(Tools.deepClone(params.mutation), options)
        }
      })
    )

    // 顶部清除格式和右键入口共用底层命令，保留原生附加操作及单步撤销语义。
    for (const command of [ClearSelectionFormatCommand, ClearSelectionAllCommand]) {
      this.commandService.unregisterCommand(command.id)
      this.disposeWithMe(
        this.commandService.registerCommand({
          ...command,
          handler: (accessor, params: IClearSelectionFormatCommandParams | undefined, options) => {
            const commandService = this.commandService
            const undoRedoService = accessor.get(IUndoRedoService)
            const commandsProxy = new Proxy(commandService, {
              get: (target, property) => {
                if (property === 'syncExecuteCommand') {
                  return (
                    id: string,
                    mutationParams: object,
                    executionOptions?: IExecutionOptions
                  ) => {
                    if (this.isClearMutation(id)) {
                      return this.executeClearMutation(
                        { id, params: mutationParams },
                        executionOptions
                      )
                    }
                    return target.syncExecuteCommand(id, mutationParams, executionOptions)
                  }
                }
                const value = Reflect.get(target, property)
                return typeof value === 'function' ? value.bind(target) : value
              }
            })
            const historyProxy = new Proxy(undoRedoService, {
              get: (target, property) => {
                if (property === 'pushUndoRedo') {
                  return (item: IUndoRedoItem) =>
                    target.pushUndoRedo({
                      ...item,
                      undoMutations: this.prepareClearHistory(item.undoMutations),
                      redoMutations: this.prepareClearHistory(item.redoMutations)
                    })
                }
                const value = Reflect.get(target, property)
                return typeof value === 'function' ? value.bind(target) : value
              }
            })
            const clearAccessor = new Proxy(accessor, {
              get(target, property) {
                if (property === 'get') {
                  return (id: Parameters<typeof accessor.get>[0]) => {
                    if (id === ICommandService) return commandsProxy
                    if (id === IUndoRedoService) return historyProxy
                    return target.get(id)
                  }
                }
                return Reflect.get(target, property)
              }
            })
            return command.handler(clearAccessor, params, options)
          }
        })
      )
    }
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

  private isClearMutation(id: string): boolean {
    return (
      id === SetRangeValuesMutation.id ||
      id === AddWorksheetMergeMutation.id ||
      id === RemoveWorksheetMergeMutation.id
    )
  }

  private executeClearMutation(mutation: IMutationInfo, options?: IExecutionOptions): boolean {
    const mutations = this.prepareClearMutation(mutation, true)
    return sequenceExecute(mutations, this.commandService, options).result
  }

  private prepareClearHistory(mutations: IMutationInfo[]): IMutationInfo[] {
    return mutations.flatMap(mutation => {
      if (!this.isClearMutation(mutation.id)) {
        return [mutation]
      }
      // 入栈前永久去掉原表格区域的旧数据，防止查询缩小后又恢复到空白区域。
      return this.prepareClearMutation(mutation, false).map(prepared => ({
        id: REPLAY_TABLE_CLEAR_MUTATION_ID,
        params: { mutation: prepared }
      }))
    })
  }

  private prepareClearMutation(
    mutation: IMutationInfo,
    resolveRemovedMerges: boolean
  ): IMutationInfo[] {
    if (mutation.id === SetRangeValuesMutation.id) {
      return this.prepareClearCellMutation(mutation.params as ISetRangeValuesMutationParams)
    }

    const params = mutation.params as IRemoveWorksheetMergeMutationParams
    const workbook = this.univerInstanceService.getUnit<Workbook>(
      params.unitId,
      UniverInstanceType.UNIVER_SHEET
    )
    const worksheet = workbook?.getSheetBySheetId(params.subUnitId)
    if (!worksheet) {
      return [mutation]
    }

    let ranges = params.ranges
    if (mutation.id === RemoveWorksheetMergeMutation.id && resolveRemovedMerges) {
      // 原生命令传入整个选区，删除按相交执行；先收敛到实际普通合并，保留跨表格边界的合并。
      ranges = worksheet
        .getMergeData()
        .filter(merge => params.ranges.some(range => Rectangle.intersects(range, merge)))
    }
    params.ranges = ranges.filter(range => !this.rangeIntersectsTable(params.subUnitId, range))
    return params.ranges.length ? [mutation] : []
  }

  private prepareClearCellMutation(params: ISetRangeValuesMutationParams): IMutationInfo[] {
    const workbook = this.univerInstanceService.getUnit<Workbook>(
      params.unitId,
      UniverInstanceType.UNIVER_SHEET
    )
    if (!params.cellValue || !workbook?.getSheetBySheetId(params.subUnitId)) {
      return [{ id: SetRangeValuesMutation.id, params }]
    }

    const allCells = new ObjectMatrix<Nullable<ICellData>>()
    const styleCells = new ObjectMatrix<Nullable<ICellData>>()
    const contentCells = new ObjectMatrix<Nullable<ICellData>>()
    new ObjectMatrix(params.cellValue).forValue((row, column, cell) => {
      let patch = cell
      if (this.isTableCell(params.subUnitId, row, column)) {
        if (cell && !Object.prototype.hasOwnProperty.call(cell, 's')) {
          return
        }
        const sourceStyle = this.resolveStyle(workbook, cell?.s)
        // 清除格式传入 { s: null }，清除全部传入 null；撤销也只恢复白名单样式。
        // 不保存当时的 n 等插件样式，重放时通过样式字段合并保留最新配置。
        patch = { s: replaceDataEaseUserStyle({}, pickDataEaseUserStyle(sourceStyle)) }
      }
      allCells.setValue(row, column, patch)
      if (
        patch &&
        Object.keys(patch).length === 1 &&
        Object.prototype.hasOwnProperty.call(patch, 's')
      ) {
        styleCells.setValue(row, column, patch)
      } else {
        contentCells.setValue(row, column, patch)
      }
    })

    params.cellValue = allCells.getMatrix()
    const mutations: IMutationInfo[] = []
    // 纯样式必须独立执行，否则公式同步会把未携带 f 的表格单元格当作清除公式。
    if (styleCells.hasValue()) {
      mutations.push({
        id: SetRangeValuesMutation.id,
        params: { ...params, cellValue: styleCells.getMatrix(), isOverrideStyle: false }
      })
    }
    // 普通单元格保留原生完整撤销数据，包括用于恢复富文本内部样式的 p。
    if (contentCells.hasValue()) {
      mutations.push({
        id: SetRangeValuesMutation.id,
        params: { ...params, cellValue: contentCells.getMatrix() }
      })
    }
    return mutations
  }

  private sanitizeFormatPainterMutations(
    mutations: IMutationInfo[],
    fallbackUnitId: string,
    fallbackSheetId: string,
    isRedo: boolean
  ): void {
    for (let index = mutations.length - 1; index >= 0; index--) {
      const mutation = mutations[index]
      if (mutation.id === SetRangeValuesMutation.id) {
        const hasRemainingCells = this.sanitizeFormatPainterCellMutation(
          mutation.params as ISetRangeValuesMutationParams,
          fallbackUnitId,
          fallbackSheetId,
          isRedo
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
    isRedo: boolean
  ): boolean {
    if (!params.cellValue) {
      return false
    }

    const unitId = params.unitId || fallbackUnitId
    const sheetId = params.subUnitId || fallbackSheetId
    const workbook = this.univerInstanceService.getUnit<Workbook>(
      unitId,
      UniverInstanceType.UNIVER_SHEET
    )
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
        // 样式撤销也携带原值和公式；仅放行样式操作，继续过滤合并产生的内容恢复。
        if (
          !isRedo &&
          params.trigger !== SetStyleCommand.id &&
          Object.keys(patch).some(key => key !== 's')
        ) {
          delete rowData[Number(columnKey)]
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
        const nextStyle = replaceDataEaseUserStyle(currentStyle as Partial<IStyleData>, userStyle)

        // 应用和撤销都只写入样式，避免恢复旧值、公式或插件元数据。
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
      const retainedRanges = params.ranges.filter(
        range => !this.rangeIntersectsTable(sheetId, range as IRange)
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
    return style && typeof style === 'object' ? (style as Partial<IStyleData>) : {}
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
      return (
        start.row >= range.startRow &&
        start.row <= range.endRow &&
        start.column >= range.startColumn &&
        start.column <= range.endColumn
      )
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
