import { Disposable, Inject } from '@univerjs/core'
import { PluginRenderStatusService } from './plugin-render-status.service'

export type TableType = 'detail' | 'pivot'

export interface TableOccupiedRange {
  tableType: TableType
  pluginId: string
  sheetId: string
  startRow: number
  endRow: number
  startColumn: number
  endColumn: number
}

export interface TableRangeProvider {
  getRanges: () => TableOccupiedRange[]
}

export type TableRangeCandidate = Omit<TableOccupiedRange, 'tableType' | 'pluginId'> &
  Partial<Pick<TableOccupiedRange, 'tableType' | 'pluginId'>>

export class TableRangeConflictService extends Disposable {
  private readonly providers = new Map<string, TableRangeProvider>()

  constructor(
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService
  ) {
    super()
  }

  registerProvider(providerId: string, provider: TableRangeProvider): { dispose: () => void } {
    this.providers.set(providerId, provider)

    let disposed = false
    return {
      dispose: () => {
        if (disposed) {
          return
        }
        disposed = true
        if (this.providers.get(providerId) === provider) {
          this.providers.delete(providerId)
        }
      }
    }
  }

  findConflict(target: TableRangeCandidate): TableOccupiedRange | undefined {
    // 异常或空数据实例以 1x1 占位符保留编辑入口，也必须视为已占用区域。
    const placeholderConflict = this.findConflictInRanges(
      target,
      this.getPlaceholderRanges()
    )
    if (placeholderConflict) {
      return placeholderConflict
    }

    for (const provider of this.providers.values()) {
      const conflict = this.findConflictInRanges(target, provider.getRanges())
      if (conflict) {
        return conflict
      }
    }
    return undefined
  }

  override dispose(): void {
    this.providers.clear()
    super.dispose()
  }

  private isOverlap(a: TableRangeCandidate, b: TableOccupiedRange): boolean {
    if (a.sheetId !== b.sheetId) {
      return false
    }

    return !(
      a.endRow < b.startRow ||
      a.startRow > b.endRow ||
      a.endColumn < b.startColumn ||
      a.startColumn > b.endColumn
    )
  }

  private findConflictInRanges(
    target: TableRangeCandidate,
    ranges: TableOccupiedRange[]
  ): TableOccupiedRange | undefined {
    for (const range of ranges) {
      if (target.pluginId && range.pluginId === target.pluginId) {
        continue
      }
      if (this.isOverlap(target, range)) {
        return range
      }
    }
    return undefined
  }

  private getPlaceholderRanges(): TableOccupiedRange[] {
    const ranges: TableOccupiedRange[] = []
    for (const status of this.pluginRenderStatusService.list()) {
      const isPlaceholder = status.status === 'error' || status.status === 'empty'
      if (!isPlaceholder || !status.sheetId || !status.startCell) {
        continue
      }

      const start = this.parseCell(status.startCell)
      ranges.push({
        tableType: status.type,
        pluginId: status.pluginId,
        sheetId: status.sheetId,
        startRow: start.row,
        endRow: start.row,
        startColumn: start.col,
        endColumn: start.col
      })
    }
    return ranges
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
}
