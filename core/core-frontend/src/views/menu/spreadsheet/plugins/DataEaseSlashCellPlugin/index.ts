import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { DataEaseSlashCellController } from './controllers/slash-cell.controller'
import { SlashCellRenderService } from './services/slash-cell-render.service'
import { SlashCellStateService } from './services/slash-cell-state.service'
import { SlashCellStyleHiderService } from './services/slash-cell-style-hider.service'

export const DATAEASE_SLASH_CELL_PLUGIN = 'DATAEASE_SLASH_CELL_PLUGIN'

export class DataEaseSlashCellPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_SLASH_CELL_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [
      [DataEaseSlashCellController],
      [SlashCellStateService],
      [SlashCellRenderService],
      [SlashCellStyleHiderService]
    ]
    dependencies.forEach(dependency => this._injector.add(dependency))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [
      [DataEaseSlashCellController],
      [SlashCellStateService],
      [SlashCellRenderService],
      [SlashCellStyleHiderService]
    ])
  }
}
