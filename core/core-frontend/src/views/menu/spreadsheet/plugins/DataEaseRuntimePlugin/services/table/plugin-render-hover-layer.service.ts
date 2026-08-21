import { Disposable, Inject } from '@univerjs/core'
import type { ISelectionWithStyle } from '@univerjs/sheets'
import { IMarkSelectionService } from '@univerjs/sheets-ui'
import type { PluginRenderHoverRange } from './plugin-render-hover.service'

const HOVER_FILL = 'rgba(31, 35, 41, 0.08)'
const HOVER_STROKE = 'rgba(31, 35, 41, 0.16)'

export class PluginRenderHoverLayerService extends Disposable {
  private _shapeId?: string
  private _rangeKey?: string

  constructor(
    @Inject(IMarkSelectionService)
    private readonly _markSelectionService: IMarkSelectionService
  ) {
    super()
  }

  show(range: PluginRenderHoverRange): void {
    const rangeKey = this._getRangeKey(range)
    if (this._rangeKey === rangeKey && this._shapeId) {
      return
    }

    this.clear()
    this._shapeId = this._markSelectionService.addShape({
      range: {
        startRow: range.startRow,
        endRow: range.startRow + range.rowCount - 1,
        startColumn: range.startColumn,
        endColumn: range.startColumn + range.colCount - 1
      },
      primary: null,
      style: {
        fill: HOVER_FILL,
        stroke: HOVER_STROKE,
        strokeWidth: 1,
        widgets: {}
      }
    } satisfies ISelectionWithStyle)
    this._rangeKey = this._shapeId ? rangeKey : undefined
  }

  clear(): void {
    if (this._shapeId) {
      this._markSelectionService.removeShape(this._shapeId)
    }
    this._shapeId = undefined
    this._rangeKey = undefined
  }

  private _getRangeKey(range: PluginRenderHoverRange): string {
    return [
      range.pluginId,
      range.unitId,
      range.sheetId,
      range.startRow,
      range.startColumn,
      range.rowCount,
      range.colCount
    ].join('|')
  }

  override dispose(): void {
    this.clear()
    super.dispose()
  }
}
