import {
  Disposable,
  ICommandService,
  IConfigService,
  IUndoRedoService,
  Inject,
  LocaleService,
  type ICommand,
  type IUndoRedoItem,
  sequenceExecute
} from '@univerjs/core'
import {
  AddSheetDataValidationCommand,
  RemoveSheetDataValidationCommand,
  UpdateSheetDataValidationOptionsCommand,
  UpdateSheetDataValidationRangeCommand,
  UpdateSheetDataValidationSettingCommand
} from '@univerjs/sheets-data-validation'
import { ISidebarService, type IUniverUIConfig, UI_PLUGIN_CONFIG_KEY } from '@univerjs/ui'

interface RuleParams {
  unitId?: string
  subUnitId?: string
  ruleId?: string
  rule?: { uid: string }
}

interface Draft {
  key: string
  history: IUndoRedoItem[]
  detail?: Element
}

// Univer 的新建面板使用实时预览；关闭未确认的面板时回滚预览及其撤销记录。
export class DataValidationDraftController extends Disposable {
  constructor(
    @ICommandService commandService: ICommandService,
    @IConfigService configService: IConfigService,
    @IUndoRedoService undoRedoService: IUndoRedoService,
    @ISidebarService sidebarService: ISidebarService,
    @Inject(LocaleService) localeService: LocaleService
  ) {
    super()
    const configured = (configService.getConfig<IUniverUIConfig>(UI_PLUGIN_CONFIG_KEY) || {})
      .container
    const container =
      typeof configured === 'string' ? document.querySelector<HTMLElement>(configured) : configured
    if (!container) return
    let draft: Draft | undefined
    let adding = false
    let confirming = false
    const cancelled = new Set<string>()
    const finish = (commit: boolean) => {
      const current = draft
      draft = undefined
      confirming = false
      if (!current) return
      if (commit) current.history.forEach(item => undoRedoService.pushUndoRedo(item))
      else {
        cancelled.add(current.key)
        // 新规则可能覆盖已有规则的范围，必须反向执行原生 undo，不能只删除新规则。
        for (const item of current.history.reverse()) {
          sequenceExecute(item.undoMutations, commandService)
        }
      }
    }
    const detailSelector = '[data-u-comp="data-validation-detail"]'
    const observer = new MutationObserver(() => {
      if (!draft) return
      const detail = container.querySelector(detailSelector)
      if (detail) draft.detail = detail
      else if (draft.detail) finish(confirming)
    })
    observer.observe(container, { childList: true, subtree: true })
    const click = (event: MouseEvent) => {
      if (!(event.target instanceof Element)) return
      const button = event.target.closest('button')
      if (!button || !container.contains(button)) return
      if (
        sidebarService.options?.id === 'DataValidationPanel' &&
        button.textContent?.trim() === localeService.t('sheets-data-validation-ui.panel.add')
      ) {
        adding = true
        setTimeout(() => (adding = false), 0)
      }
      if (draft && button.closest(detailSelector)) {
        confirming =
          button.textContent?.trim() === localeService.t('sheets-data-validation-ui.panel.done')
        // 校验失败时面板不退出，之后点击 X 仍应取消。
        if (confirming) {
          setTimeout(() => {
            if (container.querySelector(detailSelector)) confirming = false
          }, 0)
        }
      }
    }
    container.addEventListener('click', click, true)
    const addAndOpen = 'data-validation.command.addRuleAndOpen'
    this.disposeWithMe(
      commandService.beforeCommandExecuted(command => {
        if (command.id === addAndOpen) adding = true
      })
    )
    this.disposeWithMe(
      commandService.onCommandExecuted(command => {
        if (command.id === addAndOpen) adding = false
      })
    )
    const commands: ICommand<RuleParams>[] = [
      AddSheetDataValidationCommand,
      UpdateSheetDataValidationRangeCommand,
      UpdateSheetDataValidationSettingCommand,
      UpdateSheetDataValidationOptionsCommand,
      RemoveSheetDataValidationCommand
    ]
    for (const command of commands) {
      commandService.unregisterCommand(command.id)
      this.disposeWithMe(
        commandService.registerCommand({
          ...command,
          handler: (accessor, params: RuleParams, options) => {
            const key = JSON.stringify([
              params?.unitId,
              params?.subUnitId,
              params?.rule?.uid ?? params?.ruleId
            ])
            // 面板中存在防抖更新，取消后到达的更新不能进入撤销栈。
            if (cancelled.has(key)) return false
            if (command.id === AddSheetDataValidationCommand.id && adding) {
              finish(false)
              draft = { key, history: [] }
            }
            if (!draft || draft.key !== key) return command.handler(accessor, params, options)
            const current = draft
            const historyService = new Proxy(undoRedoService, {
              get(target, property) {
                if (property === 'pushUndoRedo')
                  return (item: IUndoRedoItem) => current.history.push(item)
                const value = Reflect.get(target, property)
                return typeof value === 'function' ? value.bind(target) : value
              }
            })
            const draftAccessor = new Proxy(accessor, {
              get(target, property) {
                if (property === 'get') {
                  return (id: Parameters<typeof accessor.get>[0]) =>
                    id === IUndoRedoService ? historyService : target.get(id)
                }
                return Reflect.get(target, property)
              }
            })
            return command.handler(draftAccessor, params, options)
          }
        })
      )
    }
    this.disposeWithMe({
      dispose: () => {
        observer.disconnect()
        container.removeEventListener('click', click, true)
        finish(false)
        cancelled.clear()
      }
    })
  }
}
