import {
  CommandType,
  CustomCommandExecutionError,
  Disposable,
  ICommandService,
  Inject,
  RedoCommand,
  UndoCommand,
  type ICommandInfo
} from '@univerjs/core'
import {
  OtherFormulaMarkDirty,
  SetFormulaCalculationNotificationMutation,
  SetFormulaCalculationResultMutation,
  SetFormulaCalculationStartMutation,
  SetFormulaCalculationStopMutation,
  SetTriggerFormulaCalculationStartMutation,
  SetOtherFormulaMutation
} from '@univerjs/engine-formula'
import {
  SetWorksheetColWidthMutation,
  SetWorksheetRowAutoHeightMutation,
  SetWorksheetRowHeightMutation,
  SetWorksheetRowIsAutoHeightMutation,
  ToggleGridlinesMutation
} from '@univerjs/sheets'
import { SheetCutCommand } from '@univerjs/sheets-ui'
import { SpreadsheetModeService } from '../../../services/spreadsheet-mode.service'

const PREVIEW_ALLOWED_MUTATION_IDS = new Set([
  SetWorksheetRowHeightMutation.id,
  SetWorksheetRowIsAutoHeightMutation.id,
  SetWorksheetRowAutoHeightMutation.id,
  SetWorksheetColWidthMutation.id,
  ToggleGridlinesMutation.id,
  // 自定义公式条件样式依赖内部公式注册和完整计算生命周期，预览只读不能拦截这类计算行为。
  SetOtherFormulaMutation.id,
  OtherFormulaMarkDirty.id,
  SetTriggerFormulaCalculationStartMutation.id,
  SetFormulaCalculationStartMutation.id,
  SetFormulaCalculationNotificationMutation.id,
  SetFormulaCalculationStopMutation.id,
  SetFormulaCalculationResultMutation.id
])

const PREVIEW_BLOCKED_COMMAND_IDS = new Set([
  SheetCutCommand.id,
  SheetCutCommand.name,
  UndoCommand.id,
  RedoCommand.id,
  'dataease.operation.apply-detail-table',
  'dataease.operation.apply-detail-table-style',
  'dataease.operation.clear-detail-table',
  'dataease.operation.insert-pivot-table',
  'dataease.operation.insert-image-dropdown',
  'dataease.operation.insert-floating-image',
  'dataease.operation.insert-cell-image',
  'dataease.operation.toggle-spreadsheet-filter',
  'dataease.operation.insert-dropdown',
  'dataease.operation.freeze-dropdown',
  'dataease.operation.insert-detail-table',
  'dataease.operation.open-detail-table-create-dialog',
  'dataease.operation.apply-two-slash-cell',
  'dataease.operation.apply-three-slash-cell',
  'dataease.operation.clear-slash-cell',
  'dataease.operation.open-range-select-dialog',
  'sheet.operation.rename-sheet',
  'sheet.operation.insert-hyper-link-toolbar',
  'sheet.operation.add-note-popup',
  'sheet.operation.open-table-selector'
])

export class PreviewCommandGuardController extends Disposable {
  constructor(
    @ICommandService private readonly commandService: ICommandService,
    @Inject(SpreadsheetModeService)
    private readonly modeService: SpreadsheetModeService
  ) {
    super()
    this.disposeWithMe(
      this.commandService.beforeCommandExecuted(command => this.assertCommandAllowed(command))
    )
  }

  private assertCommandAllowed(command: Readonly<ICommandInfo>): void {
    if (!this.modeService.isPreview() || this.modeService.isSystemWrite()) {
      return
    }

    if (PREVIEW_BLOCKED_COMMAND_IDS.has(command.id)) {
      throw new CustomCommandExecutionError('Preview mode is readonly')
    }

    if (
      command.type === CommandType.MUTATION &&
      !PREVIEW_ALLOWED_MUTATION_IDS.has(command.id)
    ) {
      throw new CustomCommandExecutionError('Preview mode is readonly')
    }
  }
}
