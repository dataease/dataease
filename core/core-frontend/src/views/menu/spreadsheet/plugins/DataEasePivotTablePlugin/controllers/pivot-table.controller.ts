import {
  Disposable,
  ICommandService,
  IResourceManagerService,
  type IStyleData,
  IUniverInstanceService,
  InterceptorEffectEnum,
  Inject,
  Injector,
  UniverInstanceType,
  HorizontalAlign,
  VerticalAlign
} from '@univerjs/core'
import { FUniver } from '@univerjs/core/facade'
import {
  ComponentManager,
  IDialogService,
  IMenuManagerService,
  ISidebarService,
  RibbonStartGroup
} from '@univerjs/ui'
import {
  INTERCEPTOR_POINT,
  RemoveSheetCommand,
  SetWorksheetActiveOperation,
  SheetInterceptorService
} from '@univerjs/sheets'
import { useEmitt } from '@/hooks/web/useEmitt'
import { SPREADSHEET_EVENTS } from '../../../utils/events'
import { PIVOT_TABLE_PLUGIN_RESOURCE_NAME } from '../../../utils/plugin-resource'
import {
  PluginRenderHoverLayerService,
  PluginRenderHoverService,
  PluginRenderStatusService,
  getPlaceholderPresentation,
  type PluginRenderStatus
} from '../../DataEaseRuntimePlugin/services/table'
import { DATAEASE_TABLE_STYLE_INTERCEPTOR_PRIORITY } from '../../../services/plugin-render-range-edit-policy'
import { getPluginActionToolbarPosition, getPluginCellViewportRect } from '../../../utils/plugin-action-toolbar'
import { isPluginEditorCellSelection } from '../../../utils/plugin-editor-selection'
import type { PluginActionToolbarPayload } from '../../../types/editor'
import { SpreadsheetFilterRuntimeService } from '../../DataEaseFilterPlugin/services/filter-runtime.service'
import {
  offSpreadsheetFilterQuery,
  onSpreadsheetFilterQuery,
  type SpreadsheetFilterQueryPayload
} from '../../DataEaseFilterPlugin/utils/events'
import { DATAEASE_INSERT_DROPDOWN_ID } from '../../DataEaseInsertPlugin/controllers/menu'
import { SlashCellRenderService } from '../../DataEaseSlashCellPlugin/services/slash-cell-render.service'
import { InsertPivotTableOperation } from '../commands/insert-operations'
import {
  SetPivotTableInstancesMutation,
  type ISetPivotTableInstancesMutationParams
} from '../commands/instance-mutations'
import PivotTableCreateDialog from '../components/PivotTableCreateDialog.vue'
import { PivotTableDisplayStateService } from '../services/pivot-table-display-state.service'
import { PivotTableEditProtectionService } from '../services/pivot-table-edit-protection.service'
import { PivotTableFillService } from '../services/pivot-table-fill.service'
import { PivotTableInstanceService } from '../services/pivot-table-instance.service'
import { PivotTableInsertionService } from '../services/pivot-table-insertion.service'
import { PivotTableRenderStyleService } from '../services/pivot-table-render-style.service'
import type { PivotTableConfig } from '../types'
import { InsertPivotTableMenuFactory } from './menu'

const { emitter } = useEmitt()

export class DataEasePivotTableController extends Disposable {
  private readonly univerApi: FUniver
  private readonly pendingRestoreUnits = new Set<string>()
  private readonly restoringUnits = new Set<string>()
  private readonly selectionDisposables = new Map<string, { dispose: () => void }>()
  private hoverContainerDisposable?: { dispose: () => void }
  private placeholderHoverActive = false
  private activePlaceholderPluginId?: string

