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
    tooltip: '冻结'
  }
}

export function DataEaseFreezeToCurrentRowMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_SET_ROW_FROZEN_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'FreezeRowIcon',
    title: '冻结至当前行'
  }
}

export function DataEaseFreezeToCurrentColumnMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_SET_COLUMN_FROZEN_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'FreezeColumnIcon',
    title: '冻结至当前列'
  }
}

export function DataEaseFreezeToCurrentRowColumnMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_SET_SELECTION_FROZEN_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'FreezeToSelectedIcon',
    title: '冻结至当前行列'
  }
}

export function DataEaseCancelFrozenMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_CANCEL_FROZEN_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'CancelFreezeIcon',
    title: '取消冻结'
  }
}
