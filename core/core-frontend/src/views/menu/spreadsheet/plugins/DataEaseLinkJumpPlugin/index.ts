import { Inject, Injector, Plugin, UniverInstanceType } from '@univerjs/core'
import type { SpreadsheetRuntimeOptions } from '../../types/mode'
import { SpreadsheetLinkJumpService } from './services/link-jump.service'

export class DataEaseLinkJumpPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = 'DATAEASE_LINK_JUMP_PLUGIN'

  constructor(
    private readonly options: SpreadsheetRuntimeOptions = {},
    @Inject(Injector) protected readonly _injector: Injector
  ) {
    super()
  }

  override onStarting(): void {
    this._injector.add([SpreadsheetLinkJumpService])
    this._injector.get(SpreadsheetLinkJumpService).initialize(this.options)
  }
}
