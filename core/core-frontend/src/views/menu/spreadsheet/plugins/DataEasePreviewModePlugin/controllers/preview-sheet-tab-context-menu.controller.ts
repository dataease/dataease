import { Disposable, IConfigService, Inject } from '@univerjs/core'
import { type IUniverUIConfig, UI_PLUGIN_CONFIG_KEY } from '@univerjs/ui'
import { SpreadsheetModeService } from '../../../services/spreadsheet-mode.service'

export class PreviewSheetTabContextMenuController extends Disposable {
  constructor(
    @IConfigService private readonly configService: IConfigService,
    @Inject(SpreadsheetModeService)
    private readonly modeService: SpreadsheetModeService
  ) {
    super()

    if (!this.modeService.isPreview()) {
      return
    }

    const container = this.resolveContainer()
    if (!container) {
      return
    }

    container.addEventListener('contextmenu', this.handleContextMenu, true)
    this.disposeWithMe({
      dispose: () => container.removeEventListener('contextmenu', this.handleContextMenu, true)
    })
  }

  private readonly handleContextMenu = (event: Event): void => {
    const target = event.target
    // SheetBarTabs has no scoped context-menu switch in Univer 0.25.1.
    if (!(target instanceof Element) || !target.closest('[data-u-comp="slide-tab-item"]')) {
      return
    }

    event.preventDefault()
    event.stopImmediatePropagation()
  }

  private resolveContainer(): HTMLElement | null {
    const configuredContainer = this.configService.getConfig<IUniverUIConfig>(
      UI_PLUGIN_CONFIG_KEY
    )?.container

    if (typeof configuredContainer === 'string') {
      return document.querySelector<HTMLElement>(configuredContainer)
    }

    return configuredContainer ?? null
  }
}
