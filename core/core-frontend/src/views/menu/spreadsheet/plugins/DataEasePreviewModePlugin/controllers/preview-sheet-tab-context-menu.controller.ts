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

    // onRendered runs after Univer has mounted the sheet tab bar.
    const tabBar = this.resolveContainer()?.querySelector<HTMLElement>(
      '[data-u-comp="slide-tab-bar"]'
    )
    if (!tabBar) {
      return
    }

    // Block bubbling before Univer's React context-menu handler on the parent.
    tabBar.addEventListener('contextmenu', this.handleContextMenu)
    this.disposeWithMe({
      dispose: () => tabBar.removeEventListener('contextmenu', this.handleContextMenu)
    })
  }

  private readonly handleContextMenu = (event: Event): void => {
    event.preventDefault()
    event.stopPropagation()
  }

  private resolveContainer(): HTMLElement | null {
    const configuredContainer =
      this.configService.getConfig<IUniverUIConfig>(UI_PLUGIN_CONFIG_KEY)?.container

    if (typeof configuredContainer === 'string') {
      return document.querySelector<HTMLElement>(configuredContainer)
    }

    return configuredContainer ?? null
  }
}
