import { Disposable, ICommandService, Inject, Injector } from '@univerjs/core'
import { ComponentManager } from '@univerjs/ui'
import { OpenRangeSelectDialogOperation } from '../commands/operations'
import RangeSelectDialog from '../components/RangeSelectDialog.vue'

export class RangeSelectController extends Disposable {
  constructor(
    @Inject(Injector) private readonly _injector: Injector,
    @ICommandService private readonly _commandService: ICommandService,
    @Inject(ComponentManager) private readonly _componentManager: ComponentManager
  ) {
    super()
    this._initCommands()
    this._initComponents()
  }

  private _initComponents(): void {
    // 注册 Vue 组件到 Univer 的 ComponentManager
    this.disposeWithMe(
      this._componentManager.register('RangeSelectDialog', RangeSelectDialog, {
        framework: 'vue3'
      })
    )
  }

  private _initCommands(): void {
    const commands = [OpenRangeSelectDialogOperation]

    commands.forEach(c => {
      this.disposeWithMe(this._commandService.registerCommand(c))
    })
  }
}
