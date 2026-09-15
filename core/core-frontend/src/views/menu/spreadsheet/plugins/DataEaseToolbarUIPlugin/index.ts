import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { DataEaseToolbarUIController } from './controllers/toolbar-ui.controller'
import { SheetMenuLayoutController } from './controllers/sheet-menu-layout.controller'
import './styles/toolbar.less'

export const DATAEASE_TOOLBAR_UI_PLUGIN = 'DATAEASE_TOOLBAR_UI_PLUGIN'

export class DataEaseToolbarUIPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_TOOLBAR_UI_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [[DataEaseToolbarUIController], [SheetMenuLayoutController]]

    dependencies.forEach(dependency => this._injector.add(dependency))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [[DataEaseToolbarUIController], [SheetMenuLayoutController]])
  }
}
