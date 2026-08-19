import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { RangeSelectController } from './controllers/range-select.controller'

export const RANGE_SELECT_PLUGIN = 'RANGE_SELECT_PLUGIN'

export class RangeSelectPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = RANGE_SELECT_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [[RangeSelectController]]

    dependencies.forEach(d => this._injector.add(d))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [[RangeSelectController]])
  }
}
