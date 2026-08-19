import type { ReplacementField } from '../types'

const IDENTITY_KEYS = ['id', 'fieldId', 'name', 'dataeaseName', 'groupType', 'deType', 'type'] as const

export const migrateFieldReference = <T extends Record<string, any>>(
  sourceConfig: T,
  target: ReplacementField
): T => {
  const migrated = { ...sourceConfig } as Record<string, any>
  const targetValues: Record<(typeof IDENTITY_KEYS)[number], unknown> = {
    id: target.id,
    fieldId: target.id,
    name: target.name,
    dataeaseName: target.dataeaseName ?? target.name,
    groupType: target.groupType,
    deType: target.deType,
    type: target.type
  }

  IDENTITY_KEYS.forEach(key => {
    if (key in sourceConfig && targetValues[key] !== undefined) {
      migrated[key] = targetValues[key]
    }
  })

  return migrated as T
}
