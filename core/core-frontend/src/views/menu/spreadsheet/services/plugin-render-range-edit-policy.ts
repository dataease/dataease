import { isEqual } from 'lodash-es'

export const DATAEASE_TABLE_STYLE_INTERCEPTOR_PRIORITY = 20

type CellDataLike = Record<string, unknown>

export type RawCellReader = (row: number, column: number) => unknown

const PRESENTATION_FIELDS = new Set(['s', 't'])

const normalizeNullable = (value: unknown) => value == null ? undefined : value

export const isPresentationOnlyCellValueMutation = (
  cellValue: Record<number, Record<number, CellDataLike>> | undefined,
  getRawCell: RawCellReader
): boolean => {
  if (!cellValue) {
    return false
  }

  let hasCellPatch = false
  for (const [rowKey, rowData] of Object.entries(cellValue)) {
    const row = Number(rowKey)
    if (!Number.isFinite(row) || !rowData) {
      return false
    }

    for (const [columnKey, cellPatch] of Object.entries(rowData)) {
      const column = Number(columnKey)
      if (!Number.isFinite(column) || !cellPatch || typeof cellPatch !== 'object') {
        return false
      }

      hasCellPatch = true
      const rawCellValue = getRawCell(row, column)
      const rawCell = rawCellValue && typeof rawCellValue === 'object'
        ? rawCellValue as CellDataLike
        : {}
      for (const [field, patchValue] of Object.entries(cellPatch)) {
        if (PRESENTATION_FIELDS.has(field)) {
          continue
        }

        if (!isEqual(normalizeNullable(patchValue), normalizeNullable(rawCell[field]))) {
          return false
        }
      }
    }
  }

  return hasCellPatch
}
