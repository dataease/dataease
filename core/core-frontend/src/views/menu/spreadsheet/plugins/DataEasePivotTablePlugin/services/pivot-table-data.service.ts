import { queryData } from '../../../api/data'
import type { SpreadsheetQueryFilterItem } from '../../../types/plugin'
import {
  DEFAULT_DATE_PATTERN,
  DEFAULT_DATE_STYLE,
  normalizeFormatterConfig
} from '../utils/field-format'
import type {
  PivotTableConfig,
  PivotTableField,
  PivotTableQueryResult
} from '../types'
import { normalizePivotFields } from '../utils/pivot-config-validator'

export class PivotTableDataService {
  async queryData(
    config: PivotTableConfig,
    outerParamsFilter: SpreadsheetQueryFilterItem[] = []
  ): Promise<PivotTableQueryResult> {
    const normalizeFields = (fields: PivotTableField[]) =>
      normalizePivotFields(fields).map(field => {
        if (field.deType === 1) {
          return {
            ...field,
            dateStyle: field.dateStyle ?? DEFAULT_DATE_STYLE,
            datePattern: field.datePattern ?? DEFAULT_DATE_PATTERN
          }
        }
        if (field.groupType === 'q') {
          return {
            ...field,
            formatterCfg: normalizeFormatterConfig(field.formatterCfg)
          }
        }
        return field
      }) as PivotTableField[]

    const queryConfig: PivotTableConfig = {
      ...config,
      data: {
        ...config.data,
        // 空数组也覆盖旧值，避免复用临时查询配置时残留外部条件。
        outerParamsFilter,
        zones: {
          rows: normalizeFields(config.data.zones.rows),
          columns: normalizeFields(config.data.zones.columns)
        }
      }
    }

    const response = await queryData(queryConfig)

    return response?.data as PivotTableQueryResult
  }
}
