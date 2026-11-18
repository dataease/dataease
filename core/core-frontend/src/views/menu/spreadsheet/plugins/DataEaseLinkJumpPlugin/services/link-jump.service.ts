import {
  Disposable,
  ICommandService,
  Inject,
  Injector,
  IUniverInstanceService,
  UniverInstanceType
} from '@univerjs/core'
import { SetWorksheetActiveOperation } from '@univerjs/sheets'
import { cloneDeep } from 'lodash-es'
import dayjs from 'dayjs'
import { useI18n } from '@/hooks/web/useI18n'
import { queryTargetVisualizationJumpInfo } from '@/api/visualization/linkJump'
import type { SpreadsheetRuntimeOptions } from '../../../types/mode'
import type { SpreadsheetFilterConfig } from '../../../types/plugin'
import type { SpreadsheetJumpField, SpreadsheetLinkageFilter } from '../../../types/link-jump'
import { getSpreadsheetPreviewParams } from '../../../utils/preview-params'
import { applySpreadsheetQueryParamsToFilterConfig } from '../../DataEaseFilterPlugin/utils/query-params'
import { normalizeSpreadsheetFilterTextSearchClauses } from '../../DataEaseFilterPlugin/utils/filter-condition-rules'
import { DetailTableInstanceService } from '../../DataEaseDetailTablePlugin/services/detail-table-instance.service'
import { PivotTableInstanceService } from '../../DataEasePivotTablePlugin/services/pivot-table-instance.service'
import { invalidJumpParams, parseSpreadsheetJumpParams } from '../utils/jump-params'

export class SpreadsheetLinkJumpService extends Disposable {
  private ready?: Promise<void>
  private abortController?: AbortController
  private firstLinkedTableId?: string
  private readonly fieldFilters = new Map<string, Map<string, SpreadsheetLinkageFilter>>()
  private readonly queryFields = new Map<string, SpreadsheetJumpField>()

  constructor(
    @Inject(Injector) private readonly injector: Injector,
    @Inject(IUniverInstanceService) private readonly univerInstanceService: IUniverInstanceService
  ) {
    super()
  }

  initialize({ mode, spreadsheetId }: SpreadsheetRuntimeOptions): void {
    if (mode !== 'preview' || spreadsheetId == null || spreadsheetId === '') return
    const encoded = getSpreadsheetPreviewParams(spreadsheetId).get('jumpInfoParam')
    if (!encoded) return
    const controller = new AbortController()
    this.abortController = controller
    this.ready = this.load(spreadsheetId, encoded, controller.signal)
    // 保留拒绝供初始化和取数等待消费；没有数据区域时也由预览初始化入口统一提示。
    void this.ready.catch(() => undefined)
  }

  async activateFirstLinkedSheet(unitId: string): Promise<void> {
    await this.ready
    if (this._disposed || !this.firstLinkedTableId) return
    const workbook = this.univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
    if (!workbook) return
    // 表格实例在 onRendered 时载入，延迟读取以兼容跳转插件先注册的顺序。
    const table = [
      ...this.injector.get(DetailTableInstanceService).get(unitId),
      ...this.injector.get(PivotTableInstanceService).get(unitId)
    ].find(item => item.id === this.firstLinkedTableId)
    const worksheet = table && workbook.getSheetBySheetId(table.placement.sheetId)
    if (
      !worksheet ||
      worksheet.isSheetHidden() ||
      workbook.getActiveSheet()?.getSheetId() === worksheet.getSheetId()
    ) {
      return
    }
    this.injector.get(ICommandService).syncExecuteCommand(SetWorksheetActiveOperation.id, {
      unitId,
      subUnitId: worksheet.getSheetId()
    })
  }

