import { Disposable, Inject } from '@univerjs/core'
import {
  IExclusiveRangeService,
  RangeProtectionPermissionEditPoint,
  SheetsSelectionsService,
  WorkbookEditablePermission,
  WorksheetEditPermission,
  WorksheetSetCellValuePermission
} from '@univerjs/sheets'
import { getCurrentRangeDisable$ } from '@univerjs/sheets-ui'
import { ContextMenuGroup, ContextMenuPosition, IMenuManagerService } from '@univerjs/ui'
import { combineLatest, map, startWith } from 'rxjs'

export class TableTextToNumberController extends Disposable {
  constructor(
    @IMenuManagerService menuManager: IMenuManagerService,
    @Inject(IExclusiveRangeService) exclusiveRanges: IExclusiveRangeService,
    @Inject(SheetsSelectionsService) selections: SheetsSelectionsService
  ) {
    super()
    // Univer 0.25 的菜单/区域标识未导出。只对原生结构化表格放开值转换。
    const menuId = 'sheet.contextMenu.text-to-number'
    for (const position of [
      ContextMenuPosition.MAIN_AREA,
      ContextMenuPosition.COL_HEADER,
      ContextMenuPosition.ROW_HEADER
    ]) {
      const original = menuManager
        .getMenuByPositionKey(position)
        .find(group => group.key === ContextMenuGroup.FORMAT)
        ?.children?.find(menu => menu.key === menuId)?.item
      if (!original) continue
      menuManager.mergeMenu({
        [position]: {
          [ContextMenuGroup.FORMAT]: {
            [menuId]: {
              menuItemFactory: accessor => ({
                ...original,
                disabled$: combineLatest([
                  getCurrentRangeDisable$(accessor, {
                    workbookTypes: [WorkbookEditablePermission],
                    worksheetTypes: [WorksheetEditPermission, WorksheetSetCellValuePermission],
                    rangeTypes: [RangeProtectionPermissionEditPoint]
                  }),
                  selections.selectionChanged$.pipe(startWith(selections.getCurrentSelections())),
                  exclusiveRanges.exclusiveRangesChange$.pipe(startWith(null))
                ]).pipe(
                  map(
                    ([disabled, current]) =>
                      disabled ||
                      exclusiveRanges
                        .getInterestGroupId(current ? [...current] : [])
                        .some(feature => feature !== 'SHEET_TABLE')
                  )
                )
              })
            }
          }
        }
      })
    }
  }
}
