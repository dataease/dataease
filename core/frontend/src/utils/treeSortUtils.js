import { deepCopy } from '@/components/canvas/utils/utils'

export default function treeSort(tree, sortType) {
  const result = deepCopy(tree)
  sortPer(result, sortType)
  result.forEach(node => {
    if (node.children && node.children.length > 0) {
      sortPer(node.children, sortType)
    }
  })
  return result
}

export function sortPer(subTree, sortType) {
  if (sortType === 'name_desc') {
    subTree.sort((a, b) => b.name.localeCompare(a.name, 'zh-Hans-CN', { sensitivity: 'accent' }))
  } else if (sortType === 'name_asc') {
    subTree.sort((a, b) => a.name.localeCompare(b.name, 'zh-Hans-CN', { sensitivity: 'accent' }))
  } else if (sortType === 'time_asc') {
    return subTree.reverse()
  } else {
    return subTree
  }
}