  private async load(spreadsheetId: string | number, encoded: string, signal: AbortSignal) {
    const params = parseSpreadsheetJumpParams(encoded)
    const response = await queryTargetVisualizationJumpInfo(
      {
        sourceDvId: params.sourceDvId,
        sourceViewId: params.sourceViewId,
        sourceFieldId: params.sourceFieldId,
        targetDvId: spreadsheetId,
        targetResourceType: 'spreadsheet',
        // 这里只选择来源看板的跳转配置版本，目标电子表格始终使用正式稿。
        resourceTable: params.sourceResourceTable ?? 'core'
      },
      signal
    )
    if (this._disposed || signal.aborted) throw invalidJumpParams()
    const mappings = response.data?.baseJumpInfoVisualizationMap as Record<string, string[]>
    if (!mappings || typeof mappings !== 'object' || Array.isArray(mappings))
      throw invalidJumpParams()
    // 与仪表板一致按维度、指标顺序覆盖同一目标，仅应用本次点击实际携带的字段。
    for (const field of [...params.dimensionList, ...params.quotaList]) {
      const targets = mappings[`${params.sourceViewId}#${field.id}`]
      if (!targets) continue
      if (!Array.isArray(targets)) throw invalidJumpParams()
      for (const target of targets) {
        if (typeof target !== 'string') throw invalidJumpParams()
        const parts = target.split('#')
        if (parts.length !== 2 || !parts[0] || !parts[1]) throw invalidJumpParams()
        const [targetId, targetFieldId] = parts
        // 按既有映射顺序定位首个表格；空值不参与筛选，但仍保留关联 Sheet 的定位。
        if (targetFieldId !== '1000001' && !this.firstLinkedTableId) {
          this.firstLinkedTableId = targetId
        }
        const range = Array.isArray(field.timeValue) ? field.timeValue : undefined
        // 普通跳转沿用仪表板空值规则，不把 NULL 或空串解释为清除筛选。
        if (!range && (field.value == null || field.value === '')) continue
        if (targetFieldId === '1000001') {
          this.queryFields.set(targetId, field)
          continue
        }
        const filters =
          this.fieldFilters.get(targetId) || new Map<string, SpreadsheetLinkageFilter>()
        const filter: SpreadsheetLinkageFilter = {
          fieldId: targetFieldId,
          operator: 'eq',
          value: [String(field.value)]
        }
        if (range) {
          filter.operator = 'between'
          filter.value = range.map(String)
        } else if (field.linkageDateStyle) {
          filter.dateStyle = field.linkageDateStyle
          filter.datePattern = field.linkageDatePattern
        }
        filters.set(targetFieldId, filter)
        this.fieldFilters.set(targetId, filters)
      }
    }
  }

  applyToFilterConfig(
    config: SpreadsheetFilterConfig
  ): Promise<SpreadsheetFilterConfig> | undefined {
    if (!this.ready) return undefined
    return this.ready.then(async () => {
      if (this._disposed) throw invalidJumpParams()
      const queryValues: Record<string, { self: string[] }> = Object.create(null)
      for (const [conditionId, field] of this.queryFields) {
        const condition = config.conditions.find(item => item.id === conditionId)
        if (!condition) throw new Error(useI18n().t('spreadsheet.outer_params_invalid_target'))
        if (field.value == null && condition.displayType !== 'timeRange') throw invalidJumpParams()
        if (field.linkageDateStyle && ['time', 'timeRange'].includes(condition.displayType)) {
          const key =
            field.linkageDateStyle === 'M_d'
              ? 'visualization.month_day_query_linkage_unsupported'
              : 'visualization.time_only_query_linkage_unsupported'
          throw new Error(useI18n().t(key))
        }
        let values = [String(field.value)]
        if (condition.displayType === 'timeRange' && Array.isArray(field.timeValue)) {
          // 历史日期毫秒数可能不足十三位或为负数，不能转字符串后再猜测时间戳格式。
          values = field.timeValue.map(value => {
            if (typeof value === 'number') return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
            return value
          })
        } else if (['numberRange', 'timeRange'].includes(condition.displayType)) {
          values = [String(field.value), String(field.value)]
        } else if (condition.displayType === 'textSearch') {
          // 文本搜索沿用仪表板赋值规则，只替换第一个条件，保留已配置的第二个条件。
          values = normalizeSpreadsheetFilterTextSearchClauses(
            condition.textSearchDefaultClauses,
            condition.textSearchConditionType
          ).map((clause, index) => (index === 0 ? String(field.value) : clause.value))
        }
        queryValues[conditionId] = { self: values }
      }
      // 仅复用控件赋值与展示值映射；不走外参的默认值、必填或 DE_EMPTY 解析。
      return applySpreadsheetQueryParamsToFilterConfig(config, queryValues)
    })
  }

  async getFieldFilters(unitId: string, pluginId: string): Promise<SpreadsheetLinkageFilter[]> {
    await this.ready
    if (
      this._disposed ||
      !this.univerInstanceService.getUnit(unitId, UniverInstanceType.UNIVER_SHEET)
    )
      throw invalidJumpParams()
    return cloneDeep([...(this.fieldFilters.get(pluginId)?.values() || [])])
  }

  override dispose(): void {
    this.abortController?.abort()
    this.firstLinkedTableId = undefined
    this.fieldFilters.clear()
    this.queryFields.clear()
    super.dispose()
  }
}
