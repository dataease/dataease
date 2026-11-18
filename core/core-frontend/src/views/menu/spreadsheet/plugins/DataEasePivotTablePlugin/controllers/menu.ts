import type { IMenuButtonItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import { InsertPivotTableOperation } from '../commands/insert-operations'

export function InsertPivotTableMenuFactory(): IMenuButtonItem<string> {
  return {
    id: InsertPivotTableOperation.id,
    type: MenuItemType.BUTTON,
    title: 'dataease-spreadsheet.menu.pivotTable',
    icon: 'TableIcon',
    tooltip: 'dataease-spreadsheet.menu.insertPivotTable'
  }
}
