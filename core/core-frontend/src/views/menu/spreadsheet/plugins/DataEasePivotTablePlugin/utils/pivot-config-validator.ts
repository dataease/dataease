import type { FieldItemData } from '../../../types/plugin'
import type { PivotAggregation, PivotTableConfig } from '../types'

const SUPPORTED_SUMMARIES = new Set<PivotAggregation>([
  'sum',
  'avg',
  'max',
  'min',
  'count',
  'count_distinct'
])

export function validatePivotConfig(config: PivotTableConfig): string | undefined {
  return validatePivotZones(
    config.data?.zones?.rows || [],
    config.data?.zones?.columns || []
  )
}

export function validatePivotZoneUpdate(
  config: PivotTableConfig,
  zoneId: string,
  fields: FieldItemData[]
): string | undefined {
  const rows = zoneId === 'rows' ? fields : config.data?.zones?.rows || []
  const columns = zoneId === 'columns' ? fields : config.data?.zones?.columns || []
  return validatePivotZones(rows, columns, true)
}

export function normalizePivotFields(fields: FieldItemData[]): FieldItemData[] {
  return fields.map(field => {
    if (field.groupType !== 'q') {
      return field
    }
    const summary = (field.summary || 'sum') as PivotAggregation
    return {
      ...field,
      summary: SUPPORTED_SUMMARIES.has(summary) ? summary : 'sum'
    }
  })
}

function validatePivotZones(
  rows: FieldItemData[],
  columns: FieldItemData[],
  allowEmpty = false
): string | undefined {
  if (!allowEmpty && rows.length === 0 && columns.length === 0) {
    return '透视表的行和列不能同时为空'
  }

  const fieldIds = new Set<string>()
  for (const field of [...rows, ...columns]) {
    const fieldId = String(field.id)
    if (fieldIds.has(fieldId)) {
      return '透视表的行列字段不能重复'
    }
    fieldIds.add(fieldId)
  }

  if (rows.some(isQuota) && columns.some(isQuota)) {
    return '指标字段只能全部位于行或列的一侧'
  }

  return validateQuotaPositions(rows, '行') || validateQuotaPositions(columns, '列')
}

function validateQuotaPositions(fields: FieldItemData[], zoneName: string): string | undefined {
  if (!fields.some(isQuota) || isQuota(fields[0])) {
    return undefined
  }

  let quotaStarted = false
  for (const field of fields) {
    if (isQuota(field)) {
      quotaStarted = true
    } else if (quotaStarted) {
      return `${zoneName}中的指标必须全部连续放在末尾`
    }
  }

  return undefined
}

function isQuota(field?: FieldItemData): boolean {
  return field?.groupType === 'q'
}
