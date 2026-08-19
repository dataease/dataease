import type { IMenuButtonItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import { InsertDetailTableOperation } from '../commands/insert-operations'

export function InsertDetailTableMenuFactory(): IMenuButtonItem<string> {
  return {
    id: InsertDetailTableOperation.id,
    type: MenuItemType.BUTTON,
    title: '明细表',
    icon: 'TableIcon',
    tooltip: '插入明细表'
  }
}
