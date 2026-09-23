import { Inject } from '@univerjs/core'
import { useEmitt } from '@/hooks/web/useEmitt'
import { SPREADSHEET_EVENTS } from '../../../utils/events'
import type {
  DatasetMapping,
  DatasetReplacementScope,
  DatasetUsage,
  DatasetUsageFragment,
  FieldUsage,
  ReplacementDraft,
  ReplacementField,
  ReplacementResult
} from '../types'
import { findAutoMatchedField, isFieldCompatible } from '../utils/field-compatibility'
import { DatasetReplacementAdapterRegistry } from './dataset-replacement-adapter-registry.service'

interface AdapterSnapshot {
  adapterType: string
  componentIds: string[]
  value: unknown
}

const unique = <T>(values: T[]): T[] => Array.from(new Set(values))

const getFieldKey = (datasetId: string, fieldId: string): string => `${datasetId}:${fieldId}`

export class SpreadsheetDatasetReplacementService {
  constructor(
    @Inject(DatasetReplacementAdapterRegistry)
    private readonly _adapterRegistry: DatasetReplacementAdapterRegistry
  ) {}

  collectUsage(unitId: string, componentId?: string): DatasetUsage[] {
    const fragments = this._adapterRegistry
      .getAll()
      .flatMap(adapter => adapter.collect(unitId, componentId))

    return this._mergeUsageFragments(fragments)
  }

  createDraft(
    scope: DatasetReplacementScope,
    usages: DatasetUsage[],
    componentId?: string
  ): ReplacementDraft {
    return {
      scope,
      componentId,
      mappings: usages.map(source => ({
        source,
        targetFields: [],
        fields: source.fields.map(field => ({
          source: field,
          autoMatched: false
        }))
      }))
    }
  }

  hydrateUsage(
    usage: DatasetUsage,
    dataset: { id: string; name: string },
    fields: ReplacementField[]
  ): DatasetUsage {
    const fieldsById = new Map(fields.map(field => [String(field.id), field]))
    return {
      ...usage,
      dataset,
      fields: usage.fields.map(field => {
        const metadata = fieldsById.get(String(field.fieldId))
        if (!metadata) return field
        return {
          ...field,
          name: metadata.name,
          dataeaseName: metadata.dataeaseName,
          groupType: metadata.groupType,
          deType: metadata.deType,
          extField: metadata.extField,
          type: metadata.type,
          metadataComplete: true
        }
      })
    }
  }

  setTargetDataset(
    mapping: DatasetMapping,
    target: { id: string; name: string },
    targetFields: ReplacementField[]
  ): void {
    mapping.target = target
    mapping.targetFields = targetFields
    mapping.fields = mapping.source.fields.map(source => {
      const matched = findAutoMatchedField(source, targetFields)
      return {
        source,
        target: matched,
        autoMatched: Boolean(matched)
      }
    })
  }

  clearTargetDataset(mapping: DatasetMapping): void {
    delete mapping.target
    mapping.targetFields = []
    mapping.fields = mapping.source.fields.map(source => ({
      source,
      autoMatched: false
    }))
  }

  setTargetField(mapping: DatasetMapping, fieldKey: string, target: ReplacementField): void {
    const fieldMapping = mapping.fields.find(item => item.source.key === fieldKey)
    if (!fieldMapping) {
      throw new Error(`Unknown source field: ${fieldKey}`)
    }
    if (!isFieldCompatible(fieldMapping.source, target)) {
      throw new Error(`Incompatible target field: ${target.id}`)
    }

    fieldMapping.target = target
    fieldMapping.autoMatched = false
  }

  isComplete(draft: ReplacementDraft): boolean {
    return this.validate(draft).length === 0
  }

  validate(draft: ReplacementDraft): string[] {
    const errors: string[] = []
    if (!draft.mappings.length) {
      errors.push('At least one dataset mapping is required')
      return errors
    }
    if (draft.scope === 'workbook') {
      if (!draft.mappings.some(mapping => this._isMappingComplete(mapping))) {
        errors.push('At least one complete dataset mapping is required')
      }
      return errors
    }
    if (!draft.componentId) {
      errors.push('Component replacement requires a component id')
    }

    draft.mappings.forEach(mapping => errors.push(...this._validateMapping(mapping)))
    return errors
  }

