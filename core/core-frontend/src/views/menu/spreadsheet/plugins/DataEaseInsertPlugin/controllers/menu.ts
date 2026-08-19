import type { IMenuSelectorItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'


export const DATAEASE_INSERT_DROPDOWN_ID = 'dataease.operation.insert-dropdown'

export function InsertDropdownMainButtonFactory(): IMenuSelectorItem<string> {
  return {
    id: DATAEASE_INSERT_DROPDOWN_ID,
    type: MenuItemType.SUBITEMS,
    icon: 'AdditionAndSubtractionSingle', // 尝试使用内置图标
    tooltip: '添加',
    title: '添加'
  }
}