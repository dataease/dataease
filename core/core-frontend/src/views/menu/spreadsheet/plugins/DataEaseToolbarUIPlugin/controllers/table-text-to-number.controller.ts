import { Disposable, Inject } from '@univerjs/core'
import {
  IExclusiveRangeService,
  RangeProtectionPermissionEditPoint,
  SheetsSelectionsService,
  WorkbookEditablePermission,
  WorksheetEditPermission,
  WorksheetSetCellValuePermission
} from '@univerjs/sheets'
import { getCurrentRangeDisable$, SheetsUIMenuSchema } from '@univerjs/sheets-ui'
import {
  ContextMenuGroup,
  ContextMenuPosition,
  IMenuManagerService,
  type MenuSchemaType
} from '@univerjs/ui'
import { combineLatestWith, map, startWith } from 'rxjs'

export class TableTextToNumberController extends Disposable {
  constructor(
    @IMenuManagerService menuManager: IMenuManagerService,
    @Inject(IExclusiveRangeService) exclusiveRanges: IExclusiveRangeService,
    @Inject(SheetsSelectionsService) selections: SheetsSelectionsService
  ) {
    super()
    // 独占区域中仅放行原生结构化表格，保留其他功能区域的限制。
    const menuId = 'sheet.contextMenu.text-to-number'
    for (const position of [
      ContextMenuPosition.MAIN_AREA,
      ContextMenuPosition.COL_HEADER,
      ContextMenuPosition.ROW_HEADER
    ]) {
      const menuSchema: MenuSchemaType | undefined =
        SheetsUIMenuSchema[position]?.[ContextMenuGroup.FORMAT]?.[menuId]
      const originalFactory = menuSchema?.menuItemFactory
      if (typeof originalFactory !== 'function') continue
      menuManager.mergeMenu({
        [position]: {
          [ContextMenuGroup.FORMAT]: {
            [menuId]: {
              menuItemFactory: accessor => ({
                // 每次创建菜单都调用原生工厂，避免 hidden$ 捕获旧工作表。
                ...originalFactory(accessor),
                disabled$: getCurrentRangeDisable$(accessor, {
                  workbookTypes: [WorkbookEditablePermission],
                  worksheetTypes: [WorksheetEditPermission, WorksheetSetCellValuePermission],
                  rangeTypes: [RangeProtectionPermissionEditPoint]
                }).pipe(
                  combineLatestWith(
                    selections.selectionChanged$.pipe(startWith(selections.getCurrentSelections())),
                    exclusiveRanges.exclusiveRangesChange$.pipe(startWith(null))
                  ),
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
