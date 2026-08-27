import { CommandType } from '@univerjs/core'
import type { IAccessor, IMutation } from '@univerjs/core'
import { SlashCellStateService } from '../services/slash-cell-state.service'
import type { SlashCellItem } from '../types'

export interface ISetSlashCellItemsMutationParams {
  unitId: string
  items: SlashCellItem[]
}

export const SetSlashCellItemsMutation: IMutation<ISetSlashCellItemsMutationParams> = {
  id: 'dataease.mutation.set-slash-cell-items',
  type: CommandType.MUTATION,
  handler: (accessor: IAccessor, params: ISetSlashCellItemsMutationParams) => {
    accessor.get(SlashCellStateService).set(params.unitId, params.items)
    return true
  }
}
