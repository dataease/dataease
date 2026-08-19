import {
  Disposable,
  ICommandService,
  Inject,
  Injector,
  IResourceManagerService,
  IUniverInstanceService,
  UniverInstanceType
} from '@univerjs/core'
import { SetWorksheetActiveOperation } from '@univerjs/sheets'
import {
  BuiltInUIPart,
  ComponentManager,
  IMenuManagerService,
  ISidebarService,
  IUIPartsService,
  RibbonStartGroup
} from '@univerjs/ui'
import SpreadsheetQueryBar from '../components/SpreadsheetQueryBar.vue'
import DataEaseQueryControlIcon from '../components/DataEaseQueryControlIcon.vue'
import { ToggleSpreadsheetFilterOperation } from '../commands/operations'
import { ToggleSpreadsheetFilterMenuFactory } from './menu'
import { FILTER_PLUGIN_RESOURCE_NAME } from '../../../utils/plugin-resource'
import { FilterInstanceService } from '../services/filter-instance.service'
import { SpreadsheetFilterRuntimeService } from '../services/filter-runtime.service'
import type { SpreadsheetFilterConfig } from '../../../types/plugin'
import { DetailTableInstanceService } from '../../DataEaseDetailTablePlugin/services/detail-table-instance.service'
import type { DetailTableConfig } from '../../DataEaseDetailTablePlugin/types'
import { PivotTableInstanceService } from '../../DataEasePivotTablePlugin/services/pivot-table-instance.service'
import type { PivotTableConfig } from '../../DataEasePivotTablePlugin/types'
import {
  dispatchSpreadsheetFilterConfigChange,
  dispatchSpreadsheetFilterQuery,
  dispatchSpreadsheetFilterVisibleChange,
  offSpreadsheetFilterDeleteCondition,
  offSpreadsheetFilterDisable,
  offSpreadsheetFilterOpenStyle,
  offSpreadsheetFilterRequestConfigContext,
  offSpreadsheetFilterSaveConfig,
  onSpreadsheetFilterDeleteCondition,
  onSpreadsheetFilterDisable,
  onSpreadsheetFilterOpenStyle,
  onSpreadsheetFilterRequestConfigContext,
  onSpreadsheetFilterSaveConfig,
  resetSpreadsheetFilterEventState,
  type SpreadsheetFilterAvailableField,
  type SpreadsheetFilterAvailablePlugin,
  type SpreadsheetFilterConfigContextRequestPayload,
  type SpreadsheetFilterOpenStylePayload,
  type SpreadsheetFilterSaveConfigPayload
} from '../utils/events'
import { useEmitt } from '@/hooks/web/useEmitt'
import { SPREADSHEET_EVENTS } from '../../../utils/events'
import { defineComponent, h } from 'vue'
import { SpreadsheetModeService } from '../../../services/spreadsheet-mode.service'
import { getDsDetailsWithPerm, type DatasetDetail } from '@/api/dataset'
import { cloneDeep, isEqual } from 'lodash-es'
import {
  getSpreadsheetFilterInitialValues,
  resolveSpreadsheetFilterValues,
  type SpreadsheetFilterValueMap
} from '../utils/filter-values'

const SPREADSHEET_QUERY_BAR_COMPONENT = 'SpreadsheetQueryBar'
const { emitter } = useEmitt()

export class DataEaseFilterController extends Disposable {
  constructor(
    @Inject(Injector) private readonly _injector: Injector,
    @ICommandService private readonly _commandService: ICommandService,
    @IMenuManagerService private readonly _menuManagerService: IMenuManagerService,
    @ISidebarService private readonly _sidebarService: ISidebarService,
    @IUIPartsService private readonly _uiPartsService: IUIPartsService,
    @Inject(ComponentManager) private readonly _componentManager: ComponentManager,
    @Inject(IResourceManagerService) private readonly _resourceManagerService: IResourceManagerService,
    @Inject(IUniverInstanceService)
    private readonly _univerInstanceService: IUniverInstanceService,
    @Inject(FilterInstanceService) private readonly _filterInstanceService: FilterInstanceService,
    @Inject(SpreadsheetFilterRuntimeService)
    private readonly _spreadsheetFilterRuntimeService: SpreadsheetFilterRuntimeService,
    @Inject(SpreadsheetModeService)
    private readonly _spreadsheetModeService: SpreadsheetModeService
  ) {
    super()
    resetSpreadsheetFilterEventState()
    this._initCommands()
    this._initComponents()
    this._initMenus()
    this._initEventListeners()
    this._initSheetSwitchListener()
    this._initResourceHook()
    this._initUIParts()
  }

  private _initCommands(): void {
    this.disposeWithMe(this._commandService.registerCommand(ToggleSpreadsheetFilterOperation))
  }

