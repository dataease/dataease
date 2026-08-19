import type { IBorderData, IBorderStyleData, IStyleData } from '@univerjs/core'
import type { TableBorderConfig } from './border-config'
import { normalizeTableBorderConfig } from './border-config'

export interface TableBorderCellContext {
  rowIndex: number
  columnIndex: number
  rowCount: number
  columnCount: number
}

const createBorderLine = (config: TableBorderConfig): IBorderStyleData => ({
  s: config.style,
  cl: { rgb: config.color }
})

/**
 * 将区域级的六种边框开关转换为单个单元格的四条边。
 * 内边框同时写入相邻单元格的两侧，与 Univer 原生边框命令保持一致。
 */
export const resolveTableBorderData = (
  value: Partial<TableBorderConfig> | null | undefined,
  context: TableBorderCellContext
): IBorderData => {
  const config = normalizeTableBorderConfig(value)
  const lastRow = Math.max(context.rowCount - 1, 0)
  const lastColumn = Math.max(context.columnCount - 1, 0)

  const top =
    (config.top && context.rowIndex === 0) ||
    (config.horizontal && context.rowIndex > 0)
  const right =
    (config.right && context.columnIndex === lastColumn) ||
    (config.vertical && context.columnIndex < lastColumn)
  const bottom =
    (config.bottom && context.rowIndex === lastRow) ||
    (config.horizontal && context.rowIndex < lastRow)
  const left =
    (config.left && context.columnIndex === 0) ||
    (config.vertical && context.columnIndex > 0)

  return {
    t: top ? createBorderLine(config) : null,
    r: right ? createBorderLine(config) : null,
    b: bottom ? createBorderLine(config) : null,
    l: left ? createBorderLine(config) : null
  }
}

/**
 * 旧快照没有 border 字段时不接管边框；显式配置后才覆盖单元格四条边。
 */
export const applyTableBorderStyle = (
  style: Partial<IStyleData>,
  value: Partial<TableBorderConfig> | null | undefined,
  context: TableBorderCellContext
): Partial<IStyleData> => value
  ? { ...style, bd: resolveTableBorderData(value, context) }
  : style
