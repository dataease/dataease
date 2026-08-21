import {
  Disposable,
  Direction,
  ICommandService,
  Inject,
  sequenceExecute,
  type ICellData,
  type IRange
} from '@univerjs/core'
import {
  InsertColMutation,
  InsertRowMutation,
  RemoveColMutation,
  RemoveRowMutation,
  SheetInterceptorService
} from '@univerjs/sheets'
import { DetailTableDisplayStateService } from '../plugins/DataEaseDetailTablePlugin/services/detail-table-display-state.service'
import { DetailTableInstanceService } from '../plugins/DataEaseDetailTablePlugin/services/detail-table-instance.service'
import { DetailTableRenderStyleService } from '../plugins/DataEaseDetailTablePlugin/services/detail-table-render-style.service'
import { PivotTableDisplayStateService } from '../plugins/DataEasePivotTablePlugin/services/pivot-table-display-state.service'
import { PivotTableInstanceService } from '../plugins/DataEasePivotTablePlugin/services/pivot-table-instance.service'
import { PivotTableRenderStyleService } from '../plugins/DataEasePivotTablePlugin/services/pivot-table-render-style.service'
import {
  PluginRenderHoverLayerService,
  PluginRenderHoverService,
  PluginRenderLoadingService,
  PluginRenderStatusService
} from '../plugins/DataEaseRuntimePlugin/services/table'
import { SpreadsheetModeService } from './spreadsheet-mode.service'

const INSERT_ROW_COMMAND_ID = 'sheet.command.insert-row'
const INSERT_COL_COMMAND_ID = 'sheet.command.insert-col'

type TableType = 'detail' | 'pivot'

interface TableRange extends IRange {
  pluginId: string
  sheetId: string
}

interface EnsureRenderSpaceParams {
  unitId: string
  sheetId: string
  pluginId: string
  tableType: TableType
  worksheet: any
  startCell: string
  rowCount: number
  columnCount: number
  initialRestore?: boolean
}

interface InsertCandidate {
  type: 'row' | 'column'
  position: number
  count: number
}

/**
 * 两类表格共用的结构扩容协调器。
 * 数据查询和最终写入也放入同一 Sheet 队列，避免另一个实例插入行列后继续使用旧坐标。
 */
export class TableRenderExpansionService extends Disposable {
  private readonly sheetQueues = new Map<string, Promise<void>>()

  constructor(
    @ICommandService private readonly commandService: ICommandService,
    @Inject(SheetInterceptorService)
    private readonly sheetInterceptorService: SheetInterceptorService,
    @Inject(SpreadsheetModeService)
    private readonly spreadsheetModeService: SpreadsheetModeService,
    @Inject(DetailTableInstanceService)
    private readonly detailInstanceService: DetailTableInstanceService,
    @Inject(DetailTableDisplayStateService)
    private readonly detailDisplayStateService: DetailTableDisplayStateService,
    @Inject(DetailTableRenderStyleService)
    private readonly detailRenderStyleService: DetailTableRenderStyleService,
    @Inject(PivotTableInstanceService)
    private readonly pivotInstanceService: PivotTableInstanceService,
    @Inject(PivotTableDisplayStateService)
    private readonly pivotDisplayStateService: PivotTableDisplayStateService,
    @Inject(PivotTableRenderStyleService)
    private readonly pivotRenderStyleService: PivotTableRenderStyleService,
    @Inject(PluginRenderLoadingService)
    private readonly pluginRenderLoadingService: PluginRenderLoadingService,
    @Inject(PluginRenderHoverService)
    private readonly pluginRenderHoverService: PluginRenderHoverService,
    @Inject(PluginRenderHoverLayerService)
    private readonly pluginRenderHoverLayerService: PluginRenderHoverLayerService,
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService
  ) {
    super()
    this.disposeWithMe(
      this.commandService.onCommandExecuted(commandInfo => {
        const params = (commandInfo.params || {}) as any
        const unitId = params.unitId
        const sheetId = params.subUnitId
        const range = params.range as IRange | undefined
        if (!unitId || !sheetId || !range) {
          return
        }

        // 删除区间已经由编辑保护校验；mutation 成功后只移动位于删除区间之后的实例。
        if (commandInfo.id === RemoveRowMutation.id) {
          const removedCount = range.endRow - range.startRow + 1
          this.shiftRows(unitId, sheetId, range.endRow + 1, -removedCount)
        } else if (commandInfo.id === RemoveColMutation.id) {
          const removedCount = range.endColumn - range.startColumn + 1
          this.shiftColumns(unitId, sheetId, range.endColumn + 1, -removedCount)
        }
      })
    )
  }

