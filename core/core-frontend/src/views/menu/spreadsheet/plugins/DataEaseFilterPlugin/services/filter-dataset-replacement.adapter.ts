import { Inject } from '@univerjs/core'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterConfig,
  SpreadsheetFilterLinkedField,
  SpreadsheetFilterTreeField
} from '../../../types/plugin'
import type {
  DatasetMapping,
  DatasetReplacementAdapter,
  DatasetUsageFragment,
  FieldUsageFragment,
  ReplacementFieldGroup
} from '../../DataEaseDatasetReplacementPlugin/types'
import { DatasetReplacementAdapterRegistry } from '../../DataEaseDatasetReplacementPlugin/services/dataset-replacement-adapter-registry.service'
import {
  cloneReplacementValue,
  getTargetDataset,
  getTargetField
} from '../../DataEaseDatasetReplacementPlugin/utils/replacement-lookup'
import { FilterInstanceService } from './filter-instance.service'
import { SpreadsheetFilterRuntimeService } from './filter-runtime.service'
import { dispatchSpreadsheetFilterConfigChange } from '../utils/events'

interface FieldReference {
  fieldId?: string | number
  fieldName?: string
  groupType?: ReplacementFieldGroup
  deType?: number
}

const inferConditionType = (
  condition: SpreadsheetFilterCondition
): { groupType: ReplacementFieldGroup; deType: number } => {
  if (['numberSelect', 'numberRange'].includes(condition.displayType)) {
    return { groupType: 'q', deType: 2 }
  }
  if (['time', 'timeRange'].includes(condition.displayType)) {
    return { groupType: 'd', deType: 1 }
  }
  return { groupType: 'd', deType: 0 }
}

const toUsageField = (
  condition: SpreadsheetFilterCondition,
  reference: FieldReference
): FieldUsageFragment | undefined => {
  if (reference.fieldId === undefined || reference.fieldId === null || reference.fieldId === '') {
    return undefined
  }
  const inferred = inferConditionType(condition)
  return {
    fieldId: String(reference.fieldId),
    name: reference.fieldName || String(reference.fieldId),
    groupType: reference.groupType || inferred.groupType,
    deType: reference.deType ?? inferred.deType,
    occurrences: 1,
    metadataComplete: Boolean(
      reference.fieldName &&
      (reference.groupType || reference.deType !== undefined)
    )
  }
}

const replaceLinkedField = (
  field: SpreadsheetFilterLinkedField,
  mappings: DatasetMapping[]
): SpreadsheetFilterLinkedField => {
  const targetDataset = getTargetDataset(mappings, field.datasetId)
  const targetField = getTargetField(mappings, field.datasetId, field.fieldId)
  if (!targetDataset || !targetField) return field
  return {
    ...field,
    datasetId: targetDataset.id,
    datasetName: targetDataset.name,
    fieldId: targetField.id,
    fieldName: targetField.name,
    groupType: targetField.groupType,
    deType: targetField.deType
  }
}

export class FilterDatasetReplacementAdapter implements DatasetReplacementAdapter {
  readonly type = 'filter'

  constructor(
    @Inject(DatasetReplacementAdapterRegistry)
    registry: DatasetReplacementAdapterRegistry,
    @Inject(FilterInstanceService)
    private readonly _instanceService: FilterInstanceService,
    @Inject(SpreadsheetFilterRuntimeService)
    private readonly _runtimeService: SpreadsheetFilterRuntimeService
  ) {
    registry.register(this)
  }

  collect(unitId: string, componentId?: string): DatasetUsageFragment[] {
    const config = this._instanceService.get(unitId)
    return config.conditions.flatMap(condition => [
      ...this._collectOptionDataset(condition, componentId),
      ...this._collectTreeDatasets(condition, componentId),
      ...this._collectLinkedDatasets(condition, componentId)
    ])
  }

  snapshot(unitId: string, _componentIds: string[]): unknown {
    return cloneReplacementValue(this._instanceService.get(unitId))
  }

  replace(unitId: string, mappings: DatasetMapping[], componentIds: string[]): void {
    const componentIdSet = new Set(componentIds)
    const config = cloneReplacementValue(this._instanceService.get(unitId))
    config.conditions = config.conditions.map(condition =>
      this._replaceCondition(condition, mappings, componentIdSet)
    )
    const savedConfig = this._instanceService.set(unitId, config)
    dispatchSpreadsheetFilterConfigChange(savedConfig)
  }

