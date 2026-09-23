import {
  Disposable,
  ICommandService,
  IResourceManagerService,
  IUniverInstanceService,
  InterceptorEffectEnum,
  Inject,
  Injector,
  UniverInstanceType,
  HorizontalAlign,
  VerticalAlign
} from '@univerjs/core'
import type { IStyleData } from '@univerjs/core'
import { FUniver } from '@univerjs/core/facade'
import {
  ComponentManager,
  IDialogService,
  IMenuManagerService,
  RibbonStartGroup,
  ISidebarService
} from '@univerjs/ui'
import {
  INTERCEPTOR_POINT,
  RemoveSheetCommand,
  SetWorksheetActiveOperation,
  SheetInterceptorService
} from '@univerjs/sheets'
import {
  ApplyDetailTableStyleOperation,
  ApplyDetailTableOperation,
  ClearDetailTableOperation,
  type IApplyDetailTableOperationParams,
  type IClearDetailTableOperationParams
} from '../commands/operations'
import {
  InsertDetailTableOperation,
  OpenDetailTableCreateDialogOperation
} from '../commands/insert-operations'
import {
  SetDetailTableInstancesMutation,
  type ISetDetailTableInstancesMutationParams
} from '../commands/instance-mutations'
import type { DetailTableConfig } from '../types'
import { TableFillService } from '../services/table-fill.service'
import { DETAIL_TABLE_PLUGIN_RESOURCE_NAME } from '../../../utils/plugin-resource'
import { useEmitt } from '@/hooks/web/useEmitt'
import { SPREADSHEET_EVENTS } from '../../../utils/events'
import { getPluginActionToolbarPosition, getPluginCellViewportRect } from '../../../utils/plugin-action-toolbar'
import { isPluginEditorCellSelection } from '../../../utils/plugin-editor-selection'
import type { PluginActionToolbarPayload } from '../../../types/editor'
import DetailTableCreateDialog from '../components/DetailTableCreateDialog.vue'
import { InsertDetailTableMenuFactory } from './menu'
import { DATAEASE_INSERT_DROPDOWN_ID } from '../../DataEaseInsertPlugin/controllers/menu'
import { DetailTableInstanceService } from '../services/detail-table-instance.service'
import { DetailTableInsertionService } from '../services/detail-table-insertion.service'
import { DetailTableRenderStyleService } from '../services/detail-table-render-style.service'
import { DetailTableEditProtectionService } from '../services/detail-table-edit-protection.service'
import { SpreadsheetFilterRuntimeService } from '../../DataEaseFilterPlugin/services/filter-runtime.service'
import {
  PluginRenderHoverLayerService,
  PluginRenderHoverService,
  PluginRenderStatusService,
  getPlaceholderPresentation,
  type PluginRenderStatus
} from '../../DataEaseRuntimePlugin/services/table'
import { DATAEASE_TABLE_STYLE_INTERCEPTOR_PRIORITY } from '../../../services/plugin-render-range-edit-policy'
import {
  offSpreadsheetFilterQuery,
  onSpreadsheetFilterQuery,
  type SpreadsheetFilterQueryPayload
} from '../../DataEaseFilterPlugin/utils/events'

const { emitter } = useEmitt()

export class DataEaseDetailTableController extends Disposable {
  private _tableFillService: TableFillService
  private readonly _univerApi: FUniver
  private readonly _pendingRestoreUnits = new Set<string>()
  private readonly _restoringUnits = new Set<string>()
  private readonly _selectionDisposables = new Map<string, { dispose: () => void }>()
  private _hoverContainerDisposable?: { dispose: () => void }
  private _placeholderHoverActive = false
  private _activePlaceholderPluginId?: string

