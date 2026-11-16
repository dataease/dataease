import { filterValueEquals } from '@/utils/componentUtils'
import type { SpreadsheetFilterTreePath } from '../../../types/plugin'
import { normalizeSpreadsheetFilterTreePath } from './filter-tree-values'

export interface SpreadsheetOuterParamTreeNode {
  id: string | number
  children?: SpreadsheetOuterParamTreeNode[]
}

export const matchesSpreadsheetOuterParamOption = (value: unknown, optionFilter?: unknown[]) => {
  if (!optionFilter?.length) return true
  const optionValue = typeof value === 'boolean' ? String(value) : value
  return optionFilter.some(option => {
    const filterValue = typeof option === 'boolean' ? String(option) : option
    return filterValueEquals(filterValue, optionValue)
  })
}

export const filterSpreadsheetOuterParamTree = <T extends { id: string | number; children?: T[] }>(
  nodes: T[],
  optionFilter?: unknown[]
): T[] => {
  if (!optionFilter?.length) return nodes

  return nodes.flatMap(node => {
    // 命中节点保留整棵子树；子节点命中时只保留通向它的祖先路径。
    if (matchesSpreadsheetOuterParamOption(node.id, optionFilter)) return [node]
    const children = filterSpreadsheetOuterParamTree(node.children || [], optionFilter)
    return children.length ? [{ ...node, children }] : []
  })
}

export const isSpreadsheetOuterParamTreePathAllowed = (
  path: SpreadsheetFilterTreePath,
  optionFilter?: unknown[]
): boolean => {
  if (!optionFilter?.length) return true
  if (!path.length) return false

  const pathId = normalizeSpreadsheetFilterTreePath(path)
    .map(item => String(item.value))
    .join('-de-')
  return optionFilter.some(option => {
    const optionId = String(option)
    return (
      filterValueEquals(pathId, optionId) ||
      pathId.startsWith(`${optionId}-de-`) ||
      optionId.startsWith(`${pathId}-de-`)
    )
  })
}
