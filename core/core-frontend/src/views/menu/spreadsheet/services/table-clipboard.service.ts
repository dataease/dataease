import { Disposable, Inject, Injector } from '@univerjs/core'
import { FUniver } from '@univerjs/core/facade'
import { ElMessage } from 'element-plus-secondary'
import { cloneDeep } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import type { PluginActionToolbarPayload } from '../types/editor'
import { getCurrentRangeSelection } from '../utils/current-range-selection'
import { SPREADSHEET_EVENTS } from '../utils/events'
import type { DetailTableConfig } from '../plugins/DataEaseDetailTablePlugin/types'
import { DetailTableDisplayStateService } from '../plugins/DataEaseDetailTablePlugin/services/detail-table-display-state.service'
import { DetailTableInstanceService } from '../plugins/DataEaseDetailTablePlugin/services/detail-table-instance.service'
import { TableFillService } from '../plugins/DataEaseDetailTablePlugin/services/table-fill.service'
import type { PivotTableConfig } from '../plugins/DataEasePivotTablePlugin/types'
import { PivotTableDisplayStateService } from '../plugins/DataEasePivotTablePlugin/services/pivot-table-display-state.service'
import { PivotTableFillService } from '../plugins/DataEasePivotTablePlugin/services/pivot-table-fill.service'
import { PivotTableInstanceService } from '../plugins/DataEasePivotTablePlugin/services/pivot-table-instance.service'
import { TableClipboardLayerService } from './table-clipboard-layer.service'
import { PluginRenderStatusService } from '../plugins/DataEaseRuntimePlugin/services/table'

type TableClipboardMode = 'copy' | 'cut'
type TableConfig = DetailTableConfig | PivotTableConfig

interface TableClipboardItem {
  mode: TableClipboardMode
  type: PluginActionToolbarPayload['type']
  sourceUnitId: string
  sourceSheetId: string
  sourceStartRow: number
  sourceStartColumn: number
  sourceRowCount: number
  sourceColumnCount: number
  config: TableConfig
}

const { emitter } = useEmitt()

export class TableClipboardService extends Disposable {
  private clipboard?: TableClipboardItem
  private pasting = false

  constructor(
    @Inject(Injector) private readonly injector: Injector,
    @Inject(DetailTableInstanceService)
    private readonly detailTableInstanceService: DetailTableInstanceService,
    @Inject(DetailTableDisplayStateService)
    private readonly detailTableDisplayStateService: DetailTableDisplayStateService,
    @Inject(TableFillService)
    private readonly tableFillService: TableFillService,
    @Inject(PivotTableInstanceService)
    private readonly pivotTableInstanceService: PivotTableInstanceService,
    @Inject(PivotTableDisplayStateService)
    private readonly pivotTableDisplayStateService: PivotTableDisplayStateService,
    @Inject(PivotTableFillService)
    private readonly pivotTableFillService: PivotTableFillService,
    @Inject(TableClipboardLayerService)
    private readonly tableClipboardLayerService: TableClipboardLayerService,
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService
  ) {
    super()
    this.disposeWithMe(
      this.detailTableDisplayStateService.stateChanged$.subscribe(() => {
        this.syncLayerFromDisplayState('detail')
      })
    )
    this.disposeWithMe(
      this.pivotTableDisplayStateService.stateChanged$.subscribe(() => {
        this.syncLayerFromDisplayState('pivot')
      })
    )
    this.disposeWithMe({ dispose: () => this.clear() })
  }

  set(mode: TableClipboardMode, payload: PluginActionToolbarPayload): void {
    const start = this.parseCell(payload.startCell)
    this.clipboard = {
      mode,
      type: payload.type,
      sourceUnitId: payload.unitId,
      sourceSheetId: payload.sheetId,
      sourceStartRow: start.row,
      sourceStartColumn: start.column,
      sourceRowCount: payload.rowCount,
      sourceColumnCount: payload.columnCount,
      config: cloneDeep(payload.config) as TableConfig
    }
    this.tableClipboardLayerService.show(mode, {
      unitId: payload.unitId,
      sheetId: payload.sheetId,
      startRow: start.row,
      startColumn: start.column,
      rowCount: payload.rowCount,
      columnCount: payload.columnCount
    })
  }

  hasClipboard(): boolean {
    return !!this.clipboard
  }

  clear(): void {
    this.clipboard = undefined
    this.tableClipboardLayerService.clear()
  }

  async paste(): Promise<boolean> {
    if (this.pasting) {
      ElMessage.warning('表格正在粘贴，请稍候')
      return false
    }

    const clipboard = this.clipboard
    if (!clipboard) {
      ElMessage.warning('请先复制或剪切表格')
      return false
    }

    const univerApi = FUniver.newAPI(this.injector)
    const workbook = univerApi.getActiveWorkbook()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (!workbook || !unitId || unitId !== clipboard.sourceUnitId) {
      ElMessage.warning('暂不支持跨工作簿粘贴表格')
      return false
    }

    const selection = getCurrentRangeSelection(this.injector)
    if (!selection) {
      ElMessage.warning('请选择表格粘贴位置')
      return false
    }

    // 剪切移动不能从原渲染区域内部起步，避免把表格移动到自身内容上。
    const cutStartInsideSource = this.isCutStartInsideSource(
      clipboard,
      selection.sheetId,
      selection.startRowNumber,
      selection.startColumnNumber
    )
    if (cutStartInsideSource) {
      ElMessage.warning('剪切表格的粘贴起始位置不能位于原表格区域内')
      return false
    }

    const targetConfig = cloneDeep(clipboard.config)
    if (clipboard.mode === 'copy') {
      targetConfig.id = this.createPluginId()
    }
    targetConfig.placement = {
      ...targetConfig.placement,
      sheetId: selection.sheetId,
      sheetName: selection.sheetName,
      startCell: this.toCellAddress(selection.startRowNumber, selection.startColumnNumber)
    }

    this.pasting = true
    try {
      const pasted = clipboard.type === 'detail'
        ? await this.pasteDetailTable(univerApi, unitId, targetConfig as DetailTableConfig)
        : await this.pastePivotTable(univerApi, unitId, targetConfig as PivotTableConfig)
      if (!pasted) {
        return false
      }

      if (clipboard.mode === 'cut') {
        this.clear()
        emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
      }
      ElMessage.success(clipboard.mode === 'copy' ? '表格复制成功' : '表格移动成功')
      return true
    } catch (error) {
      ElMessage.error(`粘贴表格失败：${error instanceof Error ? error.message : '未知错误'}`)
      return false
    } finally {
      this.pasting = false
    }
  }

