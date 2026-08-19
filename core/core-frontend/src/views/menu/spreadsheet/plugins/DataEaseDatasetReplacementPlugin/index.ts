import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { DatasetReplacementAdapterRegistry } from './services/dataset-replacement-adapter-registry.service'
import { SpreadsheetDatasetReplacementService } from './services/spreadsheet-dataset-replacement.service'

export const DATAEASE_DATASET_REPLACEMENT_PLUGIN = 'DATAEASE_DATASET_REPLACEMENT_PLUGIN'

export class DataEaseDatasetReplacementPlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_DATASET_REPLACEMENT_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [
      [DatasetReplacementAdapterRegistry],
      [SpreadsheetDatasetReplacementService]
    ]
    dependencies.forEach(dependency => this._injector.add(dependency))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [
      [DatasetReplacementAdapterRegistry],
      [SpreadsheetDatasetReplacementService]
    ])
  }
}
