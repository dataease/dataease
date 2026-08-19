import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { DataEaseFilterController } from './controllers/filter.controller'
import { FilterInstanceService } from './services/filter-instance.service'
import { SpreadsheetFilterRuntimeService } from './services/filter-runtime.service'
import { FilterDatasetReplacementAdapter } from './services/filter-dataset-replacement.adapter'
import './adapter'
import './styles/query-control.less'

export const DATAEASE_FILTER_PLUGIN = 'DATAEASE_FILTER_PLUGIN'

export class DataEaseFilterPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_FILTER_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [
      [DataEaseFilterController],
      [FilterInstanceService],
      [SpreadsheetFilterRuntimeService],
      [FilterDatasetReplacementAdapter]
    ]

    dependencies.forEach(d => this._injector.add(d))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [
      [DataEaseFilterController],
      [FilterInstanceService],
      [SpreadsheetFilterRuntimeService],
      [FilterDatasetReplacementAdapter]
    ])
  }
}
