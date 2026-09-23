import {
  CellValueType,
  Disposable,
  Inject,
  IUniverInstanceService,
  isRealNum,
  isTextFormat,
  UniverInstanceType,
  type Workbook
} from '@univerjs/core'
import {
  IExclusiveRangeService,
  RangeProtectionPermissionEditPoint,
  SheetsSelectionsService,
  WorkbookEditablePermission,
  WorksheetEditPermission,
  WorksheetSetCellValuePermission
} from '@univerjs/sheets'
import { getCurrentRangeDisable$ } from '@univerjs/sheets-ui'
import {
  ContextMenuGroup,
  ContextMenuPosition,
  getMenuHiddenObservable,
  IMenuManagerService
} from '@univerjs/ui'
import { combineLatest, map, startWith } from 'rxjs'

export class TableTextToNumberController extends Disposable {
  constructor(
    @IMenuManagerService menuManager: IMenuManagerService,
    @Inject(IExclusiveRangeService) exclusiveRanges: IExclusiveRangeService,
    @Inject(SheetsSelectionsService) private readonly selections: SheetsSelectionsService,
    @IUniverInstanceService private readonly instanceService: IUniverInstanceService
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
                hidden$: combineLatest([
                  getMenuHiddenObservable(accessor, UniverInstanceType.UNIVER_SHEET),
                  selections.selectionChanged$.pipe(startWith(null))
                ]).pipe(map(([hidden]) => hidden || !this.hasConvertibleCell())),
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

  private hasConvertibleCell(): boolean {
    // 原生 hidden$ 固定了菜单创建时的工作表；每次判断都读取当前工作表，避免切换后误隐藏。
    const workbook = this.instanceService.getCurrentUnitOfType<Workbook>(
      UniverInstanceType.UNIVER_SHEET
    )
    const worksheet = workbook?.getActiveSheet()
    const currentSelections = this.selections.getCurrentSelections()
    if (!worksheet || !currentSelections?.length) return false

    for (const { range } of currentSelections) {
      for (let row = range.startRow; row <= range.endRow; row++) {
        for (let column = range.startColumn; column <= range.endColumn; column++) {
          const cell = worksheet.getCellRaw(row, column)
          if (!cell?.v || !isRealNum(cell.v)) continue

          let style = cell.s
          if (typeof style === 'string') {
            style = worksheet.getStyleDataByHash(style)
          }
          // 与原生转换命令保持一致：数字文本或使用文本格式的数字才可转换。
          if (cell.t !== CellValueType.NUMBER || isTextFormat(style?.n?.pattern)) return true
        }
      }
    }
    return false
  }
}
