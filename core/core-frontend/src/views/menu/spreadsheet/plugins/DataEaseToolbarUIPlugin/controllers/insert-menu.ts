import type { IAccessor } from '@univerjs/core'
import type { IMenuSelectorItem } from '@univerjs/ui'
import { MenuItemType } from '@univerjs/ui'
import { combineLatest } from 'rxjs'
import { distinctUntilChanged, map, startWith } from 'rxjs/operators'
import {
  PluginRenderStatusService,
  TableInsertionService
} from '../../DataEaseRuntimePlugin/services/table'
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
  const insertionService = accessor.get(TableInsertionService)
  const renderStatusService = accessor.get(PluginRenderStatusService)
  const draftDisabled$ = renderStatusService.changed$.pipe(
    startWith(undefined),
    map(() => renderStatusService.hasDraft()),
    distinctUntilChanged()
  )

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
    // 未完成的草稿必须先渲染或清除，避免并行插入留下不可编辑的空白实例。
    disabled$: combineLatest([insertionService.inserting$, draftDisabled$]).pipe(
      map(([inserting, hasDraft]) => inserting || hasDraft),
      distinctUntilChanged()
    )
  }
}
