import { Disposable } from '@univerjs/core'

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
    for (const provider of this.providers.values()) {
      const ranges = provider.getRanges()
      for (const range of ranges) {
        if (
          target.pluginId &&
          range.pluginId === target.pluginId
        ) {
          continue
        }
        if (this.isOverlap(target, range)) {
          return range
        }
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
}