  async runExclusive<T>(unitId: string, sheetId: string, handler: () => Promise<T>): Promise<T> {
    const queueKey = `${unitId}|${sheetId}`
    const previous = this.sheetQueues.get(queueKey) || Promise.resolve()
    let releaseQueue!: () => void
    const current = new Promise<void>(resolve => {
      releaseQueue = resolve
    })
    const queueTail = previous.catch(() => undefined).then(() => current)
    this.sheetQueues.set(queueKey, queueTail)

    await previous.catch(() => undefined)
    try {
      return await handler()
    } finally {
      releaseQueue()
      if (this.sheetQueues.get(queueKey) === queueTail) {
        this.sheetQueues.delete(queueKey)
      }
    }
  }

  resolveStartCell(
    unitId: string,
    tableType: TableType,
    pluginId: string,
    fallback: string
  ): string {
    if (tableType === 'detail') {
      return this.detailInstanceService
        .get(unitId)
        .find(instance => instance.id === pluginId)
        ?.placement.startCell || fallback
    }
    return this.pivotInstanceService
      .get(unitId)
      .find(instance => instance.id === pluginId)
      ?.placement.startCell || fallback
  }

  async ensureRenderSpace(params: EnsureRenderSpaceParams): Promise<string | undefined> {
    const {
      unitId,
      sheetId,
      pluginId,
      tableType,
      worksheet,
      startCell,
      rowCount,
      columnCount,
      initialRestore
    } = params
    if (rowCount <= 0 || columnCount <= 0) {
      return undefined
    }

    const start = this.parseCell(startCell)
    const targetRange: TableRange = {
      pluginId,
      sheetId,
      startRow: start.row,
      endRow: start.row + rowCount - 1,
      startColumn: start.col,
      endColumn: start.col + columnCount - 1
    }
    if (initialRestore) {
      // 初始快照可能保留插件区域的用户样式，此时只按实例锚点判断，不能把样式单元格当作外部数据。
      return this.ensureInitialRestoreAnchorSpace(unitId, worksheet, targetRange)
    }

    const currentRange = this.getCurrentRange(tableType, pluginId)
    const samePlacement = currentRange?.sheetId === sheetId &&
      currentRange.startRow === targetRange.startRow &&
      currentRange.startColumn === targetRange.startColumn

    if (!samePlacement) {
      return this.ensureInitialPlacementSpace(unitId, worksheet, targetRange, currentRange)
    }

    const rowGrowth = Math.max(targetRange.endRow - currentRange.endRow, 0)
    const columnGrowth = Math.max(targetRange.endColumn - currentRange.endColumn, 0)
    if (rowGrowth <= 0 && columnGrowth <= 0) {
      return undefined
    }

    const candidates: InsertCandidate[] = []
    if (rowGrowth > 0) {
      const bottomRange: IRange = {
        startRow: currentRange.endRow + 1,
        endRow: targetRange.endRow,
        startColumn: targetRange.startColumn,
        endColumn: targetRange.endColumn
      }
      if (this.hasOccupiedContent(unitId, worksheet, bottomRange, currentRange, pluginId)) {
        candidates.push({
          type: 'row',
          position: currentRange.endRow + 1,
          count: rowGrowth
        })
      }
    }

    if (columnGrowth > 0) {
      const rightRange: IRange = {
        startRow: targetRange.startRow,
        endRow: Math.min(currentRange.endRow, targetRange.endRow),
        startColumn: currentRange.endColumn + 1,
        endColumn: targetRange.endColumn
      }
      if (this.hasOccupiedContent(unitId, worksheet, rightRange, currentRange, pluginId)) {
        candidates.push({
          type: 'column',
          position: currentRange.endColumn + 1,
          count: columnGrowth
        })
      }
    }

    // 同时横纵扩容时先完成全部校验，避免可预防的部分插入。
    for (const candidate of candidates) {
      const invalidMessage = this.validateInsertLine(
        unitId,
        sheetId,
        worksheet,
        candidate.type,
        candidate.position
      )
      if (invalidMessage) {
        return invalidMessage
      }
    }

    for (const candidate of candidates) {
      if (candidate.type === 'row') {
        await this.insertRows(
          unitId,
          sheetId,
          worksheet,
          candidate.position,
          candidate.count,
          pluginId
        )
      } else {
        await this.insertColumns(
          unitId,
          sheetId,
          worksheet,
          candidate.position,
          candidate.count,
          pluginId
        )
      }
    }

    return undefined
  }

