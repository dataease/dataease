import { CommandType } from '@univerjs/core'
import type { IAccessor, IMutation } from '@univerjs/core'
import { PluginRenderStatusService } from '../../DataEaseRuntimePlugin/services/table'
import { PivotTableInstanceService } from '../services/pivot-table-instance.service'
import type { PivotTableConfig } from '../types'

export interface ISetPivotTableInstancesMutationParams {
  unitId: string
  instances: PivotTableConfig[]
  discardedDraftIds?: string[]
}

export const SetPivotTableInstancesMutation: IMutation<ISetPivotTableInstancesMutationParams> = {
  id: 'dataease.mutation.set-pivot-table-instances',
  type: CommandType.MUTATION,
  handler: (accessor: IAccessor, params: ISetPivotTableInstancesMutationParams) => {
    accessor.get(PivotTableInstanceService).set(params.unitId, params.instances)
    params.discardedDraftIds?.forEach(pluginId => {
      accessor.get(PluginRenderStatusService).delete(pluginId)
    })
    return true
  }
}
