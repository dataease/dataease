import {
  CustomCommandExecutionError,
  Disposable,
  ICommandService,
  Inject,
  type ICommandInfo,
  type IMutationInfo,
  ObjectMatrix
} from '@univerjs/core'
import { LinkIcon } from '@univerjs/icons'
import {
  type ISetRangeValuesMutationParams,
  SetRangeValuesMutation,
  SetStyleCommand
} from '@univerjs/sheets'
import {
  FormatPainterStatus,
  IFormatPainterService,
  SetFormatPainterOperation
} from '@univerjs/sheets-ui'
import {
  BuiltInUIPart,
  ComponentManager,
  IMenuManagerService,
  IShortcutService,
  IUIPartsService,
  KeyCode,
  RibbonDataGroup,
  RibbonStartGroup
} from '@univerjs/ui'
import DataEaseFunctionDropdown from '../components/DataEaseFunctionDropdown.vue'
import DataEaseInsertDropdown from '../components/DataEaseInsertDropdown.vue'
import DataEaseInsertIcon from '../components/DataEaseInsertIcon.vue'
import { createDataEaseRibbonComponent } from '../components/dataease-ribbon'
import {
  DataEaseCancelFrozenMenuFactory,
  DataEaseFreezeDropdownMenuFactory,
  DataEaseFreezeToCurrentColumnMenuFactory,
  DataEaseFreezeToCurrentRowColumnMenuFactory,
  DataEaseFreezeToCurrentRowMenuFactory
} from './freeze-menu'
import { DataEaseFunctionDropdownMenuFactory } from './function-menu'
import { DataEaseFilterMenuFactory } from './filter-menu'
import { DataEaseInsertDropdownMenuFactory } from './insert-menu'
import { DataEaseInsertDropdownOperation } from './insert-operation'
import { DataEaseSortMenuFactory } from './sort-menu'
import {
  DATAEASE_CANCEL_FROZEN_COMMAND_ID,
  DATAEASE_FUNCTION_DROPDOWN_COMPONENT,
  DATAEASE_FUNCTION_DROPDOWN_MENU_ID,
  DATAEASE_FILTER_MENU_ID,
  DATAEASE_FREEZE_DROPDOWN_MENU_ID,
  DATAEASE_INSERT_DROPDOWN_COMPONENT,
  DATAEASE_INSERT_DROPDOWN_MENU_ID,
  DATAEASE_SET_COLUMN_FROZEN_COMMAND_ID,
  DATAEASE_SET_ROW_FROZEN_COMMAND_ID,
  DATAEASE_SET_SELECTION_FROZEN_COMMAND_ID,
  DATAEASE_SORT_MENU_ID
} from '../config/ribbon-config'
import { SpreadsheetModeService } from '../../../services/spreadsheet-mode.service'
import { DATAEASE_BLOCKED_PROTECTION_COMMAND_IDS } from '../config/protection-config'

export class DataEaseToolbarUIController extends Disposable {
  constructor(
    @ICommandService private readonly commandService: ICommandService,
    @IMenuManagerService private readonly menuManagerService: IMenuManagerService,
    @IShortcutService private readonly shortcutService: IShortcutService,
    @IUIPartsService private readonly uiPartsService: IUIPartsService,
    @IFormatPainterService private readonly formatPainterService: IFormatPainterService,
    @Inject(ComponentManager) private readonly componentManager: ComponentManager,
    @Inject(SpreadsheetModeService)
    private readonly spreadsheetModeService: SpreadsheetModeService
  ) {
    super()
    this.initProtectionGuard()
    this.initFormatPainterHook()
    this.initFormatPainterShortcut()
    this.initComponents()
    this.initMenus()
    this.initUIParts()
  }

  private initFormatPainterShortcut(): void {
    this.disposeWithMe(
      this.shortcutService.registerShortcut({
        id: SetFormatPainterOperation.id,
        binding: KeyCode.ESC,
        priority: 1000,
        // 仅在格式刷生效时接管 Esc，避免影响编辑器、弹窗等原有退出行为。
        preconditions: () => this.formatPainterService.getStatus() !== FormatPainterStatus.OFF,
        staticParameters: {
          status: FormatPainterStatus.OFF
        }
      })
    )
  }

  private initFormatPainterHook(): void {
    this.formatPainterService.addHook({
      id: 'dataease-format-painter',
      onBeforeApply: ({ redoMutationsInfo, undoMutationsInfo }) => {
        this.markFormatPainterStyleMutations(redoMutationsInfo)
        this.markFormatPainterStyleMutations(undoMutationsInfo)
        return true
      }
    })
  }

