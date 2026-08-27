import type { SpreadsheetPluginRuntime } from '../../../services/plugin-runtime.service'
import type { FieldItemData } from '../../../types/plugin'
import type { PivotTableConfig } from '../types'
import {
  normalizePivotFields,
  validatePivotZoneUpdate
} from '../utils/pivot-config-validator'
import { PivotTableInstanceService } from './pivot-table-instance.service'
import { Inject } from '@univerjs/core'
import { PivotTableFillService } from './pivot-table-fill.service'
import { PivotTableDisplayStateService } from './pivot-table-display-state.service'
import { PivotTableRenderStyleService } from './pivot-table-render-style.service'
import { PluginRenderStatusService } from '../../DataEaseRuntimePlugin/services/table'

export class PivotTableRuntimeService implements SpreadsheetPluginRuntime<PivotTableConfig> {
  readonly type = 'pivot' as const
  private readonly styleApplyTasks = new Map<string, Promise<boolean | void>>()

  constructor(
    @Inject(PivotTableInstanceService)
    private readonly pivotTableInstanceService: PivotTableInstanceService,
    @Inject(PivotTableFillService)
    private readonly pivotTableFillService: PivotTableFillService,
    @Inject(PivotTableDisplayStateService)
    private readonly pivotTableDisplayStateService: PivotTableDisplayStateService,
    @Inject(PivotTableRenderStyleService)
    private readonly pivotTableRenderStyleService: PivotTableRenderStyleService,
    @Inject(PluginRenderStatusService)
    private readonly pluginRenderStatusService: PluginRenderStatusService
  ) {}

  async refreshData({
    univerApi,
    config
  }: {
    univerApi: any
    config: PivotTableConfig
  }): Promise<boolean> {
    const queryConfig: PivotTableConfig = {
      ...config,
      data: {
        ...config.data,
        zones: {
          rows: normalizePivotFields(config.data.zones.rows),
          columns: normalizePivotFields(config.data.zones.columns)
        }
      }
    }
    const filled = await this.pivotTableFillService.fillByConfig(univerApi, queryConfig)
    if (!filled) {
      return false
    }
    this.syncInstance(univerApi, queryConfig)
    return true
  }

  async applyStyle({
    univerApi,
    config
  }: {
    univerApi: any
    config: PivotTableConfig
  }): Promise<boolean | void> {
    const previousTask = this.styleApplyTasks.get(config.id) || Promise.resolve()
    // 同一透视表的样式变更包含解除合并、恢复值和重新合并，必须串行提交。
    const currentTask = previousTask
      .catch(() => undefined)
      .then(async () => {
        const filled = await this.pivotTableFillService.applyStyleOnly(univerApi, config)
        if (filled) {
          this.syncInstance(univerApi, config)
        }
        return filled
      })

    this.styleApplyTasks.set(config.id, currentTask)
    try {
      return await currentTask
    } finally {
      if (this.styleApplyTasks.get(config.id) === currentTask) {
        this.styleApplyTasks.delete(config.id)
      }
    }
  }

  validateConfigUpdate({
    config,
    key,
    value
  }: {
    univerApi: any
    config: PivotTableConfig
    key: string
    value: any
  }): string | undefined {
    if (!key.startsWith('data.zones.')) {
      return undefined
    }
    const zoneId = key.replace('data.zones.', '')
    return validatePivotZoneUpdate(
      config,
      zoneId,
      normalizePivotFields(Array.isArray(value) ? value as FieldItemData[] : [])
    )
  }

  private syncInstance(univerApi: any, config: PivotTableConfig): void {
    const workbook = univerApi.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    if (unitId) {
      this.pivotTableInstanceService.addOrUpdate(unitId, config)
    }
  }

  async removeDraft({ univerApi, config }: { univerApi: any; config: PivotTableConfig }): Promise<void> {
    const workbook = univerApi?.getActiveWorkbook?.()
    const unitId = workbook?.getId?.() || workbook?.getUnitId?.()
    const sheetId = config.placement?.sheetId

    if (unitId && sheetId) {
      this.pivotTableRenderStyleService.deleteRange(unitId, sheetId, config.id)
    }
    this.pivotTableDisplayStateService.delete(config.id)
    if (unitId) {
      this.pivotTableInstanceService.remove(unitId, config.id)
    }
    this.pluginRenderStatusService.delete(config.id)
  }
}
