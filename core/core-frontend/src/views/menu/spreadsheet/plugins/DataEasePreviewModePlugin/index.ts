import {
  IConfigService,
  Inject,
  Injector,
  Plugin,
  UniverInstanceType,
  touchDependencies
} from '@univerjs/core'
import { type IUniverUIConfig, UI_PLUGIN_CONFIG_KEY } from '@univerjs/ui'
import { SpreadsheetModeService } from '../../services/spreadsheet-mode.service'
import type { SpreadsheetRuntimeOptions } from '../../types/mode'
import { PreviewCommandGuardController } from './controllers/preview-command-guard.controller'
import { PreviewSheetTabContextMenuController } from './controllers/preview-sheet-tab-context-menu.controller'
import './styles/preview.less'

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

    if (!this.modeService.isPreview()) return

    const config = this._injector
      .get(IConfigService)
      .getConfig<IUniverUIConfig>(UI_PLUGIN_CONFIG_KEY)
    const popupRoot = config?.popupRootId ? document.getElementById(config.popupRootId) : null
    if (!popupRoot) return

    // Univer 的批注组件没有只读配置，在当前实例的浮层容器内保留原生文本选择和滚动。
    const makeNotesReadOnly = () => {
      popupRoot
        .querySelectorAll<HTMLTextAreaElement>('textarea[data-u-comp="note-textarea"]')
        .forEach(note => {
          note.readOnly = true
          note.style.resize = 'none'
        })
    }
    const observer = new MutationObserver(makeNotesReadOnly)
    observer.observe(popupRoot, { childList: true, subtree: true })
    this.disposeWithMe({ dispose: () => observer.disconnect() })
    makeNotesReadOnly()
  }
}
