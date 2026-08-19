import type { IMenuButtonItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import { InsertPivotTableOperation } from '../commands/insert-operations'

export function InsertPivotTableMenuFactory(): IMenuButtonItem<string> {
  return {
    id: InsertPivotTableOperation.id,
    type: MenuItemType.BUTTON,
    title: '透视表',
    icon: 'TableIcon',
    tooltip: '插入透视表'
  }
}
