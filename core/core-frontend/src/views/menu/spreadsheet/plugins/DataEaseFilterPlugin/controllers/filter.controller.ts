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
import { h, render } from 'vue'
import { SpreadsheetModeService } from '../../../services/spreadsheet-mode.service'
import { getDsDetailsWithPerm, type DatasetDetail } from '@/api/dataset'
import { cloneDeep } from 'lodash-es'
import {
  getSpreadsheetFilterInitialValues,
  resolveSpreadsheetFilterValues,
  type SpreadsheetFilterValueMap
} from '../utils/filter-values'

const SPREADSHEET_QUERY_BAR_COMPONENT = 'SpreadsheetQueryBar'
const { emitter } = useEmitt()

export class DataEaseFilterController extends Disposable {
  private readonly _valuesReadyResolvers = new Map<string, () => void>()

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
    const { createElement, useEffect, useRef } = this._componentManager.reactUtils
    const QueryBarHost = () => {
      const containerRef = useRef<HTMLElement | null>(null)
      useEffect(() => {
        const container = containerRef.current
        if (!container) {
          return
        }
        // Univer 的通用 Vue 适配器会在 React props 引用变化时重挂载组件；
        // 查询栏不依赖这些 props，保持同一个 Vue 实例可避免初始化 options 被重复加载。
        render(h(SpreadsheetQueryBar, {
          mode,
          isInitialValuesPending: () => this._getPendingValuesUnitId() !== undefined,
          onInitialValuesReady: values => this._handleValuesReady(values)
        }), container)
        return () => render(null, container)
      }, [])
      return createElement('div', { ref: containerRef })
    }

    this.disposeWithMe(
      this._componentManager.register('DataEaseQueryControlIcon', DataEaseQueryControlIcon, {
        framework: 'vue3'
      })
    )
    this.disposeWithMe(
      this._componentManager.register(SPREADSHEET_QUERY_BAR_COMPONENT, QueryBarHost)
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
          const valuesReady = this._createValuesReady(unitId, config)
          this._spreadsheetFilterRuntimeService.setValuesReady(unitId, valuesReady)
          dispatchSpreadsheetFilterVisibleChange(config.visible)
          dispatchSpreadsheetFilterConfigChange(config)
          // 首次查询由明细表、透视表在默认值就绪后统一执行，资源加载本身不广播刷新。
        },
        onUnLoad: unitId => {
          this._resolveValuesReady(unitId)
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
    const normalizedConfig = this._filterInstanceService.setConfigForUnit(unitId, payload.config)
    // 与仪表板一致，保存查询组件后不沿用运行态选择：有默认值恢复默认值，无默认值清空。
    const values = await resolveSpreadsheetFilterValues(normalizedConfig, {}, false)
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

  private _createValuesReady(unitId: string, config: SpreadsheetFilterConfig): Promise<void> {
    this._resolveValuesReady(unitId)
    const requiresOptions = config.visible && config.conditions.some(condition =>
      condition.visible &&
      condition.defaultValueEnabled &&
      condition.defaultValueFirstItem &&
      ['textSelect', 'numberSelect', 'treeSelect'].includes(condition.displayType)
    )
    if (!requiresOptions) {
      return Promise.resolve()
    }

    return new Promise<void>(resolve => {
      this._valuesReadyResolvers.set(unitId, resolve)
    })
  }

  private _handleValuesReady(values: SpreadsheetFilterValueMap): void {
    const unitId = this._getPendingValuesUnitId()
    if (!unitId) {
      return
    }

    // QueryBar 已持有相同的值，这里只更新资源状态，不再反向广播配置变化。
    const config = this._filterInstanceService.get(unitId)
    this._filterInstanceService.setConfigForUnit(
      unitId,
      this._withSelectValues(config, values)
    )
    this._spreadsheetFilterRuntimeService.setValues(unitId, values)
    this._resolveValuesReady(unitId)
  }

  private _getPendingValuesUnitId(): string | undefined {
    const unitId = this._getCurrentUnitId()
    if (unitId && this._valuesReadyResolvers.has(unitId)) {
      return unitId
    }
    if (this._valuesReadyResolvers.size === 1) {
      return this._valuesReadyResolvers.keys().next().value
    }
    return undefined
  }

  private _resolveValuesReady(unitId: string): void {
    const resolve = this._valuesReadyResolvers.get(unitId)
    if (!resolve) {
      return
    }
    this._valuesReadyResolvers.delete(unitId)
    resolve()
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
      ...detailPlugins.map(plugin =>
        this._buildAvailablePlugin(unitId, plugin, 'detail', datasetMap)
      ),
      ...pivotPlugins.map(plugin =>
        this._buildAvailablePlugin(unitId, plugin, 'pivot', datasetMap)
      )
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
    unitId: string,
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
        ? this._getDetailPluginName(unitId, plugin as DetailTableConfig)
        : this._getPivotPluginName(unitId, plugin as PivotTableConfig),
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

  private _getDetailPluginName(unitId: string, plugin: DetailTableConfig): string {
    const sheetName = this._getCurrentSheetName(unitId, plugin)
    const startCell = plugin.placement?.startCell || 'A1'
    const defaultName = `${sheetName}!${startCell}`
    return plugin.style?.base?.customBlockName
      ? plugin.style.base.blockName || defaultName
      : defaultName
  }

  private _getPivotPluginName(unitId: string, plugin: PivotTableConfig): string {
    const sheetName = this._getCurrentSheetName(unitId, plugin)
    const startCell = plugin.placement?.startCell || 'A1'
    const defaultName = `${sheetName}!${startCell}`
    return plugin.style?.base?.customBlockName
      ? plugin.style.base.blockName || defaultName
      : defaultName
  }

  private _getCurrentSheetName(
    unitId: string,
    plugin: DetailTableConfig | PivotTableConfig
  ): string {
    const sheetId = plugin.placement?.sheetId
    const workbook = this._univerInstanceService.getUnit(
      unitId,
      UniverInstanceType.UNIVER_SHEET
    )
    const worksheet = sheetId ? workbook?.getSheetBySheetId(sheetId) : undefined

    // 默认区块名始终跟随当前 Sheet 名，避免使用实例创建时保存的旧名称。
    return worksheet?.getName?.() || plugin.placement?.sheetName || sheetId || 'Sheet'
  }

  private _mergeIds(...idsList: string[][]): string[] {
    return [...new Set(idsList.flat().filter(Boolean))]
  }
}