  constructor(
    @Inject(Injector) private readonly _injector: Injector,
    @ICommandService private readonly _commandService: ICommandService,
    @IDialogService private readonly _dialogService: IDialogService,
    @ISidebarService private readonly _sidebarService: ISidebarService,
    @Inject(ComponentManager) private readonly _componentManager: ComponentManager,
    @IMenuManagerService private readonly _menuManagerService: IMenuManagerService,
    @Inject(IResourceManagerService) private readonly _resourceManagerService: IResourceManagerService,
    @Inject(IUniverInstanceService) private readonly _univerInstanceService: IUniverInstanceService,
    @Inject(SheetInterceptorService) private readonly _sheetInterceptorService: SheetInterceptorService,
    @Inject(DetailTableInstanceService) private readonly _detailTableInstanceService: DetailTableInstanceService,
    @Inject(DetailTableInsertionService) private readonly _detailTableInsertionService: DetailTableInsertionService,
    @Inject(DetailTableRenderStyleService) private readonly _detailTableRenderStyleService: DetailTableRenderStyleService,
    @Inject(DetailTableEditProtectionService) private readonly _detailTableEditProtectionService: DetailTableEditProtectionService,
    @Inject(SpreadsheetFilterRuntimeService)
    private readonly _spreadsheetFilterRuntimeService: SpreadsheetFilterRuntimeService,
    @Inject(PluginRenderHoverService)
    private readonly _pluginRenderHoverService: PluginRenderHoverService,
    @Inject(PluginRenderHoverLayerService)
    private readonly _pluginRenderHoverLayerService: PluginRenderHoverLayerService,
    @Inject(PluginRenderStatusService)
    private readonly _pluginRenderStatusService: PluginRenderStatusService
  ) {
    super()
    this._tableFillService = this._injector.get(TableFillService)
    this._univerApi = FUniver.newAPI(this._injector)
    this._initCommands()
    this._initComponents()
    this._initMenus()
    this._initResourceHook()
    this._initCellContentInterceptor()
    this._initRenderStatusListener()
    this._initEditProtection()
    this._initSheetSwitchListener()
    this._initSheetDeleteLifecycle()
    this._initLifecycleEvents()
    this._initSidebarListener()
    this._initFilterListener()
    this._initHoverListener()
    this._initActionToolbarListener()
  }

  private _initComponents(): void {
    this.disposeWithMe(
      this._componentManager.register('DetailTableCreateDialog', DetailTableCreateDialog, {
        framework: 'vue3'
      })
    )
  }

