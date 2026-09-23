import type {
  SpreadsheetFilterConfig,
  SpreadsheetFilterCondition,
  SpreadsheetQueryFilterItem
} from '../../../types/plugin'
import { FilterInstanceService } from './filter-instance.service'
import { Inject } from '@univerjs/core'
import { buildSpreadsheetTextSearchFilter, buildSpreadsheetTreeFilter } from '../utils/filter-tree-builder'
import { normalizeTimeQueryValue } from '../utils/time-filter'

type QueryValueMap = Record<string, unknown>

export class SpreadsheetFilterRuntimeService {
  private readonly _valuesByUnitId = new Map<string, QueryValueMap>()
  private readonly _valuesReadyByUnitId = new Map<string, Promise<void>>()

  constructor(
    @Inject(FilterInstanceService)
    private readonly _filterInstanceService: FilterInstanceService
  ) {}

  setValues(unitId: string, values: QueryValueMap): void {
    this._valuesByUnitId.set(unitId, { ...values })
  }

  getValues(unitId: string): QueryValueMap {
    return { ...(this._valuesByUnitId.get(unitId) || {}) }
  }

  setValuesReady(unitId: string, ready: Promise<unknown>): void {
    this._valuesReadyByUnitId.set(unitId, ready.then(
      () => undefined,
      error => {
      }
    ))
  }

  waitForValues(unitId: string): Promise<void> {
    return this._valuesReadyByUnitId.get(unitId) || Promise.resolve()
  }

  clearValues(unitId: string): void {
    this._valuesByUnitId.delete(unitId)
    this._valuesReadyByUnitId.delete(unitId)
  }

  clearValuesForConditions(unitId: string, conditionIds: string[]): void {
    const conditionIdSet = new Set(conditionIds)
    const currentValues = this._valuesByUnitId.get(unitId)
    if (!currentValues || !conditionIdSet.size) return

    const nextValues = Object.fromEntries(
      Object.entries(currentValues).filter(
        ([conditionId]) => !conditionIdSet.has(conditionId)
      )
    )
    this._valuesByUnitId.set(unitId, nextValues)
  }

  pruneValuesForConfig(unitId: string, config: SpreadsheetFilterConfig): QueryValueMap {
    const currentValues = this._valuesByUnitId.get(unitId) || {}
    const availableIds = new Set(
      config.visible
        ? config.conditions
          .map(condition => condition.id)
        : []
    )
    const nextValues = Object.fromEntries(
      Object.entries(currentValues).filter(([conditionId]) => availableIds.has(conditionId))
    )
    this._valuesByUnitId.set(unitId, nextValues)
    return { ...nextValues }
  }

  getAffectedPluginIds(unitId: string): string[] {
    const config = this._filterInstanceService.get(unitId)
    const pluginIds = config.conditions.flatMap(condition => [
      ...condition.linkedFields.map(field => field.pluginId),
      ...(condition.treeLevelMappings || []).flatMap(mapping =>
        mapping.linkedFields.map(field => field.pluginId)
      )
    ])

    return [...new Set(pluginIds.filter(Boolean))]
  }

  buildQueryFilter(unitId: string, pluginId: string): SpreadsheetQueryFilterItem[] {
    const config = this._filterInstanceService.get(unitId)
    if (!config.visible) {
      return []
    }
    const values = this._valuesByUnitId.get(unitId) || {}
    return config.conditions.flatMap(condition =>
      this._buildConditionItems(condition, pluginId, values[condition.id])
    )
  }

  applyQueryFilterToConfig<T extends { data?: { queryFilter?: SpreadsheetQueryFilterItem[] } }>(
    unitId: string,
    config: T
  ): T {
    const queryFilter = this.buildQueryFilter(unitId, (config as any).id)

    return {
      ...config,
      data: {
        ...config.data,
        queryFilter
      }
    } as T
  }

  private _buildConditionItems(
    condition: SpreadsheetFilterCondition,
    pluginId: string,
    rawValue: unknown
  ): SpreadsheetQueryFilterItem[] {
    if (this._isEmptyValue(rawValue)) {
      return []
    }

    if (condition.displayType === 'treeSelect') {
      const customFilter = buildSpreadsheetTreeFilter(condition, pluginId, rawValue)
      if (!customFilter) return []
      return [{
        filterId: condition.id,
        fieldId: '',
        operator: '',
        value: [],
        parameters: [],
        isTree: true,
        customFilter
      }]
    }

    const linkedFields = condition.linkedFields.filter(field =>
      field.pluginId === pluginId && field.fieldId !== undefined && field.fieldId !== null && field.fieldId !== ''
    )
    if (!linkedFields.length) {
      return []
    }

    if (condition.displayType === 'textSearch') {
      return linkedFields.flatMap(field => {
        const customFilter = buildSpreadsheetTextSearchFilter(condition, field, rawValue)
        if (!customFilter) return []
        return [{
          filterId: condition.id,
          fieldId: String(field.fieldId),
          operator: '',
          value: [],
          parameters: [],
          isTree: false,
          customFilter
        }]
      })
    }

    const normalizedValue = normalizeTimeQueryValue(condition, rawValue)
    const values = (Array.isArray(normalizedValue) ? normalizedValue : [normalizedValue])
      .filter(value => !this._isEmptyValue(value))
      .map(value => String(value))
    if (!values.length) {
      return []
    }
    const operator = this._getOperator(condition)

    return linkedFields.map(field => ({
      filterId: condition.id,
      fieldId: String(field.fieldId),
      operator,
      value: values,
      parameters: [],
      isTree: false
    }))
  }

  private _getOperator(condition: SpreadsheetFilterCondition): string {
    if (condition.displayType === 'textSearch') {
      return 'like'
    }
    if (['numberRange', 'time', 'timeRange'].includes(condition.displayType)) {
      return 'between'
    }
    return 'in'
  }

  private _isEmptyValue(value: unknown): boolean {
    if (Array.isArray(value)) {
      return value.length === 0 || value.every(item => this._isEmptyValue(item))
    }

    return value === undefined || value === null || value === ''
  }
}
