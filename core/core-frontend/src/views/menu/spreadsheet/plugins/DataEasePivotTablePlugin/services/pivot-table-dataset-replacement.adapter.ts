import { Inject, Injector } from '@univerjs/core'
import { FUniver } from '@univerjs/core/facade'
import type {
  DatasetMapping,
  DatasetReplacementAdapter,
  DatasetUsageFragment
} from '../../DataEaseDatasetReplacementPlugin/types'
import { DatasetReplacementAdapterRegistry } from '../../DataEaseDatasetReplacementPlugin/services/dataset-replacement-adapter-registry.service'
import { migrateFieldReference } from '../../DataEaseDatasetReplacementPlugin/utils/field-migration'
import {
  cloneReplacementValue,
  getTargetDataset,
  getTargetField
} from '../../DataEaseDatasetReplacementPlugin/utils/replacement-lookup'
import {
  collectFieldReferences,
  collectFilterTreeFieldIds,
  replaceFilterTreeFieldIds
} from '../../DataEaseDatasetReplacementPlugin/utils/usage-collector'
import type { PivotTableConfig } from '../types'
import { PivotTableInstanceService } from './pivot-table-instance.service'
import { PivotTableRuntimeService } from './pivot-table-runtime.service'

export class PivotTableDatasetReplacementAdapter implements DatasetReplacementAdapter {
  readonly type = 'pivot'
  private readonly _univerApi: FUniver

  constructor(
    @Inject(Injector) injector: Injector,
    @Inject(DatasetReplacementAdapterRegistry)
    registry: DatasetReplacementAdapterRegistry,
    @Inject(PivotTableInstanceService)
    private readonly _instanceService: PivotTableInstanceService,
    @Inject(PivotTableRuntimeService)
    private readonly _runtimeService: PivotTableRuntimeService
  ) {
    this._univerApi = FUniver.newAPI(injector)
    registry.register(this)
  }

  collect(unitId: string, componentId?: string): DatasetUsageFragment[] {
    return this._instanceService.get(unitId)
      .filter(config => !componentId || config.id === componentId)
      .filter(config =>
        config.data.datasetId !== undefined &&
        config.data.datasetId !== null &&
        config.data.datasetId !== ''
      )
      .map(config => {
        const zoneFields = Object.values(config.data.zones || {}).flat()
        return {
          dataset: {
            id: String(config.data.datasetId),
            name: String(config.data.datasetId)
          },
          componentId: config.id,
          componentName: this._getComponentName(config),
          componentType: 'plugin' as const,
          fields: collectFieldReferences([
            ...zoneFields.map(field => field.id),
            ...collectFilterTreeFieldIds(config.data.customFilter)
          ], zoneFields)
        }
      })
  }

  snapshot(unitId: string, _componentIds: string[]): unknown {
    return cloneReplacementValue(this._instanceService.get(unitId))
  }

  replace(unitId: string, mappings: DatasetMapping[], componentIds: string[]): void {
    const componentIdSet = new Set(componentIds)
    this._instanceService.set(
      unitId,
      this._instanceService.get(unitId).map(config =>
        componentIdSet.has(config.id) ? this._replaceConfig(config, mappings) : config
      )
    )
  }

  restore(unitId: string, snapshot: unknown): void {
    this._instanceService.set(unitId, snapshot as PivotTableConfig[])
  }

  async refresh(unitId: string, componentIds: string[]): Promise<string[]> {
    const componentIdSet = new Set(componentIds)
    const configs = this._instanceService.get(unitId).filter(config => componentIdSet.has(config.id))
    const results = await Promise.all(configs.map(async config => {
      try {
        const refreshed = await this._runtimeService.refreshData({
          univerApi: this._univerApi,
          config
        })
        return refreshed === false ? config.id : undefined
      } catch {
        return config.id
      }
    }))
    return results.filter((id): id is string => Boolean(id))
  }

  getConfigs(unitId: string, componentIds: string[]): PivotTableConfig[] {
    const componentIdSet = new Set(componentIds)
    return this._instanceService.get(unitId).filter(config => componentIdSet.has(config.id))
  }

  private _getComponentName(config: PivotTableConfig): string {
    const defaultName = `${config.placement?.sheetName || ''}!${config.placement?.startCell || ''}`
    return config.style?.base?.customBlockName
      ? config.style.base.blockName || defaultName
      : defaultName
  }

  private _replaceConfig(config: PivotTableConfig, mappings: DatasetMapping[]): PivotTableConfig {
    const next = cloneReplacementValue(config)
    const sourceDatasetId = config.data.datasetId
    const targetDataset = getTargetDataset(mappings, sourceDatasetId)
    if (!targetDataset) return next

    next.data.datasetId = targetDataset.id
    next.data.zones = Object.fromEntries(
      Object.entries(next.data.zones || {}).map(([zoneId, fields]) => [
        zoneId,
        fields.map(field => {
          const target = getTargetField(mappings, sourceDatasetId, field.id)
          return target ? migrateFieldReference(field, target) : field
        })
      ])
    ) as PivotTableConfig['data']['zones']
    next.data.customFilter = replaceFilterTreeFieldIds(next.data.customFilter, fieldId =>
      getTargetField(mappings, sourceDatasetId, fieldId)?.id || fieldId
    ) || {}
    return next
  }
}
