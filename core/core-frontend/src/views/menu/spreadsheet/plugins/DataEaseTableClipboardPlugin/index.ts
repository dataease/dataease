import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { TableClipboardService } from '../../services/table-clipboard.service'
import { TableClipboardLayerService } from '../../services/table-clipboard-layer.service'
import { DataEaseTableClipboardController } from './controllers/table-clipboard.controller'

export const DATAEASE_TABLE_CLIPBOARD_PLUGIN = 'DATAEASE_TABLE_CLIPBOARD_PLUGIN'

export class DataEaseTableClipboardPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_TABLE_CLIPBOARD_PLUGIN

  constructor(@Inject(Injector) protected readonly injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [
      [TableClipboardLayerService],
      [TableClipboardService],
      [DataEaseTableClipboardController]
    ]
    dependencies.forEach(dependency => this.injector.add(dependency))
  }

  override onRendered(): void {
    touchDependencies(this.injector, [
      [TableClipboardLayerService],
      [TableClipboardService],
      [DataEaseTableClipboardController]
    ])
  }
}