  private _initCommands(): void {
    const commands = [
      OpenDetailTableCreateDialogOperation,
      InsertDetailTableOperation,
      ApplyDetailTableOperation,
      ApplyDetailTableStyleOperation,
      ClearDetailTableOperation,
      SetDetailTableInstancesMutation
    ]

    commands.forEach(command => {
      this.disposeWithMe(this._commandService.registerCommand(command))
    })
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

        // 切换前终止当前插入流程，避免旧 Sheet 的弹窗和状态残留到目标 Sheet。
        this._dialogService.close('RangeSelectDialog')
        this._dialogService.close('DetailTableCreateDialog')
        this._detailTableInsertionService.cancel()
        emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
      })
    )
  }

  private _initSheetDeleteLifecycle(): void {
    this.disposeWithMe(
      this._sheetInterceptorService.interceptCommand({
        getMutations: commandInfo => {
          if (commandInfo.id !== RemoveSheetCommand.id) {
            return { redos: [], undos: [] }
          }

          const { unitId, subUnitId } = commandInfo.params || {}
          const workbook = unitId
            ? this._univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
            : this._univerInstanceService.getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET)
          const targetUnitId = unitId || workbook?.getUnitId()
          if (!targetUnitId || !subUnitId) {
            return { redos: [], undos: [] }
          }

          const previousInstances = [...this._detailTableInstanceService.get(targetUnitId)]
          const draftIds = new Set(
            previousInstances
              .filter(plugin => {
                const status = this._pluginRenderStatusService.get(plugin.id)
                return plugin.placement.sheetId === subUnitId && status?.status === 'draft'
              })
              .map(plugin => plugin.id)
          )
          const nextInstances = previousInstances.filter(
            plugin => plugin.placement.sheetId !== subUnitId
          )
          if (nextInstances.length === previousInstances.length) {
            return { redos: [], undos: [] }
          }

          const restoredInstances = previousInstances.filter(plugin => !draftIds.has(plugin.id))

          // 已完成实例跟随 Sheet 进入撤销栈；未完成草稿删除后不再恢复。
          return {
            // 删除草稿状态必须早于原生 Sheet mutation，避免 Sheet 切换先触发关闭确认。
            preRedos: [
              {
                id: SetDetailTableInstancesMutation.id,
                params: {
                  unitId: targetUnitId,
                  instances: nextInstances,
                  discardedDraftIds: [...draftIds]
                }
              }
            ],
            redos: [],
            undos: [
              {
                id: SetDetailTableInstancesMutation.id,
                params: { unitId: targetUnitId, instances: restoredInstances }
              }
            ]
          }
        }
      })
    )

    this.disposeWithMe(
      this._commandService.onCommandExecuted(commandInfo => {
        if (commandInfo.id !== SetDetailTableInstancesMutation.id) {
          return
        }

        const params = commandInfo.params as ISetDetailTableInstancesMutationParams | undefined
        if (!params?.discardedDraftIds?.length) {
          return
        }

        this._dialogService.close('RangeSelectDialog')
        this._dialogService.close('DetailTableCreateDialog')
        this._detailTableInsertionService.cancel()
        emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
      })
    )
  }

  private _initMenus(): void {
    this._menuManagerService.mergeMenu({
      [RibbonStartGroup.OTHERS]: {
        [DATAEASE_INSERT_DROPDOWN_ID]: {
          [InsertDetailTableOperation.id]: {
            order: 10,
            menuItemFactory: InsertDetailTableMenuFactory
          }
        }
      }
    })
  }

  private _initResourceHook(): void {
    this.disposeWithMe(
      this._resourceManagerService.registerPluginResource<DetailTableConfig[]>({
        pluginName: DETAIL_TABLE_PLUGIN_RESOURCE_NAME,
        businesses: [UniverInstanceType.UNIVER_SHEET],
        onLoad: (unitId, resource) => {
          const instances = Array.isArray(resource) ? resource : []
          this._detailTableInstanceService.set(unitId, instances)
          this._pendingRestoreUnits.add(unitId)
          void this._restorePendingUnits()
        },
        onUnLoad: unitId => {
          this._pendingRestoreUnits.delete(unitId)
          this._detailTableInstanceService.delete(unitId)
          this._pluginRenderStatusService.deleteByUnit(unitId)
          this._clearHoverRange()
          this._detailTableRenderStyleService.deleteUnit(unitId)
        },
        toJson: unitId => JSON.stringify(this._getSerializableInstances(unitId)),
        parseJson: data => JSON.parse(data) as DetailTableConfig[]
      })
    )
  }

  private _getSerializableInstances(unitId: string): DetailTableConfig[] {
    const instances = this._detailTableInstanceService.get(unitId)
    const workbook = this._univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
    const sheets = workbook?.getSheets?.()
    if (!sheets) {
      return instances
    }

    const sheetIds = new Set(sheets.map(sheet => sheet.getSheetId()))
    const validInstances = instances.filter(plugin => sheetIds.has(plugin.placement.sheetId))
    if (validInstances.length !== instances.length) {
      // 保存前兜底清理无归属 Sheet 的历史实例，内存态与持久化结果保持一致。
      this._detailTableInstanceService.set(unitId, validInstances)
    }
    return validInstances
  }

  private _initCellContentInterceptor(): void {
    this.disposeWithMe(
      this._sheetInterceptorService.intercept(INTERCEPTOR_POINT.CELL_CONTENT, {
        effect: InterceptorEffectEnum.Style,
        // 条件格式的样式拦截器优先级为 10。这里需要先合并明细表样式，
        // 再交给条件格式覆盖命中的样式属性。
        priority: DATAEASE_TABLE_STYLE_INTERCEPTOR_PRIORITY,
        handler: (rawCell, context, next) => {
          const pluginStyle = this._detailTableRenderStyleService.getStyle(
            context.unitId,
            context.subUnitId,
            context.row,
            context.col
          )

          const rawStyle = rawCell?.s
          const resolvedRawStyle =
            typeof rawStyle === 'string'
              ? context.workbook.getStyles().get(rawStyle)
              : rawStyle

          if (!pluginStyle) {
            // 渲染失败 / 数据为空的占位符：用原生 cell markers 画左上角小三角，
            // 并在单元格内显示状态文字。
            const placeholder = this._pluginRenderStatusService.findByCell(
              context.unitId,
              context.subUnitId,
              context.row,
              context.col,
              'detail'
            )
            if (placeholder) {
              const presentation = getPlaceholderPresentation(placeholder.status)
              if (presentation) {
                return next({
                  ...(rawCell || {}),
                  v: presentation.text,
                  s: {
                    cl: { rgb: presentation.textColor },
                    fs: 11,
                    ht: HorizontalAlign.CENTER,
                    vt: VerticalAlign.MIDDLE
                  },
                  markers: presentation.markers
                })
              }
            }
            return next(rawCell)
          }

          const rawStyleObject = this._isStyleObject(resolvedRawStyle) ? resolvedRawStyle : {}
          const overrideUserStyle = this._detailTableRenderStyleService.shouldOverrideUserStyle(
            context.unitId,
            context.subUnitId,
            context.row,
            context.col
          )
          const mergedStyle = overrideUserStyle
            ? { ...rawStyleObject, ...pluginStyle }
            : { ...pluginStyle, ...rawStyleObject }

          // 仅修改拦截器链中的展示副本，工作表原始值仍用于编辑、计算和保存。
          const displayValue = this._detailTableRenderStyleService.getDisplayValue(
            context.unitId,
            context.subUnitId,
            context.row,
            context.col,
            rawCell?.v
          )

          const cloneCell = {
            ...rawCell,
            ...(displayValue === rawCell?.v ? {} : { v: displayValue }),
            s: mergedStyle
          }
          return next(cloneCell)
        }
      })
    )
  }

  private _isStyleObject(style: unknown): style is IStyleData {
    return !!style && typeof style === 'object'
  }

  private _initEditProtection(): void {
    this.disposeWithMe(
      this._commandService.beforeCommandExecuted(commandInfo => {
        this._detailTableEditProtectionService.assertCommandAllowed(commandInfo)
      })
    )
  }

  private _initLifecycleEvents(): void {
    const activeWorkbook = this._univerApi.getActiveWorkbook()
    if (activeWorkbook) {
      this._registerSelectionListener(activeWorkbook)
    }

    this.disposeWithMe(
      this._univerApi.addEvent(this._univerApi.Event.WorkbookCreated, ({ workbook }) => {
        const unitId = workbook.getId()
        this._registerSelectionListener(workbook)
        if (this._pendingRestoreUnits.has(unitId)) {
          void this._restorePendingUnits()
        }
      })
    )

    this.disposeWithMe(
      this._univerApi.addEvent(this._univerApi.Event.LifeCycleChanged, ({ stage }) => {
        if (
          stage === this._univerApi.Enum.LifecycleStages.Rendered ||
          stage === this._univerApi.Enum.LifecycleStages.Steady
        ) {
          void this._restorePendingUnits()
        }
      })
    )
  }

  private _initSidebarListener() {
    // 订阅 sidebar 的配置及状态变化
    this.disposeWithMe(
      this._sidebarService.sidebarOptions$.subscribe((options) => {
        // options.visible 为 true 时表示侧边栏打开，为 false 时表示关闭
        if (options.visible) {
          emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
        }
      })
    )
  }

  private _initFilterListener(): void {
    const refresh = (payload: SpreadsheetFilterQueryPayload) => {
      void this._refreshByFilter(payload)
    }

    onSpreadsheetFilterQuery(refresh)
    this.disposeWithMe({
      dispose: () => {
        offSpreadsheetFilterQuery(refresh)
      }
    })
  }

  private _initHoverListener(): void {
    this.disposeWithMe(
      this._univerApi.addEvent(this._univerApi.Event.CellPointerMove, (event: any) => {
        this._handleCellPointerMove(event)
      })
    )
  }

  private _handleCellPointerMove(event: any): void {
    const workbook = event?.workbook || this._univerApi.getActiveWorkbook()
    const worksheet = event?.worksheet || workbook?.getActiveSheet?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const sheetId = worksheet?.getSheetId?.()
    const row = event?.row ?? event?.location?.row
    const col = event?.column ?? event?.col ?? event?.location?.col

    if (!unitId || !sheetId || row == null || col == null) {
      this._clearHoverRange()
      this._hidePlaceholderHover()
      return
    }

    const range = this._detailTableRenderStyleService.findRangeAt(unitId, sheetId, row, col)
    if (!range) {
      // 渲染失败 / 数据为空的占位符：hover 时显示灰色遮罩 + 原因 tooltip。
      const placeholder = this._pluginRenderStatusService.findByCell(
        unitId,
        sheetId,
        row,
        col,
        'detail'
      )
      if (placeholder) {
        this._showPlaceholderHover(worksheet, placeholder, unitId, sheetId, row, col)
      } else {
        this._clearHoverRange()
        this._hidePlaceholderHover()
      }
      this._registerHoverContainerListener()
      return
    }

    this._placeholderHoverActive = false
    this._hidePlaceholderTooltip()
    this._pluginRenderHoverService.setHoverRange(range)
    this._pluginRenderHoverLayerService.show(range)
    this._showActionToolbar(event, worksheet, range)
    this._registerHoverContainerListener()
  }

  private _showPlaceholderHover(
    worksheet: any,
    placeholder: PluginRenderStatus,
    unitId: string,
    sheetId: string,
    row: number,
    col: number
  ): void {
    this._placeholderHoverActive = true
    this._activePlaceholderPluginId = placeholder.pluginId
    const hoverRange = {
      pluginId: placeholder.pluginId,
      unitId,
      sheetId,
      startRow: row,
      startColumn: col,
      rowCount: 1,
      colCount: 1
    }
    // 复用共享 hover 服务登记占位符 hover，便于 hide 时做归属判断。
    this._pluginRenderHoverService.setHoverRange(hoverRange)
    // 与已渲染表格一致的灰色遮罩（rgba(31,35,41,0.08)）。
    this._pluginRenderHoverLayerService.show(hoverRange)
    const rect = getPluginCellViewportRect(worksheet, row, col, 1, 1)
    emitter.emit(SPREADSHEET_EVENTS.PLACEHOLDER_TOOLTIP_SHOW, {
      status: placeholder.status,
      reason: placeholder.reason,
      left: rect?.left ?? 0,
      top: rect ? rect.bottom + 4 : 0
    })

    // 渲染失败的占位符同样显示工具栏（编辑/删除可用，复制/剪切禁用）。
    const plugin = this._detailTableInstanceService
      .get(unitId)
      .find(item => item.id === placeholder.pluginId)
    if (plugin) {
      this._showPlaceholderActionToolbar(worksheet, placeholder, plugin, row, col)
    } else {
      this._hideActionToolbar()
    }
  }

  private _showPlaceholderActionToolbar(
    worksheet: any,
    placeholder: PluginRenderStatus,
    plugin: DetailTableConfig,
    row: number,
    col: number
  ): void {
    const position = getPluginActionToolbarPosition(worksheet, {
      startRow: row,
      startColumn: col,
      rowCount: 1,
      columnCount: 1
    })
    if (!position) {
      this._hideActionToolbar()
      return
    }

    emitter.emit(SPREADSHEET_EVENTS.SHOW_PLUGIN_ACTION_TOOLBAR, {
      type: 'detail',
      pluginId: plugin.id,
      unitId: placeholder.unitId,
      sheetId: placeholder.sheetId,
      startCell: plugin.placement.startCell,
      rowCount: 1,
      columnCount: 1,
      config: plugin,
      position,
      disabledActions: ['copy', 'cut']
    } as PluginActionToolbarPayload)
  }

  private _hidePlaceholderTooltip(): void {
    emitter.emit(SPREADSHEET_EVENTS.PLACEHOLDER_TOOLTIP_HIDE)
  }

  private _hidePlaceholderHover(): void {
    if (!this._placeholderHoverActive) {
      return
    }
    this._placeholderHoverActive = false
    const activeId = this._activePlaceholderPluginId
    this._activePlaceholderPluginId = undefined

    // 只有当前 hover 层仍属于本控制器占位符时才清理，
    // 避免误清其他控制器（明细/汇总）刚展示的遮罩与工具栏。
    const current = this._pluginRenderHoverService.getHoverRange()
    if (current && current.pluginId === activeId) {
      this._pluginRenderHoverService.clearHoverRange()
      this._pluginRenderHoverLayerService.clear()
      this._hideActionToolbar()
    }
    this._hidePlaceholderTooltip()
  }

  private _clearHoverRange(): void {
    const hoverRange = this._pluginRenderHoverService.getHoverRange()
    if (!hoverRange || !this._detailTableRenderStyleService.hasRange(hoverRange.pluginId)) {
      return
    }
    this._pluginRenderHoverService.clearHoverRange()
    this._pluginRenderHoverLayerService.clear()
    this._hideActionToolbar()
  }

  private _registerHoverContainerListener(): void {
    if (this._hoverContainerDisposable) {
      return
    }

    const container = document.querySelector('.univer-container')
    if (!container) {
      return
    }

    const clearHover = () => {
      this._clearHoverRange()
      this._hidePlaceholderHover()
      this._hideActionToolbar()
    }
    container.addEventListener('mouseleave', clearHover)
    this._hoverContainerDisposable = {
      dispose: () => {
        container.removeEventListener('mouseleave', clearHover)
        this._hoverContainerDisposable = undefined
      }
    }
    this.disposeWithMe(this._hoverContainerDisposable)
  }

  private _refreshSheetCanvas(sheetId: string): void {
    const workbook = this._univerApi.getActiveWorkbook()
    const worksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === sheetId)

    worksheet?.refreshCanvas?.()
  }

  private _showActionToolbar(_event: any, worksheet: any, range: any): void {
    const position = getPluginActionToolbarPosition(worksheet, range)
    if (!position) {
      this._hideActionToolbar()
      return
    }

    emitter.emit(SPREADSHEET_EVENTS.SHOW_PLUGIN_ACTION_TOOLBAR, {
      type: 'detail',
      pluginId: range.pluginId,
      unitId: range.unitId,
      sheetId: range.sheetId,
      startCell: range.config.placement.startCell,
      rowCount: range.rowCount,
      columnCount: range.colCount,
      config: range.config,
      position
    } as PluginActionToolbarPayload)
  }

  private _hideActionToolbar(): void {
    emitter.emit(SPREADSHEET_EVENTS.HIDE_PLUGIN_ACTION_TOOLBAR)
  }

  private _initActionToolbarListener(): void {
    const deletePlugin = (payload: PluginActionToolbarPayload) => {
      if (payload?.type !== 'detail') {
        return
      }
      void this._deleteDetailTable(payload)
    }

    emitter.on(
      SPREADSHEET_EVENTS.DELETE_PLUGIN_RENDER,
      deletePlugin as (payload: unknown) => void
    )
    this.disposeWithMe({
      dispose: () => {
        emitter.off(
          SPREADSHEET_EVENTS.DELETE_PLUGIN_RENDER,
          deletePlugin as (payload: unknown) => void
        )
      }
    })
  }

  private _initRenderStatusListener(): void {
    // 状态变化（draft/loading/rendered/empty/error）后刷新画布，
    // 让 CELL_CONTENT 拦截器重算并绘制占位符角标。
    this.disposeWithMe(
      this._pluginRenderStatusService.changed$.subscribe(() => {
        const workbook = this._univerApi.getActiveWorkbook()
        const sheetId = workbook?.getActiveSheet?.()?.getSheetId?.()
        if (sheetId) {
          this._refreshSheetCanvas(sheetId)
        }
      })
    )
  }

  private _markRestoreError(unitId: string, plugin: DetailTableConfig, error: unknown): void {
    this._pluginRenderStatusService.set({
      pluginId: plugin.id,
      type: 'detail',
      status: 'error',
      reason: error instanceof Error ? error.message : 'Unknown error',
      unitId,
      sheetId: plugin.placement.sheetId,
      startCell: plugin.placement.startCell,
      updatedAt: Date.now()
    })
  }

  private async _deleteDetailTable(payload: PluginActionToolbarPayload): Promise<void> {
    const workbook = this._univerApi.getActiveWorkbook()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (!unitId || unitId !== payload.unitId) {
      return
    }

    const worksheet = workbook
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === payload.sheetId)
    if (!worksheet) {
      return
    }

    await this._tableFillService.clearTableData(
      this._univerApi,
      payload.startCell,
      payload.rowCount,
      payload.columnCount,
      worksheet
    )
    this._detailTableInstanceService.remove(unitId, payload.pluginId)
    this._pluginRenderStatusService.delete(payload.pluginId)
    if (this._pluginRenderHoverService.clearHoverRange()) {
      this._refreshSheetCanvas(payload.sheetId)
    }
    this._hideActionToolbar()
    emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
  }

  private async _refreshByFilter(payload: SpreadsheetFilterQueryPayload): Promise<void> {
    const workbook = this._univerApi.getActiveWorkbook()
    const unitId = payload.unitId || workbook?.getId?.() || workbook?.getUnitId?.()
    if (!unitId) {
      return
    }

    this._spreadsheetFilterRuntimeService.setValues(unitId, payload.values)
    const affectedPluginIds = payload.affectedPluginIds?.length
      ? payload.affectedPluginIds
      : this._spreadsheetFilterRuntimeService.getAffectedPluginIds(unitId)
    if (!affectedPluginIds.length) {
      return
    }

    const affectedIdSet = new Set(affectedPluginIds)
    const detailPlugins = this._detailTableInstanceService
      .get(unitId)
      .filter(plugin => affectedIdSet.has(plugin.id))

    for (const plugin of detailPlugins) {
      const queryConfig = this._spreadsheetFilterRuntimeService.applyQueryFilterToConfig(unitId, plugin)
      try {
        await this._tableFillService.fillTable(this._univerApi, queryConfig)
      } catch (error) {
        this._markRestoreError(unitId, queryConfig, error)
      }
    }
  }

  private async _restorePendingUnits(): Promise<void> {
    const unitIds = Array.from(this._pendingRestoreUnits)

    for (const unitId of unitIds) {
      await this._restoreUnit(unitId)
    }
  }

  private _registerSelectionListener(workbook: any): void {
    const unitId = workbook?.getId?.()
    if (!unitId || !workbook?.onSelectionChange || this._selectionDisposables.has(unitId)) {
      return
    }

    const disposable = workbook.onSelectionChange((selections: any[]) => {
      this._handleSelectionChange(unitId, workbook, selections)
    })

    this._selectionDisposables.set(unitId, disposable)
    this.disposeWithMe({
      dispose: () => {
        disposable.dispose()
        this._selectionDisposables.delete(unitId)
      }
    })
  }

  private _handleSelectionChange(unitId: string, workbook: any, selections: any[]): void {
    if (this._detailTableInsertionService.isInserting()) {
      return
    }

    if (this._sidebarService.visible) {
      return
    }

    if (!selections?.length) {
      emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
      return
    }

    const range = selections[0]
    const startRow = range?.startRow ?? range?.row
    const startColumn = range?.startColumn ?? range?.col

    if (
      startRow == null ||
      startColumn == null ||
      !isPluginEditorCellSelection(workbook, workbook?.getActiveSheet?.(), selections)
    ) {
      emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
      return
    }

    const activeSheet = workbook?.getActiveSheet?.()
    const activeSheetId = activeSheet?.getSheetId?.()
    if (!activeSheetId) {
      return
    }

    // 异常 / 空数据占位符也可点选重开配置面板。
    const placeholder = this._pluginRenderStatusService.findByCell(
      unitId,
      activeSheetId,
      startRow,
      startColumn,
      'detail'
    )
    if (placeholder) {
      const plugin = this._detailTableInstanceService
        .get(unitId)
        .find(item => item.id === placeholder.pluginId)
      if (plugin) {
        plugin.placement.sheetName =
          activeSheet?.getName?.() || activeSheet?.getSheetName?.() || plugin.placement.sheetName
        emitter.emit(SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR, {
          config: plugin,
          isNewSheet: false
        })
        return
      }
    }

    const matchedPlugin = this._detailTableInstanceService.get(unitId).find(plugin => {
      if (plugin.placement.sheetId !== activeSheetId) {
        return false
      }

      const fillState = this._tableFillService.getFillState(this._univerApi, plugin.placement.startCell)
      if (!fillState) {
        return false
      }

      const startPos = this._parseCellAddress(plugin.placement.startCell)
      return this._isCellInPluginRange(
        startRow,
        startColumn,
        startPos.row,
        startPos.col,
        fillState.rowCount,
        fillState.colCount
      )
    })

    if (!matchedPlugin) {
      emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
      return
    }

    matchedPlugin.placement.sheetName =
      activeSheet?.getName?.() || activeSheet?.getSheetName?.() || matchedPlugin.placement.sheetName

    emitter.emit(SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR, {
      config: matchedPlugin,
      isNewSheet: false
    })
  }

  private _isCellInPluginRange(
    row: number,
    col: number,
    startRow: number,
    startCol: number,
    rowCount: number,
    colCount: number
  ): boolean {
    return (
      row >= startRow &&
      row < startRow + rowCount &&
      col >= startCol &&
      col < startCol + colCount
    )
  }

  private _parseCellAddress(cellAddress: string): { row: number; col: number } {
    const match = cellAddress.match(/^([A-Z]+)(\d+)$/i)
    if (!match) {
      throw new Error(`Invalid cell address: ${cellAddress}`)
    }

    const colStr = match[1].toUpperCase()
    let col = 0
    for (let i = 0; i < colStr.length; i++) {
      col = col * 26 + (colStr.charCodeAt(i) - 64)
    }

    return {
      row: parseInt(match[2], 10) - 1,
      col: col - 1
    }
  }

  private async _restoreUnit(unitId: string): Promise<void> {
    if (this._restoringUnits.has(unitId)) {
      return
    }

    const workbook = this._univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
    const activeWorkbook = this._univerApi.getActiveWorkbook()

    if (!workbook || !activeWorkbook || activeWorkbook.getId() !== unitId) {
      return
    }

    const detailPlugins = this._detailTableInstanceService.get(unitId)
    this._pendingRestoreUnits.delete(unitId)

    if (!detailPlugins.length) {
      return
    }

    this._restoringUnits.add(unitId)

    try {
      // 每个实例独立处理：单个实例加载失败只标记该实例，不影响其他实例的恢复。
      for (const plugin of detailPlugins) {
        try {
          await this._tableFillService.fillTable(this._univerApi, plugin, {
            initialRestore: true
          })
        } catch (error) {
          this._markRestoreError(unitId, plugin, error)
        }
      }
    } finally {
      this._restoringUnits.delete(unitId)
    }
  }

  async applyDetailTable(
    univerApi: any,
    config: DetailTableConfig,
    startCell: string = 'A1'
  ): Promise<boolean> {
    return this._commandService.executeCommand(
      ApplyDetailTableOperation.id,
      { univerApi, config, startCell } as IApplyDetailTableOperationParams
    )
  }

  async clearDetailTable(
    univerApi: any,
    startCell: string,
    rowCount: number,
    colCount: number
  ): Promise<boolean> {
    return this._commandService.executeCommand(
      ClearDetailTableOperation.id,
      { univerApi, startCell, rowCount, colCount } as IClearDetailTableOperationParams
    )
  }

  getFillState(univerApi: any, startCell: string) {
    return this._tableFillService.getFillState(univerApi, startCell)
  }

  clearFillState(univerApi: any, startCell: string) {
    this._tableFillService.clearFillState(univerApi, startCell)
  }

  clearAllFillStates() {
    this._tableFillService.clearAllStates()
  }
}
