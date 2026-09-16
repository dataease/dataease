import {
  Disposable,
  ICommandService,
  IUndoRedoService,
  IUniverInstanceService,
  RANGE_TYPE,
  sequenceExecute,
  type IUndoRedoItem
} from '@univerjs/core'
import {
  getSheetCommandTarget,
  InsertRowCommand,
  InsertColCommand,
  InsertRowByRangeCommand,
  InsertColByRangeCommand,
  InsertMultiRowsAboveCommand,
  InsertMultiRowsAfterCommand,
  InsertMultiColsLeftCommand,
  InsertMultiColsRightCommand,
  SheetsSelectionsService
} from '@univerjs/sheets'

export class MultiSelectionInsertController extends Disposable {
  constructor(@ICommandService commandService: ICommandService) {
    super()
    for (const [command, rows] of [
      [InsertMultiRowsAboveCommand, true],
      [InsertMultiRowsAfterCommand, true],
      [InsertMultiColsLeftCommand, false],
      [InsertMultiColsRightCommand, false]
    ] as const) {
      commandService.unregisterCommand(command.id)
      this.disposeWithMe(
        commandService.registerCommand({
          ...command,
          handler: async (accessor, params: { value: number }, options) => {
            const selectionsService = accessor.get(SheetsSelectionsService)
            const selections = selectionsService.getCurrentSelections()
            if (!selections || selections.length <= 1)
              return command.handler(accessor, params, options)
            const rangeType = rows ? RANGE_TYPE.ROW : RANGE_TYPE.COLUMN
            if (selections.some(selection => selection.range.rangeType !== rangeType)) return false
            const count = Number(params?.value)
            if (!Number.isInteger(count) || count < 1 || count > 1000) return false
            const target = getSheetCommandTarget(accessor.get(IUniverInstanceService))
            if (!target) return false
            const start = rows ? 'startRow' : 'startColumn'
            const end = rows ? 'endRow' : 'endColumn'
            // 选区可能相互重叠；只合并重叠部分，保留各个独立区域的插入位置。
            const ranges = selections
              .map(selection => ({ ...selection.range }))
              .sort((a, b) => a[start] - b[start])
            const distinct = [ranges[0]]
            for (const range of ranges.slice(1)) {
              const last = distinct[distinct.length - 1]
              if (range[start] <= last[end]) last[end] = Math.max(last[end], range[end])
              else distinct.push(range)
            }
            const undoRedo = accessor.get(IUndoRedoService)
            const history: IUndoRedoItem[] = []
            let committed = false
            const historyProxy = new Proxy(undoRedo, {
              get(service, property) {
                if (property === 'pushUndoRedo') return (item: IUndoRedoItem) => history.push(item)
                const value = Reflect.get(service, property)
                return typeof value === 'function' ? value.bind(service) : value
              }
            })
            try {
              // 从后向前插入，前面的原始行列坐标不会被后面的插入改变。
              for (const range of distinct.reverse()) {
                const selectionProxy = new Proxy(selectionsService, {
                  get(service, property) {
                    if (property === 'getCurrentSelections') return () => [{ range }]
                    const value = Reflect.get(service, property)
                    return typeof value === 'function' ? value.bind(service) : value
                  }
                })
                // 将同一个局部 accessor 传递到原生插入处理器，收集其完整撤销记录。
                // 不修改全局服务，其他命令仍通过真实命令服务执行。
                const insertCommand = rows ? InsertRowCommand : InsertColCommand
                const byRangeCommand = rows ? InsertRowByRangeCommand : InsertColByRangeCommand
                const commandsProxy = new Proxy(commandService, {
                  get(service, property) {
                    if (property === 'executeCommand' || property === 'syncExecuteCommand') {
                      return (
                        id: string,
                        commandParams: Parameters<typeof InsertRowCommand.handler>[1],
                        commandOptions: typeof options
                      ) => {
                        if (id === insertCommand.id)
                          return insertCommand.handler(
                            selectionAccessor,
                            commandParams,
                            commandOptions
                          )
                        if (id === byRangeCommand.id)
                          return byRangeCommand.handler(
                            selectionAccessor,
                            commandParams,
                            commandOptions
                          )
                        return service[property](id, commandParams, commandOptions)
                      }
                    }
                    const value = Reflect.get(service, property)
                    return typeof value === 'function' ? value.bind(service) : value
                  }
                })
                const selectionAccessor = new Proxy(accessor, {
                  get(service, property) {
                    if (property === 'get')
                      return (id: Parameters<typeof accessor.get>[0]) =>
                        id === SheetsSelectionsService
                          ? selectionProxy
                          : id === ICommandService
                          ? commandsProxy
                          : id === IUndoRedoService
                          ? historyProxy
                          : service.get(id)
                    return Reflect.get(service, property)
                  }
                })
                // 继续使用原生命令，保留样式、公式/表格引用调整和权限拦截。
                if (
                  !(await command.handler(selectionAccessor, { ...params, value: count }, options))
                )
                  return false
              }
              undoRedo.pushUndoRedo({
                unitID: target.unitId,
                undoMutations: history
                  .slice()
                  .reverse()
                  .flatMap(item => item.undoMutations),
                redoMutations: history.flatMap(item => item.redoMutations)
              })
              committed = true
              return true
            } finally {
              // 后续选区被权限/表格等原生拦截器拒绝时，回滚之前已成功的插入。
              if (!committed) {
                for (const item of history.slice().reverse())
                  sequenceExecute(item.undoMutations, commandService)
              }
            }
          }
        })
      )
    }
  }
}
