import type { IMenuButtonItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import { PastePluginTableOperation } from '../commands/operations'

export const TABLE_PASTE_ICON_COMPONENT = 'TablePasteIcon'

export function PastePluginTableMenuFactory(): IMenuButtonItem<string> {
  return {
    id: PastePluginTableOperation.id,
    type: MenuItemType.BUTTON,
    title: '粘贴表格',
    icon: TABLE_PASTE_ICON_COMPONENT
  }
}