  private async ensureInitialRestoreAnchorSpace(
    unitId: string,
    worksheet: any,
    targetRange: TableRange
  ): Promise<string | undefined> {
    const conflictingAnchors = this.getInstanceAnchors(
      unitId,
      targetRange.sheetId,
      targetRange.pluginId
    ).filter(anchor => this.containsCell(targetRange, anchor.startRow, anchor.startColumn))
    if (!conflictingAnchors.length) {
      return undefined
    }

    const firstRow = Math.min(...conflictingAnchors.map(anchor => anchor.startRow))
    const firstColumn = Math.min(...conflictingAnchors.map(anchor => anchor.startColumn))
    // 从最靠前的冲突锚点插入最小数量，将目标区域内的所有实例起点整体推出边界。
    const candidates: InsertCandidate[] = [
      {
        type: 'row',
        position: firstRow,
        count: targetRange.endRow - firstRow + 1
      },
      {
        type: 'column',
        position: firstColumn,
        count: targetRange.endColumn - firstColumn + 1
      }
    ].sort((left, right) => left.count - right.count)

    for (const candidate of candidates) {
      const invalidMessage = this.validateInsertLine(
        unitId,
        targetRange.sheetId,
        worksheet,
        candidate.type,
        candidate.position
      )
      if (invalidMessage) {
        continue
      }

      if (candidate.type === 'row') {
        await this.insertRows(
          unitId,
          targetRange.sheetId,
          worksheet,
          candidate.position,
          candidate.count,
          targetRange.pluginId
        )
      } else {
        await this.insertColumns(
          unitId,
          targetRange.sheetId,
          worksheet,
          candidate.position,
          candidate.count,
          targetRange.pluginId
        )
      }
      return undefined
    }

    return '初始渲染区域包含其他表格起始位置，且可用插入位置会穿过现有表格或合并单元格'
  }

