import {
  CommandType,
  CustomCommandExecutionError,
  Disposable,
  ICommandService,
  IUniverInstanceService,
  Inject,
  RedoCommand,
  UndoCommand,
  UniverInstanceType,
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
  SetWorksheetActiveOperation,
  SetWorksheetShowCommand,
  SetWorksheetRowAutoHeightMutation,
  SetWorksheetRowHeightMutation,
  SetWorksheetRowIsAutoHeightMutation,
  ToggleGridlinesMutation,
  type ISetWorksheetActiveOperationParams
} from '@univerjs/sheets'
import { CancelHyperLinkCommand, CancelRichHyperLinkCommand } from '@univerjs/sheets-hyper-link'
import { OpenHyperLinkEditPanelOperation } from '@univerjs/sheets-hyper-link-ui'
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
  SetWorksheetShowCommand.id,
  OpenHyperLinkEditPanelOperation.id,
  CancelHyperLinkCommand.id,
  CancelRichHyperLinkCommand.id,
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
    @Inject(IUniverInstanceService)
    private readonly univerInstanceService: IUniverInstanceService,
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

    if (command.id === SetWorksheetActiveOperation.id) {
      this.assertTargetWorksheetVisible(command)
    }

    if (
      command.type === CommandType.MUTATION &&
      !PREVIEW_ALLOWED_MUTATION_IDS.has(command.id)
    ) {
      throw new CustomCommandExecutionError('Preview mode is readonly')
    }
  }

  private assertTargetWorksheetVisible(command: Readonly<ICommandInfo>): void {
    const params = command.params as ISetWorksheetActiveOperationParams | undefined
    if (!params?.unitId || !params.subUnitId) {
      return
    }

    const workbook = this.univerInstanceService.getUnit(
      params.unitId,
      UniverInstanceType.UNIVER_SHEET
    )
    const worksheet = workbook?.getSheetBySheetId(params.subUnitId)
    if (!worksheet?.isSheetHidden()) {
      return
    }

    // 预览态不仅隐藏入口，也拒绝从链接、API 等其他路径激活隐藏 Sheet。
    throw new CustomCommandExecutionError('Hidden worksheet is unavailable in preview mode')
  }
}