  constructor(
    @Inject(Injector) private readonly injector: Injector,
    @ICommandService private readonly commandService: ICommandService,
    @IDialogService private readonly dialogService: IDialogService,
    @ISidebarService private readonly sidebarService: ISidebarService,
    @Inject(ComponentManager) private readonly componentManager: ComponentManager,
    @IMenuManagerService private readonly menuManagerService: IMenuManagerService,
    @Inject(IResourceManagerService)
    private readonly resourceManagerService: IResourceManagerService,
    @Inject(IUniverInstanceService)
    private readonly univerInstanceService: IUniverInstanceService,
    @Inject(PivotTableInstanceService)
    private readonly pivotTableInstanceService: PivotTableInstanceService,
    @Inject(PivotTableInsertionService)
    private readonly pivotTableInsertionService: PivotTableInsertionService,
    @Inject(PivotTableFillService)
    private readonly pivotTableFillService: PivotTableFillService,
    @Inject(PivotTableDisplayStateService)
    private readonly pivotTableDisplayStateService: PivotTableDisplayStateService,
    @Inject(PivotTableRenderStyleService)
    private readonly pivotTableRenderStyleService: PivotTableRenderStyleService,
    @Inject(PivotTableEditProtectionService)
    private readonly pivotTableEditProtectionService: PivotTableEditProtectionService,
    @Inject(PluginRenderHoverService)
    private readonly pluginRenderHoverService: PluginRenderHoverService,
    @Inject(PluginRenderHoverLayerService)
    private readonly pluginRenderHoverLayerService: PluginRenderHoverLayerService,
    @Inject(SheetInterceptorService)
    private readonly sheetInterceptorService: SheetInterceptorService,
    @Inject(SpreadsheetFilterRuntimeService)
    private readonly spreadsheetFilterRuntimeService: SpreadsheetFilterRuntimeService,
    @Inject(SlashCellRenderService)
    private readonly slashCellRenderService: SlashCellRenderService,
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService
  ) {
    super()
    this.univerApi = FUniver.newAPI(this.injector)
    this.initComponents()
    this.initCommands()
    this.initMenus()
    this.initResourceHook()
    this.initLifecycleEvents()
    this.initFilterListener()
    this.initCellContentInterceptor()
    this.initRenderStatusListener()
    this.initEditProtection()
    this.initSheetSwitchListener()
    this.initSheetDeleteLifecycle()
    this.initHoverListener()
    this.initActionToolbarListener()
  }

  private initComponents(): void {
    this.disposeWithMe(
      this.componentManager.register('PivotTableCreateDialog', PivotTableCreateDialog, {
        framework: 'vue3'
      })
    )
  }

  private initCommands(): void {
    const commands = [InsertPivotTableOperation, SetPivotTableInstancesMutation]
    commands.forEach(command => {
      this.disposeWithMe(this.commandService.registerCommand(command))
    })
  }

  private initSheetSwitchListener(): void {
    this.disposeWithMe(
      this.commandService.beforeCommandExecuted(commandInfo => {
        if (commandInfo.id !== SetWorksheetActiveOperation.id) {
          return
        }

        const { unitId, subUnitId } = commandInfo.params || {}
        const workbook = unitId
          ? this.univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
          : undefined
        const currentSheetId = workbook?.getActiveSheet()?.getSheetId()
        if (!subUnitId || !workbook?.getSheetBySheetId(subUnitId) || currentSheetId === subUnitId) {
          return
        }

        // 切换前终止当前插入流程，避免旧 Sheet 的弹窗和状态残留到目标 Sheet。
        this.dialogService.close('RangeSelectDialog')
        this.dialogService.close('PivotTableCreateDialog')
        this.pivotTableInsertionService.cancel()
        emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
      })
    )
  }

  private initSheetDeleteLifecycle(): void {
    this.disposeWithMe(
      this.sheetInterceptorService.interceptCommand({
        getMutations: commandInfo => {
          if (commandInfo.id !== RemoveSheetCommand.id) {
            return { redos: [], undos: [] }
          }

          const { unitId, subUnitId } = commandInfo.params || {}
          const workbook = unitId
            ? this.univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
            : this.univerInstanceService.getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET)
          const targetUnitId = unitId || workbook?.getUnitId()
          if (!targetUnitId || !subUnitId) {
            return { redos: [], undos: [] }
          }

          const previousInstances = [...this.pivotTableInstanceService.get(targetUnitId)]
          const draftIds = new Set(
            previousInstances
              .filter(plugin => {
                const status = this.pluginRenderStatusService.get(plugin.id)
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
                id: SetPivotTableInstancesMutation.id,
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
                id: SetPivotTableInstancesMutation.id,
                params: { unitId: targetUnitId, instances: restoredInstances }
              }
            ]
          }
        }
      })
    )

