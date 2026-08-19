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

export class PivotTableRuntimeService implements SpreadsheetPluginRuntime<PivotTableConfig> {
  readonly type = 'pivot' as const

  constructor(
    @Inject(PivotTableInstanceService)
    private readonly pivotTableInstanceService: PivotTableInstanceService,
    @Inject(PivotTableFillService)
    private readonly pivotTableFillService: PivotTableFillService
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
    const filled = await this.pivotTableFillService.applyStyleOnly(univerApi, config)
    if (filled) {
      this.syncInstance(univerApi, config)
    }
    return filled
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
}
