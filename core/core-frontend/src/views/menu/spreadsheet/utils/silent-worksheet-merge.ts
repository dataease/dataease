import type { IRange } from '@univerjs/core'
import {
  AddWorksheetMergeMutation,
  RemoveWorksheetMergeMutation,
  SetRangeValuesMutation
} from '@univerjs/sheets'

interface SilentMergeTarget {
  unitId: string
  sheetId: string
  ranges: IRange[]
}

const buildMergedCellClearValues = (ranges: IRange[]) => {
  const cellValue: Record<number, Record<number, Record<string, null>>> = {}

  ranges.forEach(range => {
    for (let row = range.startRow; row <= range.endRow; row++) {
      for (let column = range.startColumn; column <= range.endColumn; column++) {
        if (row === range.startRow && column === range.startColumn) {
          continue
        }

        cellValue[row] ||= {}
        cellValue[row][column] = {
          v: null,
          t: null,
          f: null,
          si: null,
          p: null
        }
      }
    }
  })

  return cellValue
}

export async function removeWorksheetMergesSilently(
  univerApi: any,
  target: SilentMergeTarget
): Promise<void> {
  if (!target.ranges.length) {
    return
  }

  await univerApi.executeCommand?.(RemoveWorksheetMergeMutation.id, {
    unitId: target.unitId,
    subUnitId: target.sheetId,
    ranges: target.ranges
  })
}

export async function addWorksheetMergesSilently(
  univerApi: any,
  target: SilentMergeTarget
): Promise<void> {
  if (!target.ranges.length) {
    return
  }

  // 后台渲染不能使用合并 Command，否则 Univer 会同步目标 Sheet 的选择区并触发跳转。
  // 先移除相交的旧合并，兼容首次恢复时工作簿快照中已经存在合并信息的场景。
  await removeWorksheetMergesSilently(univerApi, target)

  const cellValue = buildMergedCellClearValues(target.ranges)
  if (Object.keys(cellValue).length) {
    await univerApi.executeCommand?.(SetRangeValuesMutation.id, {
      unitId: target.unitId,
      subUnitId: target.sheetId,
      cellValue
    })
  }

  await univerApi.executeCommand?.(AddWorksheetMergeMutation.id, {
    unitId: target.unitId,
    subUnitId: target.sheetId,
    ranges: target.ranges
  })
}
