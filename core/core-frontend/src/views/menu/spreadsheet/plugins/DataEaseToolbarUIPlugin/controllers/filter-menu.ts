import {
  IUniverInstanceService,
  type IAccessor,
  type IRange,
  UniverInstanceType
} from '@univerjs/core'
import { SheetsFilterService, SmartToggleSheetsFilterCommand } from '@univerjs/sheets-filter'
import {
  RangeProtectionPermissionViewPoint,
  SheetsSelectionsService,
  WorksheetFilterPermission,
  WorksheetViewPermission
} from '@univerjs/sheets'
import {
  getCurrentRangeDisable$,
  getObservableWithExclusiveRange$
} from '@univerjs/sheets-ui'
import { getMenuHiddenObservable, MenuItemType } from '@univerjs/ui'
import { combineLatest, merge } from 'rxjs'
import { distinctUntilChanged, map, startWith } from 'rxjs/operators'
import { DetailTableDisplayStateService } from '../../DataEaseDetailTablePlugin/services/detail-table-display-state.service'
import { PivotTableDisplayStateService } from '../../DataEasePivotTablePlugin/services/pivot-table-display-state.service'

export function DataEaseFilterMenuFactory(accessor: IAccessor) {
  const sheetsFilterService = accessor.get(SheetsFilterService)
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
      const renderedRanges = [
        ...detailTableDisplayStateService.list().map(state => ({
          sheetId: state.sheetId,
          range: toRange(state.startCell, state.rowCount, state.colCount)
        })),
        ...pivotTableDisplayStateService.list().map(state => ({
          sheetId: state.sheetId,
          range: toRange(state.startCell, state.rowCount, state.columnCount)
        }))
      ]

      // 选区只要与插件渲染区域部分重叠，就禁用原生过滤入口。
      return selections.some(selection =>
        renderedRanges.some(
          renderedRange =>
            renderedRange.sheetId === sheetId &&
            intersects(selection.range, renderedRange.range)
        )
      )
    }),
    distinctUntilChanged()
  )

  const permissionDisabled$ = getObservableWithExclusiveRange$(
    accessor,
    getCurrentRangeDisable$(accessor, {
      worksheetTypes: [WorksheetFilterPermission, WorksheetViewPermission],
      rangeTypes: [RangeProtectionPermissionViewPoint]
    })
  )

  return {
    id: SmartToggleSheetsFilterCommand.id,
    type: MenuItemType.BUTTON_SELECTOR,
    icon: 'FilterIcon',
    tooltip: 'sheets-filter-ui.toolbar.smart-toggle-filter-tooltip',
    hidden$: getMenuHiddenObservable(accessor, UniverInstanceType.UNIVER_SHEET),
    activated$: sheetsFilterService.activeFilterModel$.pipe(map(model => !!model)),
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
