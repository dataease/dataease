import type { FUniver } from '@univerjs/core/facade'
import { guid } from '@/views/visualized/data/dataset/form/util'
import type { SpreadsheetPluginRuntime } from '../../../services/plugin-runtime.service'
import { dispatchSpreadsheetContentChanged } from '../../../utils/events'
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

  ensureFieldOwnerId({ univerApi, config }: {
    univerApi: FUniver
    config: PivotTableConfig
  }): string | undefined {
    const unitId = univerApi.getActiveWorkbook()?.getId()
    if (!unitId) return

    const storedConfig = this.pivotTableInstanceService.get(unitId).find(item => item.id === config.id)
    const fieldOwnerId = config.fieldOwnerId || storedConfig?.fieldOwnerId || guid()
    // 编辑配置与查询快照可能不是同一对象，只同步归属 ID，避免覆盖已渲染配置。
    config.fieldOwnerId = fieldOwnerId
    if (storedConfig) storedConfig.fieldOwnerId = fieldOwnerId
    dispatchSpreadsheetContentChanged()
    return fieldOwnerId
  }

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
    if (String(config.data.datasetId) === String(queryConfig.data.datasetId)) {
      queryConfig.fieldOwnerId = config.fieldOwnerId
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
      const current = this.pivotTableInstanceService.get(unitId).find(item => item.id === config.id)
      // 查询等待期间首次复制字段可能已分配归属 ID，避免旧快照覆盖；切换数据集时不沿用。
      if (current && String(current.data.datasetId) === String(config.data.datasetId)) {
        config.fieldOwnerId = current.fieldOwnerId || config.fieldOwnerId
      }
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
