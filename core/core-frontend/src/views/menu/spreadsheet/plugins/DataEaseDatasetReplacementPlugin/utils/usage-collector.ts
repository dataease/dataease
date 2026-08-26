import type { FieldItemData, FilterTree } from '../../../types/plugin'
import type { FieldUsageFragment, ReplacementFieldGroup } from '../types'

const asGroup = (value?: string): ReplacementFieldGroup => value === 'q' ? 'q' : 'd'

export const toUsageField = (
  field: Partial<FieldItemData> & { id: string | number; name?: string },
  occurrences = 1
): FieldUsageFragment => ({
  fieldId: String(field.id),
  name: field.name || field.dataeaseName || String(field.id),
  dataeaseName: field.dataeaseName,
  groupType: asGroup(field.groupType),
  deType: field.deType,
  extField: field.extField,
  occurrences,
  metadataComplete: Boolean(field.name && field.groupType && field.deType !== undefined)
})

export const collectFilterTreeFieldIds = (tree?: FilterTree): Array<string | number> => {
  if (!tree?.items?.length) return []
  return tree.items.flatMap(item => [
    ...(item.fieldId !== undefined && item.fieldId !== null ? [item.fieldId] : []),
    ...collectFilterTreeFieldIds(item.subTree)
  ])
}

export const collectFieldReferences = (
  fieldIds: Array<string | number>,
  knownFields: FieldItemData[],
  fallbackGroup: ReplacementFieldGroup = 'd'
): FieldUsageFragment[] => {
  const knownById = new Map(knownFields.map(field => [String(field.id), field]))
  const counts = new Map<string, number>()
  fieldIds.forEach(fieldId => {
    const key = String(fieldId)
    counts.set(key, (counts.get(key) || 0) + 1)
  })

  return Array.from(counts.entries()).map(([fieldId, occurrences]) => {
    const field = knownById.get(fieldId)
    return field
      ? toUsageField(field, occurrences)
      : {
        fieldId,
        name: fieldId,
        groupType: fallbackGroup,
        occurrences,
        metadataComplete: false
      }
  })
}

export const replaceFilterTreeFieldIds = (
  tree: FilterTree | undefined,
  resolveFieldId: (fieldId: string | number) => string | number
): FilterTree | undefined => {
  if (!tree) return tree
  return {
    ...tree,
    items: tree.items?.map(item => ({
      ...item,
      fieldId: item.fieldId === undefined ? undefined : resolveFieldId(item.fieldId),
      subTree: replaceFilterTreeFieldIds(item.subTree, resolveFieldId)
    }))
  }
}
