import { Disposable, ICommandService, IUniverInstanceService } from '@univerjs/core'
import { IRenderManagerService } from '@univerjs/engine-render'
import { getSheetCommandTarget, SetFrozenCommand, SheetsSelectionsService } from '@univerjs/sheets'
import { SetSelectionFrozenCommand, SheetScrollManagerService } from '@univerjs/sheets-ui'

export class FreezeCommandController extends Disposable {
  constructor(@ICommandService commandService: ICommandService) {
    super()
    // 工具栏和右键菜单共用此入口，使用原生 SetFrozenCommand 保留撤销和重做。
    commandService.unregisterCommand(SetSelectionFrozenCommand.id)
    this.disposeWithMe(
      commandService.registerCommand({
        ...SetSelectionFrozenCommand,
        handler: async (accessor, params) => {
          const target = getSheetCommandTarget(accessor.get(IUniverInstanceService))
          const selections = accessor.get(SheetsSelectionsService).getCurrentSelections()
          if (!target || !selections?.length) return false
          const { unitId, subUnitId, worksheet } = target
          const scroll = accessor
            .get(IRenderManagerService)
            .getRenderById(unitId)
            ?.with(SheetScrollManagerService)
            .getCurrentScrollState()
          const viewRow = scroll?.sheetViewStartRow ?? 0
          const viewColumn = scroll?.sheetViewStartColumn ?? 0
          const { primary, range } = selections[selections.length - 1]
          const type = params?.type ?? 0
          const freeze = { ...worksheet.getFreeze() }
          if (type === 0 || type === 1 || type === 3) {
            freeze.startRow =
              type === 3 ? viewRow + 1 : Math.max(1, primary?.startRow ?? range.startRow)
            freeze.ySplit = Math.max(1, freeze.startRow - viewRow)
          }
          if (type === 0 || type === 2 || type === 4) {
            freeze.startColumn =
              type === 4 ? viewColumn + 1 : Math.max(1, primary?.startColumn ?? range.startColumn)
            freeze.xSplit = Math.max(1, freeze.startColumn - viewColumn)
          }
          return commandService.executeCommand(SetFrozenCommand.id, {
            unitId,
            subUnitId,
            ...freeze
          })
        }
      })
    )
  }
}