    this.disposeWithMe(
      this.commandService.onCommandExecuted(commandInfo => {
        if (commandInfo.id !== SetPivotTableInstancesMutation.id) {
          return
        }

        const params = commandInfo.params as ISetPivotTableInstancesMutationParams | undefined
        if (!params?.discardedDraftIds?.length) {
          return
        }

        this.dialogService.close('RangeSelectDialog')
        this.dialogService.close('PivotTableCreateDialog')
        this.pivotTableInsertionService.cancel()
        emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
      })
    )
  }

  private initMenus(): void {
    this.menuManagerService.mergeMenu({
      [RibbonStartGroup.OTHERS]: {
        [DATAEASE_INSERT_DROPDOWN_ID]: {
          [InsertPivotTableOperation.id]: {
            order: 20,
            menuItemFactory: InsertPivotTableMenuFactory
          }
        }
      }
    })
  }

  private initResourceHook(): void {
    this.disposeWithMe(
      this.resourceManagerService.registerPluginResource<PivotTableConfig[]>({
        pluginName: PIVOT_TABLE_PLUGIN_RESOURCE_NAME,
        businesses: [UniverInstanceType.UNIVER_SHEET],
        onLoad: (unitId, resource) => {
          const instances = Array.isArray(resource) ? resource : []
          this.pivotTableInstanceService.set(unitId, instances)
          this.pendingRestoreUnits.add(unitId)
          void this.restorePendingUnits()
        },
        onUnLoad: unitId => {
          this.pendingRestoreUnits.delete(unitId)
          this.pivotTableInstanceService.delete(unitId)
          this.pluginRenderStatusService.deleteByUnit(unitId)
          this.pivotTableDisplayStateService.clear()
          this.clearHoverRange()
          this.pivotTableRenderStyleService.deleteUnit(unitId)
        },
        toJson: unitId => JSON.stringify(this.getSerializableInstances(unitId)),
        parseJson: data => JSON.parse(data) as PivotTableConfig[]
      })
    )
  }

  private getSerializableInstances(unitId: string): PivotTableConfig[] {
    const instances = this.pivotTableInstanceService.get(unitId)
    const workbook = this.univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
    const sheets = workbook?.getSheets?.()
    if (!sheets) {
      return instances
    }

    const sheetIds = new Set(sheets.map(sheet => sheet.getSheetId()))
    const validInstances = instances.filter(plugin => sheetIds.has(plugin.placement.sheetId))
    if (validInstances.length !== instances.length) {
      // 保存前兜底清理无归属 Sheet 的历史实例，内存态与持久化结果保持一致。
      this.pivotTableInstanceService.set(unitId, validInstances)
    }
    return validInstances
  }

  private initLifecycleEvents(): void {
    const activeWorkbook = this.univerApi.getActiveWorkbook()
    if (activeWorkbook) {
      this.registerSelectionListener(activeWorkbook)
    }

    this.disposeWithMe(
      this.univerApi.addEvent(this.univerApi.Event.WorkbookCreated, ({ workbook }) => {
        const unitId = workbook.getId()
        this.registerSelectionListener(workbook)
        if (this.pendingRestoreUnits.has(unitId)) {
          void this.restorePendingUnits()
        }
      })
    )

    this.disposeWithMe(
      this.univerApi.addEvent(this.univerApi.Event.LifeCycleChanged, ({ stage }) => {
        if (
          stage === this.univerApi.Enum.LifecycleStages.Rendered ||
          stage === this.univerApi.Enum.LifecycleStages.Steady
        ) {
          void this.restorePendingUnits()
        }
      })
    )
  }

  private initFilterListener(): void {
    const refresh = (payload: SpreadsheetFilterQueryPayload) => {
      void this.refreshByFilter(payload)
    }

    onSpreadsheetFilterQuery(refresh)
    this.disposeWithMe({
      dispose: () => {
        offSpreadsheetFilterQuery(refresh)
      }
    })
  }

  private initCellContentInterceptor(): void {
    this.disposeWithMe(
      this.sheetInterceptorService.intercept(INTERCEPTOR_POINT.CELL_CONTENT, {
        effect: InterceptorEffectEnum.Style,
        priority: DATAEASE_TABLE_STYLE_INTERCEPTOR_PRIORITY,
        handler: (rawCell, context, next) => {
          const pluginStyle = this.pivotTableRenderStyleService.getStyle(
            context.unitId,
            context.subUnitId,
            context.row,
            context.col
          )
          if (!pluginStyle) {
            // 渲染失败 / 数据为空的占位符：用原生 cell markers 画左上角小三角，
            // 并在单元格内显示状态文字。
            const placeholder = this.pluginRenderStatusService.findByCell(
              context.unitId,
              context.subUnitId,
              context.row,
              context.col,
              'pivot'
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

          const rawStyle = rawCell?.s
          const resolvedRawStyle =
            typeof rawStyle === 'string'
              ? context.workbook.getStyles().get(rawStyle)
              : rawStyle
          const rawStyleObject = this.isStyleObject(resolvedRawStyle) ? resolvedRawStyle : {}
          const overrideUserStyle = this.pivotTableRenderStyleService.shouldOverrideUserStyle(
            context.unitId,
            context.subUnitId,
            context.row,
            context.col
          )
          const mergedStyle = overrideUserStyle
            ? { ...rawStyleObject, ...pluginStyle }
            : { ...pluginStyle, ...rawStyleObject }
          // 仅修改拦截器链中的展示副本，工作表原始值仍用于编辑、计算和保存。
          const displayValue = this.pivotTableRenderStyleService.getDisplayValue(
            context.unitId,
            context.subUnitId,
            context.row,
            context.col,
            rawCell?.v
          )
          const slashHeader = this.pivotTableRenderStyleService.getSlashHeaderRender(
            context.unitId,
            context.subUnitId,
            context.row,
            context.col
          )
          let customRender: any[] | undefined
          if (slashHeader) {
            const cell = (rawCell || {}) as any
            let existingCustomRender: any[] = []
            if (Array.isArray(cell.customRender)) {
              existingCustomRender = cell.customRender.filter(
                (render: any) => render?.uKey !== this.slashCellRenderService.customRenderKey
              )
            }
            // 透视表只复用斜线绘制能力，角头状态仍由透视表配置和 DisplayState 持有。
            customRender = [
              ...existingCustomRender,
              this.slashCellRenderService.createCustomRenderByParts(
                slashHeader.type,
                slashHeader.parts
              )
            ]
          }
          return next({
            ...rawCell,
            ...(displayValue === rawCell?.v ? {} : { v: displayValue }),
            ...(customRender ? { customRender } : {}),
            s: mergedStyle
          })
        }
      })
    )
  }

  private isStyleObject(style: unknown): style is IStyleData {
    return !!style && typeof style === 'object'
  }

  private initEditProtection(): void {
    this.disposeWithMe(
      this.commandService.beforeCommandExecuted(commandInfo => {
        this.pivotTableEditProtectionService.assertCommandAllowed(commandInfo)
      })
    )
  }

  private initHoverListener(): void {
    this.disposeWithMe(
      this.univerApi.addEvent(this.univerApi.Event.CellPointerMove, (event: any) => {
        this.handleCellPointerMove(event)
      })
    )
  }

  private handleCellPointerMove(event: any): void {
    const workbook = event?.workbook || this.univerApi.getActiveWorkbook()
    const worksheet = event?.worksheet || workbook?.getActiveSheet?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const sheetId = worksheet?.getSheetId?.()
    const row = event?.row ?? event?.location?.row
    const column = event?.column ?? event?.col ?? event?.location?.col

    if (!unitId || !sheetId || row == null || column == null) {
      this.clearHoverRange()
      this.hidePlaceholderHover()
      return
    }

    const range = this.pivotTableRenderStyleService.findRangeAt(unitId, sheetId, row, column)
    if (!range) {
      // 渲染失败 / 数据为空的占位符：hover 时显示灰色遮罩 + 原因 tooltip。
      const placeholder = this.pluginRenderStatusService.findByCell(
        unitId,
        sheetId,
        row,
        column,
        'pivot'
      )
      if (placeholder) {
        this.showPlaceholderHover(worksheet, placeholder, unitId, sheetId, row, column)
      } else {
        this.clearHoverRange()
        this.hidePlaceholderHover()
      }
      this.registerHoverContainerListener()
      return
    }

    this.placeholderHoverActive = false
    this.hidePlaceholderTooltip()
    const hoverRange = {
      pluginId: range.pluginId,
      unitId: range.unitId,
      sheetId: range.sheetId,
      startRow: range.startRow,
      startColumn: range.startColumn,
      rowCount: range.rowCount,
      colCount: range.columnCount
    }
    this.pluginRenderHoverService.setHoverRange(hoverRange)
    this.pluginRenderHoverLayerService.show(hoverRange)
    this.showActionToolbar(event, worksheet, range)
    this.registerHoverContainerListener()
  }

  private showPlaceholderHover(
    worksheet: any,
    placeholder: PluginRenderStatus,
    unitId: string,
    sheetId: string,
    row: number,
    column: number
  ): void {
    this.placeholderHoverActive = true
    this.activePlaceholderPluginId = placeholder.pluginId
    const hoverRange = {
      pluginId: placeholder.pluginId,
      unitId,
      sheetId,
      startRow: row,
      startColumn: column,
      rowCount: 1,
      colCount: 1
    }
    // 复用共享 hover 服务登记占位符 hover，便于 hide 时做归属判断。
    this.pluginRenderHoverService.setHoverRange(hoverRange)
    // 与已渲染表格一致的灰色遮罩（rgba(31,35,41,0.08)）。
    this.pluginRenderHoverLayerService.show(hoverRange)
    const rect = getPluginCellViewportRect(worksheet, row, column, 1, 1)
    emitter.emit(SPREADSHEET_EVENTS.PLACEHOLDER_TOOLTIP_SHOW, {
      status: placeholder.status,
      reason: placeholder.reason,
      left: rect?.left ?? 0,
      top: rect ? rect.bottom + 4 : 0
    })

    // 渲染失败的占位符同样显示工具栏（编辑/删除可用，复制/剪切禁用）。
    const plugin = this.pivotTableInstanceService
      .get(unitId)
      .find(item => item.id === placeholder.pluginId)
    if (plugin) {
      this.showPlaceholderActionToolbar(worksheet, placeholder, plugin, row, column)
    } else {
      this.hideActionToolbar()
    }
  }

  private showPlaceholderActionToolbar(
    worksheet: any,
    placeholder: PluginRenderStatus,
    plugin: PivotTableConfig,
    row: number,
    column: number
  ): void {
    const position = getPluginActionToolbarPosition(worksheet, {
      startRow: row,
      startColumn: column,
      rowCount: 1,
      columnCount: 1
    })
    if (!position) {
      this.hideActionToolbar()
      return
    }

    emitter.emit(SPREADSHEET_EVENTS.SHOW_PLUGIN_ACTION_TOOLBAR, {
      type: 'pivot',
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

  private hidePlaceholderTooltip(): void {
    emitter.emit(SPREADSHEET_EVENTS.PLACEHOLDER_TOOLTIP_HIDE)
  }

  private hidePlaceholderHover(): void {
    if (!this.placeholderHoverActive) {
      return
    }
    this.placeholderHoverActive = false
    const activeId = this.activePlaceholderPluginId
    this.activePlaceholderPluginId = undefined

    // 只有当前 hover 层仍属于本控制器占位符时才清理，
    // 避免误清其他控制器（明细/汇总）刚展示的遮罩与工具栏。
    const current = this.pluginRenderHoverService.getHoverRange()
    if (current && current.pluginId === activeId) {
      this.pluginRenderHoverService.clearHoverRange()
      this.pluginRenderHoverLayerService.clear()
      this.hideActionToolbar()
    }
    this.hidePlaceholderTooltip()
  }

  private clearHoverRange(): void {
    const hoverRange = this.pluginRenderHoverService.getHoverRange()
    if (!hoverRange || !this.pivotTableRenderStyleService.hasRange(hoverRange.pluginId)) {
      return
    }
    this.pluginRenderHoverService.clearHoverRange()
    this.pluginRenderHoverLayerService.clear()
    this.hideActionToolbar()
  }

  private registerHoverContainerListener(): void {
    if (this.hoverContainerDisposable) {
      return
    }

    const container = document.querySelector('.univer-container')
    if (!container) {
      return
    }

    const clearHover = () => {
      this.clearHoverRange()
      this.hidePlaceholderHover()
      this.hideActionToolbar()
    }
    container.addEventListener('mouseleave', clearHover)
    this.hoverContainerDisposable = {
      dispose: () => {
        container.removeEventListener('mouseleave', clearHover)
        this.hoverContainerDisposable = undefined
      }
    }
    this.disposeWithMe(this.hoverContainerDisposable)
  }

  private refreshSheetCanvas(sheetId: string): void {
    const worksheet = this.univerApi
      .getActiveWorkbook()
      ?.getSheets?.()
      ?.find(sheet => sheet.getSheetId?.() === sheetId)

    worksheet?.refreshCanvas?.()
  }

  private showActionToolbar(_event: any, worksheet: any, range: any): void {
    const position = getPluginActionToolbarPosition(worksheet, range)
    if (!position) {
      this.hideActionToolbar()
      return
    }

    emitter.emit(SPREADSHEET_EVENTS.SHOW_PLUGIN_ACTION_TOOLBAR, {
      type: 'pivot',
      pluginId: range.pluginId,
      unitId: range.unitId,
      sheetId: range.sheetId,
      startCell: range.config.placement.startCell,
      rowCount: range.rowCount,
      columnCount: range.columnCount,
      config: range.config,
      position
    } as PluginActionToolbarPayload)
  }

  private hideActionToolbar(): void {
    emitter.emit(SPREADSHEET_EVENTS.HIDE_PLUGIN_ACTION_TOOLBAR)
  }

  private initActionToolbarListener(): void {
    const deletePlugin = (payload: PluginActionToolbarPayload) => {
      if (payload?.type !== 'pivot') {
        return
      }
      void this.deletePivotTable(payload)
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

  private initRenderStatusListener(): void {
    // 状态变化（draft/loading/rendered/empty/error）后刷新画布，
    // 让 CELL_CONTENT 拦截器重算并绘制占位符角标。
    this.disposeWithMe(
      this.pluginRenderStatusService.changed$.subscribe(() => {
        const workbook = this.univerApi.getActiveWorkbook()
        const sheetId = workbook?.getActiveSheet?.()?.getSheetId?.()
        if (sheetId) {
          this.refreshSheetCanvas(sheetId)
        }
      })
    )
  }

  private markRestoreError(unitId: string, plugin: PivotTableConfig, error: unknown): void {
    this.pluginRenderStatusService.set({
      pluginId: plugin.id,
      type: 'pivot',
      status: 'error',
      reason: error instanceof Error ? error.message : 'Unknown error',
      unitId,
      sheetId: plugin.placement.sheetId,
      startCell: plugin.placement.startCell,
      updatedAt: Date.now()
    })
  }

  private async deletePivotTable(payload: PluginActionToolbarPayload): Promise<void> {
    const workbook = this.univerApi.getActiveWorkbook()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (!unitId || unitId !== payload.unitId) {
      return
    }

    await this.pivotTableFillService.clearTableData(this.univerApi, payload.pluginId)
    this.pivotTableInstanceService.remove(unitId, payload.pluginId)
    this.pluginRenderStatusService.delete(payload.pluginId)
    if (this.pluginRenderHoverService.clearHoverRange()) {
      this.refreshSheetCanvas(payload.sheetId)
    }
    this.hideActionToolbar()
    emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR)
  }

  private async refreshByFilter(payload: SpreadsheetFilterQueryPayload): Promise<void> {
    const workbook = this.univerApi.getActiveWorkbook()
    const unitId = payload.unitId || workbook?.getId?.() || workbook?.getUnitId?.()
    if (!unitId) {
      return
    }

    this.spreadsheetFilterRuntimeService.setValues(unitId, payload.values)
    const affectedPluginIds = payload.affectedPluginIds?.length
      ? payload.affectedPluginIds
      : this.spreadsheetFilterRuntimeService.getAffectedPluginIds(unitId)
    if (!affectedPluginIds.length) {
      return
    }

    const affectedIdSet = new Set(affectedPluginIds)
    const pivotPlugins = this.pivotTableInstanceService
      .get(unitId)
      .filter(plugin => affectedIdSet.has(plugin.id))

    for (const plugin of pivotPlugins) {
      const queryConfig = this.spreadsheetFilterRuntimeService.applyQueryFilterToConfig(unitId, plugin)
      try {
        await this.pivotTableFillService.fillByConfig(this.univerApi, queryConfig)
      } catch (error) {
        this.markRestoreError(unitId, queryConfig, error)
      }
    }
  }

  private async restorePendingUnits(): Promise<void> {
    const unitIds = Array.from(this.pendingRestoreUnits)
    for (const unitId of unitIds) {
      await this.restoreUnit(unitId)
    }
  }

  private async restoreUnit(unitId: string): Promise<void> {
    if (this.restoringUnits.has(unitId)) {
      return
    }

    const workbook = this.univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
    const activeWorkbook = this.univerApi.getActiveWorkbook()
    if (!workbook || !activeWorkbook || activeWorkbook.getId() !== unitId) {
      return
    }

    const pivotPlugins = this.pivotTableInstanceService.get(unitId)
    this.pendingRestoreUnits.delete(unitId)
    if (!pivotPlugins.length) {
      return
    }

    this.restoringUnits.add(unitId)
    try {
      // 每个实例独立处理：单个实例加载失败只标记该实例，不影响其他实例的恢复。
      for (const plugin of pivotPlugins) {
        try {
          await this.pivotTableFillService.fillByConfig(this.univerApi, plugin, {
            initialRestore: true
          })
        } catch (error) {
          this.markRestoreError(unitId, plugin, error)
        }
      }
    } finally {
      this.restoringUnits.delete(unitId)
    }
  }

  private registerSelectionListener(workbook: any): void {
    const unitId = workbook?.getId?.()
    if (!unitId || !workbook?.onSelectionChange || this.selectionDisposables.has(unitId)) {
      return
    }

    const disposable = workbook.onSelectionChange((selections: any[]) => {
      this.handleSelectionChange(unitId, workbook, selections)
    })

    this.selectionDisposables.set(unitId, disposable)
    this.disposeWithMe({
      dispose: () => {
        disposable.dispose()
        this.selectionDisposables.delete(unitId)
      }
    })
  }

  private handleSelectionChange(unitId: string, workbook: any, selections: any[]): void {
    if (this.pivotTableInsertionService.isInserting() || this.sidebarService.visible) {
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
    const placeholder = this.pluginRenderStatusService.findByCell(
      unitId,
      activeSheetId,
      startRow,
      startColumn,
      'pivot'
    )
    if (placeholder) {
      const plugin = this.pivotTableInstanceService
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

    const matchedState = this.pivotTableDisplayStateService.list().find(state =>
      state.sheetId === activeSheetId &&
      this.isCellInState(startRow, startColumn, state)
    )
    if (!matchedState) {
      return
    }

    const matchedPlugin = this.pivotTableInstanceService
      .get(unitId)
      .find(plugin => plugin.id === matchedState.pluginId)
    if (!matchedPlugin) {
      return
    }

    matchedPlugin.placement.sheetName =
      activeSheet?.getName?.() || activeSheet?.getSheetName?.() || matchedPlugin.placement.sheetName

    emitter.emit(SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR, {
      config: matchedPlugin,
      isNewSheet: false
    })
  }

  private isCellInState(
    row: number,
    column: number,
    state: { startCell: string; rowCount: number; columnCount: number }
  ): boolean {
    const start = this.parseCellAddress(state.startCell)
    return (
      row >= start.row &&
      row < start.row + state.rowCount &&
      column >= start.col &&
      column < start.col + state.columnCount
    )
  }

  private parseCellAddress(cellAddress: string): { row: number; col: number } {
    const match = cellAddress.match(/^([A-Z]+)(\d+)$/i)
    if (!match) {
      throw new Error(`Invalid cell address: ${cellAddress}`)
    }

    let col = 0
    for (const char of match[1].toUpperCase()) {
      col = col * 26 + char.charCodeAt(0) - 64
    }

    return {
      row: parseInt(match[2], 10) - 1,
      col: col - 1
    }
  }
}
