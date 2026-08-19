import { Inject } from '@univerjs/core'
import type { SpreadsheetPluginRuntime } from '../../../services/plugin-runtime.service'
import type { DetailTableConfig } from '../types'
import {
  ApplyDetailTableOperation,
  ApplyDetailTableStyleOperation
} from '../commands/operations'
import { DetailTableInstanceService } from './detail-table-instance.service'
import { DetailTableRangeService } from './detail-table-range.service'
import { DetailTableDisplayStateService } from './detail-table-display-state.service'
import { DetailTableRenderStyleService } from './detail-table-render-style.service'
import { pluginRenderStatusService } from '../../../services/plugin-render-status.service'

export class DetailTableRuntimeService implements SpreadsheetPluginRuntime<DetailTableConfig> {
  readonly type = 'detail' as const

  constructor(
    @Inject(DetailTableInstanceService)
    private readonly detailTableInstanceService: DetailTableInstanceService,
    @Inject(DetailTableRangeService)
    private readonly detailTableRangeService: DetailTableRangeService,
    @Inject(DetailTableDisplayStateService)
    private readonly detailTableDisplayStateService: DetailTableDisplayStateService,
    @Inject(DetailTableRenderStyleService)
    private readonly detailTableRenderStyleService: DetailTableRenderStyleService
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

  async removeDraft({ univerApi, config }: { univerApi: any; config: DetailTableConfig }): Promise<void> {
    const workbook = univerApi?.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const sheetId = config.placement?.sheetId

    if (unitId && sheetId) {
      this.detailTableRenderStyleService.deleteRange(unitId, sheetId, config.id)
    }
    this.detailTableDisplayStateService.delete(config.id)
    if (unitId) {
      this.detailTableInstanceService.remove(unitId, config.id)
    }
    pluginRenderStatusService.delete(config.id)
  }

  private syncInstance(univerApi: any, config: DetailTableConfig): void {
    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (unitId) {
      this.detailTableInstanceService.addOrUpdate(unitId, config)
    }
  }
}