  private _initSheetSwitchListener(): void {
    this.disposeWithMe(
      this._commandService.beforeCommandExecuted(commandInfo => {
        if (commandInfo.id !== SetWorksheetActiveOperation.id) {
          return
        }

        const { unitId, subUnitId } = commandInfo.params || {}
        const workbook = unitId
          ? this._univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
          : undefined
        const currentSheetId = workbook?.getActiveSheet()?.getSheetId()
        if (!subUnitId || !workbook?.getSheetBySheetId(subUnitId) || currentSheetId === subUnitId) {
          return
        }

        emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
      })
    )
  }

  private _initComponents(): void {
    const mode = this._spreadsheetModeService.getMode()
    const queryBar = defineComponent({
      name: 'SpreadsheetQueryBarHost',
      setup: () => () => h(SpreadsheetQueryBar, { mode })
    })

    this.disposeWithMe(
      this._componentManager.register('DataEaseQueryControlIcon', DataEaseQueryControlIcon, {
        framework: 'vue3'
      })
    )
    this.disposeWithMe(
      this._componentManager.register(SPREADSHEET_QUERY_BAR_COMPONENT, queryBar, {
        framework: 'vue3'
      })
    )
  }

  private _initMenus(): void {
    this._menuManagerService.mergeMenu({
      [RibbonStartGroup.OTHERS]: {
        [ToggleSpreadsheetFilterOperation.id]: {
          order: 20,
          menuItemFactory: ToggleSpreadsheetFilterMenuFactory
        }
      }
    })
  }

  private _initResourceHook(): void {
    this.disposeWithMe(
      this._resourceManagerService.registerPluginResource<SpreadsheetFilterConfig>({
        pluginName: FILTER_PLUGIN_RESOURCE_NAME,
        businesses: [UniverInstanceType.UNIVER_SHEET],
        onLoad: (unitId, resource) => {
          const loadedConfig = this._filterInstanceService.set(unitId, resource)
          const values = getSpreadsheetFilterInitialValues(loadedConfig)
          const config = this._filterInstanceService.setConfigForUnit(
            unitId,
            this._withSelectValues(loadedConfig, values)
          )
          this._spreadsheetFilterRuntimeService.setValues(unitId, values)
          dispatchSpreadsheetFilterVisibleChange(config.visible)
          dispatchSpreadsheetFilterConfigChange(config)
          const valuesReady = this._resolveLoadedConfigValues(unitId, config, values)
          this._spreadsheetFilterRuntimeService.setValuesReady(unitId, valuesReady)
          dispatchSpreadsheetFilterQuery({ unitId, config, values })
        },
        onUnLoad: unitId => {
          this._filterInstanceService.delete(unitId)
          this._filterInstanceService.setVisible(false)
          this._spreadsheetFilterRuntimeService.clearValues(unitId)
          resetSpreadsheetFilterEventState()
          dispatchSpreadsheetFilterVisibleChange(false)
        },
        toJson: unitId => JSON.stringify(this._filterInstanceService.get(unitId)),
        parseJson: data => JSON.parse(data) as SpreadsheetFilterConfig
      })
    )
  }

  private _initEventListeners(): void {
    const requestConfigContext = (payload: SpreadsheetFilterConfigContextRequestPayload) =>
      this._requestConfigContext(payload)
    const saveConfig = (payload: SpreadsheetFilterSaveConfigPayload) => this._saveConfig(payload)
    const deleteCondition = (conditionId: string) => this._deleteCondition(conditionId)
    const disableFilter = () => this._disableFilter()
    const openStyle = (payload?: SpreadsheetFilterOpenStylePayload) =>
      this._openStylePanel(payload)
    onSpreadsheetFilterRequestConfigContext(requestConfigContext)
    onSpreadsheetFilterSaveConfig(saveConfig)
    onSpreadsheetFilterDeleteCondition(deleteCondition)
    onSpreadsheetFilterDisable(disableFilter)
    onSpreadsheetFilterOpenStyle(openStyle)
    this.disposeWithMe({
      dispose: () => {
        offSpreadsheetFilterRequestConfigContext(requestConfigContext)
        offSpreadsheetFilterSaveConfig(saveConfig)
        offSpreadsheetFilterDeleteCondition(deleteCondition)
        offSpreadsheetFilterDisable(disableFilter)
        offSpreadsheetFilterOpenStyle(openStyle)
      }
    })
  }

  private _initUIParts(): void {
    this.disposeWithMe(
      this._uiPartsService.registerComponent(BuiltInUIPart.TOOLBAR, () =>
        this._componentManager.get(SPREADSHEET_QUERY_BAR_COMPONENT)
      )
    )
  }

