import type { IMenuButtonItem, IMenuSelectorItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import {
  DATAEASE_CANCEL_FROZEN_COMMAND_ID,
  DATAEASE_FREEZE_DROPDOWN_MENU_ID,
  DATAEASE_SET_COLUMN_FROZEN_COMMAND_ID,
  DATAEASE_SET_ROW_FROZEN_COMMAND_ID,
  DATAEASE_SET_SELECTION_FROZEN_COMMAND_ID
} from '../config/ribbon-config'

export function DataEaseFreezeDropdownMenuFactory(): IMenuSelectorItem<string> {
  return {
    id: DATAEASE_FREEZE_DROPDOWN_MENU_ID,
    type: MenuItemType.SELECTOR,
    icon: 'FreezeToSelectedIcon',
    tooltip: 'dataease-spreadsheet.menu.freeze'
  }
}

export function DataEaseFreezeToCurrentRowMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_SET_ROW_FROZEN_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'FreezeRowIcon',
    title: 'dataease-spreadsheet.menu.freezeToRow'
  }
}

export function DataEaseFreezeToCurrentColumnMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_SET_COLUMN_FROZEN_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'FreezeColumnIcon',
    title: 'dataease-spreadsheet.menu.freezeToColumn'
  }
}

export function DataEaseFreezeToCurrentRowColumnMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_SET_SELECTION_FROZEN_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'FreezeToSelectedIcon',
    title: 'dataease-spreadsheet.menu.freezeToRowColumn'
  }
}

export function DataEaseCancelFrozenMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_CANCEL_FROZEN_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'CancelFreezeIcon',
    title: 'dataease-spreadsheet.menu.unfreeze'
  }
}