  restore(unitId: string, snapshot: unknown): void {
    const restoredConfig = this._instanceService.set(
      unitId,
      snapshot as SpreadsheetFilterConfig
    )
    dispatchSpreadsheetFilterConfigChange(restoredConfig)
  }

  async refresh(
    unitId: string,
    componentIds: string[],
    mappings: DatasetMapping[] = [],
    snapshot?: unknown
  ): Promise<string[]> {
    const componentIdSet = new Set(componentIds)
    const affectedDatasetIds = new Set(
      mappings.map(mapping => mapping.source.dataset.id)
    )
    const previousConfig = snapshot as SpreadsheetFilterConfig | undefined
    const affectedConditionIds = (previousConfig || this._instanceService.get(unitId))
      .conditions
      .filter(condition =>
        this._isConditionAffected(condition, componentIdSet, affectedDatasetIds)
      )
      .map(condition => condition.id)
    this._runtimeService.clearValuesForConditions(unitId, affectedConditionIds)
    return []
  }

  getConfigs(_unitId: string, _componentIds: string[]): SpreadsheetFilterConfig[] {
    return []
  }

  private _collectOptionDataset(
    condition: SpreadsheetFilterCondition,
    componentId?: string
  ): DatasetUsageFragment[] {
    if (
      !condition.optionDatasetId ||
      (componentId && componentId !== condition.id)
    ) return []

    const fields = [
      toUsageField(condition, {
        fieldId: condition.queryFieldId,
        fieldName: condition.queryFieldName
      }),
      toUsageField(condition, {
        fieldId: condition.displayFieldId,
        fieldName: condition.displayFieldName
      }),
      toUsageField(condition, {
        fieldId: condition.sortFieldId,
        fieldName: condition.sortFieldName
      })
    ].filter((field): field is FieldUsageFragment => Boolean(field))

    return [{
      dataset: {
        id: String(condition.optionDatasetId),
        name: condition.optionDatasetName || String(condition.optionDatasetId)
      },
      componentId: condition.id,
      componentType: 'filter' as const,
      fields
    }]
  }

  private _collectTreeDatasets(
    condition: SpreadsheetFilterCondition,
    componentId?: string
  ): DatasetUsageFragment[] {
    if (componentId && componentId !== condition.id) return []
    const fieldsByDataset = new Map<string, SpreadsheetFilterTreeField[]>()
    if (condition.treeDatasetId !== undefined && condition.treeDatasetId !== null) {
      fieldsByDataset.set(String(condition.treeDatasetId), [])
    }
    condition.treeFields.forEach(field => {
      const datasetId = String(field.datasetId)
      fieldsByDataset.set(datasetId, [...(fieldsByDataset.get(datasetId) || []), field])
    })

    return Array.from(fieldsByDataset.entries()).map(([datasetId, fields]) => ({
      dataset: {
        id: datasetId,
        name: datasetId === String(condition.treeDatasetId)
          ? condition.treeDatasetName || datasetId
          : datasetId
      },
      componentId: condition.id,
      componentType: 'filter' as const,
      fields: fields.map(field => ({
        fieldId: String(field.fieldId),
        name: field.fieldName,
        groupType: 'd',
        deType: field.deType,
        occurrences: 1,
        metadataComplete: true
      }))
    }))
  }

  private _collectLinkedDatasets(
    condition: SpreadsheetFilterCondition,
    componentId?: string
  ): DatasetUsageFragment[] {
    const linkedFields = [
      ...condition.linkedFields,
      ...condition.treeLevelMappings.flatMap(mapping => mapping.linkedFields)
    ].filter(field =>
      field.datasetId !== undefined &&
      (!componentId || field.pluginId === componentId)
    )
    const fieldsByDatasetAndPlugin = new Map<string, SpreadsheetFilterLinkedField[]>()
    linkedFields.forEach(field => {
      const key = `${field.datasetId}:${field.pluginId}`
      fieldsByDatasetAndPlugin.set(key, [
        ...(fieldsByDatasetAndPlugin.get(key) || []),
        field
      ])
    })

    return Array.from(fieldsByDatasetAndPlugin.values()).map(fields => ({
      dataset: {
        id: String(fields[0].datasetId),
        name: fields[0].datasetName || String(fields[0].datasetId)
      },
      componentId: fields[0].pluginId,
      componentType: 'filter' as const,
      fields: fields
        .map(field => toUsageField(condition, field))
        .filter((field): field is FieldUsageFragment => Boolean(field))
    }))
  }

