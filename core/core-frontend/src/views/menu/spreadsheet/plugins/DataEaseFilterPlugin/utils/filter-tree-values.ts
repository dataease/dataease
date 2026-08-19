import type { SpreadsheetFilterTreePath } from '../../../types/plugin'

const TREE_PATH_SEPARATOR = '-de-'

export const resolveSpreadsheetFilterTreeLevelValue = (
  value: string | number,
  level: number
): string | number => {
  if (typeof value !== 'string') return value

  // 树接口使用累计路径作为节点 ID，实际过滤时每一层只能使用当前层的原始值。
  const pathValues = value.split(TREE_PATH_SEPARATOR)
  return pathValues.length > level ? pathValues[level] : value
}

export const normalizeSpreadsheetFilterTreePath = (
  path: SpreadsheetFilterTreePath
): SpreadsheetFilterTreePath =>
  path.map((item, level) => {
    const value = resolveSpreadsheetFilterTreeLevelValue(item.value, level)
    return value === item.value ? item : { ...item, value }
  })