  private async ensureInitialPlacementSpace(
    unitId: string,
    worksheet: any,
    targetRange: TableRange,
    currentRange?: TableRange
  ): Promise<string | undefined> {
    if (!this.hasOccupiedContent(
      unitId,
      worksheet,
      targetRange,
      currentRange,
      targetRange.pluginId
    )) {
      return undefined
    }

    // 首次落位没有既有边界，优先选择插入数量更少且不会穿过插件实例的方向。
    const candidates: InsertCandidate[] = [
      {
        type: 'row',
        position: targetRange.startRow,
        count: targetRange.endRow - targetRange.startRow + 1
      },
      {
        type: 'column',
        position: targetRange.startColumn,
        count: targetRange.endColumn - targetRange.startColumn + 1
      }
    ].sort((left, right) => left.count - right.count)

    for (const candidate of candidates) {
      const invalidMessage = this.validateInsertLine(
        unitId,
        targetRange.sheetId,
        worksheet,
        candidate.type,
        candidate.position
      )
      if (invalidMessage) {
        continue
      }
      if (candidate.type === 'row') {
        await this.insertRows(
          unitId,
          targetRange.sheetId,
          worksheet,
          candidate.position,
          candidate.count,
          targetRange.pluginId
        )
      } else {
        await this.insertColumns(
          unitId,
          targetRange.sheetId,
          worksheet,
          candidate.position,
          candidate.count,
          targetRange.pluginId
        )
      }
      return undefined
    }

    return '渲染区域已有数据，且可用插入位置会穿过现有表格实例'
  }

  private validateInsertLine(
    unitId: string,
    sheetId: string,
    worksheet: any,
    type: 'row' | 'column',
    position: number
  ): string | undefined {
    const crossesTable = this.getTrackedRanges(unitId, sheetId).some(range => {
      if (type === 'row') {
        return range.startRow < position && position <= range.endRow
      }
      return range.startColumn < position && position <= range.endColumn
    })
    if (crossesTable) {
      return type === 'row'
        ? '无法自动扩容：插入行会穿过现有表格实例'
        : '无法自动扩容：插入列会穿过现有表格实例'
    }

    const crossesMerge = worksheet?.getMergedRanges?.().some((mergedRange: any) => {
      const range = mergedRange.getRange?.() || mergedRange
      if (type === 'row') {
        return range.startRow < position && position <= range.endRow
      }
      return range.startColumn < position && position <= range.endColumn
    })
    if (crossesMerge) {
      return type === 'row'
        ? '无法自动扩容：插入行会穿过合并单元格'
        : '无法自动扩容：插入列会穿过合并单元格'
    }

    return undefined
  }

  private hasOccupiedContent(
    unitId: string,
    worksheet: any,
    range: IRange,
    allowedRange: TableRange | undefined,
    pluginId: string
  ): boolean {
    if (range.startRow > range.endRow || range.startColumn > range.endColumn) {
      return false
    }

    const rowCount = range.endRow - range.startRow + 1
    const columnCount = range.endColumn - range.startColumn + 1
    const targetRange = worksheet.getRange(
      range.startRow,
      range.startColumn,
      rowCount,
      columnCount
    )
    const cellData = targetRange.getCellDatas?.() as Array<Array<ICellData | null>> | undefined
    const values = cellData ? undefined : targetRange.getValues?.()
    const formulas = cellData ? undefined : targetRange.getFormulas?.()

    for (let rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      for (let columnIndex = 0; columnIndex < columnCount; columnIndex++) {
        const row = range.startRow + rowIndex
        const column = range.startColumn + columnIndex
        if (allowedRange && this.containsCell(allowedRange, row, column)) {
          continue
        }

        const cell = cellData?.[rowIndex]?.[columnIndex]
        if (cell && this.hasCellContent(cell)) {
          return true
        }
        const value = values?.[rowIndex]?.[columnIndex]
        const formula = formulas?.[rowIndex]?.[columnIndex]
        if (!this.isEmptyValue(value) || !!formula) {
          return true
        }
      }
    }

    const hasMergedCell = worksheet.getMergedRanges?.().some((mergedRange: any) => {
      const merged = mergedRange.getRange?.() || mergedRange
      if (!this.intersects(range, merged)) {
        return false
      }
      return !allowedRange || !this.containsRange(allowedRange, merged)
    })
    if (hasMergedCell) {
      return true
    }

    return this.getTrackedRanges(unitId, worksheet.getSheetId())
      .some(item => item.pluginId !== pluginId && this.intersects(range, item))
  }

