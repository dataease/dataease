export type SlashCellType = 'two' | 'three'
export type SlashCellSource = 'cell' | 'pivot-corner'

export interface SlashCellItem {
  unitId: string
  sheetId: string
  row: number
  col: number
  type: SlashCellType
  source?: SlashCellSource
}

export interface SlashCellRange {
  unitId: string
  sheetId: string
  startRow: number
  startColumn: number
  endRow: number
  endColumn: number
}
