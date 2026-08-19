import {
  IUniverInstanceService,
  type IAccessor,
  type IRange,
  UniverInstanceType
} from '@univerjs/core'
import {
  RangeProtectionPermissionEditPoint,
  SheetsSelectionsService,
  WorkbookEditablePermission,
  WorksheetEditPermission,
  WorksheetSortPermission
} from '@univerjs/sheets'
import { getCurrentRangeDisable$ } from '@univerjs/sheets-ui'
import { getMenuHiddenObservable, MenuItemType } from '@univerjs/ui'
import { combineLatest, merge } from 'rxjs'
import { distinctUntilChanged, map, startWith } from 'rxjs/operators'
import { DetailTableDisplayStateService } from '../../DataEaseDetailTablePlugin/services/detail-table-display-state.service'
import { PivotTableDisplayStateService } from '../../DataEasePivotTablePlugin/services/pivot-table-display-state.service'
import { DATAEASE_SORT_MENU_ID } from '../config/ribbon-config'

export function DataEaseSortMenuFactory(accessor: IAccessor) {
  const selectionService = accessor.get(SheetsSelectionsService)
  const univerInstanceService = accessor.get(IUniverInstanceService)
  const detailTableDisplayStateService = accessor.get(DetailTableDisplayStateService)
  const pivotTableDisplayStateService = accessor.get(PivotTableDisplayStateService)

  const pluginRangeDisabled$ = merge(
    selectionService.selectionChanged$,
    detailTableDisplayStateService.stateChanged$,
    pivotTableDisplayStateService.stateChanged$
  ).pipe(
    startWith(null),
    map(() => {
      const workbook = univerInstanceService.getCurrentUnitOfType<any>(
        UniverInstanceType.UNIVER_SHEET
      )
      const sheetId = workbook?.getActiveSheet?.()?.getSheetId?.()
      if (!sheetId) {
        return false
      }

      const selections = selectionService.getCurrentSelections()
      const protectedRanges = [
        ...detailTableDisplayStateService.list().map(state => ({
          sheetId: state.sheetId,
          range: toRange(state.startCell, state.rowCount, state.colCount)
        })),
        ...pivotTableDisplayStateService.list().map(state => ({
          sheetId: state.sheetId,
          range: toRange(state.startCell, state.rowCount, state.columnCount)
        }))
      ]

      return selections.some(selection =>
        protectedRanges.some(
          protectedRange =>
            protectedRange.sheetId === sheetId &&
            intersects(selection.range, protectedRange.range)
        )
      )
    }),
    distinctUntilChanged()
  )

  const permissionDisabled$ = getCurrentRangeDisable$(accessor, {
    workbookTypes: [WorkbookEditablePermission],
    worksheetTypes: [WorksheetSortPermission, WorksheetEditPermission],
    rangeTypes: [RangeProtectionPermissionEditPoint]
  })

  return {
    id: DATAEASE_SORT_MENU_ID,
    type: MenuItemType.SUBITEMS,
    icon: 'AscendingIcon',
    tooltip: 'sheets-sort-ui.general.sort',
    hidden$: getMenuHiddenObservable(accessor, UniverInstanceType.UNIVER_SHEET),
    disabled$: combineLatest([permissionDisabled$, pluginRangeDisabled$]).pipe(
      map(([permissionDisabled, pluginRangeDisabled]) =>
        permissionDisabled || pluginRangeDisabled
      ),
      distinctUntilChanged()
    )
  }
}

function toRange(startCell: string, rowCount: number, columnCount: number): IRange {
  const match = startCell.match(/^([A-Z]+)(\d+)$/i)
  if (!match) {
    throw new Error(`Invalid cell address: ${startCell}`)
  }

  let startColumn = 0
  for (const char of match[1].toUpperCase()) {
    startColumn = startColumn * 26 + char.charCodeAt(0) - 64
  }
  startColumn -= 1
  const startRow = parseInt(match[2], 10) - 1

  return {
    startRow,
    endRow: startRow + rowCount - 1,
    startColumn,
    endColumn: startColumn + columnCount - 1
  }
}

function intersects(range: IRange, target: IRange): boolean {
  return (
    range.startRow <= target.endRow &&
    range.endRow >= target.startRow &&
    range.startColumn <= target.endColumn &&
    range.endColumn >= target.startColumn
  )
}
