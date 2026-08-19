import { Subject } from 'rxjs'
import type { PivotLayoutRange } from './pivot-table-layout.service'

export interface PivotTableDisplayState {
  pluginId: string
  sheetId: string
  startCell: string
  rowCount: number
  columnCount: number
  headerRowCount: number
  headerColumnCount: number
  displayScales?: number[][]
  dataRange?: PivotLayoutRange
  merges?: PivotLayoutRange[]
  updatedAt: number
}

export class PivotTableDisplayStateService {
  private readonly _states = new Map<string, PivotTableDisplayState>()
  private readonly _stateChanged = new Subject<void>()
  readonly stateChanged$ = this._stateChanged.asObservable()

  get(pluginId: string): PivotTableDisplayState | undefined {
    return this._states.get(pluginId)
  }

  set(state: PivotTableDisplayState): void {
    this._states.set(state.pluginId, state)
    this._stateChanged.next()
  }

  delete(pluginId: string): void {
    if (this._states.delete(pluginId)) {
      this._stateChanged.next()
    }
  }

  list(): PivotTableDisplayState[] {
    return Array.from(this._states.values())
  }

  clear(): void {
    if (!this._states.size) {
      return
    }
    this._states.clear()
    this._stateChanged.next()
  }
}
