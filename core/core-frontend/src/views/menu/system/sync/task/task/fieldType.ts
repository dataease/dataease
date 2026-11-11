import type { IMaintenanceStructure, ITableField } from '@/api/sync/syncTask'

/**
 * 比较目标类型语义，原始类型名称单独保留用于展示
 */
export const normalizeFieldType = (fieldType?: string) => {
  const type = fieldType?.split('(')[0].trim().toUpperCase() || ''
  if (['DECIMALV2', 'DECIMALV3', 'DECIMAL32', 'DECIMAL64', 'DECIMAL128'].includes(type))
    return 'DECIMAL'
  if (type === 'DATETIMEV2') return 'DATETIME'
  if (type === 'DATEV2') return 'DATE'
  return type
}

/**
 * Doris 数值容量必须明确指定，其他目标保留各自对无界数值类型的支持
 */
export const decimalParameterIssue = (
  field: { fieldType?: string; fieldSize?: number; fieldPrecision?: number },
  targetType?: string
) => {
  if (targetType?.toLowerCase() !== 'doris' || normalizeFieldType(field.fieldType) !== 'DECIMAL')
    return ''
  if (!field.fieldSize) return 'sync_task.decimal_precision_required'
  if (
    !Number.isInteger(field.fieldSize) ||
    field.fieldSize < 1 ||
    field.fieldSize > 38 ||
    !Number.isInteger(field.fieldPrecision) ||
    field.fieldPrecision < 0 ||
    field.fieldPrecision > field.fieldSize
  )
    return 'sync_task.decimal_parameters_invalid'
  return ''
}

/**
 * 未更换类型的字段沿用数据库原始名称，缺少元数据时不猜测版本
 */
export const maintenanceFieldType = (
  field: ITableField,
  baseline?: ITableField,
  structure?: IMaintenanceStructure
) => {
  if (!baseline || field.fieldType !== baseline.fieldType) return field.fieldType
  const column = structure?.columns?.find(item => item.field.fieldName === baseline.fieldName)
  const nativeType = column?.nativeType?.split('(')[0].trim().toUpperCase()
  return nativeType && normalizeFieldType(nativeType) === normalizeFieldType(field.fieldType)
    ? nativeType
    : field.fieldType
}
