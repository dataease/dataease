import type {
  FilterTree,
  FilterTreeItem,
  SpreadsheetFilterCondition,
  SpreadsheetFilterLinkedField,
  SpreadsheetFilterTreePath,
  SpreadsheetFilterTextSearchValue
} from '../../../types/plugin'
import { normalizeSpreadsheetFilterTextSearchClauses } from './filter-condition-rules'
import { normalizeSpreadsheetFilterTreePath } from './filter-tree-values'

export const buildSpreadsheetTextSearchFilter = (
  condition: SpreadsheetFilterCondition,
  linkedField: SpreadsheetFilterLinkedField,
  rawValue: unknown
): FilterTree | undefined => {
  const value = rawValue as Partial<SpreadsheetFilterTextSearchValue> | undefined
  const clauses = normalizeSpreadsheetFilterTextSearchClauses(
    value?.clauses,
    condition.textSearchConditionType
  ).filter(clause => !!clause.value.trim())
  if (!clauses.length) return

  const items: FilterTreeItem[] = clauses.map(clause => ({
    type: 'item',
    fieldId: linkedField.fieldId,
    term: clause.operator,
    filterType: 'logic',
    value: clause.value
  }))

  return {
    logic: condition.textSearchConditionType === 'or' ? 'or' : 'and',
    items
  }
}

const normalizeTreePaths = (rawValue: unknown): SpreadsheetFilterTreePath[] => {
  if (!Array.isArray(rawValue) || !rawValue.length) return []
  const first = rawValue[0] as Record<string, unknown> | unknown[]
  if (!Array.isArray(first) && first && 'treeFieldId' in first) {
    return [normalizeSpreadsheetFilterTreePath(rawValue as SpreadsheetFilterTreePath)]
  }
  return (rawValue as SpreadsheetFilterTreePath[])
    .filter(path => Array.isArray(path) && path.length)
    .map(normalizeSpreadsheetFilterTreePath)
}

export const buildSpreadsheetTreeFilter = (
  condition: SpreadsheetFilterCondition,
  pluginId: string,
  rawValue: unknown
): FilterTree | undefined => {
  const pathTrees = normalizeTreePaths(rawValue).map(path => {
    const items = path.flatMap(pathItem => {
      const mapping = condition.treeLevelMappings.find(
        item => String(item.treeFieldId) === String(pathItem.treeFieldId)
      )
      const linkedField = mapping?.linkedFields.find(
        field => field.pluginId === pluginId && field.fieldId !== undefined && field.fieldId !== null && field.fieldId !== ''
      )
      if (!linkedField) return []
      return [{
        type: 'item' as const,
        fieldId: linkedField.fieldId,
        term: 'in',
        filterType: 'enum',
        enumValue: [pathItem.value]
      }]
    })
    return items.length ? { logic: 'and' as const, items } : undefined
  }).filter((tree): tree is FilterTree => !!tree)

  if (!pathTrees.length) return
  if (pathTrees.length === 1) return pathTrees[0]
  return {
    logic: 'or',
    items: pathTrees.map(subTree => ({ type: 'tree', subTree }))
  }
}
