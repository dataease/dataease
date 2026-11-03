import { CommandType, DisposableCollection, ICommandService, type Injector } from '@univerjs/core'
import {
  RemoveSheetCommand,
  SheetInterceptorService,
  type IRemoveSheetCommandParams
} from '@univerjs/sheets'
import { cloneDeep } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import {
  PluginRenderStatusService,
  type PluginRenderStatus
} from '../plugins/DataEaseRuntimePlugin/services/table/plugin-render-status.service'
import type { TablePluginConfig } from '../types/plugin'
import { SPREADSHEET_EVENTS } from '../utils/events'

interface TableSnapshot<Config, DisplayState, StyleRange> {
  config: Config
  display?: DisplayState
  style?: StyleRange
  status?: PluginRenderStatus
}

interface SheetInstancesParams<Config, DisplayState, StyleRange> {
  unitId: string
  subUnitId: string
  restore?: TableSnapshot<Config, DisplayState, StyleRange>[]
}

export function registerTableSheetHistory<
  Config extends TablePluginConfig,
  DisplayState extends object,
  StyleRange extends object
>(
  injector: Injector,
  mutationId: string,
  instances: { get(unitId: string): Config[]; set(unitId: string, configs: Config[]): void },
  display: {
    get(id: string): DisplayState | undefined
    set(state: DisplayState): void
    delete(id: string): void
  },
  styles: {
    getRange(unitId: string, sheetId: string, id: string): StyleRange | undefined
    setRange(range: StyleRange): void
    deleteRange(unitId: string, sheetId: string, id: string): void
  },
  resumeQuery: (config: Config) => Promise<boolean>
): DisposableCollection {
  const subscriptions = new DisposableCollection()
  const status = injector.get(PluginRenderStatusService)
  const { emitter } = useEmitt()

  subscriptions.add(
    injector.get(ICommandService).registerCommand({
      id: mutationId,
      type: CommandType.MUTATION,
      handler: (
        _accessor,
        { unitId, subUnitId, restore }: SheetInstancesParams<Config, DisplayState, StyleRange>
      ) => {
        const current = instances.get(unitId)
        const removed = current.filter(plugin => plugin.placement.sheetId === subUnitId)
        if (!removed.length && !restore?.length) return true

        // 每次执行都保留其他 Sheet 的实时实例。不要把历史快照本身交给编辑器修改。
        const remaining = current.filter(plugin => plugin.placement.sheetId !== subUnitId)
        // 跨 Sheet 剪切会保留实例 ID，优先保留移到其他 Sheet 的当前实例及其显示状态。
        const remainingIds = new Set(remaining.map(plugin => plugin.id))
        const restored = cloneDeep(restore?.filter(item => !remainingIds.has(item.config.id)) ?? [])
        instances.set(unitId, remaining.concat(restored.map(item => item.config)))

        const discardedDraftIds: string[] = []
        for (const { id } of removed) {
          if (status.get(id)?.status === 'draft') discardedDraftIds.push(id)
          styles.deleteRange(unitId, subUnitId, id)
          display.delete(id)
          status.delete(id)
        }
        // 原生撤销已经恢复了单元格；同步恢复同一次删除的显示范围、样式及占位状态。
        for (const item of restored) {
          if (item.style) styles.setRange(item.style)
          if (item.display) display.set(item.display)
          const renderStatus = item.status
          if (renderStatus) status.set(renderStatus)
          // 被删除工作表上的旧请求已作废；只重启删除时尚未完成的查询。
          if (renderStatus?.status === 'loading') {
            void resumeQuery(item.config).catch((error: unknown) => {
              if (instances.get(unitId).includes(item.config)) {
                status.set({
                  ...renderStatus,
                  status: 'error',
                  reason: String(error),
                  updatedAt: Date.now()
                })
              }
            })
          }
        }
        if (discardedDraftIds.length) {
          emitter.emit(SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR, {
            unitId,
            pluginIds: discardedDraftIds
          })
        }
        return true
      }
    })
  )

  subscriptions.add(
    injector.get(SheetInterceptorService).interceptCommand({
      getMutations: command => {
        if (command.id !== RemoveSheetCommand.id) return { redos: [], undos: [] }
        const { unitId, subUnitId } = (command.params || {}) as Partial<IRemoveSheetCommandParams>
        if (!unitId || !subUnitId) return { redos: [], undos: [] }

        const snapshot = instances
          .get(unitId)
          .filter(
            plugin =>
              plugin.placement.sheetId === subUnitId && status.get(plugin.id)?.status !== 'draft'
          )
          .map(config => ({
            config,
            display: display.get(config.id),
            style: styles.getRange(unitId, subUnitId, config.id),
            status: status.get(config.id)
          }))
        const undos = []
        if (snapshot.length) {
          undos.push({
            id: mutationId,
            params: { unitId, subUnitId, restore: cloneDeep(snapshot) }
          })
        }
        // 空 Sheet 也登记删除动作，撤销后新增的实例在下次重做时同样需要移除。
        return { preRedos: [{ id: mutationId, params: { unitId, subUnitId } }], redos: [], undos }
      }
    })
  )

  return subscriptions
}
