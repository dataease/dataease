import { queryData } from '../../../api/data'
import type {
  DetailTableConfig,
  DetailTableQueryConfig,
  DetailTableQueryTotalField,
  TableDataResult
} from '../types'
import {
  DEFAULT_DATE_PATTERN,
  DEFAULT_DATE_STYLE,
  normalizeFormatterConfig
} from '../utils/field-format'

export class TableDataService {
  async queryData(config: DetailTableConfig): Promise<TableDataResult> {
    const fields = config.data?.zones?.fields || []

    if (fields.length === 0) {
      throw new Error('No fields configured')
    }

    const normalizedConfig: DetailTableConfig = {
      ...config,
      data: {
        ...config.data,
        zones: {
          ...config.data.zones,
          fields: fields.map(field => {
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
          })
        }
      }
    }

    const customTotalFields: DetailTableQueryTotalField[] = []
    if (config.style?.total?.enable) {
      // 后端只接收需要额外执行聚合 SQL 的自定义总计配置。
      config.style.total.fieldConfig?.forEach(field => {
        const customExpression = field.customExpression
        if (field.aggregation !== 'CUSTOM' || !customExpression?.trim()) {
          return
        }
        customTotalFields.push({
          dataeaseName: field.dataeaseName,
          aggregation: field.aggregation,
          originName: customExpression
        })
      })
    }
    const detailQueryConfig: DetailTableQueryConfig = {
      totalFields: customTotalFields
    }
    const queryRequest = {
      ...normalizedConfig,
      queryConfig: detailQueryConfig
    }
    const response = await queryData(queryRequest)

    return response?.data
  }
}
