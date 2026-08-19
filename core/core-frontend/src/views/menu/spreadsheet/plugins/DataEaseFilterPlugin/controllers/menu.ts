import type { IAccessor } from '@univerjs/core'
import type { IMenuButtonItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import { ToggleSpreadsheetFilterOperation } from '../commands/operations'
import { FilterInstanceService } from '../services/filter-instance.service'

export function ToggleSpreadsheetFilterMenuFactory(accessor: IAccessor): IMenuButtonItem<string> {
  const filterInstanceService = accessor.get(FilterInstanceService)

  return {
    id: ToggleSpreadsheetFilterOperation.id,
    type: MenuItemType.BUTTON,
    icon: 'DataEaseQueryControlIcon',
    tooltip: '查询控件',
    activated$: filterInstanceService.visible$
  }
}
