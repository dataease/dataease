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
import { getCSSVariable } from '@/utils/color'

const LOADING_FILL = 'rgba(31, 35, 41, 0.08)'
const LOADING_Z_INDEX = 10001

export interface PluginRenderLoadingRange {
  unitId: string
  sheetId: string
  pluginId: string
  startRow: number
  startColumn: number
  rowCount: number
  columnCount: number
}

interface PluginRenderLoadingState extends PluginRenderLoadingRange {
  count: number
}

export class PluginRenderLoadingService extends Disposable {
  private readonly _states = new Map<string, PluginRenderLoadingState>()
  private readonly _visibleShapeIds = new Map<string, string>()

  constructor(
    @Inject(IUniverInstanceService)
    private readonly _univerInstanceService: IUniverInstanceService,
    @Inject(IMarkSelectionService)
    private readonly _markSelectionService: IMarkSelectionService,
    @Inject(ICommandService)
    private readonly _commandService: ICommandService
  ) {
    super()

    this.disposeWithMe(
      this._univerInstanceService
        .getCurrentTypeOfUnit$(UniverInstanceType.UNIVER_SHEET)
        .subscribe(() => {
          this._reconcileVisibleShapes()
        })
    )
    this.disposeWithMe(
      this._commandService.onCommandExecuted(command => {
        if (
          command.id !== SetWorksheetActiveOperation.id &&
          command.id !== SetCellEditVisibleOperation.id
        ) {
          return
        }
        queueMicrotask(() => this._reconcileVisibleShapes())
      })
    )
    this.disposeWithMe({
      dispose: () => {
        this._clearVisibleShapes()
        this._states.clear()
      }
    })
  }

  begin(range: PluginRenderLoadingRange): { dispose: () => void } {
    const normalizedRange = this._normalizeRange(range)
    const key = this._getKey(normalizedRange)
    const current = this._states.get(key)
    const rangeChanged = current ? !this._isSameRange(current, normalizedRange) : false

    this._states.set(key, {
      ...normalizedRange,
      count: (current?.count || 0) + 1
    })

    if (rangeChanged) {
      this._removeVisibleShape(key)
    }
    this._reconcileVisibleShapes()

    let disposed = false
    return {
      dispose: () => {
        if (disposed) {
          return
        }
        disposed = true
        this._end(key)
      }
    }
  }

  clear(unitId: string, sheetId: string, pluginId: string): void {
    const key = this._getKey({ unitId, sheetId, pluginId })
    this._states.delete(key)
    this._removeVisibleShape(key)
  }

  private _end(key: string): void {
    const current = this._states.get(key)
    if (!current) {
      return
    }

    if (current.count > 1) {
      this._states.set(key, {
        ...current,
        count: current.count - 1
      })
      return
    }

    this._states.delete(key)
    this._removeVisibleShape(key)
  }

  private _reconcileVisibleShapes(): void {
    const workbook = this._univerInstanceService.getCurrentUnitOfType(
      UniverInstanceType.UNIVER_SHEET
    )
    const unitId = workbook?.getUnitId?.()
    const sheetId = workbook?.getActiveSheet?.()?.getSheetId?.()

    const shapeMap = this._markSelectionService.getShapeMap()
    for (const [key, shapeId] of Array.from(this._visibleShapeIds.entries())) {
      const state = this._states.get(key)
      if (
        !shapeMap.has(shapeId) ||
        !state ||
        state.unitId !== unitId ||
        state.sheetId !== sheetId
      ) {
        this._removeVisibleShape(key)
      }
    }

    if (!unitId || !sheetId) {
      return
    }

    for (const [key, state] of this._states) {
      if (
        state.unitId !== unitId ||
        state.sheetId !== sheetId ||
        this._visibleShapeIds.has(key)
      ) {
        continue
      }

      const shapeId = this._markSelectionService.addShape(
        {
          range: {
            startRow: state.startRow,
            endRow: state.startRow + state.rowCount - 1,
            startColumn: state.startColumn,
            endColumn: state.startColumn + state.columnCount - 1
          },
          primary: null,
          style: {
            fill: LOADING_FILL,
            stroke: getCSSVariable().trim() || '#3370FF',
            strokeWidth: 2,
            strokeDash: 6,
            isAnimationDash: true,
            widgets: {}
          }
        } satisfies ISelectionWithStyle,
        [],
        LOADING_Z_INDEX
      )

      if (shapeId) {
        this._visibleShapeIds.set(key, shapeId)
      }
    }
  }

  private _removeVisibleShape(key: string): void {
    const shapeId = this._visibleShapeIds.get(key)
    if (shapeId) {
      this._markSelectionService.removeShape(shapeId)
    }
    this._visibleShapeIds.delete(key)
  }

  private _clearVisibleShapes(): void {
    for (const key of Array.from(this._visibleShapeIds.keys())) {
      this._removeVisibleShape(key)
    }
  }

  private _normalizeRange(range: PluginRenderLoadingRange): PluginRenderLoadingRange {
    return {
      ...range,
      rowCount: Math.max(1, Math.floor(range.rowCount)),
      columnCount: Math.max(1, Math.floor(range.columnCount))
    }
  }

  private _getKey(range: Pick<PluginRenderLoadingRange, 'unitId' | 'sheetId' | 'pluginId'>): string {
    return [range.unitId, range.sheetId, range.pluginId].join('|')
  }

  private _isSameRange(
    current: PluginRenderLoadingState,
    next: PluginRenderLoadingRange
  ): boolean {
    return current.startRow === next.startRow &&
      current.startColumn === next.startColumn &&
      current.rowCount === next.rowCount &&
      current.columnCount === next.columnCount
  }
}