  private async _requestConfigContext(payload: SpreadsheetFilterConfigContextRequestPayload): Promise<void> {
    if (this._spreadsheetModeService.isPreview()) {
      return
    }
    const unitId = this._getCurrentUnitId()
    if (!unitId) {
      return
    }

    const config = this._filterInstanceService.get(unitId)
    const availablePlugins = await this._getAvailablePlugins(unitId)

    payload.onReady({
      config,
      availablePlugins,
      selectedConditionId: payload?.selectedConditionId,
      initialAction: payload?.initialAction
    })
  }

  private async _saveConfig(payload: SpreadsheetFilterSaveConfigPayload): Promise<void> {
    if (this._spreadsheetModeService.isPreview()) {
      return
    }
    const unitId = this._getCurrentUnitId()
    if (!unitId) {
      return
    }
    const affectedPluginIds = this._spreadsheetFilterRuntimeService.getAffectedPluginIds(unitId)
    const currentValues = this._spreadsheetFilterRuntimeService.getValues(unitId)
    const normalizedConfig = this._filterInstanceService.setConfigForUnit(unitId, payload.config)
    const values = await resolveSpreadsheetFilterValues(normalizedConfig, currentValues)
    if (this._filterInstanceService.get(unitId) !== normalizedConfig) {
      return
    }
    const savedConfig = this._filterInstanceService.setConfigForUnit(
      unitId,
      this._withSelectValues(normalizedConfig, values)
    )
    this._spreadsheetFilterRuntimeService.setValues(unitId, values)
    const nextAffectedPluginIds = this._spreadsheetFilterRuntimeService.getAffectedPluginIds(unitId)
    dispatchSpreadsheetFilterConfigChange(savedConfig)
    dispatchSpreadsheetFilterVisibleChange(savedConfig.visible)
    dispatchSpreadsheetFilterQuery({
      unitId,
      config: savedConfig,
      values,
      affectedPluginIds: this._mergeIds(affectedPluginIds, nextAffectedPluginIds)
    })
  }

  private async _resolveLoadedConfigValues(
    unitId: string,
    loadedConfig: SpreadsheetFilterConfig,
    initialValues: SpreadsheetFilterValueMap
  ): Promise<void> {
    const values = await resolveSpreadsheetFilterValues(loadedConfig)
    if (this._filterInstanceService.get(unitId) !== loadedConfig || isEqual(values, initialValues)) {
      return
    }
    const config = this._filterInstanceService.setConfigForUnit(
      unitId,
      this._withSelectValues(loadedConfig, values)
    )
    this._spreadsheetFilterRuntimeService.setValues(unitId, values)
    dispatchSpreadsheetFilterConfigChange(config)
  }

  private _withSelectValues(
    config: SpreadsheetFilterConfig,
    values: SpreadsheetFilterValueMap
  ): SpreadsheetFilterConfig {
    return {
      ...config,
      conditions: config.conditions.map(condition => ({
        ...condition,
        selectValue: cloneDeep(values[condition.id])
      }))
    }
  }

  private _deleteCondition(conditionId: string): void {
    if (this._spreadsheetModeService.isPreview()) {
      return
    }
    const unitId = this._getCurrentUnitId()
    if (!unitId) {
      return
    }
    const affectedPluginIds = this._spreadsheetFilterRuntimeService.getAffectedPluginIds(unitId)
    let savedConfig = this._filterInstanceService.removeConditionForUnit(unitId, conditionId)
    const nextAffectedPluginIds = this._spreadsheetFilterRuntimeService.getAffectedPluginIds(unitId)
    const values = this._spreadsheetFilterRuntimeService.pruneValuesForConfig(unitId, savedConfig)
    savedConfig = this._filterInstanceService.setConfigForUnit(
      unitId,
      this._withSelectValues(savedConfig, values)
    )
    dispatchSpreadsheetFilterConfigChange(savedConfig)
    dispatchSpreadsheetFilterVisibleChange(savedConfig.visible)
    dispatchSpreadsheetFilterQuery({
      unitId,
      config: savedConfig,
      values,
      affectedPluginIds: this._mergeIds(affectedPluginIds, nextAffectedPluginIds)
    })
  }

  private _disableFilter(): void {
    if (this._spreadsheetModeService.isPreview()) {
      return
    }
    const unitId = this._getCurrentUnitId()
    if (!unitId) {
      dispatchSpreadsheetFilterVisibleChange(false)
      return
    }
    const savedConfig = this._filterInstanceService.setVisibleForUnit(unitId, false)
    this._spreadsheetFilterRuntimeService.clearValues(unitId)
    dispatchSpreadsheetFilterConfigChange(savedConfig)
    dispatchSpreadsheetFilterVisibleChange(false)
    dispatchSpreadsheetFilterQuery({
      unitId,
      config: savedConfig,
      values: {}
    })
  }

