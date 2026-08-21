import { Disposable } from '@univerjs/core'
import { Subject } from 'rxjs'

export type PluginRenderStatusKind =
  | 'draft'
  | 'loading'
  | 'rendered'
  | 'empty'
  | 'error'

export interface PluginRenderMarkerStyle {
  tl?: { color: string; size: number }
}

const ERROR_MARKER_COLOR = '#f54a45'
const EMPTY_MARKER_COLOR = '#faad14'
const MARKER_SIZE = 8

export interface PlaceholderPresentation {
  markers: PluginRenderMarkerStyle
  text: string
  textColor: string
}

/**
 * 依据实例状态生成占位符的完整展示信息（左上角三角 + 单元格内文字 + 文字颜色），
 * 由 CELL_CONTENT 拦截器挂到展示副本上，不写入工作表数据。
 */
export function getPlaceholderPresentation(
  status: PluginRenderStatusKind
): PlaceholderPresentation | undefined {
  switch (status) {
    case 'error':
      return {
        markers: { tl: { color: ERROR_MARKER_COLOR, size: MARKER_SIZE } },
        text: '渲染失败',
        textColor: ERROR_MARKER_COLOR
      }
    case 'empty':
      return {
        markers: { tl: { color: EMPTY_MARKER_COLOR, size: MARKER_SIZE } },
        text: '数据为空',
        textColor: EMPTY_MARKER_COLOR
      }
    default:
      return undefined
  }
}

export interface PluginRenderStatus {
  pluginId: string
  type: 'detail' | 'pivot'
  status: PluginRenderStatusKind
  reason?: string
  unitId?: string
  sheetId?: string
  startCell?: string
  updatedAt: number
}

interface CellPosition {
  row: number
  col: number
}

/**
 * 表格插件实例的渲染状态注册表。
 *
 * 与 displayStateService 不同，这里记录的是「实例级别的渲染结果」：
 * 插入后处于 draft，查询中 loading，成功后 rendered，查询失败 error，
 * 查询成功但渲染区域为空（明细表隐藏表头且 0 条数据）为 empty。
 *
 * 该服务为模块级单例，明细表与汇总表两个插件共享同一份状态，
 * 供占位符 overlay、关闭草稿确认以及点选重开配置面板使用。
 */
export class PluginRenderStatusService extends Disposable {
  private readonly _states = new Map<string, PluginRenderStatus>()
  private readonly _changed = new Subject<void>()
  readonly changed$ = this._changed.asObservable()

  get(pluginId: string): PluginRenderStatus | undefined {
    return this._states.get(pluginId)
  }

  list(): PluginRenderStatus[] {
    return Array.from(this._states.values())
  }

  set(status: PluginRenderStatus): void {
    const previous = this._states.get(status.pluginId)
    this._states.set(status.pluginId, status)
    if (
      !previous ||
      previous.status !== status.status ||
      previous.reason !== status.reason ||
      previous.unitId !== status.unitId ||
      previous.sheetId !== status.sheetId ||
      previous.startCell !== status.startCell
    ) {
      this._changed.next()
    }
  }

  delete(pluginId: string): void {
    if (this._states.delete(pluginId)) {
      this._changed.next()
    }
  }

  /** 清理某个工作簿下的全部状态（工作簿卸载时调用）。 */
  deleteByUnit(unitId: string): void {
    let changed = false
    for (const [pluginId, state] of this._states.entries()) {
      if (state.unitId === unitId) {
        this._states.delete(pluginId)
        changed = true
      }
    }
    if (changed) {
      this._changed.next()
    }
  }

  clear(): void {
    if (!this._states.size) {
      return
    }
    this._states.clear()
    this._changed.next()
  }

  shiftRows(
    unitId: string,
    sheetId: string,
    position: number,
    count: number,
    excludedPluginId?: string
  ): void {
    this.shiftPositions(unitId, sheetId, excludedPluginId, start => {
      return start.row >= position
        ? { row: start.row + count, col: start.col }
        : start
    })
  }

  shiftColumns(
    unitId: string,
    sheetId: string,
    position: number,
    count: number,
    excludedPluginId?: string
  ): void {
    this.shiftPositions(unitId, sheetId, excludedPluginId, start => {
      return start.col >= position
        ? { row: start.row, col: start.col + count }
        : start
    })
  }

  /** 需要展示占位符的状态（渲染失败 / 数据为空）。 */
  isPlaceholder(pluginId: string): boolean {
    const status = this._states.get(pluginId)?.status
    return status === 'error' || status === 'empty'
  }

  /**
   * 关闭配置面板时需要二次确认并清除实例的状态。
   *
   * 仅 draft（插入后从未成功渲染、表格中不可见的残留实例）需要确认清除；
   * error / empty 在表格中有可见占位符，关闭面板只是收起，保留实例供后续重试或删除。
   */
  needsCloseConfirm(pluginId: string): boolean {
    const status = this._states.get(pluginId)?.status
    return status === 'draft'
  }

  /**
   * 按单元格位置查找占位符，供点选重开配置面板。
   * 占位符固定为 1x1 单元格，仅 error / empty 状态可命中。
   * 传入 type 可限定插件类型，避免明细表与汇总表相互命中对方的占位符。
   */
  findByCell(
    unitId: string,
    sheetId: string,
    row: number,
    col: number,
    type?: 'detail' | 'pivot'
  ): PluginRenderStatus | undefined {
    for (const state of this._states.values()) {
      if (
        state.unitId !== unitId ||
        state.sheetId !== sheetId ||
        !state.startCell ||
        (state.status !== 'error' && state.status !== 'empty') ||
        (type && state.type !== type)
      ) {
        continue
      }
      const position = this.parseCellAddress(state.startCell)
      if (position.row === row && position.col === col) {
        return state
      }
    }
    return undefined
  }

  private parseCellAddress(cellAddress: string): CellPosition {
    const match = cellAddress.match(/^([A-Z]+)(\d+)$/i)
    if (!match) {
      throw new Error(`Invalid cell address: ${cellAddress}`)
    }

    const colStr = match[1].toUpperCase()
    let col = 0
    for (let i = 0; i < colStr.length; i++) {
      col = col * 26 + (colStr.charCodeAt(i) - 64)
    }

    return {
      row: parseInt(match[2], 10) - 1,
      col: col - 1
    }
  }

  private shiftPositions(
    unitId: string,
    sheetId: string,
    excludedPluginId: string | undefined,
    resolvePosition: (start: CellPosition) => CellPosition
  ): void {
    let changed = false
    this._states.forEach(state => {
      if (
        state.pluginId === excludedPluginId ||
        state.unitId !== unitId ||
        state.sheetId !== sheetId ||
        !state.startCell
      ) {
        return
      }
      const start = this.parseCellAddress(state.startCell)
      const next = resolvePosition(start)
      if (next.row === start.row && next.col === start.col) {
        return
      }
      state.startCell = this.toCellAddress(next.row, next.col)
      state.updatedAt = Date.now()
      changed = true
    })
    if (changed) {
      this._changed.next()
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

  override dispose(): void {
    // 状态只服务于当前 Univer，实例销毁后不得影响后续预览或新建工作簿。
    this._states.clear()
    this._changed.complete()
    super.dispose()
  }
}
