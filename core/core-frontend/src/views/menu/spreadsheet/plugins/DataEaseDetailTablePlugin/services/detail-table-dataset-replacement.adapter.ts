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
import type { DetailTableConfig } from '../types'
import { DetailTableInstanceService } from './detail-table-instance.service'
import { DetailTableRuntimeService } from './detail-table-runtime.service'

const collectCustomExpressionFieldIds = (expression?: string): string[] => {
  if (!expression) return []
  const fieldIds: string[] = []
  const pattern = /\[([^\]]+)\]/g
  let match = pattern.exec(expression)
  while (match) {
    fieldIds.push(match[1])
    match = pattern.exec(expression)
  }
  return fieldIds
}

const replaceCustomExpressionFieldIds = (
  expression: string | undefined,
  resolveFieldId: (fieldId: string) => string | number
): string | undefined => {
  if (!expression) return expression
  return expression.replace(/\[([^\]]+)\]/g, (_match, fieldId: string) => {
    return `[${resolveFieldId(fieldId)}]`
  })
}

export class DetailTableDatasetReplacementAdapter implements DatasetReplacementAdapter {
  readonly type = 'detail'
  private readonly _univerApi: FUniver

  constructor(
    @Inject(Injector) injector: Injector,
    @Inject(DatasetReplacementAdapterRegistry)
    registry: DatasetReplacementAdapterRegistry,
    @Inject(DetailTableInstanceService)
    private readonly _instanceService: DetailTableInstanceService,
    @Inject(DetailTableRuntimeService)
    private readonly _runtimeService: DetailTableRuntimeService
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
        const referencedIds = [
          ...zoneFields.map(field => field.id),
          ...collectFilterTreeFieldIds(config.data.customFilter),
          ...(config.style.total?.fieldConfig || []).flatMap(field => [
            field.fieldId,
            ...collectCustomExpressionFieldIds(field.customExpression)
          ])
        ]
        return {
          dataset: {
            id: String(config.data.datasetId),
            name: String(config.data.datasetId)
          },
          componentId: config.id,
          componentName: this._getComponentName(config),
          componentType: 'plugin' as const,
          fields: collectFieldReferences(referencedIds, zoneFields, 'q')
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
    this._instanceService.set(unitId, snapshot as DetailTableConfig[])
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

  getConfigs(unitId: string, componentIds: string[]): DetailTableConfig[] {
    const componentIdSet = new Set(componentIds)
    return this._instanceService.get(unitId).filter(config => componentIdSet.has(config.id))
  }

  private _getComponentName(config: DetailTableConfig): string {
    const defaultName = `${config.placement?.sheetName || ''}!${config.placement?.startCell || ''}`
    return config.style?.base?.customBlockName
      ? config.style.base.blockName || defaultName
      : defaultName
  }

  private _replaceConfig(config: DetailTableConfig, mappings: DatasetMapping[]): DetailTableConfig {
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
    )
    next.data.customFilter = replaceFilterTreeFieldIds(next.data.customFilter, fieldId =>
      getTargetField(mappings, sourceDatasetId, fieldId)?.id || fieldId
    ) || {}

    if (next.style.total?.fieldConfig) {
      next.style.total.fieldConfig = next.style.total.fieldConfig.map(field => {
        const target = getTargetField(mappings, sourceDatasetId, field.fieldId)
        const migratedField = target ? migrateFieldReference(field, target) : field
        // 自定义公式保存的是 [字段ID]，需要与总计字段配置一起迁移。
        migratedField.customExpression = replaceCustomExpressionFieldIds(
          field.customExpression,
          fieldId => getTargetField(mappings, sourceDatasetId, fieldId)?.id ?? fieldId
        )
        return migratedField
      })
    }
    return next
  }
}
