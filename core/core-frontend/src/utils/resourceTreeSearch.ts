import { nextTick, watch, type Ref } from 'vue'

interface TreeStoreNode {
  data?: { id?: string | number; name?: string }
  childNodes?: TreeStoreNode[]
  parent?: TreeStoreNode
  level?: number
  expanded?: boolean
  isLeaf?: boolean
  expand?: () => void
  collapse?: () => void
}

export interface ResourceTreeSearchOptions {
  getName?: (data: any) => string
  canView?: (data: any) => boolean
}

const normalize = (value?: string | null) => (value ?? '').trim()

/**
 * 资源树搜索：
 * - 命中文件夹时保留其下所有有权限的内容
 * - 仅命中资源时保留资源及其路径
 * - 搜索期间展开命中节点与命中资源的祖先，清空后恢复搜索前的展开状态
 */
export const useResourceTreeSearch = (
  treeRef: Ref<any>,
  keyword: Ref<string | null | undefined>,
  options: ResourceTreeSearchOptions = {}
) => {
  const getName = options.getName ?? ((data: any) => data?.name ?? '')
  const canView = options.canView ?? (() => true)

  const matches = (data: any, value: string) => getName(data).toLowerCase().includes(value)

  const hasMatchingDescendant = (node: TreeStoreNode, value: string): boolean =>
    (node.childNodes ?? []).some(
      child => matches(child.data, value) || hasMatchingDescendant(child, value)
    )

  const hasMatchingAncestor = (node: TreeStoreNode, value: string): boolean => {
    let parent = node.parent
    while (parent && parent.level && parent.level > 0) {
      if (matches(parent.data, value)) return true
      parent = parent.parent
    }
    return false
  }

  const filterNode = (value: string, data: any, node?: TreeStoreNode) => {
    const search = normalize(value).toLowerCase()
    if (!search) return true
    if (!canView(data)) return false
    if (matches(data, search)) return true
    if (node?.parent && hasMatchingAncestor(node, search)) return true
    return node ? hasMatchingDescendant(node, search) : false
  }

  const walk = (node: TreeStoreNode, fn: (node: TreeStoreNode) => void) => {
    fn(node)
    for (const child of node.childNodes ?? []) walk(child, fn)
  }

  let expandedSnapshot: Set<string | number> | null = null

  const captureExpansion = () => {
    if (expandedSnapshot) return
    const root = treeRef.value?.store?.root as TreeStoreNode | undefined
    if (!root) return
    const snapshot = new Set<string | number>()
    walk(root, node => {
      if (node.expanded && node.data?.id != null) snapshot.add(node.data.id)
    })
    expandedSnapshot = snapshot
  }

  const applyExpansion = (value?: string | null) => {
    const root = treeRef.value?.store?.root as TreeStoreNode | undefined
    if (!root) return
    const search = normalize(value).toLowerCase()
    if (!search) {
      if (!expandedSnapshot) return
      const snapshot = expandedSnapshot
      expandedSnapshot = null
      walk(root, node => {
        if (node.isLeaf || !node.expand || !node.collapse) return
        if (snapshot.has(node.data?.id)) {
          node.expand()
        } else if (node.expanded) {
          node.collapse()
        }
      })
      return
    }
    walk(root, node => {
      if (node.isLeaf || !node.expand || !node.collapse) return
      if (matches(node.data, search) || hasMatchingDescendant(node, search)) {
        if (!node.expanded) node.expand()
      } else if (node.expanded) {
        node.collapse()
      }
    })
  }

  const applyFilter = (value?: string | null) => {
    const tree = treeRef.value
    if (!tree) return
    // el-tree 的 filter 会自动展开命中节点，快照必须在过滤前采集
    if (normalize(value)) captureExpansion()
    tree.filter(normalize(value))
    nextTick(() => applyExpansion(value))
  }

  watch(keyword, value => applyFilter(value))

  return { filterNode, applyFilter }
}