  async replace(unitId: string, draft: ReplacementDraft): Promise<ReplacementResult> {
    const mappings = draft.scope === 'workbook'
      ? draft.mappings.filter(mapping => this._isMappingComplete(mapping))
      : draft.mappings
    const errors = this.validate({ ...draft, mappings })
    if (errors.length) {
      throw new Error(errors.join('; '))
    }

    const componentIdsByDatasetId = new Map(
      mappings.map(mapping => [
        mapping.source.dataset.id,
        new Set(mapping.source.componentIds)
      ])
    )
    const snapshots: AdapterSnapshot[] = []

    try {
      for (const adapter of this._adapterRegistry.getAll()) {
        const adapterComponentIds = unique(
          adapter.collect(unitId, draft.componentId)
            .filter(fragment =>
              componentIdsByDatasetId
                .get(fragment.dataset.id)
                ?.has(fragment.componentId)
            )
            .map(fragment => fragment.componentId)
        )
        if (!adapterComponentIds.length) continue

        snapshots.push({
          adapterType: adapter.type,
          componentIds: adapterComponentIds,
          value: adapter.snapshot(unitId, adapterComponentIds)
        })
        adapter.replace(unitId, mappings, adapterComponentIds)
      }
      if (!snapshots.length) {
        throw new Error('No matching spreadsheet components were found')
      }
    } catch (error) {
      for (const snapshot of snapshots.reverse()) {
        try {
          this._adapterRegistry.get(snapshot.adapterType)?.restore(unitId, snapshot.value)
        } catch (restoreError) {
        }
      }
      throw error
    }

    const changedConfigs = snapshots.flatMap(snapshot =>
      this._adapterRegistry
        .get(snapshot.adapterType)
        ?.getConfigs?.(unitId, snapshot.componentIds) || []
    )
    useEmitt().emitter.emit(SPREADSHEET_EVENTS.DATASET_REPLACEMENT_COMPLETED, {
      configs: changedConfigs
    })

    const refreshFailedComponentIds = (
      await Promise.all(
        snapshots.map(snapshot =>
          this._adapterRegistry
            .get(snapshot.adapterType)!
            .refresh(unitId, snapshot.componentIds, mappings, snapshot.value)
            .catch(() => snapshot.componentIds)
        )
      )
    ).flat()

    const changedComponentIds = unique(
      snapshots.flatMap(snapshot => snapshot.componentIds)
    )
    return {
      changedComponentIds,
      refreshFailedComponentIds: unique(refreshFailedComponentIds)
    }
  }

  private _isMappingComplete(mapping: DatasetMapping): boolean {
    return this._validateMapping(mapping).length === 0
  }

  private _validateMapping(mapping: DatasetMapping): string[] {
    const errors: string[] = []
    if (!mapping.target) {
      errors.push(`Target dataset is required for ${mapping.source.dataset.name}`)
    }
    mapping.fields.forEach(fieldMapping => {
      if (!fieldMapping.target) {
        errors.push(`Target field is required for ${fieldMapping.source.name}`)
      } else if (!isFieldCompatible(fieldMapping.source, fieldMapping.target)) {
        errors.push(`Target field is incompatible for ${fieldMapping.source.name}`)
      }
    })
    return errors
  }

  private _mergeUsageFragments(fragments: DatasetUsageFragment[]): DatasetUsage[] {
    const datasets = new Map<string, DatasetUsage>()

    fragments.forEach(fragment => {
      let usage = datasets.get(fragment.dataset.id)
      if (!usage) {
        usage = {
          dataset: fragment.dataset,
          componentIds: [],
          componentCount: 0,
          components: [],
          fields: []
        }
        datasets.set(fragment.dataset.id, usage)
      }

      usage.componentIds = unique([...usage.componentIds, fragment.componentId])
      const componentType = fragment.componentType || 'plugin'
      const componentKey = componentType === 'filter'
        ? 'filter'
        : `${componentType}:${fragment.componentId}`
      const componentExists = usage.components.some(component =>
        (component.type === 'filter' ? 'filter' : `${component.type}:${component.id}`) === componentKey
      )
      if (!componentExists) {
        usage.components.push({
          id: fragment.componentId,
          name: fragment.componentName || fragment.componentId,
          type: componentType
        })
      }
      usage.componentCount = usage.components.length

      fragment.fields.forEach(field => {
        const key = getFieldKey(fragment.dataset.id, field.fieldId)
        const existing = usage!.fields.find(item => item.key === key)
        if (existing) {
          existing.componentIds = unique([...existing.componentIds, fragment.componentId])
          existing.occurrences += field.occurrences ?? 1
          if (!existing.metadataComplete && field.metadataComplete) {
            existing.name = field.name
            existing.dataeaseName = field.dataeaseName
            existing.groupType = field.groupType
            existing.deType = field.deType
            existing.extField = field.extField
            existing.type = field.type
            existing.metadataComplete = true
          }
          return
        }

        usage!.fields.push({
          ...field,
          key,
          datasetId: fragment.dataset.id,
          componentIds: [fragment.componentId],
          occurrences: field.occurrences ?? 1
        } satisfies FieldUsage)
      })
    })

    return Array.from(datasets.values())
  }
}