  private _replaceCondition(
    condition: SpreadsheetFilterCondition,
    mappings: DatasetMapping[],
    componentIds: Set<string>
  ): SpreadsheetFilterCondition {
    const next = cloneReplacementValue(condition)
    const replaceOwnDatasets = componentIds.has(condition.id)
    let replaced = false

    if (replaceOwnDatasets) {
      replaced = this._replaceOptionDataset(next, mappings) || replaced
      replaced = this._replaceTreeDatasets(next, mappings) || replaced
    }

    const replaceLinkedFieldIfAffected = (field: SpreadsheetFilterLinkedField) => {
      if (!componentIds.has(field.pluginId)) return field
      const replacedField = replaceLinkedField(field, mappings)
      replaced = replacedField !== field || replaced
      return replacedField
    }
    next.linkedFields = next.linkedFields.map(replaceLinkedFieldIfAffected)
    next.treeLevelMappings = next.treeLevelMappings.map(mapping => ({
      ...mapping,
      linkedFields: mapping.linkedFields.map(replaceLinkedFieldIfAffected)
    }))
    if (replaced) {
      next.selectValue = undefined
    }
    return next
  }

  private _replaceOptionDataset(
    condition: SpreadsheetFilterCondition,
    mappings: DatasetMapping[]
  ): boolean {
    const sourceDatasetId = condition.optionDatasetId
    const targetDataset = getTargetDataset(mappings, sourceDatasetId)
    if (!targetDataset) return false

    const queryField = getTargetField(mappings, sourceDatasetId, condition.queryFieldId)
    const displayField = getTargetField(mappings, sourceDatasetId, condition.displayFieldId)
    const sortField = getTargetField(mappings, sourceDatasetId, condition.sortFieldId)
    condition.optionDatasetId = targetDataset.id
    condition.optionDatasetName = targetDataset.name
    if (queryField) {
      condition.queryFieldId = queryField.id
      condition.queryFieldName = queryField.name
    }
    if (displayField) {
      condition.displayFieldId = displayField.id
      condition.displayFieldName = displayField.name
    }
    if (sortField) {
      condition.sortFieldId = sortField.id
      condition.sortFieldName = sortField.name
    }
    return true
  }

  private _replaceTreeDatasets(
    condition: SpreadsheetFilterCondition,
    mappings: DatasetMapping[]
  ): boolean {
    let replaced = false
    const sourceTreeDatasetId = condition.treeDatasetId
    const targetTreeDataset = getTargetDataset(mappings, sourceTreeDatasetId)
    if (targetTreeDataset) {
      condition.treeDatasetId = targetTreeDataset.id
      condition.treeDatasetName = targetTreeDataset.name
      replaced = true
    }

    condition.treeFields = condition.treeFields.map(field => {
      const targetDataset = getTargetDataset(mappings, field.datasetId)
      const targetField = getTargetField(mappings, field.datasetId, field.fieldId)
      if (!targetDataset || !targetField) return field
      replaced = true
      return {
        ...field,
        datasetId: targetDataset.id,
        fieldId: targetField.id,
        fieldName: targetField.name,
        deType: targetField.deType ?? field.deType
      }
    })
    condition.treeLevelMappings = condition.treeLevelMappings.map(mapping => {
      const targetField = getTargetField(mappings, sourceTreeDatasetId, mapping.treeFieldId)
      if (!targetField) return mapping
      replaced = true
      return {
        ...mapping,
        treeFieldId: targetField.id
      }
    })
    return replaced
  }

  private _isConditionAffected(
    condition: SpreadsheetFilterCondition,
    componentIds: Set<string>,
    datasetIds: Set<string>
  ): boolean {
    if (componentIds.has(condition.id)) {
      const ownDatasetIds = [
        condition.optionDatasetId,
        condition.treeDatasetId,
        ...condition.treeFields.map(field => field.datasetId)
      ]
      if (ownDatasetIds.some(datasetId => datasetIds.has(String(datasetId)))) {
        return true
      }
    }

    return [
      ...condition.linkedFields,
      ...condition.treeLevelMappings.flatMap(mapping => mapping.linkedFields)
    ].some(field =>
      componentIds.has(field.pluginId) && datasetIds.has(String(field.datasetId))
    )
  }
}
