import type { ReplacementField } from '../types'

const REFERENCE_KEYS = ['id', 'fieldId'] as const
const FIELD_METADATA_KEYS = [
  'name',
  'dataeaseName',
  'groupType',
  'deType',
  'deExtractType',
  'extField',
  'type',
  'originName',
  'dateFormat',
  'params'
] as const

export const migrateFieldReference = <T extends Record<string, any>>(
  sourceConfig: T,
  target: ReplacementField
): T => {
  const migrated = { ...sourceConfig } as Record<string, any>

  REFERENCE_KEYS.forEach(key => {
    if (key in sourceConfig) {
      migrated[key] = target.id
    }
  })

  // 字段来源相关属性必须以目标字段为准，避免计算字段标识缺失或旧扩展字段属性残留。
  FIELD_METADATA_KEYS.forEach(key => {
    const targetValue = key === 'dataeaseName'
      ? target.dataeaseName ?? target.name
      : target[key]
    if (targetValue === undefined) {
      delete migrated[key]
      return
    }
    migrated[key] = targetValue
  })

  return migrated as T
}
