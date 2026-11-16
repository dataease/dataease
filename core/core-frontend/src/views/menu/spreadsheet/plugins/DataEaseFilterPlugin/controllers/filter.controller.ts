import {
  Disposable,
  ICommandService,
  Inject,
  Injector,
  IResourceManagerService,
  IUniverInstanceService,
  LocaleService,
  UniverInstanceType
} from '@univerjs/core'
import { SetWorksheetActiveOperation } from '@univerjs/sheets'
import {
  BuiltInUIPart,
  ComponentManager,
  IconManager,
  IMenuManagerService,
  ISidebarService,
  IUIPartsService,
  RibbonStartGroup
} from '@univerjs/ui'
import SpreadsheetQueryBar from '../components/SpreadsheetQueryBar.vue'
import DataEaseQueryControlIcon from '../components/DataEaseQueryControlIcon.vue'
import {
  createToggleSpreadsheetFilterOperation,
  TOGGLE_SPREADSHEET_FILTER_OPERATION_ID
} from '../commands/operations'
import { ToggleSpreadsheetFilterMenuFactory } from './menu'
import { FILTER_PLUGIN_RESOURCE_NAME } from '../../../utils/plugin-resource'
import { FilterInstanceService } from '../services/filter-instance.service'
import { SpreadsheetFilterRuntimeService } from '../services/filter-runtime.service'
import { FilterConfigContextService } from '../services/filter-config-context.service'
import { SpreadsheetOuterParamsService } from '../../DataEaseOuterParamsPlugin/services/outer-params.service'
import type { SpreadsheetFilterConfig } from '../../../types/plugin'
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
  type SpreadsheetFilterConfigContextRequestPayload,
  type SpreadsheetFilterOpenStylePayload,
  type SpreadsheetFilterSaveConfigPayload
} from '../utils/events'
import { useEmitt } from '@/hooks/web/useEmitt'
import { SPREADSHEET_EVENTS } from '../../../utils/events'
import { h, render } from 'vue'
import { SpreadsheetModeService } from '../../../services/spreadsheet-mode.service'
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
  private readonly _pendingInitialConfigs = new Map<string, SpreadsheetFilterConfig>()

  constructor(
    @Inject(Injector) private readonly _injector: Injector,
    @ICommandService private readonly _commandService: ICommandService,
    @IMenuManagerService private readonly _menuManagerService: IMenuManagerService,
    @ISidebarService private readonly _sidebarService: ISidebarService,
    @IUIPartsService private readonly _uiPartsService: IUIPartsService,
    @Inject(ComponentManager) private readonly _componentManager: ComponentManager,
    @Inject(IconManager) private readonly _iconManager: IconManager,
    @Inject(IResourceManagerService) private readonly _resourceManagerService: IResourceManagerService,
    @Inject(IUniverInstanceService)
    private readonly _univerInstanceService: IUniverInstanceService,
    @Inject(FilterInstanceService) private readonly _filterInstanceService: FilterInstanceService,
    @Inject(SpreadsheetFilterRuntimeService)
    private readonly _spreadsheetFilterRuntimeService: SpreadsheetFilterRuntimeService,
    @Inject(FilterConfigContextService)
    private readonly _filterConfigContextService: FilterConfigContextService,
    @Inject(SpreadsheetOuterParamsService)
    private readonly _outerParamsService: SpreadsheetOuterParamsService,
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

  override dispose(): void {
    super.dispose()
    this._pendingInitialConfigs.clear()
    // 整个 Univer 销毁时不会触发资源 onUnLoad，主动释放默认首项及初始化通知的等待。
    for (const unitId of this._valuesReadyResolvers.keys()) {
      this._resolveValuesReady(unitId)
    }
  }

  private _initCommands(): void {
    const toggle = createToggleSpreadsheetFilterOperation(unitId =>
      this._resolveValuesReady(unitId)
    )
    this.disposeWithMe(this._commandService.registerCommand(toggle))
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
    const localeService = this._injector.get(LocaleService)
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
    const QueryControlIcon = this._componentManager.get('DataEaseQueryControlIcon')
    // IconManager 接收 React 组件，复用 ComponentManager 的 Vue 适配结果。
    this.disposeWithMe(
      this._iconManager.register('DataEaseQueryControlIcon', props =>
        createElement(QueryControlIcon, {
          ...props,
          label: localeService.t('dataease-spreadsheet.menu.queryControlShort')
        })
      )
    )
    this.disposeWithMe(
      this._componentManager.register(SPREADSHEET_QUERY_BAR_COMPONENT, QueryBarHost)
    )
  }

  private _initMenus(): void {
    this._menuManagerService.mergeMenu({
      [RibbonStartGroup.OTHERS]: {
        [TOGGLE_SPREADSHEET_FILTER_OPERATION_ID]: {
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
          const loadedConfig = this._filterInstanceService.normalize(resource)
          const outerParamsConfig = this._outerParamsService.applyToFilterConfig(loadedConfig)
          if (!outerParamsConfig) {
            const valuesReady = this._initializeValues(unitId, loadedConfig)
            this._spreadsheetFilterRuntimeService.setValuesReady(unitId, valuesReady)
            return
          }
          this._pendingInitialConfigs.set(unitId, loadedConfig)
          // 只归一化并登记等待，外参转换成功后再提交配置、显示控件及初始化默认值。
          const valuesReady = outerParamsConfig.then(config => {
            if (this._disposed || this._pendingInitialConfigs.get(unitId) !== loadedConfig) return
            this._pendingInitialConfigs.delete(unitId)
            return this._initializeValues(unitId, config)
          })
          this._spreadsheetFilterRuntimeService.setValuesReady(unitId, valuesReady)
          // 首次查询由明细表、透视表在默认值就绪后统一执行，资源加载本身不广播刷新。
        },
        onUnLoad: unitId => {
          this._pendingInitialConfigs.delete(unitId)
          this._resolveValuesReady(unitId)
          this._filterInstanceService.delete(unitId)
          this._filterInstanceService.setVisible(false)
          this._spreadsheetFilterRuntimeService.clearValues(unitId)
          resetSpreadsheetFilterEventState()
          dispatchSpreadsheetFilterVisibleChange(false)
        },
        // 初始化期间保存仍保留原始条件，不能把尚未提交的资源序列化为空配置。
        toJson: unitId =>
          JSON.stringify(
            this._pendingInitialConfigs.get(unitId) || this._filterInstanceService.get(unitId)
          ),
        parseJson: data => JSON.parse(data) as SpreadsheetFilterConfig
      })
    )
  }

  private _initializeValues(unitId: string, loadedConfig: SpreadsheetFilterConfig): Promise<void> {
    const values = getSpreadsheetFilterInitialValues(loadedConfig)
    const config = this._filterInstanceService.setConfigForUnit(
      unitId,
      this._withSelectValues(loadedConfig, values)
    )
    this._spreadsheetFilterRuntimeService.setValues(unitId, values)
    const valuesReady = this._createValuesReady(unitId, config)
    dispatchSpreadsheetFilterVisibleChange(config.visible)
    dispatchSpreadsheetFilterConfigChange(config)
    return valuesReady
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

  private async _requestConfigContext(
    payload: SpreadsheetFilterConfigContextRequestPayload
  ): Promise<void> {
    const context = await this._filterConfigContextService.getConfigContext()
    if (!context) {
      return
    }

    payload.onReady({
      ...context,
      selectedConditionId: payload.selectedConditionId,
      initialAction: payload.initialAction
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

  private _mergeIds(...idsList: string[][]): string[] {
    return [...new Set(idsList.flat().filter(Boolean))]
  }
}
