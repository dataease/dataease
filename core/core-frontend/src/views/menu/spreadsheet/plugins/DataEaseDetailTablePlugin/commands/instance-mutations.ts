import { CommandType } from '@univerjs/core'
import type { IAccessor, IMutation } from '@univerjs/core'
import { PluginRenderStatusService } from '../../DataEaseRuntimePlugin/services/table'
import { DetailTableInstanceService } from '../services/detail-table-instance.service'
import type { DetailTableConfig } from '../types'

export interface ISetDetailTableInstancesMutationParams {
  unitId: string
  instances: DetailTableConfig[]
  discardedDraftIds?: string[]
}

export const SetDetailTableInstancesMutation: IMutation<ISetDetailTableInstancesMutationParams> = {
  id: 'dataease.mutation.set-detail-table-instances',
  type: CommandType.MUTATION,
  handler: (accessor: IAccessor, params: ISetDetailTableInstancesMutationParams) => {
    accessor.get(DetailTableInstanceService).set(params.unitId, params.instances)
    params.discardedDraftIds?.forEach(pluginId => {
      accessor.get(PluginRenderStatusService).delete(pluginId)
    })
    return true
  }
}
