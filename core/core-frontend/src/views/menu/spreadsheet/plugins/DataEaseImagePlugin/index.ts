import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { DataEaseImageMenuController } from './controllers/image-menu.controller'

export const DATAEASE_IMAGE_PLUGIN = 'DATAEASE_IMAGE_PLUGIN'

export class DataEaseImagePlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_IMAGE_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [[DataEaseImageMenuController]]

    dependencies.forEach(dependency => this._injector.add(dependency))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [[DataEaseImageMenuController]])
  }
}
