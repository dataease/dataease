import type { CellPosition } from '../types'

/**
 * 解析单元格地址
 * @param cellAddress 单元格地址（如 "A1"）
 * @returns 行和列索引（0基）
 */
export function parseCell(cellAddress: string): CellPosition {
  const match = cellAddress.match(/^([A-Z]+)(\d+)$/i)
  if (!match) {
    throw new Error(`Invalid cell address: ${cellAddress}`)
  }

  const colStr = match[1].toUpperCase()
  const rowStr = match[2]

  // 转换列字母为索引（A=0, B=1, ...）
  let col = 0
  for (let i = 0; i < colStr.length; i++) {
    col = col * 26 + (colStr.charCodeAt(i) - 65)
  }

  // 行号转换为 0 基索引
  const row = parseInt(rowStr, 10) - 1

  return { row, col }
}

/**
 * 将行列索引转换为单元格地址
 * @param row 行索引（0基）
 * @param col 列索引（0基）
 * @returns 单元格地址（如 "A1"）
 */
export function toCellAddress(row: number, col: number): string {
  // 转换列索引为字母
  let colStr = ''
  let tempCol = col
  do {
    colStr = String.fromCharCode(65 + (tempCol % 26)) + colStr
    tempCol = Math.floor(tempCol / 26) - 1
  } while (tempCol >= 0)

  return `${colStr}${row + 1}`
}

/**
 * 生成单元格范围
 * @param startCell 起始单元格
 * @param rowCount 行数
 * @param colCount 列数
 * @returns 单元格范围（如 "A1:E10"）
 */
export function getCellRange(
  startCell: string,
  rowCount: number,
  colCount: number
): string {
  const start = parseCell(startCell)
  const endRow = start.row + rowCount - 1
  const endCol = start.col + colCount - 1
  const endCell = toCellAddress(endRow, endCol)

  return `${startCell}:${endCell}`
}
