import {
  Disposable,
  ICommandService,
  Inject,
  IUniverInstanceService,
  UniverInstanceType
} from '@univerjs/core'
import { SetWorksheetActiveOperation } from '@univerjs/sheets'
import type { ISelectionWithStyle } from '@univerjs/sheets'
import { IMarkSelectionService, SetCellEditVisibleOperation } from '@univerjs/sheets-ui'

export type TableClipboardLayerMode = 'copy' | 'cut'

export interface TableClipboardLayerRange {
  unitId: string
  sheetId: string
  startRow: number
  startColumn: number
  rowCount: number
  columnCount: number
}

interface TableClipboardLayerState extends TableClipboardLayerRange {
  mode: TableClipboardLayerMode
}

const CLIPBOARD_LAYER_Z_INDEX = 10000
const COPY_STROKE = '#34A853'
const CUT_STROKE = '#3370FF'

export class TableClipboardLayerService extends Disposable {
  private state?: TableClipboardLayerState
  private shapeId?: string

  constructor(
    @Inject(IUniverInstanceService)
    private readonly univerInstanceService: IUniverInstanceService,
    @Inject(IMarkSelectionService)
    private readonly markSelectionService: IMarkSelectionService,
    @Inject(ICommandService)
    private readonly commandService: ICommandService
  ) {
    super()
    this.disposeWithMe(
      this.univerInstanceService
        .getCurrentTypeOfUnit$(UniverInstanceType.UNIVER_SHEET)
        .subscribe(() => this.reconcileVisibleShape())
    )
    this.disposeWithMe(
      this.commandService.onCommandExecuted(command => {
        if (
          command.id !== SetWorksheetActiveOperation.id &&
          command.id !== SetCellEditVisibleOperation.id
        ) {
          return
        }
        queueMicrotask(() => this.reconcileVisibleShape())
      })
    )
    this.disposeWithMe({ dispose: () => this.clear() })
  }

  show(mode: TableClipboardLayerMode, range: TableClipboardLayerRange): void {
    this.state = {
      ...range,
      mode,
      rowCount: Math.max(1, Math.floor(range.rowCount)),
      columnCount: Math.max(1, Math.floor(range.columnCount))
    }
    this.removeVisibleShape()
    this.reconcileVisibleShape()
  }

  clear(): void {
    this.state = undefined
    this.removeVisibleShape()
  }

  private reconcileVisibleShape(): void {
    const workbook = this.univerInstanceService.getCurrentUnitOfType(
      UniverInstanceType.UNIVER_SHEET
    )
    const unitId = workbook?.getUnitId?.()
    const sheetId = workbook?.getActiveSheet?.()?.getSheetId?.()
    const shapeExists = this.shapeId
      ? this.markSelectionService.getShapeMap().has(this.shapeId)
      : false

    if (this.shapeId && (!shapeExists || !this.isActiveRange(unitId, sheetId))) {
      this.removeVisibleShape()
    }
    if (!this.state || this.shapeId || !this.isActiveRange(unitId, sheetId)) {
      return
    }

    const stroke = this.state.mode === 'copy' ? COPY_STROKE : CUT_STROKE
    // 沿用获取数据时的动态虚线选区，仅用颜色区分复制和剪切状态。
    this.shapeId = this.markSelectionService.addShape(
      {
        range: {
          startRow: this.state.startRow,
          endRow: this.state.startRow + this.state.rowCount - 1,
          startColumn: this.state.startColumn,
          endColumn: this.state.startColumn + this.state.columnCount - 1
        },
        primary: null,
        style: {
          fill: 'rgba(0, 0, 0, 0)',
          stroke,
          strokeWidth: 2,
          strokeDash: 6,
          isAnimationDash: true,
          widgets: {}
        }
      } satisfies ISelectionWithStyle,
      [],
      CLIPBOARD_LAYER_Z_INDEX
    )
  }

  private isActiveRange(unitId?: string, sheetId?: string): boolean {
    return !!this.state && this.state.unitId === unitId && this.state.sheetId === sheetId
  }

  private removeVisibleShape(): void {
    if (this.shapeId) {
      this.markSelectionService.removeShape(this.shapeId)
    }
    this.shapeId = undefined
  }
}