  private markFormatPainterStyleMutations(mutations: IMutationInfo[]): void {
    mutations.forEach(mutation => {
      if (mutation.id !== SetRangeValuesMutation.id) {
        return
      }

      const params = mutation.params as ISetRangeValuesMutationParams
      if (!this.isStyleOnlyMutation(params)) {
        return
      }

      // 将格式刷的纯样式写入标记为样式操作，避免公式插件误判为内容修改并清除公式。
      params.trigger = SetStyleCommand.id
    })
  }

  private isStyleOnlyMutation(params: ISetRangeValuesMutationParams): boolean {
    if (!params.cellValue) {
      return false
    }

    let hasCell = false
    let styleOnly = true
    new ObjectMatrix(params.cellValue).forValue((_row, _column, cell) => {
      hasCell = true
      if (!cell || Object.keys(cell).some(key => key !== 's')) {
        styleOnly = false
        return false
      }
    })

    return hasCell && styleOnly
  }

  private initProtectionGuard(): void {
    this.disposeWithMe(
      this.commandService.beforeCommandExecuted(command => this.assertProtectionCommandAllowed(command))
    )
  }

  /**
   * 菜单隐藏负责常规用户入口，此处继续拦截快捷键、API 或后续新增入口
   * 对保护 Command 和 Mutation 的直接调用。
   */
  private assertProtectionCommandAllowed(command: Readonly<ICommandInfo>): void {
    if (!DATAEASE_BLOCKED_PROTECTION_COMMAND_IDS.has(command.id)) {
      return
    }

    throw new CustomCommandExecutionError('Spreadsheet protection is disabled')
  }

  private initComponents(): void {
    this.disposeWithMe(this.componentManager.register('LinkIcon', LinkIcon))
    this.disposeWithMe(
      this.componentManager.register('DataEaseInsertIcon', DataEaseInsertIcon, {
        framework: 'vue3'
      })
    )
    this.disposeWithMe(
      this.componentManager.register(DATAEASE_INSERT_DROPDOWN_COMPONENT, DataEaseInsertDropdown, {
        framework: 'vue3'
      })
    )
    this.disposeWithMe(
      this.componentManager.register(DATAEASE_FUNCTION_DROPDOWN_COMPONENT, DataEaseFunctionDropdown, {
        framework: 'vue3'
      })
    )
  }

  private initMenus(): void {
    this.disposeWithMe(this.commandService.registerCommand(DataEaseInsertDropdownOperation))
    this.menuManagerService.mergeMenu({
      [RibbonStartGroup.OTHERS]: {
        [DATAEASE_INSERT_DROPDOWN_MENU_ID]: {
          order: 10,
          menuItemFactory: DataEaseInsertDropdownMenuFactory
        }
      },
      [RibbonDataGroup.ORGANIZATION]: {
        [DATAEASE_FILTER_MENU_ID]: {
          order: 2,
          menuItemFactory: DataEaseFilterMenuFactory
        },
        [DATAEASE_SORT_MENU_ID]: {
          order: 3,
          menuItemFactory: DataEaseSortMenuFactory
        },
        [DATAEASE_FREEZE_DROPDOWN_MENU_ID]: {
          order: 0,
          menuItemFactory: DataEaseFreezeDropdownMenuFactory,
          [DATAEASE_SET_ROW_FROZEN_COMMAND_ID]: {
            order: 0,
            menuItemFactory: DataEaseFreezeToCurrentRowMenuFactory
          },
          [DATAEASE_SET_COLUMN_FROZEN_COMMAND_ID]: {
            order: 1,
            menuItemFactory: DataEaseFreezeToCurrentColumnMenuFactory
          },
          [DATAEASE_SET_SELECTION_FROZEN_COMMAND_ID]: {
            order: 2,
            menuItemFactory: DataEaseFreezeToCurrentRowColumnMenuFactory
          },
          [DATAEASE_CANCEL_FROZEN_COMMAND_ID]: {
            order: 3,
            menuItemFactory: DataEaseCancelFrozenMenuFactory
          }
        },
        [DATAEASE_FUNCTION_DROPDOWN_MENU_ID]: {
          order: 5,
          menuItemFactory: DataEaseFunctionDropdownMenuFactory
        }
      }
    })
  }

  private initUIParts(): void {
    if (this.spreadsheetModeService.isPreview()) {
      return
    }
    this.disposeWithMe(
      this.uiPartsService.registerComponent(
        BuiltInUIPart.CUSTOM_HEADER,
        () => createDataEaseRibbonComponent(this.componentManager, this.menuManagerService)
      )
    )
  }
}
