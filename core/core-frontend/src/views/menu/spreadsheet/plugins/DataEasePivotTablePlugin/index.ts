import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import './adapter'
import { pluginRuntimeRegistry } from '../../services/plugin-runtime.service'
import { DataEasePivotTableController } from './controllers/pivot-table.controller'
import { PivotTableInstanceService } from './services/pivot-table-instance.service'
import { PivotTableInsertionService } from './services/pivot-table-insertion.service'
import { PivotTableRuntimeService } from './services/pivot-table-runtime.service'
import { PivotTableDisplayStateService } from './services/pivot-table-display-state.service'
import { PivotTableFillService } from './services/pivot-table-fill.service'
import { PivotTableRangeService } from './services/pivot-table-range.service'
import { PivotTableSnapshotCleaner } from './services/pivot-table-snapshot-cleaner.service'
import { pluginSnapshotCleaningService } from '../../services/plugin-snapshot-cleaning.service'
import { PivotTableRenderStyleService } from './services/pivot-table-render-style.service'
import { PivotTableEditProtectionService } from './services/pivot-table-edit-protection.service'
import { PivotTableDatasetReplacementAdapter } from './services/pivot-table-dataset-replacement.adapter'

export const DATAEASE_PIVOT_TABLE_PLUGIN = 'DATAEASE_PIVOT_TABLE_PLUGIN'

export class DataEasePivotTablePlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_PIVOT_TABLE_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [
      [DataEasePivotTableController],
      [PivotTableInstanceService],
      [PivotTableInsertionService],
      [PivotTableRuntimeService],
      [PivotTableDisplayStateService],
      [PivotTableRangeService],
      [PivotTableFillService],
      [PivotTableSnapshotCleaner],
      [PivotTableRenderStyleService],
      [PivotTableEditProtectionService],
      [PivotTableDatasetReplacementAdapter]
    ]
    dependencies.forEach(dependency => this._injector.add(dependency))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [
      [DataEasePivotTableController],
      [PivotTableInstanceService],
      [PivotTableInsertionService],
      [PivotTableRuntimeService],
      [PivotTableDisplayStateService],
      [PivotTableRangeService],
      [PivotTableFillService],
      [PivotTableSnapshotCleaner],
      [PivotTableRenderStyleService],
      [PivotTableEditProtectionService],
      [PivotTableDatasetReplacementAdapter]
    ])
    pluginSnapshotCleaningService.register(this._injector.get(PivotTableSnapshotCleaner))
    pluginRuntimeRegistry.register(this._injector.get(PivotTableRuntimeService))
  }
}
