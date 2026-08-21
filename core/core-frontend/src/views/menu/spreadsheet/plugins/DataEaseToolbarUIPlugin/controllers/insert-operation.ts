import type { IAccessor, ICommand } from '@univerjs/core'
import { CommandType, ICommandService } from '@univerjs/core'
import { InsertDetailTableOperation } from '../../DataEaseDetailTablePlugin/commands/insert-operations'
import { InsertPivotTableOperation } from '../../DataEasePivotTablePlugin/commands/insert-operations'
import {
  PluginRenderStatusService,
  TableInsertionService
} from '../../DataEaseRuntimePlugin/services/table'
import {
  DATAEASE_ADD_NOTE_MENU_ID,
  DATAEASE_INSERT_LINK_MENU_ID
} from '../config/ribbon-config'

export const DATAEASE_INSERT_DROPDOWN_OPERATION_ID = 'dataease.operation.insert-dropdown-select'

const TABLE_INSERT_COMMAND_IDS = new Set([
  InsertDetailTableOperation.id,
  InsertPivotTableOperation.id
])

export const DataEaseInsertDropdownOperation: ICommand = {
  id: DATAEASE_INSERT_DROPDOWN_OPERATION_ID,
  type: CommandType.OPERATION,
  handler: async (accessor: IAccessor, params?: string | { value?: string; commandId?: string }) => {
    const commandId = normalizeCommandId(params)
    if (!commandId) {
      return false
    }
    // 与工具栏禁用规则保持一致，草稿未结束前不分发任何插入菜单命令。
    if (accessor.get(PluginRenderStatusService).hasDraft()) {
      return false
    }
    if (
      TABLE_INSERT_COMMAND_IDS.has(commandId) &&
      accessor.get(TableInsertionService).isInserting()
    ) {
      return false
    }

    await accessor.get(ICommandService).executeCommand(commandId)
    return true
  }
}

export const DATAEASE_INSERT_DROPDOWN_ITEMS = [
  {
    key: 'pivot-table',
    label: '透视表',
    commandId: InsertPivotTableOperation.id
  },
  {
    key: 'detail-table',
    label: '明细表',
    commandId: InsertDetailTableOperation.id
  },
  {
    key: 'divider-data',
    label: '',
    divided: true
  },
  {
    key: 'link',
    label: '链接',
    commandId: DATAEASE_INSERT_LINK_MENU_ID
  },
  {
    key: 'note',
    label: '批注',
    commandId: DATAEASE_ADD_NOTE_MENU_ID
  }
]

function normalizeCommandId(params?: string | { value?: string; commandId?: string }): string | undefined {
  if (typeof params === 'string') {
    return params
  }

  return params?.commandId || params?.value
}
