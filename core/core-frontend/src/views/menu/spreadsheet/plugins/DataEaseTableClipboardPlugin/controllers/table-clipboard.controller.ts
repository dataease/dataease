import { Disposable, ICommandService, Inject } from '@univerjs/core'
import {
  ComponentManager,
  ContextMenuGroup,
  ContextMenuPosition,
  IMenuManagerService
} from '@univerjs/ui'
import { useEmitt } from '@/hooks/web/useEmitt'
import type { PluginActionToolbarPayload } from '../../../types/editor'
import { SPREADSHEET_EVENTS } from '../../../utils/events'
import { TableClipboardService } from '../../../services/table-clipboard.service'
import { PastePluginTableOperation } from '../commands/operations'
import {
  PastePluginTableMenuFactory,
  TABLE_PASTE_ICON_COMPONENT
} from './menu'
import TablePasteIcon from '../components/TablePasteIcon.vue'

const { emitter } = useEmitt()

export class DataEaseTableClipboardController extends Disposable {
  constructor(
    @ICommandService private readonly commandService: ICommandService,
    @IMenuManagerService private readonly menuManagerService: IMenuManagerService,
    @Inject(ComponentManager) private readonly componentManager: ComponentManager,
    @Inject(TableClipboardService)
    private readonly tableClipboardService: TableClipboardService
  ) {
    super()
    this.initComponent()
    this.initCommand()
    this.initMenu()
    this.initEscapeListener()
    this.initToolbarListeners()
  }

  private initComponent(): void {
    this.disposeWithMe(
      this.componentManager.register(TABLE_PASTE_ICON_COMPONENT, TablePasteIcon, {
        framework: 'vue3'
      })
    )
  }

  private initCommand(): void {
    this.disposeWithMe(this.commandService.registerCommand(PastePluginTableOperation))
  }

  private initMenu(): void {
    this.menuManagerService.mergeMenu({
      [ContextMenuPosition.MAIN_AREA]: {
        [ContextMenuGroup.OTHERS]: {
          order: 1,
          [PastePluginTableOperation.id]: {
            order: -1,
            menuItemFactory: PastePluginTableMenuFactory
          }
        }
      }
    })
  }

  private initEscapeListener(): void {
    const cancelClipboard = (event: KeyboardEvent) => {
      if (event.key !== 'Escape' || !this.tableClipboardService.hasClipboard()) {
        return
      }

      // 下拉菜单通过 Teleport 渲染在 Univer 容器外，因此这里不依赖表格焦点。
      // 不拦截事件，保留弹窗、下拉菜单和编辑器原有的 Esc 行为。
      this.tableClipboardService.clear()
    }
    document.addEventListener('keydown', cancelClipboard, true)
    this.disposeWithMe({
      dispose: () => document.removeEventListener('keydown', cancelClipboard, true)
    })
  }

  private initToolbarListeners(): void {
    const copyTable = (payload: PluginActionToolbarPayload) => {
      this.tableClipboardService.set('copy', payload)
    }
    const cutTable = (payload: PluginActionToolbarPayload) => {
      this.tableClipboardService.set('cut', payload)
    }

    emitter.on(SPREADSHEET_EVENTS.COPY_PLUGIN_TABLE, copyTable as (payload: unknown) => void)
    emitter.on(SPREADSHEET_EVENTS.CUT_PLUGIN_TABLE, cutTable as (payload: unknown) => void)
    this.disposeWithMe({
      dispose: () => {
        emitter.off(
          SPREADSHEET_EVENTS.COPY_PLUGIN_TABLE,
          copyTable as (payload: unknown) => void
        )
        emitter.off(
          SPREADSHEET_EVENTS.CUT_PLUGIN_TABLE,
          cutTable as (payload: unknown) => void
        )
      }
    })
  }
}
