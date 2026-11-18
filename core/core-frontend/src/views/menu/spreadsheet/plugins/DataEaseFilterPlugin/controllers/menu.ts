import type { IAccessor } from '@univerjs/core'
import type { IMenuButtonItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import { TOGGLE_SPREADSHEET_FILTER_OPERATION_ID } from '../commands/operations'
import { FilterInstanceService } from '../services/filter-instance.service'

export function ToggleSpreadsheetFilterMenuFactory(accessor: IAccessor): IMenuButtonItem<string> {
  const filterInstanceService = accessor.get(FilterInstanceService)

  return {
    id: TOGGLE_SPREADSHEET_FILTER_OPERATION_ID,
    type: MenuItemType.BUTTON,
    icon: 'DataEaseQueryControlIcon',
    tooltip: 'dataease-spreadsheet.menu.queryControl',
    activated$: filterInstanceService.visible$
  }
}
