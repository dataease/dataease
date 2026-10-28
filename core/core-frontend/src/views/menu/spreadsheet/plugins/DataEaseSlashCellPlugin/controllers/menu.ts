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
    tooltip: 'dataease-spreadsheet.menu.slashCell'
  }
}

export function TwoSlashCellMenuFactory(): IMenuButtonItem<string> {
  return {
    id: ApplyTwoSlashCellOperation.id,
    type: MenuItemType.BUTTON,
    icon: 'BackSlashDoubleIcon',
    title: 'dataease-spreadsheet.menu.slashCellTwo',
    tooltip: 'dataease-spreadsheet.menu.slashCellTwo'
  }
}

export function ThreeSlashCellMenuFactory(): IMenuButtonItem<string> {
  return {
    id: ApplyThreeSlashCellOperation.id,
    type: MenuItemType.BUTTON,
    icon: 'LeftDoubleDiagonalDoubleIcon',
    title: 'dataease-spreadsheet.menu.slashCellThree',
    tooltip: 'dataease-spreadsheet.menu.slashCellThree'
  }
}

export function ClearSlashCellMenuFactory(): IMenuButtonItem<string> {
  return {
    id: ClearSlashCellOperation.id,
    type: MenuItemType.BUTTON,
    icon: 'AllBorderIcon',
    title: 'dataease-spreadsheet.menu.removeSlashCell',
    tooltip: 'dataease-spreadsheet.menu.removeSlashCell'
  }
}
