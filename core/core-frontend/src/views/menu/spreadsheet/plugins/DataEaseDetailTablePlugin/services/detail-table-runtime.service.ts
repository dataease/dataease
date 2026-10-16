import { Inject } from '@univerjs/core'
import type { FUniver } from '@univerjs/core/facade'
import { guid } from '@/views/visualized/data/dataset/form/util'
import type { SpreadsheetPluginRuntime } from '../../../services/plugin-runtime.service'
import { dispatchSpreadsheetContentChanged } from '../../../utils/events'
import type { DetailTableConfig } from '../types'
import {
  ApplyDetailTableOperation,
  ApplyDetailTableStyleOperation
} from '../commands/operations'
import { DetailTableInstanceService } from './detail-table-instance.service'
import { DetailTableRangeService } from './detail-table-range.service'
import { DetailTableDisplayStateService } from './detail-table-display-state.service'
import { DetailTableRenderStyleService } from './detail-table-render-style.service'
import { PluginRenderStatusService } from '../../DataEaseRuntimePlugin/services/table'

import type { FieldItemData } from '../../../types/plugin'
import { validateDetailZoneUpdate } from '../utils/detail-config-validator'

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
    private readonly detailTableRenderStyleService: DetailTableRenderStyleService,
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService
  ) {}

  ensureFieldOwnerId({ univerApi, config }: {
    univerApi: FUniver
    config: DetailTableConfig
  }): string | undefined {
    const unitId = univerApi.getActiveWorkbook()?.getId()
    if (!unitId) return

    const storedConfig = this.detailTableInstanceService.get(unitId).find(item => item.id === config.id)
    const fieldOwnerId = config.fieldOwnerId || storedConfig?.fieldOwnerId || guid()
    // 编辑配置与保存快照可能不是同一对象，只同步归属 ID，避免覆盖其他配置。
    config.fieldOwnerId = fieldOwnerId
    if (storedConfig) storedConfig.fieldOwnerId = fieldOwnerId
    dispatchSpreadsheetContentChanged()
    return fieldOwnerId
  }

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
    if (key.startsWith('data.zones.')) {
      const zoneId = key.replace('data.zones.', '')
      return validateDetailZoneUpdate(config, zoneId, (value || []) as FieldItemData[])
    }
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
    this.pluginRenderStatusService.delete(config.id)
  }

  private syncInstance(univerApi: any, config: DetailTableConfig): void {
    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (unitId) {
      this.detailTableInstanceService.addOrUpdate(unitId, config)
    }
  }
}