  private _openStylePanel(payload?: SpreadsheetFilterOpenStylePayload): void {
    if (this._spreadsheetModeService.isPreview()) {
      return
    }
    // 空白区域属于自动打开入口，侧边栏展开时不抢占当前面板，与表格插件保持一致。
    if (payload?.skipWhenSidebarVisible && this._sidebarService.visible) {
      return
    }
    const unitId = this._getCurrentUnitId()
    if (!unitId) {
      return
    }
    const config = this._filterInstanceService.get(unitId)
    emitter.emit(SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR, {
      config,
      isNewSheet: false
    })
  }

  private _getCurrentUnitId(): string | undefined {
    const workbook = this._univerInstanceService.getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET)
    return (workbook as any)?.getUnitId?.() || (workbook as any)?.getId?.()
  }

  private async _getAvailablePlugins(unitId: string): Promise<SpreadsheetFilterAvailablePlugin[]> {
    const detailTableInstanceService = this._getDetailTableInstanceService()
    const pivotTableInstanceService = this._getPivotTableInstanceService()
    const detailPlugins = detailTableInstanceService?.get(unitId) || []
    const pivotPlugins = pivotTableInstanceService?.get(unitId) || []
    const datasetIds = [
      ...detailPlugins.map(plugin => plugin.data?.datasetId),
      ...pivotPlugins.map(plugin => plugin.data?.datasetId)
    ].filter((id): id is string | number => id !== undefined && id !== null && id !== '')
    const datasetMap = new Map<string, DatasetDetail>()

    if (datasetIds.length) {
      try {
        const details = await getDsDetailsWithPerm([...new Set(datasetIds)])
        details?.forEach((detail: DatasetDetail) => datasetMap.set(String(detail.id), detail))
      } catch {
        // Keep plugin rows visible even when their dataset fields cannot be loaded.
      }
    }

    return [
      ...detailPlugins.map(plugin => this._buildAvailablePlugin(plugin, 'detail', datasetMap)),
      ...pivotPlugins.map(plugin => this._buildAvailablePlugin(plugin, 'pivot', datasetMap))
    ]
  }

  private _getDetailTableInstanceService(): DetailTableInstanceService | undefined {
    try {
      return this._injector.get(DetailTableInstanceService)
    } catch (error) {
      return undefined
    }
  }

  private _getPivotTableInstanceService(): PivotTableInstanceService | undefined {
    try {
      return this._injector.get(PivotTableInstanceService)
    } catch (error) {
      return undefined
    }
  }

  private _buildAvailablePlugin(
    plugin: DetailTableConfig | PivotTableConfig,
    pluginType: 'detail' | 'pivot',
    datasetMap: Map<string, DatasetDetail>
  ): SpreadsheetFilterAvailablePlugin {
    const datasetId = plugin.data?.datasetId
    const dataset = datasetId === undefined ? undefined : datasetMap.get(String(datasetId))
    const fields: SpreadsheetFilterAvailableField[] = [
      ...(dataset?.fields?.dimensionList || []).map(field => ({
        fieldId: field.id,
        fieldName: field.name || field.dataeaseName,
        groupType: 'd' as const,
        deType: field.deType,
        desensitized: field.desensitized
      })),
      ...(dataset?.fields?.quotaList || []).map(field => ({
        fieldId: field.id,
        fieldName: field.name || field.dataeaseName,
        groupType: 'q' as const,
        deType: field.deType,
        desensitized: field.desensitized
      }))
    ]

    return {
      pluginId: plugin.id,
      pluginName: pluginType === 'detail'
        ? this._getDetailPluginName(plugin as DetailTableConfig)
        : this._getPivotPluginName(plugin as PivotTableConfig),
      pluginType,
      datasetId,
      datasetName: dataset?.name || this._getDatasetName(plugin as DetailTableConfig),
      fields
    }
  }

  private _getDatasetName(plugin: DetailTableConfig): string {
    const data = plugin.data as any
    return data?.datasetName || data?.dataset?.name || (plugin.data?.datasetId ? `数据集 ${plugin.data.datasetId}` : '-')
  }

  private _getDetailPluginName(plugin: DetailTableConfig): string {
    const sheetName = plugin.placement?.sheetName || plugin.placement?.sheetId || 'Sheet'
    const startCell = plugin.placement?.startCell || 'A1'
    const defaultName = `${sheetName}!${startCell}`
    return plugin.style?.base?.customBlockName
      ? plugin.style.base.blockName || defaultName
      : defaultName
  }

  private _getPivotPluginName(plugin: PivotTableConfig): string {
    const sheetName = plugin.placement?.sheetName || plugin.placement?.sheetId || 'Sheet'
    const startCell = plugin.placement?.startCell || 'A1'
    const defaultName = `${sheetName}!${startCell}`
    return plugin.style?.base?.customBlockName
      ? plugin.style.base.blockName || defaultName
      : defaultName
  }

  private _mergeIds(...idsList: string[][]): string[] {
    return [...new Set(idsList.flat().filter(Boolean))]
  }
}
