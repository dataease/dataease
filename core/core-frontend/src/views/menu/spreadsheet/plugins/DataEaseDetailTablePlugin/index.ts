import type { Dependency } from '@univerjs/core'
import { Inject, Injector, Plugin, touchDependencies, UniverInstanceType } from '@univerjs/core'
import { DataEaseDetailTableController } from './controllers/detail-table.controller'
import { TableFillService } from './services/table-fill.service'
import { DetailTableInstanceService } from './services/detail-table-instance.service'
import { DetailTableDisplayStateService } from './services/detail-table-display-state.service'
import { DetailTableSnapshotCleaner } from './services/detail-table-snapshot-cleaner.service'
import { DetailTableRuntimeService } from './services/detail-table-runtime.service'
import { DetailTableInsertionService } from './services/detail-table-insertion.service'
import { DetailTableRangeService } from './services/detail-table-range.service'
import { DetailTableRenderStyleService } from './services/detail-table-render-style.service'
import { DetailTableEditProtectionService } from './services/detail-table-edit-protection.service'
import { pluginSnapshotCleaningService } from '../../services/plugin-snapshot-cleaning.service'
import { pluginRuntimeRegistry } from '../../services/plugin-runtime.service'
import { PluginRenderHoverService } from '../../services/plugin-render-hover.service'
import { PluginRenderHoverLayerService } from '../../services/plugin-render-hover-layer.service'
import { PluginRenderLoadingService } from '../../services/plugin-render-loading.service'
import { DetailTableDatasetReplacementAdapter } from './services/detail-table-dataset-replacement.adapter'
import { TableRangeConflictService } from '../../services/table-range-conflict.service'
import { TableInsertionService } from '../../services/table-insertion.service'
import { TableRenderExpansionService } from '../../services/table-render-expansion.service'

export const DATAEASE_DETAIL_TABLE_PLUGIN = 'DATAEASE_DETAIL_TABLE_PLUGIN'

export class DataEaseDetailTablePlugin extends Plugin {
  static override type = UniverInstanceType.UNIVER_SHEET
  static override pluginName = DATAEASE_DETAIL_TABLE_PLUGIN

  constructor(@Inject(Injector) protected readonly _injector: Injector) {
    super()
  }

  override onStarting(): void {
    const dependencies: Dependency[] = [
      [DataEaseDetailTableController],
      [DetailTableInstanceService],
      [DetailTableDisplayStateService],
      [DetailTableSnapshotCleaner],
      [DetailTableRuntimeService],
      [TableInsertionService],
      [DetailTableInsertionService],
      [TableRangeConflictService],
      [TableRenderExpansionService],
      [DetailTableRangeService],
      [DetailTableRenderStyleService],
      [DetailTableEditProtectionService],
      [PluginRenderHoverService],
      [PluginRenderHoverLayerService],
      [PluginRenderLoadingService],
      [DetailTableDatasetReplacementAdapter],
      [TableFillService]
    ]

    dependencies.forEach(d => this._injector.add(d))
  }

  override onRendered(): void {
    touchDependencies(this._injector, [
      [DataEaseDetailTableController],
      [DetailTableInstanceService],
      [DetailTableDisplayStateService],
      [DetailTableSnapshotCleaner],
      [DetailTableRuntimeService],
      [TableInsertionService],
      [DetailTableInsertionService],
      [TableRangeConflictService],
      [TableRenderExpansionService],
      [DetailTableRangeService],
      [DetailTableRenderStyleService],
      [DetailTableEditProtectionService],
      [PluginRenderHoverService],
      [PluginRenderHoverLayerService],
      [PluginRenderLoadingService],
      [DetailTableDatasetReplacementAdapter],
      [TableFillService]
    ])

    pluginSnapshotCleaningService.register(this._injector.get(DetailTableSnapshotCleaner))
    pluginRuntimeRegistry.register(this._injector.get(DetailTableRuntimeService))
  }
}
