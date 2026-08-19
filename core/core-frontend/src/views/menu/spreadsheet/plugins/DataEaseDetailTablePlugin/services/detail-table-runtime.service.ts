import { Inject } from '@univerjs/core'
import type { SpreadsheetPluginRuntime } from '../../../services/plugin-runtime.service'
import type { DetailTableConfig } from '../types'
import {
  ApplyDetailTableOperation,
  ApplyDetailTableStyleOperation
} from '../commands/operations'
import { DetailTableInstanceService } from './detail-table-instance.service'
import { DetailTableRangeService } from './detail-table-range.service'

export class DetailTableRuntimeService implements SpreadsheetPluginRuntime<DetailTableConfig> {
  readonly type = 'detail' as const

  constructor(
    @Inject(DetailTableInstanceService)
    private readonly detailTableInstanceService: DetailTableInstanceService,
    @Inject(DetailTableRangeService)
    private readonly detailTableRangeService: DetailTableRangeService
  ) {}

  async refreshData({ univerApi, config }: { univerApi: any; config: DetailTableConfig }): Promise<boolean | void> {
    this.syncInstance(univerApi, config)
    return univerApi.executeCommand(ApplyDetailTableOperation.id, {
      univerApi,
      config,
      startCell: config.placement.startCell || 'A1'
    })
  }

  async applyStyle({ univerApi, config }: { univerApi: any; config: DetailTableConfig }): Promise<boolean | void> {
    this.syncInstance(univerApi, config)
    return univerApi.executeCommand(ApplyDetailTableStyleOperation.id, {
      univerApi,
      config
    })
  }

  validateConfigUpdate({ univerApi, config, key, value }: {
    univerApi: any
    config: DetailTableConfig
    key: string
    value: any
  }): string | undefined {
    return this.detailTableRangeService.validateConfigUpdate(config, key, value, univerApi)
  }

  private syncInstance(univerApi: any, config: DetailTableConfig): void {
    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (unitId) {
      this.detailTableInstanceService.addOrUpdate(unitId, config)
    }
  }
}
