import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterConfig,
  SpreadsheetFilterStyle
} from '../../../types/plugin'
import { normalizeSpreadsheetFilterDisplayType } from '../utils/filter-condition-rules'

type VisibleListener = (visible: boolean) => void

class VisibleObservable {
  private readonly _listeners = new Set<VisibleListener>()

  constructor(private readonly _getValue: () => boolean) {}

  subscribe(listener: VisibleListener) {
    this._listeners.add(listener)
    listener(this._getValue())

    return {
      unsubscribe: () => {
        this._listeners.delete(listener)
      }
    }
  }

  notify(visible: boolean): void {
    this._listeners.forEach(listener => listener(visible))
  }
}

const createDefaultFilterConfig = (): SpreadsheetFilterConfig => ({
  id: 'spreadsheet_filter',
  type: 'filter',
  visible: false,
  conditions: [],
  style: createDefaultFilterStyle()
})

export const createDefaultFilterStyle = (): SpreadsheetFilterStyle => ({
  base: {
    paddingMode: 'uniform',
    padding: {
      top: 16,
      right: 16,
      bottom: 16,
      left: 16
    },
    radius: 0,
    backgroundEnabled: false,
    backgroundColor: '#ffffff',
    layout: 'horizontal',
    align: 'left',
    gap: 16
  },
  condition: {
    fillEnabled: false,
    fillColor: '#ffffff',
    borderEnabled: false,
    borderColor: '#d9dcdf',
    borderWidth: 1,
    color: '#1f2329',
    fontSize: 12,
    fontWeight: 'normal',
    fontStyle: 'normal'
  },
  conditionName: {
    show: true,
    position: 'top',
    color: '#1f2329',
    fontSize: 12,
    fontWeight: 'normal',
    fontStyle: 'normal',
    gap: 8
  },
  button: {
    btnList: ['sure'],
    primaryColor: '#3370ff',
    textColor: '#ffffff',
    fontSize: 12,
    fontWeight: 'normal',
    fontStyle: 'normal'
  }
})

const normalizeFilterStyle = (style?: Partial<SpreadsheetFilterStyle> | null): SpreadsheetFilterStyle => {
  const defaultStyle = createDefaultFilterStyle()

  return {
    base: {
      ...defaultStyle.base,
      ...style?.base,
      padding: {
        ...defaultStyle.base.padding,
        ...style?.base?.padding
      }
    },
    condition: {
      ...defaultStyle.condition,
      ...style?.condition
    },
    conditionName: {
      ...defaultStyle.conditionName,
      ...style?.conditionName
    },
    button: {
      ...defaultStyle.button,
      ...style?.button
    }
  }
}

const normalizeCondition = (
  condition: Partial<SpreadsheetFilterCondition>,
  index: number
): SpreadsheetFilterCondition => ({
  id: condition.id || `condition_${Date.now()}_${index}`,
  name: condition.name || `查询条件${index + 1}`,
  visible: condition.visible !== false,
  required: !!condition.required,
  displayType: normalizeSpreadsheetFilterDisplayType(condition.displayType),
  optionSource: condition.optionSource || 'auto',
  optionDatasetId: condition.optionDatasetId,
  optionDatasetName: condition.optionDatasetName,
  queryFieldId: condition.queryFieldId,
  queryFieldName: condition.queryFieldName,
  displayFieldId: condition.displayFieldId,
  displayFieldName: condition.displayFieldName,
  sortFieldId: condition.sortFieldId,
  sortFieldName: condition.sortFieldName,
  sortType: condition.sortType,
  manualOptions: Array.isArray(condition.manualOptions) ? condition.manualOptions : [],
  displayForm: condition.displayForm || 'dropdown',
  optionCountMode: condition.optionCountMode || 'default',
  queryMode: condition.queryMode || 'click',
  multiple: !!condition.multiple,
  defaultValueEnabled: !!condition.defaultValueEnabled,
  defaultValueFirstItem: !!condition.defaultValueFirstItem,
  showRule: condition.showRule || 'always',
  defaultValue: condition.defaultValue,
  selectValue: condition.selectValue,
  textSearchConditionType: condition.textSearchConditionType || 'single',
  hideTextSearchConditionSwitch: !!condition.hideTextSearchConditionSwitch,
  textSearchDefaultClauses: condition.textSearchDefaultClauses?.length
    ? condition.textSearchDefaultClauses.slice(0, 2)
    : [
      { operator: 'eq', value: '' },
      { operator: 'like', value: '' }
    ],
  treeDatasetId: condition.treeDatasetId,
  treeDatasetName: condition.treeDatasetName,
  treeFields: Array.isArray(condition.treeFields)
    ? condition.treeFields.slice(0, 5).map((field, order) => ({ ...field, order }))
    : [],
  treeLevelMappings: Array.isArray(condition.treeLevelMappings)
    ? condition.treeLevelMappings
    : [],
  timeGranularity: condition.timeGranularity || 'date',
  timeRangeGranularity: condition.timeRangeGranularity || 'daterange',
  timeDefaultType: condition.timeDefaultType === 'dynamic' ? 'dynamic' : 'fixed',
  timeDynamicDefault: condition.timeDynamicDefault || {
    offset: { value: 0, unit: 'day', direction: 'before', relativeToCurrent: 'custom' }
  },
  timeRangeDynamicDefault: condition.timeRangeDynamicDefault || {
    start: { value: 7, unit: 'day', direction: 'before', relativeToCurrent: 'custom' },
    end: { value: 0, unit: 'day', direction: 'before', relativeToCurrent: 'custom' }
  },
  timeFilterRangeEnabled: !!condition.timeFilterRangeEnabled,
  timeFilterRange: condition.timeFilterRange || { intervalType: 'none' },
  linkedFields: Array.isArray(condition.linkedFields) ? condition.linkedFields : []
})

