import { CellType, type Node, type S2CellType, type SpreadSheet } from '@antv/s2'

export function getTableConditionScopes(chartType: string): TableConditionScope[] {
  if (chartType === 'table-pivot') return ['detail', 'subtotal', 'total']
  return []
}

export function getTableConditionScope(
  cell: S2CellType | undefined,
  facet: Pick<SpreadSheet['facet'], 'getRowLeafNodeByIndex' | 'getColLeafNodeByIndex'>
): TableConditionScope {
  const meta = cell?.getMeta()
  const nodes: Node[] = []
  if (cell?.cellType === CellType.DATA_CELL) {
    nodes.push(
      facet.getRowLeafNodeByIndex(meta.rowIndex),
      facet.getColLeafNodeByIndex(meta.colIndex)
    )
  } else if (cell?.cellType === CellType.ROW_CELL || cell?.cellType === CellType.COL_CELL) {
    nodes.push(meta as Node)
  }
  let subtotal = false
  // 指标节点可能挂在汇总节点下；跨行列交叉时总计优先于小计。
  for (let node of nodes) {
    while (node) {
      if (node.isGrandTotals) return 'total'
      if (node.isSubTotals || node.isCollapsed) subtotal = true
      node = node.parent
    }
  }
  return subtotal ? 'subtotal' : 'detail'
}

export function appliesToTableScope(rule: Threshold, scope: TableConditionScope) {
  return rule.applyTo == null || rule.applyTo.includes(scope)
}
