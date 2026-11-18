import type { IMenuButtonItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import { InsertDetailTableOperation } from '../commands/insert-operations'

export function InsertDetailTableMenuFactory(): IMenuButtonItem<string> {
  return {
    id: InsertDetailTableOperation.id,
    type: MenuItemType.BUTTON,
    title: 'dataease-spreadsheet.menu.detailTable',
    icon: 'TableIcon',
    tooltip: 'dataease-spreadsheet.menu.insertDetailTable'
  }
}