export class FilterInstanceService {
  private _visible = false
  private readonly _configsByUnitId = new Map<string, SpreadsheetFilterConfig>()
  private readonly _visibleObservable = new VisibleObservable(() => this._visible)

  readonly visible$ = this._visibleObservable as any

  get visible(): boolean {
    return this._visible
  }

  setVisible(visible: boolean): void {
    this._visible = visible
    this._visibleObservable.notify(visible)
  }

  get(unitId: string): SpreadsheetFilterConfig {
    return this._configsByUnitId.get(unitId) || createDefaultFilterConfig()
  }

  set(unitId: string, config?: Partial<SpreadsheetFilterConfig> | null): SpreadsheetFilterConfig {
    const normalized = this.normalize(config)
    this._configsByUnitId.set(unitId, normalized)
    this.setVisible(normalized.visible)
    return normalized
  }

  delete(unitId: string): void {
    this._configsByUnitId.delete(unitId)
  }

  setVisibleForUnit(unitId: string, visible: boolean): SpreadsheetFilterConfig {
    const nextConfig = {
      ...this.get(unitId),
      visible
    }
    this._configsByUnitId.set(unitId, nextConfig)
    this.setVisible(visible)
    return nextConfig
  }

  setConfigForUnit(unitId: string, config: SpreadsheetFilterConfig): SpreadsheetFilterConfig {
    const current = this.get(unitId)
    const nextConfig = this.normalize({
      ...current,
      ...config
    })
    this._configsByUnitId.set(unitId, nextConfig)
    this.setVisible(nextConfig.visible)
    return nextConfig
  }

  removeConditionForUnit(unitId: string, conditionId: string): SpreadsheetFilterConfig {
    const current = this.get(unitId)
    const nextConfig = this.normalize({
      ...current,
      conditions: current.conditions.filter(condition => condition.id !== conditionId)
    })
    this._configsByUnitId.set(unitId, nextConfig)
    this.setVisible(nextConfig.visible)
    return nextConfig
  }

  toggleVisible(): boolean {
    const nextVisible = !this.visible
    this.setVisible(nextVisible)
    return nextVisible
  }

  dispose(): void {
    this._visible = false
    this._configsByUnitId.clear()
  }

  private normalize(config?: Partial<SpreadsheetFilterConfig> | null): SpreadsheetFilterConfig {
    const defaultConfig = createDefaultFilterConfig()
    const conditions = Array.isArray(config?.conditions) ? config.conditions : []

    return {
      ...defaultConfig,
      ...config,
      id: config?.id || defaultConfig.id,
      type: 'filter',
      visible: !!config?.visible,
      conditions: conditions.map(normalizeCondition),
      style: normalizeFilterStyle(config?.style)
    }
  }
}
