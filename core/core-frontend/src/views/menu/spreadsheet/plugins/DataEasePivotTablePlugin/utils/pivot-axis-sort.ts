import type { FieldItemData } from '../../../types/plugin'

export interface PivotDimensionTuple {
  values: unknown[]
}

interface PivotAxisTreeNode {
  value: unknown
  originalOrder: number
  children: Map<string, PivotAxisTreeNode>
  tuple?: PivotDimensionTuple
}

type PivotAxisNodeComparator = (
  left: PivotAxisTreeNode,
  right: PivotAxisTreeNode
) => number

const NUMERIC_FIELD_TYPES = new Set([2, 3, 4])

export function sortPivotDimensionTuples(
  fields: FieldItemData[],
  tuples: PivotDimensionTuple[]
): PivotDimensionTuple[] {
  if (!fields.length || tuples.length <= 1) {
    return tuples
  }

  const rootChildren = new Map<string, PivotAxisTreeNode>()
  let originalOrder = 0

  // 即使父层级未配置排序，也必须按完整父路径构树，避免另一轴的扁平顺序拆散同一父节点。
  for (const tuple of tuples) {
    let children = rootChildren
    tuple.values.forEach((value, level) => {
      const key = axisValueKey(value)
      let node = children.get(key)
      if (!node) {
        node = {
          value,
          originalOrder: originalOrder++,
          children: new Map()
        }
        children.set(key, node)
      }
      if (level === fields.length - 1) {
        node.tuple = tuple
      }
      children = node.children
    })
  }

  const comparators = fields.map(createNodeComparator)
  const result: PivotDimensionTuple[] = []
  flattenAxisTree(rootChildren, 0, comparators, result)
  return result
}

function flattenAxisTree(
  children: Map<string, PivotAxisTreeNode>,
  level: number,
  comparators: PivotAxisNodeComparator[],
  result: PivotDimensionTuple[]
): void {
  const nodes = Array.from(children.values())
  nodes.sort(comparators[level])

  for (const node of nodes) {
    if (level === comparators.length - 1) {
      if (node.tuple) {
        result.push(node.tuple)
      }
      continue
    }
    flattenAxisTree(node.children, level + 1, comparators, result)
  }
}

function createNodeComparator(field: FieldItemData): PivotAxisNodeComparator {
  if (field.sort === 'custom_sort') {
    return createCustomSortComparator(field.customSort || [])
  }
  if (field.sort !== 'asc' && field.sort !== 'desc') {
    return compareOriginalOrder
  }

  const direction = field.sort === 'asc' ? 1 : -1
  return (left, right) => {
    const compared = compareFieldValues(left.value, right.value, field)
    if (compared === 0) {
      return compareOriginalOrder(left, right)
    }
    return compared * direction
  }
}

function createCustomSortComparator(
  customSort: Array<string | number>
): PivotAxisNodeComparator {
  const rankMap = new Map<string, number>()
  customSort.forEach((value, index) => {
    const key = customSortValueKey(value)
    if (!rankMap.has(key)) {
      rankMap.set(key, index)
    }
  })

  return (left, right) => {
    const leftRank = rankMap.get(customSortValueKey(left.value))
    const rightRank = rankMap.get(customSortValueKey(right.value))
    const leftMatched = leftRank !== undefined
    const rightMatched = rightRank !== undefined

    if (leftMatched && rightMatched) {
      const compared = leftRank - rightRank
      return compared === 0 ? compareOriginalOrder(left, right) : compared
    }
    if (leftMatched) {
      return -1
    }
    if (rightMatched) {
      return 1
    }
    return compareOriginalOrder(left, right)
  }
}

function compareFieldValues(
  left: unknown,
  right: unknown,
  field: FieldItemData
): number {
  const leftEmpty = isEmptyValue(left)
  const rightEmpty = isEmptyValue(right)
  if (leftEmpty || rightEmpty) {
    if (leftEmpty && rightEmpty) {
      return 0
    }
    return leftEmpty ? -1 : 1
  }

  if (NUMERIC_FIELD_TYPES.has(field.deType ?? -1)) {
    const leftNumber = Number(left)
    const rightNumber = Number(right)
    const leftIsFinite = Number.isFinite(leftNumber)
    const rightIsFinite = Number.isFinite(rightNumber)
    if (leftIsFinite && rightIsFinite) {
      if (leftNumber === rightNumber) {
        return 0
      }
      return leftNumber < rightNumber ? -1 : 1
    }
    // 数值与异常文本先固定分组，避免混用两套比较规则形成非传递排序。
    if (leftIsFinite !== rightIsFinite) {
      return leftIsFinite ? -1 : 1
    }
  }

  return String(left).localeCompare(String(right))
}

function compareOriginalOrder(
  left: PivotAxisTreeNode,
  right: PivotAxisTreeNode
): number {
  return left.originalOrder - right.originalOrder
}

function isEmptyValue(value: unknown): boolean {
  return value == null || value === ''
}

function axisValueKey(value: unknown): string {
  return JSON.stringify([value == null ? null : value])
}

function customSortValueKey(value: unknown): string {
  return String(value ?? '').toLowerCase()
}