  private async pasteDetailTable(
    univerApi: FUniver,
    unitId: string,
    config: DetailTableConfig
  ): Promise<boolean> {
    // 粘贴前先注册实例并标记为草稿：渲染失败时占位符也能据此重新打开配置面板，
    // 与「插入明细表」的注册时机保持一致。
    this.detailTableInstanceService.addOrUpdate(unitId, config)
    this.pluginRenderStatusService.set({
      pluginId: config.id,
      type: 'detail',
      status: 'draft',
      unitId,
      sheetId: config.placement.sheetId,
      startCell: config.placement.startCell,
      updatedAt: Date.now()
    })

    await this.tableFillService.fillTable(univerApi, config)
    const state = this.detailTableDisplayStateService.get(config.id)
    const pasted = state?.sheetId === config.placement.sheetId &&
      state.startCell === config.placement.startCell
    return pasted
  }

  private async pastePivotTable(
    univerApi: FUniver,
    unitId: string,
    config: PivotTableConfig
  ): Promise<boolean> {
    // 粘贴前先注册实例并标记为草稿：渲染失败时占位符也能据此重新打开配置面板，
    // 与「插入透视表」的注册时机保持一致。
    this.pivotTableInstanceService.addOrUpdate(unitId, config)
    this.pluginRenderStatusService.set({
      pluginId: config.id,
      type: 'pivot',
      status: 'draft',
      unitId,
      sheetId: config.placement.sheetId,
      startCell: config.placement.startCell,
      updatedAt: Date.now()
    })

    const filled = await this.pivotTableFillService.fillByConfig(univerApi, config)
    if (!filled) {
      return false
    }

    const state = this.pivotTableDisplayStateService.get(config.id)
    const pasted = state?.sheetId === config.placement.sheetId &&
      state.startCell === config.placement.startCell
    return pasted
  }

  private isCutStartInsideSource(
    clipboard: TableClipboardItem,
    sheetId: string,
    row: number,
    column: number
  ): boolean {
    if (clipboard.mode !== 'cut' || clipboard.sourceSheetId !== sheetId) {
      return false
    }

    const sourceEndRow = clipboard.sourceStartRow + clipboard.sourceRowCount - 1
    const sourceEndColumn = clipboard.sourceStartColumn + clipboard.sourceColumnCount - 1
    return row >= clipboard.sourceStartRow &&
      row <= sourceEndRow &&
      column >= clipboard.sourceStartColumn &&
      column <= sourceEndColumn
  }

  private syncLayerFromDisplayState(type: TableClipboardItem['type']): void {
    const clipboard = this.clipboard
    if (!clipboard || clipboard.type !== type) {
      return
    }

    const state = type === 'detail'
      ? this.detailTableDisplayStateService.get(clipboard.config.id)
      : this.pivotTableDisplayStateService.get(clipboard.config.id)
    if (!state) {
      // 源表格已被删除后不再保留失效的剪贴板实例和范围标识。
      this.clear()
      return
    }

    const start = this.parseCell(state.startCell)
    const rowCount = state.rowCount
    const columnCount = 'colCount' in state ? state.colCount : state.columnCount
    const rangeChanged = clipboard.sourceSheetId !== state.sheetId ||
      clipboard.sourceStartRow !== start.row ||
      clipboard.sourceStartColumn !== start.column ||
      clipboard.sourceRowCount !== rowCount ||
      clipboard.sourceColumnCount !== columnCount
    if (!rangeChanged) {
      return
    }

    clipboard.sourceSheetId = state.sheetId
    clipboard.sourceStartRow = start.row
    clipboard.sourceStartColumn = start.column
    clipboard.sourceRowCount = rowCount
    clipboard.sourceColumnCount = columnCount
    this.tableClipboardLayerService.show(clipboard.mode, {
      unitId: clipboard.sourceUnitId,
      sheetId: state.sheetId,
      startRow: start.row,
      startColumn: start.column,
      rowCount,
      columnCount
    })
  }

  private parseCell(cellAddress: string): { row: number; column: number } {
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
      column: column - 1
    }
  }

  private toCellAddress(row: number, column: number): string {
    let columnName = ''
    let current = column
    do {
      columnName = String.fromCharCode(65 + current % 26) + columnName
      current = Math.floor(current / 26) - 1
    } while (current >= 0)
    return `${columnName}${row + 1}`
  }

  private createPluginId(): string {
    return `plugin_${Date.now()}_${Math.random().toString(36).slice(2, 11)}`
  }
}