  private async insertRows(
    unitId: string,
    sheetId: string,
    worksheet: any,
    position: number,
    count: number,
    targetPluginId: string
  ): Promise<void> {
    const range: IRange = {
      startRow: position,
      endRow: position + count - 1,
      startColumn: 0,
      endColumn: Math.max(worksheet.getColumnCount?.() - 1, 0)
    }
    await this.executeInsertMutation(
      INSERT_ROW_COMMAND_ID,
      InsertRowMutation.id,
      {
        unitId,
        subUnitId: sheetId,
        direction: Direction.UP,
        range
      },
      () => this.shiftRows(unitId, sheetId, position, count, targetPluginId)
    )
  }

  private async insertColumns(
    unitId: string,
    sheetId: string,
    worksheet: any,
    position: number,
    count: number,
    targetPluginId: string
  ): Promise<void> {
    const range: IRange = {
      startRow: 0,
      endRow: Math.max(worksheet.getRowCount?.() - 1, 0),
      startColumn: position,
      endColumn: position + count - 1
    }
    await this.executeInsertMutation(
      INSERT_COL_COMMAND_ID,
      InsertColMutation.id,
      {
        unitId,
        subUnitId: sheetId,
        direction: Direction.LEFT,
        range
      },
      () => this.shiftColumns(unitId, sheetId, position, count, targetPluginId)
    )
  }

  private async executeInsertMutation(
    commandId: string,
    mutationId: string,
    params: { unitId: string; subUnitId: string; direction: Direction; range: IRange },
    onInserted: () => void
  ): Promise<void> {
    const intercepted = this.sheetInterceptorService.onCommandExecute({ id: commandId, params })
    await this.spreadsheetModeService.runAsSystemWrite(async () => {
      const preRedos = intercepted.preRedos || []
      if (preRedos.length > 0) {
        const preResult = sequenceExecute(preRedos, this.commandService).result
        if (!preResult) {
          throw new Error('自动插入行列前置处理失败')
        }
      }

      const inserted = this.commandService.syncExecuteCommand(mutationId, {
        unitId: params.unitId,
        subUnitId: params.subUnitId,
        range: params.range
      })
      if (!inserted) {
        throw new Error('自动插入行列失败')
      }

      // 插入一旦成功就立即同步插件坐标；后续联动失败也不回滚结构扩容。
      onInserted()
      const redos = intercepted.redos || []
      if (redos.length > 0) {
        const postResult = sequenceExecute(redos, this.commandService).result
        if (!postResult) {
          throw new Error('自动插入行列联动处理失败')
        }
      }

      const afterIntercepted = this.sheetInterceptorService.afterCommandExecute({
        id: commandId,
        params
      })
      const afterRedos = afterIntercepted.redos || []
      if (afterRedos.length > 0) {
        const afterResult = sequenceExecute(afterRedos, this.commandService).result
        if (!afterResult) {
          throw new Error('自动插入行列后置处理失败')
        }
      }
    })
  }

  private shiftRows(
    unitId: string,
    sheetId: string,
    position: number,
    count: number,
    targetPluginId?: string
  ): void {
    this.shiftInstanceRows(
      this.detailInstanceService.get(unitId),
      sheetId,
      position,
      count,
      targetPluginId
    )
    this.shiftInstanceRows(
      this.pivotInstanceService.get(unitId),
      sheetId,
      position,
      count,
      targetPluginId
    )
    this.shiftDisplayStateRows(this.detailDisplayStateService, sheetId, position, count)
    this.shiftDisplayStateRows(this.pivotDisplayStateService, sheetId, position, count)
    this.detailRenderStyleService.shiftRows(unitId, sheetId, position, count, targetPluginId)
    this.pivotRenderStyleService.shiftRows(unitId, sheetId, position, count, targetPluginId)
    this.pluginRenderLoadingService.shiftRows(unitId, sheetId, position, count, targetPluginId)
    this.pluginRenderStatusService.shiftRows(unitId, sheetId, position, count, targetPluginId)
    this.clearTransientHover()
  }

