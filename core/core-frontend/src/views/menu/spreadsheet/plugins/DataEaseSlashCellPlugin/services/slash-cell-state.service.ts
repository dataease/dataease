import type { SlashCellItem, SlashCellRange, SlashCellSource, SlashCellType } from '../types'

const keyOf = (sheetId: string, row: number, col: number) => `${sheetId}:${row}:${col}`

export class SlashCellStateService {
  private readonly statesByUnitId = new Map<string, Map<string, SlashCellItem>>()

  get(unitId: string, sheetId: string, row: number, col: number): SlashCellItem | undefined {
    return this.statesByUnitId.get(unitId)?.get(keyOf(sheetId, row, col))
  }

  list(unitId: string): SlashCellItem[] {
    return Array.from(this.statesByUnitId.get(unitId)?.values() || [])
  }

  set(unitId: string, items: SlashCellItem[]): void {
    const unitMap = new Map<string, SlashCellItem>()
    items.forEach(item => {
      unitMap.set(keyOf(item.sheetId, item.row, item.col), this.normalizeItem(item, unitId))
    })
    this.statesByUnitId.set(unitId, unitMap)
  }

  setCells(ranges: SlashCellRange[], type: SlashCellType, source: SlashCellSource = 'cell'): void {
    ranges.forEach(range => {
      const unitMap = this.getOrCreateUnitMap(range.unitId)
      for (let row = range.startRow; row <= range.endRow; row++) {
        for (let col = range.startColumn; col <= range.endColumn; col++) {
          const item: SlashCellItem = {
            unitId: range.unitId,
            sheetId: range.sheetId,
            row,
            col,
            type,
            source
          }
          unitMap.set(keyOf(range.sheetId, row, col), item)
        }
      }
    })
  }

  clearCells(ranges: SlashCellRange[]): void {
    ranges.forEach(range => {
      const unitMap = this.statesByUnitId.get(range.unitId)
      if (!unitMap) {
        return
      }
      for (let row = range.startRow; row <= range.endRow; row++) {
        for (let col = range.startColumn; col <= range.endColumn; col++) {
          unitMap.delete(keyOf(range.sheetId, row, col))
        }
      }
    })
  }

  // 结构命令先基于旧状态生成完整快照，再由 Mutation 写入，确保删除场景可撤销恢复。
  getAfterInsertRows(
    unitId: string,
    sheetId: string,
    position: number,
    count: number
  ): SlashCellItem[] {
    if (count <= 0) {
      return this.list(unitId)
    }
    return this.mapCells(unitId, item => {
      if (item.sheetId !== sheetId || item.row < position) {
        return item
      }
      return { ...item, row: item.row + count }
    })
  }

  getAfterInsertColumns(
    unitId: string,
    sheetId: string,
    position: number,
    count: number
  ): SlashCellItem[] {
    if (count <= 0) {
      return this.list(unitId)
    }
    return this.mapCells(unitId, item => {
      if (item.sheetId !== sheetId || item.col < position) {
        return item
      }
      return { ...item, col: item.col + count }
    })
  }

  getAfterRemoveRows(
    unitId: string,
    sheetId: string,
    startRow: number,
    endRow: number
  ): SlashCellItem[] {
    const count = endRow - startRow + 1
    if (count <= 0) {
      return this.list(unitId)
    }
    return this.mapCells(unitId, item => {
      if (item.sheetId !== sheetId || item.row < startRow) {
        return item
      }
      if (item.row <= endRow) {
        return undefined
      }
      return { ...item, row: item.row - count }
    })
  }

  getAfterRemoveColumns(
    unitId: string,
    sheetId: string,
    startColumn: number,
    endColumn: number
  ): SlashCellItem[] {
    const count = endColumn - startColumn + 1
    if (count <= 0) {
      return this.list(unitId)
    }
    return this.mapCells(unitId, item => {
      if (item.sheetId !== sheetId || item.col < startColumn) {
        return item
      }
      if (item.col <= endColumn) {
        return undefined
      }
      return { ...item, col: item.col - count }
    })
  }

  deleteUnit(unitId: string): void {
    this.statesByUnitId.delete(unitId)
  }

  dispose(): void {
    this.statesByUnitId.clear()
  }

  private getOrCreateUnitMap(unitId: string): Map<string, SlashCellItem> {
    let unitMap = this.statesByUnitId.get(unitId)
    if (!unitMap) {
      unitMap = new Map<string, SlashCellItem>()
      this.statesByUnitId.set(unitId, unitMap)
    }
    return unitMap
  }

  private mapCells(
    unitId: string,
    transform: (item: SlashCellItem) => SlashCellItem | undefined
  ): SlashCellItem[] {
    const unitMap = this.statesByUnitId.get(unitId)
    if (!unitMap?.size) {
      return []
    }

    const items: SlashCellItem[] = []
    unitMap.forEach(item => {
      const nextItem = transform(item)
      if (nextItem) {
        items.push(nextItem)
      }
    })
    return items
  }

  private normalizeItem(item: SlashCellItem, fallbackUnitId: string): SlashCellItem {
    return {
      unitId: item.unitId || fallbackUnitId,
      sheetId: item.sheetId,
      row: Number(item.row),
      col: Number(item.col),
      type: item.type === 'three' ? 'three' : 'two',
      source: item.source || 'cell'
    }
  }
}
