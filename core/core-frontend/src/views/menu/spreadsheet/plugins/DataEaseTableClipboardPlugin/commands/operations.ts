import { CommandType, type ICommand, type IAccessor } from '@univerjs/core'
import { TableClipboardService } from '../../../services/table-clipboard.service'

export const PastePluginTableOperation: ICommand = {
  id: 'dataease.operation.paste-plugin-table',
  type: CommandType.OPERATION,
  handler: (accessor: IAccessor) => accessor.get(TableClipboardService).paste()
}
