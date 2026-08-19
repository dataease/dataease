import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { DataEaseInsertMenuController } from './controllers/insert-menu.controller'

export const DATAEASE_INSERT_PLUGIN = 'DATAEASE_INSERT_PLUGIN'

export class DataEaseInsertPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_INSERT_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [[DataEaseInsertMenuController]]

    dependencies.forEach(d => this._injector.add(d))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [[DataEaseInsertMenuController]])
  }
}