  private shiftColumns(
    unitId: string,
    sheetId: string,
    position: number,
    count: number,
    targetPluginId?: string
  ): void {
    this.shiftInstanceColumns(
      this.detailInstanceService.get(unitId),
      sheetId,
      position,
      count,
      targetPluginId
    )
    this.shiftInstanceColumns(
      this.pivotInstanceService.get(unitId),
      sheetId,
      position,
      count,
      targetPluginId
    )
    this.shiftDisplayStateColumns(this.detailDisplayStateService, sheetId, position, count)
    this.shiftDisplayStateColumns(this.pivotDisplayStateService, sheetId, position, count)
    this.detailRenderStyleService.shiftColumns(unitId, sheetId, position, count, targetPluginId)
    this.pivotRenderStyleService.shiftColumns(unitId, sheetId, position, count, targetPluginId)
    this.pluginRenderLoadingService.shiftColumns(unitId, sheetId, position, count, targetPluginId)
    this.pluginRenderStatusService.shiftColumns(unitId, sheetId, position, count, targetPluginId)
    this.clearTransientHover()
  }

  private clearTransientHover(): void {
    if (this.pluginRenderHoverService.clearHoverRange()) {
      this.pluginRenderHoverLayerService.clear()
    }
  }

  private shiftInstanceRows(
    instances: Array<{ id: string; placement: { sheetId: string; startCell: string } }>,
    sheetId: string,
    position: number,
    count: number,
    targetPluginId?: string
  ): void {
    instances.forEach(instance => {
      if (instance.id === targetPluginId || instance.placement.sheetId !== sheetId) {
        return
      }
      const start = this.parseCell(instance.placement.startCell)
      if (start.row >= position) {
        instance.placement.startCell = this.toCellAddress(start.row + count, start.col)
      }
    })
  }

  private shiftInstanceColumns(
    instances: Array<{ id: string; placement: { sheetId: string; startCell: string } }>,
    sheetId: string,
    position: number,
    count: number,
    targetPluginId?: string
  ): void {
    instances.forEach(instance => {
      if (instance.id === targetPluginId || instance.placement.sheetId !== sheetId) {
        return
      }
      const start = this.parseCell(instance.placement.startCell)
      if (start.col >= position) {
        instance.placement.startCell = this.toCellAddress(start.row, start.col + count)
      }
    })
  }

  private shiftDisplayStateRows(
    service: { list: () => any[]; set: (state: any) => void },
    sheetId: string,
    position: number,
    count: number
  ): void {
    service.list().forEach(state => {
      if (state.sheetId !== sheetId) {
        return
      }
      const start = this.parseCell(state.startCell)
      if (start.row >= position) {
        service.set({
          ...state,
          startCell: this.toCellAddress(start.row + count, start.col),
          updatedAt: Date.now()
        })
      }
    })
  }

  private shiftDisplayStateColumns(
    service: { list: () => any[]; set: (state: any) => void },
    sheetId: string,
    position: number,
    count: number
  ): void {
    service.list().forEach(state => {
      if (state.sheetId !== sheetId) {
        return
      }
      const start = this.parseCell(state.startCell)
      if (start.col >= position) {
        service.set({
          ...state,
          startCell: this.toCellAddress(start.row, start.col + count),
          updatedAt: Date.now()
        })
      }
    })
  }

  private getCurrentRange(tableType: TableType, pluginId: string): TableRange | undefined {
    if (tableType === 'detail') {
      const state = this.detailDisplayStateService.get(pluginId)
      return state
        ? this.buildTableRange(pluginId, state.sheetId, state.startCell, state.rowCount, state.colCount)
        : undefined
    }

    const state = this.pivotDisplayStateService.get(pluginId)
    return state
      ? this.buildTableRange(
          pluginId,
          state.sheetId,
          state.startCell,
          state.rowCount,
          state.columnCount
        )
      : undefined
  }

