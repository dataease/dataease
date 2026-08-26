import type { ICellData, IRange, IStyleData } from '@univerjs/core'
import { covertCellValues } from '@univerjs/core'
import {
  SetRangeValuesMutation,
  SetWorksheetColumnCountMutation,
  SetWorksheetRowCountMutation
} from '@univerjs/sheets'

interface SilentWorksheetTarget {
  unitId: string
  sheetId: string
}

interface SilentWorksheetValueTarget extends SilentWorksheetTarget {
  range: IRange
  values: any[][]
}

interface SilentWorksheetRangeTarget extends SilentWorksheetTarget {
  ranges: IRange[]
}

interface SilentWorksheetStyleTarget extends SilentWorksheetRangeTarget {
  style: IStyleData
}

interface SilentWorksheetSizeTarget extends SilentWorksheetTarget {
  count: number
}

const buildStyleCellValues = (
  ranges: IRange[],
  style: IStyleData | null
): Record<number, Record<number, ICellData>> => {
  const cellValue: Record<number, Record<number, ICellData>> = {}

  ranges.forEach(range => {
    for (let row = range.startRow; row <= range.endRow; row++) {
      cellValue[row] ||= {}
      for (let column = range.startColumn; column <= range.endColumn; column++) {
        cellValue[row][column] = { s: style }
      }
    }
  })

  return cellValue
}

export async function setWorksheetValuesSilently(
  univerApi: any,
  target: SilentWorksheetValueTarget
): Promise<void> {
  if (!target.values.length || !target.values[0]?.length) {
    return
  }

  // 查询结果是可重建的派生数据，直接执行 Mutation，避免后台刷新覆盖用户操作历史。
  const cellValue = covertCellValues(target.values, target.range)
  await univerApi.executeCommand?.(SetRangeValuesMutation.id, {
    unitId: target.unitId,
    subUnitId: target.sheetId,
    cellValue
  })
}

export async function setWorksheetStylesSilently(
  univerApi: any,
  target: SilentWorksheetStyleTarget
): Promise<void> {
  if (!target.ranges.length) {
    return
  }

  const cellValue = buildStyleCellValues(target.ranges, target.style)
  await univerApi.executeCommand?.(SetRangeValuesMutation.id, {
    unitId: target.unitId,
    subUnitId: target.sheetId,
    cellValue
  })
}

export async function clearWorksheetFormatsSilently(
  univerApi: any,
  target: SilentWorksheetRangeTarget
): Promise<void> {
  if (!target.ranges.length) {
    return
  }

  // 插件区域属于查询结果，格式清理直接执行 Mutation，避免污染用户的 undo/redo 栈。
  const cellValue = buildStyleCellValues(target.ranges, null)
  await univerApi.executeCommand?.(SetRangeValuesMutation.id, {
    unitId: target.unitId,
    subUnitId: target.sheetId,
    cellValue
  })
}

export async function setWorksheetRowCountSilently(
  univerApi: any,
  target: SilentWorksheetSizeTarget
): Promise<void> {
  await univerApi.executeCommand?.(SetWorksheetRowCountMutation.id, {
    unitId: target.unitId,
    subUnitId: target.sheetId,
    rowCount: target.count
  })
}

export async function setWorksheetColumnCountSilently(
  univerApi: any,
  target: SilentWorksheetSizeTarget
): Promise<void> {
  await univerApi.executeCommand?.(SetWorksheetColumnCountMutation.id, {
    unitId: target.unitId,
    subUnitId: target.sheetId,
    columnCount: target.count
  })
}
