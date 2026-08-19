import type { IMenuButtonItem, IMenuSelectorItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import {
  ApplyThreeSlashCellOperation,
  ApplyTwoSlashCellOperation,
  ClearSlashCellOperation
} from '../commands/operations'

export const DATAEASE_SLASH_CELL_DROPDOWN_ID = 'dataease.operation.slash-cell-dropdown'

export function SlashCellDropdownFactory(): IMenuSelectorItem<string> {
  return {
    id: DATAEASE_SLASH_CELL_DROPDOWN_ID,
    type: MenuItemType.SUBITEMS,
    icon: 'BackSlashDoubleIcon',
    tooltip: '斜线单元格'
  }
}

export function TwoSlashCellMenuFactory(): IMenuButtonItem<string> {
  return {
    id: ApplyTwoSlashCellOperation.id,
    type: MenuItemType.BUTTON,
    icon: 'BackSlashDoubleIcon',
    title: '二分斜线单元格',
    tooltip: '二分斜线单元格'
  }
}

export function ThreeSlashCellMenuFactory(): IMenuButtonItem<string> {
  return {
    id: ApplyThreeSlashCellOperation.id,
    type: MenuItemType.BUTTON,
    icon: 'LeftDoubleDiagonalDoubleIcon',
    title: '三分斜线单元格',
    tooltip: '三分斜线单元格'
  }
}

export function ClearSlashCellMenuFactory(): IMenuButtonItem<string> {
  return {
    id: ClearSlashCellOperation.id,
    type: MenuItemType.BUTTON,
    icon: 'AllBorderIcon',
    title: '取消斜线单元格',
    tooltip: '取消斜线单元格'
  }
}
