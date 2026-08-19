import type {
  FieldUsage,
  ReplacementField
} from '../types'

type ComparableField = Pick<ReplacementField, 'name' | 'groupType' | 'deType' | 'type'>

const getTypeFamily = (field: Pick<ComparableField, 'deType' | 'type'>): string => {
  if (field.deType !== undefined && field.deType !== null) {
    const deType = Number(field.deType)
    if ([2, 3, 4].includes(deType)) return 'number'
    if ([0, 7].includes(deType)) return 'text'
    if (deType === 1) return 'date'
    if (deType === 5) return 'location'
    return `de-type:${deType}`
  }

  return `type:${String(field.type ?? '').trim().toLowerCase()}`
}

export const isSameFieldGroup = (
  source: Pick<ComparableField, 'groupType'>,
  target: Pick<ComparableField, 'groupType'>
): boolean => source.groupType === target.groupType

export const isCompatibleFieldType = (
  source: Pick<ComparableField, 'deType' | 'type'>,
  target: Pick<ComparableField, 'deType' | 'type'>
): boolean => getTypeFamily(source) === getTypeFamily(target)

export const isFieldCompatible = (
  source: ComparableField,
  target: ComparableField
): boolean => isSameFieldGroup(source, target) && isCompatibleFieldType(source, target)

export const filterCompatibleFields = (
  source: FieldUsage,
  candidates: ReplacementField[]
): ReplacementField[] => candidates.filter(candidate => isFieldCompatible(source, candidate))

export const findAutoMatchedField = (
  source: FieldUsage,
  candidates: ReplacementField[]
): ReplacementField | undefined => {
  const matches = candidates.filter(candidate =>
    candidate.name === source.name && isFieldCompatible(source, candidate)
  )
  return matches.length === 1 ? matches[0] : undefined
}
