import type { IStyleData } from '@univerjs/core'
import { isEqual } from 'lodash-es'

export const DATAEASE_TABLE_STYLE_INTERCEPTOR_PRIORITY = 20

type CellDataLike = Record<string, unknown>

export type RawCellReader = (row: number, column: number) => unknown
export type CellPatchFilter = (row: number, column: number) => boolean

/**
 * 表格渲染区域允许用户直接修改的样式字段。
 * 数值格式、换行、旋转等插件或扩展样式不在此范围内，格式刷和清除样式共用该边界。
 */
export const DATAEASE_USER_STYLE_KEYS = [
  'ff',
  'fs',
  'bl',
  'it',
  'st',
  'ul',
  'cl',
  'bg',
  'bd',
  'ht',
  'vt'
] as const satisfies ReadonlyArray<keyof IStyleData>

const DATAEASE_USER_STYLE_KEY_SET = new Set<keyof IStyleData>(DATAEASE_USER_STYLE_KEYS)

export const pickDataEaseUserStyle = (style: Partial<IStyleData>): Partial<IStyleData> => {
  const result: Record<string, unknown> = {}
  Object.entries(style).forEach(([key, value]) => {
    if (DATAEASE_USER_STYLE_KEY_SET.has(key as keyof IStyleData)) {
      result[key] = value
    }
  })
  return result as Partial<IStyleData>
}

export const omitDataEaseUserStyle = (style: Partial<IStyleData>): Partial<IStyleData> => {
  const result: Record<string, unknown> = {}
  Object.entries(style).forEach(([key, value]) => {
    if (!DATAEASE_USER_STYLE_KEY_SET.has(key as keyof IStyleData)) {
      result[key] = value
    }
  })
  return result as Partial<IStyleData>
}

export const replaceDataEaseUserStyle = (
  currentStyle: Partial<IStyleData>,
  userStyle: Partial<IStyleData>
): Partial<IStyleData> => {
  const result = omitDataEaseUserStyle(currentStyle) as Record<string, unknown>
  DATAEASE_USER_STYLE_KEYS.forEach(key => {
    const value = userStyle[key]
    // Univer 按字段合并样式，缺失字段不会清除旧值，因此必须显式写入 null。
    result[key] = value === undefined ? null : value
  })
  return result as Partial<IStyleData>
}

const PRESENTATION_FIELDS = new Set(['s', 't'])

const normalizeNullable = (value: unknown) => value == null ? undefined : value

export const isPresentationOnlyCellValueMutation = (
  cellValue: Record<number, Record<number, CellDataLike>> | undefined,
  getRawCell: RawCellReader,
  shouldCheckCell: CellPatchFilter = () => true
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
      if (!Number.isFinite(column)) {
        return false
      }
      if (!shouldCheckCell(row, column)) {
        continue
      }
      if (!cellPatch || typeof cellPatch !== 'object') {
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
