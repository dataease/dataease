import { CommandType } from '@univerjs/core'
import type { ICommand } from '@univerjs/core'

export const DATAEASE_INSERT_FLOATING_IMAGE_COMMAND_ID = 'dataease.operation.insert-floating-image'
export const DATAEASE_INSERT_CELL_IMAGE_COMMAND_ID = 'dataease.operation.insert-cell-image'

export const InsertFloatingImageOperation: ICommand = {
  id: DATAEASE_INSERT_FLOATING_IMAGE_COMMAND_ID,
  type: CommandType.OPERATION,
  handler: () => true
}

export const InsertCellImageOperation: ICommand = {
  id: DATAEASE_INSERT_CELL_IMAGE_COMMAND_ID,
  type: CommandType.OPERATION,
  handler: () => true
}
