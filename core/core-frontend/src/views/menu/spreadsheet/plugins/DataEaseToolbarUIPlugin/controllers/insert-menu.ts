import type { IAccessor } from '@univerjs/core'
import type { IMenuSelectorItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import { TableInsertionService } from '../../../services/table-insertion.service'
import {
  DATAEASE_INSERT_DROPDOWN_COMPONENT,
  DATAEASE_INSERT_DROPDOWN_MENU_ID
} from '../config/ribbon-config'
import {
  DATAEASE_INSERT_DROPDOWN_ITEMS,
  DATAEASE_INSERT_DROPDOWN_OPERATION_ID
} from './insert-operation'

export function DataEaseInsertDropdownMenuFactory(
  accessor: IAccessor
): IMenuSelectorItem<string> {
  return {
    id: DATAEASE_INSERT_DROPDOWN_MENU_ID,
    commandId: DATAEASE_INSERT_DROPDOWN_OPERATION_ID,
    type: MenuItemType.SELECTOR,
    icon: 'DataEaseInsertIcon',
    tooltip: '插入',
    slot: true,
    selections: [
      {
        label: {
          name: DATAEASE_INSERT_DROPDOWN_COMPONENT,
          hoverable: false,
          selectable: false,
          props: {
            items: DATAEASE_INSERT_DROPDOWN_ITEMS
          } as any
        },
        value: ''
      }
    ],
    disabled$: accessor.get(TableInsertionService).inserting$
  }
}
