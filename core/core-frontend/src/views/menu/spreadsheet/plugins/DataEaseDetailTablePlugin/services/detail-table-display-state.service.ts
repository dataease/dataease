import { Subject } from 'rxjs'
import type { FieldItemData } from '../../../types/plugin'

export interface DetailTableDisplayState {
  pluginId: string
  sheetId: string
  startCell: string
  rowCount: number
  colCount: number
  dataValues?: any[][]
  fields?: Array<FieldItemData | undefined>
  showIndex?: boolean
  indexLabel?: string
  totalEnabled?: boolean
  totalDataSignature?: string
  hideHeader?: boolean
  updatedAt: number
}

export class DetailTableDisplayStateService {
  private readonly _states = new Map<string, DetailTableDisplayState>()
  private readonly _stateChanged = new Subject<void>()
  readonly stateChanged$ = this._stateChanged.asObservable()

  get(pluginId: string): DetailTableDisplayState | undefined {
    return this._states.get(pluginId)
  }

  list(): DetailTableDisplayState[] {
    return Array.from(this._states.values())
  }

  findByLocation(sheetId: string, startCell: string): DetailTableDisplayState | undefined {
    return Array.from(this._states.values()).find(
      state => state.sheetId === sheetId && state.startCell === startCell
    )
  }

  set(state: DetailTableDisplayState): void {
    this._states.set(state.pluginId, state)
    this._stateChanged.next()
  }

  delete(pluginId: string): void {
    if (this._states.delete(pluginId)) {
      this._stateChanged.next()
    }
  }

  clear(): void {
    if (!this._states.size) {
      return
    }
    this._states.clear()
    this._stateChanged.next()
  }
}
