import { Disposable, ICommandService, Inject } from '@univerjs/core'
import { ComponentManager, IMenuManagerService, RibbonInsertGroup } from '@univerjs/ui'
import { AddImageIcon } from '@univerjs/icons'
import {
  InsertCellImageOperation,
  InsertFloatingImageOperation,
  DATAEASE_INSERT_CELL_IMAGE_COMMAND_ID,
  DATAEASE_INSERT_FLOATING_IMAGE_COMMAND_ID
} from '../commands/operations'
import {
  DATAEASE_IMAGE_DROPDOWN_MENU_ID,
  DataEaseCellImageMenuFactory,
  DataEaseFloatingImageMenuFactory,
  DataEaseImageDropdownMenuFactory
} from './menu'

export class DataEaseImageMenuController extends Disposable {
  constructor(
    @ICommandService private readonly commandService: ICommandService,
    @IMenuManagerService private readonly menuManagerService: IMenuManagerService,
    @Inject(ComponentManager) private readonly componentManager: ComponentManager
  ) {
    super()
    this.initComponents()
    this.initCommands()
    this.initMenus()
  }

  private initComponents(): void {
    this.disposeWithMe(this.componentManager.register('AddImageIcon', AddImageIcon))
  }

  private initCommands(): void {
    this.disposeWithMe(this.commandService.registerCommand(InsertFloatingImageOperation))
    this.disposeWithMe(this.commandService.registerCommand(InsertCellImageOperation))
  }

  private initMenus(): void {
    this.menuManagerService.mergeMenu({
      [RibbonInsertGroup.MEDIA]: {
        [DATAEASE_IMAGE_DROPDOWN_MENU_ID]: {
          order: 1,
          menuItemFactory: DataEaseImageDropdownMenuFactory,
          [DATAEASE_INSERT_FLOATING_IMAGE_COMMAND_ID]: {
            order: 0,
            menuItemFactory: DataEaseFloatingImageMenuFactory
          },
          [DATAEASE_INSERT_CELL_IMAGE_COMMAND_ID]: {
            order: 1,
            menuItemFactory: DataEaseCellImageMenuFactory
          }
        }
      }
    })
  }
}
