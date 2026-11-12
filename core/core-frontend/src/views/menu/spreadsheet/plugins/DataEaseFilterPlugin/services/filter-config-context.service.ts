import {
  Disposable,
  Inject,
  Injector,
  IUniverInstanceService,
  UniverInstanceType
} from '@univerjs/core'
import { getDsDetailsWithPerm, type DatasetDetail } from '@/api/dataset'
import { getFieldByDQ, type Field } from '@/api/chart'
import { SpreadsheetModeService } from '../../../services/spreadsheet-mode.service'
import { FilterInstanceService } from './filter-instance.service'
import { DetailTableInstanceService } from '../../DataEaseDetailTablePlugin/services/detail-table-instance.service'
import type { DetailTableConfig } from '../../DataEaseDetailTablePlugin/types'
import { PivotTableInstanceService } from '../../DataEasePivotTablePlugin/services/pivot-table-instance.service'
import type { PivotTableConfig } from '../../DataEasePivotTablePlugin/types'
import type {
  SpreadsheetFilterAvailableField,
  SpreadsheetFilterAvailablePlugin,
  SpreadsheetFilterConfigDialogPayload
} from '../utils/events'

export class FilterConfigContextService extends Disposable {
  constructor(
    @Inject(Injector) private readonly _injector: Injector,
    @Inject(IUniverInstanceService) private readonly _univerInstanceService: IUniverInstanceService,
    @Inject(FilterInstanceService) private readonly _filterInstanceService: FilterInstanceService,
    @Inject(SpreadsheetModeService) private readonly _spreadsheetModeService: SpreadsheetModeService
  ) {
    super()
  }

  async getConfigContext(): Promise<SpreadsheetFilterConfigDialogPayload | undefined> {
    if (this._disposed || this._spreadsheetModeService.isPreview()) return
    const workbook = this._univerInstanceService.getCurrentUnitOfType(
      UniverInstanceType.UNIVER_SHEET
    )
    const unitId = workbook?.getUnitId()
    if (!unitId) return
    const config = this._filterInstanceService.get(unitId)
    const availablePlugins = await this._getAvailablePlugins(unitId)
    // 字段加载期间可能退出或切换工作簿，旧实例的数据不能交给新会话。
    if (
      this._disposed ||
      this._univerInstanceService.getCurrentUnitOfType(UniverInstanceType.UNIVER_SHEET) !== workbook
    )
      return
    return { config, availablePlugins }
  }

  private async _getAvailablePlugins(unitId: string): Promise<SpreadsheetFilterAvailablePlugin[]> {
    const detailTableInstanceService = this._getDetailTableInstanceService()
    const pivotTableInstanceService = this._getPivotTableInstanceService()
    const detailPlugins = detailTableInstanceService?.get(unitId) || []
    const pivotPlugins = pivotTableInstanceService?.get(unitId) || []
    const datasetIds = [
      ...detailPlugins.map(plugin => plugin.data?.datasetId),
      ...pivotPlugins.map(plugin => plugin.data?.datasetId)
    ].filter((id): id is string | number => id !== undefined && id !== null && id !== '')
    const datasetMap = new Map<string, DatasetDetail>()

    if (datasetIds.length) {
      try {
        const details = await getDsDetailsWithPerm([...new Set(datasetIds)])
        details?.forEach((detail: DatasetDetail) => datasetMap.set(String(detail.id), detail))
      } catch {
        // 字段读取失败时保留数据区域，避免配置弹窗丢失已有绑定目标。
      }
    }

    return Promise.all([
      ...detailPlugins.map(plugin =>
        this._buildAvailablePlugin(unitId, plugin, 'detail', datasetMap)
      ),
      ...pivotPlugins.map(plugin => this._buildAvailablePlugin(unitId, plugin, 'pivot', datasetMap))
    ])
  }

  private _getDetailTableInstanceService(): DetailTableInstanceService | undefined {
    try {
      return this._injector.get(DetailTableInstanceService)
    } catch {
      return undefined
    }
  }

  private _getPivotTableInstanceService(): PivotTableInstanceService | undefined {
    try {
      return this._injector.get(PivotTableInstanceService)
    } catch {
      return undefined
    }
  }

  private async _buildAvailablePlugin(
    unitId: string,
    plugin: DetailTableConfig | PivotTableConfig,
    pluginType: 'detail' | 'pivot',
    datasetMap: Map<string, DatasetDetail>
  ): Promise<SpreadsheetFilterAvailablePlugin> {
    const datasetId = plugin.data?.datasetId
    const dataset = datasetId === undefined ? undefined : datasetMap.get(String(datasetId))
    let datasetFields:
      | {
          dimensionList?: Array<Field & { agg?: boolean }>
          quotaList?: Array<Field & { agg?: boolean }>
        }
      | undefined = dataset?.fields
    if (dataset && plugin.fieldOwnerId) {
      try {
        // 联动字段按实例读取副本，原字段及副本继续由后端过滤列权限。
        datasetFields = (await getFieldByDQ(
          datasetId,
          plugin.fieldOwnerId,
          {}
        )) as unknown as typeof datasetFields
      } catch {
        // 副本加载失败时保留已取得的公共字段，不影响其他实例配置。
      }
    }
    // 沿用筛选配置的约束，不能把新增的计数、聚合字段带入联动选项。
    const isSelectableField = (field: Field & { agg?: boolean }) =>
      String(field.id) !== '-1' && !field.agg
    const fields: SpreadsheetFilterAvailableField[] = [
      ...(datasetFields?.dimensionList || []).filter(isSelectableField).map(field => ({
        fieldId: field.id,
        fieldName: field.name || field.dataeaseName,
        groupType: 'd' as const,
        deType: field.deType,
        desensitized: field.desensitized
      })),
      ...(datasetFields?.quotaList || []).filter(isSelectableField).map(field => ({
        fieldId: field.id,
        fieldName: field.name || field.dataeaseName,
        groupType: 'q' as const,
        deType: field.deType,
        desensitized: field.desensitized
      }))
    ]

    return {
      pluginId: plugin.id,
      pluginName: this._getPluginName(unitId, plugin),
      pluginType,
      datasetId,
      datasetName: dataset?.name || this._getDatasetName(plugin),
      fields
    }
  }

  private _getDatasetName(plugin: DetailTableConfig | PivotTableConfig): string {
    const data = plugin.data as typeof plugin.data & {
      datasetName?: string
      dataset?: { name?: string }
    }
    return (
      data?.datasetName ||
      data?.dataset?.name ||
      (plugin.data?.datasetId ? `数据集 ${plugin.data.datasetId}` : '-')
    )
  }

  private _getPluginName(unitId: string, plugin: DetailTableConfig | PivotTableConfig): string {
    const sheetId = plugin.placement?.sheetId
    const workbook = this._univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
    const worksheet = sheetId ? workbook?.getSheetBySheetId(sheetId) : undefined
    // 默认区块名始终跟随当前 Sheet 名，避免使用实例创建时保存的旧名称。
    const sheetName = worksheet?.getName?.() || plugin.placement?.sheetName || sheetId || 'Sheet'
    const defaultName = `${sheetName}!${plugin.placement?.startCell || 'A1'}`
    return plugin.style?.base?.customBlockName
      ? plugin.style.base.blockName || defaultName
      : defaultName
  }
}
