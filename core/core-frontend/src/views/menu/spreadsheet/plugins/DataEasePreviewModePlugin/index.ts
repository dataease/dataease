import {
  Inject,
  Injector,
  Plugin,
  UniverInstanceType,
  touchDependencies
} from '@univerjs/core'
import { SpreadsheetModeService } from '../../services/spreadsheet-mode.service'
import type { SpreadsheetRuntimeOptions } from '../../types/mode'
import { PreviewCommandGuardController } from './controllers/preview-command-guard.controller'
import { PreviewSheetTabContextMenuController } from './controllers/preview-sheet-tab-context-menu.controller'

export const DATAEASE_PREVIEW_MODE_PLUGIN = 'DATAEASE_PREVIEW_MODE_PLUGIN'

export class DataEasePreviewModePlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_PREVIEW_MODE_PLUGIN

  private readonly modeService: SpreadsheetModeService

  constructor(
    config: SpreadsheetRuntimeOptions = {},
    @Inject(Injector) protected readonly _injector: Injector
  ) {
    super()
    this.modeService = new SpreadsheetModeService(config.mode ?? 'edit')
  }

  override onStarting(): void {
    this._injector.add([SpreadsheetModeService, { useValue: this.modeService }])
    this._injector.add([PreviewCommandGuardController])
    this._injector.add([PreviewSheetTabContextMenuController])
  }

  override onRendered(): void {
    touchDependencies(this._injector, [
      [PreviewCommandGuardController],
      [PreviewSheetTabContextMenuController]
    ])
  }
}
