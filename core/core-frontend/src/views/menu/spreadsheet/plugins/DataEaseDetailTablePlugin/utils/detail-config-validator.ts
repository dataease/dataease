import type { DetailTableConfig } from '../types'
import type { FieldItemData } from '../../../types/plugin'

export function validateDetailConfig(config: DetailTableConfig): string | undefined {
  const fields = config.data?.zones?.fields || []
  return validateDetailFields(fields)
}

export function validateDetailZoneUpdate(
  config: DetailTableConfig,
  zoneId: string,
  fields: FieldItemData[]
): string | undefined {
  if (zoneId === 'fields') {
    const currentFields = config.data?.zones?.fields || []
    if (fields.length < currentFields.length) {
      return undefined
    }
    return validateDetailFields(fields, true)
  }
  return undefined
}

export function validateDetailFields(fields: FieldItemData[], allowEmpty = false): string | undefined {
  if (!allowEmpty && fields.length === 0) {
    return '明细表的数据列不能为空'
  }

  const fieldIds = new Set<string>()
  for (const field of fields) {
    const fieldId = String(field.id)
    if (fieldIds.has(fieldId)) {
      return '明细表的数据列不能重复'
    }
    fieldIds.add(fieldId)
  }

  return undefined
}
