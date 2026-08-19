import type { IMenuButtonItem, IMenuSelectorItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import {
  DATAEASE_INSERT_CELL_IMAGE_COMMAND_ID,
  DATAEASE_INSERT_FLOATING_IMAGE_COMMAND_ID
} from '../commands/operations'

export const DATAEASE_IMAGE_DROPDOWN_MENU_ID = 'dataease.operation.insert-image-dropdown'

export function DataEaseImageDropdownMenuFactory(): IMenuSelectorItem<string> {
  return {
    id: DATAEASE_IMAGE_DROPDOWN_MENU_ID,
    type: MenuItemType.SELECTOR,
    icon: 'AddImageIcon',
    tooltip: '插入图片'
  }
}

export function DataEaseFloatingImageMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_INSERT_FLOATING_IMAGE_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'AddImageIcon',
    title: '浮动图片'
  }
}

export function DataEaseCellImageMenuFactory(): IMenuButtonItem<string> {
  return {
    id: DATAEASE_INSERT_CELL_IMAGE_COMMAND_ID,
    type: MenuItemType.BUTTON,
    icon: 'AddImageIcon',
    title: '单元格图片'
  }
}