  private buildTableRange(
    pluginId: string,
    sheetId: string,
    startCell: string,
    rowCount: number,
    columnCount: number
  ): TableRange | undefined {
    if (rowCount <= 0 || columnCount <= 0) {
      return undefined
    }
    const start = this.parseCell(startCell)
    return {
      pluginId,
      sheetId,
      startRow: start.row,
      endRow: start.row + rowCount - 1,
      startColumn: start.col,
      endColumn: start.col + columnCount - 1
    }
  }

  private getTrackedRanges(unitId: string, sheetId: string): TableRange[] {
    const ranges: TableRange[] = []
    const appendRanges = (
      instances: Array<{ id: string; placement: { sheetId: string; startCell: string } }>,
      getState: (pluginId: string) => any,
      getColumnCount: (state: any) => number
    ) => {
      instances.forEach(instance => {
        const state = getState(instance.id)
        if (state?.sheetId === sheetId && state.rowCount > 0 && getColumnCount(state) > 0) {
          const start = this.parseCell(state.startCell)
          ranges.push({
            pluginId: instance.id,
            sheetId,
            startRow: start.row,
            endRow: start.row + state.rowCount - 1,
            startColumn: start.col,
            endColumn: start.col + getColumnCount(state) - 1
          })
        }

        if (instance.placement.sheetId !== sheetId) {
          return
        }
        if (state?.sheetId === sheetId && state.startCell === instance.placement.startCell) {
          return
        }
        const placement = this.parseCell(instance.placement.startCell)
        ranges.push({
          pluginId: instance.id,
          sheetId,
          startRow: placement.row,
          endRow: placement.row,
          startColumn: placement.col,
          endColumn: placement.col
        })
      })
    }

    appendRanges(
      this.detailInstanceService.get(unitId),
      pluginId => this.detailDisplayStateService.get(pluginId),
      state => state.colCount
    )
    appendRanges(
      this.pivotInstanceService.get(unitId),
      pluginId => this.pivotDisplayStateService.get(pluginId),
      state => state.columnCount
    )
    return ranges
  }

  private getInstanceAnchors(
    unitId: string,
    sheetId: string,
    excludedPluginId: string
  ): TableRange[] {
    const instances = [
      ...this.detailInstanceService.get(unitId),
      ...this.pivotInstanceService.get(unitId)
    ]

    return instances.flatMap(instance => {
      if (instance.id === excludedPluginId || instance.placement.sheetId !== sheetId) {
        return []
      }
      const start = this.parseCell(instance.placement.startCell)
      return [{
        pluginId: instance.id,
        sheetId,
        startRow: start.row,
        endRow: start.row,
        startColumn: start.col,
        endColumn: start.col
      }]
    })
  }

  private hasCellContent(cell: ICellData): boolean {
    return !this.isEmptyValue(cell.v) || !!cell.f || !!cell.p || !!cell.custom
  }

  private isEmptyValue(value: unknown): boolean {
    return value === null || value === undefined || value === ''
  }

  private containsCell(range: IRange, row: number, column: number): boolean {
    return row >= range.startRow && row <= range.endRow &&
      column >= range.startColumn && column <= range.endColumn
  }

  private containsRange(container: IRange, range: IRange): boolean {
    return range.startRow >= container.startRow && range.endRow <= container.endRow &&
      range.startColumn >= container.startColumn && range.endColumn <= container.endColumn
  }

  private intersects(left: IRange, right: IRange): boolean {
    return left.startRow <= right.endRow && left.endRow >= right.startRow &&
      left.startColumn <= right.endColumn && left.endColumn >= right.startColumn
  }

  private parseCell(cellAddress: string): { row: number; col: number } {
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

  private toCellAddress(row: number, column: number): string {
    let columnName = ''
    let current = column
    do {
      columnName = String.fromCharCode(65 + (current % 26)) + columnName
      current = Math.floor(current / 26) - 1
    } while (current >= 0)
    return `${columnName}${row + 1}`
  }
}
