export interface PermissionTreeRow {
  id: string | number
  children?: PermissionTreeRow[]
  hidden?: boolean
  dataHidden?: boolean
  authRowKey?: string
  authParentKey?: string | null
}

// VXE builds its own tree without modifying the children used by permission cascading.
export const permissionTreeConfig = {
  transform: true,
  reserve: true,
  rowField: 'authRowKey',
  parentField: 'authParentKey',
  childrenField: 'authTableChildren',
  mapChildrenField: 'authTableMapChildren',
  indent: 20
}

export const flattenPermissionRows = <T extends PermissionTreeRow>(tree: T[] = []): T[] => {
  const rows: T[] = []
  const visit = (nodes: T[], path: string, visibleParent: string | null) => {
    nodes.forEach((row, index) => {
      // A subject can appear in several organizations. Business IDs remain unchanged.
      const key = `${path}/${index}:${row.id}`
      const visible = !row.hidden && !row.dataHidden
      // Hidden ancestors also need stable keys so expansion can be restored after clearing search.
      row.authRowKey = key
      row.authParentKey = visibleParent
      if (visible) {
        rows.push(row)
      }
      if (row.children?.length) {
        visit(row.children as T[], key, visible ? key : visibleParent)
      }
    })
  }
  visit(tree, '', null)
  return rows
}

export const permissionAncestorIds = (
  tree: PermissionTreeRow[],
  permissionIds: Set<string | number>
): (string | number)[] => {
  const expanded = new Set<string | number>()
  const visit = (nodes: PermissionTreeRow[]): boolean => {
    let matched = false
    nodes.forEach(row => {
      const childMatched = row.children?.length ? visit(row.children) : false
      if (childMatched) expanded.add(row.id)
      matched = permissionIds.has(row.id) || childMatched || matched
    })
    return matched
  }
  visit(tree)
  return [...expanded]
}

// Reset only permission state, preserving filtering and VXE's structural metadata.
export const resetPermissionRows = (rows: PermissionTreeRow[]) => {
  rows.forEach(row => {
    for (const key of Object.keys(row)) {
      if (/^(weight|ext|level\d+|value\d+|independent\d+)$/.test(key)) {
        delete row[key]
      }
    }
    row.dataHidden = false
    if (row.children?.length) resetPermissionRows(row.children)
  })
}
