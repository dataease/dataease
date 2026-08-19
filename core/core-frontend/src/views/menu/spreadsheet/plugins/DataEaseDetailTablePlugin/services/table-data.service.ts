import { queryData } from '../../../api/data'
import type { DetailTableConfig } from '../types'
import type { TableDataResult } from '../types'
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

    const queryConfig: DetailTableConfig = {
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

    const response = await queryData(queryConfig)

    return response
  }
}
